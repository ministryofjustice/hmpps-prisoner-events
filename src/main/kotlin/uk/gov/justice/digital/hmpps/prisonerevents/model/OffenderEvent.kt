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
) {
  override fun toString() =
    "(eventType=$eventType, offenderIdDisplay=$offenderIdDisplay, bookingId=$bookingId, eventDatetime=$eventDatetime, offenderId=$offenderId, nomisEventType=$nomisEventType)"
}

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
class NonAssociationDetailsOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  bookingId: Long?,
  offenderIdDisplay: String?,
  nomisEventType: String?,

  val nsOffenderIdDisplay: String?,
  val nsBookingId: Long?,
  val reasonCode: String?,
  val levelCode: String? = null,

  val nsType: String?,
  val typeSeq: Int?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val authorisedBy: String?,
  val comment: String?,

  val auditModuleName: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class RestrictionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,

  val offenderRestrictionId: Long?,
  val restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val comment: String?,
  val authorisedById: Long?,
  val enteredById: Long?,
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
  val contactPersonId: Long?,
  val offenderPersonRestrictionId: Long?,
  val restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val authorisedById: Long?,
  val comment: String?,
  val enteredById: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class VisitorRestrictionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String? = null,

  val personId: Long?,
  val restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val comment: String?,
  val visitorRestrictionId: Long?,
  val enteredById: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PrisonerActivityUpdateEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  val prisonId: String?,
  val action: String?,
  val user: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PrisonerAppointmentUpdateEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  val prisonId: String?,
  val action: String?,
  val user: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class AssessmentUpdateEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  bookingId: Long?,
  val assessmentSeq: Long?,
  val assessmentType: String?,
  val evaluationResultCode: String?,
  val reviewLevelSupType: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderIdentifierUpdatedEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderId: Long?,
  offenderIdDisplay: String? = null,
  val rootOffenderId: Long?,
  val identifierType: String?,
  val identifierValue: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderChargeUpdatedEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val chargeId: Long?,
  val recordDeleted: Boolean,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class AgencyInternalLocationUpdatedEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val internalLocationId: Long?,
  val prisonId: String?,
  val description: String?,
  val oldDescription: String?,
  val auditModuleName: String?,
  val recordDeleted: Boolean,
  val usageLocationId: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderBookingReassignedEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  offenderId: Long,
  val previousOffenderId: Long,
  var previousOffenderIdDisplay: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
  offenderId = offenderId,
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
  val bookingNumber: String? = null,
  val previousBookingNumber: String? = null,
  val sanctionSeq: Long? = null,
  val movementSeq: Long? = null,
  val imprisonmentStatusSeq: Long? = null,
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
