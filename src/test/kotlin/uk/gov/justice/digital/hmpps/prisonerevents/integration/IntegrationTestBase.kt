package uk.gov.justice.digital.hmpps.prisonerevents.integration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.prisonerevents.integration.LocalStackContainer.setLocalStackProperties

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  companion object {
    private val log = LoggerFactory.getLogger(IntegrationTestBase::class.java).also {
      it.info("Starting IntegrationTestBase with environment variables: ${System.getenv()}")
    }

    private val localStackContainer = LocalStackContainer.instance
    private val oracleContainer = OracleContainerConfig.instance

    @JvmStatic
    @DynamicPropertySource
    fun localstackProperties(registry: DynamicPropertyRegistry) {
      localStackContainer?.also { setLocalStackProperties(it, registry) }
    }

    @JvmStatic
    @DynamicPropertySource
    fun oracleProperties(registry: DynamicPropertyRegistry) {
      oracleContainer?.also {
        val url = oracleContainer::getJdbcUrl
        registry.add("spring.flyway.url", url)
        registry.add("spring.datasource.url", url)
        log.info("Starting Oracle container with url: ${url()}")
      }
    }
  }
}
