package uk.gov.justice.digital.hmpps.prisonerevents.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer
import uk.gov.justice.digital.hmpps.prisonerevents.service.JMSReceiver
import javax.jms.ConnectionFactory
import javax.sql.DataSource

@Configuration
class JMSConfiguration(@Value("\${source.queue.transacted}") val transacted: Boolean) {

  @Bean
  fun jmsTemplate(conFactory: ConnectionFactory) =
    JmsTemplate().apply {
      this.isSessionTransacted = transacted
      this.connectionFactory = conFactory
    }

  @Bean
  fun messageListenerContainer(connectionFactory: ConnectionFactory, dataSource: DataSource, jmsReceiver: JMSReceiver) =
    DefaultMessageListenerContainer().apply {
      this.destinationName = "XTAG.XTAG_DPS"
      this.isSessionTransacted = transacted
      this.connectionFactory = connectionFactory

      val manager = DataSourceTransactionManager()
      manager.dataSource = dataSource
      this.setTransactionManager(manager)

      this.messageListener = jmsReceiver
    }
}
