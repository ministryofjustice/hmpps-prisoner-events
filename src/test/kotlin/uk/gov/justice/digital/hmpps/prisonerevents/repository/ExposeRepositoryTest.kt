package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.prisonerevents.builders.build
import uk.gov.justice.digital.hmpps.prisonerevents.integration.IntegrationTestBase

class ExposeRepositoryTest @Autowired constructor(
  private val repository: ExposeRepository,
) : IntegrationTestBase() {

  @Suppress("SqlWithoutWhere")
  @AfterEach
  fun tearDown() {
    transaction {
      Offenders.deleteAll()
    }
  }

  @Test
  @Transactional
  fun `get root offender by prison number when multiple offenders share a root offender id`() {
    transaction {
      val rootOffender = Offender.build(offenderNo = "A1234AA") {}
      Offender.build(offenderNo = "A1234AA", rootOffenderId = rootOffender.offenderId.value) {}

      assertThat(repository.getRootOffenderByPrisonNumber("A1234AA")).isEqualTo(rootOffender.offenderId.value)
    }
  }
}
