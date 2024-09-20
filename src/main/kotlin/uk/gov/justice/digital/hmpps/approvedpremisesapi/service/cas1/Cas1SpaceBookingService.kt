package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1

import kotlinx.datetime.toKotlinDatePeriod
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1NewArrival
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingResidency
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingSummarySortField
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas1SpaceBookingEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas1SpaceBookingRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas1SpaceBookingSearchResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserPermission
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas1.Cas1SpaceSearchRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.PaginationMetadata
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.validatedCasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequestService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PremisesService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.PageCriteria
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.getMetadata
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class Cas1SpaceBookingService(
  private val premisesService: PremisesService,
  private val placementRequestService: PlacementRequestService,
  private val cas1SpaceBookingRepository: Cas1SpaceBookingRepository,
  private val cas1SpaceSearchRepository: Cas1SpaceSearchRepository,
  private val cas1BookingDomainEventService: Cas1BookingDomainEventService,
  private val cas1BookingEmailService: Cas1BookingEmailService,
  private val cas1SpaceBookingManagementDomainEventService: Cas1SpaceBookingManagementDomainEventService,
  private val cas1ApplicationStatusService: Cas1ApplicationStatusService,
) {
  @Transactional
  fun createNewBooking(
    premisesId: UUID,
    placementRequestId: UUID,
    arrivalDate: LocalDate,
    departureDate: LocalDate,
    createdBy: UserEntity,
  ): CasResult<Cas1SpaceBookingEntity> = validatedCasResult {
    val premises = premisesService.getApprovedPremises(premisesId)
    if (premises == null) {
      "$.premisesId" hasValidationError "doesNotExist"
    }

    val placementRequest = placementRequestService.getPlacementRequestOrNull(placementRequestId)
    if (placementRequest == null) {
      "$.placementRequestId" hasValidationError "doesNotExist"
    }

    if (arrivalDate >= departureDate) {
      "$.departureDate" hasValidationError "shouldBeAfterArrivalDate"
    }

    if (validationErrors.any()) {
      return fieldValidationError
    }

    premises!!
    placementRequest!!

    when (val existingBooking = cas1SpaceBookingRepository.findByPremisesIdAndPlacementRequestId(premisesId, placementRequestId)) {
      null -> {}
      else -> return existingBooking.id hasConflictError "A Space Booking already exists for this premises and placement request"
    }

    val durationInDays = arrivalDate.until(departureDate).toKotlinDatePeriod().days
    cas1SpaceSearchRepository.getSpaceAvailabilityForCandidatePremises(listOf(premisesId), arrivalDate, durationInDays)

    val application = placementRequest.application

    val spaceBooking = cas1SpaceBookingRepository.save(
      Cas1SpaceBookingEntity(
        id = UUID.randomUUID(),
        premises = premises,
        application = application,
        placementRequest = placementRequest,
        createdBy = createdBy,
        createdAt = OffsetDateTime.now(),
        expectedArrivalDate = arrivalDate,
        expectedDepartureDate = departureDate,
        actualArrivalDateTime = null,
        actualDepartureDateTime = null,
        canonicalArrivalDate = arrivalDate,
        canonicalDepartureDate = departureDate,
        crn = placementRequest.application.crn,
        keyWorkerStaffCode = null,
        keyWorkerName = null,
        keyWorkerAssignedAt = null,
        cancellationOccurredAt = null,
        cancellationRecordedAt = null,
        cancellationReason = null,
        cancellationReasonNotes = null,
      ),
    )

    cas1ApplicationStatusService.spaceBookingMade(spaceBooking)

    cas1BookingDomainEventService.spaceBookingMade(
      application = application,
      booking = spaceBooking,
      user = createdBy,
      placementRequest = placementRequest,
    )

    cas1BookingEmailService.spaceBookingMade(
      spaceBooking = spaceBooking,
      application = application,
      placementApplication = placementRequest.placementApplication,
    )

    success(spaceBooking)
  }

  @Transactional
  fun recordArrivalForBooking(
    premisesId: UUID,
    bookingId: UUID,
    cas1NewArrival: Cas1NewArrival,
  ): CasResult<Cas1SpaceBookingEntity> = validatedCasResult {
    val premises = premisesService.getApprovedPremises(premisesId)
    if (premises == null) {
      "$.premisesId" hasValidationError "doesNotExist"
    }

    val existingCas1SpaceBooking = cas1SpaceBookingRepository.findByIdOrNull(bookingId)
    if (existingCas1SpaceBooking == null) {
      "$.bookingId" hasValidationError "doesNotExist"
    }
    if (validationErrors.any()) {
      return fieldValidationError
    }

    existingCas1SpaceBooking!!

    if (existingCas1SpaceBooking.actualArrivalDateTime != null) {
      return existingCas1SpaceBooking.id hasConflictError "An arrival is already recorded for this Space Booking"
    }

    val previousExpectedDepartureOn =
      if (existingCas1SpaceBooking.expectedDepartureDate != cas1NewArrival.expectedDepartureDate) {
        existingCas1SpaceBooking.expectedDepartureDate
      } else {
        null
      }

    val updatedCas1SpaceBooking = existingCas1SpaceBooking.copy(
      actualArrivalDateTime = cas1NewArrival.arrivalDateTime,
      canonicalArrivalDate = LocalDate.ofInstant(cas1NewArrival.arrivalDateTime, ZoneId.systemDefault()),
      expectedDepartureDate = cas1NewArrival.expectedDepartureDate,
      canonicalDepartureDate = cas1NewArrival.expectedDepartureDate,
    )

    val result = cas1SpaceBookingRepository.save(updatedCas1SpaceBooking)

    cas1SpaceBookingManagementDomainEventService.arrivalRecorded(
      updatedCas1SpaceBooking,
      previousExpectedDepartureOn,
    )

    success(result)
  }

  fun search(
    premisesId: UUID,
    filterCriteria: SpaceBookingFilterCriteria,
    pageCriteria: PageCriteria<Cas1SpaceBookingSummarySortField>,
  ): CasResult<Pair<List<Cas1SpaceBookingSearchResult>, PaginationMetadata?>> {
    if (premisesService.getApprovedPremises(premisesId) == null) return CasResult.NotFound("premises", premisesId.toString())

    val page = cas1SpaceBookingRepository.search(
      filterCriteria.residency?.name,
      filterCriteria.crnOrName,
      premisesId,
      pageCriteria.toPageableOrAllPages(
        sortByConverter = when (pageCriteria.sortBy) {
          Cas1SpaceBookingSummarySortField.personName -> "personName"
          Cas1SpaceBookingSummarySortField.canonicalArrivalDate -> "canonicalArrivalDate"
          Cas1SpaceBookingSummarySortField.canonicalDepartureDate -> "canonicalDepartureDate"
          Cas1SpaceBookingSummarySortField.keyWorkerName -> "keyWorkerName"
          Cas1SpaceBookingSummarySortField.tier -> "tier"
        },
      ),
    )

    return CasResult.Success(
      Pair(
        page.toList(),
        getMetadata(page, pageCriteria),
      ),
    )
  }

  fun getBooking(premisesId: UUID, bookingId: UUID): CasResult<Cas1SpaceBookingEntity> {
    if (premisesService.getApprovedPremises(premisesId) !is ApprovedPremisesEntity) return CasResult.NotFound("premises", premisesId.toString())

    val booking = cas1SpaceBookingRepository.findByIdOrNull(bookingId) ?: return CasResult.NotFound("booking", bookingId.toString())

    return CasResult.Success(booking)
  }

  fun getBookingsForPremisesAndCrn(premisesId: UUID, crn: String) = cas1SpaceBookingRepository.findByPremisesIdAndCrn(
    premisesId = premisesId,
    crn = crn,
  )

  fun getWithdrawableState(spaceBooking: Cas1SpaceBookingEntity, user: UserEntity): WithdrawableState {
    return WithdrawableState(
      withdrawable = !spaceBooking.isCancelled() && !spaceBooking.hasArrival(),
      withdrawn = spaceBooking.isCancelled(),
      userMayDirectlyWithdraw = user.hasPermission(UserPermission.CAS1_BOOKING_WITHDRAW),
      blockingReason = if (spaceBooking.hasArrival()) {
        BlockingReason.ArrivalRecordedInCas1
      } else {
        null
      },
    )
  }

  data class SpaceBookingFilterCriteria(
    val residency: Cas1SpaceBookingResidency?,
    val crnOrName: String?,
  )
}
