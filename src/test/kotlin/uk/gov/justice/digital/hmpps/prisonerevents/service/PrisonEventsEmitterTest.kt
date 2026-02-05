package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sns.model.ValidationException
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.prisonerevents.model.AlertOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
@JsonTest
class PrisonEventsEmitterTest(@Autowired private val jsonMapper: JsonMapper) {
  private lateinit var service: PrisonEventsEmitter

  @Mock
  private lateinit var telemetryClient: TelemetryClient

  @Captor
  private lateinit var telemetryAttributesCaptor: ArgumentCaptor<Map<String, String>>

  @Captor
  private lateinit var publishRequestCaptor: ArgumentCaptor<PublishRequest>

  private val hmppsQueueService = mock<HmppsQueueService>()
  private val prisonEventSnsClient = mock<SnsAsyncClient>()
  private val publishResult: PublishResponse = mock<PublishResponse>()

  @BeforeEach
  fun setup() {
    whenever(hmppsQueueService.findByTopicId("prisoneventtopic"))
      .thenReturn(HmppsTopic("prisoneventtopic", "topicARN", prisonEventSnsClient))

    val sdk = SdkHttpResponse.builder().statusCode(200).build()
    whenever(publishResult.sdkHttpResponse()).thenReturn(sdk)
    whenever(prisonEventSnsClient.publish(any<PublishRequest>()))
      .thenReturn(CompletableFuture.completedFuture(publishResult))

    service = PrisonEventsEmitter(hmppsQueueService, jsonMapper, telemetryClient)
  }

  @Test
  fun `will add payload as message`() {
    val payload = AlertOffenderEvent(
      eventType = "my-event-type",
      alertCode = "alert-code",
      bookingId = 12345L,
    )
    service.sendEvent(payload)
    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request).extracting("message")
      .isEqualTo("{\"eventType\":\"my-event-type\",\"bookingId\":12345,\"alertCode\":\"alert-code\"}")
  }

  @Test
  fun `will add additional fields to JSON payload`() {
    service.sendEvent(
      AlertOffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      ),
    )

    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(jsonMapper.readTree(request.message())).isEqualTo(
      jsonMapper.readTree(
        """
      {
        "eventType":"my-event-type",
        "bookingId":12345,
        "alertCode":"alert-code"
      }""",
      ),
    )
  }

  @Test
  fun `will add telemetry event`() {
    service.sendEvent(
      AlertOffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      ),
    )

    verify(telemetryClient).trackEvent(
      ArgumentMatchers.eq("my-event-type"),
      telemetryAttributesCaptor.capture(),
      ArgumentMatchers.isNull(),
    )
    assertThat(telemetryAttributesCaptor.value).containsAllEntriesOf(
      mapOf(
        "eventType" to "my-event-type",
        "bookingId" to "12345",
        "alertCode" to "alert-code",
      ),
    )
  }

  @Test
  fun `will add code`() {
    service.sendEvent(
      AlertOffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      ),
    )

    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes()["code"]).satisfies(
      Consumer {
        assertThat(it?.stringValue()).isEqualTo("alert-code")
      },
    )
  }

  @Test
  fun `code is present only for some events`() {
    service.sendEvent(
      OffenderEvent(
        eventType = "my-event-type",
        bookingId = 12345L,
      ),
    )

    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes()["code"]).isNull()
  }

  @Test
  fun `will add event type`() {
    service.sendEvent(
      AlertOffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      ),
    )

    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes()["eventType"]).satisfies(
      Consumer {
        assertThat(it?.stringValue()).isEqualTo("my-event-type")
      },
    )
  }

  @Test
  fun `will add the date time event is published`() {
    service.sendEvent(
      AlertOffenderEvent(
        eventType = "my-event-type",
        alertCode = "alert-code",
        bookingId = 12345L,
      ),
    )

    verify(prisonEventSnsClient).publish(publishRequestCaptor.capture())
    val request = publishRequestCaptor.value

    assertThat(request.messageAttributes()["publishedAt"]).isNotNull.satisfies(
      Consumer {
        assertThat(OffsetDateTime.parse(it?.stringValue()).toLocalDateTime())
          .isCloseTo(LocalDateTime.now(), Assertions.within(10, SECONDS))
      },
    )
  }

  @Test
  fun `will send telemetry for bad JSON`() {
    whenever(prisonEventSnsClient.publish(any<PublishRequest>()))
      .thenReturn(CompletableFuture.failedFuture(ValidationException.builder().build()))

    assertDoesNotThrow {
      service.sendEvent(
        AlertOffenderEvent(
          eventType = "my-event-type",
          alertCode = "alert-code",
          bookingId = 12345L,
        ),
      )
    }

    verify(telemetryClient, never()).trackEvent(eq("my-event-type"), any(), isNull())
    verify(telemetryClient).trackEvent(eq("my-event-type_FAILED"), any(), isNull())
  }

  @Test
  fun `will throw and send telemetry if publishing fails`() {
    whenever(prisonEventSnsClient.publish(any<PublishRequest>()))
      .thenReturn(CompletableFuture.failedFuture(RuntimeException("test")))

    assertThatThrownBy {
      service.sendEvent(
        AlertOffenderEvent(
          eventType = "my-event-type",
          alertCode = "alert-code",
          bookingId = 12345L,
        ),
      )
    }.rootCause().isInstanceOf(RuntimeException::class.java)
      .message()
      .isEqualTo("test")

    verify(telemetryClient, never()).trackEvent(eq("my-event-type"), any(), isNull())
    verify(telemetryClient).trackEvent(eq("my-event-type_FAILED"), any(), isNull())
  }

  @Test
  fun `will throw if publishing fails with an http error`() {
    val sdk = SdkHttpResponse.builder().statusCode(500).build()
    val publishResult: PublishResponse = mock<PublishResponse>()
    whenever(publishResult.sdkHttpResponse()).thenReturn(sdk)
    whenever(prisonEventSnsClient.publish(any<PublishRequest>()))
      .thenReturn(CompletableFuture.completedFuture(publishResult))

    assertThatThrownBy {
      service.sendEvent(
        AlertOffenderEvent(
          eventType = "my-event-type",
          alertCode = "alert-code",
          bookingId = 12345L,
        ),
      )
    }.isInstanceOf(RuntimeException::class.java)
      .message()
      .isEqualTo("Attempt to publish message my-event-type resulted in an http 500 error")

    verify(telemetryClient, never()).trackEvent(eq("my-event-type"), any(), isNull())
    verify(telemetryClient).trackEvent(eq("my-event-type_FAILED"), any(), isNull())
  }
}
