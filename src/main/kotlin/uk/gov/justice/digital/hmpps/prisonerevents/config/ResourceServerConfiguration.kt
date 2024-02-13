package uk.gov.justice.digital.hmpps.prisonerevents.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer

@Configuration
class ResourceServerConfiguration {

  @Bean
  fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
    unauthorizedRequestPaths {
      addPaths = setOf("/housekeeping")
    }
  }
}
