package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import oracle.AQ.AQDequeueOption
import oracle.AQ.AQEnqueueOption
import oracle.AQ.AQOracleDebug
import oracle.AQ.AQQueue
import oracle.jdbc.OracleConnection
import oracle.jdbc.aq.AQDequeueOptions
import oracle.jdbc.aq.AQDequeueOptions.DEQUEUE_NO_WAIT
import oracle.jdbc.aq.AQEnqueueOptions
import oracle.jms.AQjmsConnection
import oracle.jms.AQjmsDestination
import oracle.jms.AQjmsDestinationProperty
import oracle.jms.AQjmsSession
import oracle.sql.TypeDescriptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import javax.jms.ConnectionFactory
import javax.jms.QueueConnectionFactory
import javax.jms.Session
import javax.sql.DataSource

@Service
class AQService(
  private val sqlRepository: SqlRepository,
  private val transactionTemplate: TransactionTemplate,
  private val retryJmsTemplate: JmsTemplate,
  private val telemetryClient: TelemetryClient?,
  @Qualifier("retryConnectionFactory") private val connectionFactory: QueueConnectionFactory,
  @Qualifier("nativeConnectionFactory") private val nativeConnectionFactory: ConnectionFactory,
  private val dataSource: DataSource,
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

    // https://javadoc.io/doc/com.oracle.database.jdbc/ojdbc11/latest/index.html
    // https://docs.oracle.com/database/121/JAJMS/toc.htm

    requeueAllSession()

//    // TODO does this re-execute the message selector each time and if so is there a more efficient alternative?
//    val selector = "JMSMessageID in ${join(ids)}"
//    while (requeueAndIsMore(tableOwner, selector)) {
//    }

    telemetryClient?.trackEvent(
      "RetryDLQ",
      mapOf("queue-name" to QUEUE_NAME, "messages-found" to "$messageCount"),
      null,
    )
  }

  private fun requeueAllProprietaryConnection() {
    // val connRaw = nativeConnectionFactory.createConnection()
    // val conn =  connRaw as AQjmsConnection// : OracleConnection = connRaw as OracleConnection

    val conn = dataSource.connection as OracleConnection
    AQOracleDebug.setTraceLevel(5)

    val dequeueOptions = AQDequeueOptions().apply { wait = DEQUEUE_NO_WAIT }
    val enqueueOptions = AQEnqueueOptions()

    val datatype = "SYS.AQ${'$'}_JMS_MESSAGE"
    val toid = TypeDescriptor.RAWTOID
    val JMS_MESSAGE_TOID = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 16, 35)
    val JMS_MAP_MESSAGE_TOID = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 16, 39)

    sqlRepository.getExceptionMessageIdsBytes().forEach { messageId ->
      dequeueOptions.dequeueMessageId = messageId

      conn.dequeue("XTAG.$EXCEPTION_QUEUE_NAME", dequeueOptions, datatype)?.also { message ->

        StringBuilder()
          .apply {
            for (b in message.payloadTOID) {
              append(String.format("%02X", b))
            }
          }
          .run { log.info("toid is $this") }

        conn.enqueue(QUEUE_NAME, enqueueOptions, message)

//        val st = conn.createStatement()
//
//        st.execute("begin dbms_aq.purge_queue_table('XTAG.$EXCEPTION_QUEUE_NAME', null, null); end;")
        // ORA-00932: inconsistent datatypes: expected SYS.AQ$_JMS_MESSAGE got SYS.AQ$_JMS_MESSAGE
      }
    }
    conn.close()
  }

  private fun requeueAllSession() {

    val conn = nativeConnectionFactory.createConnection() as AQjmsConnection
    conn.createSession(false, Session.AUTO_ACKNOWLEDGE).also { session ->
      val sess = session as AQjmsSession

      val table = sess.getQueueTable("XTAG", "XTAG_LISTENER_TAB")
      // val e = sess.createQueue(table, EXCEPTION_QUEUE_NAME, AQjmsDestinationProperty()) as AQQueue
      val e = sess.createQueue(EXCEPTION_QUEUE_NAME) as AQQueue
      val d = sess.createQueue(table, "XTAG_DPS", AQjmsDestinationProperty()) as AQQueue

      val exceptionQueue = sess.getQueue("XTAG", EXCEPTION_QUEUE_NAME) as AQjmsDestination
      val dpsQueue = sess.getQueue("XTAG", "XTAG_DPS") as AQjmsDestination
      val dequeueOption = AQDequeueOption().apply { waitTime = DEQUEUE_NO_WAIT }
      val enqueueOption = AQEnqueueOption()

      sqlRepository.getExceptionMessageIdsBytes().forEach { messageId ->

        dequeueOption.messageId = messageId
        e.dequeue(dequeueOption)?.also {
          d.enqueue(enqueueOption, it)
        }
      }
    }

//    retryJmsTemplate.execute { session ->
//      val sess = session // as AQjmsSession
//      val exceptionQueue =  sess.getQueue("XTAG", EXCEPTION_QUEUE_NAME) as AQQueue// sess.createQueue("XTAG.$EXCEPTION_QUEUE_NAME") //
//      val dpsQueue = sess.getQueue("XTAG", "XTAG_DPS") as AQQueue // sess.createQueue("XTAG.XTAG_DPS") //
//      val dequeueOption = AQDequeueOption().apply { waitTime = DEQUEUE_NO_WAIT }
//      val enqueueOption = AQEnqueueOption()
//
//      sqlRepository.getExceptionMessageIdsBytes().forEach { messageId ->
//        dequeueOption.messageId = messageId
//        exceptionQueue.dequeue(dequeueOption)?.also {
//          dpsQueue.enqueue(enqueueOption, it)
//        }
//      }
//    }
  }

  private fun requeueAndIsMore(
    tableOwner: String,
    selector: String,
  ): Boolean = transactionTemplate.execute {
    retryJmsTemplate.receiveSelected("$tableOwner.$EXCEPTION_QUEUE_NAME", selector)
      ?.also { message ->
        log.debug("Got message ${message.jmsMessageID} from exception queue")

        retryJmsTemplate.send { message }
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
