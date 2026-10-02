package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class Address(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Address>(Addresses)

  var addressId by Addresses.id
  var ownerClass by Addresses.ownerClass
  var ownerId by Addresses.ownerId
  var primaryFlag by Addresses.primaryFlag
  var mailFlag by Addresses.mailFlag
}

object Addresses : IdTable<Long>("ADDRESSES") {
  override val id: Column<EntityID<Long>> = long("ADDRESS_ID").autoIncrement("ADDRESS_ID").entityId()
  val ownerClass = varchar("OWNER_CLASS", 12)
  val ownerId = long("OWNER_ID").nullable()
  val primaryFlag = varchar("PRIMARY_FLAG", 1)
  val mailFlag = varchar("MAIL_FLAG", 1)
}
