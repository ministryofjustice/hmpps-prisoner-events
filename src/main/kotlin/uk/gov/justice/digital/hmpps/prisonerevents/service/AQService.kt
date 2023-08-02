package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.TABLE_OWNER
import uk.gov.justice.digital.hmpps.prisonerevents.config.trackEvent
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import java.time.LocalDate

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val transactionTemplate: TransactionTemplate,
  private val retryJmsTemplate: JmsTemplate,
  private val telemetryClient: TelemetryClient?,
) {
  fun requeueExceptions() {
    val ids = sqlRepository.getExceptionMessageIds(QUEUE_NAME)
    val messageCount = ids.size
    if (messageCount == 0) {
      log.info("No messages found on exception queue $EXCEPTION_QUEUE_NAME")
      return
    }

    log.info("For exception queue $EXCEPTION_QUEUE_NAME of $FULL_QUEUE_NAME we found $messageCount messages, attempting to retry them")
    telemetryClient?.trackEvent("RetryDLQ", mapOf("queue-name" to FULL_QUEUE_NAME, "messages-found" to "$messageCount"))

    // https://javadoc.io/doc/com.oracle.database.jdbc/ojdbc11/latest/index.html
    // https://docs.oracle.com/database/121/JAJMS/toc.htm

//    AQOracleDebug.setTraceLevel(5)
//    AQjmsOracleDebug.setTraceLevel(6)

    ids.chunked(10).forEach { chunk ->
      transactionTemplate.executeWithoutResult {
        chunk.forEach { id ->
          retryJmsTemplate.receiveSelected("$TABLE_OWNER.$EXCEPTION_QUEUE_NAME", "JMSMessageID='$id'")
            ?.also { message ->
              log.debug("Got message ${message.jmsMessageID} from exception queue")

              retryJmsTemplate.send { message }
              log.debug("Requeued message ${message.jmsMessageID} on $FULL_QUEUE_NAME")
            }
        }
        log.debug("Committing transaction")
      }
    }
  }

  fun dequeueExceptions(originalQueue: String, onlyBefore: LocalDate) {
    val ids = sqlRepository.getExceptionMessageIds(originalQueue, onlyBefore)
    val messageCount = ids.size
    if (messageCount == 0) {
      log.info("No messages found for $originalQueue on exception queue $EXCEPTION_QUEUE_NAME")
      return
    }

    log.info("For exception queue $EXCEPTION_QUEUE_NAME of $originalQueue we found $messageCount messages, attempting to dequeue them")
    telemetryClient?.trackEvent("DequeueDLQ", mapOf("queue-name" to originalQueue, "messages-found" to "$messageCount", "only-before" to "$onlyBefore"))

    ids.chunked(10).forEach { chunk ->
      chunk.forEach { id ->
        retryJmsTemplate.receiveSelected("$TABLE_OWNER.$EXCEPTION_QUEUE_NAME", "JMSMessageID='$id'")
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
