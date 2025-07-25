package uk.gov.justice.digital.hmpps.prisonerevents.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
open class OffenderEvent(
  val eventType: String? = null,
  val eventDatetime: LocalDateTime? = null,
  var bookingId: Long? = null,
  val offenderId: Long? = null,
  var offenderIdDisplay: String? = null,
  val nomisEventType: String? = null,
) {
  override fun toString() = "(eventType=$eventType, offenderIdDisplay=$offenderIdDisplay, bookingId=$bookingId," +
    " eventDatetime=$eventDatetime, offenderId=$offenderId, nomisEventType=$nomisEventType)"
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
open class CSIPReportOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,

  val rootOffenderId: Long? = null,
  val csipReportId: Long? = null,
  val auditModuleName: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CSIPPlanOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,
  rootOffenderId: Long? = null,
  csipReportId: Long? = null,
  auditModuleName: String? = null,

  val csipPlanId: Long? = null,
) : CSIPReportOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,

  rootOffenderId = rootOffenderId,
  csipReportId = csipReportId,
  auditModuleName = auditModuleName,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class CSIPReviewOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,
  rootOffenderId: Long? = null,
  csipReportId: Long? = null,
  auditModuleName: String? = null,

  val csipReviewId: Long? = null,
) : CSIPReportOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,

  rootOffenderId = rootOffenderId,
  csipReportId = csipReportId,
  auditModuleName = auditModuleName,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CSIPAttendeeOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,
  rootOffenderId: Long? = null,
  csipReportId: Long? = null,
  csipReviewId: Long? = null,
  auditModuleName: String? = null,

  val csipAttendeeId: Long? = null,
) : CSIPReviewOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,

  rootOffenderId = rootOffenderId,
  csipReportId = csipReportId,
  csipReviewId = csipReviewId,
  auditModuleName = auditModuleName,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CSIPFactorOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,
  rootOffenderId: Long? = null,
  csipReportId: Long? = null,
  auditModuleName: String? = null,

  val csipFactorId: Long? = null,
) : CSIPReportOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,

  rootOffenderId = rootOffenderId,
  csipReportId = csipReportId,
  auditModuleName = auditModuleName,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CSIPInterviewOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,
  rootOffenderId: Long? = null,
  csipReportId: Long? = null,
  auditModuleName: String? = null,

  val csipInterviewId: Long? = null,
) : CSIPReportOffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,

  rootOffenderId = rootOffenderId,
  csipReportId = csipReportId,
  auditModuleName = auditModuleName,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class IWPDocumentOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime? = null,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  nomisEventType: String? = null,

  val documentId: Long? = null,
  val documentName: String? = null,
  val templateId: Long? = null,
  val templateName: String? = null,
  val auditModuleName: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
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
  var recordInserted: Boolean?,
  var recordDeleted: Boolean?,
  var auditModuleName: String?,
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
  val authorisedById: Long?,
  val enteredById: Long?,
  val auditModuleName: String,
  val isUpdated: Boolean,
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
  var personId: Long? = null,
  val offenderPersonRestrictionId: Long?,
  val restrictionType: String?,
  val effectiveDate: LocalDate?,
  val expiryDate: LocalDate?,
  val authorisedById: Long?,
  val enteredById: Long?,
  val auditModuleName: String,
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
  val visitorRestrictionId: Long?,
  val enteredById: Long?,
  val auditModuleName: String,
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
  val offenderIdSeq: Int?,
  val identifierType: String?,
  val identifierValue: String? = null,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderChargeEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val chargeId: Long?,
  val offenceCodeChange: Boolean? = false,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CourtAppearanceEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val eventId: Long?,
  val caseId: Long?,
  val isBreachHearing: Boolean,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class CourtEventChargeEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val eventId: Long?,
  val chargeId: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

class CourtEventChargeLinkingEvent(
  val combinedCaseId: Long,
  val caseId: Long,
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  auditModuleName: String? = null,
  eventId: Long?,
  chargeId: Long?,
) : CourtEventChargeEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
  auditModuleName = auditModuleName,
  eventId = eventId,
  chargeId = chargeId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
open class CourtCaseEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val caseId: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CourtCaseLinkingEvent(
  val combinedCaseId: Long,
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  auditModuleName: String? = null,
  caseId: Long?,
) : CourtCaseEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
  auditModuleName = auditModuleName,
  caseId = caseId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val orderId: Long?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderSentenceEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val sentenceSeq: Long? = null,
  val caseId: Long? = null,
  val sentenceLevel: String? = null,
  val sentenceCategory: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderSentenceChargeEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val sentenceSeq: Long? = null,
  val chargeId: Long? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderSentenceTermEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long? = null,
  offenderIdDisplay: String? = null,
  val auditModuleName: String? = null,
  val sentenceSeq: Long? = null,
  val termSequence: Long? = null,
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
  var bookingStartDateTime: LocalDateTime? = null,
  var lastAdmissionDate: LocalDate? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
  offenderId = offenderId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderPhoneNumberEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  offenderId: Long? = null,
  val addressId: Long? = null,
  val phoneId: Long?,
  val phoneType: String?,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  offenderId = offenderId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderEmailEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  offenderId: Long? = null,
  val internetAddressId: Long?,
  val internetAddressClass: String?,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  offenderId = offenderId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderContactEvent(
  eventType: String,
  eventDatetime: LocalDateTime,
  offenderIdDisplay: String,
  bookingId: Long,
  var username: String? = null,
  val personId: Long?,
  val contactRootOffenderId: Long?,
  val contactId: Long,
  val approvedVisitor: Boolean,
  val auditModuleName: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = eventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderBookingNumberChangeOrMergeEvent(
  eventType: String,
  nomisEventType: String,
  eventDatetime: LocalDateTime,
  offenderIdDisplay: String? = null,
  var previousOffenderIdDisplay: String? = null,
  bookingId: Long,
  offenderId: Long?,
  val bookingNumber: String? = null,
  var previousBookingNumber: String? = null,
  var type: BookingNumberChangedType = BookingNumberChangedType.BOOK_NUMBER_CHANGE,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
  offenderId = offenderId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CaseIdentifierEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String? = null,
  val caseId: Long,
  val identifierType: String,
  val identifierNo: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonAddressEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
  val addressId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonPhoneEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
  val phoneId: Long,
  val addressId: Long? = null,
  val isAddress: Boolean,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonInternetAddressEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
  val internetAddressId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonEmploymentEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
  val employmentSequence: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonIdentifierEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val personId: Long,
  val identifierSequence: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CorporateEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val corporateId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CorporatePhoneEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val corporateId: Long,
  val phoneId: Long,
  val addressId: Long? = null,
  val isAddress: Boolean,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CorporateAddressEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val corporateId: Long,
  val addressId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CorporateInternetAddressEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val corporateId: Long,
  val internetAddressId: Long,
  val internetAddressClass: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class CorporateTypeEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val auditModuleName: String,
  val corporateId: Long,
  val corporateType: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderFixedTermRecallEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String? = null,
  bookingId: Long,
  val auditModuleName: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderIdentifyingMarksEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  bookingId: Long?,
  val idMarkSeq: Int?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class VisitBalanceAdjustmentEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  bookingId: Long?,
  offenderId: Long?,
  offenderIdDisplay: String?,
  nomisEventType: String?,
  val rootOffenderId: Long?,
  val auditModuleName: String?,
  val visitBalanceAdjustmentId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

enum class BookingNumberChangedType {
  MERGE,
  BOOK_NUMBER_CHANGE,
  BOOK_NUMBER_CHANGE_DUPLICATE,
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class OffenderImageEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String? = null,
  bookingId: Long?,
  val auditModuleName: String,
  val offenderImageId: Long,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class PersonImageEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val personImageId: Long,
  val personId: Long,
  val auditModuleName: String,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class HealthEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  bookingId: Long,
  val offenderHealthProblemId: Long,
  val problemType: String,
  val problemCode: String,
  val startDate: LocalDateTime?,
  val endDate: LocalDateTime?,
  val caseloadType: String,
  val description: String?,
  val problemStatus: String?,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class LanguageEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  bookingId: Long,
  val languageType: String,
  val languageCode: String,
  val readSkill: String?,
  val speakSkill: String?,
  val writeSkill: String?,
  val commentText: String?,
  val numeracySkill: String?,
  val preferedWriteFlag: String,
  val preferedSpeakFlag: String,
  val interpreterRequestedFlag: String,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class GLTransactionEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String?,
  bookingId: Long?,

  val transactionId: Long,
  val entrySequence: Int?,
  val gLEntrySequence: Int?,
  val caseload: String,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class TransactionOffenderEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  offenderIdDisplay: String,
  bookingId: Long?,

  val transactionId: Long,
  val entrySequence: Int,
  val caseload: String,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  offenderIdDisplay = offenderIdDisplay,
  bookingId = bookingId,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class MovementApplicationEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  bookingId: Long?,
  offenderIdDisplay: String?,
  val movementApplicationId: Long,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  nomisEventType = nomisEventType,
  bookingId = bookingId,
  offenderIdDisplay = offenderIdDisplay,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class MovementApplicationMultiEvent(
  eventType: String?,
  eventDatetime: LocalDateTime?,
  nomisEventType: String?,
  val movementApplicationMultiId: Long,
  val movementApplicationId: Long,
  val auditModuleName: String?,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
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

  // offender profile details
  val profileType: String? = null,
) : OffenderEvent(
  eventType = eventType,
  eventDatetime = eventDatetime,
  bookingId = bookingId,
  offenderId = offenderId,
  offenderIdDisplay = offenderIdDisplay,
  nomisEventType = nomisEventType,
)
