package uk.gov.justice.digital.hmpps.prisonerevents.service

import oracle.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer
import javax.jms.Message
import javax.jms.MessageListener

@Component
class JMSReceiver(
  private val offenderEventsTransformer: OffenderEventsTransformer,
  private val xtagEventsService: XtagEventsService,
  private val eventsEmitter: PrisonEventsEmitter
) : MessageListener {

  override fun onMessage(message: Message) {
    log.info("JMS AQ Message received: $message")

    xtagEventsService.addAdditionalEventData(
      offenderEventsTransformer.offenderEventOf(
        message as AQjmsMapMessage
      )
    )?.also {

      // eventsEmitter.sendEvent(it)
      log.info(it.toString())
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(JMSReceiver::class.java)
  }
}
