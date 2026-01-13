package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.date

class OffenderContactRestriction(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<OffenderContactRestriction>(OffenderContactRestrictions)

  var restrictionId by OffenderContactRestrictions.id
  var offenderContactPerson by OffenderContactPerson referencedOn OffenderContactRestrictions.offenderContactPerson
  var restrictionType by OffenderContactRestrictions.restrictionType
  var effectiveDate by OffenderContactRestrictions.effectiveDate
}

object OffenderContactRestrictions : IdTable<Long>("OFFENDER_PERSON_RESTRICTS") {
  override val id: Column<EntityID<Long>> = long("OFFENDER_PERSON_RESTRICT_ID").autoIncrement("OFFENDER_PERSON_RESTRICT_ID").entityId()
  val offenderContactPerson = reference("OFFENDER_CONTACT_PERSON_ID", OffenderContactPersons)
  val restrictionType = varchar("RESTRICTION_TYPE", 12)
  val effectiveDate = date("RESTRICTION_EFFECTIVE_DATE")
}
