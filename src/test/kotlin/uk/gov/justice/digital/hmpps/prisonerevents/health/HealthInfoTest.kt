package uk.gov.justice.digital.hmpps.prisonerevents.health

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.boot.info.BuildProperties
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import java.util.Properties

@ExtendWith(MockitoExtension::class)
class HealthInfoTest {

  @Mock
  private lateinit var sqlRepository: SqlRepository

  @Test
  fun `should include version info`() {
    val properties = Properties()
    properties.setProperty("version", "somever")
    whenever(sqlRepository.getExceptionCount()).thenReturn(3)
    Assertions.assertThat(HealthInfo(BuildProperties(properties), sqlRepository).health().details)
      .isEqualTo(mapOf("version" to "somever", "oracleDLQ" to 3))
  }
}
