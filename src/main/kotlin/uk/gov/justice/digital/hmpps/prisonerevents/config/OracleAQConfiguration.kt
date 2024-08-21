package uk.gov.justice.digital.hmpps.prisonerevents.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.jms.QueueConnectionFactory
import oracle.jakarta.jms.AQjmsFactory
import oracle.jdbc.datasource.impl.OracleDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jms.connection.CachingConnectionFactory
import javax.sql.DataSource

@Configuration
class OracleAQConfiguration {
  // see https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/jdbc/OracleConnection.html
  @Bean
  fun listenerDataSource(dataSourceProperties: DataSourceProperties): DataSource = OracleDataSource().apply {
    this.url = dataSourceProperties.url
    this.user = dataSourceProperties.username
    this.setPassword(dataSourceProperties.password)
    this.setConnectionProperty("autocommit", "false")
  }

  @Bean
  @Primary
  fun dataSource(dataSourceProperties: DataSourceProperties, poolConfig: HikariPoolConfiguration): DataSource = HikariDataSource(
    // since the data source is not auto-configured we need to set the hikari properties manually from config and set
    // the data source properties as well
    poolConfig.apply {
      this.jdbcUrl = dataSourceProperties.url
      this.username = dataSourceProperties.username
      this.password = dataSourceProperties.password
    },
  )

  @Configuration
  @ConfigurationProperties("spring.datasource.hikari")
  class HikariPoolConfiguration : HikariConfig()

  @Bean
  fun listenerConnectionFactory(@Qualifier("listenerDataSource") dataSource: DataSource): QueueConnectionFactory =
    AQjmsFactory.getQueueConnectionFactory(dataSource) // Should not cache this connection factory according to Spring documentation

  @Bean
  fun retryConnectionFactory(@Qualifier("listenerDataSource") dataSource: DataSource): QueueConnectionFactory =
    CachingConnectionFactory(AQjmsFactory.getQueueConnectionFactory(dataSource)) // Should cache this connection factory
}
