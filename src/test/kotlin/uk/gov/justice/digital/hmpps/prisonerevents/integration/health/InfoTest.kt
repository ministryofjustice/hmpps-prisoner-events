package uk.gov.justice.digital.hmpps.prisonerevents.integration.health

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.integration.IntegrationTestBase

class InfoTest(
  @Autowired private val buildProperties: BuildProperties,
) : IntegrationTestBase() {

  @Test
  fun `Info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("hmpps-prisoner-events")
  }

  @Test
  fun `Info page reports version`() {
    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").isEqualTo(buildProperties.version)
  }

  @Test
  fun `Info page reports Nomis queue info when requested`() {
    webTestClient.get()
      .uri("/info?show-queue-details=true")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$QUEUE_NAME-health.status").isEqualTo("UP")
      .jsonPath("$QUEUE_NAME-health.details.dlqStatus").isEqualTo("UP")
      .jsonPath("$QUEUE_NAME-health.details.dlqName").isEqualTo(EXCEPTION_QUEUE_NAME)
      .jsonPath("$QUEUE_NAME-health.details.messagesOnDlq").isEqualTo(0)
  }

  @Test
  fun `Info page does not report Nomis queue info when not requested`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$QUEUE_NAME-health.status").doesNotExist()
  }
}
