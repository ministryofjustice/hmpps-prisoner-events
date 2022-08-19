package uk.gov.justice.digital.hmpps.prisonerevents

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsPrisonerEvents

fun main(args: Array<String>) {
  runApplication<HmppsPrisonerEvents>(*args)
}
