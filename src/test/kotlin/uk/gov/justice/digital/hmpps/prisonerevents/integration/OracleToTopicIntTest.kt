package uk.gov.justice.digital.hmpps.prisonerevents.integration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.jms.ConnectionFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mockingDetails
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.util.JsonPathExpectationsHelper
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offender
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBookings
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPerson
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPersons
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactRestriction
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactRestrictions
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offenders
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Person
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Persons
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import uk.gov.justice.hmpps.sqs.HmppsTopicFactory
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.net.SocketException
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture

@TestConfiguration
class SnsConfig(private val hmppsTopicFactory: HmppsTopicFactory) {
  @Bean("prisoneventtopic-sns-client")
  fun topicSnsClient(
    hmppsSqsProperties: HmppsSqsProperties,
  ): SnsAsyncClient =
    hmppsTopicFactory.createSnsAsyncClient(topicId = "prisoneventtopic", topicConfig = HmppsSqsProperties.TopicConfig(arn = hmppsSqsProperties.topics["prisoneventtopic"]!!.arn), hmppsSqsProperties = hmppsSqsProperties)
}

@Import(SnsConfig::class)
class OracleToTopicIntTest : IntegrationTestBase() {

  @SpyBean
  @Qualifier("prisoneventtopic-sns-client")
  protected lateinit var snsClient: SnsAsyncClient

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var retryConnectionFactory: ConnectionFactory

  @Autowired
  private lateinit var sqlRepository: SqlRepository

  private val jmsTemplate by lazy {
    JmsTemplate(retryConnectionFactory).also { it.defaultDestinationName = FULL_QUEUE_NAME }
  }

  internal val prisonEventQueue by lazy { hmppsQueueService.findByQueueId("prisoneventtestqueue") as HmppsQueue }
  internal val prisonEventQueueSqsClient by lazy { prisonEventQueue.sqsClient }
  internal val prisonEventQueueUrl by lazy { prisonEventQueue.queueUrl }

  @BeforeEach
  fun setup() {
    reset(snsClient)
    purgeQueues()
  }

  @Nested
  inner class Consume {

    @Test
    fun `will consume a prison offender events message`() {
      simulateTrigger()

      awaitQueueSizeToBe(1)

      val receiveMessageResult = prisonEventQueueSqsClient.receiveMessage(
        ReceiveMessageRequest.builder().queueUrl(prisonEventQueueUrl).build(),
      )
      with(receiveMessageResult.get().messages().first()) {
        assertThat(body().contains("""\"nomisEventType\":\"OFF_RECEP_OASYS\"""")).isTrue
        assertThat(body().contains("""\"eventType\":\"OFFENDER_MOVEMENT-RECEPTION\"""")).isTrue
        assertThat(body().contains("""\"bookingId\":1234567""")).isTrue
        assertThat(body().contains("""\"movementSeq\":4""")).isTrue
      }
    }

    @Test
    fun `will consume after one publish failure`() {
      // Sabotage the topic temporarily - publish will fail
      sabotageTopic()
      simulateTrigger()

      awaitPublishTries(1)

      assertThat(getNumberOfMessagesCurrentlyOnPrisonEventQueue()).isEqualTo(0)

      fixTopic() // Now  publish will succeed

      awaitPublishTries(2)

      awaitQueueSizeToBe(1)
    }

    @Test
    fun `will move to exception queue after repeated publish failures`() {
      sabotageTopic()

      simulateTrigger()

      awaitPublishTries(4)

      assertThat(getNumberOfMessagesCurrentlyOnPrisonEventQueue()).isEqualTo(0)

      // After 5 attempts, the message should be on the exception queue
      awaitExceptionQueueSizeToBe(1)
    }
  }

  @Nested
  inner class RetryEndpoint {
    @Test
    fun `Retry exception messages when none present`() {
      webTestClient.put().uri("/housekeeping")
        .headers(setAuthorisation(roles = emptyList()))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Retry exception messages when messages present`() {
      sabotageTopic()
      repeat(2) { simulateTrigger() }
      awaitExceptionQueueSizeToBe(2)
      fixTopic()

      webTestClient
        .put().uri("/housekeeping")
        .headers(setAuthorisation(roles = emptyList()))
        .exchange()
        .expectStatus().isOk

      awaitExceptionQueueSizeToBe(0)
      // Messages have been moved from exception queue to normal queue
      awaitQueueSizeToBe(2)
      // Messages arrived at the topic and were sent to subscribed queue
    }

    @Test
    fun `Retry exception messages when messages present and page size set`() {
      sabotageTopic()
      repeat(2) { simulateTrigger() }
      awaitExceptionQueueSizeToBe(2)
      fixTopic()

      webTestClient
        .put().uri("/housekeeping?pageSize=1")
        .headers(setAuthorisation(roles = emptyList()))
        .exchange()
        .expectStatus().isOk

      awaitExceptionQueueSizeToBe(1)
      awaitQueueSizeToBe(1)

      webTestClient
        .put().uri("/housekeeping?pageSize=1")
        .headers(setAuthorisation(roles = emptyList()))
        .exchange()
        .expectStatus().isOk

      awaitExceptionQueueSizeToBe(0)
      awaitQueueSizeToBe(2)
    }
  }

  @Nested
  inner class DequeueEndpoint {
    @Test
    fun `must have valid token`() {
      webTestClient.delete().uri("/exceptions/XTAG_DPS?onlyBefore=2023-06-01")
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `must have correct role`() {
      webTestClient.delete().uri("/exceptions/XTAG_DPS?onlyBefore=2023-06-01")
        .headers(setAuthorisation(roles = listOf("ROLE_MIGRATE_BANANAS")))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Delete exception messages`() {
      sabotageTopic()
      repeat(1) { simulateTrigger() }
      awaitExceptionQueueSizeToBe(1)
      fixTopic()

      webTestClient
        .delete().uri("/exceptions/doesntexist?onlyBefore=2023-06-01")
        .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
        .exchange()
        .expectStatus().isOk

      assertThat(getNumberOfMessagesCurrentlyOnExceptionQueue()).isEqualTo(1)

      webTestClient
        .delete().uri("/exceptions/XTAG_DPS?onlyBefore=2023-08-01")
        .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
        .exchange()
        .expectStatus().isOk

      assertThat(getNumberOfMessagesCurrentlyOnExceptionQueue()).isEqualTo(1)

      webTestClient
        .delete().uri("/exceptions/XTAG_DPS?onlyBefore=${LocalDate.now().plusDays(1)}")
        .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
        .exchange()
        .expectStatus().isOk

      assertThat(getNumberOfMessagesCurrentlyOnExceptionQueue()).isEqualTo(0)
    }
  }

  @Nested
  inner class EventMappings {
    @Suppress("SqlWithoutWhere")
    @AfterEach
    fun tearDown() {
      transaction {
        OffenderContactRestrictions.deleteAll()
        OffenderContactPersons.deleteAll()
        Persons.deleteAll()
        OffenderBookings.deleteAll()
        Offenders.deleteAll()
      }
    }

    @Nested
    @DisplayName("OFF_PERS_RESTRICTS-UPDATED -> PERSON_RESTRICTION-UPSERTED")
    inner class PersonRestrictionUpserted {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private var bookingId = 0L
      private var personId = 0L
      private val offenderNo = "A1234AA"
      private var restrictionId = 0L
      private var contactPersonId = 0L

      @BeforeEach
      fun setUp() {
        lateinit var booking: OffenderBooking
        lateinit var person: Person
        lateinit var offenderContactPerson: OffenderContactPerson
        lateinit var restriction: OffenderContactRestriction
        transaction {
          this.addLogger(StdOutSqlLogger)

          val offender = Offender.new {
            this.offenderNo = this@PersonRestrictionUpserted.offenderNo
            this.idSource = "source"
            this.lastName = "SMITH"
            this.sexCode = "M"
            this.lastNameKey = "key"
          }
          booking = OffenderBooking.new {
            this.offender = offender
            this.rootOffender = offender
            this.inOutStatus = "IN"
            this.youthAdultCode = "A"
            this.sequence = 1
          }
          person = Person.new {
            this.firstName = "SARAH"
            this.lastName = "JENKINS"
          }
          offenderContactPerson = OffenderContactPerson.new {
            this.offenderBooking = booking
            this.person = person
            this.contactType = "S"
            this.relationshipType = "BRO"
          }
          restriction = OffenderContactRestriction.new {
            this.offenderContactPerson = offenderContactPerson
            this.restrictionType = "BAN"
            this.effectiveDate = LocalDate.parse("2022-08-15")
          }
        }

        bookingId = booking.bookingId.value
        personId = person.personId.value
        contactPersonId = offenderContactPerson.contactId.value
        restrictionId = restriction.restrictionId.value

        simulateTrigger(
          nomisEventType = "OFF_PERS_RESTRICTS-UPDATED",
          "p_offender_contact_person_id" to contactPersonId,
          "p_offender_person_restrict_id" to restrictionId,
          "p_restriction_type" to "RESTRICTED",
          "p_restriction_effective_date" to "2023-01-03",
          "p_restriction_expiry_date" to "2029-01-03",
          "p_authorized_staff_id" to 1234,
          "p_entered_staff_id" to 1138583,
          "p_comment_text" to "some comment",
        )

        prisonerEvent = awaitMessage()
      }

      @Test
      fun `will map to PERSON_RESTRICTION-UPSERTED`() {
        with(prisonerEvent.message) {
          assertJsonPath("eventType", "PERSON_RESTRICTION-UPSERTED")
          assertJsonPath("comment", "some comment")
          assertJsonPath("contactPersonId", "$contactPersonId")
          assertJsonPath("personId", "$personId")
          assertJsonPath("effectiveDate", "2023-01-03")
          assertJsonPath("expiryDate", "2029-01-03")
          assertJsonPath("enteredById", "1138583")
          assertJsonPath("eventType", "PERSON_RESTRICTION-UPSERTED")
          assertJsonPath("nomisEventType", "OFF_PERS_RESTRICTS-UPDATED")
          assertJsonPath("offenderIdDisplay", offenderNo)
          assertJsonPath("offenderPersonRestrictionId", "$restrictionId")
          assertJsonPath("restrictionType", "RESTRICTED")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("PERSON_RESTRICTION-UPSERTED")
        assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
      }
    }
  }

  private fun sabotageTopic() {
    doReturn(CompletableFuture.failedFuture<PublishResponse>(SocketException("Test exception"))).whenever(snsClient)
      .publish(any<PublishRequest>())
  }

  private fun fixTopic() {
    doCallRealMethod().whenever(snsClient).publish(any<PublishRequest>())
  }

  private fun simulateTrigger() {
    jmsTemplate.send { session ->
      session.createMapMessage().apply {
        jmsType = "OFF_RECEP_OASYS"
        setLong("p_offender_book_id", 1234567L)
        setInt("p_movement_seq", 4)
        setString("eventType", "OFF_RECEP_OASYS")
      }
    }
  }

  private fun simulateTrigger(nomisEventType: String, vararg attributes: Pair<String, Any>) {
    jmsTemplate.send { session ->
      session.createMapMessage().apply {
        jmsType = nomisEventType
        attributes.forEach { (key, value) -> setString(key, value.toString()) }
      }
    }
  }

  fun purgeQueues() {
    prisonEventQueueSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(prisonEventQueueUrl).build())
    sqlRepository.purgeExceptionQueue()
    awaitQueueSizeToBe(0)
  }

  val maxWait: Duration = Duration.ofSeconds(30)
  val poll: Duration = Duration.ofSeconds(1)

  fun awaitPublishTries(count: Int) {
    await.atMost(maxWait).pollInterval(poll) untilCallTo {
      mockingDetails(snsClient).invocations.filter { it.method.name == "publish" }.size
    } matches {
      log.info("Number of publish calls: $it")
      it == count
    }
  }

  fun awaitQueueSizeToBe(count: Int) {
    await.atMost(maxWait)
      .pollInterval(poll) untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == count }
  }

  fun awaitExceptionQueueSizeToBe(count: Int) {
    await.atMost(maxWait)
      .pollInterval(poll) untilCallTo { getNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == count }
  }

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int =
    prisonEventQueueSqsClient.countAllMessagesOnQueue(prisonEventQueueUrl).get()
      .also { log.info("Number of messages on prison queue: $it") }

  fun getNumberOfMessagesCurrentlyOnExceptionQueue() =
    sqlRepository.getExceptionMessageIds(QUEUE_NAME).size
      .also { log.info("Number of messages on exception queue: $it") }

  fun awaitMessage(): PrisonerEventMessage {
    awaitQueueSizeToBe(1)

    val queueMessage = prisonEventQueueSqsClient.receiveMessage(
      ReceiveMessageRequest.builder().queueUrl(prisonEventQueueUrl).build(),
    ).get().messages().first().body()

    val sqsMessage: SQSMessage = objectMapper.readValue(queueMessage, SQSMessage::class.java)
    return PrisonerEventMessage(
      message = sqsMessage.Message,
      publishedAt = OffsetDateTime.parse(sqsMessage.MessageAttributes.publishedAt.Value),
      eventType = sqsMessage.MessageAttributes.eventType.Value,
    )
  }
}

internal data class SQSMessage(val Message: String, val MessageId: String, val MessageAttributes: MessageAttributes)
internal data class MessageAttributes(val publishedAt: TypeValuePair, val eventType: TypeValuePair)
internal data class TypeValuePair(val Value: String, val Type: String)

data class PrisonerEventMessage(
  val message: String,
  val publishedAt: OffsetDateTime,
  val eventType: String,
)

private fun String.assertJsonPath(path: String, expectedValue: Any) = JsonPathExpectationsHelper(path).assertValue(this, expectedValue)
