package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.springframework.stereotype.Repository

@Repository
class ExposeRepository {
  fun getPersonIdFromRestriction(offenderPersonRestrictionId: Long): Long? =
    OffenderContactRestriction.findById(offenderPersonRestrictionId)?.offenderContactPerson?.person?.personId?.value
}
