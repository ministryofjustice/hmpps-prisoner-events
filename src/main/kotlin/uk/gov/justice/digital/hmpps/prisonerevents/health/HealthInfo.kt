package uk.gov.justice.digital.hmpps.prisonerevents.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository

/**
 * Adds version data to the /health endpoint. This is called by the UI to display API details
 */
@Component
class HealthInfo(
  buildProperties: BuildProperties,
  val sqlRepository: SqlRepository,
) : HealthIndicator {
  private val version: String = buildProperties.version

  override fun health(): Health {
    val exceptionCount = sqlRepository.getExceptionCount()
    return Health.up().withDetail("version", version).withDetail("oracleDLQ", exceptionCount).build()
  }
}
