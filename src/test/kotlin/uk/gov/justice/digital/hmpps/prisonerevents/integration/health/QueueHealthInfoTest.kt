package uk.gov.justice.digital.hmpps.prisonerevents.integration.health

import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.Status
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.health.QueueHealthInfo
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService

class QueueHealthInfoTest {
  private val aqService: AQService = mock()
  private val request: HttpServletRequest = mock()

  private val messagesOnDLQCount = 789
  private val messagesOnQueueCount = 7
  private val queueHealth = QueueHealthInfo(aqService, request)

  @BeforeEach
  fun setUp() {
    whenever(request.getParameter("show-queue-details")).thenReturn("true")
  }

  private fun getQueueHealthInfo(): Health {
    val builder = Info.Builder()
    queueHealth.contribute(builder)
    return builder.build().details["$QUEUE_NAME-health"] as Health
  }

  @Test
  fun `should show status UP`() {
    mockHealthyQueue()

    val health = getQueueHealthInfo()
    assertThat(health.status).isEqualTo(Status.UP)
  }

  @Test
  fun `should show DLQ status UP`() {
    mockHealthyQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["dlqStatus"]).isEqualTo("UP")
  }

  @Test
  fun `should show DLQ name`() {
    mockHealthyQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["dlqName"]).isEqualTo(EXCEPTION_QUEUE_NAME)
  }

  @Test
  fun `should show interesting DLQ attributes`() {
    mockHealthyQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["messagesOnDlq"]).isEqualTo("$messagesOnDLQCount")
  }

  @Test
  fun `should show queue depth for main queue`() {
    mockHealthyQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["messagesOnQueue"]).isEqualTo("$messagesOnQueueCount")
  }

  @Test
  fun `should show status DOWN if DLQ status is down`() {
    mockDownQueue()
    val health = getQueueHealthInfo()

    assertThat(health.status).isEqualTo(Status.DOWN)
    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ name if DLQ status is down`() {
    mockDownQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["dlqName"]).isEqualTo(EXCEPTION_QUEUE_NAME)
  }

  @Test
  fun `should show exception causing DLQ status DOWN`() {
    mockDownQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show DLQ status DOWN if unable to retrieve DLQ attributes`() {
    mockDownQueue()
    val health = getQueueHealthInfo()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  private fun mockHealthyQueue() {
    whenever(aqService.exceptionQueueMessageCount()).thenReturn(messagesOnDLQCount)
    whenever(aqService.queueMessageCount()).thenReturn(messagesOnQueueCount)
  }

  private fun mockDownQueue() {
    whenever(aqService.exceptionQueueMessageCount()).thenThrow(RuntimeException::class.java)
  }
}
