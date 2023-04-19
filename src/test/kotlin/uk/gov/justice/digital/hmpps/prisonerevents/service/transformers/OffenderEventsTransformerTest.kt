@file:Suppress("TestFunctionName")

package uk.gov.justice.digital.hmpps.prisonerevents.service.transformers

import oracle.jms.AQjmsMapMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.NonAssociationDetailsOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.RestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.VisitorRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.externalMovementEventOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localDateOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localDateTimeOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.localTimeOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer.Companion.xtagFudgedTimestampOf
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.Xtag
import uk.gov.justice.digital.hmpps.prisonerevents.service.xtag.XtagContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class OffenderEventsTransformerTest {
  private val offenderEventsTransformer = OffenderEventsTransformer()
  private val fixedEventTime = LocalDateTime.now()

  @Suppress("UNCHECKED_CAST")
  private fun <T : OffenderEvent> withCallTransformer(xtag: Xtag, block: T.() -> Unit): Unit =
    (offenderEventsTransformer.offenderEventOf(xtag)!! as T).block()

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
  fun S2_RESULT_IsMappedTo_SENTENCE_DATES_CHANGED() {
    withCallTransformer<OffenderEvent>(
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
    }
  }

  @Test
  fun OFF_SENT_OASYS_IsMappedTo_SENTENCE_CALCULATION_DATES_CHANGED() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_SENT_OASYS",
        content = XtagContent(
          mapOf("p_offender_book_id" to "99"),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("SENTENCE_CALCULATION_DATES-CHANGED")
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
        content = XtagContent(mapOf("p_offender_id_display" to "A123BC")),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHYSICAL_DETAILS-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
    }
  }

  @Test
  fun `offender physical characteristics changes mapped correctly`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_PROFILE_DETS-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(mapOf("p_offender_id_display" to "A123BC")),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHYSICAL_DETAILS-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
    }
  }

  @Test
  fun `offender physical marks changes mapped correctly`() {
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "OFF_IDENT_MARKS-CHANGED",
        nomisTimestamp = fixedEventTime,
        content = XtagContent(mapOf("p_offender_id_display" to "A123BC")),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_PHYSICAL_DETAILS-CHANGED")
      assertThat(offenderIdDisplay).isEqualTo("A123BC")
      assertThat(eventDatetime).isEqualTo(fixedEventTime)
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
      assertThat(comment).isEqualTo("comment")
    }
  }

  @Test
  fun `restriction changes mapped correctly`() {
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
      assertThat(comment).isEqualTo("comment")
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
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
      assertThat(comment).isEqualTo("comment")
      assertThat(authorisedById).isEqualTo(12345L)
      assertThat(enteredById).isEqualTo(23456L)
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
      assertThat(comment).isEqualTo("comment")
      assertThat(visitorRestrictionId).isEqualTo(123456)
      assertThat(enteredById).isEqualTo(23456)
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
    withCallTransformer<GenericOffenderEvent>(
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
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "BOOK_UPD_OASYS",
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
    withCallTransformer<GenericOffenderEvent>(
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
      assertThat(previousOffenderId).isEqualTo(456L)
      assertThat(bookingId).isEqualTo(789)
      assertThat(nomisEventType).isEqualTo("OFF_BKB_UPD")
      assertThat(offenderIdDisplay).isNull()
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
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_identifier_type" to "some type",
            "p_identifier_value" to "value",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFIER-INSERTED")
      assertThat(offenderId).isEqualTo(123)
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(identifierType).isEqualTo("some type")
      assertThat(identifierValue).isEqualTo("value")
      assertThat(nomisEventType).isEqualTo("P3_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }

  @Test
  fun `offender identifier deleted mapped correctly`() {
    val now = LocalDateTime.now()
    withCallTransformer<GenericOffenderEvent>(
      Xtag(
        eventType = "P3_RESULT",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_id" to "123",
            "p_root_offender_id" to "456",
            "p_identifier_type" to "some type",
          ),
        ),
      ),
    ) {
      assertThat(eventType).isEqualTo("OFFENDER_IDENTIFIER-DELETED")
      assertThat(offenderId).isEqualTo(123)
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(rootOffenderId).isEqualTo(456)
      assertThat(identifierType).isEqualTo("some type")
      assertThat(nomisEventType).isEqualTo("P3_RESULT")
      assertThat(offenderIdDisplay).isNull()
    }
  }
}
