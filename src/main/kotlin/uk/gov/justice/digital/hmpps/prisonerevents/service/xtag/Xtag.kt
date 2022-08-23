package uk.gov.justice.digital.hmpps.prisonerevents.service.xtag

import java.time.LocalDateTime

data class Xtag(
  val nomisTimestamp: LocalDateTime? = null,
  val content: XtagContent,
  val eventType: String? = null,
)
