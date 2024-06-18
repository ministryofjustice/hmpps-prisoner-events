package uk.gov.justice.digital.hmpps.prisonerevents.service.transformers

import oracle.jakarta.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.model.AgencyInternalLocationUpdatedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AlertOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AssessmentUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPAttendeeOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPFactorOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPInterviewOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPPlanOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPReportOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPReviewOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtAppearanceEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtCaseEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtEventChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.NonAssociationDetailsOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingReassignedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderContactEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEmailEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderIdentifierUpdatedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderPhoneNumberEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceTermEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OrderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PrisonerActivityUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PrisonerAppointmentUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.RestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.VisitorRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.Xtag
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.XtagContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException

@Component
class OffenderEventsTransformer {

  fun offenderEventOf(xtagEvent: AQjmsMapMessage): OffenderEvent? {
    val map = mutableMapOf<String, String>()
    xtagEvent.mapNames.iterator().forEach { name ->
      map[name as String] = xtagEvent.getString(name)
    }

    val seconds = xtagEvent.jmsTimestamp / 1000
    val nanos = (xtagEvent.jmsTimestamp % 1000 * 1000000).toInt()
    return offenderEventOf(
      Xtag(
        eventType = xtagEvent.jmsType,
        nomisTimestamp = xtagFudgedTimestampOf(LocalDateTime.ofEpochSecond(seconds, nanos, bst)),
        content = XtagContent(map),
      ),
    )
  }

  fun offenderEventOf(xtag: Xtag?): OffenderEvent? {
    if (xtag?.eventType == null) {
      log.warn("Bad xtag: {}", xtag)
      return null
    }
    log.debug("Processing Xtag {}...", xtag)
    return try {
      when (xtag.eventType) {
        "P8_RESULT" -> riskScoreEventOf(xtag)
        "A3_RESULT" -> offenderSanctionEventOf(xtag)
        "P1_RESULT", "BOOK_UPD_OASYS" -> bookingNumberEventOf(xtag)
        "OFF_HEALTH_PROB_INS" -> maternityStatusInsertedEventOf(xtag)
        "OFF_HEALTH_PROB_UPD" -> maternityStatusUpdatedEventOf(xtag)
        "SCHEDULE_INT_APP-CHANGED" -> offenderIndividualScheduleChanged(xtag)
        "OFF_RECEP_OASYS" -> offenderMovementReceptionEventOf(xtag)
        "OFF_DISCH_OASYS" -> offenderMovementDischargeEventOf(xtag)
        "M1_RESULT", "M1_UPD_RESULT" -> externalMovementRecordEventOf(xtag, externalMovementEventOf(xtag))
        "OFF_UPD_OASYS" -> if (!xtag.content.p_offender_book_id.isNullOrEmpty()) {
          offenderBookingChangedEventOf(xtag)
        } else {
          offenderDetailsChangedEventOf(xtag)
        }

        "ADDR_USG_INS" -> addressUsageInsertedEventOf(xtag)
        "ADDR_USG_UPD" -> if (xtag.content.p_address_deleted == "Y") {
          addressUsageDeletedEventOf(xtag)
        } else {
          addressUsageUpdatedEventOf(xtag)
        }

        "P4_RESULT" -> offenderAliasChangedEventOf(xtag)
        "P2_RESULT" -> offenderUpdatedEventOf(xtag)
        "OFF_BKB_INS" -> offenderBookingInsertedEventOf(xtag)
        "OFF_BKB_UPD" -> offenderBookingReassignedEventOf(xtag)
        "OFF_CONT_PER_INS" -> contactPersonInsertedEventOf(xtag)
        "OFF_CONT_PER_UPD" -> if (xtag.content.p_address_deleted == "Y") {
          contactPersonDeletedEventOf(xtag)
        } else {
          contactPersonUpdatedEventOf(xtag)
        }

        "OFF_EDUCATION_INS" -> educationLevelInsertedEventOf(xtag)
        "OFF_EDUCATION_UPD" -> educationLevelUpdatedEventOf(xtag)
        "OFF_EDUCATION_DEL" -> educationLevelDeletedEventOf(xtag)
        "P3_RESULT" -> if (xtag.content.p_identifier_type == "NOMISP3") {
          offenderBookingInsertedEventOf(xtag)
        } else if (!xtag.content.p_identifier_value.isNullOrEmpty()) {
          offenderIdentifierInsertedEventOf(xtag)
        } else {
          offenderIdentifierDeletedEventOf(xtag)
        }

        "OFFENDER_IDENTIFIERS-UPDATED" -> offenderIdentifierUpdatedEventOf(xtag)

        "S1_RESULT" -> if (!xtag.content.p_imprison_status_seq.isNullOrEmpty()) {
          imprisonmentStatusChangedEventOf(xtag)
        } else if (!xtag.content.p_assessment_seq.isNullOrEmpty()) {
          assessmentChangedEventOf(xtag)
        } else {
          null
        }

        "OFFENDER_ASSESSMENTS-UPDATED" -> assessmentUpdatedEventOf(xtag)

        "OFF_ALERT_INSERT" -> alertInsertedEventOf(xtag)
        "OFF_ALERT_UPDATE" -> alertUpdatedEventOf(xtag)
        "OFF_ALERT_DELETE" -> alertDeletedEventOf(xtag)
        "INCIDENT-INSERTED" -> incidentInsertedEventOf(xtag)
        "INCIDENT-UPDATED" -> incidentUpdatedEventOf(xtag)
        "OFF_IMP_STAT_OASYS" -> imprisonmentStatusChangedEventOf(xtag)
        "OFF_PROF_DETAIL_INS" -> offenderProfileDetailInsertedEventOf(xtag)
        "OFF_PROF_DETAIL_UPD" -> offenderProfileUpdatedEventOf(xtag)
        "S2_RESULT" -> sentenceDatesChangedEventOf(xtag)
        "SENTENCING-CHANGED" -> sentencingChangedEventOf(xtag)
        "A2_CALLBACK" -> hearingDateChangedEventOf(xtag)
        "A2_RESULT" -> if ("Y" == xtag.content.p_delete_flag) {
          hearingResultDeletedEventOf(xtag)
        } else {
          hearingResultChangedEventOf(xtag)
        }

        "PHONES_INS" -> phoneInsertedEventOf(xtag)
        "PHONES_UPD" -> phoneUpdatedEventOf(xtag)
        "PHONES_DEL" -> phoneDeletedEventOf(xtag)
        "OFF_EMPLOYMENTS_INS" -> offenderEmploymentInsertedEventOf(xtag)
        "OFF_EMPLOYMENTS_UPD" -> offenderEmploymentUpdatedEventOf(xtag)
        "OFF_EMPLOYMENTS_DEL" -> offenderEmploymentDeletedEventOf(xtag)
        "D5_RESULT" -> hdcConditionChanged(xtag)
        "D4_RESULT" -> hdcFineInserted(xtag)
        "ADDR_INS" -> addressInserted(xtag)
        "ADDR_UPD" -> addressUpdatedOrDeleted(xtag)

        "OFF_SENT_OASYS" -> sentenceCalculationDateChangedEventOf(xtag)
        "C_NOTIFICATION" -> courtSentenceChangedEventOf(xtag)
        "IEDT_OUT" -> offenderTransferOutOfLidsEventOf(xtag)
        "BED_ASSIGNMENT_HISTORY-INSERTED" -> offenderBedAssignmentEventOf(xtag)
        "CONFIRMED_RELEASE_DATE-CHANGED" -> confirmedReleaseDateOf(xtag)
        "OFFENDER-INSERTED", "OFFENDER-UPDATED", "OFFENDER-DELETED" -> offenderUpdatedOf(xtag)
        "OFF_IDENT_MARKS-CHANGED" -> offenderIdentifyingMarksUpdatedOf(xtag)
        "OFF_PHYS_ATTR-CHANGED" -> offenderPhysicalAttributesUpdatedOf(xtag)
        "OFF_PROFILE_DETS-CHANGED" -> offenderPhysicalDetailsUpdatedOf(xtag)

        "EXTERNAL_MOVEMENT-CHANGED" -> externalMovementRecordEventOf(xtag, null)

        "OFFENDER_IEP_LEVEL-UPDATED" -> iepUpdatedEventOf(xtag)
        "OFF_KEY_DATES_ADJ-UPDATED" -> keyDateAdjustmentUpdatedEventOf(xtag)
        "OFF_SENT_ADJ-UPDATED" -> sentenceAdjustmentUpdatedEventOf(xtag)
        "OFFENDER_VISIT-UPDATED" -> visitCancelledEventOf(xtag)

        "OFFENDER_CASE_NOTES-INSERTED",
        "OFFENDER_CASE_NOTES-UPDATED",
        "OFFENDER_CASE_NOTES-DELETED",
        -> caseNotesEventOf(xtag)

        "OFF_NON_ASSOC-UPDATED" -> null // Redundant as only happens with a corresponding OFF_NA_DETAILS_ASSOC-UPDATED event
        "OFF_NA_DETAILS_ASSOC-UPDATED" -> nonAssociationDetailsEventOf(xtag)

        "OFF_RESTRICTS-UPDATED" -> restrictionEventOf(xtag)
        "OFF_PERS_RESTRICTS-UPDATED" -> restrictionPersonEventOf(xtag)
        "VISITOR_RESTRICTS-UPDATED" -> visitorRestrictionEventOf(xtag)
        "PRISONER_ACTIVITY-UPDATE" -> prisonerActivityUpdateEventOf(xtag)
        "PRISONER_APPOINTMENT-UPDATE" -> prisonerAppointmentUpdateEventOf(xtag)
        "OFFENDER_CHARGES-UPDATED", "OFFENDER_CHARGES-INSERTED", "OFFENDER_CHARGES-DELETED" -> offenderChargeEventOf(
          xtag,
        )

        "COURT_EVENT-UPDATED", "COURT_EVENT-INSERTED", "COURT_EVENT-DELETED" -> courtAppearanceEventOf(
          xtag,
        )

        "COURT_EVENT_CHARGES-UPDATED", "COURT_EVENT_CHARGES-INSERTED", "COURT_EVENT_CHARGES-DELETED" -> courtEventChargeEventOf(
          xtag,
        )

        "OFFENDER_CASES-UPDATED", "OFFENDER_CASES-INSERTED", "OFFENDER_CASES-DELETED" -> courtCaseEventOf(
          xtag,
        )

        "ORDERS-UPDATED", "ORDERS-INSERTED", "ORDERS-DELETED" -> orderEventOf(
          xtag,
        )

        "AGENCY_INTERNAL_LOCATIONS-UPDATED",
        "AGY_INT_LOC_PROFILES-UPDATED",
        "INT_LOC_USAGE_LOCATIONS-UPDATED",
        -> agencyInternalLocationUpdatedEventOf(xtag)

        "PHONES-INSERTED", "PHONES-UPDATED", "PHONES-DELETED" -> offenderPhoneNoEventOf(xtag)

        "INTERNET_ADDRESSES-INSERTED", "INTERNET_ADDRESSES-UPDATED", "INTERNET_ADDRESSES-DELETED" -> offenderEmailEventOf(xtag)
        "OFFENDER_CONTACT-INSERTED", "OFFENDER_CONTACT-UPDATED", "OFFENDER_CONTACT-DELETED" -> offenderContactEventOf(xtag)

        "CSIP_REPORTS-INSERTED", "CSIP_REPORTS-UPDATED", "CSIP_REPORTS-DELETED" -> csipReportEventOf(xtag)
        "CSIP_PLANS-INSERTED", "CSIP_PLANS-UPDATED", "CSIP_PLANS-DELETED" -> csipPlanEventOf(xtag)
        "CSIP_REVIEWS-INSERTED", "CSIP_REVIEWS-UPDATED", "CSIP_REVIEWS-DELETED" -> csipReviewEventOf(xtag)
        "CSIP_ATTENDEES-INSERTED", "CSIP_ATTENDEES-UPDATED", "CSIP_ATTENDEES-DELETED" -> csipAttendeeEventOf(xtag)
        "CSIP_FACTORS-INSERTED", "CSIP_FACTORS-UPDATED", "CSIP_FACTORS-DELETED" -> csipFactorEventOf(xtag)
        "CSIP_INTVW-INSERTED", "CSIP_INTVW-UPDATED", "CSIP_INTVW-DELETED" -> csipInterviewEventOf(xtag)

        "OFFENDER_SENTENCES-UPDATED", "OFFENDER_SENTENCES-INSERTED", "OFFENDER_SENTENCES-DELETED" -> offenderSentenceEventOf(xtag)
        "OFFENDER_SENTENCE_CHARGES-UPDATED", "OFFENDER_SENTENCE_CHARGES-INSERTED", "OFFENDER_SENTENCE_CHARGES-DELETED" -> offenderSentenceChargeEventOf(xtag)
        "OFFENDER_SENTENCE_TERMS-UPDATED", "OFFENDER_SENTENCE_TERMS-INSERTED", "OFFENDER_SENTENCE_TERMS-DELETED" -> offenderSentenceTermEventOf(xtag)

        else -> OffenderEvent(
          eventType = xtag.eventType,
          eventDatetime = xtag.nomisTimestamp,
        )
      }
    } catch (t: Throwable) {
      log.error("Caught throwable", t)
      throw t
    }
  }

  private fun offenderTransferOutOfLidsEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_TRANSFER-OUT_OF_LIDS",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun offenderBedAssignmentEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "BED_ASSIGNMENT_HISTORY-INSERTED",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    bedAssignmentSeq = xtag.content.p_bed_assign_seq?.toInt(),
    livingUnitId = xtag.content.p_living_unit_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun courtSentenceChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "COURT_SENTENCE-CHANGED",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun alertDeletedEventOf(xtag: Xtag) = AlertOffenderEvent(
    eventType = "ALERT-DELETED",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    alertSeq = xtag.content.p_alert_seq?.toLong(),
    alertDateTime = localDateTimeOf(xtag.content.p_alert_date, xtag.content.p_alert_time),
    alertType = xtag.content.p_alert_type,
    alertCode = xtag.content.p_alert_code,
    expiryDateTime = localDateTimeOf(xtag.content.p_expiry_date, xtag.content.p_expiry_time),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun personAddressUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "PERSON_ADDRESS-UPDATED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun offenderAddressUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_ADDRESS-UPDATED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun addressUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "ADDRESS-UPDATED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun personAddressDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "PERSON_ADDRESS-DELETED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun offenderAddressDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_ADDRESS-DELETED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun addressDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "ADDRESS-DELETED",
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun addressInserted(xtag: Xtag) = GenericOffenderEvent(
    eventType = when (xtag.content.p_owner_class) {
      "PER" -> "PERSON_ADDRESS-INSERTED"
      "OFF" -> "OFFENDER_ADDRESS-INSERTED"
      else -> "ADDRESS-INSERTED"
    },
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    addressId = xtag.content.p_address_id?.toLong(),
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    addressEndDate = localDateOf(xtag.content.p_address_end_date),
    primaryAddressFlag = xtag.content.p_primary_addr_flag,
    mailAddressFlag = xtag.content.p_mail_addr_flag,
    personId = xtag.content.p_person_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun addressUpdatedOrDeleted(xtag: Xtag) =
    if (xtag.content.p_owner_class == "PER") {
      if (xtag.content.p_address_deleted == "N") {
        personAddressUpdatedEventOf(xtag)
      } else {
        personAddressDeletedEventOf(
          xtag,
        )
      }
    } else if (xtag.content.p_owner_class == "OFF") {
      if (xtag.content.p_address_deleted == "N") {
        offenderAddressUpdatedEventOf(xtag)
      } else {
        offenderAddressDeletedEventOf(
          xtag,
        )
      }
    } else {
      if (xtag.content.p_address_deleted == "N") addressUpdatedEventOf(xtag) else addressDeletedEventOf(xtag)
    }

  private fun hdcFineInserted(xtag: Xtag) = GenericOffenderEvent(
    eventType = "HDC_FINE-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun hdcConditionChanged(xtag: Xtag) = GenericOffenderEvent(
    eventType = "HDC_CONDITION-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    conditionCode = xtag.content.p_condition_code,
    offenderSentenceConditionId = xtag.content.p_offender_sent_calculation_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderIndividualScheduleChanged(xtag: Xtag) = GenericOffenderEvent(
    eventType = "APPOINTMENT_CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    scheduleEventId = xtag.content.p_event_id?.toLong(),
    scheduledStartTime = localDateTimeOf(xtag.content.p_event_date, xtag.content.p_start_time),
    scheduledEndTime =
    if (xtag.content.p_end_time == null) {
      null
    } else {
      localDateTimeOf(
        xtag.content.p_event_date,
        xtag.content.p_end_time,
      )
    },
    scheduleEventClass = xtag.content.p_event_class,
    scheduleEventType = xtag.content.p_event_type,
    scheduleEventSubType = xtag.content.p_event_sub_type,
    scheduleEventStatus = xtag.content.p_event_status,
    recordDeleted = "Y".equals(xtag.content.p_delete_flag),
    agencyLocationId = xtag.content.p_agy_loc_id,
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun phoneInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "PHONE-INSERTED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun phoneUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "PHONE-UPDATED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun phoneDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "PHONE-DELETED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun hearingResultChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "HEARING_RESULT-CHANGED",
    oicHearingId = xtag.content.p_oic_hearing_id?.toLong(),
    resultSeq = xtag.content.p_result_seq?.toLong(),
    agencyIncidentId = xtag.content.p_agency_incident_id?.toLong(),
    chargeSeq = xtag.content.p_charge_seq?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun hearingResultDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "HEARING_RESULT-DELETED",
    oicHearingId = xtag.content.p_oic_hearing_id?.toLong(),
    resultSeq = xtag.content.p_result_seq?.toLong(),
    agencyIncidentId = xtag.content.p_agency_incident_id?.toLong(),
    chargeSeq = xtag.content.p_charge_seq?.toLong(),
    oicOffenceId = xtag.content.p_oic_offence_id?.toLong(),
    pleaFindingCode = xtag.content.p_plea_finding_code,
    findingCode = xtag.content.p_finding_code,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun hearingDateChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "HEARING_DATE-CHANGED",
    oicHearingId = xtag.content.p_oic_hearing_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun sentenceCalculationDateChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "SENTENCE_CALCULATION_DATES-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun sentenceDatesChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "SENTENCE_DATES-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceCalculationId = xtag.content.p_offender_sent_calculation_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun sentencingChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    nomisEventType = xtag.eventType,
  )

  private fun offenderProfileUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_PROFILE_DETAILS-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderProfileDetailInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_PROFILE_DETAILS-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun alertInsertedEventOf(xtag: Xtag) = AlertOffenderEvent(
    eventType = "ALERT-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    alertSeq = xtag.content.p_alert_seq?.toLong(),
    alertDateTime = localDateTimeOf(xtag.content.p_alert_date, xtag.content.p_alert_time),
    alertType = xtag.content.p_alert_type,
    alertCode = xtag.content.p_alert_code,
    nomisEventType = xtag.eventType,
  )

  private fun alertUpdatedEventOf(xtag: Xtag) = AlertOffenderEvent(
    eventType = "ALERT-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    alertSeq = xtag.content.p_alert_seq?.toLong(),
    alertDateTime = localDateTimeOf(xtag.content.p_alert_date, xtag.content.p_alert_time),
    alertType = xtag.content.p_alert_type,
    alertCode = xtag.content.p_alert_code,
    nomisEventType = xtag.eventType,
  )

  private fun assessmentChangedEventOf(xtag: Xtag) = AssessmentUpdateEvent(
    eventType = "ASSESSMENT-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    assessmentSeq = xtag.content.p_assessment_seq?.toLong(),
    assessmentType = null,
    evaluationResultCode = null,
    reviewLevelSupType = null,
    offenderIdDisplay = null,
    nomisEventType = xtag.eventType,
  )

  private fun assessmentUpdatedEventOf(xtag: Xtag) = AssessmentUpdateEvent(
    eventType = "ASSESSMENT-UPDATED",
    eventDatetime = localDateTimeOf(xtag.content.p_nomis_timestamp),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    assessmentSeq = xtag.content.p_assessment_seq?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    reviewLevelSupType = xtag.content.p_review_level_sup_type,
    evaluationResultCode = xtag.content.p_evaluation_result_code,
    assessmentType = xtag.content.p_assessment_type,
    nomisEventType = xtag.eventType,
  )

  private fun imprisonmentStatusChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "IMPRISONMENT_STATUS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    imprisonmentStatusSeq = xtag.content.p_imprison_status_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun incidentInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "INCIDENT-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    incidentCaseId = xtag.content.p_incident_case_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun incidentUpdatedEventOf(xtag: Xtag): OffenderEvent {
    val v =
      (if (xtag.content.p_delete_flag == "N") "CHANGED-" else "DELETED-") + INCIDENT_TABLE_MAP[xtag.content.p_table_name]
    return GenericOffenderEvent(
      eventType = "INCIDENT-$v",
      eventDatetime = xtag.nomisTimestamp,
      incidentCaseId = xtag.content.p_incident_case_id?.toLong(),
      incidentPartySeq = xtag.content.p_party_seq?.toLong(),
      incidentRequirementSeq = xtag.content.p_requirement_seq?.toLong(),
      incidentQuestionSeq = xtag.content.p_question_seq?.toLong(),
      incidentResponseSeq = xtag.content.p_response_seq?.toLong(),
      nomisEventType = xtag.eventType,
    )
  }

  private fun csipReportEventOf(xtag: Xtag) = CSIPReportOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun csipPlanEventOf(xtag: Xtag) = CSIPPlanOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    csipPlanId = xtag.content.p_plan_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun csipReviewEventOf(xtag: Xtag) = CSIPReviewOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    csipReviewId = xtag.content.p_review_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun csipAttendeeEventOf(xtag: Xtag) = CSIPAttendeeOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    csipReviewId = xtag.content.p_review_id?.toLong(),
    csipAttendeeId = xtag.content.p_attendee_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun csipFactorEventOf(xtag: Xtag) = CSIPFactorOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    csipFactorId = xtag.content.p_csip_factor_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun csipInterviewEventOf(xtag: Xtag) = CSIPInterviewOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    csipReportId = xtag.content.p_csip_id?.toLong(),
    csipInterviewId = xtag.content.p_csip_intvw_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    auditModuleName = xtag.content.p_audit_module_name,
    nomisEventType = xtag.eventType,
  )

  private fun offenderIdentifierInsertedEventOf(xtag: Xtag) = OffenderIdentifierUpdatedEvent(
    eventType = "OFFENDER_IDENTIFIER-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    identifierValue = xtag.content.p_identifier_value,
    nomisEventType = xtag.eventType,
  )

  private fun offenderIdentifierDeletedEventOf(xtag: Xtag) = OffenderIdentifierUpdatedEvent(
    eventType = "OFFENDER_IDENTIFIER-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    nomisEventType = xtag.eventType,
  )

  private fun offenderIdentifierUpdatedEventOf(xtag: Xtag) = OffenderIdentifierUpdatedEvent(
    eventType = "OFFENDER_IDENTIFIER-UPDATED",
    eventDatetime = localDateTimeOf(xtag.content.p_nomis_timestamp),
    nomisEventType = xtag.eventType,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    offenderId = xtag.content.p_offender_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    identifierType = xtag.content.p_identifier_type,
    identifierValue = xtag.content.p_identifier_value,
  )

  private fun educationLevelInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "EDUCATION_LEVEL-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun educationLevelUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "EDUCATION_LEVEL-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun educationLevelDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "EDUCATION_LEVEL-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "CONTACT_PERSON-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "CONTACT_PERSON-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "CONTACT_PERSON-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderAliasChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_ALIAS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    aliasOffenderId = xtag.content.p_alias_offender_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "ADDRESS_USAGE-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "ADDRESS_USAGE-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageDeletedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "ADDRESS_USAGE-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun offenderDetailsChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_DETAILS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_BOOKING-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingChangedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_BOOKING-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingReassignedEventOf(xtag: Xtag) = OffenderBookingReassignedEvent(
    eventType = "OFFENDER_BOOKING-REASSIGNED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id!!.toLong(),
    previousOffenderId = xtag.content.p_old_offender_id!!.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  fun externalMovementRecordEventOf(xtag: Xtag, overrideEventType: String?) = ExternalMovementOffenderEvent(
    eventType = overrideEventType ?: xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    movementSeq = xtag.content.p_movement_seq?.toLong(),
    movementDateTime = localDateTimeOf(xtag.content.p_movement_date, xtag.content.p_movement_time),
    movementType = xtag.content.p_movement_type,
    movementReasonCode = xtag.content.p_movement_reason_code,
    directionCode = xtag.content.p_direction_code,
    escortCode = xtag.content.p_escort_code,
    fromAgencyLocationId = xtag.content.p_from_agy_loc_id,
    toAgencyLocationId = xtag.content.p_to_agy_loc_id,
    nomisEventType = xtag.eventType,
  )

  private fun offenderMovementDischargeEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_MOVEMENT-DISCHARGE",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    movementSeq = xtag.content.p_movement_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderMovementReceptionEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_MOVEMENT-RECEPTION",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    movementSeq = xtag.content.p_movement_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun maternityStatusInsertedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "MATERNITY_STATUS-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun maternityStatusUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "MATERNITY_STATUS-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun riskScoreEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "RISK_SCORE-" + if (xtag.content.p_delete_flag == "N") "CHANGED" else "DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    riskPredictorId = xtag.content.p_offender_risk_predictor_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderSanctionEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_SANCTION-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sanctionSeq = xtag.content.p_sanction_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun bookingNumberEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "BOOKING_NUMBER-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    bookingNumber = xtag.content.p_new_prison_num,
    previousBookingNumber =
    xtag.content.p_old_prison_num ?: xtag.content.p_old_prision_num ?: xtag.content.p_old_prison_number,
    nomisEventType = xtag.eventType,
  )

  private fun confirmedReleaseDateOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
  )

  private fun offenderUpdatedOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
  )

  private fun offenderIdentifyingMarksUpdatedOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_IDENTIFYING_MARKS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
  )

  private fun offenderPhysicalAttributesUpdatedOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_PHYSICAL_ATTRIBUTES-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
  )

  private fun offenderPhysicalDetailsUpdatedOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "OFFENDER_PHYSICAL_DETAILS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
  )

  private fun iepUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "IEP_" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    agencyLocationId = xtag.content.p_agy_loc_id,
    iepSeq = xtag.content.p_iep_level_seq?.toLong(),
    iepLevel = xtag.content.p_iep_level,
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun keyDateAdjustmentUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "KEY_DATE_ADJUSTMENT_" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    adjustmentId = xtag.content.p_offender_key_date_adjust_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun sentenceAdjustmentUpdatedEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "SENTENCE_ADJUSTMENT_" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    adjustmentId = xtag.content.p_offender_sentence_adjust_id?.toLong(),
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun visitCancelledEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = "VISIT_CANCELLED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    agencyLocationId = xtag.content.p_agy_loc_id,
    nomisEventType = xtag.eventType,
    visitId = xtag.content.p_offender_visit_id?.toLong(),
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun caseNotesEventOf(xtag: Xtag) = GenericOffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    caseNoteId = xtag.content.p_case_note_id?.toLong(),
    caseNoteType = xtag.content.p_case_note_type,
    caseNoteSubType = xtag.content.p_case_note_sub_type,
    recordDeleted = "Y".equals(xtag.content.p_delete_flag),
  )

  private fun nonAssociationDetailsEventOf(xtag: Xtag) = NonAssociationDetailsOffenderEvent(
    eventType = "NON_ASSOCIATION_DETAIL-" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    nsOffenderIdDisplay = xtag.content.p_ns_offender_id_display,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nsBookingId = xtag.content.p_ns_offender_book_id?.toLong(),
    typeSeq = xtag.content.p_type_seq?.toInt(),
    reasonCode = xtag.content.p_ns_reason_code,
    levelCode = xtag.content.p_ns_level_code,
    nsType = xtag.content.p_ns_type,
    effectiveDate = localDateOf(xtag.content.p_ns_effective_date),
    expiryDate = localDateOf(xtag.content.p_ns_expiry_date),
    authorisedBy = xtag.content.p_authorized_staff,
    comment = xtag.content.p_comment_text,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun restrictionEventOf(xtag: Xtag) = RestrictionOffenderEvent(
    eventType = "RESTRICTION-" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderRestrictionId = xtag.content.p_offender_restriction_id?.toLong(),
    restrictionType = xtag.content.p_restriction_type,
    effectiveDate = localDateOf(xtag.content.p_effective_date),
    expiryDate = localDateOf(xtag.content.p_expiry_date),
    comment = xtag.content.p_comment_text,
    authorisedById = xtag.content.p_authorised_staff_id?.toLong(),
    enteredById = xtag.content.p_entered_staff_id?.toLong(),
  )

  private fun restrictionPersonEventOf(xtag: Xtag) = PersonRestrictionOffenderEvent(
    eventType = "PERSON_RESTRICTION-" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    contactPersonId = xtag.content.p_offender_contact_person_id?.toLong(),
    offenderPersonRestrictionId = xtag.content.p_offender_person_restrict_id?.toLong(),
    restrictionType = xtag.content.p_restriction_type,
    effectiveDate = localDateOf(xtag.content.p_restriction_effective_date),
    expiryDate = localDateOf(xtag.content.p_restriction_expiry_date),
    authorisedById = xtag.content.p_authorized_staff_id?.toLong(),
    comment = xtag.content.p_comment_text,
    enteredById = xtag.content.p_entered_staff_id?.toLong(),
  )

  private fun visitorRestrictionEventOf(xtag: Xtag) = VisitorRestrictionOffenderEvent(
    eventType = "VISITOR_RESTRICTION-" + if (xtag.content.p_delete_flag == "Y") "DELETED" else "UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    personId = xtag.content.p_person_id?.toLong(),
    restrictionType = xtag.content.p_visit_restriction_type,
    effectiveDate = localDateOf(xtag.content.p_effective_date),
    expiryDate = localDateOf(xtag.content.p_expiry_date),
    comment = xtag.content.p_comment_txt,
    visitorRestrictionId = xtag.content.p_visitor_restriction_id?.toLong(),
    enteredById = xtag.content.p_entered_staff_id?.toLong(),
  )

  private fun prisonerActivityUpdateEventOf(xtag: Xtag) = PrisonerActivityUpdateEvent(
    eventType = "PRISONER_ACTIVITY-UPDATE",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    prisonId = xtag.content.p_agy_loc_id,
    action = xtag.content.p_action,
    user = xtag.content.p_user,
  )

  private fun prisonerAppointmentUpdateEventOf(xtag: Xtag) = PrisonerAppointmentUpdateEvent(
    eventType = "PRISONER_APPOINTMENT-UPDATE",
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    prisonId = xtag.content.p_agy_loc_id,
    action = xtag.content.p_action,
    user = xtag.content.p_user,
  )

  private fun offenderChargeEventOf(xtag: Xtag) = OffenderChargeEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    chargeId = xtag.content.p_offender_charge_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun courtAppearanceEventOf(xtag: Xtag) = CourtAppearanceEvent(
    eventType = xtag.eventType!!.replace(oldValue = "COURT_EVENT", newValue = "COURT_EVENTS"),
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    eventId = xtag.content.p_event_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun courtEventChargeEventOf(xtag: Xtag) = CourtEventChargeEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    eventId = xtag.content.p_event_id?.toLong(),
    chargeId = xtag.content.p_offender_charge_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun courtCaseEventOf(xtag: Xtag) = CourtCaseEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    caseId = xtag.content.p_case_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun orderEventOf(xtag: Xtag) = OrderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    orderId = xtag.content.p_order_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun offenderSentenceEventOf(xtag: Xtag) = OffenderSentenceEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun offenderSentenceChargeEventOf(xtag: Xtag) = OffenderSentenceChargeEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    chargeId = xtag.content.p_offender_charge_id?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun offenderSentenceTermEventOf(xtag: Xtag) = OffenderSentenceTermEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    termSequence = xtag.content.p_term_seq?.toLong(),
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  private fun agencyInternalLocationUpdatedEventOf(xtag: Xtag) = AgencyInternalLocationUpdatedEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    internalLocationId = xtag.content.p_internal_location_id?.toLong(),
    prisonId = xtag.content.p_agy_loc_id,
    description = xtag.content.p_description,
    oldDescription = xtag.content.p_old_description,
    auditModuleName = xtag.content.p_audit_module_name,
    recordDeleted = xtag.content.p_delete_flag == "Y",
    usageLocationId = xtag.content.p_usage_location_id?.toLong(),
  )

  private fun offenderPhoneNoEventOf(xtag: Xtag) = OffenderPhoneNumberEvent(
    eventType = if (xtag.content.p_owner_class == "OFF") {
      xtag.eventType?.replace("PHONES-", "OFFENDER_PHONE-")
    } else if (xtag.content.p_owner_class == "ADDR") {
      xtag.eventType?.replace("PHONES-", "OFFENDER_ADDRESS_PHONE-")
    } else {
      xtag.eventType
    },
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    phoneId = xtag.content.p_phone_id?.toLong(),
    phoneType = xtag.content.p_phone_type,
    auditModuleName = xtag.content.p_audit_module_name,
    offenderId = if (xtag.content.p_owner_class == "OFF") {
      xtag.content.p_owner_id?.toLong()
    } else {
      null
    },
    addressId = if (xtag.content.p_owner_class == "ADDR") {
      xtag.content.p_owner_id?.toLong()
    } else {
      null
    },
  )

  private fun offenderEmailEventOf(xtag: Xtag) = OffenderEmailEvent(
    eventType = if (xtag.content.p_internet_address_class == "EMAIL" && xtag.content.p_owner_class == "OFF") {
      xtag.eventType?.replace("INTERNET_ADDRESSES-", "OFFENDER_EMAIL-")
    } else {
      xtag.eventType
    },
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
    offenderIdDisplay = xtag.content.p_offender_id_display,
    internetAddressId = xtag.content.p_internet_address_id?.toLong(),
    internetAddressClass = xtag.content.p_internet_address_class,
    auditModuleName = xtag.content.p_audit_module_name,
    offenderId = if (xtag.content.p_owner_class == "OFF") {
      xtag.content.p_owner_id?.toLong()
    } else {
      null
    },
  )

  private fun offenderContactEventOf(xtag: Xtag) = OffenderContactEvent(
    eventType = xtag.eventType!!,
    eventDatetime = xtag.nomisTimestamp!!,
    bookingId = xtag.content.p_offender_book_id!!.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display!!,
    personId = xtag.content.p_person_id!!.toLong(),
    approvedVisitor = xtag.content.p_approved_visitor_flag == "Y",
    auditModuleName = xtag.content.p_audit_module_name!!,
    contactId = xtag.content.p_offender_contact_person_id!!.toLong(),
  )

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    // Xtag events are in British Summer Time all year round at rest in Oracle.
    private val bst = ZoneOffset.ofHours(1)

    private val INCIDENT_TABLE_MAP = mapOf(
      "incident_cases" to "CASES",
      "incident_case_parties" to "PARTIES",
      "incident_case_responses" to "RESPONSES",
      "incident_case_requirements" to "REQUIREMENTS",
    )

    fun xtagFudgedTimestampOf(xtagEnqueueTime: LocalDateTime): LocalDateTime {
      val london: ZoneId = ZoneId.of("Europe/London")
      return if (london.rules.isDaylightSavings(xtagEnqueueTime.atZone(london).toInstant())) {
        xtagEnqueueTime
      } else {
        xtagEnqueueTime.minusHours(1L)
      }
    }

    fun externalMovementEventOf(xtag: Xtag): String {
      return when (xtag.content.p_record_deleted) {
        "N" -> "EXTERNAL_MOVEMENT_RECORD-INSERTED"
        "Y" -> "EXTERNAL_MOVEMENT_RECORD-DELETED"
        else -> "EXTERNAL_MOVEMENT_RECORD-UPDATED"
      }
    }

    private const val DATE_PATTERN = "[yyyy-MM-dd HH:mm:ss][yyyy-MM-dd][dd-MMM-yyyy][dd-MMM-yy]"
    private val caseInsensitiveFormatter =
      DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(DATE_PATTERN).toFormatter()

    fun localDateOf(date: String?): LocalDate? =
      try {
        date?.let {
          LocalDate.parse(it, caseInsensitiveFormatter)
        }
      } catch (dtpe: DateTimeParseException) {
        log.error("Unable to parse $date into a LocalDateTime using pattern $DATE_PATTERN", dtpe)
        null
      }

    private const val TIME_PATTERN = "[yyyy-MM-dd ]HH:mm:ss"

    fun localTimeOf(dateTime: String?): LocalTime? =
      try {
        dateTime?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern(TIME_PATTERN)) }
      } catch (dtpe: DateTimeParseException) {
        log.error("Unable to parse $dateTime into a LocalTime using pattern $TIME_PATTERN", dtpe)
        null
      }

    fun localDateTimeOf(date: String?, time: String?): LocalDateTime? =
      localDateOf(date)?.let {
        val t = localTimeOf(time)
        if (t == null) {
          it.atStartOfDay()
        } else {
          t.atDate(it)
        }
      }

    private const val TIMESTAMP_PATTERN = "yyyyMMddHHmmss.SSSSSSSSS"

    fun localDateTimeOf(dateTime: String?): LocalDateTime? =
      try {
        dateTime?.let {
          LocalDateTime.parse(it, DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN))
        }
      } catch (dtpe: DateTimeParseException) {
        log.error("Unable to parse $dateTime into a LocalDateTime using pattern $TIMESTAMP_PATTERN", dtpe)
        null
      }
  }
}
