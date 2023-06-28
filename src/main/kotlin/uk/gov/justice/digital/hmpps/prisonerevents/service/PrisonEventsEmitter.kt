package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse
import software.amazon.awssdk.services.sns.model.ValidationException
import uk.gov.justice.digital.hmpps.prisonerevents.model.AlertOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors

@Service
class PrisonEventsEmitter(
  hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val telemetryClient: TelemetryClient,
) {
  private val prisonEventTopic: HmppsTopic? by lazy {
    hmppsQueueService.findByTopicId("prisoneventtopic")
  }
  private val prisonEventTopicSnsClient: SnsAsyncClient by lazy { prisonEventTopic!!.snsClient }
  private val topicArn: String by lazy { prisonEventTopic!!.arn }

  fun sendEvent(payload: OffenderEvent) {
    val publishResult: PublishResponse = try {
      prisonEventTopicSnsClient.publish(
        PublishRequest.builder()
          .topicArn(topicArn)
          .message(objectMapper.writeValueAsString(payload))
          .messageAttributes(metaData(payload))
          .build(),
      ).get()
    } catch (e: ExecutionException) {
      if (e.cause is ValidationException) {
        log.error("Invalid payload $payload or other parameter", e.cause)
        telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
        return
      } else {
        // Exception traceback will be logged by DefaultMessageListenerContainer
        log.error("Failed to publish message $payload", e.cause)
        telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
        throw e
      }
    } catch (e: Exception) {
      log.error("Failed to publish (unexpected exception) message $payload", e)
      telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
      throw e
    }
    val httpStatusCode = publishResult.sdkHttpResponse().statusCode()
    if (httpStatusCode >= 400) {
      telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
      throw RuntimeException("Attempt to publish message ${payload.eventType} resulted in an http $httpStatusCode error")
    }
    telemetryClient.trackEvent(payload.eventType, asTelemetryMap(payload), null)
  }

  private fun metaData(payload: OffenderEvent): Map<String, MessageAttributeValue> {
    val messageAttributes: MutableMap<String, MessageAttributeValue> = HashMap()
    messageAttributes["eventType"] =
      MessageAttributeValue.builder().dataType("String").stringValue(payload.eventType).build()
    messageAttributes["publishedAt"] = MessageAttributeValue.builder().dataType("String").stringValue(
      OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
    ).build()
    Optional.ofNullable(buildOptionalCode(payload)).ifPresent { code: String? ->
      messageAttributes["code"] = MessageAttributeValue.builder().dataType("String").stringValue(code).build()
    }
    return messageAttributes
  }

  private fun asTelemetryMap(event: OffenderEvent): Map<String, String> {
    val entries = objectMapper
      .convertValue<Map<String, String>>(event)
      .entries
    return entries.stream().collect(
      Collectors.toMap(
        { (key): Map.Entry<String, Any?> -> key },
        { (_, value): Map.Entry<String, Any?> -> value.toString() },
      ),
    )
  }

  private fun buildOptionalCode(payload: OffenderEvent): String? =
    if (payload is AlertOffenderEvent) {
      payload.alertCode
    } else if (payload is ExternalMovementOffenderEvent) {
      "${payload.movementType}-${payload.directionCode}"
    } else {
      null
    }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
