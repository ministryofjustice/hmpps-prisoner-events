package uk.gov.justice.digital.hmpps.prisonerevents.service

import jakarta.jms.Message
import jakarta.jms.MessageListener
import oracle.jakarta.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer

@Component
class JMSReceiver(
  private val offenderEventsTransformer: OffenderEventsTransformer,
  private val xtagEventsService: XtagEventsService,
  private val eventsEmitter: PrisonEventsEmitter,
) : MessageListener {

  override fun onMessage(message: Message) {
    xtagEventsService.addAdditionalEventData(
      offenderEventsTransformer.offenderEventOf(
        message as AQjmsMapMessage,
      ),
    )?.also {
      eventsEmitter.sendEvent(it)
      log.info(it.toString())
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
