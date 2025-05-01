package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

class OffenderContactPerson(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<OffenderContactPerson>(OffenderContactPersons)

  var contactId by OffenderContactPersons.id
  var offenderBooking by OffenderBooking referencedOn OffenderContactPersons.offenderBooking
  var person by Person optionalReferencedOn OffenderContactPersons.person
  var contactType by OffenderContactPersons.contactType
  var relationshipType by OffenderContactPersons.relationshipType
  var createUserName by OffenderContactPersons.createUserName
  var modifyUserName by OffenderContactPersons.modifyUserName
}

object OffenderContactPersons : IdTable<Long>("OFFENDER_CONTACT_PERSONS") {
  override val id: Column<EntityID<Long>> = long("OFFENDER_CONTACT_PERSON_ID").autoIncrement("OFFENDER_CONTACT_PERSON_ID").entityId()
  val offenderBooking = reference("OFFENDER_BOOK_ID", OffenderBookings)
  val person = reference("PERSON_ID", Persons).nullable()
  val contactType = varchar("CONTACT_TYPE", 12)
  val relationshipType = varchar("RELATIONSHIP_TYPE", 12)
  val createUserName = varchar("CREATE_USER_ID", 32)
  val modifyUserName = varchar("MODIFY_USER_ID", 32).nullable()
}
