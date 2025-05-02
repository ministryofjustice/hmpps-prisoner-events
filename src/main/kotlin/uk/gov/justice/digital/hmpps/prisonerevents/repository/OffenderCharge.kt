package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class OffenderCharge(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<OffenderCharge>(OffenderCharges)
  var offenderChargeId by OffenderCharges.id
  var offenderBooking by OffenderBooking referencedOn OffenderCharges.offenderBooking
  var statuteCode by OffenderCharges.statuteCode
  var offenceCode by OffenderCharges.offenceCode
  var caseId by OffenderCharges.caseId
}

object OffenderCharges : IdTable<Long>("OFFENDER_CHARGES") {
  override val id: Column<EntityID<Long>> = long("OFFENDER_CHARGE_ID").autoIncrement("OFFENDER_CHARGE_ID").entityId()
  val offenderBooking = reference("OFFENDER_BOOK_ID", OffenderBookings)
  val statuteCode = varchar("STATUTE_CODE", 12)
  val offenceCode = varchar("OFFENCE_CODE", 25)
  val caseId = long("CASE_ID")
}
