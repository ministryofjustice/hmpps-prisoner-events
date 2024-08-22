package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository

@Repository
class ExposeRepository {
  fun getPersonIdFromRestriction(offenderPersonRestrictionId: Long): Long? =
    (OffenderContactRestrictions innerJoin OffenderContactPersons)
      .select(OffenderContactPersons.person)
      .where(OffenderContactRestrictions.id eq offenderPersonRestrictionId)
      .singleOrNull()
      ?.get(Persons.id)?.value
}
