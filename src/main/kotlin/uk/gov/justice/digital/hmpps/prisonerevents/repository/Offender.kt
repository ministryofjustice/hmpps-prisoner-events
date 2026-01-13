package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

class Offender(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Offender>(Offenders)

  var offenderId by Offenders.id
  var offenderNo by Offenders.offenderNo
  var idSource by Offenders.idSource
  var lastName by Offenders.lastName
  var firstName by Offenders.firstName
  var dateOfBirth by Offenders.dateOfBirth
  var sexCode by Offenders.sexCode
  var createDate by Offenders.createDate
  var lastNameKey by Offenders.lastNameKey
}

object Offenders : IdTable<Long>("OFFENDERS") {
  override val id: Column<EntityID<Long>> = long("OFFENDER_ID").autoIncrement("OFFENDER_ID").entityId()
  val offenderNo = varchar("OFFENDER_ID_DISPLAY", 10)
  val idSource = varchar("ID_SOURCE_CODE", 12)
  val lastName = varchar("LAST_NAME", 35)
  val firstName = varchar("FIRST_NAME", 35).nullable()
  val dateOfBirth = date("BIRTH_DATE")
  val sexCode = varchar("SEX_CODE", 12)
  val createDate = datetime("CREATE_DATE").default(LocalDateTime.now())
  val lastNameKey = varchar("LAST_NAME_KEY", 35)
}
