package uk.gov.justice.digital.hmpps.prisonerevents.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingReassignedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.repository.ExposeRepository
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Movement
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

class XtagEventsServiceTest {
  private val movementTime = Timestamp.valueOf("2019-07-12 21:00:00.000")

  private val repository: SqlRepository = mock()
  private val exposeRepository: ExposeRepository = mock()
  private val service: XtagEventsService = XtagEventsService(repository, exposeRepository)

  @Test
  fun shouldAddNomsIdToOffenderAliasEvent() {
    assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER_ALIAS-CHANGED")
  }

  @Test
  fun shouldAddNomsIdToOffenderDetailsChangedEvent() {
    assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER_DETAILS-CHANGED")
  }

  @Test
  fun shouldDecorateWithExternalMovementData() {
    whenever(repository.getMovement(1L, 2)).thenReturn(
      listOf(
        Movement(
          fromAgency = "MDI",
          toAgency = "BAI",
          movementType = "REL",
          movementDate = (movementTime.toLocalDateTime().toLocalDate()),
          movementTime = (movementTime.toLocalDateTime().toLocalTime()),
          offenderNo = ("A2345GB"),
          directionCode = "IN",
        ),
      ),
    )

    val offenderEvent = service.addAdditionalEventData(
      ExternalMovementOffenderEvent(
        bookingId = 1L,
        movementSeq = 2L,
        eventType = "EXTERNAL_MOVEMENT_RECORD-INSERTED",
        movementDateTime = null,
        movementType = null,
        movementReasonCode = null,
        directionCode = null,
        escortCode = null,
        fromAgencyLocationId = null,
        toAgencyLocationId = null,
      ),
    ) as ExternalMovementOffenderEvent?

    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
    assertThat(offenderEvent?.fromAgencyLocationId).isEqualTo("MDI")
    assertThat(offenderEvent?.toAgencyLocationId).isEqualTo("BAI")
    assertThat(offenderEvent?.movementDateTime).isEqualTo(movementTime.toLocalDateTime())
    assertThat(offenderEvent?.movementType).isEqualTo("REL")
    assertThat(offenderEvent?.directionCode).isEqualTo("IN")
  }

  @Test
  fun shouldDecorateWithExternalMovementDataHandlesNullableFields() {
    whenever(repository.getMovement(1L, 2)).thenReturn(
      listOf(Movement(offenderNo = ("A2345GB"))),
    )

    val offenderEvent = service.addAdditionalEventData(
      ExternalMovementOffenderEvent(
        bookingId = 1L,
        movementSeq = 2L,
        eventType = "EXTERNAL_MOVEMENT_RECORD-INSERTED",
        movementDateTime = null,
        movementType = null,
        movementReasonCode = null,
        directionCode = null,
        escortCode = null,
        fromAgencyLocationId = null,
        toAgencyLocationId = null,
      ),
    ) as ExternalMovementOffenderEvent?

    assertThat(offenderEvent?.bookingId).isEqualTo(1L)
    assertThat(offenderEvent?.movementSeq).isEqualTo(2L)
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
    assertThat(offenderEvent?.fromAgencyLocationId).isNull()
    assertThat(offenderEvent?.toAgencyLocationId).isNull()
    assertThat(offenderEvent?.movementDateTime).isNull()
    assertThat(offenderEvent?.movementType).isNull()
    assertThat(offenderEvent?.directionCode).isNull()
  }

  @Test
  fun shouldDecorateWithExternalMovementDataHandlesNoRecordFound() {
    whenever(repository.getMovement(1L, 2)).thenReturn(listOf())

    val offenderEvent = service.addAdditionalEventData(
      ExternalMovementOffenderEvent(
        bookingId = 1L,
        movementSeq = 2L,
        eventType = "EXTERNAL_MOVEMENT_RECORD-INSERTED",
        movementDateTime = null,
        movementType = null,
        movementReasonCode = null,
        directionCode = null,
        escortCode = null,
        fromAgencyLocationId = null,
        toAgencyLocationId = null,
      ),
    ) as ExternalMovementOffenderEvent?

    assertThat(offenderEvent?.bookingId).isEqualTo(1L)
    assertThat(offenderEvent?.movementSeq).isEqualTo(2L)
    assertThat(offenderEvent?.offenderIdDisplay).isNull()
    assertThat(offenderEvent?.fromAgencyLocationId).isNull()
    assertThat(offenderEvent?.toAgencyLocationId).isNull()
    assertThat(offenderEvent?.movementDateTime).isNull()
    assertThat(offenderEvent?.movementType).isNull()
    assertThat(offenderEvent?.directionCode).isNull()
  }

  @Test
  fun shouldDecorateOffenderUpdatedWithOffenderDisplayNo() {
    assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId("OFFENDER-UPDATED")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "OFFENDER_MOVEMENT-DISCHARGE", "OFFENDER_MOVEMENT-RECEPTION", "BED_ASSIGNMENT_HISTORY-INSERTED",
      "CONFIRMED_RELEASE_DATE-CHANGED", "SENTENCE_DATES-CHANGED",
      "OFFENDER_CASE_NOTES-INSERTED", "OFFENDER_CASE_NOTES-UPDATED", "OFFENDER_CASE_NOTES-DELETED",
      "ALERT-UPDATED", "ALERT-INSERTED", "ALERT-DELETED",
    ],
  )
  fun shouldDecorateWithOffenderDisplayNoUsingBookingId(eventType: String) {
    assertEventIsDecoratedWithOffenderDisplayNoUsingBookingId(eventType)
  }

  @Test
  fun sentenceDateChangedDecorationFailureShouldNotPreventEventBeingRaised() {
    val offenderEvent = service.addAdditionalEventData(
      OffenderEvent(bookingId = 1234L, offenderId = 1L, eventType = "SENTENCE_DATES-CHANGED-INSERTED"),
    )

    assertThat(offenderEvent?.bookingId).isEqualTo(1234L)
  }

  @Test
  fun confirmedReleaseDateChangedDecorationFailureShouldNotPreventEventBeingRaised() {
    whenever(repository.getNomsIdFromBooking(1234L)).thenReturn(listOf())

    val offenderEvent = service.addAdditionalEventData(
      OffenderEvent(bookingId = 1234L, offenderId = 1L, eventType = "CONFIRMED_RELEASE_DATE-CHANGED"),
    )

    assertThat(offenderEvent?.bookingId).isEqualTo(1234L)
  }

  @Test
  fun shouldDecoratePersonRistrictionUpdatedWithOffenderDisplayNo() {
    whenever(repository.getNomsIdFromRestriction(1234L)).thenReturn(listOf("A2345GB"))

    val offenderEvent = service.addAdditionalEventData(
      PersonRestrictionOffenderEvent(
        offenderPersonRestrictionId = 1234L,
        eventType = "PERSON_RESTRICTION-UPSERTED",
        eventDatetime = LocalDateTime.now(),
        nomisEventType = "OFF_PERSON",
        contactPersonId = 1L,
        restrictionType = "TEST",
        effectiveDate = LocalDate.now(),
        expiryDate = null,
        authorisedById = null,
        enteredById = null,
      ),
    )
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
  }

  @Test
  fun `should add offender id and previous offender id to booking updated event`() {
    whenever(repository.getNomsIdFromOffender(1234L)).thenReturn(listOf("A1234GB"))
    whenever(repository.getNomsIdFromOffender(2345L)).thenReturn(listOf("A2345GC"))

    val offenderEvent = service.addAdditionalEventData(
      OffenderBookingReassignedEvent(
        eventType = "OFFENDER_BOOKING-REASSIGNED",
        eventDatetime = LocalDateTime.now(),
        nomisEventType = "OFF_BKB_UPD",
        bookingId = 12L,
        offenderId = 1234L,
        previousOffenderId = 2345L,
      ),
    )
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A1234GB")
    assertThat((offenderEvent as OffenderBookingReassignedEvent).previousOffenderIdDisplay).isEqualTo("A2345GC")
  }

  @Test
  fun `should add offender number for offender address inserted event`() {
    whenever(repository.getNomsIdFromOffender(1234L)).thenReturn(listOf("A1234GB"))

    val offenderEvent = service.addAdditionalEventData(
      GenericOffenderEvent(
        eventType = "OFFENDER_ADDRESS-INSERTED",
        eventDatetime = LocalDateTime.now(),
        nomisEventType = "ADDR_INS",
        ownerClass = "OFF",
        ownerId = 1234L,
      ),
    )
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A1234GB")
  }

  @Test
  fun `should add offender number for offender address udpated event`() {
    whenever(repository.getNomsIdFromOffender(1234L)).thenReturn(listOf("A1234GB"))

    val offenderEvent = service.addAdditionalEventData(
      GenericOffenderEvent(
        eventType = "OFFENDER_ADDRESS-UPDATED",
        eventDatetime = LocalDateTime.now(),
        nomisEventType = "ADDR_UPD",
        ownerClass = "OFF",
        ownerId = 1234L,
      ),
    )
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A1234GB")
  }

  private fun assertEventIsDecoratedWithOffenderDisplayNoUsingOffenderId(eventName: String) {
    whenever(repository.getNomsIdFromOffender(1L)).thenReturn(listOf("A2345GB"))

    val offenderEvent = service.addAdditionalEventData(
      OffenderEvent(offenderId = 1L, eventType = eventName),
    )

    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
    assertThat(offenderEvent?.offenderId).isEqualTo(1L)
  }

  private fun assertEventIsDecoratedWithOffenderDisplayNoUsingBookingId(eventName: String) {
    whenever(repository.getNomsIdFromBooking(1234L)).thenReturn(listOf("A2345GB"))

    val offenderEvent = service.addAdditionalEventData(
      OffenderEvent(bookingId = 1234L, offenderId = 1L, eventType = eventName),
    )

    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
    assertThat(offenderEvent?.bookingId).isEqualTo(1234L)
  }
}
