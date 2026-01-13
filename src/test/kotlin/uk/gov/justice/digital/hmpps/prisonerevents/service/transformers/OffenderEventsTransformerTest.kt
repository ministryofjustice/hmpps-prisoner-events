@file:Suppress("TestFunctionName")

package uk.gov.justice.digital.hmpps.prisonerevents.service.transformers

import oracle.jakarta.jms.AQjmsMapMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerevents.model.AgencyAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AgencyInternalLocationUpdatedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AgencyVisitSlotEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AgencyVisitTimesEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AlertOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.AssessmentUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPAttendeeOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPFactorOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPInterviewOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPPlanOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPReportOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CSIPReviewOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CaseIdentifierEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CorporateAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CorporateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CorporateInternetAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CorporatePhoneEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CorporateTypeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtAppearanceEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtCaseEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtCaseLinkingEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtEventChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtEventChargeLinkingEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.FinePaymentEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GLTransactionEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.HealthEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.IWPDocumentOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.LanguageEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.MilitaryEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.MovementApplicationEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.MovementApplicationMultiEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.NonAssociationDetailsOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBeliefsEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingNumberChangeOrMergeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingReassignedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderContactEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEmailEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderFixedTermRecallEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderIdentifierUpdatedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderIdentifyingMarksEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderImageEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderPhoneNumberEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceChargeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderSentenceTermEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OrderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonEmploymentEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonIdentifierEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonImageEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonInternetAddressEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonPhoneEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PrisonerActivityUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PrisonerAppointmentUpdateEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.RestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.ScheduledExternalMovementEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.TransactionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.VisitBalanceAdjustmentEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.VisitVisitorEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.VisitorRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.externalMovementEventOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localDateOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localDateTimeOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localTimeOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.xtagFudgedTimestampOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.Xtag
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.XtagContent
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class OffenderEventsTransformerTest {
  private val offenderEventsTransformer = OffenderEventsTransformer(aqHasDaylightSavings = false)
  private val fixedEventTime = LocalDateTime.now()

  @Suppress("UNCHECKED_CAST")
  private fun <T : OffenderEvent> withCallTransformer(xtag: Xtag, block: T.() -> Unit): Unit = (offenderEventsTransformer.offenderEventOf(xtag)!! as T).block()

  @Test
  fun offenderEventOfAQ() {
    val summerMessage = AQjmsMapMessage().apply {
      setString("p_offender_book_id", "12345")
      jmsType = "test"
      jmsTimestamp = 1661949223123 // Wednesday, 31 August 2022 13:33:43.123 BST
    }

    val winterMessage = AQjmsMapMessage().apply {
      setString("p_offender_book_id", "12345")
      jmsType = "test"
      jmsTimestamp = 1645939200000 // Sunday, 27 February 2022 05:20:00 GMT or 6:20 BST
    }

    assertThat(offenderEventsTransformer.offenderEventOf(summerMessage)?.eventDatetime).isEqualTo(LocalDateTime.parse("2022-08-31T13:33:43.123"))
    assertThat(offenderEventsTransformer.offenderEventOf(winterMessage)?.eventDatetime).isEqualTo(LocalDateTime.parse("2022-02-27T05:20:00.000"))
  }

  @Test
  fun xtagEnqueueTimestampIsSeasonallyAdjustedIntoDaylightSavings() {
    val lastSecondOfWinter = LocalDateTime.of(2019, 3, 31, 0, 59, 59)
    val firstSecondOfSummer = lastSecondOfWinter.plusSeconds(1L)
    assertThat(xtagFudgedTimestampOf(lastSecondOfWinter)).isEqualTo(lastSecondOfWinter.minusHours(1L))
    assertThat(xtagFudgedTimestampOf(firstSecondOfSummer)).isEqualTo(firstSecondOfSummer)
  }

  @Test
  fun xtagEnqueueTimestampIsSeasonallyAdjustedIntoUTC() {
    val lastSecondOfSummer = LocalDateTime.of(2019, 10, 27, 1, 59, 59)
    val firstSecondOfWinter = lastSecondOfSummer.plusSeconds(1L)
    assertThat(xtagFudgedTimestampOf(lastSecondOfSummer)).isEqualTo(lastSecondOfSummer)
    assertThat(xtagFudgedTimestampOf(firstSecondOfWinter)).isEqualTo(firstSecondOfWinter.minusHours(1L))
  }

  @Test
  fun externalMovementDescriptorBehavesAppropriately() {
    assertThat(
      externalMovementEventOf(
        Xtag(content = XtagContent(mapOf("p_record_deleted" to "Y"))),
      ),
    ).isEqualTo("EXTERNAL_MOVEMENT_RECORD-DELETED")
    assertThat(
      externalMovementEventOf(
        Xtag(content = XtagContent(mapOf("p_record_deleted" to "N"))),
      ),
    ).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED")
    assertThat(
      externalMovementEventOf(
        Xtag(content = XtagContent(mapOf("p_record_deleted" to UUID.randomUUID().toString()))),
      ),
    ).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED")
    assertThat(externalMovementEventOf(Xtag(content = XtagContent(mapOf()))))
      .isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED")
  }

  @Test
  fun localDateOfBehavesAppropriately() {
    assertThat(localDateOf("2019-02-14 10:11:12")).isEqualTo(LocalDate.of(2019, 2, 14))
    assertThat(localDateOf("14-FEB-2019")).isEqualTo(LocalDate.of(2019, 2, 14))
    assertThat(localDateOf("14-FEB-19")).isEqualTo(LocalDate.of(2019, 2, 14))
    assertThat(localDateOf(null)).isNull()
    assertThat(localDateOf("Some rubbish")).isNull()
  }

  @Test
  fun localTimeOfBehavesAppropriately() {
    assertThat(localTimeOf("2019-02-14 10:11:12")).isEqualTo(LocalTime.of(10, 11, 12))
    assertThat(localTimeOf("09:10:11")).isEqualTo(LocalTime.of(9, 10, 11))
    assertThat(localTimeOf(null)).isNull()
    assertThat(localTimeOf("Some rubbish")).isNull()
  }

  @Test
  fun localDateTimeOfDateAndTimeBehavesAppropriately() {
    assertThat(localDateTimeOf("2019-02-14 00:00:00", "1970-01-01 10:11:12"))
      .isEqualTo(LocalDateTime.of(2019, 2, 14, 10, 11, 12))
    assertThat(localDateTimeOf(null, "1970-01-01 10:11:12")).isNull()
    assertThat(localDateTimeOf(null, "Some rubbish")).isNull()
    assertThat(localDateTimeOf(null, null)).isNull()
    assertThat(localDateTimeOf("2019-02-14 00:00:00", "Some rubbish"))
      .isEqualTo(LocalDateTime.of(2019, 2, 14, 0, 0, 0))
    assertThat(localDateTimeOf("2019-02-14 00:00:00", null))
      .isEqualTo(LocalDateTime.of(2019, 2, 14, 0, 0, 0))
  }

  @Test
  fun externalMovementRecordEventOfHandlesAgyLocIdsAsStrings() {
    assertThat(
      offenderEventsTransformer.externalMovementRecordEventOf(
        Xtag(
          content = XtagContent(
            mapOf(
              "p_from_agy_loc_id" to "BARBECUE",
              "p_to_agy_loc_id" to "SAUCE",
            ),
          ),
        ),
        externalMovementEventOf(
          Xtag(
            content = XtagContent(
              mapOf(
                "p_from_agy_loc_id" to "BARBECUE",
                "p_to_agy_loc_id" to "SAUCE",
              ),
            ),
          ),
        ),
      ),
    ).extracting("fromAgencyLocationId", "toAgencyLocationId").containsOnly("BARBECUE", "SAUCE")
  }

  @Test
  fun unknownEventTypesAreHandledAppropriately() {
    assertThat(offenderEventsTransformer.offenderEventOf(null as Xtag?)).isNull()
    assertThat(offenderEventsTransformer.offenderEventOf(Xtag(content = XtagContent(mapOf())))).isNull()
    assertThat(
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          content = XtagContent(mapOf()),
          eventType = "meh",
        ),
      ),
    ).isNotNull
  }

  @Test
  fun `S2_RESULT IsMappedTo SENTENCE_DATES_CHANGED`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "S2_RESULT",
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
            "p_offender_sent_calculation_id" to "88",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCE_DATES-CHANGED")
      assertThat(bookingId).isEqualTo(99L)
      assertThat(sentenceCalculationId).isEqualTo(88L)
      assertThat(nomisEventType).isEqualTo("S2_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `OFF_SENT_OASYS IsMappedTo SENTENCE_CALCULATION_DATES_CHANGED`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_SENT_OASYS",
        content = XtagContent(
          mapOf("p_offender_book_id" to "234"),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCE_CALCULATION_DATES-CHANGED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_SENT_OASYS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun bedAssignmentCorrectlyMapped() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "BED_ASSIGNMENT_HISTORY-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
            "p_bed_assign_seq" to "1",
            "p_living_unit_id" to "34123412",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED")
      assertThat(bookingId).isEqualTo(99L)
      assertThat(bedAssignmentSeq).isEqualTo(1)
      assertThat(livingUnitId).isEqualTo(34123412L)
      assertThat(nomisEventType).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED")
      assertThat(eventDatetime).isEqualTo(now)
    }
  }

  @Test
  fun externalMovementChangedCorrectlyMapped() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "EXTERNAL_MOVEMENT-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "232",
            "p_from_agy_loc_id" to "MDI",
            "p_to_agy_loc_id" to "HOSP",
            "p_direction_code" to "OUT",
            "p_movement_type" to "REL",
            "p_movement_reason_code" to "HP",
            "p_movement_seq" to "1",
            "p_movement_date" to "2019-02-14",
            "p_movement_time" to "2019-02-14 10:11:12",
            "p_insert_flag" to "Y",
            "p_delete_flag" to "N",
            "p_audit_module_name" to "DPS_SYNCHRONISATION",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EXTERNAL_MOVEMENT-CHANGED")
      assertThat(bookingId).isEqualTo(232L)
      assertThat(movementSeq).isEqualTo(1)
      assertThat(nomisEventType).isEqualTo("EXTERNAL_MOVEMENT-CHANGED")
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(fromAgencyLocationId).isEqualTo("MDI")
      assertThat(toAgencyLocationId).isEqualTo("HOSP")
      assertThat(directionCode).isEqualTo("OUT")
      assertThat(movementReasonCode).isEqualTo("HP")
      assertThat(movementType).isEqualTo("REL")
      assertThat(recordInserted).isEqualTo(true)
      assertThat(recordDeleted).isEqualTo(false)
      assertThat(auditModuleName).isEqualTo("DPS_SYNCHRONISATION")
    }
  }

  @Test
  fun externalMovementAuditModuleHandlesNull() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "EXTERNAL_MOVEMENT-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(mapOf()),
      ),
    ) {
      assertThat(auditModuleName).isEqualTo("UNKNOWN_MODULE")
    }
  }

  @Test
  fun sentencingChangedCorrectlyMapped() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "SENTENCING-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "2322322",
            "p_offender_id_display" to "A1234AA",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCING-CHANGED")
      assertThat(bookingId).isEqualTo(2322322L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(offenderIdDisplay).isEqualTo("A1234AA")
    }
  }

  @Test
  fun confirmedReleaseDateChangeMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "CONFIRMED_RELEASE_DATE-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("CONFIRMED_RELEASE_DATE-CHANGED")
      assertThat(bookingId).isEqualTo(99L)
      assertThat(eventDatetime).isEqualTo(now)
    }
  }

  @Test
  fun offenderIndividualScheduledEventMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "SCHEDULE_INT_APP-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "LEI",
            "p_event_id" to "2362162",
            "p_event_date" to "2022-07-19",
            "p_event_status" to "SCH",
            "p_offender_book_id" to "52303",
            "p_event_sub_type" to "CALA",
            "p_start_time" to "16:00:00",
            "p_end_time" to "16:30:00",
            "p_event_class" to "INT_MOV",
            "p_event_type" to "APP",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("APPOINTMENT_CHANGED")
      assertThat(bookingId).isEqualTo(52303L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(agencyLocationId).isEqualTo("LEI")
      assertThat(scheduleEventId).isEqualTo(2362162)
      assertThat(scheduledStartTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 0, 0))
      assertThat(scheduledEndTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 30, 0))
      assertThat(scheduleEventStatus).isEqualTo("SCH")
      assertThat(scheduleEventType).isEqualTo("APP")
      assertThat(scheduleEventSubType).isEqualTo("CALA")
      assertThat(scheduleEventClass).isEqualTo("INT_MOV")
      assertThat(recordDeleted).isEqualTo(true)
    }
  }

  @Test
  fun offenderIndividualScheduledEventMappedCorrectlyWhenEndDateMissing() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "SCHEDULE_INT_APP-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "LEI",
            "p_event_id" to "2362162",
            "p_event_date" to "2022-07-19",
            "p_event_status" to "SCH",
            "p_offender_book_id" to "52303",
            "p_event_sub_type" to "CALA",
            "p_start_time" to "16:00:00",
            "p_event_class" to "INT_MOV",
            "p_event_type" to "APP",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("APPOINTMENT_CHANGED")
      assertThat(bookingId).isEqualTo(52303L)
      assertThat(scheduledStartTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 0, 0))
      assertThat(scheduledEndTime).isNull()
    }
  }

  @Test
  fun offenderIepUpdatedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER_IEP_LEVEL-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "MDI",
            "p_iep_level" to "STD",
            "p_iep_level_seq" to "3",
            "p_event_id" to "2323",
            "p_event_date" to "2022-08-23",
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_audit_module_name" to "transfer",
            "p_delete_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("IEP_UPSERTED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(iepSeq).isEqualTo(3)
      assertThat(iepLevel).isEqualTo("STD")
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(auditModuleName).isEqualTo("transfer")
    }
  }

  @Test
  fun offenderIepDeletedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER_IEP_LEVEL-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "MDI",
            "p_iep_level" to "STD",
            "p_iep_level_seq" to "3",
            "p_event_id" to "2323",
            "p_event_date" to "2022-08-23",
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_audit_module_name" to "transfer",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("IEP_DELETED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(iepSeq).isEqualTo(3)
      assertThat(iepLevel).isEqualTo("STD")
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(auditModuleName).isEqualTo("transfer")
    }
  }

  @Test
  fun offenderKeyDateAdjustmentUpdatedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_KEY_DATES_ADJ-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_offender_key_date_adjust_id" to "987",
            "p_audit_module_name" to "SENTDATES",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("KEY_DATE_ADJUSTMENT_UPSERTED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(adjustmentId).isEqualTo(987)
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(auditModuleName).isEqualTo("SENTDATES")
    }
  }

  @Test
  fun offenderKeyDateAdjustmentDeletedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_KEY_DATES_ADJ-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_offender_key_date_adjust_id" to "987",
            "p_audit_module_name" to "SENTDATES",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("KEY_DATE_ADJUSTMENT_DELETED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(adjustmentId).isEqualTo(987)
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(auditModuleName).isEqualTo("SENTDATES")
    }
  }

  @Test
  fun offenderSentenceAdjustmentUpdatedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_SENT_ADJ-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_sentence_seq" to "2",
            "p_offender_sentence_adjust_id" to "987",
            "p_audit_module_name" to "SENTDATES",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCE_ADJUSTMENT_UPSERTED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(adjustmentId).isEqualTo(987)
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(auditModuleName).isEqualTo("SENTDATES")
    }
  }

  @Test
  fun offenderSentenceAdjustmentDeletedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_SENT_ADJ-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_sentence_seq" to "2",
            "p_offender_sentence_adjust_id" to "987",
            "p_audit_module_name" to "SENTDATES",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCE_ADJUSTMENT_DELETED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(adjustmentId).isEqualTo(987)
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(auditModuleName).isEqualTo("SENTDATES")
    }
  }

  @Test
  fun visitCancelledMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = ("OFFENDER_VISIT-UPDATED"),
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "MDI",
            "p_offender_visit_id" to "4",
            "p_event_id" to "2323",
            "p_event_date" to "2022-08-23",
            "p_offender_book_id" to "434",
            "p_offender_id_display" to "AF123",
            "p_audit_module_name" to "visit_screen",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("VISIT_CANCELLED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(visitId).isEqualTo(4)
      assertThat(offenderIdDisplay).isEqualTo("AF123")
      assertThat(agencyLocationId).isEqualTo("MDI")
      assertThat(auditModuleName).isEqualTo("visit_screen")
    }
  }

  @Nested
  inner class OfficialVisits {
    @Test
    fun `OFFENDER_OFFICIAL_VISIT-INSERTED`() {
      val now = LocalDateTime.now()
      withCallTransformer<GenericOffenderEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agy_loc_id" to "MDI",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT-INSERTED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("MDI")
        assertThat(visitId).isEqualTo(10314507L)
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }

    @Test
    fun `OFFENDER_OFFICIAL_VISIT-UPDATED`() {
      val now = LocalDateTime.now()
      withCallTransformer<GenericOffenderEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agy_loc_id" to "MDI",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT-UPDATED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("MDI")
        assertThat(visitId).isEqualTo(10314507L)
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }

    @Test
    fun `OFFENDER_OFFICIAL_VISIT-DELETED`() {
      val now = LocalDateTime.now()
      withCallTransformer<GenericOffenderEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agy_loc_id" to "MDI",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT-DELETED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("MDI")
        assertThat(visitId).isEqualTo(10314507L)
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }
  }

  @Nested
  inner class OffenderBeliefsEvents {
    @Test
    fun offenderBeliefCreatedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderBeliefsEvent>(
        Xtag(
          eventType = ("OFFENDER_BELIEFS-INSERTED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "2323",
              "p_event_date" to "2022-08-23",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "offender_beliefs_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
              "p_belief_id" to "112233",
              "p_belief_code" to "BAPT",
              "p_effective_date" to "2012-10-09 15:21:59",
              "p_end_date" to "2014-05-06 18:57:22",
              "p_change_reason" to "new.change_reason",
              "p_comments" to "This is a comment",
              "p_verified_flag" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_BELIEFS-INSERTED")
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("offender_beliefs_screen")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(offenderBeliefId).isEqualTo(112233)
        assertThat(beliefCode).isEqualTo("BAPT")
        assertThat(effectiveDate).isEqualTo(LocalDateTime.parse("2012-10-09T15:21:59"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2014-05-06T18:57:22"))
        assertThat(changeReason).isEqualTo("new.change_reason")
        assertThat(comments).isEqualTo("This is a comment")
        assertThat(verifiedFlag).isFalse()
      }
    }

    @Test
    fun offenderBeliefUpdatedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderBeliefsEvent>(
        Xtag(
          eventType = ("OFFENDER_BELIEFS-UPDATED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "2323",
              "p_event_date" to "2022-08-23",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "offender_beliefs_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
              "p_belief_id" to "112233",
              "p_belief_code" to "BAPT",
              "p_effective_date" to "2012-10-09 15:21:59",
              "p_end_date" to "2014-05-06 18:57:22",
              "p_change_reason" to "new.change_reason",
              "p_comments" to "This is a comment",
              "p_verified_flag" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_BELIEFS-UPDATED")
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("offender_beliefs_screen")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(offenderBeliefId).isEqualTo(112233)
        assertThat(beliefCode).isEqualTo("BAPT")
        assertThat(effectiveDate).isEqualTo(LocalDateTime.parse("2012-10-09T15:21:59"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2014-05-06T18:57:22"))
        assertThat(changeReason).isEqualTo("new.change_reason")
        assertThat(comments).isEqualTo("This is a comment")
        assertThat(verifiedFlag).isFalse()
      }
    }

    @Test
    fun offenderBeliefDeletedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderBeliefsEvent>(
        Xtag(
          eventType = ("OFFENDER_BELIEFS-DELETED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "2323",
              "p_event_date" to "2022-08-23",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "offender_beliefs_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
              "p_belief_id" to "112233",
              "p_belief_code" to "BAPT",
              "p_effective_date" to "2012-10-09 15:21:59",
              "p_end_date" to "2014-05-06 18:57:22",
              "p_change_reason" to "new.change_reason",
              "p_comments" to "This is a comment",
              "p_verified_flag" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_BELIEFS-DELETED")
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("offender_beliefs_screen")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(offenderBeliefId).isEqualTo(112233)
        assertThat(beliefCode).isEqualTo("BAPT")
        assertThat(effectiveDate).isEqualTo(LocalDateTime.parse("2012-10-09T15:21:59"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2014-05-06T18:57:22"))
        assertThat(changeReason).isEqualTo("new.change_reason")
        assertThat(comments).isEqualTo("This is a comment")
        assertThat(verifiedFlag).isFalse()
      }
    }
  }

  @Nested
  inner class OfficialVisitVisitors {
    @Test
    fun `OFFENDER_OFFICIAL_VISIT_VISTORS-INSERTED`() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitVisitorEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT_VISTORS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_visit_visitor_id" to "32876004",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_person_id" to "4729590",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT_VISITORS-INSERTED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(visitVisitorId).isEqualTo(32876004L)
        assertThat(visitId).isEqualTo(10314507L)
        assertThat(personId).isEqualTo(4729590L)
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }

    @Test
    fun `OFFENDER_OFFICIAL_VISIT_VISTORS-UPDATED`() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitVisitorEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT_VISTORS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_visit_visitor_id" to "32876004",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT_VISITORS-UPDATED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(visitVisitorId).isEqualTo(32876004L)
        assertThat(visitId).isEqualTo(10314507L)
        // personId is optional
        assertThat(personId).isNull()
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }

    @Test
    fun `OFFENDER_OFFICIAL_VISIT_VISTORS-DELETED`() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitVisitorEvent>(
        Xtag(
          eventType = "OFFENDER_OFFICIAL_VISIT_VISTORS-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_visit_visitor_id" to "32876004",
              "p_offender_visit_id" to "10314507",
              "p_offender_book_id" to "1231132",
              "p_offender_id_display" to "A7764EC",
              "p_audit_module_name" to "OIDUVISI",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_OFFICIAL_VISIT_VISITORS-DELETED")
        assertThat(bookingId).isEqualTo(1231132L)
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(visitVisitorId).isEqualTo(32876004L)
        assertThat(visitId).isEqualTo(10314507L)
        assertThat(personId).isNull()
        assertThat(offenderIdDisplay).isEqualTo("A7764EC")
        assertThat(auditModuleName).isEqualTo("OIDUVISI")
      }
    }
  }

  @Nested
  inner class OfficialVisitsConfiguration {
    @Test
    fun `AGENCY_VISIT_TIMES-INSERTED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitTimesEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_TIMES-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_TIMES-INSERTED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }

    @Test
    fun `AGENCY_VISIT_TIMES-UPDATED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitTimesEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_TIMES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_TIMES-UPDATED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }

    @Test
    fun `AGENCY_VISIT_TIMES-DELETED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitTimesEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_TIMES-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_TIMES-DELETED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }

    @Test
    fun `AGENCY_VISIT_SLOTS-INSERTED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitSlotEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_SLOTS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_visit_slot_id" to "2377348",
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_SLOTS-INSERTED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyVisitSlotId).isEqualTo(2377348)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }

    @Test
    fun `AGENCY_VISIT_SLOTS-UPDATED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitSlotEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_SLOTS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_visit_slot_id" to "2377348",
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_SLOTS-UPDATED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyVisitSlotId).isEqualTo(2377348)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }

    @Test
    fun `AGENCY_VISIT_SLOTS-DELETED`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyVisitSlotEvent>(
        Xtag(
          eventType = "AGENCY_VISIT_SLOTS-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_visit_slot_id" to "2377348",
              "p_week_day" to "MON",
              "p_agy_loc_id" to "LEI",
              "p_audit_module_name" to "OIMVDTSL",
              "p_time_slot_seq" to "99",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_VISIT_SLOTS-DELETED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(agencyVisitSlotId).isEqualTo(2377348)
        assertThat(agencyLocationId).isEqualTo("LEI")
        assertThat(weekDay).isEqualTo("MON")
        assertThat(timeslotSequence).isEqualTo(99)
        assertThat(auditModuleName).isEqualTo("OIMVDTSL")
      }
    }
  }

  @Nested
  inner class VisitBalanceAdjustmentEvents {
    @Test
    fun visitBalanceAdjustmentCreatedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitBalanceAdjustmentEvent>(
        Xtag(
          eventType = ("OFFENDER_VISIT_BALANCE_ADJS-INSERTED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "2323",
              "p_event_date" to "2022-08-23",
              "p_offender_visit_balance_adj_id" to "112233",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_offender_id" to "12345",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "visit_balance_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_VISIT_BALANCE_ADJS-INSERTED")
        assertThat(visitBalanceAdjustmentId).isEqualTo(112233)
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(offenderId).isEqualTo(12345)
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("visit_balance_screen")
        assertThat(eventDatetime).isEqualTo(now)
      }
    }

    @Test
    fun visitBalanceAdjustmentUpdatedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitBalanceAdjustmentEvent>(
        Xtag(
          eventType = ("OFFENDER_VISIT_BALANCE_ADJS-UPDATED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "2323",
              "p_event_date" to "2022-08-23",
              "p_offender_visit_balance_adj_id" to "112233",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_offender_id" to "12345",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "visit_balance_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_VISIT_BALANCE_ADJS-UPDATED")
        assertThat(visitBalanceAdjustmentId).isEqualTo(112233)
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(offenderId).isEqualTo(12345)
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("visit_balance_screen")
        assertThat(eventDatetime).isEqualTo(now)
      }
    }

    @Test
    fun visitBalanceAdjustmentDeletedMappedCorrectly() {
      val now = LocalDateTime.now()
      withCallTransformer<VisitBalanceAdjustmentEvent>(
        Xtag(
          eventType = ("OFFENDER_VISIT_BALANCE_ADJS-DELETED"),
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_visit_balance_adj_id" to "112233",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A1234BC",
              "p_offender_id" to "12345",
              "p_root_offender_id" to "34567",
              "p_audit_module_name" to "visit_balance_screen",
              "p_nomis_timestamp" to "20230509215740.443718000",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_VISIT_BALANCE_ADJS-DELETED")
        assertThat(visitBalanceAdjustmentId).isEqualTo(112233)
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(offenderId).isEqualTo(12345)
        assertThat(rootOffenderId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("visit_balance_screen")
        assertThat(eventDatetime).isEqualTo(now)
      }
    }
  }

  @Test
  fun caseNotesMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER_CASE_NOTES-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "12345",
            "p_case_note_id" to "456789",
            "p_case_note_type" to "CHAP",
            "p_case_note_sub_type" to "FAITH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CASE_NOTES-INSERTED")
      assertThat(bookingId).isEqualTo(12345L)
      assertThat(eventDatetime).isEqualTo(now)
      assertThat(caseNoteId).isEqualTo(456789L)
      assertThat(caseNoteType).isEqualTo("CHAP")
      assertThat(caseNoteSubType).isEqualTo("FAITH")
    }
  }

  @Test
  fun `offender physical attributes changes mapped correctly`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_PHYS_ATTR-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(mapOf("p_offender_id_display" to "A123BC", "p_offender_book_id" to "12345")),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHYSICAL_ATTRIBUTES-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
    }
  }

  @Test
  fun `offender physical characteristics changes mapped correctly`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_PROFILE_DETS-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_profile_code" to "MOS",
            "p_profile_seq" to "1",
            "p_profile_type" to "RELF",
            "p_caseload_type" to "INST",
            "p_list_seq" to "16",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHYSICAL_DETAILS-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(profileType).isEqualTo("RELF")
    }
  }

  @Test
  fun `offender physical marks changes mapped correctly`() {
    withCallTransformer<OffenderIdentifyingMarksEvent>(
      Xtag(
        eventType = "OFF_IDENT_MARKS-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_id_mark_seq" to "3",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFYING_MARKS-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(nomisEventType).isEqualTo("OFF_IDENT_MARKS-CHANGED")
      assertThat(idMarkSeq).isEqualTo(3)
    }
  }

  @Test
  fun `offender physical marks deleted mapped correctly`() {
    withCallTransformer<OffenderIdentifyingMarksEvent>(
      Xtag(
        eventType = "OFF_IDENT_MARKS-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_id_mark_seq" to "3",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFYING_MARKS-DELETED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(nomisEventType).isEqualTo("OFF_IDENT_MARKS-CHANGED")
      assertThat(idMarkSeq).isEqualTo(3)
    }
  }

  @Test
  fun `non-association detail changes mapped correctly`() {
    withCallTransformer<NonAssociationDetailsOffenderEvent>(
      Xtag(
        eventType = "OFF_NA_DETAILS_ASSOC-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_ns_offender_id_display" to "G4567DE",
            "p_offender_book_id" to "12345",
            "p_ns_offender_book_id" to "67890",
            "p_type_seq" to "12",
            "p_ns_reason_code" to "REASON",
            "p_ns_level_code" to "LEVEL",
            "p_ns_type" to "TYPE",
            "p_ns_effective_date" to "12-AUG-2022",
            "p_ns_expiry_date" to "2023-03-31",
            "p_authorized_staff" to "staff 1",
            "p_comment_text" to "comment",
            "p_audit_module_name" to "non_associations",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("NON_ASSOCIATION_DETAIL-UPSERTED")
      assertThat(nomisEventType).isEqualTo("OFF_NA_DETAILS_ASSOC-UPDATED")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345L)
      assertThat(nsOffenderIdDisplay).isEqualTo("G4567DE")
      assertThat(nsBookingId).isEqualTo(67890L)
      assertThat(typeSeq).isEqualTo(12L)
      assertThat(reasonCode).isEqualTo("REASON")
      assertThat(levelCode).isEqualTo("LEVEL")
      assertThat(nsType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(authorisedBy).isEqualTo("staff 1")
      assertThat(auditModuleName).isEqualTo("non_associations")
    }
  }

  @Test
  fun `restriction changes mapped correctly for creation`() {
    withCallTransformer<RestrictionOffenderEvent>(
      Xtag(
        eventType = "OFF_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_offender_restriction_id" to "12345678900",
            "p_restriction_type" to "TYPE",
            "p_effective_date" to "12-AUG-2022",
            "p_expiry_date" to "2023-03-31",
            "p_authorised_staff_id" to "12345",
            "p_comment_text" to "comment",
            "p_entered_staff_id" to "23456",
            "p_audit_module_name" to "OIDVIRES",
            "p_update_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("RESTRICTION-UPSERTED")
      assertThat(nomisEventType).isEqualTo("OFF_RESTRICTS-UPDATED")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345L)
      assertThat(offenderRestrictionId).isEqualTo(12345678900L)
      assertThat(restrictionType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(isUpdated).isFalse
    }
  }

  @Test
  fun `restriction changes mapped correctly for update`() {
    withCallTransformer<RestrictionOffenderEvent>(
      Xtag(
        eventType = "OFF_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_offender_restriction_id" to "12345678900",
            "p_restriction_type" to "TYPE",
            "p_effective_date" to "12-AUG-2022",
            "p_expiry_date" to "2023-03-31",
            "p_authorised_staff_id" to "12345",
            "p_comment_text" to "comment",
            "p_entered_staff_id" to "23456",
            "p_audit_module_name" to "OIDVIRES",
            "p_update_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("RESTRICTION-UPSERTED")
      assertThat(nomisEventType).isEqualTo("OFF_RESTRICTS-UPDATED")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345L)
      assertThat(offenderRestrictionId).isEqualTo(12345678900L)
      assertThat(restrictionType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(isUpdated).isTrue
    }
  }

  @Test
  fun `restriction changes mapped correctly for delete`() {
    withCallTransformer<RestrictionOffenderEvent>(
      Xtag(
        eventType = "OFF_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_offender_book_id" to "12345",
            "p_offender_restriction_id" to "12345678900",
            "p_restriction_type" to "TYPE",
            "p_effective_date" to "12-AUG-2022",
            "p_expiry_date" to "2023-03-31",
            "p_authorised_staff_id" to "12345",
            "p_comment_text" to "comment",
            "p_entered_staff_id" to "23456",
            "p_audit_module_name" to "OIDVIRES",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("RESTRICTION-DELETED")
      assertThat(nomisEventType).isEqualTo("OFF_RESTRICTS-UPDATED")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(bookingId).isEqualTo(12345L)
      assertThat(offenderRestrictionId).isEqualTo(12345678900L)
      assertThat(restrictionType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
    }
  }

  @Test
  fun `restriction person changes mapped correctly`() {
    withCallTransformer<PersonRestrictionOffenderEvent>(
      Xtag(
        eventType = "OFF_PERS_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "1234567",
            "p_offender_person_restrict_id" to "2345678",
            "p_restriction_type" to "TYPE",
            "p_restriction_effective_date" to "12-AUG-2022",
            "p_restriction_expiry_date" to "2023-03-31",
            "p_authorized_staff_id" to "12345",
            "p_comment_text" to "comment",
            "p_entered_staff_id" to "23456",
            "p_audit_module_name" to "OIUOVRES",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PERSON_RESTRICTION-UPSERTED")
      assertThat(nomisEventType).isEqualTo("OFF_PERS_RESTRICTS-UPDATED")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
      assertThat(contactPersonId).isEqualTo(1234567L)
      assertThat(offenderPersonRestrictionId).isEqualTo(2345678L)
      assertThat(restrictionType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
      assertThat(auditModuleName).isEqualTo("OIUOVRES")
    }
  }

  @Test
  fun `restriction person changes with missing audit moduke mapped correctly`() {
    withCallTransformer<PersonRestrictionOffenderEvent>(
      Xtag(
        eventType = "OFF_PERS_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "1234567",
            "p_offender_person_restrict_id" to "2345678",
            "p_restriction_type" to "TYPE",
            "p_restriction_effective_date" to "12-AUG-2022",
            "p_restriction_expiry_date" to "2023-03-31",
            "p_authorized_staff_id" to "12345",
            "p_comment_text" to "comment",
            "p_entered_staff_id" to "23456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PERSON_RESTRICTION-UPSERTED")
      assertThat(auditModuleName).isEqualTo("UNKNOWN")
    }
  }

  @Test
  fun `restriction visitor mapped correctly`() {
    withCallTransformer<VisitorRestrictionOffenderEvent>(
      Xtag(
        eventType = "VISITOR_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_person_id" to "12345",
            "p_visit_restriction_type" to "TYPE",
            "p_effective_date" to "12-AUG-2022",
            "p_expiry_date" to "2023-03-31",
            "p_comment_txt" to "comment",
            "p_visitor_restriction_id" to "123456",
            "p_entered_staff_id" to "23456",
            "p_audit_module_name" to "OMUVREST",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("VISITOR_RESTRICTION-UPSERTED")
      assertThat(nomisEventType).isEqualTo("VISITOR_RESTRICTS-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(personId).isEqualTo(12345)
      assertThat(restrictionType).isEqualTo("TYPE")
      assertThat(effectiveDate).isEqualTo(LocalDate.of(2022, 8, 12))
      assertThat(expiryDate).isEqualTo(LocalDate.of(2023, 3, 31))
      assertThat(visitorRestrictionId).isEqualTo(123456)
      assertThat(enteredById).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("OMUVREST")
    }
  }

  @Test
  fun `restriction visitor with missing module is mapped correctly`() {
    withCallTransformer<VisitorRestrictionOffenderEvent>(
      Xtag(
        eventType = "VISITOR_RESTRICTS-UPDATED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A123BC",
            "p_person_id" to "12345",
            "p_visit_restriction_type" to "TYPE",
            "p_effective_date" to "12-AUG-2022",
            "p_expiry_date" to "2023-03-31",
            "p_comment_txt" to "comment",
            "p_visitor_restriction_id" to "123456",
            "p_entered_staff_id" to "23456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("VISITOR_RESTRICTION-UPSERTED")
      assertThat(auditModuleName).isEqualTo("UNKNOWN")
    }
  }

  @Test
  fun `risk score mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P8_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_risk_predictor_id" to "987",
            "p_delete_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("RISK_SCORE-CHANGED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(riskPredictorId).isEqualTo(987)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("P8_RESULT")
    }
  }

  @Test
  fun `risk score deletion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P8_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_risk_predictor_id" to "987",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("RISK_SCORE-DELETED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(riskPredictorId).isEqualTo(987)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("P8_RESULT")
    }
  }

  @Test
  fun `offender sanction mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "A3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_sanction_seq" to "987",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_SANCTION-CHANGED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(sanctionSeq).isEqualTo(987)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("A3_RESULT")
    }
  }

  @Test
  fun `booking number P1_RESULT mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderBookingNumberChangeOrMergeEvent>(
      Xtag(
        eventType = "P1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id" to "987",
            "p_new_prison_num" to "Y123CD",
            "p_old_prison_num" to "Y123AB",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(offenderId).isEqualTo(987)
      assertThat(bookingNumber).isEqualTo("Y123CD")
      assertThat(previousBookingNumber).isEqualTo("Y123AB")
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("P1_RESULT")
    }
  }

  @Test
  fun `booking number mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderBookingNumberChangeOrMergeEvent>(
      Xtag(
        eventType = "BOOK_UPD_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_offender_id" to "987",
            "p_old_prison_num" to "Y123AB",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("BOOKING_NUMBER-CHANGED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(offenderId).isEqualTo(987)
      assertThat(bookingNumber).isNull()
      assertThat(previousBookingNumber).isEqualTo("Y123AB")
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("BOOK_UPD_OASYS")
    }
  }

  @Test
  fun `maternity status inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_HEALTH_PROB_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("MATERNITY_STATUS-INSERTED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_HEALTH_PROB_INS")
    }
  }

  @Test
  fun `maternity status updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_HEALTH_PROB_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("MATERNITY_STATUS-UPDATED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_HEALTH_PROB_UPD")
    }
  }

  @Test
  fun `offender movement reception mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_RECEP_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_movement_seq" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_MOVEMENT-RECEPTION")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(movementSeq).isEqualTo(789)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_RECEP_OASYS")
    }
  }

  @Nested
  inner class TransactionEvents {
    @Test
    fun `GL transaction mapped correctly`() {
      val now = LocalDateTime.now()
      withCallTransformer<GLTransactionEvent>(
        Xtag(
          eventType = "GL_TRANSACTIONS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            // A lot of fields come in from the trigger but we dont need most of them
            mapOf(
              "p_txn_id" to "1234567890123",
              "p_txn_entry_seq" to "5",
              "p_caseload_id" to "RMI",
              "p_offender_id" to "xxxxx",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A2468ZZ",
              "p_gl_entry_seq" to "17",
              "p_account_period_id" to "xxxxx",
              "p_account_code" to "xxxxx",
              "p_txn_entry_date" to "YYYY-MM-DD HH24:MI",
              "p_txn_type" to "xxxxx",
              "p_txn_post_usage" to "xxxxx",
              "p_txn_entry_amount" to "xxxxx",
              "p_txn_entry_desc" to "xxxxx",
              "p_txn_reference_number" to "xxxxx",
              "p_bank_statement_date" to "YYYY-MM-DD HH24:MI",
              "p_recon_clear_flag" to "xxxxx",
              "p_txn_reversed_flag" to "xxxxx",
              "p_reversed_txn_id" to "xxxxx",
              "p_payee_person_id" to "xxxxx",
              "p_reversed_txn_entry_seq" to "xxxxx",
              "p_reversed_gl_entry_seq" to "xxxxx",
              "p_payee_corporate_id" to "xxxxx",
              "p_payee_name_text" to "xxxxx",
              "p_txn_object_code" to "xxxxx",
              "p_list_seq" to "xxxxx",
              "p_txn_object_id" to "xxxxx",
              "p_create_date" to "YYYY-MM-DD HH24:MI",
              "p_info_number" to "xxxxx",
              "p_deduction_id" to "xxxxx",
              "p_txn_entry_time" to "YYYY-MM-DD HH24:MI",
              "p_receipt_number" to "xxxxx",
              "p_reversal_reason_code" to "xxxxx",
              "p_txn_loc_id" to "xxxxx",
              "p_payee_clear_flag" to "xxxxx",
              "p_audit_module_name" to "MODULE",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("GL_TRANSACTIONS-INSERTED")
        assertThat(nomisEventType).isEqualTo("GL_TRANSACTIONS-INSERTED")
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A2468ZZ")
        assertThat(transactionId).isEqualTo(1234567890123L)
        assertThat(entrySequence).isEqualTo(5)
        assertThat(gLEntrySequence).isEqualTo(17)
        assertThat(caseload).isEqualTo("RMI")
        assertThat(auditModuleName).isEqualTo("MODULE")
      }
    }

    @Test
    fun `offender transaction mapped correctly`() {
      val now = LocalDateTime.now()
      withCallTransformer<TransactionOffenderEvent>(
        Xtag(
          eventType = "OFFENDER_TRANSACTIONS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            // A lot of fields come in from the trigger but we dont need most of them
            mapOf(
              "p_txn_id" to "123456789",
              "p_txn_entry_seq" to "5",
              "p_caseload_id" to "RMI",
              "p_offender_id" to "xxxxx",
              "p_offender_book_id" to "434",
              "p_offender_id_display" to "A2468ZZ",
              "p_txn_posting_type" to "xxxxx",
              "p_txn_type" to "xxxxx",
              "p_txn_entry_desc" to "xxxxx",
              "p_txn_entry_amount" to "xxxxx",
              "p_txn_entry_date" to "YYYY-MM-DD HH24:MI",
              "p_sub_account_type" to "xxxxx",
              "p_txn_reference_number" to "xxxxx",
              "p_modify_date" to "YYYY-MM-DD HH24:MI",
              "p_receipt_number" to "xxxxx",
              "p_slip_printed_flag" to "xxxxx",
              "p_transfer_caseload_id" to "xxxxx",
              "p_receipt_printed_flag" to "xxxxx",
              "p_pre_withhold_amount" to "xxxxx",
              "p_deduction_flag" to "xxxxx",
              "p_closing_cheque_number" to "xxxxx",
              "p_remitter_name" to "xxxxx",
              "p_payee_code" to "xxxxx",
              "p_payee_name_text" to "xxxxx",
              "p_payee_corporate_id" to "xxxxx",
              "p_payee_person_id" to "xxxxx",
              "p_adjust_txn_id" to "xxxxx",
              "p_adjust_txn_entry_id" to "xxxxx",
              "p_adjust_offender_id" to "xxxxx",
              "p_adjust_account_code" to "xxxxx",
              "p_txn_adjusted_flag" to "xxxxx",
              "p_deduction_type" to "xxxxx",
              "p_info_number" to "xxxxx",
              "p_hold_clear_flag" to "xxxxx",
              "p_hold_until_date" to "YYYY-MM-DD HH24:MI",
              "p_hold_number" to "xxxxx",
              "p_gross_amount" to "xxxxx",
              "p_gross_net_flag" to "xxxxx",
              "p_remitter_id" to "xxxxx",
              "p_apply_spending_limit_amount" to "xxxxx",
              "p_receipt_pending_print_flag" to "xxxxx",
              "p_event_id" to "xxxxx",
              "p_from_date" to "YYYY-MM-DD HH24:MI",
              "p_to_date" to "YYYY-MM-DD HH24:MI",
              "p_client_unique_ref" to "xxxxx",
              "p_audit_module_name" to "MODULE",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_TRANSACTIONS-INSERTED")
        assertThat(nomisEventType).isEqualTo("OFFENDER_TRANSACTIONS-INSERTED")
        assertThat(bookingId).isEqualTo(434L)
        assertThat(offenderIdDisplay).isEqualTo("A2468ZZ")
        assertThat(transactionId).isEqualTo(123456789L)
        assertThat(entrySequence).isEqualTo(5)
        assertThat(caseload).isEqualTo("RMI")
        assertThat(auditModuleName).isEqualTo("MODULE")
      }
    }
  }

  @Test
  fun `offender movement discharge mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_DISCH_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_movement_seq" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_MOVEMENT-DISCHARGE")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(movementSeq).isEqualTo(789)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_DISCH_OASYS")
    }
  }

  @Test
  fun `external movement mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "M1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_movement_seq" to "789",
            "p_movement_date" to "2019-02-14",
            "p_movement_time" to "12:23:30",
            "p_movement_type" to "TAP",
            "p_movement_reason_code" to "EXT",
            "p_direction_code" to "IN",
            "p_escort_code" to "ECS",
            "p_from_agy_loc_id" to "MDI",
            "p_to_agy_loc_id" to "LIE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(movementSeq).isEqualTo(789)
      assertThat(movementDateTime).isEqualTo(LocalDateTime.parse("2019-02-14T12:23:30"))
      assertThat(movementType).isEqualTo("TAP")
      assertThat(movementReasonCode).isEqualTo("EXT")
      assertThat(directionCode).isEqualTo("IN")
      assertThat(escortCode).isEqualTo("ECS")
      assertThat(fromAgencyLocationId).isEqualTo("MDI")
      assertThat(toAgencyLocationId).isEqualTo("LIE")
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("M1_RESULT")
    }
  }

  @Test
  fun `external movement insertion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "M1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_record_deleted" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED")
      assertThat(nomisEventType).isEqualTo("M1_RESULT")
    }
  }

  @Test
  fun `external movement deletion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "M1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
            "p_record_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EXTERNAL_MOVEMENT_RECORD-DELETED")
      assertThat(nomisEventType).isEqualTo("M1_RESULT")
    }
  }

  @Test
  fun `external movement update result mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<ExternalMovementOffenderEvent>(
      Xtag(
        eventType = "M1_UPD_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "434",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED")
      assertThat(nomisEventType).isEqualTo("M1_UPD_RESULT")
    }
  }

  @Test
  fun `offender booking changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_UPD_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_offender_book_id" to "434",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_BOOKING-CHANGED")
      assertThat(bookingId).isEqualTo(434L)
      assertThat(offenderId).isEqualTo(123L)
      assertThat(rootOffenderId).isEqualTo(456L)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_UPD_OASYS")
    }
  }

  @Test
  fun `offender booking deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "BOOKING-DELETED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A1234AA",
            "p_offender_book_id" to "1234",
          ),
        ),
      ),
    ) {
      assertThat(bookingId).isEqualTo(1234L)
      assertThat(offenderIdDisplay).isEqualTo("A1234AA")
      assertThat(nomisEventType).isEqualTo("BOOKING-DELETED")
      assertThat(eventType).isEqualTo("BOOKING-DELETED")
    }
  }

  @Test
  fun `offender details changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_UPD_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_DETAILS-CHANGED")
      assertThat(bookingId).isNull()
      assertThat(offenderId).isEqualTo(123L)
      assertThat(rootOffenderId).isEqualTo(456L)
      assertThat(offenderIdDisplay).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_UPD_OASYS")
    }
  }

  @Test
  fun `address usage inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_USG_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "123",
            "p_address_usage" to "usage",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ADDRESS_USAGE-INSERTED")
      assertThat(addressId).isEqualTo(123L)
      assertThat(addressUsage).isEqualTo("usage")
      assertThat(nomisEventType).isEqualTo("ADDR_USG_INS")
    }
  }

  @Test
  fun `address usage deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_USG_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "123",
            "p_address_usage" to "usage",
            "p_address_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ADDRESS_USAGE-DELETED")
      assertThat(addressId).isEqualTo(123L)
      assertThat(addressUsage).isEqualTo("usage")
      assertThat(nomisEventType).isEqualTo("ADDR_USG_UPD")
    }
  }

  @Test
  fun `address usage updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_USG_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "123",
            "p_address_usage" to "usage",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ADDRESS_USAGE-UPDATED")
      assertThat(addressId).isEqualTo(123L)
      assertThat(addressUsage).isEqualTo("usage")
      assertThat(nomisEventType).isEqualTo("ADDR_USG_UPD")
    }
  }

  @Test
  fun `offender alias changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P4_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_alias_offender_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ALIAS-CHANGED")
      assertThat(offenderId).isEqualTo(123L)
      assertThat(rootOffenderId).isEqualTo(456L)
      assertThat(aliasOffenderId).isEqualTo(789L)
      assertThat(nomisEventType).isEqualTo("P4_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P2_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER-UPDATED")
      assertThat(offenderId).isEqualTo(123L)
      assertThat(rootOffenderId).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("P2_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender booking inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_BKB_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_offender_book_id" to "456",
            "p_identifier_type" to "some type",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_BOOKING-INSERTED")
      assertThat(offenderId).isEqualTo(123L)
      assertThat(bookingId).isEqualTo(456L)
      assertThat(identifierType).isEqualTo("some type")
      assertThat(nomisEventType).isEqualTo("OFF_BKB_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender booking inserted p3_result mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_offender_book_id" to "456",
            "p_identifier_type" to "NOMISP3",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_BOOKING-INSERTED")
      assertThat(offenderId).isEqualTo(123L)
      assertThat(bookingId).isEqualTo(456L)
      assertThat(identifierType).isEqualTo("NOMISP3")
      assertThat(nomisEventType).isEqualTo("P3_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender booking updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderBookingReassignedEvent>(
      Xtag(
        eventType = "OFF_BKB_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_old_offender_id" to "456",
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_BOOKING-REASSIGNED")
      assertThat(offenderId).isEqualTo(123L)
      assertThat(offenderIdDisplay).isNull() // null since xtagEventsService will add the prison number
      assertThat(previousOffenderId).isEqualTo(456L)
      assertThat(previousOffenderIdDisplay).isNull() // null since xtagEventsService will add the prison number
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_BKB_UPD")
      assertThat(offenderIdDisplay).isNull()
      assertThat(bookingStartDateTime).isNull() // null since xtagEventsService will add the booking start Date
      assertThat(bookingEndDateTime).isNull() // null since xtagEventsService will add the booking end Date
      assertThat(lastAdmissionDate).isNull() // null since xtagEventsService will add the external movement Date
    }
  }

  @Test
  fun `person inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_CONT_PER_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_person_id" to "456",
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("CONTACT_PERSON-INSERTED")
      assertThat(personId).isEqualTo(456L)
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_CONT_PER_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `person deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_CONT_PER_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_person_id" to "456",
            "p_offender_book_id" to "789",
            "p_address_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("CONTACT_PERSON-DELETED")
      assertThat(personId).isEqualTo(456L)
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_CONT_PER_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `person updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_CONT_PER_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_person_id" to "456",
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("CONTACT_PERSON-UPDATED")
      assertThat(personId).isEqualTo(456L)
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_CONT_PER_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender education inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EDUCATION_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EDUCATION_LEVEL-INSERTED")
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_EDUCATION_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender education updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EDUCATION_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EDUCATION_LEVEL-UPDATED")
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_EDUCATION_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender education deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EDUCATION_DEL",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "789",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("EDUCATION_LEVEL-DELETED")
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_EDUCATION_DEL")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender identifier inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderIdentifierUpdatedEvent>(
      Xtag(
        eventType = "P3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_identifier_type" to "some type",
            "p_identifier_value" to "value",
            "p_offender_id_seq" to "2",
            "p_audit_module_name" to "OCUIMAGE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFIER-INSERTED")
      assertThat(offenderId).isEqualTo(123)
      assertThat(offenderIdSeq).isEqualTo(2)
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(identifierType).isEqualTo("some type")
      assertThat(identifierValue).isEqualTo("value")
      assertThat(nomisEventType).isEqualTo("P3_RESULT")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("OCUIMAGE")
    }
  }

  @Test
  fun `offender identifier deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderIdentifierUpdatedEvent>(
      Xtag(
        eventType = "P3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_identifier_type" to "some type",
            "p_offender_id_seq" to "2",
            "p_audit_module_name" to "OCUIMAGE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFIER-DELETED")
      assertThat(offenderId).isEqualTo(123)
      assertThat(offenderIdSeq).isEqualTo(2)
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(identifierType).isEqualTo("some type")
      assertThat(nomisEventType).isEqualTo("P3_RESULT")
      assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender identifier updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderIdentifierUpdatedEvent>(
      Xtag(
        eventType = "OFFENDER_IDENTIFIERS-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "456",
            "p_offender_id" to "123",
            "p_offender_id_seq" to "2",
            "p_offender_id_display" to "A2435CD",
            "p_identifier_type" to "some type",
            "p_identifier_value" to "value",
            "p_nomis_timestamp" to "20230509215740.443718000",
            "p_audit_module_name" to "OCUIMAGE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFIER-UPDATED")
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(offenderId).isEqualTo(123)
      assertThat(offenderIdSeq).isEqualTo(2)
      assertThat(offenderIdDisplay).isEqualTo("A2435CD")
      assertThat(identifierType).isEqualTo("some type")
      assertThat(identifierValue).isEqualTo("value")
      assertThat(nomisEventType).isEqualTo("OFFENDER_IDENTIFIERS-UPDATED")
      assertThat(eventDatetime).isEqualTo(LocalDateTime.parse("2023-05-09T21:57:40.443718"))
      assertThat(auditModuleName).isEqualTo("OCUIMAGE")
    }
  }

  @Test
  fun `imprisonment status changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "S1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "456",
            "p_imprison_status_seq" to "123",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("IMPRISONMENT_STATUS-CHANGED")
      assertThat(bookingId).isEqualTo(456L)
      assertThat(imprisonmentStatusSeq).isEqualTo(123L)
      assertThat(nomisEventType).isEqualTo("S1_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `deprecated assessment changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<AssessmentUpdateEvent>(
      Xtag(
        eventType = "S1_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "456",
            "p_assessment_seq" to "123",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ASSESSMENT-CHANGED")
      assertThat(bookingId).isEqualTo(456L)
      assertThat(assessmentSeq).isEqualTo(123L)
      assertThat(nomisEventType).isEqualTo("S1_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `assessment changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<AssessmentUpdateEvent>(
      Xtag(
        eventType = "OFFENDER_ASSESSMENTS-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "456",
            "p_assessment_seq" to "123",
            "p_offender_id_display" to "A1234AA",
            "p_assessment_type" to "CSR",
            "p_evaluation_result_code" to "APP",
            "p_review_level_sup_type" to "STANDARD",
            "p_nomis_timestamp" to "20230509215740.443718000",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ASSESSMENT-UPDATED")
      assertThat(bookingId).isEqualTo(456L)
      assertThat(assessmentSeq).isEqualTo(123L)
      assertThat(assessmentType).isEqualTo("CSR")
      assertThat(evaluationResultCode).isEqualTo("APP")
      assertThat(reviewLevelSupType).isEqualTo("STANDARD")
      assertThat(offenderIdDisplay).isEqualTo("A1234AA")
      assertThat(nomisEventType).isEqualTo("OFFENDER_ASSESSMENTS-UPDATED")
      assertThat(eventDatetime).isEqualTo(LocalDateTime.parse("2023-05-09T21:57:40.443718"))
    }
  }

  @Test
  fun `other s1 result mapped correctly`() {
    val now = LocalDateTime.now()
    assertThat(
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "S1_RESULT",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_book_id" to "456",
            ),
          ),
        ),
      ),
    ).isNull()
  }

  @Test
  fun `alert updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<AlertOffenderEvent>(
      Xtag(
        eventType = "OFF_ALERT_UPDATE",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "123",
            "p_offender_book_id" to "456",
            "p_alert_seq" to "789",
            "p_alert_date" to "123",
            "p_alert_date" to "2019-02-14",
            "p_alert_time" to "10:12:23",
            "p_alert_type" to "some type",
            "p_alert_code" to "some code",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ALERT-UPDATED")
      assertThat(rootOffenderId).isEqualTo(123L)
      assertThat(bookingId).isEqualTo(456L)
      assertThat(alertSeq).isEqualTo(789L)
      assertThat(alertDateTime).isEqualTo(LocalDateTime.parse("2019-02-14T10:12:23"))
      assertThat(alertType).isEqualTo("some type")
      assertThat(alertCode).isEqualTo("some code")
      assertThat(nomisEventType).isEqualTo("OFF_ALERT_UPDATE")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `alert inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<AlertOffenderEvent>(
      Xtag(
        eventType = "OFF_ALERT_INSERT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "123",
            "p_offender_book_id" to "456",
            "p_alert_seq" to "789",
            "p_alert_type" to "some type",
            "p_alert_code" to "some code",
            "p_alert_date" to "2019-02-14",
            "p_alert_time" to "10:12:23",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ALERT-INSERTED")
      assertThat(rootOffenderId).isEqualTo(123L)
      assertThat(bookingId).isEqualTo(456L)
      assertThat(alertSeq).isEqualTo(789L)
      assertThat(alertDateTime).isEqualTo(LocalDateTime.parse("2019-02-14T10:12:23"))
      assertThat(alertType).isEqualTo("some type")
      assertThat(alertCode).isEqualTo("some code")
      assertThat(nomisEventType).isEqualTo("OFF_ALERT_INSERT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `alert deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<AlertOffenderEvent>(
      Xtag(
        eventType = "OFF_ALERT_DELETE",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "123",
            "p_offender_book_id" to "456",
            "p_alert_seq" to "789",
            "p_alert_date" to "2019-02-14",
            "p_alert_time" to "10:12:23",
            "p_alert_type" to "some type",
            "p_alert_code" to "some code",
            "p_expiry_date" to "2012-02-14",
            "p_expiry_time" to "11:12:23",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ALERT-DELETED")
      assertThat(rootOffenderId).isEqualTo(123L)
      assertThat(bookingId).isEqualTo(456L)
      assertThat(alertSeq).isEqualTo(789L)
      assertThat(alertDateTime).isEqualTo(LocalDateTime.parse("2019-02-14T10:12:23"))
      assertThat(alertType).isEqualTo("some type")
      assertThat(alertCode).isEqualTo("some code")
      assertThat(expiryDateTime).isEqualTo(LocalDateTime.parse("2012-02-14T11:12:23"))
      assertThat(nomisEventType).isEqualTo("OFF_ALERT_DELETE")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `incident inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-INSERTED")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-INSERTED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_table_name" to "incident_cases",
            "p_delete_flag" to "N",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-CHANGED-CASES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_table_name" to "incident_cases",
            "p_delete_flag" to "Y",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-DELETED-CASES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case parties updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_party_seq" to "123",
            "p_table_name" to "incident_case_parties",
            "p_delete_flag" to "N",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-CHANGED-PARTIES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentPartySeq).isEqualTo(123L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case parties deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_party_seq" to "123",
            "p_table_name" to "incident_case_parties",
            "p_delete_flag" to "Y",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-DELETED-PARTIES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentPartySeq).isEqualTo(123L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case responses updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_question_seq" to "789",
            "p_response_seq" to "345",
            "p_table_name" to "incident_case_responses",
            "p_delete_flag" to "N",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-CHANGED-RESPONSES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentQuestionSeq).isEqualTo(789L)
      assertThat(incidentResponseSeq).isEqualTo(345L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case responses deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_question_seq" to "789",
            "p_response_seq" to "345",
            "p_table_name" to "incident_case_responses",
            "p_delete_flag" to "Y",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-DELETED-RESPONSES")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentQuestionSeq).isEqualTo(789L)
      assertThat(incidentResponseSeq).isEqualTo(345L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case requirements updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_requirement_seq" to "456",
            "p_table_name" to "incident_case_requirements",
            "p_delete_flag" to "N",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-CHANGED-REQUIREMENTS")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentRequirementSeq).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `incident case requirements deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "INCIDENT-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_incident_case_id" to "234",
            "p_requirement_seq" to "456",
            "p_table_name" to "incident_case_requirements",
            "p_delete_flag" to "Y",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("INCIDENT-DELETED-REQUIREMENTS")
      assertThat(incidentCaseId).isEqualTo(234L)
      assertThat(incidentRequirementSeq).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("INCIDENT-UPDATED")
      assertThat(offenderIdDisplay).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `imprisonment status change mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_IMP_STAT_OASYS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
            "p_imprison_status_seq" to "456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("IMPRISONMENT_STATUS-CHANGED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(imprisonmentStatusSeq).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("OFF_IMP_STAT_OASYS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender profile detail inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_PROF_DETAIL_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PROFILE_DETAILS-INSERTED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_PROF_DETAIL_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender profile detail updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_PROF_DETAIL_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PROFILE_DETAILS-UPDATED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_PROF_DETAIL_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `hearing date changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "A2_CALLBACK",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_oic_hearing_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("HEARING_DATE-CHANGED")
      assertThat(oicHearingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("A2_CALLBACK")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `hearing result deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "A2_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_oic_hearing_id" to "234",
            "p_result_seq" to "123",
            "p_agency_incident_id" to "345",
            "p_charge_seq" to "456",
            "p_oic_offence_id" to "789",
            "p_plea_finding_code" to "pleas",
            "p_finding_code" to "finding code",
            "p_delete_flag" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("HEARING_RESULT-DELETED")
      assertThat(oicHearingId).isEqualTo(234L)
      assertThat(resultSeq).isEqualTo(123L)
      assertThat(agencyIncidentId).isEqualTo(345L)
      assertThat(chargeSeq).isEqualTo(456L)
      assertThat(oicOffenceId).isEqualTo(789L)
      assertThat(pleaFindingCode).isEqualTo("pleas")
      assertThat(findingCode).isEqualTo("finding code")
      assertThat(nomisEventType).isEqualTo("A2_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `hearing result changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "A2_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_oic_hearing_id" to "234",
            "p_result_seq" to "123",
            "p_agency_incident_id" to "345",
            "p_charge_seq" to "456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("HEARING_RESULT-CHANGED")
      assertThat(oicHearingId).isEqualTo(234L)
      assertThat(resultSeq).isEqualTo(123L)
      assertThat(agencyIncidentId).isEqualTo(345L)
      assertThat(chargeSeq).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("A2_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `phone inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "PHONES_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_owner_id" to "234",
            "p_owner_class" to "class",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PHONE-INSERTED")
      assertThat(ownerId).isEqualTo(234L)
      assertThat(ownerClass).isEqualTo("class")
      assertThat(nomisEventType).isEqualTo("PHONES_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `phone updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "PHONES_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_owner_id" to "234",
            "p_owner_class" to "class",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PHONE-UPDATED")
      assertThat(ownerId).isEqualTo(234L)
      assertThat(ownerClass).isEqualTo("class")
      assertThat(nomisEventType).isEqualTo("PHONES_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `phone deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "PHONES_DEL",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_owner_id" to "234",
            "p_owner_class" to "class",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PHONE-DELETED")
      assertThat(ownerId).isEqualTo(234L)
      assertThat(ownerClass).isEqualTo("class")
      assertThat(nomisEventType).isEqualTo("PHONES_DEL")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender employment inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EMPLOYMENTS_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMPLOYMENT-INSERTED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_EMPLOYMENTS_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender employment updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EMPLOYMENTS_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMPLOYMENT-UPDATED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_EMPLOYMENTS_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender employment deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_EMPLOYMENTS_DEL",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMPLOYMENT-DELETED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("OFF_EMPLOYMENTS_DEL")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `hdc condition changed mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "D5_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
            "p_sentence_seq" to "345",
            "p_condition_code" to "code",
            "p_offender_sent_calculation_id" to "456",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("HDC_CONDITION-CHANGED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(sentenceSeq).isEqualTo(345L)
      assertThat(conditionCode).isEqualTo("code")
      assertThat(offenderSentenceConditionId).isEqualTo(456L)
      assertThat(nomisEventType).isEqualTo("D5_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `hdc fine inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "D4_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
            "p_sentence_seq" to "345",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("HDC_FINE-INSERTED")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(sentenceSeq).isEqualTo(345L)
      assertThat(nomisEventType).isEqualTo("D4_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `person address inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "123",
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "PER",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PERSON_ADDRESS-INSERTED")
      assertThat(rootOffenderId).isEqualTo(123L)
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("PER")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender address inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_INS",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_root_offender_id" to "123",
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "OFF",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS-INSERTED")
      assertThat(rootOffenderId).isEqualTo(123L)
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("OFF")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_INS")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `person address updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "PER",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PERSON_ADDRESS-UPDATED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("PER")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `person address deletion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "PER",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PERSON_ADDRESS-DELETED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("PER")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender address updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "OFF",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS-UPDATED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("OFF")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender address deletion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "OFF",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS-DELETED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("OFF")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `address updated mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "something",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ADDRESS-UPDATED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("something")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `address deletion mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "ADDR_UPD",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_address_id" to "234",
            "p_owner_id" to "345",
            "p_owner_class" to "something",
            "p_address_end_date" to "2019-02-03",
            "p_primary_addr_flag" to "Y",
            "p_mail_addr_flag" to "N",
            "p_person_id" to "567",
            "p_address_deleted" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("ADDRESS-DELETED")
      assertThat(addressId).isEqualTo(234L)
      assertThat(ownerId).isEqualTo(345L)
      assertThat(ownerClass).isEqualTo("something")
      assertThat(addressEndDate).isEqualTo(LocalDate.parse("2019-02-03"))
      assertThat(primaryAddressFlag).isEqualTo("Y")
      assertThat(mailAddressFlag).isEqualTo("N")
      assertThat(personId).isEqualTo(567L)
      assertThat(nomisEventType).isEqualTo("ADDR_UPD")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender transfer out of LIDS mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "IEDT_OUT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "234",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_TRANSFER-OUT_OF_LIDS")
      assertThat(bookingId).isEqualTo(234L)
      assertThat(nomisEventType).isEqualTo("IEDT_OUT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender inserted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "234",
            "p_offender_id_display" to "A234BC",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER-INSERTED")
      assertThat(offenderId).isEqualTo(234L)
      assertThat(nomisEventType).isNull()
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
    }
  }

  @Test
  fun `offender deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER-DELETED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "234",
            "p_offender_id_display" to "A234BC",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER-DELETED")
      assertThat(offenderId).isEqualTo(234L)
      assertThat(nomisEventType).isNull()
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
    }
  }

  @Test
  fun `offender updated event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFFENDER-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "234",
            "p_offender_id_display" to "A234BC",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER-UPDATED")
      assertThat(offenderId).isEqualTo(234L)
      assertThat(nomisEventType).isNull()
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
    }
  }

  @Test
  fun `prisoner activity suspend event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<PrisonerActivityUpdateEvent>(
      Xtag(
        eventType = "PRISONER_ACTIVITY-UPDATE",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_agy_loc_id" to "LEI",
            "p_action" to "SUSPEND",
            "p_user" to "Some User",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PRISONER_ACTIVITY-UPDATE")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("PRISONER_ACTIVITY-UPDATE")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(prisonId).isEqualTo("LEI")
      assertThat(action).isEqualTo("SUSPEND")
      assertThat(user).isEqualTo("Some User")
    }
  }

  @Test
  fun `prisoner appointment suspend event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<PrisonerAppointmentUpdateEvent>(
      Xtag(
        eventType = "PRISONER_APPOINTMENT-UPDATE",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_agy_loc_id" to "LEI",
            "p_action" to "SUSPEND",
            "p_user" to "Some User",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("PRISONER_APPOINTMENT-UPDATE")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("PRISONER_APPOINTMENT-UPDATE")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(prisonId).isEqualTo("LEI")
      assertThat(action).isEqualTo("SUSPEND")
      assertThat(user).isEqualTo("Some User")
    }
  }

  @Test
  fun `offender non association updated mapped correctly`() {
    val now = LocalDateTime.now()
    assertThat(
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "OFF_NON_ASSOC-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id" to "234",
            ),
          ),
        ),
      ),
    ).isNull()
  }

  @Test
  fun `offender charges update event mapped correctly`() {
    offenderChargeEventUpdatedMappedCorrectly()
  }

  @Test
  fun `offender charges insert event mapped correctly`() {
    offenderChargeEventMappedCorrectly("OFFENDER_CHARGES-INSERTED")
  }

  @Test
  fun `offender charges delete event mapped correctly`() {
    offenderChargeEventMappedCorrectly("OFFENDER_CHARGES-DELETED")
  }

  private fun offenderChargeEventMappedCorrectly(eventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderChargeEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_offender_charge_id" to "23456",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(eventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(chargeId).isEqualTo(23456)
      assertThat(offenceCodeChange).isFalse()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  private fun offenderChargeEventUpdatedMappedCorrectly() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderChargeEvent>(
      Xtag(
        eventType = "OFFENDER_CHARGES-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_offender_charge_id" to "23456",
            "p_audit_module_name" to "DPS_AUDIT",
            "p_has_offence_code_changed" to "Y",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CHARGES-UPDATED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("OFFENDER_CHARGES-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(chargeId).isEqualTo(23456)
      assertThat(offenceCodeChange).isTrue()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `court case update event mapped correctly`() {
    courtCaseEventMappedCorrectly("OFFENDER_CASES-UPDATED")
  }

  @Test
  fun `court case insert event mapped correctly`() {
    courtCaseEventMappedCorrectly("OFFENDER_CASES-INSERTED")
  }

  @Test
  fun `court case deleted event mapped correctly`() {
    courtCaseEventMappedCorrectly("OFFENDER_CASES-DELETED")
  }

  @Test
  fun `court case linked event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtCaseLinkingEvent>(
      Xtag(
        eventType = "OFFENDER_CASES-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_case_status" to "I",
            "p_status_update_staff_id" to "485887",
            "p_lids_case_number" to "6",
            "p_combined_case_id" to "23456",
            "p_begin_date" to "20250501",
            "p_status_update_date" to "20250501",
            "p_case_seq" to "7",
            "p_agy_loc_id" to "ABDSUM",
            "p_case_type" to "Y",
            "p_status_update_reason" to "LINKED",
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_case_id" to "1604141",
            "p_audit_module_name" to "OCULCASE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CASES-LINKED")
      assertThat(nomisEventType).isEqualTo("OFFENDER_CASES-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isEqualTo(1604141)
      assertThat(combinedCaseId).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("OCULCASE")
    }
  }

  @Test
  fun `court case unlinked event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtCaseLinkingEvent>(
      Xtag(
        eventType = "OFFENDER_CASES-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_case_status" to "A",
            "p_status_update_staff_id" to "485887",
            "p_lids_case_number" to "6",
            "p_previous_combined_case_id" to "23456",
            "p_begin_date" to "20250501",
            "p_status_update_date" to "20250501",
            "p_case_seq" to "7",
            "p_agy_loc_id" to "ABDSUM",
            "p_case_type" to "Y",
            "p_status_update_reason" to "A",
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_case_id" to "1604141",
            "p_audit_module_name" to "OCULCASE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CASES-UNLINKED")
      assertThat(nomisEventType).isEqualTo("OFFENDER_CASES-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isEqualTo(1604141)
      assertThat(combinedCaseId).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("OCULCASE")
    }
  }

  @Test
  fun `court case updated with both combined and previous combined ids is mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtCaseEvent>(
      Xtag(
        eventType = "OFFENDER_CASES-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_case_status" to "A",
            "p_status_update_staff_id" to "485887",
            "p_lids_case_number" to "6",
            "p_combined_case_id" to "23456",
            "p_previous_combined_case_id" to "23456",
            "p_begin_date" to "20250501",
            "p_status_update_date" to "20250501",
            "p_case_seq" to "7",
            "p_agy_loc_id" to "ABDSUM",
            "p_case_type" to "Y",
            "p_status_update_reason" to "A",
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_case_id" to "1604141",
            "p_audit_module_name" to "OCULCASE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CASES-UPDATED")
      assertThat(nomisEventType).isEqualTo("OFFENDER_CASES-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isEqualTo(1604141)
      assertThat(auditModuleName).isEqualTo("OCULCASE")
    }
  }

  private fun courtCaseEventMappedCorrectly(eventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<CourtCaseEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_case_id" to "23456",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(eventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `court event charge inserted event mapped correctly`() {
    courtEventChargeEventMappedCorrectly("COURT_EVENT_CHARGES-INSERTED")
  }

  @Test
  fun `court event charge deleted event mapped correctly`() {
    courtEventChargeEventMappedCorrectly("COURT_EVENT_CHARGES-DELETED")
  }

  @Test
  fun `court event charge updated event mapped correctly`() {
    courtEventChargeEventMappedCorrectly("COURT_EVENT_CHARGES-UPDATED")
  }

  @Test
  fun `court event charge linked event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtEventChargeLinkingEvent>(
      Xtag(
        eventType = "LINK_CASE_TXNS-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_charge_id" to "23456",
            "p_event_id" to "65432",
            "p_case_id" to "1604142",
            "p_combined_case_id" to "1604141",
            "p_audit_module_name" to "OCULCASE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("COURT_EVENT_CHARGES-LINKED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("LINK_CASE_TXNS-INSERTED")
      assertThat(chargeId).isEqualTo(23456)
      assertThat(eventId).isEqualTo(65432)
      assertThat(combinedCaseId).isEqualTo(1604141)
      assertThat(caseId).isEqualTo(1604142)
      assertThat(auditModuleName).isEqualTo("OCULCASE")
    }
  }

  @Test
  fun `court event charge unlinked event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtEventChargeLinkingEvent>(
      Xtag(
        eventType = "LINK_CASE_TXNS-DELETED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_charge_id" to "23456",
            "p_event_id" to "65432",
            "p_case_id" to "1604142",
            "p_combined_case_id" to "1604141",
            "p_audit_module_name" to "OCULCASE",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("COURT_EVENT_CHARGES-UNLINKED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("LINK_CASE_TXNS-DELETED")
      assertThat(chargeId).isEqualTo(23456)
      assertThat(eventId).isEqualTo(65432)
      assertThat(combinedCaseId).isEqualTo(1604141)
      assertThat(auditModuleName).isEqualTo("OCULCASE")
    }
  }

  private fun courtEventChargeEventMappedCorrectly(eventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<CourtEventChargeEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_offender_charge_id" to "23456",
            "p_event_id" to "65432",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(eventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(chargeId).isEqualTo(23456)
      assertThat(eventId).isEqualTo(65432)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `court appearance update event mapped correctly`() {
    courtAppearanceEventMappedCorrectly(eventName = "COURT_EVENT-UPDATED", translatedEventName = "COURT_EVENTS-UPDATED")
    courtAppearanceEventMappedCorrectlyForNullCase(eventName = "COURT_EVENT-UPDATED", translatedEventName = "COURT_EVENTS-UPDATED")
  }

  @Test
  fun `court appearance inserted event mapped correctly`() {
    courtAppearanceEventMappedCorrectly(
      eventName = "COURT_EVENT-INSERTED",
      translatedEventName = "COURT_EVENTS-INSERTED",
    )
  }

  @Test
  fun `breach court appearance inserted event mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<CourtAppearanceEvent>(
      Xtag(
        eventType = "COURT_EVENT-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_event_id" to "65432",
            "p_case_id" to "55555",
            "p_court_event_type" to "BREACH",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("COURT_EVENTS-INSERTED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("COURT_EVENT-INSERTED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventId).isEqualTo(65432)
      assertThat(caseId).isEqualTo(55555)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
      assertThat(isBreachHearing).isEqualTo(true)
    }
  }

  @Test
  fun `court appearance deleted event mapped correctly`() {
    courtAppearanceEventMappedCorrectly(eventName = "COURT_EVENT-DELETED", translatedEventName = "COURT_EVENTS-DELETED")
  }

  private fun courtAppearanceEventMappedCorrectly(eventName: String, translatedEventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<CourtAppearanceEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_event_id" to "65432",
            "p_case_id" to "55555",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(translatedEventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventId).isEqualTo(65432)
      assertThat(caseId).isEqualTo(55555)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
      assertThat(isBreachHearing).isEqualTo(false)
    }
  }

  @Suppress("SameParameterValue")
  private fun courtAppearanceEventMappedCorrectlyForNullCase(eventName: String, translatedEventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<CourtAppearanceEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_event_id" to "65432",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(translatedEventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(eventId).isEqualTo(65432)
      assertThat(caseId).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `order update event mapped correctly`() {
    orderEventMappedCorrectly("ORDERS-UPDATED")
  }

  @Test
  fun `order inserted event mapped correctly`() {
    orderEventMappedCorrectly("ORDERS-INSERTED")
  }

  @Test
  fun `order deleted event mapped correctly`() {
    orderEventMappedCorrectly("ORDERS-DELETED")
  }

  private fun orderEventMappedCorrectly(eventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<OrderEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_order_id" to "23456",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(eventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(orderId).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `offender sentence update event mapped correctly`() {
    offenderSentenceEventMappedCorrectly("OFF_SENT-UPDATED", "OFFENDER_SENTENCES-UPDATED")
  }

  @Test
  fun `offender sentence inserted event mapped correctly`() {
    offenderSentenceEventMappedCorrectly("OFF_SENT-INSERTED", "OFFENDER_SENTENCES-INSERTED")
  }

  @Test
  fun `offender sentence inserted event mapped correctly without nullables`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderSentenceEvent>(
      Xtag(
        eventType = "OFF_SENT-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_sentence_seq" to "2",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_SENTENCES-INSERTED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("OFF_SENT-INSERTED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isNull()
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(sentenceLevel).isNull()
      assertThat(sentenceCategory).isNull()
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `offender sentence deleted event mapped correctly`() {
    offenderSentenceEventMappedCorrectly("OFF_SENT-DELETED", "OFFENDER_SENTENCES-DELETED")
  }

  private fun offenderSentenceEventMappedCorrectly(xtagEventName: String, dpsEventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderSentenceEvent>(
      Xtag(
        eventType = xtagEventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_sentence_seq" to "2",
            "p_case_id" to "123",
            "p_sentence_level" to "IND",
            "p_sentence_category" to "2020",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(dpsEventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(xtagEventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(caseId).isEqualTo(123)
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(sentenceLevel).isEqualTo("IND")
      assertThat(sentenceCategory).isEqualTo("2020")
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `offender sentence charge update event mapped correctly`() {
    offenderSentenceChargeEventMappedCorrectly("OFF_SENT_CHRG-UPDATED", "OFFENDER_SENTENCE_CHARGES-UPDATED")
  }

  @Test
  fun `offender sentence charge inserted event mapped correctly`() {
    offenderSentenceChargeEventMappedCorrectly("OFF_SENT_CHRG-INSERTED", "OFFENDER_SENTENCE_CHARGES-INSERTED")
  }

  @Test
  fun `offender sentence charge deleted event mapped correctly`() {
    offenderSentenceChargeEventMappedCorrectly("OFF_SENT_CHRG-DELETED", "OFFENDER_SENTENCE_CHARGES-DELETED")
  }

  private fun offenderSentenceChargeEventMappedCorrectly(xtagEventName: String, dpsEventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderSentenceChargeEvent>(
      Xtag(
        eventType = xtagEventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_sentence_seq" to "2",
            "p_offender_charge_id" to "3",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(dpsEventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(xtagEventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(chargeId).isEqualTo(3)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `offender sentence term update event mapped correctly`() {
    offenderSentenceTermEventMappedCorrectly("OFF_SENT_TERM-UPDATED", "OFFENDER_SENTENCE_TERMS-UPDATED")
  }

  @Test
  fun `offender sentence term inserted event mapped correctly`() {
    offenderSentenceTermEventMappedCorrectly("OFF_SENT_TERM-INSERTED", "OFFENDER_SENTENCE_TERMS-INSERTED")
  }

  @Test
  fun `offender sentence term deleted event mapped correctly`() {
    offenderSentenceTermEventMappedCorrectly("OFF_SENT_TERM-DELETED", "OFFENDER_SENTENCE_TERMS-DELETED")
  }

  private fun offenderSentenceTermEventMappedCorrectly(xtagEventName: String, dpsEventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderSentenceTermEvent>(
      Xtag(
        eventType = xtagEventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_sentence_seq" to "2",
            "p_term_seq" to "3",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(dpsEventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(xtagEventName)
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(sentenceSeq).isEqualTo(2)
      assertThat(termSequence).isEqualTo(3)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Test
  fun `OFFENDER_FIXED_TERM_RECALLS-INSERTED mapped`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderFixedTermRecallEvent>(
      Xtag(
        eventType = "OFFENDER_FIXED_TERM_RECALLS-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_comment_text" to "comment",
            "p_staff_id" to "485887",
            "p_recall_length" to "28",
            "p_return_to_custody_date" to "2025-05-16 00:00",
            "p_audit_module_name" to "OIUFTRDA",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-INSERTED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-INSERTED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(auditModuleName).isEqualTo("OIUFTRDA")
    }
  }

  @Test
  fun `OFFENDER_FIXED_TERM_RECALLS-UPDATED mapped`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderFixedTermRecallEvent>(
      Xtag(
        eventType = "OFFENDER_FIXED_TERM_RECALLS-UPDATED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_comment_text" to "comment",
            "p_staff_id" to "485887",
            "p_recall_length" to "28",
            "p_return_to_custody_date" to "2025-05-16 00:00",
            "p_audit_module_name" to "OIUFTRDA",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-UPDATED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-UPDATED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(auditModuleName).isEqualTo("OIUFTRDA")
    }
  }

  @Test
  fun `OFFENDER_FIXED_TERM_RECALLS-DELETED mapped`() {
    val now = LocalDateTime.now()
    withCallTransformer<OffenderFixedTermRecallEvent>(
      Xtag(
        eventType = "OFFENDER_FIXED_TERM_RECALLS-DELETED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id_display" to "A234BC",
            "p_offender_book_id" to "12345",
            "p_comment_text" to "comment",
            "p_staff_id" to "485887",
            "p_recall_length" to "28",
            "p_return_to_custody_date" to "2025-05-16 00:00",
            "p_audit_module_name" to "OIUFTRDA",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-DELETED")
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo("OFFENDER_FIXED_TERM_RECALLS-DELETED")
      assertThat(offenderIdDisplay).isEqualTo("A234BC")
      assertThat(bookingId).isEqualTo(12345)
      assertThat(auditModuleName).isEqualTo("OIUFTRDA")
    }
  }

  @Nested
  inner class AgencyInternalLocationEvents {
    @Test
    fun `Agency Internal Location Update Event mapped correctly`() {
      val now = LocalDateTime.now()

      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "AGENCY_INTERNAL_LOCATIONS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(agencyInternalLocationMap),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_INTERNAL_LOCATIONS-UPDATED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(internalLocationId).isEqualTo(12345)
        assertThat(prisonId).isEqualTo("LEI")
        assertThat(description).isEqualTo("LEI-E-3-135")
        assertThat(oldDescription).isEqualTo("LEI-E-3-246")
        assertThat(auditModuleName).isEqualTo("module")
        assertThat(recordDeleted).isFalse()
      }
    }

    @Test
    fun `Agency Internal Location Delete Event mapped correctly`() {
      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "AGENCY_INTERNAL_LOCATIONS-UPDATED",
          nomisTimestamp = LocalDateTime.now(),
          content = XtagContent(agencyInternalLocationMap + mapOf("p_delete_flag" to "Y")),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGENCY_INTERNAL_LOCATIONS-UPDATED")
        assertThat(internalLocationId).isEqualTo(12345)
        assertThat(recordDeleted).isTrue()
      }
    }

    @Test
    fun `Agency Internal Location Usage Event mapped correctly`() {
      val now = LocalDateTime.now()

      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "INT_LOC_USAGE_LOCATIONS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(agencyInternalLocationUsageMap),
        ),
      ) {
        assertThat(eventType).isEqualTo("INT_LOC_USAGE_LOCATIONS-UPDATED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(internalLocationId).isEqualTo(34567)
        assertThat(usageLocationId).isEqualTo(45678)
        assertThat(auditModuleName).isEqualTo("module")
        assertThat(recordDeleted).isFalse()
      }
    }

    @Test
    fun `Agency Internal Location Usage Deleted Event mapped correctly`() {
      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "INT_LOC_USAGE_LOCATIONS-UPDATED",
          nomisTimestamp = LocalDateTime.now(),
          content = XtagContent(agencyInternalLocationUsageMap + mapOf("p_delete_flag" to "Y")),
        ),
      ) {
        assertThat(eventType).isEqualTo("INT_LOC_USAGE_LOCATIONS-UPDATED")
        assertThat(usageLocationId).isEqualTo(45678)
        assertThat(recordDeleted).isTrue()
      }
    }

    @Test
    fun `Agency Internal Location Profile Event mapped correctly`() {
      val now = LocalDateTime.now()

      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "AGY_INT_LOC_PROFILES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(agencyInternalLocationProfileMap),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGY_INT_LOC_PROFILES-UPDATED")
        assertThat(eventDatetime).isEqualTo(now)
        assertThat(internalLocationId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("module")
        assertThat(recordDeleted).isFalse()
      }
    }

    @Test
    fun `Agency Internal Location Profile Deleted Event mapped correctly`() {
      withCallTransformer<AgencyInternalLocationUpdatedEvent>(
        Xtag(
          eventType = "AGY_INT_LOC_PROFILES-UPDATED",
          nomisTimestamp = LocalDateTime.now(),
          content = XtagContent(agencyInternalLocationProfileMap + mapOf("p_delete_flag" to "Y")),
        ),
      ) {
        assertThat(eventType).isEqualTo("AGY_INT_LOC_PROFILES-UPDATED")
        assertThat(internalLocationId).isEqualTo(34567)
        assertThat(auditModuleName).isEqualTo("module")
        assertThat(recordDeleted).isTrue()
      }
    }

    private val agencyInternalLocationMap = mapOf(
      "p_internal_location_id" to "12345",
      "p_description" to "LEI-E-3-135",
      "p_old_description" to "LEI-E-3-246",
      "p_internal_location_code" to "135",
      "p_agy_loc_id" to "LEI",
      "p_internal_location_type" to "CELL",
      "p_security_level_code" to "dummy",
      "p_capacity" to "1",
      "p_create_user_id" to "user",
      "p_parent_internal_location_id" to "23456",
      "p_active_flag" to "Y",
      "p_list_seq" to "135",
      "p_cna_no" to "1",
      "p_certified_flag" to "Y",
      "p_deactivate_date" to "2024-01-01",
      "p_reactivate_date" to "2024-01-01",
      "p_deactivate_reason_code" to "dummy",
      "p_comment_text" to "comment",
      "p_user_desc" to "description",
      "p_aca_cap_rating" to "dummy",
      "p_unit_type" to "NA",
      "p_operation_capacity" to "1",
      "p_tracking_flag" to "Y",
      "p_audit_module_name" to "module",
    )

    private val agencyInternalLocationUsageMap = mapOf(
      "p_internal_location_usage_id" to "12345",
      "p_internal_location_id" to "34567",
      "p_capacity" to "1",
      "p_usage_location_type" to "135",
      "p_list_seq" to "135",
      "p_usage_location_id" to "45678",
      "p_parent_usage_location_id" to "23456",
      "p_audit_module_name" to "module",
    )

    private val agencyInternalLocationProfileMap = mapOf(
      "p_internal_location_id" to "34567",
      "p_int_loc_profile_type" to "TYPE",
      "p_int_loc_profile_code" to "CODE",
      "p_audit_module_name" to "module",
    )
  }

  @Test
  fun `Offender phone number inserted mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHONE-INSERTED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender phone number updated mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-UPDATED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHONE-UPDATED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender phone number deleted mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-DELETED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHONE-DELETED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender address phone number inserted mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "ADDR",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS_PHONE-INSERTED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(addressId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender address phone number updated mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-UPDATED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "ADDR",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS_PHONE-UPDATED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(addressId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender address phone number deleted mapped correctly`() {
    withCallTransformer<OffenderPhoneNumberEvent>(
      Xtag(
        eventType = "PHONES-DELETED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_phone_id" to "1326483",
            "p_phone_type" to "HOME",
            "p_audit_module_name" to "OCDADDRE",
            "p_phone_no" to "0987654321",
            "p_owner_id" to "2465630",
            "p_owner_class" to "ADDR",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_ADDRESS_PHONE-DELETED")
      assertThat(phoneId).isEqualTo(1326483)
      assertThat(phoneType).isEqualTo("HOME")
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(addressId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender email address inserted mapped correctly`() {
    withCallTransformer<OffenderEmailEvent>(
      Xtag(
        eventType = "INTERNET_ADDRESSES-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_internet_address_class" to "EMAIL",
            "p_internet_address_id" to "140204",
            "p_audit_module_name" to "OCDADDRE",
            "p_internet_address" to "davide@davidee.com",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMAIL-INSERTED")
      assertThat(internetAddressClass).isEqualTo("EMAIL")
      assertThat(internetAddressId).isEqualTo(140204)
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender email address updated mapped correctly`() {
    withCallTransformer<OffenderEmailEvent>(
      Xtag(
        eventType = "INTERNET_ADDRESSES-UPDATED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_internet_address_class" to "EMAIL",
            "p_internet_address_id" to "140204",
            "p_audit_module_name" to "OCDADDRE",
            "p_internet_address" to "davide@davidee.com",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMAIL-UPDATED")
      assertThat(internetAddressClass).isEqualTo("EMAIL")
      assertThat(internetAddressId).isEqualTo(140204)
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `Offender email address deleted mapped correctly`() {
    withCallTransformer<OffenderEmailEvent>(
      Xtag(
        eventType = "INTERNET_ADDRESSES-DELETED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_internet_address_class" to "EMAIL",
            "p_internet_address_id" to "140204",
            "p_audit_module_name" to "OCDADDRE",
            "p_internet_address" to "davide@davidee.com",
            "p_owner_id" to "2465630",
            "p_owner_class" to "OFF",
            "p_offender_id_display" to "G4560UH",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_EMAIL-DELETED")
      assertThat(internetAddressClass).isEqualTo("EMAIL")
      assertThat(internetAddressId).isEqualTo(140204)
      assertThat(auditModuleName).isEqualTo("OCDADDRE")
      assertThat(offenderId).isEqualTo(2465630)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `OFFENDER_CONTACT-INSERTED mapped correctly`() {
    withCallTransformer<OffenderContactEvent>(
      Xtag(
        eventType = "OFFENDER_CONTACT-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "7550868",
            "p_person_id" to "4729911",
            "p_audit_module_name" to "OIDVIRES",
            "p_offender_id_display" to "G4560UH",
            "p_offender_book_id" to "1215922",
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_relationship_type" to "BRO",
            "p_active_flag" to "N",
            "p_contact_type" to "S",
            "p_next_of_kin_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CONTACT-INSERTED")
      assertThat(approvedVisitor).isFalse()
      assertThat(personId).isEqualTo(4729911)
      assertThat(contactRootOffenderId).isNull()
      assertThat(contactId).isEqualTo(7550868)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(bookingId).isEqualTo(1215922)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `OFFENDER_CONTACT-INSERTED with null person Id mapped correctly`() {
    withCallTransformer<OffenderContactEvent>(
      Xtag(
        eventType = "OFFENDER_CONTACT-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "7550868",
            "p_contact_root_offender_id" to "4729911",
            "p_audit_module_name" to "OIDVIRES",
            "p_offender_id_display" to "G4560UH",
            "p_offender_book_id" to "1215922",
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_relationship_type" to "BRO",
            "p_active_flag" to "N",
            "p_contact_type" to "S",
            "p_next_of_kin_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CONTACT-INSERTED")
      assertThat(approvedVisitor).isFalse()
      assertThat(contactRootOffenderId).isEqualTo(4729911)
      assertThat(personId).isNull()
      assertThat(contactId).isEqualTo(7550868)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(bookingId).isEqualTo(1215922)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `OFFENDER_CONTACT-INSERTED with null audit module does not break`() {
    withCallTransformer<OffenderContactEvent>(
      Xtag(
        eventType = "OFFENDER_CONTACT-INSERTED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "7550868",
            "p_contact_root_offender_id" to "4729911",
            "p_offender_id_display" to "G4560UH",
            "p_offender_book_id" to "1215922",
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_relationship_type" to "BRO",
            "p_active_flag" to "N",
            "p_contact_type" to "S",
            "p_next_of_kin_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CONTACT-INSERTED")
      assertThat(auditModuleName).isEqualTo("UNKNOWN")
    }
  }

  @Test
  fun `OFFENDER_CONTACT-UPDATED mapped correctly`() {
    withCallTransformer<OffenderContactEvent>(
      Xtag(
        eventType = "OFFENDER_CONTACT-UPDATED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "7550868",
            "p_person_id" to "4729911",
            "p_audit_module_name" to "OIDVIRES",
            "p_approved_visitor_flag" to "Y",
            "p_offender_id_display" to "G4560UH",
            "p_offender_book_id" to "1215922",
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_relationship_type" to "BRO",
            "p_active_flag" to "N",
            "p_contact_type" to "S",
            "p_next_of_kin_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CONTACT-UPDATED")
      assertThat(approvedVisitor).isTrue()
      assertThat(personId).isEqualTo(4729911)
      assertThat(contactId).isEqualTo(7550868)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(bookingId).isEqualTo(1215922)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `OFFENDER_CONTACT-DELETED mapped correctly`() {
    withCallTransformer<OffenderContactEvent>(
      Xtag(
        eventType = "OFFENDER_CONTACT-DELETED",
        nomisTimestamp = LocalDateTime.now(),
        content = XtagContent(
          mapOf(
            "p_offender_contact_person_id" to "7550868",
            "p_person_id" to "4729911",
            "p_audit_module_name" to "OIDVIRES",
            "p_approved_visitor_flag" to "Y",
            "p_offender_id_display" to "G4560UH",
            "p_offender_book_id" to "1215922",
            "p_emergency_contact_flag" to "N",
            "p_can_be_contacted_flag" to "N",
            "p_aware_of_charges_flag" to "N",
            "p_relationship_type" to "BRO",
            "p_active_flag" to "N",
            "p_contact_type" to "S",
            "p_next_of_kin_flag" to "N",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_CONTACT-DELETED")
      assertThat(approvedVisitor).isTrue()
      assertThat(personId).isEqualTo(4729911)
      assertThat(contactId).isEqualTo(7550868)
      assertThat(auditModuleName).isEqualTo("OIDVIRES")
      assertThat(bookingId).isEqualTo(1215922)
      assertThat(offenderIdDisplay).isEqualTo("G4560UH")
    }
  }

  @Test
  fun `case identifier update event mapped correctly`() {
    caseIdentifierEventMappedCorrectly("OFFENDER_CASE_IDENTIFIERS-UPDATED")
  }

  @Test
  fun `case identifier insert event mapped correctly`() {
    caseIdentifierEventMappedCorrectly("OFFENDER_CASE_IDENTIFIERS-INSERTED")
  }

  @Test
  fun `case identifier deleted event mapped correctly`() {
    caseIdentifierEventMappedCorrectly("OFFENDER_CASE_IDENTIFIERS-DELETED")
  }

  private fun caseIdentifierEventMappedCorrectly(eventName: String) {
    val now = LocalDateTime.now()
    withCallTransformer<CaseIdentifierEvent>(
      Xtag(
        eventType = eventName,
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_identifier_no" to "GF123",
            "p_case_id" to "23456",
            "p_identifier_type" to "CASE/INFO#",
            "p_audit_module_name" to "DPS_AUDIT",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo(eventName)
      assertThat(offenderId).isNull()
      assertThat(nomisEventType).isEqualTo(eventName)
      assertThat(identifierNo).isEqualTo("GF123")
      assertThat(identifierType).isEqualTo("CASE/INFO#")
      assertThat(caseId).isEqualTo(23456)
      assertThat(auditModuleName).isEqualTo("DPS_AUDIT")
    }
  }

  @Nested
  inner class CSIPEvents {

    @Nested
    inner class CSIPReportEvents {
      @Test
      fun `CSIP Report inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReportOffenderEvent>(
          Xtag(
            eventType = "CSIP_REPORTS-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REPORTS-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REPORTS-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Report updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReportOffenderEvent>(
          Xtag(
            eventType = "CSIP_REPORTS-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REPORTS-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REPORTS-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Report deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReportOffenderEvent>(
          Xtag(
            eventType = "CSIP_REPORTS-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REPORTS-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REPORTS-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }

    @Nested
    inner class CSIPPlanEvents {

      @Test
      fun `CSIP Plan inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPPlanOffenderEvent>(
          Xtag(
            eventType = "CSIP_PLANS-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_plan_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_PLANS-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_PLANS-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipPlanId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Plan updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPPlanOffenderEvent>(
          Xtag(
            eventType = "CSIP_PLANS-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_plan_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_PLANS-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_PLANS-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipPlanId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Plan deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPPlanOffenderEvent>(
          Xtag(
            eventType = "CSIP_PLANS-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_plan_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_PLANS-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_PLANS-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipPlanId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }

    @Nested
    inner class CSIPReviewEvents {
      @Test
      fun `CSIP Review inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_REVIEWS-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REVIEWS-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REVIEWS-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Review updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_REVIEWS-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REVIEWS-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REVIEWS-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Review deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPReviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_REVIEWS-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_REVIEWS-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_REVIEWS-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }

    @Nested
    inner class CSIPAttendeeEvents {
      @Test
      fun `CSIP Attendee inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPAttendeeOffenderEvent>(
          Xtag(
            eventType = "CSIP_ATTENDEES-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "123",
                "p_attendee_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_ATTENDEES-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_ATTENDEES-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(123L)
          assertThat(csipAttendeeId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Attendee updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPAttendeeOffenderEvent>(
          Xtag(
            eventType = "CSIP_ATTENDEES-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "123",
                "p_attendee_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_ATTENDEES-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_ATTENDEES-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(123L)
          assertThat(csipAttendeeId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Attendee deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPAttendeeOffenderEvent>(
          Xtag(
            eventType = "CSIP_ATTENDEES-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_review_id" to "123",
                "p_attendee_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_ATTENDEES-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_ATTENDEES-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipReviewId).isEqualTo(123L)
          assertThat(csipAttendeeId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }

    @Nested
    inner class CSIPFactorsEvents {
      @Test
      fun `CSIP Factor inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPFactorOffenderEvent>(
          Xtag(
            eventType = "CSIP_FACTORS-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_factor_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_FACTORS-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_FACTORS-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipFactorId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Factor updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPFactorOffenderEvent>(
          Xtag(
            eventType = "CSIP_FACTORS-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_factor_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_FACTORS-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_FACTORS-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipFactorId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Factor deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPFactorOffenderEvent>(
          Xtag(
            eventType = "CSIP_FACTORS-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_factor_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_FACTORS-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_FACTORS-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipFactorId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }

    @Nested
    inner class CSIPInterviewEvents {
      @Test
      fun `CSIP Interview inserted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPInterviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_INTVW-INSERTED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_intvw_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_INTVW-INSERTED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_INTVW-INSERTED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipInterviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Interview updated mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPInterviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_INTVW-UPDATED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_intvw_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_INTVW-UPDATED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_INTVW-UPDATED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipInterviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }

      @Test
      fun `CSIP Interview deleted mapped correctly`() {
        val now = LocalDateTime.now()
        withCallTransformer<CSIPInterviewOffenderEvent>(
          Xtag(
            eventType = "CSIP_INTVW-DELETED",
            nomisTimestamp = now,
            content = XtagContent(
              mapOf(
                "p_offender_book_id" to "234",
                "p_offender_id_display" to "AB1234C",
                "p_csip_id" to "54321",
                "p_csip_intvw_id" to "34567",
                "p_root_offender_id" to "12345",
                "p_audit_module_name" to "csip",
              ),
            ),
          ),
        ) {
          assertThat(eventType).isEqualTo("CSIP_INTVW-DELETED")
          assertThat(bookingId).isEqualTo(234L)
          assertThat(nomisEventType).isEqualTo("CSIP_INTVW-DELETED")
          assertThat(rootOffenderId).isEqualTo(12345L)
          assertThat(offenderIdDisplay).isEqualTo("AB1234C")
          assertThat(csipReportId).isEqualTo(54321L)
          assertThat(csipInterviewId).isEqualTo(34567L)
          assertThat(auditModuleName).isEqualTo("csip")
        }
      }
    }
  }

  @Nested
  inner class IWPDocumentEvents {

    @Test
    fun `IWP Document inserted mapped correctly`() {
      val now = LocalDateTime.now()
      withCallTransformer<IWPDocumentOffenderEvent>(
        Xtag(
          eventType = "IWP_DOCUMENTS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_book_id" to "1214881",
              "p_offender_id_display" to "AB1234C",
              "p_document_id" to "126677",
              "p_document_name" to "CSIPA2_HMP.doc",
              "p_template_id" to "1011",
              "p_template_name" to "CSIPA2_HMP",
              "p_audit_module_name" to "OIUIWPGN",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("IWP_DOCUMENTS-INSERTED")
        assertThat(bookingId).isEqualTo(1214881L)
        assertThat(nomisEventType).isEqualTo("IWP_DOCUMENTS-INSERTED")
        assertThat(offenderIdDisplay).isEqualTo("AB1234C")
        assertThat(documentId).isEqualTo(126677L)
        assertThat(documentName).isEqualTo("CSIPA2_HMP.doc")
        assertThat(templateId).isEqualTo(1011L)
        assertThat(templateName).isEqualTo("CSIPA2_HMP")
        assertThat(auditModuleName).isEqualTo("OIUIWPGN")
      }
    }

    @Test
    fun `IWP Document updated mapped correctly`() {
      val now = LocalDateTime.now()
      withCallTransformer<IWPDocumentOffenderEvent>(
        Xtag(
          eventType = "IWP_DOCUMENTS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_book_id" to "1214881",
              "p_offender_id_display" to "AB1234C",
              "p_document_id" to "126677",
              "p_document_name" to "CSIPA2_HMP.doc",
              "p_template_id" to "1011",
              "p_template_name" to "CSIPA2_HMP",
              "p_audit_module_name" to "OIUIWPGN",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("IWP_DOCUMENTS-UPDATED")
        assertThat(bookingId).isEqualTo(1214881L)
        assertThat(nomisEventType).isEqualTo("IWP_DOCUMENTS-UPDATED")
        assertThat(offenderIdDisplay).isEqualTo("AB1234C")
        assertThat(documentId).isEqualTo(126677L)
        assertThat(documentName).isEqualTo("CSIPA2_HMP.doc")
        assertThat(templateId).isEqualTo(1011L)
        assertThat(templateName).isEqualTo("CSIPA2_HMP")
        assertThat(auditModuleName).isEqualTo("OIUIWPGN")
      }
    }

    @Test
    fun `IWP Document deleted mapped correctly`() {
      val now = LocalDateTime.now()
      withCallTransformer<IWPDocumentOffenderEvent>(
        Xtag(
          eventType = "IWP_DOCUMENTS-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_book_id" to "1214881",
              "p_offender_id_display" to "AB1234C",
              "p_document_id" to "126677",
              "p_document_name" to "CSIPA2_HMP.doc",
              "p_template_id" to "1011",
              "p_template_name" to "CSIPA2_HMP",
              "p_audit_module_name" to "OIUIWPGN",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("IWP_DOCUMENTS-DELETED")
        assertThat(bookingId).isEqualTo(1214881L)
        assertThat(nomisEventType).isEqualTo("IWP_DOCUMENTS-DELETED")
        assertThat(offenderIdDisplay).isEqualTo("AB1234C")
        assertThat(documentId).isEqualTo(126677L)
        assertThat(documentName).isEqualTo("CSIPA2_HMP.doc")
        assertThat(templateId).isEqualTo(1011L)
        assertThat(templateName).isEqualTo("CSIPA2_HMP")
        assertThat(auditModuleName).isEqualTo("OIUIWPGN")
      }
    }
  }

  @Nested
  inner class PersonEvents {

    @Test
    fun `PERSON-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEvent>(
        Xtag(
          eventType = "PERSON-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_audit_module_name" to "OCUCNPER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("PERSON-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCUCNPER")
      }
    }

    @Test
    fun `PERSON-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEvent>(
        Xtag(
          eventType = "PERSON-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_audit_module_name" to "OSIPSEAR",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("PERSON-UPDATED")
        assertThat(auditModuleName).isEqualTo("OSIPSEAR")
      }
    }

    @Test
    fun `PERSON-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEvent>(
        Xtag(
          eventType = "PERSON-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_audit_module_name" to "OIUDPERS",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("PERSON-DELETED")
        assertThat(auditModuleName).isEqualTo("OIUDPERS")
      }
    }
  }

  @Nested
  inner class PersonAddressEvents {

    @Test
    fun `ADDRESSES_PERSON-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_PERSON-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_PERSON-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_PERSON-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_PERSON-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_PERSON-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_PERSON-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_PERSON-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_PERSON-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_PERSON-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "DataGrip",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_PERSON-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_PERSON-DELETED")
        assertThat(auditModuleName).isEqualTo("DataGrip")
      }
    }
  }

  @Nested
  inner class PersonPhoneEvents {

    @Test
    fun `PHONES_PERSON-INSERTED (global) is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_PERSON-UPDATED (global)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_PERSON-DELETED (global)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-DELETED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_PERSON-INSERTED (address) is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDCCONT",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDCCONT")
        assertThat(isAddress).isTrue()
      }
    }

    @Test
    fun `PHONES_PERSON-UPDATED (address)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDCCONT",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDCCONT")
        assertThat(isAddress).isTrue()
      }
    }

    @Test
    fun `PHONES_PERSON-DELETED (address)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonPhoneEvent>(
        Xtag(
          eventType = "PHONES_PERSON-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OCDCCONT",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_PERSON-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_PERSON-DELETED")
        assertThat(auditModuleName).isEqualTo("OCDCCONT")
        assertThat(isAddress).isTrue()
      }
    }
  }

  @Nested
  inner class PersonInternetAddressEvents {

    @Test
    fun `INTERNET_ADDRESSES_PERSON-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_PERSON-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_PERSON-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_PERSON-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
      }
    }

    @Test
    fun `INTERNET_ADDRESSES_PERSON-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_PERSON-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_PERSON-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_PERSON-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
      }
    }

    @Test
    fun `INTERNET_ADDRESSES_PERSON-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_PERSON-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_audit_module_name" to "OCDGNUMB",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_PERSON-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_PERSON-DELETED")
        assertThat(auditModuleName).isEqualTo("OCDGNUMB")
      }
    }
  }

  @Nested
  inner class PersonEmploymentEvents {

    @Test
    fun `PERSON_EMPLOYMENTS-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEmploymentEvent>(
        Xtag(
          eventType = "PERSON_EMPLOYMENTS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_employment_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_EMPLOYMENTS-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(employmentSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_EMPLOYMENTS-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }

    @Test
    fun `PERSON_EMPLOYMENTS-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEmploymentEvent>(
        Xtag(
          eventType = "PERSON_EMPLOYMENTS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_employment_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_EMPLOYMENTS-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(employmentSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_EMPLOYMENTS-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }

    @Test
    fun `PERSON_EMPLOYMENTS-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonEmploymentEvent>(
        Xtag(
          eventType = "PERSON_EMPLOYMENTS-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_employment_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_EMPLOYMENTS-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(employmentSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_EMPLOYMENTS-DELETED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }
  }

  @Nested
  inner class PersonIdentifierEvents {

    @Test
    fun `PERSON_IDENTIFIERS-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonIdentifierEvent>(
        Xtag(
          eventType = "PERSON_IDENTIFIERS-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_id_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IDENTIFIERS-INSERTED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(identifierSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_IDENTIFIERS-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }

    @Test
    fun `PERSON_IDENTIFIERS-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonIdentifierEvent>(
        Xtag(
          eventType = "PERSON_IDENTIFIERS-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_id_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IDENTIFIERS-UPDATED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(identifierSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_IDENTIFIERS-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }

    @Test
    fun `PERSON_IDENTIFIERS-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonIdentifierEvent>(
        Xtag(
          eventType = "PERSON_IDENTIFIERS-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_person_id" to "4730074",
              "p_id_seq" to "0",
              "p_audit_module_name" to "OCDPERSO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IDENTIFIERS-DELETED")
        assertThat(personId).isEqualTo(4730074L)
        assertThat(identifierSequence).isEqualTo(0L)
        assertThat(nomisEventType).isEqualTo("PERSON_IDENTIFIERS-DELETED")
        assertThat(auditModuleName).isEqualTo("OCDPERSO")
      }
    }
  }

  @Nested
  inner class CorporateEvents {

    @Test
    fun `CORPORATE-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateEvent>(
        Xtag(
          eventType = "CORPORATE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_audit_module_name" to "OUMAGENC",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("CORPORATE-INSERTED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }

    @Test
    fun `CORPORATE-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateEvent>(
        Xtag(
          eventType = "CORPORATE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_audit_module_name" to "OUMAGENC",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("CORPORATE-UPDATED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }

    @Test
    fun `CORPORATE-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateEvent>(
        Xtag(
          eventType = "CORPORATE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_audit_module_name" to "OUMAGENC",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(nomisEventType).isEqualTo("CORPORATE-DELETED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }
  }

  @Nested
  inner class CorporatePhoneEvents {

    @Test
    fun `PHONES_CORPORATE-INSERTED (global) is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-INSERTED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_CORPORATE-UPDATED (global)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-UPDATED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_CORPORATE-DELETED (global)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-DELETED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isFalse
      }
    }

    @Test
    fun `PHONES_CORPORATE-INSERTED (address) is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-INSERTED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isTrue()
      }
    }

    @Test
    fun `PHONES_CORPORATE-UPDATED (address)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-UPDATED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isTrue()
      }
    }

    @Test
    fun `PHONES_CORPORATE-DELETED (address)  is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporatePhoneEvent>(
        Xtag(
          eventType = "PHONES_CORPORATE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_phone_id" to "5623860",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "ADDR",
              "p_address_id" to "7654",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PHONES_CORPORATE-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(phoneId).isEqualTo(5623860L)
        assertThat(addressId).isEqualTo(7654L)
        assertThat(nomisEventType).isEqualTo("PHONES_CORPORATE-DELETED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
        assertThat(isAddress).isTrue()
      }
    }
  }

  @Nested
  inner class AgencyAddressEvents {

    @Test
    fun `ADDRESSES_AGENCY-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_AGENCY-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_code" to "LEEDMC",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "AGY",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_AGENCY-INSERTED")
        assertThat(agencyCode).isEqualTo("LEEDMC")
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_AGENCY-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_AGENCY-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_AGENCY-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_code" to "LEEDMC",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "AGY",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_AGENCY-UPDATED")
        assertThat(agencyCode).isEqualTo("LEEDMC")
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_AGENCY-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_AGENCY-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<AgencyAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_AGENCY-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_agency_code" to "LEEDMC",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "DataGrip",
              "p_owner_class" to "AGY",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_AGENCY-DELETED")
        assertThat(agencyCode).isEqualTo("LEEDMC")
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_AGENCY-DELETED")
        assertThat(auditModuleName).isEqualTo("DataGrip")
      }
    }
  }

  @Nested
  inner class CorporateAddressEvents {

    @Test
    fun `ADDRESSES_CORPORATE-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_CORPORATE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_CORPORATE-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_CORPORATE-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_CORPORATE-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_CORPORATE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_CORPORATE-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_CORPORATE-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_CORPORATE-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_CORPORATE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "DataGrip",
              "p_owner_class" to "CORP",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_CORPORATE-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_CORPORATE-DELETED")
        assertThat(auditModuleName).isEqualTo("DataGrip")
      }
    }
  }

  @Nested
  inner class OffenderAddressEvents {

    @Test
    fun `ADDRESSES_OFFENDER-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_OFFENDER-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id" to "432733",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "OFF",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_OFFENDER-INSERTED")
        assertThat(offenderId).isEqualTo(432733)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_OFFENDER-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_OFFENDER-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_OFFENDER-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id" to "432733",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "OCDOAPOP",
              "p_owner_class" to "OFF",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_OFFENDER-UPDATED")
        assertThat(offenderId).isEqualTo(432733)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_OFFENDER-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCDOAPOP")
      }
    }

    @Test
    fun `ADDRESSES_OFFENDER-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderAddressEvent>(
        Xtag(
          eventType = "ADDRESSES_OFFENDER-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id" to "432733",
              "p_address_id" to "5623860",
              "p_audit_module_name" to "DataGrip",
              "p_owner_class" to "OFF",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("ADDRESSES_OFFENDER-DELETED")
        assertThat(offenderId).isEqualTo(432733)
        assertThat(addressId).isEqualTo(5623860L)
        assertThat(nomisEventType).isEqualTo("ADDRESSES_OFFENDER-DELETED")
        assertThat(auditModuleName).isEqualTo("DataGrip")
      }
    }
  }

  @Nested
  inner class CorporateInternetAddressEvents {

    @Test
    fun `INTERNET_ADDRESSES_CORPORATE-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_CORPORATE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_internet_address_class" to "EMAIL",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(internetAddressClass).isEqualTo("EMAIL")
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-INSERTED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }

    @Test
    fun `INTERNET_ADDRESSES_CORPORATE-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_CORPORATE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_internet_address_class" to "EMAIL",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(internetAddressClass).isEqualTo("EMAIL")
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-UPDATED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }

    @Test
    fun `INTERNET_ADDRESSES_CORPORATE-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateInternetAddressEvent>(
        Xtag(
          eventType = "INTERNET_ADDRESSES_CORPORATE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_internet_address_id" to "5623860",
              "p_internet_address_class" to "WEB",
              "p_audit_module_name" to "OUMAGENC",
              "p_owner_class" to "PER",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(internetAddressId).isEqualTo(5623860L)
        assertThat(internetAddressClass).isEqualTo("WEB")
        assertThat(nomisEventType).isEqualTo("INTERNET_ADDRESSES_CORPORATE-DELETED")
        assertThat(auditModuleName).isEqualTo("OUMAGENC")
      }
    }
  }

  @Nested
  inner class CorporateTypeEvents {

    @Test
    fun `CORPORATE_TYPES-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateTypeEvent>(
        Xtag(
          eventType = "CORPORATE_TYPES-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_corporate_type" to "BSKILLS",
              "p_audit_module_name" to "OCUCORPT",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE_TYPES-INSERTED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(corporateType).isEqualTo("BSKILLS")
        assertThat(nomisEventType).isEqualTo("CORPORATE_TYPES-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCUCORPT")
      }
    }

    @Test
    fun `CORPORATE_TYPES-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateTypeEvent>(
        Xtag(
          eventType = "CORPORATE_TYPES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_corporate_type" to "BSKILLS",
              "p_audit_module_name" to "OCUCORPT",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE_TYPES-UPDATED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(corporateType).isEqualTo("BSKILLS")
        assertThat(nomisEventType).isEqualTo("CORPORATE_TYPES-UPDATED")
        assertThat(auditModuleName).isEqualTo("OCUCORPT")
      }
    }

    @Test
    fun `CORPORATE_TYPES-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<CorporateTypeEvent>(
        Xtag(
          eventType = "CORPORATE_TYPES-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_corporate_id" to "4730074",
              "p_corporate_type" to "BSKILLS",
              "p_audit_module_name" to "OCUCORPT",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("CORPORATE_TYPES-DELETED")
        assertThat(corporateId).isEqualTo(4730074L)
        assertThat(corporateType).isEqualTo("BSKILLS")
        assertThat(nomisEventType).isEqualTo("CORPORATE_TYPES-DELETED")
        assertThat(auditModuleName).isEqualTo("OCUCORPT")
      }
    }
  }

  @Nested
  inner class OffenderImageEvents {
    @Test
    fun `OFFENDER_IMAGES-UPDATED for identifying marks with full size image added publishes created event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_IDM",
              "p_active_flag_changed" to "N",
              "p_image_view_type" to "TAT",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_MARKS_IMAGE-CREATED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-UPDATED for identifying marks with active flag changed publishes updated event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_IDM",
              "p_active_flag_changed" to "Y",
              "p_image_view_type" to "TAT",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_MARKS_IMAGE-UPDATED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-DELETED for identifying marks publishes deleted event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OIUOIMAG",
              "p_image_object_type" to "OFF_IDM",
              "p_active_flag_changed" to "Y",
              "p_image_view_type" to "TAT",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_MARKS_IMAGE-DELETED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OIUOIMAG")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-UPDATED for identifying marks record created before image added is ignored`() {
      val now = LocalDateTime.now()
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_IDM",
              "p_active_flag_changed" to "N",
              "p_image_view_type" to "TAT",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ).also {
        assertThat(it).isNull()
      }
    }

    @Test
    fun `OFFENDER_IMAGES-UPDATED for offender facial image with full size image added publishes created event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_BKG",
              "p_active_flag_changed" to "N",
              "p_image_view_type" to "FACE",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_IMAGE-CREATED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-UPDATED for offender facial image with active flag changed publishes updated event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_BKG",
              "p_active_flag_changed" to "Y",
              "p_image_view_type" to "FACE",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_IMAGE-UPDATED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-DELETED for offender facial image publishes deleted event`() {
      val now = LocalDateTime.now()
      withCallTransformer<OffenderImageEvent>(
        Xtag(
          eventType = "OFFENDER_IMAGES-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_BKG",
              "p_active_flag_changed" to "Y",
              "p_image_view_type" to "FACE",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_IMAGE-DELETED")
        assertThat(bookingId).isEqualTo(1108078)
        assertThat(offenderImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `OFFENDER_IMAGES-UPDATED for offender facial image record created before image added is ignored`() {
      val now = LocalDateTime.now()
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "OFFENDER_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "OFF_BKG",
              "p_active_flag_changed" to "N",
              "p_image_view_type" to "FACE",
              "p_offender_book_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_offender_image_id" to "1996215",
            ),
          ),
        ),
      ).also {
        assertThat(it).isNull()
      }
    }
  }

  @Nested
  inner class PersonImageEvents {

    @Test
    fun `TAG_IMAGES-UPDATED for person with full size image added publishes created event`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonImageEvent>(
        Xtag(
          eventType = "TAG_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "PERSON",
              "p_active_flag_changed" to "N",
              // personId
              "p_image_object_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_tag_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IMAGE-CREATED")
        assertThat(personId).isEqualTo(1108078)
        assertThat(personImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `TAG_IMAGES-UPDATED for person with active flag changed publishes updated event`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonImageEvent>(
        Xtag(
          eventType = "TAG_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "PERSON",
              "p_active_flag_changed" to "Y",
              // personId
              "p_image_object_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_tag_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IMAGE-UPDATED")
        assertThat(personId).isEqualTo(1108078)
        assertThat(personImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `TAG_IMAGES-DELETED for person publishes deleted event`() {
      val now = LocalDateTime.now()
      withCallTransformer<PersonImageEvent>(
        Xtag(
          eventType = "TAG_IMAGES-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "PERSON",
              "p_active_flag_changed" to "Y",
              // personId
              "p_image_object_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_tag_image_id" to "1996215",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("PERSON_IMAGE-DELETED")
        assertThat(personId).isEqualTo(1108078)
        assertThat(personImageId).isEqualTo(1996215)
        assertThat(auditModuleName).isEqualTo("OCUIMAGE")
      }
    }

    @Test
    fun `TAG_IMAGES-UPDATED for person record created before image added is ignored`() {
      val now = LocalDateTime.now()
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "TAG_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "N",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "PERSON",
              "p_active_flag_changed" to "N",
              // personId
              "p_image_object_id" to "1108078",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_tag_image_id" to "1996215",
            ),
          ),
        ),
      ).also {
        assertThat(it).isNull()
      }
    }

    @Test
    fun `TAG_IMAGES-UPDATED for staff image is ignored`() {
      val now = LocalDateTime.now()
      offenderEventsTransformer.offenderEventOf(
        Xtag(
          eventType = "TAG_IMAGES-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_full_size_image_changed" to "Y",
              "p_thumbnail_image_changed" to "Y",
              "p_audit_module_name" to "OCUIMAGE",
              "p_image_object_type" to "STAFF",
              "p_active_flag_changed" to "N",
              "p_image_object_id" to "99999",
              "p_nomis_timestamp" to "20250103091727.250845000",
              "p_tag_image_id" to "1996215",
            ),
          ),
        ),
      ).also {
        assertThat(it).isNull()
      }
    }
  }

  @Nested
  inner class HealthEvents {
    @Test
    fun `OFF_HEALTH_PROBLEMS-INSERTED is mapped`() {
      withCallTransformer<HealthEvent>(
        Xtag(
          eventType = "OFF_HEALTH_PROBLEMS-INSERTED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_start_date" to "2025-05-29 13:14",
              "p_end_date" to "2025-05-29 15:16",
              "p_caseload_type" to "INST",
              "p_audit_module_name" to "OCDHEALT",
              "p_description" to "test",
              "p_offender_book_id" to "1117525",
              "p_offender_health_problem_id" to "1388109",
              "p_problem_status" to "ON",
              "p_problem_code" to "BSC4.5",
              "p_problem_type" to "BSCAN",
              "p_nomis_timestamp" to "20250529134835.553626000",
              "p_offender_id_display" to "G4133UO",
            ),
          ),
        ),
      ) {
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(eventType).isEqualTo("OFF_HEALTH_PROBLEMS-INSERTED")
        assertThat(offenderHealthProblemId).isEqualTo(1388109L)
        assertThat(problemType).isEqualTo("BSCAN")
        assertThat(problemCode).isEqualTo("BSC4.5")
        assertThat(startDate).isEqualTo(LocalDateTime.parse("2025-05-29T13:14:00"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2025-05-29T15:16:00"))
        assertThat(caseloadType).isEqualTo("INST")
        assertThat(description).isEqualTo("test")
        assertThat(problemStatus).isEqualTo("ON")
        assertThat(auditModuleName).isEqualTo("OCDHEALT")
        assertThat(nomisEventType).isEqualTo("OFF_HEALTH_PROBLEMS-INSERTED")
      }
    }

    @Test
    fun `OFF_HEALTH_PROBLEMS-UPDATED is mapped`() {
      withCallTransformer<HealthEvent>(
        Xtag(
          eventType = "OFF_HEALTH_PROBLEMS-UPDATED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_start_date" to "2025-05-29 13:14",
              "p_end_date" to "2025-05-29 15:16",
              "p_caseload_type" to "INST",
              "p_audit_module_name" to "OCDHEALT",
              "p_description" to "test",
              "p_offender_book_id" to "1117525",
              "p_offender_health_problem_id" to "1388109",
              "p_problem_status" to "ON",
              "p_problem_code" to "BSC4.5",
              "p_problem_type" to "BSCAN",
              "p_nomis_timestamp" to "20250529134835.553626000",
              "p_offender_id_display" to "G4133UO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFF_HEALTH_PROBLEMS-UPDATED")
        assertThat(offenderHealthProblemId).isEqualTo(1388109L)
        assertThat(problemType).isEqualTo("BSCAN")
        assertThat(problemCode).isEqualTo("BSC4.5")
        assertThat(startDate).isEqualTo(LocalDateTime.parse("2025-05-29T13:14:00"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2025-05-29T15:16:00"))
        assertThat(caseloadType).isEqualTo("INST")
        assertThat(description).isEqualTo("test")
        assertThat(problemStatus).isEqualTo("ON")
        assertThat(auditModuleName).isEqualTo("OCDHEALT")
        assertThat(nomisEventType).isEqualTo("OFF_HEALTH_PROBLEMS-UPDATED")
      }
    }

    @Test
    fun `OFF_HEALTH_PROBLEMS-DELETED is mapped`() {
      withCallTransformer<HealthEvent>(
        Xtag(
          eventType = "OFF_HEALTH_PROBLEMS-DELETED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_start_date" to "2025-05-29 13:14",
              "p_end_date" to "2025-05-29 15:16",
              "p_caseload_type" to "INST",
              "p_audit_module_name" to "OCDHEALT",
              "p_description" to "test",
              "p_offender_book_id" to "1117525",
              "p_offender_health_problem_id" to "1388109",
              "p_problem_status" to "ON",
              "p_problem_code" to "BSC4.5",
              "p_problem_type" to "BSCAN",
              "p_nomis_timestamp" to "20250529134835.553626000",
              "p_offender_id_display" to "G4133UO",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFF_HEALTH_PROBLEMS-DELETED")
        assertThat(offenderHealthProblemId).isEqualTo(1388109L)
        assertThat(problemType).isEqualTo("BSCAN")
        assertThat(problemCode).isEqualTo("BSC4.5")
        assertThat(startDate).isEqualTo(LocalDateTime.parse("2025-05-29T13:14:00"))
        assertThat(endDate).isEqualTo(LocalDateTime.parse("2025-05-29T15:16:00"))
        assertThat(caseloadType).isEqualTo("INST")
        assertThat(description).isEqualTo("test")
        assertThat(problemStatus).isEqualTo("ON")
        assertThat(auditModuleName).isEqualTo("OCDHEALT")
        assertThat(nomisEventType).isEqualTo("OFF_HEALTH_PROBLEMS-DELETED")
      }
    }
  }

  @Nested
  inner class LanguageEvents {
    @Test
    fun `OFFENDER_LANGUAGES-INSERTED is mapped`() {
      withCallTransformer<LanguageEvent>(
        Xtag(
          eventType = "OFFENDER_LANGUAGES-INSERTED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_prefered_speak_flag" to "N",
              "p_language_code" to "BEN",
              "p_write_skill" to "Y",
              "p_audit_module_name" to "OCDLANGS",
              "p_read_skill" to "Y",
              "p_speak_skill" to "Y",
              "p_offender_book_id" to "1117525",
              "p_prefered_write_flag" to "N",
              "p_language_type" to "SEC",
              "p_interpreter_requested_flag" to "N",
              "p_nomis_timestamp" to "20250529140515.415786000",
              "p_offender_id_display" to "G4133UO",
              "p_comment_text" to "comment",
              "p_numeracy_skill" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_LANGUAGES-INSERTED")
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(languageType).isEqualTo("SEC")
        assertThat(languageCode).isEqualTo("BEN")
        assertThat(readSkill).isEqualTo("Y")
        assertThat(speakSkill).isEqualTo("Y")
        assertThat(writeSkill).isEqualTo("Y")
        assertThat(commentText).isEqualTo("comment")
        assertThat(numeracySkill).isEqualTo("N")
        assertThat(preferedWriteFlag).isEqualTo("N")
        assertThat(preferedSpeakFlag).isEqualTo("N")
        assertThat(interpreterRequestedFlag).isEqualTo("N")
        assertThat(auditModuleName).isEqualTo("OCDLANGS")
        assertThat(nomisEventType).isEqualTo("OFFENDER_LANGUAGES-INSERTED")
      }
    }

    @Test
    fun `OFFENDER_LANGUAGES-UPDATED is mapped`() {
      withCallTransformer<LanguageEvent>(
        Xtag(
          eventType = "OFFENDER_LANGUAGES-UPDATED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_prefered_speak_flag" to "N",
              "p_language_code" to "BEN",
              "p_write_skill" to "Y",
              "p_audit_module_name" to "OCDLANGS",
              "p_read_skill" to "Y",
              "p_speak_skill" to "Y",
              "p_offender_book_id" to "1117525",
              "p_prefered_write_flag" to "N",
              "p_language_type" to "SEC",
              "p_interpreter_requested_flag" to "N",
              "p_nomis_timestamp" to "20250529140515.415786000",
              "p_offender_id_display" to "G4133UO",
              "p_comment_text" to "comment",
              "p_numeracy_skill" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_LANGUAGES-UPDATED")
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(languageType).isEqualTo("SEC")
        assertThat(languageCode).isEqualTo("BEN")
        assertThat(readSkill).isEqualTo("Y")
        assertThat(speakSkill).isEqualTo("Y")
        assertThat(writeSkill).isEqualTo("Y")
        assertThat(commentText).isEqualTo("comment")
        assertThat(numeracySkill).isEqualTo("N")
        assertThat(preferedWriteFlag).isEqualTo("N")
        assertThat(preferedSpeakFlag).isEqualTo("N")
        assertThat(interpreterRequestedFlag).isEqualTo("N")
        assertThat(nomisEventType).isEqualTo("OFFENDER_LANGUAGES-UPDATED")
      }
    }

    @Test
    fun `OFFENDER_LANGUAGES-DELETED is mapped`() {
      withCallTransformer<LanguageEvent>(
        Xtag(
          eventType = "OFFENDER_LANGUAGES-DELETED",
          nomisTimestamp = fixedEventTime,
          content = XtagContent(
            mapOf(
              "p_prefered_speak_flag" to "N",
              "p_language_code" to "BEN",
              "p_write_skill" to "Y",
              "p_audit_module_name" to "OCDLANGS",
              "p_read_skill" to "Y",
              "p_speak_skill" to "Y",
              "p_offender_book_id" to "1117525",
              "p_prefered_write_flag" to "N",
              "p_language_type" to "SEC",
              "p_interpreter_requested_flag" to "N",
              "p_nomis_timestamp" to "20250529140515.415786000",
              "p_offender_id_display" to "G4133UO",
              "p_comment_text" to "comment",
              "p_numeracy_skill" to "N",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_LANGUAGES-DELETED")
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(languageType).isEqualTo("SEC")
        assertThat(languageCode).isEqualTo("BEN")
        assertThat(readSkill).isEqualTo("Y")
        assertThat(speakSkill).isEqualTo("Y")
        assertThat(writeSkill).isEqualTo("Y")
        assertThat(commentText).isEqualTo("comment")
        assertThat(numeracySkill).isEqualTo("N")
        assertThat(preferedWriteFlag).isEqualTo("N")
        assertThat(preferedSpeakFlag).isEqualTo("N")
        assertThat(interpreterRequestedFlag).isEqualTo("N")
        assertThat(nomisEventType).isEqualTo("OFFENDER_LANGUAGES-DELETED")
      }
    }
  }

  @Nested
  inner class MovementEvents {
    @Test
    fun `MOVEMENT_APPLICATION-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_from_date" to "2025-07-14 00:00",
              "p_application_time" to "2025-07-14 00:00",
              "p_to_date" to "2025-07-15 00:00",
              "p_return_time" to "2025-07-15 09:00",
              "p_tap_abs_subtype" to "RDR",
              "p_tap_abs_type" to "RR",
              "p_application_date" to "2025-07-14 00:00",
              "p_event_class" to "EXT_MOV",
              "p_event_type" to "TAP",
              "p_offender_id_display" to "G2610GR",
              "p_transport_code" to "POL",
              "p_application_status" to "PEN",
              "p_application_type" to "SINGLE",
              "p_agy_loc_id" to "RSI",
              "p_escort_code" to "Z",
              "p_release_time" to "2025-07-14 09:00",
              "p_audit_module_name" to "OIDTAAPP",
              "p_offender_book_id" to "1007000",
              "p_event_sub_type" to "FB",
              "p_offender_movement_app_id" to "2883190",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION-INSERTED")
        assertThat(auditModuleName).isEqualTo("OIDTAAPP")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(bookingId).isEqualTo(1007000)
        assertThat(offenderIdDisplay).isEqualTo("G2610GR")
      }
    }

    @Test
    fun `MOVEMENT_APPLICATION-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id_display" to "G2610GR",
              "p_offender_book_id" to "1007000",
              "p_offender_movement_app_id" to "2883190",
              "p_audit_module_name" to "DPS_SYNCHRONISATION",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION-UPDATED")
        assertThat(auditModuleName).isEqualTo("DPS_SYNCHRONISATION")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(bookingId).isEqualTo(1007000)
        assertThat(offenderIdDisplay).isEqualTo("G2610GR")
      }
    }

    @Test
    fun `MOVEMENT_APPLICATION-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_offender_id_display" to "G2610GR",
              "p_offender_book_id" to "1007000",
              "p_offender_movement_app_id" to "2883190",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION-DELETED")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(bookingId).isEqualTo(1007000)
        assertThat(offenderIdDisplay).isEqualTo("G2610GR")
        // check missing audit module is handled
        assertThat(auditModuleName).isEqualTo("UNKNOWN_MODULE")
      }
    }

    @Test
    fun `MOVEMENT_APPLICATION_MULTI-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationMultiEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION_MULTI-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_from_date" to "2025-07-14 00:00",
              "p_to_date" to "2025-07-15 00:00",
              "p_return_time" to "2025-07-15 08:00",
              "p_release_time" to "2025-07-14 17:00",
              "p_tap_abs_subtype" to "RDR",
              "p_tap_abs_type" to "RR",
              "p_event_sub_type" to "RO",
              "p_audit_module_name" to "OCMOMSCH",
              "p_off_movement_apps_multi_id" to "245",
              "p_offender_movement_app_id" to "2883190",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION_MULTI-INSERTED")
        assertThat(auditModuleName).isEqualTo("OCMOMSCH")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(movementApplicationMultiId).isEqualTo(245)
      }
    }

    @Test
    fun `MOVEMENT_APPLICATION_MULTI-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationMultiEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION_MULTI-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_audit_module_name" to "DPS_SYNCHRONISATION",
              "p_off_movement_apps_multi_id" to "245",
              "p_offender_movement_app_id" to "2883190",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION_MULTI-UPDATED")
        assertThat(auditModuleName).isEqualTo("DPS_SYNCHRONISATION")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(movementApplicationMultiId).isEqualTo(245)
      }
    }

    @Test
    fun `MOVEMENT_APPLICATION_MULTI-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<MovementApplicationMultiEvent>(
        Xtag(
          eventType = "MOVEMENT_APPLICATION_MULTI-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_off_movement_apps_multi_id" to "245",
              "p_offender_movement_app_id" to "2883190",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("MOVEMENT_APPLICATION_MULTI-DELETED")
        assertThat(movementApplicationId).isEqualTo(2883190)
        assertThat(movementApplicationMultiId).isEqualTo(245)
        // check missing audit module is handled
        assertThat(auditModuleName).isEqualTo("UNKNOWN_MODULE")
      }
    }

    @Test
    fun `SCHEDULED_EXT_MOVE-INSERTED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<ScheduledExternalMovementEvent>(
        Xtag(
          eventType = "SCHEDULED_EXT_MOVE-INSERTED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "579105221",
              "p_audit_module_name" to "OIUSCINQ",
              "p_offender_book_id" to "1125205",
              "p_event_type" to "TAP",
              "p_nomis_timestamp" to "20250815090952.912443000",
              "p_offender_id_display" to "A1234BC",
              "p_direction_code" to "OUT",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("SCHEDULED_EXT_MOVE-INSERTED")
        assertThat(eventId).isEqualTo(579105221)
        assertThat(auditModuleName).isEqualTo("OIUSCINQ")
        assertThat(bookingId).isEqualTo(1125205)
        assertThat(eventMovementType).isEqualTo("TAP")
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(directionCode).isEqualTo("OUT")
      }
    }

    @Test
    fun `SCHEDULED_EXT_MOVE-UPDATED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<ScheduledExternalMovementEvent>(
        Xtag(
          eventType = "SCHEDULED_EXT_MOVE-UPDATED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "579105221",
              "p_audit_module_name" to "OIUSCINQ",
              "p_offender_book_id" to "1125205",
              "p_event_type" to "TAP",
              "p_nomis_timestamp" to "20250815090952.912443000",
              "p_offender_id_display" to "A1234BC",
              "p_direction_code" to "IN",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("SCHEDULED_EXT_MOVE-UPDATED")
        assertThat(eventId).isEqualTo(579105221)
        assertThat(auditModuleName).isEqualTo("OIUSCINQ")
        assertThat(bookingId).isEqualTo(1125205)
        assertThat(eventMovementType).isEqualTo("TAP")
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(directionCode).isEqualTo("IN")
      }
    }

    @Test
    fun `SCHEDULED_EXT_MOVE-DELETED is mapped`() {
      val now = LocalDateTime.now()
      withCallTransformer<ScheduledExternalMovementEvent>(
        Xtag(
          eventType = "SCHEDULED_EXT_MOVE-DELETED",
          nomisTimestamp = now,
          content = XtagContent(
            mapOf(
              "p_event_id" to "579105221",
              "p_audit_module_name" to "OIUSCINQ",
              "p_offender_book_id" to "1125205",
              "p_event_type" to "TAP",
              "p_nomis_timestamp" to "20250815090952.912443000",
              "p_offender_id_display" to "A1234BC",
              "p_direction_code" to "OUT",
            ),
          ),
        ),
      ) {
        assertThat(eventType).isEqualTo("SCHEDULED_EXT_MOVE-DELETED")
        assertThat(eventId).isEqualTo(579105221)
        assertThat(auditModuleName).isEqualTo("OIUSCINQ")
        assertThat(bookingId).isEqualTo(1125205)
        assertThat(eventMovementType).isEqualTo("TAP")
        assertThat(offenderIdDisplay).isEqualTo("A1234BC")
        assertThat(directionCode).isEqualTo("OUT")
      }
    }
  }

  @Nested
  inner class MilitaryEvents {
    val xtagContent = XtagContent(
      mapOf(
        "p_military_seq" to "4",
        "p_audit_module_name" to "OCDIMILI",
        "p_offender_book_id" to "1117525",
        "p_nomis_timestamp" to "20250529140515.415786000",
        "p_offender_id_display" to "G4133UO",
      ),
    )

    @Test
    fun `OFF_MILITARY_REC-INSERTED is mapped`() {
      withCallTransformer<MilitaryEvent>(
        Xtag(
          eventType = "OFF_MILITARY_REC-INSERTED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFF_MILITARY_REC-INSERTED")
        assertThat(eventDatetime).isEqualTo(fixedEventTime)
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(militarySequence).isEqualTo(4)
        assertThat(auditModuleName).isEqualTo("OCDIMILI")
        assertThat(nomisEventType).isEqualTo("OFF_MILITARY_REC-INSERTED")
      }
    }

    @Test
    fun `OFF_MILITARY_REC-UPDATED is mapped`() {
      withCallTransformer<MilitaryEvent>(
        Xtag(
          eventType = "OFF_MILITARY_REC-UPDATED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFF_MILITARY_REC-UPDATED")
        assertThat(nomisEventType).isEqualTo("OFF_MILITARY_REC-UPDATED")
      }
    }

    @Test
    fun `OFF_MILITARY_REC-DELETED is mapped`() {
      withCallTransformer<MilitaryEvent>(
        Xtag(
          eventType = "OFF_MILITARY_REC-DELETED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFF_MILITARY_REC-DELETED")
        assertThat(nomisEventType).isEqualTo("OFF_MILITARY_REC-DELETED")
      }
    }
  }

  @Nested
  inner class FinePaymentEvents {
    val xtagContent = XtagContent(
      mapOf(
        "p_payment_seq" to "4",
        "p_payment_date" to "2025-10-20 13:45:56",
        "p_payment_amount" to "456.79",
        "p_comment_text" to "comment",
        "p_weekend_days" to "2",
        "p_staff_id" to "4567890000",
        "p_payment_status" to "STATUS",
        "p_audit_module_name" to "OCCIFINE",
        "p_offender_book_id" to "1117525",
        "p_nomis_timestamp" to "20250529140515.415786000",
        "p_offender_id_display" to "G4133UO",
      ),
    )

    @Test
    fun `OFFENDER_FINE_PAYMENTS-INSERTED is mapped`() {
      withCallTransformer<FinePaymentEvent>(
        Xtag(
          eventType = "OFFENDER_FINE_PAYMENTS-INSERTED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_FINE_PAYMENTS-INSERTED")
        assertThat(eventDatetime).isEqualTo(fixedEventTime)
        assertThat(offenderIdDisplay).isEqualTo("G4133UO")
        assertThat(bookingId).isEqualTo(1117525L)
        assertThat(paymentSequence).isEqualTo(4)
        assertThat(paymentDate).isEqualTo(LocalDateTime.parse("2025-10-20T13:45:56"))
        assertThat(paymentAmount).isEqualTo(BigDecimal.valueOf(456.79))
        assertThat(comment).isEqualTo("comment")
        assertThat(weekEndDays).isEqualTo(2)
        assertThat(staffId).isEqualTo(4567890000L)
        assertThat(paymentStatus).isEqualTo("STATUS")
        assertThat(auditModuleName).isEqualTo("OCCIFINE")
        assertThat(nomisEventType).isEqualTo("OFFENDER_FINE_PAYMENTS-INSERTED")
      }
    }

    @Test
    fun `OFFENDER_FINE_PAYMENTS-UPDATED is mapped`() {
      withCallTransformer<FinePaymentEvent>(
        Xtag(
          eventType = "OFFENDER_FINE_PAYMENTS-UPDATED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_FINE_PAYMENTS-UPDATED")
        assertThat(nomisEventType).isEqualTo("OFFENDER_FINE_PAYMENTS-UPDATED")
      }
    }

    @Test
    fun `OFFENDER_FINE_PAYMENTS-DELETED is mapped`() {
      withCallTransformer<FinePaymentEvent>(
        Xtag(
          eventType = "OFFENDER_FINE_PAYMENTS-DELETED",
          nomisTimestamp = fixedEventTime,
          content = xtagContent,
        ),
      ) {
        assertThat(eventType).isEqualTo("OFFENDER_FINE_PAYMENTS-DELETED")
        assertThat(nomisEventType).isEqualTo("OFFENDER_FINE_PAYMENTS-DELETED")
      }
    }
  }
}
