package uk.gov.justice.digital.hmpps.prisonerevents.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerevents.config.ErrorResponse
import uk.gov.justice.digital.hmpps.prisonerevents.service.AQService

@RestController
@Validated
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AQResource(private val aqService: AQService) {

  @PreAuthorize("hasRole('ROLE_QUEUE_ADMIN')")
  @GetMapping("/housekeeping")
  @Operation(
    summary = "Retry messages on the Oracle AQ exception queue",
    description = "Retrieve any messages on the Oracle AQ exception queue and move them back to the main queue to be retried.",
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
  fun requeueExceptions() =
    aqService.requeueExceptions()
}
