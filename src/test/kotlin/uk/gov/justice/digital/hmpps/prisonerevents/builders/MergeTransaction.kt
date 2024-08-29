package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.MergeTransaction
import java.time.LocalDateTime

fun MergeTransaction.Companion.build(
  status: String = "COMPLETED",
  requestDate: LocalDateTime = LocalDateTime.now(),
  offenderNo1: String,
  offenderNo2: String,
  bookingId1: Long,
  bookingId2: Long,
  offenderId1: Long,
  offenderId2: Long,
  rootOffenderId1: Long = offenderId1,
  rootOffenderId2: Long = offenderId2,
  createDatetime: LocalDateTime = LocalDateTime.now(),
  modifyDatetime: LocalDateTime = LocalDateTime.now(),
  init: MergeTransaction.() -> Unit = {},
) = MergeTransaction.new {
  this.status = status
  this.requestDate = requestDate
  this.offenderNo1 = offenderNo1
  this.offenderNo2 = offenderNo2
  this.bookingId1 = bookingId1
  this.bookingId2 = bookingId2
  this.offenderId1 = offenderId1
  this.offenderId2 = offenderId2
  this.rootOffenderId1 = rootOffenderId1
  this.rootOffenderId2 = rootOffenderId2
  this.createDatetime = createDatetime
  this.modifyDatetime = modifyDatetime
  init()
}
