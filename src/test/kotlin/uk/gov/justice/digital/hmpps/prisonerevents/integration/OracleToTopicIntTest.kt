package uk.gov.justice.digital.hmpps.prisonerevents.integration

import jakarta.jms.ConnectionFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.util.JsonPathExpectationsHelper
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.prisonerevents.builders.build
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.MergeTransaction
import uk.gov.justice.digital.hmpps.prisonerevents.repository.MergeTransactions
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offender
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBookings
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderCharge
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPerson
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPersons
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactRestriction
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactRestrictions
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderExternalMovement
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderExternalMovements
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offenders
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Person
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Persons
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsSqsProperties
import uk.gov.justice.hmpps.sqs.HmppsTopicFactory
import uk.gov.justice.hmpps.sqs.SnsMessage
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.net.SocketException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture

@TestConfiguration
class SnsConfig(private val hmppsTopicFactory: HmppsTopicFactory) {
  @Bean("prisoneventtopic-sns-client")
  fun topicSnsClient(
    hmppsSqsProperties: HmppsSqsProperties,
  ): SnsAsyncClient = hmppsTopicFactory.createSnsAsyncClient(topicId = "prisoneventtopic", topicConfig = HmppsSqsProperties.TopicConfig(arn = hmppsSqsProperties.topics["prisoneventtopic"]!!.arn), hmppsSqsProperties = hmppsSqsProperties)
}

@Import(SnsConfig::class)
class OracleToTopicIntTest(@Autowired private val jsonMapper: JsonMapper) : IntegrationTestBase() {

  @MockitoSpyBean
  @Qualifier("prisoneventtopic-sns-client")
  protected lateinit var snsClient: SnsAsyncClient

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

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
        OffenderExternalMovements.deleteAll()
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
      private val offenderNo = "A1234AA"
      private lateinit var person: Person
      private lateinit var offenderContactPerson: OffenderContactPerson
      private lateinit var restriction: OffenderContactRestriction

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          person = Person.build {}
          Offender.build {
            offenderNo = this@PersonRestrictionUpserted.offenderNo
          }.also {
            OffenderBooking.build(offender = it).also {
              offenderContactPerson = OffenderContactPerson.build(offenderBooking = it, person = person)
            }
          }
          restriction = OffenderContactRestriction.build(offenderContactPerson = offenderContactPerson)
        }

        simulateTrigger(
          nomisEventType = "OFF_PERS_RESTRICTS-UPDATED",
          "p_offender_contact_person_id" to offenderContactPerson.contactId.value,
          "p_offender_person_restrict_id" to restriction.restrictionId.value,
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
          assertJsonPath("nomisEventType", "OFF_PERS_RESTRICTS-UPDATED")
          assertDoesNotHaveJsonPath("comment")
          assertJsonPath("contactPersonId", "${offenderContactPerson.contactId.value}")
          assertJsonPath("personId", "${person.personId.value}")
          assertJsonPath("effectiveDate", "2023-01-03")
          assertJsonPath("expiryDate", "2029-01-03")
          assertJsonPath("enteredById", "1138583")
          assertJsonPath("offenderIdDisplay", offenderNo)
          assertJsonPath("offenderPersonRestrictionId", "${restriction.restrictionId.value}")
          assertJsonPath("restrictionType", "RESTRICTED")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("PERSON_RESTRICTION-UPSERTED")
        assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
      }
    }

    @Nested
    @DisplayName("OFFENDER_CONTACT-INSERTED -> OFFENDER_CONTACT-INSERTED")
    inner class OffenderContactInserted {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val offenderNo = "A1234AA"
      private lateinit var person: Person
      private lateinit var booking: OffenderBooking
      private lateinit var offenderContactPerson: OffenderContactPerson

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          person = Person.build {}
          Offender.build {
            offenderNo = this@OffenderContactInserted.offenderNo
          }.also {
            booking = OffenderBooking.build(offender = it).also {
              offenderContactPerson = OffenderContactPerson.build(offenderBooking = it, person = person, createUserName = "M.MARGE")
            }
          }
        }
      }

      @Nested
      inner class HappyPath {
        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "OFFENDER_CONTACT-INSERTED",
            "p_offender_contact_person_id" to offenderContactPerson.id.value,
            "p_person_id" to person.id.value,
            "p_offender_book_id" to booking.id.value,
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_approved_visitor_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_active_flag" to "Y",
            "p_next_of_kin_flag" to "Y",
            "p_offender_id_display" to offenderNo,
            "p_relationship_type" to "CA",
            "p_contact_type" to "O",
            "p_audit_module_name" to "OCDPERSO",
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to OFFENDER_CONTACT-INSERTED`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "OFFENDER_CONTACT-INSERTED")
            assertJsonPath("nomisEventType", "OFFENDER_CONTACT-INSERTED")
            assertJsonPath("contactId", "${offenderContactPerson.id.value}")
            assertJsonPath("personId", "${person.personId.value}")
            assertJsonPath("bookingId", "${booking.id.value}")
            assertJsonPath("approvedVisitor", "false")
            assertJsonPath("offenderIdDisplay", offenderNo)
            assertJsonPath("username", "M.MARGE")
            assertJsonPath("auditModuleName", "OCDPERSO")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("OFFENDER_CONTACT-INSERTED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      inner class HappyPathRecordDeleted {
        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "OFFENDER_CONTACT-INSERTED",
            "p_offender_contact_person_id" to 999,
            "p_person_id" to person.id.value,
            "p_offender_book_id" to booking.id.value,
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_approved_visitor_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_active_flag" to "Y",
            "p_next_of_kin_flag" to "Y",
            "p_offender_id_display" to offenderNo,
            "p_relationship_type" to "CA",
            "p_contact_type" to "O",
            "p_audit_module_name" to "OCDPERSO",
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to OFFENDER_CONTACT-INSERTED even if record has been deleted`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "OFFENDER_CONTACT-INSERTED")
            assertJsonPath("nomisEventType", "OFFENDER_CONTACT-INSERTED")
            assertJsonPath("contactId", "999")
            assertJsonPath("personId", "${person.personId.value}")
            assertJsonPath("bookingId", "${booking.id.value}")
            assertJsonPath("approvedVisitor", "false")
            assertJsonPath("offenderIdDisplay", offenderNo)
            assertDoesNotHaveJsonPath("username")
            assertJsonPath("auditModuleName", "OCDPERSO")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("OFFENDER_CONTACT-INSERTED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }
    }

    @Nested
    @DisplayName("OFFENDER_CONTACT-UPDATED -> OFFENDER_CONTACT-UPDATED")
    inner class OffenderContactUpdated {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val offenderNo = "A1234AA"
      private lateinit var person: Person
      private lateinit var booking: OffenderBooking
      private lateinit var offenderContactPerson: OffenderContactPerson

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          person = Person.build {}
          Offender.build {
            offenderNo = this@OffenderContactUpdated.offenderNo
          }.also {
            booking = OffenderBooking.build(offender = it).also {
              offenderContactPerson = OffenderContactPerson.build(offenderBooking = it, person = person, modifyUserName = "M.MARGE")
            }
          }
        }
      }

      @Nested
      inner class HappyPath {
        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "OFFENDER_CONTACT-UPDATED",
            "p_offender_contact_person_id" to offenderContactPerson.id.value,
            "p_person_id" to person.id.value,
            "p_offender_book_id" to booking.id.value,
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_approved_visitor_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_active_flag" to "Y",
            "p_next_of_kin_flag" to "Y",
            "p_offender_id_display" to offenderNo,
            "p_relationship_type" to "CA",
            "p_contact_type" to "O",
            "p_audit_module_name" to "OCDPERSO",
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to OFFENDER_CONTACT-UPDATED`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "OFFENDER_CONTACT-UPDATED")
            assertJsonPath("nomisEventType", "OFFENDER_CONTACT-UPDATED")
            assertJsonPath("contactId", "${offenderContactPerson.id.value}")
            assertJsonPath("personId", "${person.personId.value}")
            assertJsonPath("bookingId", "${booking.id.value}")
            assertJsonPath("approvedVisitor", "false")
            assertJsonPath("offenderIdDisplay", offenderNo)
            assertJsonPath("username", "M.MARGE")
            assertJsonPath("auditModuleName", "OCDPERSO")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("OFFENDER_CONTACT-UPDATED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      inner class HappyPathRecordDeleted {
        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "OFFENDER_CONTACT-UPDATED",
            "p_offender_contact_person_id" to 999,
            "p_person_id" to person.id.value,
            "p_offender_book_id" to booking.id.value,
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_approved_visitor_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_active_flag" to "Y",
            "p_next_of_kin_flag" to "Y",
            "p_offender_id_display" to offenderNo,
            "p_relationship_type" to "CA",
            "p_contact_type" to "O",
            "p_audit_module_name" to "OCDPERSO",
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to OFFENDER_CONTACT-UPDATED even if record has been deleted`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "OFFENDER_CONTACT-UPDATED")
            assertJsonPath("nomisEventType", "OFFENDER_CONTACT-UPDATED")
            assertJsonPath("contactId", "999")
            assertJsonPath("personId", "${person.personId.value}")
            assertJsonPath("bookingId", "${booking.id.value}")
            assertJsonPath("approvedVisitor", "false")
            assertJsonPath("offenderIdDisplay", offenderNo)
            assertDoesNotHaveJsonPath("username")
            assertJsonPath("auditModuleName", "OCDPERSO")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("OFFENDER_CONTACT-UPDATED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }
    }

    @Nested
    @DisplayName("OFF_PERS_RESTRICTS-UPDATED -> PERSON_RESTRICTION-DELETED")
    inner class PersonRestrictionDeleted {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val offenderNo = "A1234AA"
      private lateinit var person: Person
      private lateinit var offenderContactPerson: OffenderContactPerson

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          person = Person.build {}
          Offender.build {
            offenderNo = this@PersonRestrictionDeleted.offenderNo
          }.also {
            OffenderBooking.build(offender = it).also {
              offenderContactPerson = OffenderContactPerson.build(offenderBooking = it, person = person)
            }
          }
        }

        simulateTrigger(
          nomisEventType = "OFF_PERS_RESTRICTS-UPDATED",
          "p_offender_contact_person_id" to offenderContactPerson.contactId.value,
          "p_offender_person_restrict_id" to 9999,
          "p_restriction_type" to "RESTRICTED",
          "p_delete_flag" to "Y",
          "p_restriction_effective_date" to "2023-01-03",
          "p_restriction_expiry_date" to "2029-01-03",
          "p_authorized_staff_id" to 1234,
          "p_entered_staff_id" to 1138583,
          "p_comment_text" to "some comment",
        )

        prisonerEvent = awaitMessage()
      }

      @Test
      fun `will map to PERSON_RESTRICTION-DELETED`() {
        with(prisonerEvent.message) {
          assertJsonPath("eventType", "PERSON_RESTRICTION-DELETED")
          assertJsonPath("nomisEventType", "OFF_PERS_RESTRICTS-UPDATED")
          assertDoesNotHaveJsonPath("comment")
          assertJsonPath("contactPersonId", "${offenderContactPerson.contactId.value}")
          assertJsonPath("personId", "${person.personId.value}")
          assertJsonPath("effectiveDate", "2023-01-03")
          assertJsonPath("expiryDate", "2029-01-03")
          assertJsonPath("enteredById", "1138583")
          assertJsonPath("offenderIdDisplay", offenderNo)
          assertJsonPath("offenderPersonRestrictionId", "9999")
          assertJsonPath("restrictionType", "RESTRICTED")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("PERSON_RESTRICTION-DELETED")
        assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
      }
    }

    @Nested
    @DisplayName("M1_RESULT -> EXTERNAL_MOVEMENT_RECORD-INSERTED")
    inner class ExternalMovementInserted {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val offenderNo = "A1234AA"
      private lateinit var booking: OffenderBooking
      private lateinit var movement: OffenderExternalMovement

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          Offender.build {
            offenderNo = this@ExternalMovementInserted.offenderNo
          }.also {
            booking = OffenderBooking.build(offender = it).also { booking ->
              movement = OffenderExternalMovement.build(offenderBooking = booking, sequence = 11) {
                direction = "IN"
                type = "TAP"
                date = LocalDate.parse("2024-08-23")
                time = LocalDateTime.parse("2024-08-23T15:05:00")
              }
            }
          }
        }
        simulateTrigger(
          nomisEventType = "M1_RESULT",
          "p_record_deleted" to "N",
          "p_movement_seq" to 11,
          "p_offender_book_id" to booking.id.value,
        )

        prisonerEvent = awaitMessage()
      }

      @Test
      fun `will map to PEXTERNAL_MOVEMENT_RECORD-INSERTED`() {
        with(prisonerEvent.message) {
          assertJsonPath("eventType", "EXTERNAL_MOVEMENT_RECORD-INSERTED")
          assertJsonPath("nomisEventType", "M1_RESULT")
          assertJsonPath("movementDateTime", "2024-08-23T15:05:00")
          assertJsonPath("movementType", "TAP")
          assertJsonPath("offenderIdDisplay", offenderNo)
          assertJsonPath("bookingId", "${booking.bookingId.value}")
          assertJsonPath("movementSeq", "11")
          assertJsonPath("directionCode", "IN")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED")
        assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
      }
    }

    @Nested
    @DisplayName("OFF_BKB_UPD -> OFFENDER_BOOKING-REASSIGNED")
    inner class OffenderBookingReassigned {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val oldOffenderNo = "A1234AA"
      private val oldOffenderId = 12345L
      private val bookingStartDateTime = LocalDateTime.parse("2024-08-23T12:20:30")
      private val bookingEndDateTime = LocalDateTime.parse("2025-09-25T12:21:30")
      private val newOffenderId = 6789L
      private val newOffenderNo = "A5678BB"
      private lateinit var bookingMoved: OffenderBooking
      private lateinit var movement: OffenderExternalMovement

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          Offender.build {
            offenderNo = this@OffenderBookingReassigned.oldOffenderNo
          }.also {
            OffenderBooking.build(offender = it).also { booking ->
              OffenderExternalMovement.build(offenderBooking = booking, sequence = 1) {
                direction = "IN"
                type = "ADM"
                date = LocalDate.parse("2024-08-23")
                time = LocalDateTime.parse("2024-08-23T10:10:00")
              }
              OffenderExternalMovement.build(offenderBooking = booking, sequence = 2) {
                direction = "OUT"
                type = "REL"
                date = LocalDate.parse("2024-10-24")
                time = LocalDateTime.parse("2024-10-23T15:05:00")
              }
            }
            bookingMoved = OffenderBooking.build(offender = it, beginDate = bookingStartDateTime, endDate = bookingEndDateTime).also { booking ->
              movement = OffenderExternalMovement.build(offenderBooking = booking, sequence = 3) {
                direction = "IN"
                type = "ADM"
                date = LocalDate.parse("2024-12-25")
                time = LocalDateTime.parse("2024-08-25T10:20:00")
              }
            }
          }

          Offender.build {
            offenderNo = this@OffenderBookingReassigned.newOffenderNo
          }.also {
            OffenderBooking.build(offender = it).also { booking ->
              OffenderExternalMovement.build(offenderBooking = booking, sequence = 1) {
                direction = "IN"
                type = "ADM"
                date = LocalDate.parse("2024-09-23")
                time = LocalDateTime.parse("2024-09-23T10:10:00")
              }
              OffenderExternalMovement.build(offenderBooking = booking, sequence = 2) {
                direction = "OUT"
                type = "REL"
                date = LocalDate.parse("2024-11-24")
                time = LocalDateTime.parse("2024-11-23T15:05:00")
              }
            }
          }
        }

        simulateTrigger(
          nomisEventType = "OFF_BKB_UPD",
          "p_old_offender_id" to oldOffenderId,
          "p_offender_id" to newOffenderId,
          "p_offender_book_id" to bookingMoved.id.value,
        )
        prisonerEvent = awaitMessage()
      }

      @Test
      fun `will map to OFFENDER_BOOKING-REASSIGNED with additional parameters`() {
        with(prisonerEvent.message) {
          assertJsonPath("nomisEventType", "OFF_BKB_UPD")
          assertJsonPath("eventType", "OFFENDER_BOOKING-REASSIGNED")
          assertJsonPath("previousOffenderId", oldOffenderId)
          assertJsonPath("offenderId", newOffenderId)
          assertJsonPath("bookingId", "${bookingMoved.bookingId.value}")
          assertJsonPath("bookingStartDateTime", "${bookingMoved.beginDate}")
          assertJsonPath("bookingEndDateTime", "${bookingMoved.endDate}")
          assertJsonPath("lastAdmissionDate", "${movement.date}")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("OFFENDER_BOOKING-REASSIGNED")
        assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
      }
    }

    @Nested
    @DisplayName("BOOKING_NUMBER-CHANGED")
    inner class BookingNumberChanged {
      @BeforeEach
      fun setUp() {
        transaction {
          MergeTransactions.deleteAll()
          Offenders.deleteAll()
        }
      }

      @Nested
      @DisplayName("P1_RESULT -> BOOKING_NUMBER-CHANGED")
      inner class P1ResultBookingNumberChanged {
        private lateinit var prisonerEvent: PrisonerEventMessage
        private val bookingId = 1234
        private val offenderId = 12345

        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "P1_RESULT",
            "p_old_prison_num" to "96971F",
            "p_new_prison_num" to "63102D",
            "p_offender_id" to offenderId,
            "p_offender_book_id" to bookingId,
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to BOOKING_NUMBER-CHANGED with type of BOOK_NUMBER_CHANGE`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "BOOKING_NUMBER-CHANGED")
            assertJsonPath("nomisEventType", "P1_RESULT")
            assertJsonPath("bookingId", "$bookingId")
            assertJsonPath("offenderId", "$offenderId")
            assertJsonPath("type", "BOOK_NUMBER_CHANGE")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      @DisplayName("BOOK_UPD_OASYS -> BOOKING_NUMBER-CHANGED (book number change)")
      inner class BookUpdOasysBookingNumberChanged {
        private lateinit var prisonerEvent: PrisonerEventMessage
        private val bookingId = 1234
        private val offenderId = 12345

        @BeforeEach
        fun setUp() {
          simulateTrigger(
            nomisEventType = "BOOK_UPD_OASYS",
            "p_old_prison_num" to "96971F",
            "p_offender_id" to offenderId,
            "p_offender_book_id" to bookingId,
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to BOOKING_NUMBER-CHANGED with type of BOOK_NUMBER_CHANGE_DUPLICATE`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "BOOKING_NUMBER-CHANGED")
            assertJsonPath("nomisEventType", "BOOK_UPD_OASYS")
            assertJsonPath("bookingId", "$bookingId")
            assertJsonPath("offenderId", "$offenderId")
            assertJsonPath("type", "BOOK_NUMBER_CHANGE_DUPLICATE")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      @DisplayName("BOOK_UPD_OASYS -> BOOKING_NUMBER-CHANGED (merge) - OFFENDER_ID_DISPLAY_2 retained")
      inner class BookUpdOasysMergeOffender2Retained {
        private lateinit var prisonerEvent: PrisonerEventMessage
        private val bookingId = 1234L
        private var offenderId = 0L

        @BeforeEach
        fun setUp() {
          transaction {
            val offender = Offender.build(offenderNo = "A1234KT") {}
            commit()
            offenderId = offender.offenderId.value
            MergeTransaction.build(
              requestDate = LocalDateTime.now().minusMinutes(11),
              modifyDatetime = LocalDateTime.now().minusMinutes(9),
              offenderId1 = 54321,
              offenderNo1 = "A4321TK",
              bookingId1 = 4321,
              offenderId2 = offenderId,
              bookingId2 = bookingId,
              offenderNo2 = "A1234KT",
            )
          }
          simulateTrigger(
            nomisEventType = "BOOK_UPD_OASYS",
            "p_old_prison_num" to "96971F",
            "p_offender_id" to offenderId,
            "p_offender_book_id" to bookingId,
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to BOOKING_NUMBER-CHANGED with type of MERGE`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "BOOKING_NUMBER-CHANGED")
            assertJsonPath("nomisEventType", "BOOK_UPD_OASYS")
            assertJsonPath("bookingId", "$bookingId")
            assertJsonPath("offenderId", "$offenderId")
            assertJsonPath("type", "MERGE")
            assertJsonPath("offenderIdDisplay", "A1234KT")
            assertJsonPath("previousOffenderIdDisplay", "A4321TK")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      @DisplayName("BOOK_UPD_OASYS -> BOOKING_NUMBER-CHANGED (merge) - OFFENDER_ID_DISPLAY_1 retained")
      inner class BookUpdOasysMergeOffender1Retained {
        private lateinit var prisonerEvent: PrisonerEventMessage
        private val bookingId = 1234L
        private var offenderId = 0L

        @BeforeEach
        fun setUp() {
          transaction {
            val offender = Offender.build(offenderNo = "A4321TK") {}
            commit()
            offenderId = offender.offenderId.value
            MergeTransaction.build(
              requestDate = LocalDateTime.now().minusMinutes(11),
              modifyDatetime = LocalDateTime.now().minusMinutes(9),
              offenderId1 = 54321,
              offenderNo1 = "A4321TK",
              bookingId1 = 4321,
              offenderId2 = offenderId,
              bookingId2 = bookingId,
              offenderNo2 = "A1234KT",
            )
          }
          simulateTrigger(
            nomisEventType = "BOOK_UPD_OASYS",
            "p_old_prison_num" to "96971F",
            "p_offender_id" to offenderId,
            "p_offender_book_id" to bookingId,
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to BOOKING_NUMBER-CHANGED with type of MERGE`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "BOOKING_NUMBER-CHANGED")
            assertJsonPath("nomisEventType", "BOOK_UPD_OASYS")
            assertJsonPath("bookingId", "$bookingId")
            assertJsonPath("offenderId", "$offenderId")
            assertJsonPath("type", "MERGE")
            assertJsonPath("offenderIdDisplay", "A4321TK")
            assertJsonPath("previousOffenderIdDisplay", "A1234KT")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }

      @Nested
      @DisplayName("BOOK_UPD_OASYS -> BOOKING_NUMBER-CHANGED (book number change after previous merge)")
      inner class BookUpdOasysBookNumberChangeAfterMerge {
        private lateinit var prisonerEvent: PrisonerEventMessage
        private val bookingId = 1234L
        private val offenderId = 12345L

        @BeforeEach
        fun setUp() {
          transaction {
            MergeTransaction.build(
              // with an old merge
              requestDate = LocalDateTime.now().minusMinutes(12),
              modifyDatetime = LocalDateTime.now().minusMinutes(11),
              offenderId1 = 54321,
              offenderNo1 = "A4321TK",
              bookingId1 = 4321,
              offenderId2 = offenderId,
              bookingId2 = bookingId,
              offenderNo2 = "A1234KT",
            )
          }
          simulateTrigger(
            nomisEventType = "BOOK_UPD_OASYS",
            "p_old_prison_num" to "96971F",
            "p_offender_id" to offenderId,
            "p_offender_book_id" to bookingId,
          )

          prisonerEvent = awaitMessage()
        }

        @Test
        fun `will map to BOOKING_NUMBER-CHANGED with type of BOOK_NUMBER_CHANGE_DUPLICATE`() {
          with(prisonerEvent.message) {
            assertJsonPath("eventType", "BOOKING_NUMBER-CHANGED")
            assertJsonPath("nomisEventType", "BOOK_UPD_OASYS")
            assertJsonPath("bookingId", "$bookingId")
            assertJsonPath("offenderId", "$offenderId")
            assertJsonPath("type", "BOOK_NUMBER_CHANGE_DUPLICATE")
          }
        }

        @Test
        fun `will map meta data for the event`() {
          assertThat(prisonerEvent.eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
          assertThat(prisonerEvent.publishedAt).isCloseToUtcNow(within(10, ChronoUnit.SECONDS))
        }
      }
    }

    @Nested
    @DisplayName("LINK_CASE_TXNS-INSERTED -> COURT_EVENT_CHARGES-LINKED")
    inner class CourtEventChargesLinked {
      private lateinit var prisonerEvent: PrisonerEventMessage
      private val offenderNo = "A1234AA"
      private lateinit var booking: OffenderBooking
      private lateinit var charge: OffenderCharge

      @BeforeEach
      fun setUp() {
        transaction {
          this.addLogger(StdOutSqlLogger)
          Offender.build {
            offenderNo = this@CourtEventChargesLinked.offenderNo
          }.also {
            booking = OffenderBooking.build(offender = it).also { booking ->
              charge = OffenderCharge.build(
                offenderBooking = booking,
                caseId = 8765L,
              )
            }
          }
        }

        simulateTrigger(
          nomisEventType = "LINK_CASE_TXNS-INSERTED",
          "p_offender_charge_id" to "${charge.offenderChargeId.value}",
          "p_event_id" to "65432",
          "p_case_id" to "1604142",
          "p_combined_case_id" to "1604141",
          "p_audit_module_name" to "OCULCASE",
        )
        prisonerEvent = awaitMessage()
      }

      @Test
      fun `will map to COURT_EVENT_CHARGES-LINKED with additional parameters`() {
        with(prisonerEvent.message) {
          assertJsonPath("nomisEventType", "LINK_CASE_TXNS-INSERTED")
          assertJsonPath("eventType", "COURT_EVENT_CHARGES-LINKED")
          //   assertJsonPath("offenderIdDisplay", offenderNo)
          assertJsonPath("bookingId", "${booking.bookingId.value}")
          assertJsonPath("chargeId", "${charge.offenderChargeId.value}")
          assertJsonPath("eventId", "65432")
          assertJsonPath("combinedCaseId", "1604141")
          assertJsonPath("auditModuleName", "OCULCASE")
        }
      }

      @Test
      fun `will map meta data for the event`() {
        assertThat(prisonerEvent.eventType).isEqualTo("COURT_EVENT_CHARGES-LINKED")
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

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int = prisonEventQueueSqsClient.countAllMessagesOnQueue(prisonEventQueueUrl).get()
    .also { log.info("Number of messages on prison queue: $it") }

  fun getNumberOfMessagesCurrentlyOnExceptionQueue() = sqlRepository.getExceptionMessageIds(QUEUE_NAME).size
    .also { log.info("Number of messages on exception queue: $it") }

  fun awaitMessage(): PrisonerEventMessage {
    awaitQueueSizeToBe(1)

    val queueMessage = prisonEventQueueSqsClient.receiveMessage(
      ReceiveMessageRequest.builder().queueUrl(prisonEventQueueUrl).build(),
    ).get().messages().first().body()

    val sqsMessage: SnsMessage = jsonMapper.readValue(queueMessage)
    return PrisonerEventMessage(
      message = sqsMessage.message,
      publishedAt = OffsetDateTime.parse(sqsMessage.messageAttributes["publishedAt"]?.value.toString()),
      eventType = sqsMessage.messageAttributes.eventType!!,
    )
  }
}

data class PrisonerEventMessage(
  val message: String,
  val publishedAt: OffsetDateTime,
  val eventType: String,
)

private fun String.assertJsonPath(path: String, expectedValue: Any) = JsonPathExpectationsHelper(path).assertValue(this, expectedValue)
private fun String.assertDoesNotHaveJsonPath(path: String) = JsonPathExpectationsHelper(path).doesNotHaveJsonPath(this)
