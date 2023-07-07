package uk.gov.justice.digital.hmpps.prisonerevents.integration

import org.slf4j.LoggerFactory
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.ServerSocket

object OracleContainerConfig {
  val instance: OracleContainer? by lazy { startOracleContainer() }

  private fun startOracleContainer(): OracleContainer? {
    if (oracleIsRunning()) {
      log.warn("Using existing Oracle-XE database")
      return null
    }
    log.info("Creating an Oracle-XE database")
    return OracleContainer("gvenzl/oracle-xe:18-slim").apply {
      // withExposedPorts(1521)
      setWaitStrategy(Wait.forLogMessage(".*DATABASE IS READY TO USE.*", 1))
      withStartupTimeoutSeconds(500)
      withConnectTimeoutSeconds(400)
      withReuse(true)
      start()
    }
  }

  private fun oracleIsRunning(): Boolean =
    (System.getenv("DOCKER_HOST") ?: "null").contains("colima") ||
      try {
        val serverSocket = ServerSocket(1521)
        serverSocket.localPort == 0
      } catch (e: IOException) {
        true
      }

  private val log = LoggerFactory.getLogger(this::class.java)
}
