package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

class OffenderBooking(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<OffenderBooking>(OffenderBookings)

  var bookingId by OffenderBookings.id
  var beginDate by OffenderBookings.beginDate
  var endDate by OffenderBookings.endDate
  var offender by Offender referencedOn OffenderBookings.offender

  var rootOffender by Offender referencedOn OffenderBookings.rootOffender
  var inOutStatus by OffenderBookings.inOutStatus
  var youthAdultCode by OffenderBookings.youthAdultCode
  var sequence by OffenderBookings.sequence
}

object OffenderBookings : IdTable<Long>("OFFENDER_BOOKINGS") {
  override val id: Column<EntityID<Long>> = long("OFFENDER_BOOK_ID").autoIncrement("OFFENDER_BOOK_ID").entityId()
  val beginDate = datetime("BOOKING_BEGIN_DATE").default(LocalDateTime.now())
  val endDate = datetime("BOOKING_END_DATE").nullable()
  val offender = reference("OFFENDER_ID", Offenders)
  val rootOffender = reference("ROOT_OFFENDER_ID", Offenders)
  val inOutStatus = varchar("IN_OUT_STATUS", 12)
  val youthAdultCode = varchar("YOUTH_ADULT_CODE", 12)
  val sequence = integer("BOOKING_SEQ")
}
