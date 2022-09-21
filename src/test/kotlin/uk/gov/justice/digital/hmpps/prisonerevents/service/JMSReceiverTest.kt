package uk.gov.justice.digital.hmpps.prisonerevents.service

import oracle.jms.AQjmsMapMessage
import oracle.jms.AQjmsMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer

@ExtendWith(MockitoExtension::class)
class JMSReceiverTest {

  private lateinit var service: JMSReceiver

  @Mock
  private lateinit var xtagEventsService: XtagEventsService

  @Mock
  private lateinit var eventsEmitter: PrisonEventsEmitter

  @Captor
  private lateinit var captor: ArgumentCaptor<OffenderEvent>

  var START = 1667653847000
  var END = 1667653857000
  var ONE_SECOND = 1000

  @BeforeEach
  fun setup() {
    service = JMSReceiver(
      OffenderEventsTransformer(),
      xtagEventsService, eventsEmitter,
      "2022-11-05T13:10:47",
      "2022-11-05T13:10:57"
    )
  }

  @Test
  fun `will get message`() {
    whenever(xtagEventsService.addAdditionalEventData(any()))
      .thenReturn(OffenderEvent(eventId = "abc"))

    val message = AQjmsMapMessage().apply {
      this.setString("p_offender_book_id", "12345")
      this.jmsType = "test"
      this.jmsTimestamp = START
    }

    service.onMessage(message)

    verify(xtagEventsService).addAdditionalEventData(captor.capture())
    verify(eventsEmitter).sendEvent(OffenderEvent(eventId = "abc"))

    assertThat(captor.value.eventType).isEqualTo("test")
  }

  @Test
  fun `will get message if in JMS range`() {
    whenever(xtagEventsService.addAdditionalEventData(any()))
      .thenReturn(OffenderEvent(eventId = "abc"))

    val message = AQjmsMapMessage().apply {
      this.setString("p_offender_book_id", "12345")
      this.jmsType = "test"
      this.jmsTimestamp = END - ONE_SECOND
    }

    service.onMessage(message)

    verify(xtagEventsService).addAdditionalEventData(captor.capture())
    verify(eventsEmitter).sendEvent(OffenderEvent(eventId = "abc"))

    assertThat(captor.value.eventType).isEqualTo("test")
  }

  @Test
  fun `will only log message if before JMS timestamp range`() {
    whenever(xtagEventsService.addAdditionalEventData(any()))
      .thenReturn(OffenderEvent(eventId = "abc"))

    val message = AQjmsMapMessage().apply {
      this.jmsType = "test"
      this.jmsTimestamp = START - ONE_SECOND
    }

    service.onMessage(message)

    verifyNoInteractions(eventsEmitter)
  }

  @Test
  fun `will only log message if after JMS timestamp range`() {
    whenever(xtagEventsService.addAdditionalEventData(any()))
      .thenReturn(OffenderEvent(eventId = "abc"))

    val message = AQjmsMapMessage().apply {
      this.jmsType = "test"
      this.jmsTimestamp = END
    }

    service.onMessage(message)

    verifyNoInteractions(eventsEmitter)
  }

  @Test
  fun `message wrong type`() {
    val message = mock<AQjmsMessage>()

    assertThrows<ClassCastException> {
      service.onMessage(message)
    }
  }

  @Test
  fun `message has no jmsType`() {
    val message = AQjmsMapMessage().apply {
      this.setString("p_offender_book_id", "12345")
    }

    service.onMessage(message)

    verify(xtagEventsService).addAdditionalEventData(captor.capture())

    assertThat(captor.value).isNull()
  }
}
