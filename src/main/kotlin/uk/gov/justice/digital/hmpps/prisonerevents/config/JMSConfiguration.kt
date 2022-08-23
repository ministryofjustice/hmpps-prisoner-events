package uk.gov.justice.digital.hmpps.prisonerevents.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer
import uk.gov.justice.digital.hmpps.prisonerevents.service.JMSReceiver
import javax.jms.ConnectionFactory
import javax.sql.DataSource

@Configuration
class JMSConfiguration {

  @Autowired
  lateinit var jmsReceiver: JMSReceiver

  @Bean
  fun jmsTemplate(conFactory: ConnectionFactory) =
    JmsTemplate().apply {
      this.isSessionTransacted = true
      this.connectionFactory = conFactory
    }

  @Bean
  fun messageListenerContainer(conFactory: ConnectionFactory, dataSource: DataSource) =
    DefaultMessageListenerContainer().apply {
      this.destinationName = "XTAG.XTAG_DPS"
      this.isSessionTransacted = true
      this.connectionFactory = conFactory

      val manager = DataSourceTransactionManager()
      manager.dataSource = dataSource
      this.setTransactionManager(manager)

      this.messageListener = jmsReceiver
    }
}
