package uk.gov.justice.digital.hmpps.prisonerevents.builders

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offender
import java.time.LocalDate
import java.time.LocalDateTime

fun Offender.Companion.build(
  offenderNo: String = "A1234KT",
  idSource: String = "SEQ",
  lastName: String = "DAVIS",
  firstName: String = "BLODWYN",
  dateOfBirth: LocalDate = LocalDate.now().minusYears(23),
  sexCode: String = "F",
  rootOffenderId: Long? = null,
  init: Offender.() -> Unit = {},
): Offender {
  val offenderId = transaction {
    exec("SELECT OFFENDER_ID.NEXTVAL FROM DUAL") { resultSet ->
      check(resultSet.next())
      resultSet.getLong(1)
    }
  } ?: error("Could not generate offender ID")
  return Offender.new(id = offenderId) {
    this.offenderNo = offenderNo
    this.idSource = idSource
    this.lastName = lastName
    this.firstName = firstName
    this.dateOfBirth = dateOfBirth
    this.sexCode = sexCode
    this.createDate = LocalDateTime.now()
    this.lastNameKey = lastName
    this.rootOffenderId = rootOffenderId ?: offenderId
    init()
  }
}
