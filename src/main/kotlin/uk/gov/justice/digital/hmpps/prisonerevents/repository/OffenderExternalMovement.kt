package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

class OffenderExternalMovement(id: EntityID<CompositeID>) : CompositeEntity(id) {
  companion object : CompositeEntityClass<OffenderExternalMovement>(OffenderExternalMovements)

  var sequence by OffenderExternalMovements.sequence
  var offenderBookingId by OffenderExternalMovements.offenderBookingId
  var date by OffenderExternalMovements.date
  var time by OffenderExternalMovements.time
  var type by OffenderExternalMovements.type
  var direction by OffenderExternalMovements.direction
  var fromAgency by OffenderExternalMovements.fromAgency
  var toAgency by OffenderExternalMovements.toAgency
}

object OffenderExternalMovements : CompositeIdTable("OFFENDER_EXTERNAL_MOVEMENTS") {
  val sequence = integer("MOVEMENT_SEQ").entityId()
  val offenderBookingId = long("OFFENDER_BOOK_ID").entityId()
  val date = date("MOVEMENT_DATE")
  val time = datetime("MOVEMENT_TIME")
  val type = varchar("MOVEMENT_TYPE", 12).nullable()
  val direction = varchar("DIRECTION_CODE", 12).nullable()
  val fromAgency = varchar("FROM_AGY_LOC_ID", 6).nullable()
  val toAgency = varchar("TO_AGY_LOC_ID", 6).nullable()
  override val primaryKey = PrimaryKey(sequence, offenderBookingId)
}
