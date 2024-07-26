package uk.gov.justice.digital.hmpps.prisonerevents.integration

import jakarta.jms.ConnectionFactory
import jakarta.jms.MessageProducer
import jakarta.jms.Session
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mockingDetails
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.ProducerCallback
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.net.SocketException
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

class OracleToTopicIntTest : IntegrationTestBase() {

  @SpyBean
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

  companion object {
    private var isTopicSpySetUp = false
    private lateinit var snsClient: SnsAsyncClient
  }

  @BeforeEach
  fun setup() {
    if (!isTopicSpySetUp) {
      val realTopic = hmppsQueueService.findByTopicId("prisoneventtopic")!!
      val realSnsClient = realTopic.snsClient
      snsClient = spy(realSnsClient)
      val mockTopic = HmppsTopic(arn = realTopic.arn, snsClient = snsClient, id = "prisoneventtopic")
      whenever(hmppsQueueService.findByTopicId("prisoneventtopic")).thenReturn(mockTopic)
      isTopicSpySetUp = true
    }
    // Ensure publish is called normally and call logging is reset
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
    fun `will consume all prison offender events message`() {
      val count = 10
      simulateBatchTrigger(count)

      awaitQueueSizeToBe(count)

      (1..count).forEach { _ ->

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
  private fun simulateBatchTrigger(count: Int) {
    val producerCallback: ProducerCallback<*> = ProducerCallback { session: Session, producer: MessageProducer ->
      (1..count).forEach { _ ->
        producer.send(
          session.createMapMessage().apply {
            jmsType = "OFF_RECEP_OASYS"
            setLong("p_offender_book_id", 1234567L)
            setInt("p_movement_seq", 4)
            setString("eventType", "OFF_RECEP_OASYS")
          },
        )
      }
    }
    jmsTemplate.execute(producerCallback)
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
}
