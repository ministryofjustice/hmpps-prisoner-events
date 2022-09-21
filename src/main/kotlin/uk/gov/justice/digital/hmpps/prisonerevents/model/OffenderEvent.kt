package uk.gov.justice.digital.hmpps.prisonerevents.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderEvent(
  val eventId: String? = null,
  val eventType: String? = null,
  val eventDatetime: LocalDateTime? = null,
  val scheduleEventId: Long? = null,
  val scheduledStartTime: LocalDateTime? = null,
  val scheduledEndTime: LocalDateTime? = null,
  val scheduleEventClass: String? = null,
  val scheduleEventType: String? = null,
  val scheduleEventSubType: String? = null,
  val scheduleEventStatus: String? = null,
  val recordDeleted: Boolean? = null,
  val rootOffenderId: Long? = null,
  val offenderId: Long? = null,
  val aliasOffenderId: Long? = null,
  val previousOffenderId: Long? = null,
  var offenderIdDisplay: String? = null,
  val bookingId: Long? = null,
  val bookingNumber: String? = null,
  val previousBookingNumber: String? = null,
  val sanctionSeq: Long? = null,
  val movementSeq: Long? = null,
  val imprisonmentStatusSeq: Long? = null,
  val assessmentSeq: Long? = null,
  val alertSeq: Long? = null,
  val alertDateTime: LocalDateTime? = null,
  val alertType: String? = null,
  val alertCode: String? = null,
  val expiryDateTime: LocalDateTime? = null,
  val caseNoteId: Long? = null,
  val agencyLocationId: String? = null,
  val riskPredictorId: Long? = null,
  val addressId: Long? = null,
  val personId: Long? = null,
  val sentenceCalculationId: Long? = null,
  val oicHearingId: Long? = null,
  val oicOffenceId: Long? = null,
  val pleaFindingCode: String? = null,
  val findingCode: String? = null,
  val resultSeq: Long? = null,
  val agencyIncidentId: Long? = null,
  val chargeSeq: Long? = null,
  val identifierType: String? = null,
  val identifierValue: String? = null,
  val ownerId: Long? = null,
  val ownerClass: String? = null,
  val sentenceSeq: Long? = null,
  val conditionCode: String? = null,
  val offenderSentenceConditionId: Long? = null,
  val addressEndDate: LocalDate? = null,
  val primaryAddressFlag: String? = null,
  val mailAddressFlag: String? = null,
  val addressUsage: String? = null,

  // incident event data
  val incidentCaseId: Long? = null,
  val incidentPartySeq: Long? = null,
  val incidentRequirementSeq: Long? = null,
  val incidentQuestionSeq: Long? = null,
  val incidentResponseSeq: Long? = null,

  // bed assignment data
  val bedAssignmentSeq: Int? = null,
  val livingUnitId: Long? = null,

  // external movement event data
  var movementDateTime: LocalDateTime? = null,
  var movementType: String? = null,
  val movementReasonCode: String? = null,
  var directionCode: String? = null,
  val escortCode: String? = null,
  var fromAgencyLocationId: String? = null,
  var toAgencyLocationId: String? = null,

  // iep data
  val iepSeq: Long? = null,
  val iepLevel: String? = null,

  // visit data
  val visitId: Long? = null,

  val nomisEventType: String? = null,
  val auditModuleName: String? = null,
)
