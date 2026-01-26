package uk.gov.justice.digital.hmpps.prisonerevents.integration

import org.slf4j.LoggerFactory
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.IOException
import java.net.Socket

object OracleContainerConfig {
  val instance: OracleContainer? by lazy { startOracleContainer() }

  private fun startOracleContainer(): OracleContainer? {
    if (oracleIsRunning()) {
      log.warn("Using existing Oracle-XE database")
      return null
    }
    log.info("Creating an Oracle-XE database")
    return OracleContainer("gvenzl/oracle-xe:21-slim").apply {
      withExposedPorts(1521)
      setWaitStrategy(Wait.forLogMessage(".*DATABASE IS READY TO USE.*", 1))
      withReuse(true)
      start()
    }
  }

  private fun oracleIsRunning(): Boolean = try {
    Socket("127.0.0.1", 1521)
    true
  } catch (e: IOException) {
    false
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
