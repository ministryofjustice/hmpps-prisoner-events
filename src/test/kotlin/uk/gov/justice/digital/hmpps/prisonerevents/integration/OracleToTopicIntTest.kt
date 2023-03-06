package uk.gov.justice.digital.hmpps.prisonerevents.integration

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import javax.jms.ConnectionFactory

class OracleToTopicIntTest : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var connectionFactory: ConnectionFactory

  internal val topic by lazy { hmppsQueueService.findByTopicId("prisoneventtopic") as HmppsTopic }
  internal val prisonEventQueue by lazy { hmppsQueueService.findByQueueId("prisoneventtestqueue") as HmppsQueue }
  internal val prisonEventQueueSqsClient by lazy { prisonEventQueue.sqsClient }
  internal val prisonEventQueueUrl by lazy { prisonEventQueue.queueUrl }

  @Test
  fun `will consume a prison offender events message`() {
    // AQjmsOracleDebug.setTraceLevel(3)

    val jmsTemplate = JmsTemplate(connectionFactory)

    jmsTemplate.defaultDestinationName = "XTAG.XTAG_DPS"
    jmsTemplate.send { session ->
      val testMessage = session.createMapMessage()
      testMessage.jmsType = "OFF_RECEP_OASYS"
      testMessage.setLong("p_offender_book_id", 1234567L)
      testMessage.setInt("p_movement_seq", 4)
      testMessage.setString("eventType", "OFF_RECEP_OASYS")
      testMessage
    }

    await untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == 1 }

    val receiveMessageResult = prisonEventQueueSqsClient.receiveMessage(prisonEventQueueUrl)
    with(receiveMessageResult.messages.first()) {
      assertThat(body.contains("""\"nomisEventType\":\"OFF_RECEP_OASYS\"""")).isTrue()
      assertThat(body.contains("""\"eventType\":\"OFFENDER_MOVEMENT-RECEPTION\"""")).isTrue()
      assertThat(body.contains("""\"bookingId\":1234567""")).isTrue()
      assertThat(body.contains("""\"movementSeq\":4""")).isTrue()
    }
  }

  fun purgeQueues() {
    prisonEventQueueSqsClient.purgeQueue(PurgeQueueRequest(prisonEventQueueUrl))
    await.until { getNumberOfMessagesCurrentlyOnPrisonEventQueue() == 0 }
  }

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int = prisonEventQueueSqsClient.numMessages(prisonEventQueueUrl)
}

fun AmazonSQS.numMessages(url: String): Int {
  val queueAttributes = getQueueAttributes(url, listOf("ApproximateNumberOfMessages"))
  return queueAttributes.attributes["ApproximateNumberOfMessages"]!!.toInt()
}
