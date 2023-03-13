package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.PublishResult
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
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
  private val prisonEventTopicSnsClient: AmazonSNSAsync by lazy { prisonEventTopic!!.snsClient as AmazonSNSAsync }
  private val topicArn: String by lazy { prisonEventTopic!!.arn }

  fun sendEvent(payload: OffenderEvent) {
    val publishResult: PublishResult? = try {
      prisonEventTopicSnsClient.publish(
        PublishRequest(topicArn, objectMapper.writeValueAsString(payload))
          .withMessageAttributes(metaData(payload)),
      )
    } catch (e: JsonProcessingException) {
      log.error("Failed to convert payload {} to json", payload, e)
      telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
      return
    } catch (e: Exception) {
      // Exception traceback will be logged by DefaultMessageListenerContainer
      log.error("Failed to publish message {}", payload)
      telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
      throw e
    }
    val httpStatusCode = publishResult?.sdkHttpMetadata?.httpStatusCode
    if (httpStatusCode != null && httpStatusCode >= 400) {
      telemetryClient.trackEvent("${payload.eventType}_FAILED", asTelemetryMap(payload), null)
      throw RuntimeException("Attempt to publish message ${payload.eventType} resulted in an http $httpStatusCode error")
    }
    telemetryClient.trackEvent(payload.eventType, asTelemetryMap(payload), null)
  }

  private fun metaData(payload: OffenderEvent): Map<String, MessageAttributeValue> {
    val messageAttributes: MutableMap<String, MessageAttributeValue> = HashMap()
    messageAttributes["eventType"] = MessageAttributeValue().withDataType("String").withStringValue(payload.eventType)
    messageAttributes["publishedAt"] = MessageAttributeValue().withDataType("String").withStringValue(
      OffsetDateTime.now().format(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
      ),
    )
    Optional.ofNullable(buildOptionalCode(payload)).ifPresent { code: String? ->
      messageAttributes["code"] = MessageAttributeValue().withDataType("String").withStringValue(code)
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
    if (payload.alertCode != null) {
      payload.alertCode
    } else if (payload.movementType != null) {
      payload.movementType + "-" + payload.directionCode
    } else {
      null
    }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
