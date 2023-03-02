package uk.gov.justice.digital.hmpps.prisonerevents.config

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import oracle.jms.AQjmsFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.jms.QueueConnectionFactory
import javax.sql.DataSource

@Configuration
class OracleAQConfiguration {
  @Value("\${spring.datasource.username:dummy}")
  private lateinit var user: String

  @Value("\${spring.datasource.password:dummy}")
  private lateinit var password: String

  @Value("\${spring.datasource.url}")
  private lateinit var connectionstring: String

  @ConditionalOnProperty(name = ["source.queue.provider"], havingValue = "oracle")
  @Bean
  fun dataSource(): DataSource {
    val ds = oracle.jdbc.pool.OracleDataSource()
    ds.user = user
    ds.setPassword(password)
    ds.url = connectionstring
    ds.implicitCachingEnabled = true
    // Do not reconnect from scratch for each poll:
    ds.connectionCachingEnabled = true
    return ds
  }

  @ConditionalOnProperty(name = ["source.queue.provider"], havingValue = "oracle")
  @Bean
  fun connectionFactory(dataSource: DataSource?): QueueConnectionFactory =
    AQjmsFactory.getQueueConnectionFactory(dataSource)

  @ConditionalOnProperty(name = ["source.queue.provider"], havingValue = "localstack")
  @Bean
  fun connectionFactoryLocalstack(dataSource: DataSource?): QueueConnectionFactory =
    SQSConnectionFactory(
      ProviderConfiguration(),
      AmazonSQSClientBuilder
        .standard()
        .withCredentials(
          AWSStaticCredentialsProvider(
            BasicAWSCredentials("foo", "bar"),
          ),
        )
        .withRegion("eu-west-2")
        .build(),
    )
}
