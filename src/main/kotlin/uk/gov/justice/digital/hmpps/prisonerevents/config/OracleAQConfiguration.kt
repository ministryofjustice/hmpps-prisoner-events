package uk.gov.justice.digital.hmpps.prisonerevents.config

import jakarta.jms.QueueConnectionFactory
import oracle.jakarta.jms.AQjmsFactory
import oracle.jdbc.datasource.impl.OracleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.connection.CachingConnectionFactory
import javax.sql.DataSource

@Configuration
class OracleAQConfiguration {
  @Value("\${spring.datasource.username:dummy}")
  private lateinit var user: String

  @Value("\${spring.datasource.password:dummy}")
  private lateinit var password: String

  @Value("\${spring.datasource.url}")
  private lateinit var connectionstring: String

  // see https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/jdbc/OracleConnection.html

  @Bean
  fun dataSource(): DataSource {
    val ds = OracleDataSource()
    ds.user = user
    ds.setPassword(password)
    ds.url = connectionstring
    ds.setConnectionProperty("autocommit", "false")
    return ds
  }

  @Bean
  fun listenerConnectionFactory(dataSource: DataSource): QueueConnectionFactory =
    AQjmsFactory.getQueueConnectionFactory(dataSource) // Should not cache this connection factory according to Spring documentation

  @Bean
  fun retryConnectionFactory(dataSource: DataSource): QueueConnectionFactory =
    CachingConnectionFactory(AQjmsFactory.getQueueConnectionFactory(dataSource)) // Should cache this connection factory
}
