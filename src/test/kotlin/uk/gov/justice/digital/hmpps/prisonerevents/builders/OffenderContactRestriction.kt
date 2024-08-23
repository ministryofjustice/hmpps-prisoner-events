package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPerson
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactRestriction
import java.time.LocalDate

fun OffenderContactRestriction.Companion.build(
  offenderContactPerson: OffenderContactPerson,
  restrictionType: String = "BAN",
  effectiveDate: LocalDate = LocalDate.now(),
  init: OffenderContactRestriction.() -> Unit = {},
) = OffenderContactRestriction.new {
  this.offenderContactPerson = offenderContactPerson
  this.restrictionType = restrictionType
  this.effectiveDate = effectiveDate
  init()
}
