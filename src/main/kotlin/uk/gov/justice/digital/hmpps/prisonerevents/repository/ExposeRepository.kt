package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

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

  fun findRelatedMerge(bookingId: Long, eventDatetime: LocalDateTime): MergeTransaction? =
    MergeTransaction.find {
      (MergeTransactions.bookingId2 eq bookingId) and (MergeTransactions.requestDate greater eventDatetime.minusHours(1))
    }
      .orderBy(MergeTransactions.createDatetime to SortOrder.DESC)
      .firstOrNull()
}
