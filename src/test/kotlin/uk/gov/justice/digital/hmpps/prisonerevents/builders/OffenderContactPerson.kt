package uk.gov.justice.digital.hmpps.prisonerevents.builders

import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderBooking
import uk.gov.justice.digital.hmpps.prisonerevents.repository.OffenderContactPerson
import uk.gov.justice.digital.hmpps.prisonerevents.repository.Person

fun OffenderContactPerson.Companion.build(
  offenderBooking: OffenderBooking,
  person: Person,
  contactType: String = "S",
  relationshipType: String = "BRO",
  createUserName: String = "J.SMITH",
  modifyUserName: String? = null,
  init: OffenderContactPerson.() -> Unit = {},
) = OffenderContactPerson.new {
  this.offenderBooking = offenderBooking
  this.person = person
  this.contactType = contactType
  this.relationshipType = relationshipType
  this.createUserName = createUserName
  this.modifyUserName = modifyUserName
  init()
}
