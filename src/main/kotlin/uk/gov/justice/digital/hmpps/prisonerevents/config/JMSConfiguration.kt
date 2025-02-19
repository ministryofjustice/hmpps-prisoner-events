package uk.gov.justice.digital.hmpps.prisonerevents.config

import jakarta.jms.ConnectionFactory
import jakarta.jms.ExceptionListener
import oracle.jakarta.jms.AQjmsOracleDebug
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.util.ErrorHandler
import uk.gov.justice.digital.hmpps.prisonerevents.service.XtagEventsListener
import javax.sql.DataSource

const val TABLE_OWNER = "XTAG"
const val QUEUE_NAME = "XTAG_DPS"
const val FULL_QUEUE_NAME = "$TABLE_OWNER.$QUEUE_NAME"
const val EXCEPTION_QUEUE_NAME = "AQ\$_XTAG_LISTENER_TAB_E"

@Configuration
class JMSConfiguration {
  @Bean
  fun retryJmsTemplate(retryConnectionFactory: ConnectionFactory) = JmsTemplate(retryConnectionFactory).apply {
    isSessionTransacted = true
    receiveTimeout = JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT
    defaultDestinationName = FULL_QUEUE_NAME
  }

  @Bean
  fun messageListenerContainer(
    listenerConnectionFactory: ConnectionFactory,
    dataSource: DataSource,
    xtagEventsListener: XtagEventsListener,
    @Value("\${jms.connection.concurrentConsumers:1}") concurrentConsumers: Int,
    @Value("\${jms.connection.maxConcurrentConsumers:1}") maxConcurrentConsumers: Int,
    @Value("\${jms.connection.oracle.traceLevel:0}") oracleTraceLevel: Int,
  ): DefaultMessageListenerContainer = DefaultMessageListenerContainer().apply {
    this.destinationName = FULL_QUEUE_NAME
    this.connectionFactory = listenerConnectionFactory
    this.cacheLevel = DefaultMessageListenerContainer.CACHE_SESSION
    this.maxConcurrentConsumers = maxConcurrentConsumers
    this.concurrentConsumers = concurrentConsumers
    this.exceptionListener = ExceptionListener {
      log.error("DefaultMessageListenerContainer exceptionListener detected error:", it)
    }
    this.errorHandler = ErrorHandler {
      log.error("DefaultMessageListenerContainer errorHandler detected error:", it)
    }
    this.isSessionTransacted = true

    this.messageListener = xtagEventsListener

    AQjmsOracleDebug.setTraceLevel(oracleTraceLevel)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
