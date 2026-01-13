package uk.gov.justice.digital.hmpps.prisonerevents.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AQResource(private val aqService: AQService) {

  @PutMapping("/housekeeping")
  @Operation(
    summary = "Retry messages on the Oracle AQ exception queue",
    description = "Retrieve any messages on the Oracle AQ exception queue and move them back to the main queue to be retried.",
    security = [SecurityRequirement(name = "queue-admin-role")],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "success",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class)),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint when role not present",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class)),
        ],
      ),
    ],
  )
  fun requeueExceptions(
    @RequestParam(defaultValue = "500")
    @Parameter(description = "Total messages to requeue", example = "2000", required = false)
    pageSize: Int,
  ) = aqService.requeueExceptions(pageSize)

  @DeleteMapping("/exceptions/{originalQueue}")
  @PreAuthorize("hasRole('ROLE_QUEUE_ADMIN')")
  @Operation(
    summary = "Discards messages on the Oracle AQ exception queue",
    description = "Dequeue messages on the Oracle AQ exception queue for the specified original queue.",
    security = [SecurityRequirement(name = "queue-admin-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "success"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class)),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint when role not present",
        content = [
          Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class)),
        ],
      ),
    ],
  )
  fun dequeueExceptions(
    @PathVariable
    @Schema(description = "Original queue name", example = "XTAG_UPD_OFFENDERS", required = true)
    originalQueue: String,
    @RequestParam
    @Parameter(
      description = "Only include messages enqueued before this date time",
      example = "2021-03-23",
      required = true,
    )
    onlyBefore: LocalDate,
  ) = aqService.dequeueExceptions(originalQueue, onlyBefore)
}
