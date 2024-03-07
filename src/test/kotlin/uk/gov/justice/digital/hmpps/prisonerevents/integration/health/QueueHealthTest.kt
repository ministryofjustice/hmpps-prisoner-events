package uk.gov.justice.digital.hmpps.prisonerevents.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.health.QueueHealth
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService

class QueueHealthTest {
  private val aqService: AQService = mock()

  private val messagesOnDLQCount = 789
  private val queueHealth = QueueHealth(aqService)

  @Test
  fun `should show status UP`() {
    mockHealthyQueue()

    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.UP)
  }

  @Test
  fun `should show DLQ status UP`() {
    mockHealthyQueue()
    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("UP")
  }

  @Test
  fun `should show DLQ name`() {
    mockHealthyQueue()
    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isEqualTo(EXCEPTION_QUEUE_NAME)
  }

  @Test
  fun `should show interesting DLQ attributes`() {
    mockHealthyQueue()
    val health = queueHealth.health()

    assertThat(health.details["messagesOnDlq"]).isEqualTo("$messagesOnDLQCount")
  }

  @Test
  fun `should show status DOWN if DLQ status is down`() {
    mockDownQueue()
    val health = queueHealth.health()

    assertThat(health.status).isEqualTo(Status.DOWN)
    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  @Test
  fun `should show DLQ name if DLQ status is down`() {
    mockDownQueue()
    val health = queueHealth.health()

    assertThat(health.details["dlqName"]).isEqualTo(EXCEPTION_QUEUE_NAME)
  }

  @Test
  fun `should show exception causing DLQ status DOWN`() {
    mockDownQueue()
    val health = queueHealth.health()

    assertThat(health.details["error"] as String).contains("Exception")
  }

  @Test
  fun `should show DLQ status DOWN if unable to retrieve DLQ attributes`() {
    mockDownQueue()
    val health = queueHealth.health()

    assertThat(health.details["dlqStatus"]).isEqualTo("DOWN")
  }

  private fun mockHealthyQueue() {
    whenever(aqService.exceptionQueueMessageCount()).thenReturn(messagesOnDLQCount)
  }

  private fun mockDownQueue() {
    whenever(aqService.exceptionQueueMessageCount()).thenThrow(RuntimeException::class.java)
  }
}
