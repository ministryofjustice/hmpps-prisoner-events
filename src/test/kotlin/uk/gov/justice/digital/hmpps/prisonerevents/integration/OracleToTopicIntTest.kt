package uk.gov.justice.digital.hmpps.prisonerevents.integration

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
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
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.Duration
import javax.jms.ConnectionFactory

class OracleToTopicIntTest : IntegrationTestBase() {

  @SpyBean
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var connectionFactory: ConnectionFactory

  @Autowired
  private lateinit var sqlRepository: SqlRepository

  private val jmsTemplate by lazy {
    JmsTemplate(connectionFactory).also { it.defaultDestinationName = "XTAG.XTAG_DPS" }
  }

  internal val prisonEventQueue by lazy { hmppsQueueService.findByQueueId("prisoneventtestqueue") as HmppsQueue }
  internal val prisonEventQueueSqsClient by lazy { prisonEventQueue.sqsClient }
  internal val prisonEventQueueUrl by lazy { prisonEventQueue.queueUrl }

  companion object {
    private var isTopicSpySetUp = false
    private lateinit var snsClient: AmazonSNS
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

  @Test
  fun `will consume a prison offender events message`() {
    // AQjmsOracleDebug.setTraceLevel(3)

    simulateTrigger()

    await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 1 }

    val receiveMessageResult = prisonEventQueueSqsClient.receiveMessage(prisonEventQueueUrl)
    with(receiveMessageResult.messages.first()) {
      assertThat(body.contains("""\"nomisEventType\":\"OFF_RECEP_OASYS\"""")).isTrue
      assertThat(body.contains("""\"eventType\":\"OFFENDER_MOVEMENT-RECEPTION\"""")).isTrue
      assertThat(body.contains("""\"bookingId\":1234567""")).isTrue
      assertThat(body.contains("""\"movementSeq\":4""")).isTrue
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
    // Sabotage the topic
    sabotageTopic()

    simulateTrigger()

    awaitPublishTries(5)

    assertThat(getNumberOfMessagesCurrentlyOnPrisonEventQueue()).isEqualTo(0)

    // After 5 attempts, the message should be on the exception queue
    await.atMost(Duration.ofSeconds(10)) untilCallTo { getGetNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == 1 }
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

    webTestClient.get().uri("/housekeeping")
      .headers(setAuthorisation(roles = listOf("ROLE_QUEUE_ADMIN")))
      .exchange()
      .expectStatus().isOk

    await untilCallTo { getGetNumberOfMessagesCurrentlyOnExceptionQueue() } matches { it == 0 }
    await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 2 }
  }

  private fun sabotageTopic() {
    doThrow(RuntimeException("Test exception")).whenever(snsClient).publish(any<PublishRequest>())
  }

  private fun fixTopic() {
    doCallRealMethod().whenever(snsClient).publish(any<PublishRequest>())
  }

  private fun simulateTrigger() {
    jmsTemplate.send { session ->
      val testMessage = session.createMapMessage()
      testMessage.jmsType = "OFF_RECEP_OASYS"
      testMessage.setLong("p_offender_book_id", 1234567L)
      testMessage.setInt("p_movement_seq", 4)
      testMessage.setString("eventType", "OFF_RECEP_OASYS")
      testMessage
    }
  }

  fun purgeQueues() {
    prisonEventQueueSqsClient.purgeQueue(PurgeQueueRequest(prisonEventQueueUrl))
    sqlRepository.purgeExceptionQueue()
    await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() == 0 }
  }

  fun awaitPublishTries(count: Int) {
    await untilCallTo { mockingDetails(snsClient).invocations.filter { it.method.name == "publish" }.size } matches {
      // log.info("Number of publish calls: $it")
      it == count
    }
  }

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int = prisonEventQueueSqsClient.numMessages(prisonEventQueueUrl)
  // .also { log.info("Number of messages on prison queue: $it") }

  fun getGetNumberOfMessagesCurrentlyOnExceptionQueue() =
    sqlRepository.getExceptionMessageIds().size // .also { log.info("Number of messages on exception queue: $it") }
}

fun AmazonSQS.numMessages(url: String): Int {
  val queueAttributes = getQueueAttributes(url, listOf("ApproximateNumberOfMessages"))
  return queueAttributes.attributes["ApproximateNumberOfMessages"]!!.toInt()
}
