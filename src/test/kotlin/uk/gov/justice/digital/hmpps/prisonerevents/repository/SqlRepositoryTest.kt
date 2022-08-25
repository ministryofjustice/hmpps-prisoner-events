package uk.gov.justice.digital.hmpps.prisonerevents.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.prisonerevents.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalTime

class SqlRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @Autowired
  private lateinit var repository: SqlRepository

  @BeforeEach
  fun setup() {
    jdbcTemplate.update("delete from OFFENDER_EXTERNAL_MOVEMENTS")
    jdbcTemplate.update("delete from OFFENDER_BOOKINGS")
    jdbcTemplate.update("delete from OFFENDERS")
  }

  @Test
  fun getNomsIdFromOffender() {
    seedOffenders()

    val data = repository.getNomsIdFromOffender(12L)
    assertThat(data.first()).isEqualTo("A1234AA")
    assertThat(data).hasSize(1)

    assertThat(repository.getNomsIdFromOffender(13L)).isEmpty()
  }

  @Test
  fun getNomsIdFromBooking() {
    seedOffenders()
    seedBookings()

    val data = repository.getNomsIdFromBooking(1234L)
    assertThat(data.first()).isEqualTo("A1234AA")
    assertThat(data).hasSize(1)

    assertThat(repository.getNomsIdFromBooking(2468L)).isEmpty()
  }

  @Test
  fun getMovement() {
    seedOffenders()
    seedBookings()
    seedMovements()

    val data = repository.getMovement(1234L, 5)
    assertThat(data.first()).isEqualTo(
      Movement(
        offenderNo = "A1234AA",
        fromAgency = "AAA",
        toAgency = "BBB",
        movementType = "TRN",
        directionCode = "OUT",
        movementDate = LocalDate.parse("2022-08-24"),
        movementTime = LocalTime.parse("13:45:00")
      )
    )
    assertThat(data).hasSize(1)

    assertThat(repository.getMovement(2468L, 5)).isEmpty()
  }

  private fun seedOffenders() {
    jdbcTemplate.update(
      """insert into OFFENDERS(
          OFFENDER_ID,  
          ID_SOURCE_CODE,
          LAST_NAME,
          SEX_CODE,
          CREATE_DATE,
          LAST_NAME_KEY,
          OFFENDER_ID_DISPLAY
        ) values (
          12, 
          'source',
          'SMITH',
          'M',
          SYSDATE,
          'key',
          'A1234AA'
          )"""
    )
  }

  private fun seedBookings() {
    jdbcTemplate.update(
      """insert into OFFENDER_BOOKINGS(
          OFFENDER_ID,  
          OFFENDER_BOOK_ID,
          BOOKING_BEGIN_DATE,
          IN_OUT_STATUS,
          YOUTH_ADULT_CODE,
          BOOKING_SEQ
        ) values (
          12, 
          1234,
          SYSDATE,
          'IN',
          'CODE',
          1)"""
    )
  }

  private fun seedMovements() {
    jdbcTemplate.update(
      """insert into OFFENDER_EXTERNAL_MOVEMENTS(
          OFFENDER_BOOK_ID,
          MOVEMENT_SEQ,
          MOVEMENT_DATE,
          MOVEMENT_TIME,
          FROM_AGY_LOC_ID,
          TO_AGY_LOC_ID,
          DIRECTION_CODE,
          MOVEMENT_TYPE
        ) values (
          1234,
          5,
          TO_DATE('2022/08/24', 'YYYY/MM/DD'),
          TO_DATE('13:45', 'HH24:MI'),
          'AAA',
          'BBB',
          'OUT',
          'TRN')"""
    )
  }
}
