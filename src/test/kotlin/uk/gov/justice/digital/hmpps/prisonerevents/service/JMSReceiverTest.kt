package uk.gov.justice.digital.hmpps.prisonerevents.service

import oracle.jakarta.jms.AQjmsMapMessage
import oracle.jakarta.jms.AQjmsMessage
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
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
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

  @BeforeEach
  fun setup() {
    service = JMSReceiver(
      OffenderEventsTransformer(),
      xtagEventsService,
      eventsEmitter,
    )
  }

  @Test
  fun `will get message`() {
    whenever(xtagEventsService.addAdditionalEventData(any()))
      .thenReturn(GenericOffenderEvent(eventId = "abc"))

    val message = AQjmsMapMessage().apply {
      this.setString("p_offender_book_id", "12345")
      this.jmsType = "test"
    }

    service.onMessage(message)

    verify(xtagEventsService).addAdditionalEventData(captor.capture())
    verify(eventsEmitter).sendEvent(
      check<GenericOffenderEvent> {
        assertThat(it.eventId).isEqualTo("abc")
      },
    )

    assertThat(captor.value.eventType).isEqualTo("test")
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
