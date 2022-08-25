package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class PrisonEventsEmitterTest {
  private val objectMapper = ObjectMapper()
  private lateinit var service: PrisonEventsEmitter

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Captor
  private lateinit var telemetryAttributesCaptor: ArgumentCaptor<Map<String, String>>

  @Captor
  private lateinit var publishRequestCaptor: ArgumentCaptor<PublishRequest>

  private val hmppsQueueService = mock<HmppsQueueService>()
  private val prisonEventSnsClient = mock<AmazonSNSAsync>()

  @BeforeEach
  fun setup() {
    whenever(hmppsQueueService.findByTopicId("prisoneventtopic"))
      .thenReturn(HmppsTopic("prisoneventtopic", "topicARN", prisonEventSnsClient))

    service = PrisonEventsEmitter(hmppsQueueService, objectMapper, telemetryClient)
  }

  @Test
  fun `will add payload as message`() {
    val payload = OffenderEvent(
      eventType = "my-event-type",
      alertCode = "alert-code",
      bookingId = 12345L,
    )
    service.sendEvent(payload)
    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request).extracting("message")
      .isEqualTo("{\"eventType\":\"my-event-type\",\"bookingId\":12345,\"alertCode\":\"alert-code\"}")
  }

  @Test
  fun `will add additional fields to JSON payload`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      )
    )

    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(objectMapper.readTree(request.message)).isEqualTo(
      objectMapper.readTree(
        """
      {
        "eventType":"my-event-type",
        "bookingId":12345,
        "alertCode":"alert-code"
      }"""
      )
    )
  }

  @Test
  fun `will add telemetry event`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      )
    )

    verify(telemetryClient).trackEvent(
      ArgumentMatchers.eq("my-event-type"),
      telemetryAttributesCaptor.capture(),
      ArgumentMatchers.isNull()
    )
    assertThat(telemetryAttributesCaptor.value).containsAllEntriesOf(
      java.util.Map.of(
        "eventType",
        "my-event-type",
        "bookingId",
        "12345",
        "alertCode",
        "alert-code"
      )
    )
  }

  @Test
  fun `will add code`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      )
    )

    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes["code"]).satisfies(
      Consumer {
        assertThat(it?.stringValue).isEqualTo("alert-code")
      }
    )
  }

  @Test
  fun `code is present only for some events`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        bookingId = 12345L,
      )
    )

    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes["code"]).isNull()
  }

  @Test
  fun `will add event type`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      )
    )

    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes["eventType"]).satisfies(
      Consumer {
        assertThat(it?.stringValue).isEqualTo("my-event-type")
      }
    )
  }

  @Test
  fun `will add the date time event is published`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      )
    )

    verify(prisonEventSnsClient).publishAsync(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes["publishedAt"]).isNotNull.satisfies(
      Consumer {
        assertThat(OffsetDateTime.parse(it?.stringValue).toLocalDateTime())
          .isCloseTo(LocalDateTime.now(), Assertions.within(10, SECONDS))
      }
    )
  }
}
