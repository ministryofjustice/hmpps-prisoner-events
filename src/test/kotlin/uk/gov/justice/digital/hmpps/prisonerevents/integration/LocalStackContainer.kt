package uk.gov.justice.digital.hmpps.prisonerevents.integration

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.IOException
import java.net.ServerSocket

object LocalStackContainer {
  val log = LoggerFactory.getLogger(this::class.java)
  val instance by lazy { startLocalstackIfNotRunning() }

  fun setLocalStackProperties(localStackContainer: LocalStackContainer, registry: DynamicPropertyRegistry) =
    localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.SNS)
      .let { it.serviceEndpoint to it.signingRegion }
      .also {
        registry.add("hmpps.sqs.localstackUrl") { it.first }
        registry.add("hmpps.sqs.region") { it.second }
      }

  private fun startLocalstackIfNotRunning(): LocalStackContainer? {
    if (localstackIsRunning()) {
      log.warn("Using existing localstack instance")
      return null
    }
    log.info("Creating a localstack instance")
    val logConsumer = Slf4jLogConsumer(log).withPrefix("localstack")
    return LocalStackContainer(
      DockerImageName.parse("localstack/localstack").withTag("1.3"),
    ).apply {
      withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.SNS)
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withEnv("DEFAULT_REGION", "eu-west-2")
      waitingFor(
        Wait.forLogMessage(".*Ready.*", 1),
      )
      start()
      followOutput(logConsumer)
    }
  }

  private fun localstackIsRunning(): Boolean =
    System.getenv("DOCKER_HOST").contains("colima") ||
      try {
        val serverSocket = ServerSocket(4566)
        serverSocket.localPort == 0
      } catch (e: IOException) {
        true
      }
}
