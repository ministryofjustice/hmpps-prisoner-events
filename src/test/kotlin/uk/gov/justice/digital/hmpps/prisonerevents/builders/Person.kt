package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.Person
import java.time.LocalDate

fun Person.Companion.build(
  lastName: String = "STEVENS",
  firstName: String = "KWEKU",
  dateOfBirth: LocalDate = LocalDate.now().minusYears(23),
  init: Person.() -> Unit = {},
) = Person.new {
  this.lastName = lastName
  this.firstName = firstName
  this.dateOfBirth = dateOfBirth
  init()
}
