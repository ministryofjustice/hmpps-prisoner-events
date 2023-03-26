package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.TABLE_OWNER
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val transactionTemplate: TransactionTemplate,
  private val retryJmsTemplate: JmsTemplate,
  private val telemetryClient: TelemetryClient?,
) {
  fun requeueExceptions() {
    val ids = sqlRepository.getExceptionMessageIds()
    val messageCount = ids.size
    if (messageCount == 0) {
      log.info("No messages found on exception queue $EXCEPTION_QUEUE_NAME")
      return
    }
    log.info("For exception queue $EXCEPTION_QUEUE_NAME of $FULL_QUEUE_NAME we found $messageCount messages, attempting to retry them")

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

    telemetryClient?.trackEvent(
      "RetryDLQ",
      mapOf("queue-name" to FULL_QUEUE_NAME, "messages-found" to "$messageCount"),
      null,
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
