package uk.gov.justice.digital.hmpps.approvedpremisesapi.service

import arrow.core.Either
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.DateCapacity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PropertyStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BedRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LostBedCancellationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LostBedCancellationRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LostBedReasonRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LostBedsEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LostBedsRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesWithBedCount
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ProbationDeliveryUnitEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ProbationDeliveryUnitRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ProbationRegionRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.RoomRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.TemporaryAccommodationPremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.TemporaryAccommodationPremisesSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.Availability
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.ValidationErrors
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.validated
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.ValidatableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.getDaysUntilExclusiveEnd
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PremisesService(
  private val premisesRepository: PremisesRepository,
  private val lostBedsRepository: LostBedsRepository,
  private val bookingRepository: BookingRepository,
  private val lostBedReasonRepository: LostBedReasonRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
  private val probationRegionRepository: ProbationRegionRepository,
  private val lostBedCancellationRepository: LostBedCancellationRepository,
  private val probationDeliveryUnitRepository: ProbationDeliveryUnitRepository,
  private val characteristicService: CharacteristicService,
  private val roomRepository: RoomRepository,
  private val bedRepository: BedRepository,
  private val timeService: TimeService,
) {
  private val serviceNameToEntityType = mapOf(
    ServiceName.approvedPremises to ApprovedPremisesEntity::class.java,
    ServiceName.temporaryAccommodation to TemporaryAccommodationPremisesEntity::class.java,
  )

  fun getAllPremises(): List<PremisesWithBedCount> = premisesRepository.findAllWithBedCount()

  fun getAllTemporaryAccommodationPremisesSummaries(regionId: UUID): List<TemporaryAccommodationPremisesSummary> {
    return premisesRepository.findAllTemporaryAccommodationSummary(regionId)
  }

  fun getAllApprovedPremisesSummaries(probationRegionId: UUID?, apAreaId: UUID?): List<ApprovedPremisesSummary> {
    return premisesRepository.findAllApprovedPremisesSummary(probationRegionId, apAreaId)
  }

  fun getAllPremisesInRegion(probationRegionId: UUID): List<PremisesWithBedCount> = premisesRepository.findAllByProbationRegion(probationRegionId)

  fun getAllPremisesForService(service: ServiceName): List<PremisesWithBedCount> = serviceNameToEntityType[service]?.let {
    premisesRepository.findAllByType(it)
  } ?: listOf()

  fun getAllPremisesInRegionForService(
    probationRegionId: UUID,
    service: ServiceName,
  ): List<PremisesWithBedCount> = serviceNameToEntityType[service]?.let {
    premisesRepository.findAllByProbationRegionAndType(probationRegionId, it)
  } ?: listOf()

  fun getPremises(premisesId: UUID): PremisesEntity? = premisesRepository.findByIdOrNull(premisesId)

  fun getPremisesSummary(premisesId: UUID): List<BookingSummary> = premisesRepository.getBookingSummariesForPremisesId(premisesId)

  fun getLastBookingDate(premises: PremisesEntity) = bookingRepository.getHighestBookingDate(premises.id)
  fun getLastLostBedsDate(premises: PremisesEntity) = lostBedsRepository.getHighestBookingDate(premises.id)

  fun getAvailabilityForRange(
    premises: PremisesEntity,
    startDate: LocalDate,
    endDate: LocalDate,
  ): Map<LocalDate, Availability> {
    if (endDate.isBefore(startDate)) throw RuntimeException("startDate must be before endDate when calculating availability for range")

    val bookings = bookingRepository.findAllByPremisesIdAndOverlappingDate(premises.id, startDate, endDate)
    val lostBeds = lostBedsRepository.findAllByPremisesIdAndOverlappingDate(premises.id, startDate, endDate)

    return startDate.getDaysUntilExclusiveEnd(endDate).map { date ->
      val bookingsOnDay = bookings.filter { booking -> booking.getArrivalDate() <= date && booking.getDepartureDate() > date }
      val lostBedsOnDay = lostBeds.filter { lostBed -> lostBed.startDate <= date && lostBed.endDate > date && lostBed.cancellation == null }

      Availability(
        date = date,
        pendingBookings = bookingsOnDay.count { !it.getArrived() && !it.getIsNotArrived() && !it.getCancelled() },
        arrivedBookings = bookingsOnDay.count { it.getArrived() },
        nonArrivedBookings = bookingsOnDay.count { it.getIsNotArrived() },
        cancelledBookings = bookingsOnDay.count { it.getCancelled() },
        lostBeds = lostBedsOnDay.size,
      )
    }.associateBy { it.date }
  }

  fun createLostBeds(
    premises: PremisesEntity,
    startDate: LocalDate,
    endDate: LocalDate,
    reasonId: UUID,
    referenceNumber: String?,
    notes: String?,
    bedId: UUID,
  ): ValidatableActionResult<LostBedsEntity> =
    validated {
      if (endDate.isBefore(startDate)) {
        "$.endDate" hasValidationError "beforeStartDate"
      }

      val bed = premises.rooms.flatMap { it.beds }.firstOrNull { it.id == bedId }
      if (bed == null) {
        "$.bedId" hasValidationError "doesNotExist"
      }

      val reason = lostBedReasonRepository.findByIdOrNull(reasonId)
      if (reason == null) {
        "$.reason" hasValidationError "doesNotExist"
      } else if (!serviceScopeMatches(reason.serviceScope, premises)) {
        "$.reason" hasValidationError "incorrectLostBedReasonServiceScope"
      }

      if (validationErrors.any()) {
        return fieldValidationError
      }

      val lostBedsEntity = lostBedsRepository.save(
        LostBedsEntity(
          id = UUID.randomUUID(),
          premises = premises,
          startDate = startDate,
          endDate = endDate,
          bed = bed!!,
          reason = reason!!,
          referenceNumber = referenceNumber,
          notes = notes,
          cancellation = null,
        ),
      )

      return success(lostBedsEntity)
    }

  fun updateLostBeds(
    lostBedId: UUID,
    startDate: LocalDate,
    endDate: LocalDate,
    reasonId: UUID,
    referenceNumber: String?,
    notes: String?,
  ): AuthorisableActionResult<ValidatableActionResult<LostBedsEntity>> {
    val lostBed = lostBedsRepository.findByIdOrNull(lostBedId)
      ?: return AuthorisableActionResult.NotFound()

    return AuthorisableActionResult.Success(
      validated {
        if (endDate.isBefore(startDate)) {
          "$.endDate" hasValidationError "beforeStartDate"
        }

        val reason = lostBedReasonRepository.findByIdOrNull(reasonId)
        if (reason == null) {
          "$.reason" hasValidationError "doesNotExist"
        } else if (!serviceScopeMatches(reason.serviceScope, lostBed.premises)) {
          "$.reason" hasValidationError "incorrectLostBedReasonServiceScope"
        }

        if (validationErrors.any()) {
          return@validated fieldValidationError
        }

        val updatedLostBedsEntity = lostBedsRepository.save(
          lostBed.apply {
            this.startDate = startDate
            this.endDate = endDate
            this.reason = reason!!
            this.referenceNumber = referenceNumber
            this.notes = notes
          },
        )

        success(updatedLostBedsEntity)
      },
    )
  }

  fun cancelLostBed(
    lostBed: LostBedsEntity,
    notes: String?,
  ) = validated<LostBedCancellationEntity> {
    if (lostBed.cancellation != null) {
      return generalError("This Lost Bed already has a cancellation set")
    }

    val cancellationEntity = lostBedCancellationRepository.save(
      LostBedCancellationEntity(
        id = UUID.randomUUID(),
        lostBed = lostBed,
        notes = notes,
        createdAt = OffsetDateTime.now(),
      ),
    )

    return success(cancellationEntity)
  }

  fun createNewPremises(
    addressLine1: String,
    addressLine2: String?,
    town: String?,
    postcode: String,
    latitude: Double?,
    longitude: Double?,
    service: String,
    localAuthorityAreaId: UUID?,
    probationRegionId: UUID,
    name: String,
    notes: String?,
    characteristicIds: List<UUID>,
    status: PropertyStatus,
    probationDeliveryUnitIdentifier: Either<String, UUID>?,
    turnaroundWorkingDayCount: Int?,
  ) = validated {
    val probationRegion = probationRegionRepository.findByIdOrNull(probationRegionId)
    if (probationRegion == null) {
      "$.probationRegionId" hasValidationError "doesNotExist"
    }

    val localAuthorityArea = if (localAuthorityAreaId == null) {
      if (service == ServiceName.approvedPremises.value) {
        "$.localAuthorityAreaId" hasValidationError "empty"
      }
      null
    } else {
      val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId)
      if (localAuthorityArea == null) {
        "$.localAuthorityAreaId" hasValidationError "doesNotExist"
      }
      localAuthorityArea
    }

    // start of validation
    if (addressLine1.isEmpty()) {
      "$.address" hasValidationError "empty"
    }

    if (postcode.isEmpty()) {
      "$.postcode" hasValidationError "empty"
    }

    if (service.isEmpty()) {
      "$.service" hasValidationError "empty"
    } else if (service != ServiceName.temporaryAccommodation.value) {
      "$.service" hasValidationError "onlyCas3Supported"
    }

    if (name.isEmpty()) {
      "$.name" hasValidationError "empty"
    }

    if (!premisesRepository.nameIsUniqueForType(name, TemporaryAccommodationPremisesEntity::class.java)) {
      "$.name" hasValidationError "notUnique"
    }

    val probationDeliveryUnit = tryGetProbationDeliveryUnit(probationDeliveryUnitIdentifier, probationRegionId) { property, err ->
      property hasValidationError err
    }

    if (turnaroundWorkingDayCount != null && turnaroundWorkingDayCount < 0) {
      "$.turnaroundWorkingDayCount" hasValidationError "isNotAPositiveInteger"
    }

    if (validationErrors.any()) {
      return fieldValidationError
    }

    val premises = TemporaryAccommodationPremisesEntity(
      id = UUID.randomUUID(),
      name = name,
      addressLine1 = addressLine1,
      addressLine2 = addressLine2,
      town = town,
      postcode = postcode,
      latitude = null,
      longitude = null,
      probationRegion = probationRegion!!,
      localAuthorityArea = localAuthorityArea,
      bookings = mutableListOf(),
      lostBeds = mutableListOf(),
      notes = if (notes.isNullOrEmpty()) "" else notes,
      emailAddress = null,
      rooms = mutableListOf(),
      characteristics = mutableListOf(),
      status = status,
      probationDeliveryUnit = probationDeliveryUnit!!,
      turnaroundWorkingDayCount = turnaroundWorkingDayCount ?: 2,
    )

    val characteristicEntities = characteristicIds.mapIndexed { index, uuid ->
      val entity = characteristicService.getCharacteristic(uuid)

      if (entity == null) {
        "$.characteristics[$index]" hasValidationError "doesNotExist"
      } else {
        if (!characteristicService.modelScopeMatches(entity, premises)) {
          "$.characteristics[$index]" hasValidationError "incorrectCharacteristicModelScope"
        }
        if (!characteristicService.serviceScopeMatches(entity, premises)) {
          "$.characteristics[$index]" hasValidationError "incorrectCharacteristicServiceScope"
        }
      }

      entity
    }

    if (validationErrors.any()) {
      return fieldValidationError
    }
    // end of validation
    premises.characteristics.addAll(characteristicEntities.map { it!! })
    premisesRepository.save(premises)

    return success(premises)
  }

  fun updatePremises(
    premisesId: UUID,
    addressLine1: String,
    addressLine2: String?,
    town: String?,
    postcode: String,
    localAuthorityAreaId: UUID?,
    probationRegionId: UUID,
    characteristicIds: List<UUID>,
    notes: String?,
    status: PropertyStatus,
    probationDeliveryUnitIdentifier: Either<String, UUID>?,
    turnaroundWorkingDayCount: Int?,
  ): AuthorisableActionResult<ValidatableActionResult<PremisesEntity>> {
    val premises = premisesRepository.findByIdOrNull(premisesId)
      ?: return AuthorisableActionResult.NotFound()

    val validationErrors = ValidationErrors()

    val localAuthorityArea = if (localAuthorityAreaId == null) {
      if (premises is ApprovedPremisesEntity) {
        validationErrors["$.localAuthorityAreaId"] = "empty"
      }
      null
    } else {
      val localAuthorityArea = localAuthorityAreaRepository.findByIdOrNull(localAuthorityAreaId)
      if (localAuthorityArea == null) {
        validationErrors["$.localAuthorityAreaId"] = "doesNotExist"
      }
      localAuthorityArea
    }

    val probationRegion = probationRegionRepository.findByIdOrNull(probationRegionId)

    if (probationRegion == null) {
      validationErrors["$.probationRegionId"] = "doesNotExist"
    }

    val probationDeliveryUnit = when (premises) {
      is TemporaryAccommodationPremisesEntity -> {
        tryGetProbationDeliveryUnit(probationDeliveryUnitIdentifier, probationRegionId) { property, err ->
          validationErrors[property] = err
        }
      }
      else -> null
    }

    val characteristicEntities = characteristicIds.mapIndexed { index, uuid ->
      val entity = characteristicService.getCharacteristic(uuid)

      if (entity == null) {
        validationErrors["$.characteristics[$index]"] = "doesNotExist"
      } else {
        if (!characteristicService.modelScopeMatches(entity, premises)) {
          validationErrors["$.characteristics[$index]"] = "incorrectCharacteristicModelScope"
        }
        if (!characteristicService.serviceScopeMatches(entity, premises)) {
          validationErrors["$.characteristics[$index]"] = "incorrectCharacteristicServiceScope"
        }
      }

      entity
    }

    if (turnaroundWorkingDayCount != null) {
      when (premises) {
        is TemporaryAccommodationPremisesEntity -> {
          if (turnaroundWorkingDayCount < 0) {
            validationErrors["$.turnaroundWorkingDayCount"] = "isNotAPositiveInteger"
          }
        }

        else -> validationErrors["$.turnaroundWorkingDayCount"] = "onlyAppliesToTemporaryAccommodationPremises"
      }
    }

    if (validationErrors.any()) {
      return AuthorisableActionResult.Success(
        ValidatableActionResult.FieldValidationError(validationErrors),
      )
    }

    premises.let {
      it.addressLine1 = addressLine1
      it.addressLine2 = addressLine2
      it.town = town
      it.postcode = postcode
      it.localAuthorityArea = localAuthorityArea
      it.probationRegion = probationRegion!!
      it.characteristics = characteristicEntities.map { it!! }.toMutableList()
      it.notes = if (notes.isNullOrEmpty()) "" else notes
      it.status = status
      if (it is TemporaryAccommodationPremisesEntity) {
        it.probationDeliveryUnit = probationDeliveryUnit!!
        if (turnaroundWorkingDayCount != null) {
          it.turnaroundWorkingDayCount = turnaroundWorkingDayCount!!
        }
      }
    }

    if (premises is TemporaryAccommodationPremisesEntity && status == PropertyStatus.archived) {
      premises.rooms.forEach { room ->
        room.beds.forEach { bed ->
          bed.endDate = LocalDate.now()
        }
      }
    }

    val savedPremises = premisesRepository.save(premises)

    return AuthorisableActionResult.Success(
      ValidatableActionResult.Success(savedPremises),
    )
  }

  fun renamePremises(
    premisesId: UUID,
    name: String,
  ): AuthorisableActionResult<ValidatableActionResult<PremisesEntity>> {
    val premises = premisesRepository.findByIdOrNull(premisesId) ?: return AuthorisableActionResult.NotFound()

    return AuthorisableActionResult.Success(
      validated {
        if (!premisesRepository.nameIsUniqueForType(name, premises::class.java)) {
          "$.name" hasValidationError "notUnique"
        }

        if (validationErrors.any()) {
          return@validated fieldValidationError
        }

        premises.name = name

        return@validated success(premisesRepository.save(premises))
      },
    )
  }

  fun getBeds(premisesId: UUID) = bedRepository.findAllBedsForPremises(premisesId)

  @Transactional
  fun deletePremises(premises: PremisesEntity): ValidatableActionResult<Unit> = validated {
    if (premises.bookings.any()) {
      return premises.id hasConflictError "A premises cannot be hard-deleted if it has any bookings associated with it"
    }

    premises.lostBeds.forEach { lostBed ->
      lostBedsRepository.delete(lostBed)
    }

    premises.rooms.forEach { room ->
      room.beds.forEach { bed ->
        bedRepository.delete(bed)
      }
      roomRepository.delete(room)
    }
    premisesRepository.delete(premises)

    success(Unit)
  }

  private fun serviceScopeMatches(scope: String, premises: PremisesEntity) = when (scope) {
    "*" -> true
    ServiceName.approvedPremises.value -> premises is ApprovedPremisesEntity
    ServiceName.temporaryAccommodation.value -> premises is TemporaryAccommodationPremisesEntity
    else -> false
  }

  private fun tryGetProbationDeliveryUnit(
    probationDeliveryUnitIdentifier: Either<String, UUID>?,
    probationRegionId: UUID,
    onValidationError: (property: String, err: String) -> Unit,
  ): ProbationDeliveryUnitEntity? {
    val probationDeliveryUnit = when (probationDeliveryUnitIdentifier) {
      null -> {
        onValidationError("$.probationDeliveryUnitId", "empty")
        null
      }
      else -> probationDeliveryUnitIdentifier.fold({ name ->
        if (name.isBlank()) {
          onValidationError("$.pdu", "empty")
        }

        val result = probationDeliveryUnitRepository.findByNameAndProbationRegionId(name, probationRegionId)

        if (result == null) {
          onValidationError("$.pdu", "doesNotExist")
        }

        result
      }, { id ->
        val result = probationDeliveryUnitRepository.findByIdAndProbationRegionId(id, probationRegionId)

        if (result == null) {
          onValidationError("$.probationDeliveryUnitId", "doesNotExist")
        }

        result
      })
    }

    return probationDeliveryUnit
  }

  fun getDateCapacities(premises: PremisesEntity): List<DateCapacity> {
    val now = timeService.nowAsLocalDate()

    val oneYearsTime = now.plusYears(1)
    val lastBookingDate = minOf(
      getLastBookingDate(premises) ?: now,
      oneYearsTime,
    )
    val lastLostBedsDate = minOf(
      getLastLostBedsDate(premises) ?: now,
      oneYearsTime,
    )

    val capacityForPeriod = getAvailabilityForRange(
      premises,
      now,
      maxOf(
        now,
        lastBookingDate ?: now,
        lastLostBedsDate ?: now,
      ),
    )

    val totalBeds = getBedCount(premises)

    return capacityForPeriod.map {
      DateCapacity(
        date = it.key,
        availableBeds = it.value.getFreeCapacity(totalBeds),
      )
    }
  }

  fun getBedCount(premises: PremisesEntity): Int {
    return premisesRepository.getBedCount(premises)
  }
}
