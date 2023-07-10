package uk.gov.justice.digital.hmpps.prisonerevents.integration

import jakarta.jms.ConnectionFactory
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doCallRealMethod
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mockingDetails
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.jms.core.JmsTemplate
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.net.SocketException
import java.time.Duration
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

      await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 1 }

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

      await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 1 }
    }

    @Test
    fun `will move to exception queue after repeated publish failures`() {
      sabotageTopic()

      simulateTrigger()

      awaitPublishTries(4)

      assertThat(getNumberOfMessagesCurrentlyOnPrisonEventQueue()).isEqualTo(0)

      // After 5 attempts, the message should be on the exception queue
      await.atMost(Duration.ofSeconds(10)) untilCallTo { getGetNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == 1 }
    }
  }

  @Nested
  inner class RetryEndpoint {
    @Test
    fun `access forbidden when no authority`() {
      webTestClient.get().uri("/housekeeping")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `access forbidden when no role`() {
      webTestClient.get().uri("/housekeeping")
        .headers(setAuthorisation(roles = listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access forbidden with wrong role`() {
      webTestClient.get().uri("/housekeeping")
        .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Retry exception messages when none present`() {
      webTestClient.get().uri("/housekeeping")
        .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Retry exception messages when messages present`() {
      sabotageTopic()
      repeat(2) { simulateTrigger() }
      await.atMost(Duration.ofSeconds(20)) untilCallTo { getGetNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == 2 }
      fixTopic()

      webTestClient
        .get().uri("/housekeeping")
        .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
        .exchange()
        .expectStatus().isOk

      await untilCallTo { getGetNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == 0 }
      // Messages have been moved from exception queue to normal queue
      await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 2 }
      // Messages arrived on topic and were sent to subscribed queue
    }
  }

  private fun sabotageTopic() {
    whenever(snsClient.publish(any<PublishRequest>())).thenReturn(CompletableFuture.failedFuture(SocketException("Test exception")))
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

  fun purgeQueues() {
    prisonEventQueueSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(prisonEventQueueUrl).build())
    sqlRepository.purgeExceptionQueue()
    await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() == 0 }
  }

  fun awaitPublishTries(count: Int) {
    await.atMost(Duration.ofSeconds(20)) untilCallTo { mockingDetails(snsClient).invocations.filter { it.method.name == "publish" }.size } matches {
      log.info("Number of publish calls: $it")
      it == count
    }
  }

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int =
    prisonEventQueueSqsClient.countAllMessagesOnQueue(prisonEventQueueUrl).get()

  fun getGetNumberOfMessagesCurrentlyOnExceptionQueue() =
    sqlRepository.getExceptionMessageIds().size
      .also { log.info("Number of messages on exception queue: $it") }
}
