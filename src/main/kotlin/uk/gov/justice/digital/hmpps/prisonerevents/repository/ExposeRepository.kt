package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class ExposeRepository {
  fun getPersonIdFromContact(contactId: Long): Long? =
    OffenderContactPersons
      .select(OffenderContactPersons.person)
      .where(OffenderContactPersons.id eq contactId)
      .singleOrNull()
      ?.get(Persons.id)?.value

  fun getNomsIdFromContact(contactId: Long): String? =
    (OffenderContactPersons innerJoin OffenderBookings.join(Offenders, joinType = JoinType.INNER, onColumn = OffenderBookings.offender, otherColumn = Offenders.id))
      .select(Offenders.offenderNo)
      .where(OffenderContactPersons.id eq contactId)
      .singleOrNull()
      ?.get(Offenders.offenderNo)
}
