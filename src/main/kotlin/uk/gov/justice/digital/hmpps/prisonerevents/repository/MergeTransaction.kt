package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime

class MergeTransaction(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<MergeTransaction>(MergeTransactions)

  var status by MergeTransactions.status
  var requestDate by MergeTransactions.requestDate
  var offenderNo1 by MergeTransactions.offenderNo1
  var offenderNo2 by MergeTransactions.offenderNo2
  var bookingId1 by MergeTransactions.bookingId1
  var bookingId2 by MergeTransactions.bookingId2
  var offenderId1 by MergeTransactions.offenderId1
  var offenderId2 by MergeTransactions.offenderId2
  var rootOffenderId1 by MergeTransactions.rootOffenderId1
  var rootOffenderId2 by MergeTransactions.rootOffenderId2
  var createDatetime by MergeTransactions.createDatetime
  var modifyDatetime by MergeTransactions.modifyDatetime
}

object MergeTransactions : IdTable<Long>("MERGE_TRANSACTIONS") {
  override val id: Column<EntityID<Long>> = long("MERGE_TRANSACTION_ID").autoIncrement("MERGE_TRANSACTION_ID").entityId()
  val requestDate = datetime("REQUEST_DATE")
  val createDatetime = datetime("CREATE_DATETIME")
  val modifyDatetime = datetime("MODIFY_DATETIME")
  val status = varchar("REQUEST_STATUS_CODE", 12)
  val transactionSource = varchar("TRANSACTION_SOURCE", 12).default("MANUAL")
  val bookingId1 = long("OFFENDER_BOOK_ID_1").nullable()
  val bookingId2 = long("OFFENDER_BOOK_ID_2").nullable()
  val offenderId1 = long("OFFENDER_ID_1").nullable()
  val offenderId2 = long("OFFENDER_ID_2").nullable()
  val rootOffenderId1 = long("ROOT_OFFENDER_ID_1").nullable()
  val rootOffenderId2 = long("ROOT_OFFENDER_ID_2").nullable()
  val offenderNo1 = varchar("OFFENDER_ID_DISPLAY_1", 10)
  val offenderNo2 = varchar("OFFENDER_ID_DISPLAY_2", 10)
}
