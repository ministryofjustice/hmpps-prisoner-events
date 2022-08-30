package uk.gov.justice.digital.hmpps.prisonerevents.service.transformers

import com.google.common.base.Strings
import oracle.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
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
class OffenderEventsTransformer @Autowired constructor() {
  fun offenderEventOf(xtagEvent: AQjmsMapMessage): OffenderEvent? {

    val map = mutableMapOf<String, String>()
    xtagEvent.mapNames.iterator().forEach { name ->
      map[name as String] = xtagEvent.getString(name)
    }

    return offenderEventOf(
      Xtag(
        eventType = xtagEvent.jmsType,
        nomisTimestamp = xtagFudgedTimestampOf(
          LocalDateTime.ofEpochSecond(
            xtagEvent.jmsTimestamp / 1000,
            0,
            ZoneOffset.UTC
          )
        ),
        content = XtagContent(map)
      )
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
        "OFF_UPD_OASYS" -> if (!Strings.isNullOrEmpty(xtag.content.p_offender_book_id)) offenderBookingChangedEventOf(
          xtag
        ) else offenderDetailsChangedEventOf(xtag)

        "ADDR_USG_INS" -> addressUsageInsertedEventOf(xtag)
        "ADDR_USG_UPD" -> if (xtag.content.p_address_deleted == "Y") addressUsageDeletedEventOf(xtag) else addressUsageUpdatedEventOf(
          xtag
        )

        "P4_RESULT" -> offenderAliasChangedEventOf(xtag)
        "P2_RESULT" -> offenderUpdatedEventOf(xtag)
        "OFF_BKB_INS" -> offenderBookingInsertedEventOf(xtag)
        "OFF_BKB_UPD" -> offenderBookingReassignedEventOf(xtag)
        "OFF_CONT_PER_INS" -> contactPersonInsertedEventOf(xtag)
        "OFF_CONT_PER_UPD" -> if (xtag.content.p_address_deleted == "Y") contactPersonDeletedEventOf(xtag) else contactPersonUpdatedEventOf(
          xtag
        )

        "OFF_EDUCATION_INS" -> educationLevelInsertedEventOf(xtag)
        "OFF_EDUCATION_UPD" -> educationLevelUpdatedEventOf(xtag)
        "OFF_EDUCATION_DEL" -> educationLevelDeletedEventOf(xtag)
        "P3_RESULT" -> if (xtag.content.p_identifier_type == "NOMISP3") offenderBookingInsertedEventOf(xtag) else if (!Strings.isNullOrEmpty(
            xtag.content.p_identifier_value
          )
        ) offenderIdentifierInsertedEventOf(xtag) else offenderIdentifierDeletedEventOf(xtag)

        "S1_RESULT" -> if (!Strings.isNullOrEmpty(xtag.content.p_imprison_status_seq)) imprisonmentStatusChangedEventOf(
          xtag
        ) else if (!Strings.isNullOrEmpty(xtag.content.p_assessment_seq)) assessmentChangedEventOf(xtag) else if (!Strings.isNullOrEmpty(
            xtag.content.p_alert_date
          )
        ) alertUpdatedEventOf(xtag) else alertInsertedEventOf(xtag)

        "OFF_ALERT_INSERT" -> alertInsertedEventOf(xtag)
        "OFF_ALERT_UPDATE" -> alertUpdatedEventOf(xtag)
        "OFF_ALERT_DELETE", "S1_DEL_RESULT" -> alertDeletedEventOf(xtag)
        "INCIDENT-INSERTED" -> incidentInsertedEventOf(xtag)
        "INCIDENT-UPDATED" -> incidentUpdatedEventOf(xtag)
        "OFF_IMP_STAT_OASYS" -> imprisonmentStatusChangedEventOf(xtag)
        "OFF_PROF_DETAIL_INS" -> offenderProfileDetailInsertedEventOf(xtag)
        "OFF_PROF_DETAIL_UPD" -> offenderProfileUpdatedEventOf(xtag)
        "S2_RESULT" -> sentenceDatesChangedEventOf(xtag)
        "SENTENCING-CHANGED" -> sentencingChangedEventOf(xtag)
        "A2_CALLBACK" -> hearingDateChangedEventOf(xtag)
        "A2_RESULT" -> if ("Y" == xtag.content.p_delete_flag) hearingResultDeletedEventOf(xtag) else hearingResultChangedEventOf(
          xtag
        )

        "PHONES_INS" -> phoneInsertedEventOf(xtag)
        "PHONES_UPD" -> phoneUpdatedEventOf(xtag)
        "PHONES_DEL" -> phoneDeletedEventOf(xtag)
        "OFF_EMPLOYMENTS_INS" -> offenderEmploymentInsertedEventOf(xtag)
        "OFF_EMPLOYMENTS_UPD" -> offenderEmploymentUpdatedEventOf(xtag)
        "OFF_EMPLOYMENTS_DEL" -> offenderEmploymentDeletedEventOf(xtag)
        "D5_RESULT" -> hdcConditionChanged(xtag)
        "D4_RESULT" -> hdcFineInserted(xtag)
        "ADDR_INS" -> personAddressInserted(xtag)
        "ADDR_UPD" -> {
          if (xtag.content.p_owner_class == "PER") {
            if (xtag.content.p_address_deleted == "N") personAddressUpdatedEventOf(xtag) else personAddressDeletedEventOf(
              xtag
            )
          } else if (xtag.content.p_owner_class == "OFF") {
            if (xtag.content.p_address_deleted == "N") offenderAddressUpdatedEventOf(xtag) else offenderAddressDeletedEventOf(
              xtag
            )
          } else {
            if (xtag.content.p_address_deleted == "N") addressUpdatedEventOf(xtag) else addressDeletedEventOf(xtag)
          }
        }

        "OFF_SENT_OASYS" -> sentenceCalculationDateChangedEventOf(xtag)
        "C_NOTIFICATION" -> courtSentenceChangedEventOf(xtag)
        "IEDT_OUT" -> offenderTransferOutOfLidsEventOf(xtag)
        "BED_ASSIGNMENT_HISTORY-INSERTED" -> offenderBedAssignmentEventOf(xtag)
        "CONFIRMED_RELEASE_DATE-CHANGED" -> confirmedReleaseDateOf(xtag)
        "OFFENDER-INSERTED", "OFFENDER-UPDATED", "OFFENDER-DELETED" -> offenderUpdatedOf(xtag)
        "EXTERNAL_MOVEMENT-CHANGED" -> externalMovementRecordEventOf(xtag, null)

        "OFFENDER_IEP_LEVEL-UPDATED" -> iepUpdatedEventOf(xtag)

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

  private fun offenderTransferOutOfLidsEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_TRANSFER-OUT_OF_LIDS",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun offenderBedAssignmentEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "BED_ASSIGNMENT_HISTORY-INSERTED",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    bedAssignmentSeq = xtag.content.p_bed_assign_seq?.toInt(),
    livingUnitId = xtag.content.p_living_unit_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun courtSentenceChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "COURT_SENTENCE-CHANGED",
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun alertDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ALERT-DELETED",
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    alertDateTime = localDateTimeOf(xtag.content.p_alert_date, xtag.content.p_alert_time),
    alertType = xtag.content.p_alert_type,
    alertCode = xtag.content.p_alert_code,
    expiryDateTime = localDateTimeOf(xtag.content.p_expiry_date, xtag.content.p_expiry_time),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun personAddressUpdatedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun offenderAddressUpdatedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun addressUpdatedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun personAddressDeletedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun offenderAddressDeletedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun addressDeletedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun personAddressInserted(xtag: Xtag) = OffenderEvent(
    eventType = "PERSON_ADDRESS-INSERTED",
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

  private fun hdcFineInserted(xtag: Xtag) = OffenderEvent(
    eventType = "HDC_FINE-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun hdcConditionChanged(xtag: Xtag) = OffenderEvent(
    eventType = "HDC_CONDITION-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceSeq = xtag.content.p_sentence_seq?.toLong(),
    conditionCode = xtag.content.p_condition_code,
    offenderSentenceConditionId = xtag.content.p_offender_sent_calculation_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderIndividualScheduleChanged(xtag: Xtag) = OffenderEvent(
    eventType = "APPOINTMENT_CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    scheduleEventId = xtag.content.p_event_id?.toLong(),
    scheduledStartTime = localDateTimeOf(xtag.content.p_event_date, xtag.content.p_start_time),
    scheduledEndTime =
    if (xtag.content.p_end_time == null) null else localDateTimeOf(
      xtag.content.p_event_date,
      xtag.content.p_end_time
    ),
    scheduleEventClass = xtag.content.p_event_class,
    scheduleEventType = xtag.content.p_event_type,
    scheduleEventSubType = xtag.content.p_event_sub_type,
    scheduleEventStatus = xtag.content.p_event_status,
    recordDeleted = "Y".equals(xtag.content.p_record_deleted),
    agencyLocationId = xtag.content.p_agy_loc_id,
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderEmploymentDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_EMPLOYMENT-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun phoneInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "PHONE-INSERTED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun phoneUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "PHONE-UPDATED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun phoneDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "PHONE-DELETED",
    ownerId = xtag.content.p_owner_id?.toLong(),
    ownerClass = xtag.content.p_owner_class,
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun hearingResultChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "HEARING_RESULT-CHANGED",
    oicHearingId = xtag.content.p_oic_hearing_id?.toLong(),
    resultSeq = xtag.content.p_result_seq?.toLong(),
    agencyIncidentId = xtag.content.p_agency_incident_id?.toLong(),
    chargeSeq = xtag.content.p_charge_seq?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun hearingResultDeletedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun hearingDateChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "HEARING_DATE-CHANGED",
    oicHearingId = xtag.content.p_oic_hearing_id?.toLong(),
    eventDatetime = xtag.nomisTimestamp,
    nomisEventType = xtag.eventType,
  )

  private fun sentenceCalculationDateChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "SENTENCE_CALCULATION_DATES-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun sentenceDatesChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "SENTENCE_DATES-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sentenceCalculationId = xtag.content.p_offender_sent_calculation_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun sentencingChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    nomisEventType = xtag.eventType,
  )

  private fun offenderProfileUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_PROFILE_DETAILS-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderProfileDetailInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_PROFILE_DETAILS-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun alertInsertedEventOf(xtag: Xtag) = OffenderEvent(
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

  private fun alertUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ALERT-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    alertSeq = xtag.content.p_alert_seq?.toLong(),
    alertDateTime = localDateTimeOf(xtag.content.p_old_alert_date, xtag.content.p_old_alert_time),
    alertType = xtag.content.p_alert_type,
    alertCode = xtag.content.p_alert_code,
    nomisEventType = xtag.eventType,
  )

  private fun assessmentChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ASSESSMENT-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    assessmentSeq = xtag.content.p_assessment_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun imprisonmentStatusChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "IMPRISONMENT_STATUS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    imprisonmentStatusSeq = xtag.content.p_imprison_status_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun incidentInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "INCIDENT-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    incidentCaseId = xtag.content.p_incident_case_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun incidentUpdatedEventOf(xtag: Xtag): OffenderEvent {
    val v =
      if (xtag.content.p_delete_flag == "N") "CHANGED-" else "DELETED-" + INCIDENT_TABLE_MAP[xtag.content.p_table_name]
    return OffenderEvent(
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

  private fun offenderIdentifierInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_IDENTIFIER-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    identifierValue = xtag.content.p_identifier_value,
    nomisEventType = xtag.eventType,
  )

  private fun offenderIdentifierDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_IDENTIFIER-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    nomisEventType = xtag.eventType,
  )

  private fun educationLevelInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "EDUCATION_LEVEL-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun educationLevelUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "EDUCATION_LEVEL-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun educationLevelDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "EDUCATION_LEVEL-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "CONTACT_PERSON-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "CONTACT_PERSON-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun contactPersonDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "CONTACT_PERSON-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    personId = xtag.content.p_person_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderAliasChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_ALIAS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    aliasOffenderId = xtag.content.p_alias_offender_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ADDRESS_USAGE-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ADDRESS_USAGE-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun addressUsageDeletedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "ADDRESS_USAGE-DELETED",
    eventDatetime = xtag.nomisTimestamp,
    addressId = xtag.content.p_address_id?.toLong(),
    addressUsage = xtag.content.p_address_usage,
    nomisEventType = xtag.eventType,
  )

  private fun offenderDetailsChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_DETAILS-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_BOOKING-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    identifierType = xtag.content.p_identifier_type,
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingChangedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_BOOKING-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    rootOffenderId = xtag.content.p_root_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderBookingReassignedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_BOOKING-REASSIGNED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    previousOffenderId = xtag.content.p_old_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  fun externalMovementRecordEventOf(xtag: Xtag, overrideEventType: String?) = OffenderEvent(
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

  private fun offenderMovementDischargeEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_MOVEMENT-DISCHARGE",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    movementSeq = xtag.content.p_movement_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderMovementReceptionEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_MOVEMENT-RECEPTION",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    movementSeq = xtag.content.p_movement_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun maternityStatusInsertedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "MATERNITY_STATUS-INSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun maternityStatusUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "MATERNITY_STATUS-UPDATED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun riskScoreEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "RISK_SCORE-" + if (xtag.content.p_delete_flag == "N") "CHANGED" else "DELETED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    riskPredictorId = xtag.content.p_offender_risk_predictor_id?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun offenderSanctionEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "OFFENDER_SANCTION-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    sanctionSeq = xtag.content.p_sanction_seq?.toLong(),
    nomisEventType = xtag.eventType,
  )

  private fun bookingNumberEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "BOOKING_NUMBER-CHANGED",
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    bookingNumber = xtag.content.p_new_prison_num,
    previousBookingNumber =
    xtag.content.p_old_prison_num ?: xtag.content.p_old_prision_num ?: xtag.content.p_old_prison_number,
    nomisEventType = xtag.eventType,
  )

  private fun confirmedReleaseDateOf(xtag: Xtag) = OffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
  )

  private fun offenderUpdatedOf(xtag: Xtag) = OffenderEvent(
    eventType = xtag.eventType,
    eventDatetime = xtag.nomisTimestamp,
    offenderId = xtag.content.p_offender_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
  )

  private fun iepUpdatedEventOf(xtag: Xtag) = OffenderEvent(
    eventType = "IEP_UPSERTED",
    eventDatetime = xtag.nomisTimestamp,
    bookingId = xtag.content.p_offender_book_id?.toLong(),
    offenderIdDisplay = xtag.content.p_offender_id_display,
    agencyLocationId = xtag.content.p_agy_loc_id,
    iepSeq = xtag.content.p_iep_level_seq?.toLong(),
    iepLevel = xtag.content.p_iep_level,
    nomisEventType = xtag.eventType,
    auditModuleName = xtag.content.p_audit_module_name,
  )

  companion object {
    private val log = LoggerFactory.getLogger(OffenderEventsTransformer::class.java)

    private val INCIDENT_TABLE_MAP = java.util.Map.of(
      "incident_cases", "CASES",
      "incident_case_parties", "PARTIES",
      "incident_case_responses", "RESPONSES",
      "incident_case_requirements", "REQUIREMENTS"
    )

    fun xtagFudgedTimestampOf(xtagEnqueueTime: LocalDateTime): LocalDateTime {
      val london: ZoneId = ZoneId.of("Europe/London")
      return if (london.rules.isDaylightSavings(xtagEnqueueTime.atZone(london).toInstant())) {
        xtagEnqueueTime
      } else xtagEnqueueTime.minusHours(1L)
    }

    fun externalMovementEventOf(xtag: Xtag): String {
      return when (xtag.content.p_record_deleted) {
        "N" -> "EXTERNAL_MOVEMENT_RECORD-INSERTED"
        "Y" -> "EXTERNAL_MOVEMENT_RECORD-DELETED"
        else -> "EXTERNAL_MOVEMENT_RECORD-UPDATED"
      }
    }

    fun localDateOf(date: String?): LocalDate? {
      val pattern = "[yyyy-MM-dd HH:mm:ss][yyyy-MM-dd][dd-MMM-yyyy][dd-MMM-yy]"
      try {
        return date?.let {
          LocalDate.parse(
            it,
            DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter()
          )
        }
      } catch (dtpe: DateTimeParseException) {
        log.error("Unable to parse {} into a LocalDate using pattern {}", date, pattern)
      }
      return null
    }

    fun localTimeOf(dateTime: String?): LocalTime? {
      val pattern = "[yyyy-MM-dd ]HH:mm:ss"
      try {
        return dateTime?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern(pattern)) }
      } catch (dtpe: DateTimeParseException) {
        log.error("Unable to parse {} into a LocalTime using pattern {}", dateTime, pattern)
      }
      return null
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
  }
}
