package uk.gov.justice.digital.hmpps.prisonerevents.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import com.fasterxml.jackson.module.kotlin.convertValue
import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishRequest
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
  objectMapper: ObjectMapper,
  telemetryClient: TelemetryClient,
) {
  private val prisonEventTopicSnsClient: SnsAsyncClient
  private val topicArn: String
  private val objectMapper: ObjectMapper
  private val telemetryClient: TelemetryClient

  init {
    val prisonEventTopic: HmppsTopic? = hmppsQueueService.findByTopicId("prisoneventtopic")
    topicArn = prisonEventTopic!!.arn
    prisonEventTopicSnsClient = prisonEventTopic.snsClient
    this.objectMapper = objectMapper
    this.telemetryClient = telemetryClient
  }

  fun sendEvent(payload: OffenderEvent) {
    try {
      prisonEventTopicSnsClient.publish(
        PublishRequest.builder()
          .topicArn(topicArn)
          .message(objectMapper.writeValueAsString(payload))
          .messageAttributes(metaData(payload))
          .build()
      )
      telemetryClient.trackEvent(payload.eventType, asTelemetryMap(payload), null)
    } catch (e: JsonProcessingException) {
      log.error("Failed to convert payload {} to json", payload)
    }
  }

  private fun metaData(payload: OffenderEvent): Map<String, MessageAttributeValue> {
    val messageAttributes: MutableMap<String, MessageAttributeValue> = HashMap()
    messageAttributes["eventType"] = MessageAttributeValue.builder().dataType("String").stringValue(payload.eventType).build()
    messageAttributes["publishedAt"] = MessageAttributeValue.builder().dataType("String").stringValue(
      OffsetDateTime.now().format(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
      )
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
        { (_, value): Map.Entry<String, Any?> -> value.toString() }
      )
    )
  }

  private fun buildOptionalCode(payload: OffenderEvent): String? =
    if (payload.alertCode != null) {
      payload.alertCode
    } else if (payload.movementType != null) {
      payload.movementType + "-" + payload.directionCode
    } else
      null

  companion object {
    private val log = LoggerFactory.getLogger(PrisonEventsEmitter::class.java)
  }
}
