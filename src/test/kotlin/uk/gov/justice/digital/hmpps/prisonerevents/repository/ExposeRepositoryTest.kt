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
      Addresses.deleteAll()
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

  @Test
  @Transactional
  fun `get root offender id and prison number from address id when owner class is OFF`() {
    transaction {
      val offender = Offender.build(offenderNo = "A1234BB") {}
      val address = Address.build(ownerClass = "OFF", ownerId = offender.rootOffenderId) {}

      assertThat(repository.getRootOffenderIdAndPrisonNumberFromAddressId(address.addressId.value))
        .isEqualTo(offender.rootOffenderId to offender.offenderNo)
    }
  }

  @Test
  @Transactional
  fun `get root offender id and prison number from address id returns null when owner class is not OFF`() {
    transaction {
      val offender = Offender.build(offenderNo = "A1234CC") {}
      val address = Address.build(ownerClass = "CORP", ownerId = offender.rootOffenderId) {}

      assertThat(repository.getRootOffenderIdAndPrisonNumberFromAddressId(address.addressId.value)).isNull()
    }
  }

  @Test
  @Transactional
  fun `get root offender id and prison number from address id returns null when address does not exist`() {
    transaction {
      assertThat(repository.getRootOffenderIdAndPrisonNumberFromAddressId(999_999L)).isNull()
    }
  }

  @Test
  @Transactional
  fun `get root offender id and prison number from address id returns the first match when multiple offenders share a root offender id`() {
    transaction {
      val rootOffender = Offender.build(offenderNo = "A1234DD") {}
      Offender.build(offenderNo = "A1234DD", rootOffenderId = rootOffender.offenderId.value) {}
      val address = Address.build(ownerClass = "OFF", ownerId = rootOffender.offenderId.value) {}

      val result = repository.getRootOffenderIdAndPrisonNumberFromAddressId(address.addressId.value)

      assertThat(result?.first).isEqualTo(rootOffender.offenderId.value)
      assertThat(result?.second).isIn("A1234DD", "A1234EE")
    }
  }
}
