package uk.gov.justice.digital.hmpps.prisonerevents.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
open class OffenderEvent(
  val eventType: String? = null,
  val eventDatetime: LocalDateTime? = null,
  val bookingId: Long? = null,
  val offenderId: Long? = null,
  var offenderIdDisplay: String? = null,
  val nomisEventType: String? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class AlertOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,

  val rootOffenderId: Long? = null,
  val alertSeq: Long? = null,
  val alertDateTime: LocalDateTime? = null,
  val alertType: String? = null,
  val alertCode: String?,
  val expiryDateTime: LocalDateTime? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExternalMovementOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,

  val movementSeq: Long?,
  var movementDateTime: LocalDateTime?,
  var movementType: String?,
  val movementReasonCode: String?,
  var directionCode: String?,
  val escortCode: String?,
  var fromAgencyLocationId: String?,
  var toAgencyLocationId: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class NonAssociationOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  bookingId: Long?,
  offenderIdDisplay: String?,
  nomisEventType: String?,

  val nsOffenderIdDisplay: String?,
  var nsBookingId: Long?,
  var reasonCode: String?,
  val levelCode: String?,
  var internalLocationFlag: String?,
  val transportFlag: String?,
  var recipNsReasonCode: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class NonAssociationDetailsOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  bookingId: Long?,
  offenderIdDisplay: String?,
  nomisEventType: String?,

  nsOffenderIdDisplay: String?,
  nsBookingId: Long?,
  reasonCode: String?,
  levelCode: String? = null,
  internalLocationFlag: String? = null,
  transportFlag: String? = null,
  recipNsReasonCode: String? = null,

  val nsType: String?,
  val typeSeq: Int?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val authorizedBy: String?,
  val comment: String?,
) : NonAssociationOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
  nsOffenderIdDisplay = nsOffenderIdDisplay,
  nsBookingId = nsBookingId,
  reasonCode = reasonCode,
  levelCode = levelCode,
  internalLocationFlag = internalLocationFlag,
  transportFlag = transportFlag,
  recipNsReasonCode = recipNsReasonCode,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class RestrictionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,

  val restrictionId: Long?,
  var restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  var comment: String?,
  val authorisedBy: Long?,
  var enteredBy: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonRestrictionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  restrictionId: Long?,
  restrictionType: String?,
  effectiveDate: LocalDate?,
  expiryDate: LocalDate?,
  authorizedBy: Long?,
  comment: String?,
  enteredBy: Long?,

  val contactPersonId: Long?,
) : RestrictionOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  restrictionId = restrictionId,
  restrictionType = restrictionType,
  effectiveDate = effectiveDate,
  expiryDate = expiryDate,
  authorisedBy = authorizedBy,
  comment = comment,
  enteredBy = enteredBy,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class VisitorRestrictionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String? = null,

  val personId: Long?,
  var restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  var comment: String?,
  val visitorRestrictionId: Long?,
  var enteredBy: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class GenericOffenderEvent(
  eventType: String? = null,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,

  val eventId: String? = null,
  val scheduleEventId: Long? = null,
  val scheduledStartTime: LocalDateTime? = null,
  val scheduledEndTime: LocalDateTime? = null,
  val scheduleEventClass: String? = null,
  val scheduleEventType: String? = null,
  val scheduleEventSubType: String? = null,
  val scheduleEventStatus: String? = null,
  val recordDeleted: Boolean? = null,
  val rootOffenderId: Long? = null,
  val aliasOffenderId: Long? = null,
  val previousOffenderId: Long? = null,
  val bookingNumber: String? = null,
  val previousBookingNumber: String? = null,
  val sanctionSeq: Long? = null,
  val movementSeq: Long? = null,
  val imprisonmentStatusSeq: Long? = null,
  val assessmentSeq: Long? = null,
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

  // iep data
  val iepSeq: Long? = null,
  val iepLevel: String? = null,

  // visit data
  val visitId: Long? = null,

  val auditModuleName: String? = null,

  val caseNoteType: String? = null,
  val caseNoteSubType: String? = null,

  // sentence and key date adjustments
  val adjustmentId: Long? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)
