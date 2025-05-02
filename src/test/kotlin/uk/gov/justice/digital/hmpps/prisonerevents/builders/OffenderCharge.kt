package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderCharge

fun OffenderCharge.Companion.build(
  offenderBooking: OffenderBooking,
  statuteCode: String = "AB",
  offenceCode: String = "CD",
  caseId: Long = 8765L,
  init: OffenderCharge.() -> Unit = {},
) = OffenderCharge.new {
  this.offenderBooking = offenderBooking
  this.statuteCode = statuteCode
  this.offenceCode = offenceCode
  this.caseId = caseId
  init()
}
