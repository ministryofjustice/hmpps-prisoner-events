package uk.gov.justice.digital.hmpps.prisonerevents.service

import jakarta.jms.Message
import jakarta.jms.MessageListener
import oracle.jakarta.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer

@Component
class XtagEventsListener(
  private val offenderEventsTransformer: OffenderEventsTransformer,
  private val xtagEventsService: XtagEventsService,
  private val eventsEmitter: PrisonEventsEmitter,
) : MessageListener {

  override fun onMessage(message: Message) {
    val aqMessage = message as AQjmsMapMessage
    try {
      offenderEventsTransformer.offenderEventOf(aqMessage).addAdditionalEventData()
        ?.also {
          log.debug("Publishing {}", it)
          eventsEmitter.sendEvent(it)
        }
    } catch (ne: NullPointerException) {
      // TODO we handle null pointers separately because they don't appear in App Insights due to a bug on their side - can remove this when fixed
      log.error("Failed to send event with message due to null pointer {}", ne.cause?.message)
      throw ne
    } catch (ex: Exception) {
      log.error("Failed to send event due to exception", ex)
      throw ex
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun OffenderEvent?.addAdditionalEventData(): OffenderEvent? = xtagEventsService.addAdditionalEventData(this)
}
