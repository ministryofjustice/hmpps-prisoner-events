package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import java.sql.ResultSet
import java.sql.Types
import java.time.LocalDate
import java.time.LocalTime

@Repository
class SqlRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

  fun getNomsIdFromOffender(offenderId: Long): Collection<String> = jdbcTemplate.query(
    """
      SELECT OFFENDER_ID_DISPLAY
      FROM OFFENDERS 
      WHERE OFFENDER_ID = :offenderId
    """.trimIndent(),
    MapSqlParameterSource().addValue("offenderId", offenderId),
  ) { resultSet: ResultSet, _: Int -> resultSet.getString("OFFENDER_ID_DISPLAY") }

  fun getNomsIdFromBooking(bookingId: Long): Collection<String> = jdbcTemplate.query(
    """
      SELECT OFFENDER_ID_DISPLAY
      FROM OFFENDER_BOOKINGS
        INNER JOIN OFFENDERS ON OFFENDER_BOOKINGS.OFFENDER_ID = OFFENDERS.OFFENDER_ID
      WHERE OFFENDER_BOOK_ID = :bookingId
    """.trimIndent(),
    MapSqlParameterSource().addValue("bookingId", bookingId),
  ) { resultSet: ResultSet, _: Int -> resultSet.getString("OFFENDER_ID_DISPLAY") }

  fun getMovement(bookingId: Long, sequenceNumber: Int): Collection<Movement> = jdbcTemplate.query(
    """
      SELECT OFFENDERS.OFFENDER_ID_DISPLAY  AS OFFENDER_NO,
      OEM.FROM_AGY_LOC_ID   AS FROM_AGENCY,
      OEM.TO_AGY_LOC_ID     AS TO_AGENCY,
      OEM.MOVEMENT_DATE,
      OEM.MOVEMENT_TIME,
      OEM.MOVEMENT_TYPE,
      OEM.DIRECTION_CODE
      FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID
        INNER JOIN OFFENDERS            ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
      WHERE OEM.MOVEMENT_SEQ = :sequenceNumber
      AND OEM.OFFENDER_BOOK_ID = :bookingId
    """.trimIndent(),
    MapSqlParameterSource()
      .addValue("bookingId", bookingId)
      .addValue("sequenceNumber", sequenceNumber),
  ) { resultSet: ResultSet, _: Int ->
    Movement(
      resultSet.getString("OFFENDER_NO"),
      resultSet.getString("FROM_AGENCY"),
      resultSet.getString("TO_AGENCY"),
      resultSet.getString("MOVEMENT_TYPE"),
      resultSet.getString("DIRECTION_CODE"),
      resultSet.getDate("MOVEMENT_DATE").toLocalDate(),
      resultSet.getTime("MOVEMENT_TIME").toLocalTime(),
    )
  }

  fun getCreatedByUserOffenderContact(offenderContactPersonId: Long): String? = jdbcTemplate.queryForObject(
    """
        SELECT CREATE_USER_ID
        FROM OFFENDER_CONTACT_PERSONS
        WHERE OFFENDER_CONTACT_PERSON_ID = :offenderContactPersonId
    """,
    MapSqlParameterSource().addValue("offenderContactPersonId", offenderContactPersonId),
  ) { resultSet: ResultSet, _: Int -> resultSet.getString("CREATE_USER_ID") }

  fun getModifiedByUserOffenderContact(offenderContactPersonId: Long): String? = jdbcTemplate.queryForObject(
    """
        SELECT MODIFY_USER_ID
        FROM OFFENDER_CONTACT_PERSONS
        WHERE OFFENDER_CONTACT_PERSON_ID = :offenderContactPersonId
    """,
    MapSqlParameterSource().addValue("offenderContactPersonId", offenderContactPersonId),
  ) { resultSet: ResultSet, _: Int -> resultSet.getString("MODIFY_USER_ID") }

  fun getExceptionMessageIds(
    exceptionQueue: String,
    enqueuedBefore: LocalDate? = null,
    pageSize: Int = 500,
  ): List<String> = jdbcTemplate.query(
    """
        SELECT MSGID
        FROM XTAG.XTAG_LISTENER_TAB
        WHERE Q_NAME = '$EXCEPTION_QUEUE_NAME'
          AND EXCEPTION_QUEUE = :exceptionQueue
          AND (:enqueuedBefore is null OR ENQ_TIME < :enqueuedBefore)
          AND ROWNUM <= :pageSize
        ORDER BY ENQ_TIME
    """.trimIndent(),
    MapSqlParameterSource()
      .addValue("exceptionQueue", exceptionQueue)
      .addValue("enqueuedBefore", enqueuedBefore, Types.TIMESTAMP)
      .addValue("pageSize", pageSize),
  ) { resultSet: ResultSet, _: Int ->
    resultSet.getBytes("MSGID").let {
      StringBuilder()
        .apply {
          for (b in it) {
            append(String.format("%02X", b))
          }
        }
        .run { "ID:$this" }
    }
  }

  fun getExceptionMessageCount(exceptionQueue: String): Int? = jdbcTemplate.queryForObject(
    """
        SELECT COUNT(MSGID)
        FROM XTAG.XTAG_LISTENER_TAB
        WHERE Q_NAME = '$EXCEPTION_QUEUE_NAME'
          AND EXCEPTION_QUEUE = :exceptionQueue
    """.trimIndent(),
    MapSqlParameterSource().addValue("exceptionQueue", exceptionQueue),
    Int::class.java,
  )

  fun getMessageCount(queueName: String): Int? = jdbcTemplate.queryForObject(
    """
      SELECT COUNT(*)
      FROM XTAG.XTAG_LISTENER_TAB
      WHERE Q_NAME = :queueName 
      AND STATE = 0
    """,
    MapSqlParameterSource().addValue("queueName", queueName),
    Int::class.java,
  )

  fun purgeExceptionQueue() {
    jdbcTemplate.update("DELETE FROM XTAG.XTAG_LISTENER_TAB", MapSqlParameterSource())
  }
}

data class Movement(
  val offenderNo: String?,
  val fromAgency: String? = null,
  val toAgency: String? = null,
  val movementType: String? = null,
  val directionCode: String? = null,
  val movementDate: LocalDate? = null,
  val movementTime: LocalTime? = null,
)
