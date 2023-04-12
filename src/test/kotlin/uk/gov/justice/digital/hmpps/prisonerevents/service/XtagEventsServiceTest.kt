package uk.gov.justice.digital.hmpps.prisonerevents.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Movement
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class XtagEventsServiceTest {
  private val MOVEMENT_TIME = Timestamp.valueOf("2019-07-12 21:00:00.000")

  @Mock
  private lateinit var repository: SqlRepository

  private lateinit var service: XtagEventsService

  @BeforeEach
  fun setUp() {
    service = XtagEventsService(repository)
  }

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
          movementDate = (MOVEMENT_TIME.toLocalDateTime().toLocalDate()),
          movementTime = (MOVEMENT_TIME.toLocalDateTime().toLocalTime()),
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
    assertThat(offenderEvent?.movementDateTime).isEqualTo(MOVEMENT_TIME.toLocalDateTime())
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
        comment = "TEST COMMENT",
        enteredById = null,
      ),
    )
    assertThat(offenderEvent?.offenderIdDisplay).isEqualTo("A2345GB")
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
