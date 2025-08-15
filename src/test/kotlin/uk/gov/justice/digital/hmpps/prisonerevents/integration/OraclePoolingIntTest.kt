package uk.gov.justice.digital.hmpps.prisonerevents.integration

import com.zaxxer.hikari.HikariDataSource
import jakarta.jms.ConnectionFactory
import jakarta.jms.MapMessage
import jakarta.jms.MessageProducer
import jakarta.jms.Session
import oracle.jdbc.datasource.impl.OracleDataSource
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.ProducerCallback
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.prisonerevents.config.FULL_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.time.Duration
import javax.sql.DataSource

@SpringBootTest(
  webEnvironment = RANDOM_PORT,
  properties = [
    "jms.connection.concurrentConsumers=3",
    "jms.connection.maxConcurrentConsumers=3",
    "spring.datasource.hikari.maximum-pool-size=4",
  ],
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OraclePoolingIntTest : IntegrationTestBase() {

  @MockitoSpyBean
  private lateinit var hmppsQueueService: HmppsQueueService

  @MockitoSpyBean
  @Qualifier("listenerDataSource")
  private lateinit var listenerDataSource: DataSource

  @MockitoSpyBean
  private lateinit var jdbcDataSource: DataSource

  @Autowired
  private lateinit var retryConnectionFactory: ConnectionFactory

  @Autowired
  private lateinit var sqlRepository: SqlRepository

  private val jmsTemplate by lazy {
    JmsTemplate(retryConnectionFactory).also { it.defaultDestinationName = FULL_QUEUE_NAME }
  }

  internal val prisonEventQueue by lazy { hmppsQueueService.findByQueueId("prisoneventtestqueue") as HmppsQueue }
  internal val prisonEventQueueSqsClient by lazy { prisonEventQueue.sqsClient }
  internal val prisonEventQueueUrl by lazy { prisonEventQueue.queueUrl }

  @BeforeEach
  fun setup() {
    purgeQueues()
  }

  @Nested
  inner class Consume {

    // This is best efforts to confirm connection factories are setup correctly and connection pooling
    // is being used for jdbcTemplate but nt by the listeners
    // Ideally we woul want to look at how many real Oracle connections have been created, but not sure of a good
    // way to do this in an integration test.
    @Test
    fun `will consume all prison offender events message with pooled jdbc connection and connection per listener thread`() {
      val count = 60
      simulateBatchTrigger(count) {
        it.createMapMessage().apply {
          jmsType = "OFF_RECEP_OASYS"
          setLong("p_offender_book_id", 1234567L)
          setInt("p_movement_seq", 4)
          setString("eventType", "OFF_RECEP_OASYS")
        }
      }

      awaitQueueSizeToBe(count)

      // NB The number of connections should match messages and threads buyt will not be exact due to health check beans
      // and test beans being created
      // connection for pooled jdbc connection called as many times as there is message plus 2 other beans
      // but check it is a Hikari pool so we trust it uses a pooled connection
      assertThat(jdbcDataSource is HikariDataSource).isTrue()
      verify(jdbcDataSource, times(62)).connection
      // connection for each of 3 listeners plus 2 jms templates
      assertThat(listenerDataSource is OracleDataSource).isTrue()
      verify(listenerDataSource, times(4)).connection
    }
  }

  private fun simulateBatchTrigger(count: Int, messageCreator: (session: Session) -> MapMessage) {
    val producerCallback: ProducerCallback<*> = ProducerCallback { session: Session, producer: MessageProducer ->
      (1..count).forEach { _ ->
        producer.send(messageCreator(session))
      }
    }
    jmsTemplate.execute(producerCallback)
  }

  fun purgeQueues() {
    prisonEventQueueSqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(prisonEventQueueUrl).build())
    sqlRepository.purgeExceptionQueue()
    awaitQueueSizeToBe(0)
  }

  val maxWait: Duration = Duration.ofSeconds(30)
  val poll: Duration = Duration.ofSeconds(1)

  fun awaitQueueSizeToBe(count: Int) {
    await.atMost(maxWait)
      .pollInterval(poll) untilCallTo { getNumberOfMessagesCurrentlyOnPrisonEventQueue() } matches { it == count }
  }

  fun getNumberOfMessagesCurrentlyOnPrisonEventQueue(): Int = prisonEventQueueSqsClient.countAllMessagesOnQueue(prisonEventQueueUrl).get()
    .also { log.info("Number of messages on prison queue: $it") }
}
