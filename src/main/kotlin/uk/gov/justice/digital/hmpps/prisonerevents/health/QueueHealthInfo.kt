package uk.gov.justice.digital.hmpps.prisonerevents.health

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.Health.Builder
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService
import kotlin.Result.Companion.success

@Component("$QUEUE_NAME-health")
class QueueHealthInfo(private val aqService: AQService, private val request: HttpServletRequest) : InfoContributor {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun contribute(builder: Info.Builder) {
    val showQueueDetails = request.getParameter("show-queue-details")?.toBoolean() ?: false
    if (showQueueDetails) {
      builder.withDetail("$QUEUE_NAME-health", buildHealth(checkDlqHealth()))
    }
  }

  @JvmInline
  private value class HealthDetail(private val detail: Pair<String, String>) {
    fun key() = detail.first
    fun value() = detail.second
  }

  private fun checkDlqHealth(): List<Result<HealthDetail>> = buildList {
    add(success(HealthDetail("dlqName" to EXCEPTION_QUEUE_NAME)))
    add(check { HealthDetail("messagesOnDlq" to "${aqService.exceptionQueueMessageCount()}") })
    add(check { HealthDetail("messagesOnQueue" to "${aqService.queueMessageCount()}") })
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

  private fun dlqStatus(dlqResults: List<Result<HealthDetail>>): String = if (dlqResults.any { it.isFailure }) "DOWN" else "UP"

  private fun Builder.addHealthResult(result: Result<HealthDetail>) = result
    .onSuccess { healthDetail -> withDetail(healthDetail.key(), healthDetail.value()) }
    .onFailure { throwable ->
      withException(throwable)
        .also { log.error("Queue health for queueId $EXCEPTION_QUEUE_NAME failed due to exception", throwable) }
    }

  private fun check(check: () -> HealthDetail): Result<HealthDetail> = runCatching {
    success(check())
  }.onFailure { throwable -> return Result.failure(throwable) }.getOrThrow()
}
