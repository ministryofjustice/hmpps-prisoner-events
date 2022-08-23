package uk.gov.justice.digital.hmpps.prisonerevents.service.xtag

import oracle.sql.STRUCT
import java.sql.Timestamp

data class XtagEventNonJpa(
  val msgId: String,
  val qName: String? = null,
  val corrID: String? = null,
  val priority: Long? = null,
  val state: Long? = null,
  val delay: Timestamp? = null,
  val expiration: Long? = null,
  val timeManagerInfo: Timestamp? = null,
  val localOrderNo: Long? = null,
  val chainNo: Long? = null,
  val cscn: Long? = null,
  val dscn: Long? = null,
  val enqTime: Timestamp? = null,
  val enqUID: String? = null,
  val enqTID: String? = null,
  val deqTime: Timestamp? = null,
  val deqUID: String? = null,
  val deqTID: String? = null,
  val retryCount: Long? = null,
  val exceptionQSchema: String? = null,
  val exceptionQueue: String? = null,
  val stepNo: Long? = null,
  val recipientKey: Long? = null,
  val dequeueMsgId: String? = null,
  val senderName: String? = null,
  val senderAddress: String? = null,
  val senderProtocol: Long? = null,
  val userData: STRUCT? = null,
  val userProp: STRUCT? = null,
)
