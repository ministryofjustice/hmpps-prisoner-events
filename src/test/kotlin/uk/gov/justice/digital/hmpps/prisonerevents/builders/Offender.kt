package uk.gov.justice.digital.hmpps.prisonerevents.builders

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
  init: Offender.() -> Unit = {},
) = Offender.new {
  this.offenderNo = offenderNo
  this.idSource = idSource
  this.lastName = lastName
  this.firstName = firstName
  this.dateOfBirth = dateOfBirth
  this.sexCode = sexCode
  this.createDate = LocalDateTime.now()
  this.lastNameKey = lastName
  init()
}
