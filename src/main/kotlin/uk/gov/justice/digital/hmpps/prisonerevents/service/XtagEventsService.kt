package uk.gov.justice.digital.hmpps.prisonerevents.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerevents.model.BookingNumberChangedType
import uk.gov.justice.digital.hmpps.prisonerevents.model.CourtEventChargeLinkingEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.ExternalMovementOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.GenericOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingNumberChangeOrMergeEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderBookingReassignedEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderContactEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.OffenderPhoneNumberEvent
import uk.gov.justice.digital.hmpps.prisonerevents.model.PersonRestrictionOffenderEvent
import uk.gov.justice.digital.hmpps.prisonerevents.repository.ExposeRepository
import uk.gov.justice.digital.hmpps.prisonerevents.repository.SqlRepository

@Service
class XtagEventsService(
  private val sqlRepository: SqlRepository,
  private val exposeRepository: ExposeRepository,
) {
  @Transactional
  fun addAdditionalEventData(oe: OffenderEvent?): OffenderEvent? {
    when (oe?.eventType) {
      "OFFENDER_DETAILS-CHANGED", "OFFENDER_ALIAS-CHANGED", "OFFENDER-UPDATED",
      "ADDRESSES_OFFENDER-INSERTED", "ADDRESSES_OFFENDER-UPDATED", "ADDRESSES_OFFENDER-DELETED",
      ->
        oe.offenderIdDisplay = sqlRepository.getNomsIdFromOffender(oe.offenderId!!).firstOrNull()

      "BED_ASSIGNMENT_HISTORY-INSERTED", "OFFENDER_MOVEMENT-DISCHARGE", "OFFENDER_MOVEMENT-RECEPTION",
      "CONFIRMED_RELEASE_DATE-CHANGED", "SENTENCE_DATES-CHANGED",
      "OFFENDER_CASE_NOTES-INSERTED", "OFFENDER_CASE_NOTES-UPDATED", "OFFENDER_CASE_NOTES-DELETED",
      "ALERT-UPDATED", "ALERT-INSERTED", "ALERT-DELETED",
      "OFFENDER_MARKS_IMAGE-CREATED", "OFFENDER_MARKS_IMAGE-UPDATED", "OFFENDER_MARKS_IMAGE-DELETED",
      "OFFENDER_IMAGE-CREATED", "OFFENDER_IMAGE-UPDATED", "OFFENDER_IMAGE-DELETED",
      ->
        oe.offenderIdDisplay = oe.bookingId?.let { sqlRepository.getNomsIdFromBooking(it).firstOrNull() }

      "EXTERNAL_MOVEMENT_RECORD-INSERTED", "EXTERNAL_MOVEMENT-CHANGED" -> {
        oe as ExternalMovementOffenderEvent
        sqlRepository.getMovement(oe.bookingId!!, oe.movementSeq!!.toInt())
          .firstOrNull()
          ?.apply {
            oe.offenderIdDisplay = this.offenderNo
            oe.fromAgencyLocationId = this.fromAgency
            oe.toAgencyLocationId = this.toAgency
            oe.directionCode = this.directionCode
            oe.movementDateTime =
              if (this.movementTime != null && this.movementDate != null) this.movementTime.atDate(this.movementDate) else null
            oe.movementType = this.movementType
          }
        // For deleted movements, there is no record to retrieve but we can still find the prisoner number
        if (oe.eventType == "EXTERNAL_MOVEMENT-CHANGED" && oe.recordDeleted!!) {
          oe.offenderIdDisplay = sqlRepository.getNomsIdFromBooking(oe.bookingId!!)
            .firstOrNull()
            .also {
              oe.offenderIdDisplay = it
            }
        }
      }

      "PERSON_RESTRICTION-UPSERTED", "PERSON_RESTRICTION-DELETED" -> {
        oe as PersonRestrictionOffenderEvent
        oe.offenderIdDisplay = exposeRepository.getNomsIdFromContact(oe.contactPersonId!!)
        oe.personId = exposeRepository.getPersonIdFromContact(oe.contactPersonId)
      }

      "OFFENDER_BOOKING-REASSIGNED" -> {
        oe as OffenderBookingReassignedEvent
        oe.offenderIdDisplay = sqlRepository.getNomsIdFromOffender(oe.offenderId!!).firstOrNull()
        oe.previousOffenderIdDisplay = sqlRepository.getNomsIdFromOffender(oe.previousOffenderId).firstOrNull()
        val dates = exposeRepository.getBookingStartAndEndDateForOffenderBooking(oe.bookingId!!)
        oe.bookingStartDateTime = dates?.first
        oe.bookingEndDateTime = dates?.second
        oe.lastAdmissionDate = exposeRepository.getLastAdmissionDateForOffenderBooking(oe.bookingId!!)
      }

      "OFFENDER_CONTACT-INSERTED" -> {
        oe as OffenderContactEvent
        oe.username = sqlRepository.getCreatedByUserOffenderContact(oe.contactId)
      }

      "OFFENDER_CONTACT-UPDATED" -> {
        oe as OffenderContactEvent
        oe.username = sqlRepository.getModifiedByUserOffenderContact(oe.contactId)
      }

      "COURT_EVENT_CHARGES-LINKED", "COURT_EVENT_CHARGES-UNLINKED" -> {
        oe as CourtEventChargeLinkingEvent
        oe.bookingId = exposeRepository.getBookingIdFromChargeId(oe.chargeId!!)
        oe.offenderIdDisplay = oe.bookingId?.let { sqlRepository.getNomsIdFromBooking(it).firstOrNull() }
      }

      "BOOKING_NUMBER-CHANGED" -> {
        oe as OffenderBookingNumberChangeOrMergeEvent
        if (oe.nomisEventType == "P1_RESULT") {
          oe.type = BookingNumberChangedType.BOOK_NUMBER_CHANGE
        } else {
          // BOOK_UPD_OASYS is fired both for a merge and a booking number change
          // so look for a very recent merge
          exposeRepository.findRelatedMerge(oe.bookingId!!, oe.eventDatetime!!)?.also {
            val retainedOffender = exposeRepository.getOffenderById(it.offenderId2!!) ?: throw IllegalStateException("No OFFENDERS record found for ${it.offenderId2}")
            val retainedOffenderNo = retainedOffender.offenderNo
            val removedOffenderNo = if (retainedOffender.offenderNo == it.offenderNo2) it.offenderNo1 else it.offenderNo2
            oe.type = BookingNumberChangedType.MERGE
            oe.offenderIdDisplay = retainedOffenderNo
            oe.previousOffenderIdDisplay = removedOffenderNo
          } ?: run {
            oe.type = BookingNumberChangedType.BOOK_NUMBER_CHANGE_DUPLICATE
          }
        }
      }

      "OFFENDER_ADDRESS_PHONE-INSERTED", "OFFENDER_ADDRESS_PHONE-UPDATED", "OFFENDER_ADDRESS_PHONE-DELETED" -> {
        oe as OffenderPhoneNumberEvent
        oe.offenderId = exposeRepository.getRootOffenderByPrisonNumber(oe.offenderIdDisplay!!)
      }

      "ADDRESS_USAGE-INSERTED", "ADDRESS_USAGE-UPDATED", "ADDRESS_USAGE-DELETED" -> {
        oe as GenericOffenderEvent
        // Ignore DISC dodgy data that doesn't have a related reference code
        if (oe.addressUsage == "DISC") return null

        // Only offender addresses are of interest - any other owner type (eg. agency, corporate, person) is discarded
        return exposeRepository.getRootOffenderIdAndPrisonNumberFromAddressId(oe.addressId!!)?.let { (offenderId, offenderIdDisplay) ->
          GenericOffenderEvent(
            eventType = oe.eventType.replace("ADDRESS_USAGE", "OFFENDER_ADDRESS_USAGE"),
            eventDatetime = oe.eventDatetime,
            nomisEventType = oe.nomisEventType,
            offenderId = offenderId,
            offenderIdDisplay = offenderIdDisplay,
            addressId = oe.addressId,
            addressUsage = oe.addressUsage,
          )
        }
      }
    }
    return oe
  }
}
