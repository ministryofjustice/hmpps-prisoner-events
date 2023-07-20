package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonerevents.config.EXCEPTION_QUEUE_NAME
import uk.gov.justice.digital.hmpps.prisonerevents.config.QUEUE_NAME
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalTime

const val LIMIT = 100

@Repository
class SqlRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

  fun getNomsIdFromOffender(offenderId: Long): Collection<String> {
    return jdbcTemplate.query(
      GET_OFFENDER,
      MapSqlParameterSource().addValue("offenderId", offenderId),
    ) { resultSet: ResultSet, _: Int -> resultSet.getString("OFFENDER_ID_DISPLAY") }
  }

  fun getNomsIdFromBooking(bookingId: Long): Collection<String> {
    return jdbcTemplate.query(
      GET_BOOKING,
      MapSqlParameterSource().addValue("bookingId", bookingId),
    ) { resultSet: ResultSet, _: Int -> resultSet.getString("OFFENDER_ID_DISPLAY") }
  }

  fun getMovement(bookingId: Long, sequenceNumber: Int): Collection<Movement> {
    return jdbcTemplate.query(
      GET_MOVEMENT_BY_BOOKING_AND_SEQUENCE,
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
  }

  fun getNomsIdFromRestriction(offenderPersonRestrictId: Long): Collection<String> {
    return jdbcTemplate.query(
      GET_NOMS_ID_FROM_RESTRICTION,
      MapSqlParameterSource().addValue("offenderPersonRestrictId", offenderPersonRestrictId),
    ) { resultSet: ResultSet, _: Int -> resultSet.getString("OFFENDER_ID_DISPLAY") }
  }

  fun getExceptionMessageIds(): List<String> =
    jdbcTemplate.query(GET_EXCEPTION_MESSAGES) { resultSet: ResultSet, _: Int ->
      resultSet.getBytes("MSGID").let {
        StringBuilder()
          .apply { for (b in it) { append(String.format("%02X", b)) } }
          .run { "ID:$this" }
      }
    }

  fun getExceptionCount(): Int =
    jdbcTemplate.queryForObject(GET_EXCEPTION_COUNT, MapSqlParameterSource(), Int::class.java)!!

  fun purgeExceptionQueue() {
    jdbcTemplate.update("DELETE FROM XTAG.XTAG_LISTENER_TAB", MapSqlParameterSource())
  }

  companion object {

    val GET_OFFENDER = """
      SELECT OFFENDER_ID_DISPLAY
      FROM OFFENDERS 
      WHERE OFFENDER_ID = :offenderId
    """.trimIndent()

    val GET_BOOKING = """
      SELECT OFFENDER_ID_DISPLAY
      FROM OFFENDER_BOOKINGS
        INNER JOIN OFFENDERS ON OFFENDER_BOOKINGS.OFFENDER_ID = OFFENDERS.OFFENDER_ID
      WHERE OFFENDER_BOOK_ID = :bookingId
    """.trimIndent()

    val GET_MOVEMENT_BY_BOOKING_AND_SEQUENCE = """
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
    """.trimIndent()

    val GET_NOMS_ID_FROM_RESTRICTION = """
      SELECT OFFENDER_ID_DISPLAY
      FROM OFFENDER_BOOKINGS
        INNER JOIN OFFENDERS ON OFFENDER_BOOKINGS.OFFENDER_ID = OFFENDERS.OFFENDER_ID
        JOIN OFFENDER_CONTACT_PERSONS ON OFFENDER_CONTACT_PERSONS.OFFENDER_BOOK_ID = OFFENDER_BOOKINGS.OFFENDER_BOOK_ID
        JOIN OFFENDER_PERSON_RESTRICTS ON OFFENDER_PERSON_RESTRICTS.OFFENDER_CONTACT_PERSON_ID = OFFENDER_CONTACT_PERSONS.OFFENDER_CONTACT_PERSON_ID
        WHERE OFFENDER_PERSON_RESTRICTS.OFFENDER_PERSON_RESTRICT_ID = :offenderPersonRestrictId
    """.trimIndent()

    val GET_EXCEPTION_MESSAGES = """
      SELECT MSGID
      FROM XTAG.XTAG_LISTENER_TAB
      WHERE Q_NAME = '$EXCEPTION_QUEUE_NAME'
        AND EXCEPTION_QUEUE = '$QUEUE_NAME'
        AND ROWNUM <= $LIMIT
    """.trimIndent()

    val GET_EXCEPTION_COUNT = """
      SELECT COUNT(1)
      FROM XTAG.XTAG_LISTENER_TAB
      WHERE Q_NAME = '$EXCEPTION_QUEUE_NAME'
        AND EXCEPTION_QUEUE = '$QUEUE_NAME'
    """.trimIndent()
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
