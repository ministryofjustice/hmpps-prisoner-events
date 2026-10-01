package uk.gov.justice.digital.hmpps.prisonerevents.builders

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Address

fun Address.Companion.build(
  ownerClass: String = "OFF",
  ownerId: Long? = null,
  init: Address.() -> Unit = {},
): Address {
  val addressId = transaction {
    exec("SELECT ADDRESS_ID.NEXTVAL FROM DUAL") { resultSet ->
      check(resultSet.next())
      resultSet.getLong(1)
    }
  } ?: error("Could not generate address ID")
  return Address.new(id = addressId) {
    this.ownerClass = ownerClass
    this.ownerId = ownerId
    this.primaryFlag = "Y"
    this.mailFlag = "Y"
    init()
  }
}
