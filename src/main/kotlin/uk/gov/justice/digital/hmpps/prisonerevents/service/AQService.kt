package uk.gov.justice.digital.hmpps.prisonerevents.service

import oracle.jms.AQjmsSession
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import javax.jms.QueueConnection
import javax.jms.QueueConnectionFactory
import javax.jms.Session

val EXCEPTION_QUEUE_NAME = "AQ\$_XTAG_LISTENER_TAB_E"

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val queueConnectionFactory: QueueConnectionFactory,
  @Value("\${source.queue.name}") val queueName: String,
) {
  fun requeueExceptions() {
    val queueDetails = queueName.split(".")
    val tableOwner = queueDetails[0]
    val queueSimpleName = queueDetails[1]

    val ids = sqlRepository.getExceptionMessageIds()

    val queueConnection: QueueConnection = queueConnectionFactory.createQueueConnection()
    val session = queueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE) as AQjmsSession
    val exceptionQueue = session.getQueue(tableOwner, EXCEPTION_QUEUE_NAME)
    queueConnection.start()
    try {
      val dpsQueue = session.getQueue(tableOwner, queueSimpleName)
      val messageConsumer = session.createConsumer(
        exceptionQueue,
        "JMSMessageID in ${join(ids)}" // MSGID column
      )
      val messageProducer = session.createProducer(dpsQueue)
      while (true) {
        val message = messageConsumer.receiveNoWait() ?: break

        messageProducer.send(message)
        log.debug("Requeued message ${message.jmsMessageID}")
      }
    } finally {
      queueConnection.stop()
      queueConnection.close()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(JMSReceiver::class.java)

    private fun join(ids: List<String>): String {
      return ids.joinToString(",", "(", ")", truncated = "", transform = { "'$it'" })
    }
  }
}
