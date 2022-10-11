package uk.gov.justice.digital.hmpps.prisonerevents.service

import oracle.jms.AQjmsMapMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonerevents.service.transformers.OffenderEventsTransformer
import java.lang.Thread.sleep
import java.time.LocalDateTime
import javax.jms.Message
import javax.jms.MessageListener

@Component
class JMSReceiver(
  private val offenderEventsTransformer: OffenderEventsTransformer,
  private val xtagEventsService: XtagEventsService,
  private val eventsEmitter: PrisonEventsEmitter,
  @Value("\${hmpps.oracle.delayMillis}")
  private val delayMillis: Long,
  @Value("\${jms.events.start}")
  private val startString: String,
  @Value("\${jms.events.end}")
  private val endString: String
) : MessageListener {

  val start = LocalDateTime.parse(startString)
  val end = LocalDateTime.parse(endString)

  override fun onMessage(message: Message) {
    sleep(delayMillis) // Give time for the replica db to catch up
    xtagEventsService.addAdditionalEventData(
      offenderEventsTransformer.offenderEventOf(
        message as AQjmsMapMessage
      )
    )?.also {

      if (isLive(message)) {
        eventsEmitter.sendEvent(it)
      }
      log.info(it.toString())
    }
  }

  private fun isLive(message: AQjmsMapMessage): Boolean {
    val seconds = message.jmsTimestamp / 1000
    val nanos = (message.jmsTimestamp % 1000 * 1000000).toInt()

    val nomisTimestamp = OffenderEventsTransformer.xtagFudgedTimestampOf(
      LocalDateTime.ofEpochSecond(seconds, nanos, OffenderEventsTransformer.BST)
    )

    return !nomisTimestamp.isBefore(start) && nomisTimestamp.isBefore(end)
  }

  companion object {
    private val log = LoggerFactory.getLogger(JMSReceiver::class.java)
  }
}
