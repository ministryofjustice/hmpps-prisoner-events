package uk.gov.justice.digital.hmpps.prisonerevents.builders

import org.jetbrains.exposed.dao.id.CompositeID
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderExternalMovement
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderExternalMovements
import java.time.LocalDate
import java.time.LocalDateTime

fun OffenderExternalMovement.Companion.build(
  sequence: Int = 1,
  offenderBooking: OffenderBooking,
  date: LocalDate = LocalDate.now(),
  time: LocalDateTime = LocalDateTime.now(),
  type: String = "TRN",
  direction: String = "IN",
  fromAgency: String = "MDI",
  toAgency: String = "WWI",
  init: OffenderExternalMovement.() -> Unit = {},
) = CompositeID {
  it[OffenderExternalMovements.sequence] = sequence
  it[OffenderExternalMovements.offenderBookingId] = offenderBooking.id.value
}.let {
  OffenderExternalMovement.new(it) {
    this.date = date
    this.time = time
    this.type = type
    this.direction = direction
    this.fromAgency = fromAgency
    this.toAgency = toAgency
    init()
  }
}
