package uk.gov.justice.digital.hmpps.prisonerevents.service.transformers

import oracle.jms.AQjmsMapMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
        Xtag(content = XtagContent(mapOf("p_record_deleted" to "Y")))
      )
    ).isEqualTo("EXTERNAL_MOVEMENT_RECORD-DELETED")
    assertThat(
      externalMovementEventOf(
        Xtag(content = XtagContent(mapOf("p_record_deleted" to "N")))
      )
    ).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED")
    assertThat(
      externalMovementEventOf(
        Xtag(content = XtagContent(mapOf("p_record_deleted" to UUID.randomUUID().toString())))
      )
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
              "p_to_agy_loc_id" to "SAUCE"
            )
          )
        ),
        externalMovementEventOf(
          Xtag(
            content = XtagContent(
              mapOf(
                "p_from_agy_loc_id" to "BARBECUE",
                "p_to_agy_loc_id" to "SAUCE"
              )
            )
          )
        )
      )
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
          eventType = "meh"
        )
      )
    ).isNotNull()
  }

  @Test
  fun S2_RESULT_IsMappedTo_SENTENCE_DATES_CHANGED() {
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = "S2_RESULT",
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
            "p_offender_sent_calculation_id" to "88"
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("SENTENCE_DATES-CHANGED")
  }

  @Test
  fun OFF_SENT_OASYS_IsMappedTo_SENTENCE_CALCULATION_DATES_CHANGED() {
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = "OFF_SENT_OASYS",
        content = XtagContent(
          mapOf("p_offender_book_id" to "99")
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("SENTENCE_CALCULATION_DATES-CHANGED")
  }

  @Test
  fun bedAssignmentCorrectlyMapped() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = "BED_ASSIGNMENT_HISTORY-INSERTED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
            "p_bed_assign_seq" to "1",
            "p_living_unit_id" to "34123412"
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED")
    assertThat(event?.bookingId).isEqualTo(99L)
    assertThat(event?.bedAssignmentSeq).isEqualTo(1)
    assertThat(event?.livingUnitId).isEqualTo(34123412L)
    assertThat(event?.nomisEventType).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED")
    assertThat(event?.eventDatetime).isEqualTo(now)
  }

  @Test
  fun externalMovementChangedCorrectlyMapped() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
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
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("EXTERNAL_MOVEMENT-CHANGED")
    assertThat(event?.bookingId).isEqualTo(232L)
    assertThat(event?.movementSeq).isEqualTo(1)
    assertThat(event?.nomisEventType).isEqualTo("EXTERNAL_MOVEMENT-CHANGED")
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.fromAgencyLocationId).isEqualTo("MDI")
    assertThat(event?.toAgencyLocationId).isEqualTo("HOSP")
    assertThat(event?.directionCode).isEqualTo("OUT")
    assertThat(event?.movementReasonCode).isEqualTo("HP")
    assertThat(event?.movementType).isEqualTo("REL")
  }

  @Test
  fun sentencingChangedCorrectlyMapped() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = "SENTENCING-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "2322322",
            "p_offender_id_display" to "A1234AA",
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("SENTENCING-CHANGED")
    assertThat(event?.bookingId).isEqualTo(2322322L)
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.offenderIdDisplay).isEqualTo("A1234AA")
  }

  @Test
  fun confirmedReleaseDateChangeMappedCorrectly() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = "CONFIRMED_RELEASE_DATE-CHANGED",
        nomisTimestamp = now,
        content = XtagContent(
          mapOf(
            "p_offender_book_id" to "99",
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("CONFIRMED_RELEASE_DATE-CHANGED")
    assertThat(event?.bookingId).isEqualTo(99L)
    assertThat(event?.eventDatetime).isEqualTo(now)
  }

  @Test
  fun offenderIndividualScheduledEventMappedCorrectly() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
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
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("APPOINTMENT_CHANGED")
    assertThat(event?.bookingId).isEqualTo(52303L)
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.agencyLocationId).isEqualTo("LEI")
    assertThat(event?.scheduleEventId).isEqualTo(2362162)
    assertThat(event?.scheduledStartTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 0, 0))
    assertThat(event?.scheduledEndTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 30, 0))
    assertThat(event?.scheduleEventStatus).isEqualTo("SCH")
    assertThat(event?.scheduleEventType).isEqualTo("APP")
    assertThat(event?.scheduleEventSubType).isEqualTo("CALA")
    assertThat(event?.scheduleEventClass).isEqualTo("INT_MOV")
    assertThat(event?.recordDeleted).isEqualTo(true)
  }

  @Test
  fun offenderIndividualScheduledEventMappedCorrectlyWhenEndDateMissing() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
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
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("APPOINTMENT_CHANGED")
    assertThat(event?.bookingId).isEqualTo(52303L)
    assertThat(event?.scheduledStartTime).isEqualTo(LocalDateTime.of(2022, 7, 19, 16, 0, 0))
    assertThat(event?.scheduledEndTime).isNull()
  }

  @Test
  fun offenderIepUpdatedMappedCorrectly() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
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
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("IEP_UPSERTED")
    assertThat(event?.bookingId).isEqualTo(434L)
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.iepSeq).isEqualTo(3)
    assertThat(event?.iepLevel).isEqualTo("STD")
    assertThat(event?.offenderIdDisplay).isEqualTo("AF123")
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.auditModuleName).isEqualTo("transfer")
  }

  @Test
  fun offenderIepDeletedMappedCorrectly() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
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
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("IEP_DELETED")
    assertThat(event?.bookingId).isEqualTo(434L)
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.iepSeq).isEqualTo(3)
    assertThat(event?.iepLevel).isEqualTo("STD")
    assertThat(event?.offenderIdDisplay).isEqualTo("AF123")
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.auditModuleName).isEqualTo("transfer")
  }

  @Test
  fun visitCancelledMappedCorrectly() {
    val now = LocalDateTime.now()
    val event = offenderEventsTransformer.offenderEventOf(
      Xtag(
        eventType = ("OFFENDER_VISIT-UPDATED"),
        nomisTimestamp = (now),
        content = XtagContent(
          mapOf(
            "p_agy_loc_id" to "MDI",
            "p_offender_visit_id" to "4",
            "p_event_id" to ("2323"),
            "p_event_date" to ("2022-08-23"),
            "p_offender_book_id" to ("434"),
            "p_offender_id_display" to ("AF123"),
            "p_audit_module_name" to ("visit_screen")
          )
        )
      )
    )
    assertThat(event?.eventType).isEqualTo("VISIT_CANCELLED")
    assertThat(event?.bookingId).isEqualTo(434L)
    assertThat(event?.eventDatetime).isEqualTo(now)
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.visitId).isEqualTo(4)
    assertThat(event?.offenderIdDisplay).isEqualTo("AF123")
    assertThat(event?.agencyLocationId).isEqualTo("MDI")
    assertThat(event?.auditModuleName).isEqualTo("visit_screen")
  }
}
