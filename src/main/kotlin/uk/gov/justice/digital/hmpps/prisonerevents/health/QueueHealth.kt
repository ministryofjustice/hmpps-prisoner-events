package uk.gov.justice.digital.hmpps.prisonerevents.health

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService
import kotlin.Result.Companion.success

@Component("$QUEUE_NAME-health")
class QueueHealth(private val aqService: AQService) : HealthIndicator {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun health(): Health = buildHealth(checkDlqHealth())

  @JvmInline
  private value class HealthDetail(private val detail: Pair<String, String>) {
    fun key() = detail.first
    fun value() = detail.second
  }

  private fun checkDlqHealth(): List<Result<HealthDetail>> {
    val results = mutableListOf<Result<HealthDetail>>()
    results += success(HealthDetail("dlqName" to EXCEPTION_QUEUE_NAME))
    runCatching {
      results += success(HealthDetail("messagesOnDlq" to "${aqService.exceptionQueueMessageCount()}"))
    }.onFailure { throwable -> results += Result.failure(throwable) }
    return results.toList()
  }

  private fun buildHealth(dlqResults: List<Result<HealthDetail>>): Health {
    val healthBuilder = if (dlqStatus(dlqResults) == "UP") Builder().up() else Builder().down()
    dlqResults.forEach { healthBuilder.addHealthResult(it) }

    if (dlqResults.isNotEmpty()) {
      healthBuilder.withDetail("dlqStatus", dlqStatus(dlqResults))
      dlqResults.forEach { healthBuilder.addHealthResult(it) }
    }

    return healthBuilder.build()
  }

  private fun dlqStatus(dlqResults: List<Result<HealthDetail>>): String =
    if (dlqResults.any { it.isFailure }) "DOWN" else "UP"

  private fun Builder.addHealthResult(result: Result<HealthDetail>) =
    result
      .onSuccess { healthDetail -> withDetail(healthDetail.key(), healthDetail.value()) }
      .onFailure { throwable ->
        withException(throwable)
          .also { log.error("Queue health for queueId $EXCEPTION_QUEUE_NAME failed due to exception", throwable) }
      }
}
