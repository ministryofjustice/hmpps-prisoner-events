package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.jdbc.select
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class ExposeRepository {
  fun getPersonIdFromContact(contactId: Long): Long? = OffenderContactPersons
    .select(OffenderContactPersons.person)
    .where(OffenderContactPersons.id eq contactId)
    .singleOrNull()
    ?.get(Persons.id)?.value

  fun getNomsIdFromContact(contactId: Long) = (
    OffenderContactPersons innerJoin
      OffenderBookings.join(
        Offenders,
        joinType = JoinType.INNER,
        onColumn = OffenderBookings.offender,
        otherColumn = Offenders.id,
      )
    )
    .select(Offenders.offenderNo)
    .where(OffenderContactPersons.id eq contactId)
    .singleOrNull()
    ?.get(Offenders.offenderNo)

  fun findRelatedMerge(bookingId: Long, eventDatetime: LocalDateTime): MergeTransaction? = MergeTransaction.find {
    (MergeTransactions.bookingId2 eq bookingId) and (MergeTransactions.modifyDatetime greater eventDatetime.minusMinutes(10))
  }
    .orderBy(MergeTransactions.createDatetime to SortOrder.DESC)
    .firstOrNull()

  fun getBookingStartDateForOffenderBooking(bookingId: Long): LocalDateTime? = OffenderBookings
    .select(OffenderBookings.beginDate)
    .where(OffenderBookings.id eq bookingId)
    .singleOrNull()
    ?.get(OffenderBookings.beginDate)

  fun getLastAdmissionDateForOffenderBooking(bookingId: Long): LocalDate? = OffenderBookings
    .join(OffenderExternalMovements, JoinType.INNER, additionalConstraint = {
      (OffenderExternalMovements.offenderBookingId eq OffenderBookings.id) and
        (OffenderExternalMovements.type eq "ADM")
    })
    .select(OffenderExternalMovements.date)
    .where(OffenderBookings.id eq bookingId)
    .orderBy(OffenderExternalMovements.sequence, SortOrder.DESC)
    .firstOrNull()
    ?.get(OffenderExternalMovements.date)

  fun getOffenderById(offenderId: Long): Offender? = Offender.findById(offenderId)

  fun getBookingIdFromChargeId(offenderChargeId: Long): Long? = OffenderCharges
    .select(OffenderCharges.offenderBooking)
    .where(OffenderCharges.id eq offenderChargeId)
    .singleOrNull()
    ?.get(OffenderBookings.id)?.value
}
