package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.Offender
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import java.time.LocalDateTime

fun OffenderBooking.Companion.build(
  offender: Offender,
  rootOffender: Offender = offender,
  beginDate: LocalDateTime = LocalDateTime.now(),
  inOutStatus: String = "IN",
  youthAdultCode: String = "A",
  sequence: Int = 1,
  init: OffenderBooking.() -> Unit = {},
) = OffenderBooking.new {
  this.offender = offender
  this.rootOffender = rootOffender
  this.beginDate = beginDate
  this.inOutStatus = inOutStatus
  this.youthAdultCode = youthAdultCode
  this.sequence = sequence
  init()
}
