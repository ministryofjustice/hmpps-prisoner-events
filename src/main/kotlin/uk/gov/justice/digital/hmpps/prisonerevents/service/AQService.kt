package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import oracle.jms.AQjmsSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerevents.repository.LIMIT
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import javax.jms.QueueConnection
import javax.jms.QueueConnectionFactory
import javax.jms.Session

val EXCEPTION_QUEUE_NAME = "AQ\$_XTAG_LISTENER_TAB_E"

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val queueConnectionFactory: QueueConnectionFactory,
  private val telemetryClient: TelemetryClient?,
  @Value("\${source.queue.name}") val queueName: String,
) {
  fun requeueExceptions() {
    val queueDetails = queueName.split(".")
    val tableOwner = queueDetails[0]
    val queueSimpleName = queueDetails[1]

    val ids = sqlRepository.getExceptionMessageIds()
    val messageCount = ids.size
    if (messageCount == 0) {
      log.info("No messages found on exception queue $queueName")
      return
    }
    log.info("For exception queue $queueName we found $messageCount messages, attempting to retry them")

    val queueConnection: QueueConnection = queueConnectionFactory.createQueueConnection()
    val session = queueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE) as AQjmsSession
    val exceptionQueue = session.getQueue(tableOwner, EXCEPTION_QUEUE_NAME)
    queueConnection.start()
    try {
      val dpsQueue = session.getQueue(tableOwner, queueSimpleName)
      val messageConsumer = session.createConsumer(
        exceptionQueue,
        "JMSMessageID in ${join(ids)}", // MSGID column
      )
      val messageProducer = session.createProducer(dpsQueue)
      for (count in 1..LIMIT) {
        // TODO does this re-execute the message selector each time and if so is there a more efficient alternative?
        val message = messageConsumer.receiveNoWait() ?: break
        log.debug("Got message ${message.jmsMessageID} from exception queue")

        messageProducer.send(message)
        session.commit()
        log.debug("Requeued message ${message.jmsMessageID} on $queueName")
      }
      telemetryClient?.trackEvent(
        "RetryDLQ",
        mapOf("queue-name" to queueName, "messages-found" to "$messageCount"),
        null,
      )
    } catch (e: Exception) {
      log.error("Error requeuing messages", e)
      session.rollback()
      throw e
    } finally {
      queueConnection.stop()
      queueConnection.close()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun join(ids: List<String>): String {
      return ids.joinToString(",", "(", ")", truncated = "", transform = { "'$it'" })
    }
  }
}
