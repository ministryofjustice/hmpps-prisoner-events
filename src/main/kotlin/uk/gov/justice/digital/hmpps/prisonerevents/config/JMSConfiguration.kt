package uk.gov.justice.digital.hmpps.prisonerevents.config

import jakarta.jms.ConnectionFactory
import jakarta.jms.ExceptionListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.connection.JmsTransactionManager
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer
import org.springframework.jms.support.destination.JmsDestinationAccessor
import org.springframework.util.ErrorHandler
import uk.gov.justice.digital.hmpps.prisonerevents.service.JMSReceiver
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
    jmsReceiver: JMSReceiver,
    @Value("\${jms.connection.concurrentConsumers:1}") concurrentConsumers: Int,
  ): DefaultMessageListenerContainer =
    DefaultMessageListenerContainer().apply {
      this.destinationName = FULL_QUEUE_NAME
      this.connectionFactory = listenerConnectionFactory
      this.cacheLevel = DefaultMessageListenerContainer.CACHE_SESSION
      this.concurrentConsumers = concurrentConsumers
      this.exceptionListener = ExceptionListener {
        log.error("DefaultMessageListenerContainer exceptionListener detected error:", it)
      }
      this.errorHandler = ErrorHandler {
        log.error("DefaultMessageListenerContainer errorHandler detected error:", it)
      }
      this.isSessionTransacted = true

      val manager = JmsTransactionManager()
      manager.connectionFactory = listenerConnectionFactory

      this.setTransactionManager(manager)

      this.messageListener = jmsReceiver
    }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
