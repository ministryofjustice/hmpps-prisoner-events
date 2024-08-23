package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.date

class Person(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Person>(Persons)

  var personId by Persons.id
  var lastName by Persons.lastName
  var firstName by Persons.firstName
  var dateOfBirth by Persons.dateOfBirth
}

object Persons : IdTable<Long>("PERSONS") {
  override val id: Column<EntityID<Long>> = long("PERSON_ID").autoIncrement("PERSON_ID").entityId()
  val lastName = varchar("LAST_NAME", 35)
  val firstName = varchar("FIRST_NAME", 35)
  val dateOfBirth = date("BIRTHDATE").nullable()
}
