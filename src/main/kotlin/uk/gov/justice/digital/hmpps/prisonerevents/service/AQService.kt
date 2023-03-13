package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import javax.jms.QueueConnectionFactory

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val queueConnectionFactory: QueueConnectionFactory,
  private val transactionTemplate: TransactionTemplate,
  private val telemetryClient: TelemetryClient?,
) {
  fun requeueExceptions() {
    val tableOwner = QUEUE_NAME.split(".")[0]

    val ids = sqlRepository.getExceptionMessageIds()
    val messageCount = ids.size
    if (messageCount == 0) {
      log.info("No messages found on exception queue $EXCEPTION_QUEUE_NAME")
      return
    }
    log.info("For exception queue $QUEUE_NAME we found $messageCount messages, attempting to retry them")

    val jmsTemplate = JmsTemplate(queueConnectionFactory).apply {
      isSessionTransacted = true
      receiveTimeout = RECEIVE_TIMEOUT_NO_WAIT
      defaultDestinationName = QUEUE_NAME
    }

    // TODO does this re-execute the message selector each time and if so is there a more efficient alternative?
    val selector = "JMSMessageID in ${join(ids)}"

    while (doInTransactionWithMore(jmsTemplate, tableOwner, selector)) {
    }

    telemetryClient?.trackEvent(
      "RetryDLQ",
      mapOf("queue-name" to QUEUE_NAME, "messages-found" to "$messageCount"),
      null,
    )
  }

  private fun doInTransactionWithMore(
    jmsTemplate: JmsTemplate,
    tableOwner: String,
    selector: String,
  ): Boolean = transactionTemplate.execute {
    jmsTemplate.receiveSelected("$tableOwner.$EXCEPTION_QUEUE_NAME", selector)
      ?.also { message ->
        log.debug("Got message ${message.jmsMessageID} from exception queue")

        jmsTemplate.send { message }
        log.debug("Requeued message ${message.jmsMessageID} on $QUEUE_NAME")
      }
      ?: return@execute false

    return@execute true
  }!!

  /*
  -- purge queue
DECLARE
 po_t dbms_aqadm.aq$_purge_options_t;
BEGIN
  dbms_aqadm.purge_queue_table('MY_QUEUE_TABLE', NULL, po_t);
END;
   */
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun join(ids: List<String>): String {
      return ids.joinToString(",", "(", ")", truncated = "", transform = { "'$it'" })
    }
  }
}
