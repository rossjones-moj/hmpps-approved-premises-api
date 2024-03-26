package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.service

import io.mockk.Called
import io.mockk.Runs
import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.allocations.UserAllocator
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.PersonReference
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementDates
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementRequestSortField
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementRequestStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SortDirection
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApAreaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesAssessmentEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.BookingEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.LocalAuthorityEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.OffenderDetailsSummaryFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PersonRisksFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequestEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequirementsEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ProbationRegionEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.StaffUserDetailsFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.UserEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.UserRoleAssignmentEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingNotMadeEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingNotMadeRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementDateRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequestEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequestWithdrawalReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequirementsRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.ApprovedPremisesApplicationStatus.PENDING_PLACEMENT_REQUEST
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.RiskTier
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.RiskWithStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.ValidatableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.ApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.CruService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.DomainEventService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.OffenderService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequestService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.TaskDeadlineService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.Cas1PlacementRequestDomainEventService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.Cas1PlacementRequestEmailService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.WithdrawableEntityType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.WithdrawalContext
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.PageCriteria
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.PaginationConfig
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class PlacementRequestServiceTest {
  private val placementRequestRepository = mockk<PlacementRequestRepository>()
  private val bookingNotMadeRepository = mockk<BookingNotMadeRepository>()
  private val domainEventService = mockk<DomainEventService>()
  private val offenderService = mockk<OffenderService>()
  private val communityApiClient = mockk<CommunityApiClient>()
  private val cruService = mockk<CruService>()
  private val placementRequirementsRepository = mockk<PlacementRequirementsRepository>()
  private val placementDateRepository = mockk<PlacementDateRepository>()
  private val cancellationRepository = mockk<CancellationRepository>()
  private val userAllocator = mockk<UserAllocator>()
  private val userAccessService = mockk<UserAccessService>()
  private val applicationService = mockk<ApplicationService>()
  private val cas1PlacementRequestEmailService = mockk<Cas1PlacementRequestEmailService>()
  private val cas1PlacementRequestDomainEventService = mockk<Cas1PlacementRequestDomainEventService>()
  private val taskDeadlineServiceMock = mockk<TaskDeadlineService>()

  private val placementRequestService = PlacementRequestService(
    placementRequestRepository,
    bookingNotMadeRepository,
    domainEventService,
    offenderService,
    communityApiClient,
    cruService,
    placementRequirementsRepository,
    placementDateRepository,
    cancellationRepository,
    userAllocator,
    userAccessService,
    applicationService,
    cas1PlacementRequestEmailService,
    cas1PlacementRequestDomainEventService,
    "http://frontend/applications/#id",
    taskDeadlineServiceMock,
  )

  private val previousUser = UserEntityFactory()
    .withYieldedProbationRegion {
      ProbationRegionEntityFactory()
        .withYieldedApArea { ApAreaEntityFactory().produce() }
        .produce()
    }
    .produce()

  private val assigneeUser = UserEntityFactory()
    .withYieldedProbationRegion {
      ProbationRegionEntityFactory()
        .withYieldedApArea { ApAreaEntityFactory().produce() }
        .produce()
    }
    .produce()

  @BeforeEach
  fun before() {
    PaginationConfig(defaultPageSize = 10).postInit()
  }

  @Test
  fun `createPlacementRequest creates a placement request with the correct deadline`() {
    val dueAt = OffsetDateTime.now()

    every { taskDeadlineServiceMock.getDeadline(any<PlacementRequestEntity>()) } returns dueAt
    every { userAllocator.getUserForPlacementRequestAllocation(any()) } returns assigneeUser
    every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val placementRequirements = PlacementRequirementsEntityFactory()
      .withApplication(application)
      .withAssessment(assessment)
      .produce()

    val placementDates = PlacementDates(
      expectedArrival = LocalDate.now(),
      duration = 12,
    )

    val placementRequest = placementRequestService.createPlacementRequest(
      placementRequirements,
      placementDates,
      "Some notes",
      false,
      null,
    )

    assertThat(placementRequest.duration).isEqualTo(placementDates.duration)
    assertThat(placementRequest.expectedArrival).isEqualTo(placementDates.expectedArrival)
    assertThat(placementRequest.placementRequirements).isEqualTo(placementRequirements)
    assertThat(placementRequest.assessment.id).isEqualTo(assessment.id)
    assertThat(placementRequest.application.id).isEqualTo(application.id)
    assertThat(placementRequest.isParole).isFalse()
    assertThat(placementRequest.dueAt).isEqualTo(dueAt)
    assertThat(placementRequest.allocatedToUser!!.id).isEqualTo(assigneeUser.id)
  }

  @Test
  fun `reallocatePlacementRequest returns General Validation Error when request already has an associated booking`() {
    val premisesEntity = ApprovedPremisesEntityFactory()
      .withYieldedProbationRegion {
        ProbationRegionEntityFactory()
          .withYieldedApArea { ApAreaEntityFactory().produce() }
          .produce()
      }
      .withYieldedLocalAuthorityArea { LocalAuthorityEntityFactory().produce() }
      .produce()

    val booking = BookingEntityFactory()
      .withYieldedPremises { premisesEntity }
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val previousPlacementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withBooking(booking)
      .withAssessment(assessment)
      .withAllocatedToUser(previousUser)
      .produce()

    every { placementRequestRepository.findByIdOrNull(previousPlacementRequest.id) } returns previousPlacementRequest

    val result = placementRequestService.reallocatePlacementRequest(assigneeUser, previousPlacementRequest.id)

    assertThat(result is AuthorisableActionResult.Success).isTrue
    val validationResult = (result as AuthorisableActionResult.Success).entity

    assertThat(validationResult is ValidatableActionResult.GeneralValidationError).isTrue
    validationResult as ValidatableActionResult.GeneralValidationError
    assertThat(validationResult.message).isEqualTo("This placement request has already been completed")
  }

  @Test
  fun `reallocatePlacementRequest returns Field Validation Error when user to assign to is not a MATCHER`() {
    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val previousPlacementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(previousUser)
      .produce()

    every { placementRequestRepository.findByIdOrNull(previousPlacementRequest.id) } returns previousPlacementRequest

    val result = placementRequestService.reallocatePlacementRequest(assigneeUser, previousPlacementRequest.id)

    assertThat(result is AuthorisableActionResult.Success).isTrue
    val validationResult = (result as AuthorisableActionResult.Success).entity

    assertThat(validationResult is ValidatableActionResult.FieldValidationError).isTrue
    validationResult as ValidatableActionResult.FieldValidationError
    assertThat(validationResult.validationMessages).containsEntry("$.userId", "lackingMatcherRole")
  }

  @Test
  fun `reallocatePlacementRequest returns Success, deallocates old placementRequest and creates a new one`() {
    val assigneeUser = UserEntityFactory()
      .withYieldedProbationRegion {
        ProbationRegionEntityFactory()
          .withYieldedApArea { ApAreaEntityFactory().produce() }
          .produce()
      }
      .produce()
      .apply {
        roles += UserRoleAssignmentEntityFactory()
          .withUser(this)
          .withRole(UserRole.CAS1_MATCHER)
          .produce()
      }

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val previousPlacementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(previousUser)
      .produce()

    val dueAt = OffsetDateTime.now()

    every { taskDeadlineServiceMock.getDeadline(any<PlacementRequestEntity>()) } returns dueAt
    every { placementRequestRepository.findByIdOrNull(previousPlacementRequest.id) } returns previousPlacementRequest

    every { placementRequestRepository.save(previousPlacementRequest) } answers { it.invocation.args[0] as PlacementRequestEntity }
    every { placementRequestRepository.save(match { it.allocatedToUser == assigneeUser }) } answers { it.invocation.args[0] as PlacementRequestEntity }

    val result = placementRequestService.reallocatePlacementRequest(assigneeUser, previousPlacementRequest.id)

    assertThat(result is AuthorisableActionResult.Success).isTrue
    val validationResult = (result as AuthorisableActionResult.Success).entity

    assertThat(validationResult is ValidatableActionResult.Success).isTrue
    validationResult as ValidatableActionResult.Success

    assertThat(previousPlacementRequest.reallocatedAt).isNotNull

    verify { placementRequestRepository.save(match { it.allocatedToUser == assigneeUser }) }

    val newPlacementRequest = validationResult.entity

    assertThat(newPlacementRequest.application).isEqualTo(application)
    assertThat(newPlacementRequest.allocatedToUser).isEqualTo(assigneeUser)
    assertThat(newPlacementRequest.placementRequirements.radius).isEqualTo(previousPlacementRequest.placementRequirements.radius)
    assertThat(newPlacementRequest.placementRequirements.postcodeDistrict).isEqualTo(previousPlacementRequest.placementRequirements.postcodeDistrict)
    assertThat(newPlacementRequest.placementRequirements.gender).isEqualTo(previousPlacementRequest.placementRequirements.gender)
    assertThat(newPlacementRequest.expectedArrival).isEqualTo(previousPlacementRequest.expectedArrival)
    assertThat(newPlacementRequest.placementRequirements.apType).isEqualTo(previousPlacementRequest.placementRequirements.apType)
    assertThat(newPlacementRequest.duration).isEqualTo(previousPlacementRequest.duration)
    assertThat(newPlacementRequest.placementRequirements.desirableCriteria).isEqualTo(previousPlacementRequest.placementRequirements.desirableCriteria)
    assertThat(newPlacementRequest.placementRequirements.essentialCriteria).isEqualTo(previousPlacementRequest.placementRequirements.essentialCriteria)
    assertThat(newPlacementRequest.dueAt).isEqualTo(dueAt)
  }

  @Test
  fun `getPlacementRequestForUser returns NotFound when PlacementRequest doesn't exist`() {
    val placementRequestId = UUID.fromString("72f15a57-8f3a-48bc-abc7-be09fe548fea")

    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns null

    val result = placementRequestService.getPlacementRequestForUser(requestingUser, placementRequestId)

    assertThat(result is AuthorisableActionResult.NotFound).isTrue()
  }

  @Test
  fun `getPlacementRequestForUser returns Unauthorised when PlacementRequest not allocated to User and User does not have WORKFLOW_MANAGER role`() {
    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(requestingUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(requestingUser)
      .produce()

    val placementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(assigneeUser)
      .produce()

    every { placementRequestRepository.findByIdOrNull(placementRequest.id) } returns placementRequest

    val result = placementRequestService.getPlacementRequestForUser(requestingUser, placementRequest.id)

    assertThat(result is AuthorisableActionResult.Unauthorised).isTrue()
  }

  @Test
  fun `getPlacementRequestForUser returns Success when PlacementRequest is allocated to User`() {
    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val placementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(requestingUser)
      .produce()

    val mockCancellations = mockk<List<CancellationEntity>>()

    every { placementRequestRepository.findByIdOrNull(placementRequest.id) } returns placementRequest
    every { cancellationRepository.getCancellationsForApplicationId(application.id) } returns mockCancellations

    val result = placementRequestService.getPlacementRequestForUser(requestingUser, placementRequest.id)

    assertThat(result is AuthorisableActionResult.Success).isTrue()

    val (expectedPlacementRequest, expectedCancellations) = (result as AuthorisableActionResult.Success).entity

    assertThat(expectedPlacementRequest).isEqualTo(placementRequest)
    assertThat(expectedCancellations).isEqualTo(mockCancellations)
  }

  @Test
  fun `getPlacementRequestForUser returns Success when User has the WORKFLOW_MANAGER role`() {
    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()
      .apply {
        roles += UserRoleAssignmentEntityFactory()
          .withUser(this)
          .withRole(UserRole.CAS1_WORKFLOW_MANAGER)
          .produce()
      }

    val otherUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(assigneeUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(assigneeUser)
      .produce()

    val placementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(otherUser)
      .produce()

    val mockCancellations = mockk<List<CancellationEntity>>()

    every { placementRequestRepository.findByIdOrNull(placementRequest.id) } returns placementRequest
    every { cancellationRepository.getCancellationsForApplicationId(application.id) } returns mockCancellations

    val result = placementRequestService.getPlacementRequestForUser(requestingUser, placementRequest.id)

    assertThat(result is AuthorisableActionResult.Success).isTrue()

    val (expectedPlacementRequest, expectedCancellations) = (result as AuthorisableActionResult.Success).entity

    assertThat(expectedPlacementRequest).isEqualTo(placementRequest)
    assertThat(expectedCancellations).isEqualTo(mockCancellations)
  }

  @Test
  fun `createBookingNotMade returns Not Found when Placement Request doesn't exist`() {
    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val placementRequestId = UUID.fromString("25dd65b1-38b5-47bc-a00b-f2df228ed06b")

    every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns null

    val result = placementRequestService.createBookingNotMade(requestingUser, placementRequestId, null)
    assertThat(result is AuthorisableActionResult.NotFound).isTrue
  }

  @Test
  fun `createBookingNotMade returns Success, saves Booking Not Made and saves domain event`() {
    val requestingUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val otherUser = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(otherUser)
      .produce()

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(otherUser)
      .produce()

    val placementRequest = PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withAllocatedToUser(requestingUser)
      .withApplication(application)
      .withAssessment(assessment)
      .produce()

    val offenderDetails = OffenderDetailsSummaryFactory()
      .withCrn(application.crn)
      .produce()

    every { offenderService.getOffenderByCrn(application.crn, requestingUser.deliusUsername) } returns AuthorisableActionResult.Success(offenderDetails)

    val staffUserDetails = StaffUserDetailsFactory().produce()

    every { communityApiClient.getStaffUserDetails(requestingUser.deliusUsername) } returns ClientResult.Success(
      HttpStatus.OK,
      staffUserDetails,
    )

    every { domainEventService.saveBookingNotMadeEvent(any()) } just Runs

    every { placementRequestRepository.findByIdOrNull(placementRequest.id) } returns placementRequest
    every { bookingNotMadeRepository.save(any()) } answers { it.invocation.args[0] as BookingNotMadeEntity }

    every { cruService.cruNameFromProbationAreaCode(staffUserDetails.probationArea.code) } returns "CRU NAME"

    val result = placementRequestService.createBookingNotMade(requestingUser, placementRequest.id, "some notes")
    assertThat(result is AuthorisableActionResult.Success).isTrue
    val bookingNotMade = (result as AuthorisableActionResult.Success).entity

    assertThat(bookingNotMade.placementRequest).isEqualTo(placementRequest)
    assertThat(bookingNotMade.notes).isEqualTo("some notes")

    verify(exactly = 1) { bookingNotMadeRepository.save(match { it.notes == "some notes" && it.placementRequest == placementRequest }) }

    verify(exactly = 1) {
      domainEventService.saveBookingNotMadeEvent(
        match {
          val data = it.data.eventDetails
          val application = placementRequest.application

          it.applicationId == application.id &&
            it.crn == application.crn &&
            data.applicationId == application.id &&
            data.applicationUrl == "http://frontend/applications/${application.id}" &&
            data.personReference == PersonReference(
            crn = offenderDetails.otherIds.crn,
            noms = offenderDetails.otherIds.nomsNumber!!,
          ) &&
            data.deliusEventNumber == application.eventNumber &&
            data.failureDescription == "some notes"
        },
      )
    }
  }

  @Nested
  inner class GetWithdrawableState {
    val user = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(UserEntityFactory().withUnitTestControlProbationRegion().produce())
      .produce()

    @Test
    fun `getWithdrawableState not withdrawable if reallocated`() {
      val placementRequest = createValidPlacementRequest(application, user)
      placementRequest.reallocatedAt = OffsetDateTime.now()

      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true

      val result = placementRequestService.getWithdrawableState(placementRequest, user)

      assertThat(result.withdrawable).isFalse()
    }

    @Test
    fun `getWithdrawableState not withdrawable if already withdrawn`() {
      val placementRequest = createValidPlacementRequest(application, user)
      placementRequest.isWithdrawn = true

      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true

      val result = placementRequestService.getWithdrawableState(placementRequest, user)

      assertThat(result.withdrawable).isFalse()
    }

    @Test
    fun `getWithdrawableState withdrawable if not already withdrawn and not reallocated`() {
      val placementRequest = createValidPlacementRequest(application, user)
      placementRequest.isWithdrawn = false
      placementRequest.reallocatedAt = null

      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true

      val result = placementRequestService.getWithdrawableState(placementRequest, user)

      assertThat(result.withdrawable).isTrue()
    }

    @ParameterizedTest
    @CsvSource("true", "false")
    fun `getWithdrawableState userMayDirectlyWithdraw delegates to user access service`(canWithdraw: Boolean) {
      val placementRequest = createValidPlacementRequest(application, user)

      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns canWithdraw

      val result = placementRequestService.getWithdrawableState(placementRequest, user)

      assertThat(result.userMayDirectlyWithdraw).isEqualTo(canWithdraw)
    }

    @Test
    fun `getWithdrawableState userMayDirectlyWithdraw returns false if not for original app dates`() {
      val placementRequest = createValidPlacementRequest(
        application,
        user,
        placementApplication = PlacementApplicationEntityFactory()
          .withCreatedByUser(user)
          .withApplication(application)
          .produce(),
      )

      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true

      val result = placementRequestService.getWithdrawableState(placementRequest, user)

      assertThat(result.userMayDirectlyWithdraw).isFalse()
    }
  }

  @Nested
  inner class WithdrawPlacementRequest {
    val user = UserEntityFactory()
      .withUnitTestControlProbationRegion()
      .produce()

    val application = ApprovedPremisesApplicationEntityFactory()
      .withCreatedByUser(user)
      .produce()

    val placementRequest = createValidPlacementRequest(application, user)
    val placementRequestId = placementRequest.id

    @Test
    fun `withdrawPlacementRequest returns Not Found if no Placement Request with ID exists`() {
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns null

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST,
        WithdrawalContext(
          user,
          WithdrawableEntityType.PlacementRequest,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.NotFound).isTrue
    }

    @ParameterizedTest
    @EnumSource(PlacementRequestWithdrawalReason::class)
    @NullSource
    fun `withdrawPlacementRequest returns Success and saves withdrawn PlacementRequest, triggering emails and domain events and cascades`(
      reason: PlacementRequestWithdrawalReason?,
    ) {
      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { placementRequestRepository.findByApplication(application) } returns listOf(placementRequest)
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every {
        applicationService.updateApprovedPremisesApplicationStatus(application.id, PENDING_PLACEMENT_REQUEST)
      } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val withdrawalContext = WithdrawalContext(
        user,
        WithdrawableEntityType.PlacementRequest,
        placementRequestId,
      )

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        reason,
        withdrawalContext,
      )

      assertThat(result is CasResult.Success).isTrue

      verify {
        placementRequestRepository.save(
          match {
            it.id == placementRequestId &&
              it.isWithdrawn &&
              it.withdrawalReason == reason
          },
        )
      }

      verify { cas1PlacementRequestEmailService.placementRequestWithdrawn(placementRequest, user) }
      verify { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(placementRequest, withdrawalContext) }
    }

    @Test
    fun `withdrawPlacementRequest updates application status to 'PENDING_PLACEMENT_REQUEST' if no other non-active placement requests`() {
      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val withdrawnPlacementRequest = createValidPlacementRequest(application, user)
      withdrawnPlacementRequest.isWithdrawn = true

      val reallocatedPlacementRequest = createValidPlacementRequest(application, user)
      reallocatedPlacementRequest.reallocatedAt = OffsetDateTime.now()

      every {
        placementRequestRepository.findByApplication(application)
      } returns listOf(
        withdrawnPlacementRequest,
        reallocatedPlacementRequest,
      )
      every {
        applicationService.updateApprovedPremisesApplicationStatus(application.id, PENDING_PLACEMENT_REQUEST)
      } returns Unit

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST,
        WithdrawalContext(
          user,
          WithdrawableEntityType.PlacementRequest,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      verify {
        applicationService.updateApprovedPremisesApplicationStatus(
          application.id,
          PENDING_PLACEMENT_REQUEST,
        )
      }
    }

    @Test
    fun `withdrawPlacementRequest does not update application status to 'PENDING_PLACEMENT_REQUEST' if there are other active placement requests`() {
      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val withdrawnPlacementRequest = createValidPlacementRequest(application, user)
      withdrawnPlacementRequest.isWithdrawn = true

      val reallocatedPlacementRequest = createValidPlacementRequest(application, user)
      withdrawnPlacementRequest.reallocatedAt = OffsetDateTime.now()

      val activePlacementRequest = createValidPlacementRequest(application, user)

      every {
        placementRequestRepository.findByApplication(application)
      } returns listOf(
        withdrawnPlacementRequest,
        activePlacementRequest,
        reallocatedPlacementRequest,
      )

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST,
        WithdrawalContext(
          user,
          WithdrawableEntityType.PlacementRequest,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      verify { applicationService wasNot Called }
    }

    @Test
    fun `withdrawPlacementRequest doesnt updates application status if user didn't trigger withdrawal`() {
      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val withdrawnPlacementRequest = createValidPlacementRequest(application, user)
      withdrawnPlacementRequest.isWithdrawn = true

      val reallocatedPlacementRequest = createValidPlacementRequest(application, user)
      reallocatedPlacementRequest.reallocatedAt = OffsetDateTime.now()

      every {
        placementRequestRepository.findByApplication(application)
      } returns listOf(
        withdrawnPlacementRequest,
        reallocatedPlacementRequest,
      )

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST,
        WithdrawalContext(
          user,
          WithdrawableEntityType.Application,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      verify { applicationService wasNot Called }
    }

    @ParameterizedTest
    @EnumSource(value = WithdrawableEntityType::class, names = ["Booking", "PlacementRequest"], mode = EnumSource.Mode.EXCLUDE)
    fun `withdrawPlacementRequest sets correct reason if withdrawal triggered by other entity and user permissions not checked`(triggeringEntity: WithdrawableEntityType) {
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val providedReason = PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST
      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        providedReason,
        WithdrawalContext(
          user,
          triggeringEntity,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      val expectedWithdrawalReason = when (triggeringEntity) {
        WithdrawableEntityType.Application -> PlacementRequestWithdrawalReason.RELATED_APPLICATION_WITHDRAWN
        WithdrawableEntityType.PlacementApplication -> PlacementRequestWithdrawalReason.RELATED_PLACEMENT_APPLICATION_WITHDRAWN
        WithdrawableEntityType.Booking -> providedReason
        WithdrawableEntityType.PlacementRequest -> providedReason
      }

      verify { userAccessService wasNot Called }

      verify {
        placementRequestRepository.save(
          match {
            it.id == placementRequestId &&
              it.isWithdrawn &&
              it.withdrawalReason == expectedWithdrawalReason
          },
        )
      }
    }

    @ParameterizedTest
    @EnumSource(value = WithdrawableEntityType::class, names = ["Booking"], mode = EnumSource.Mode.INCLUDE)
    fun `withdrawPlacementRequest throws exception if withdrawal triggered by invalid entity`(triggeringEntity: WithdrawableEntityType) {
      val user = UserEntityFactory()
        .withUnitTestControlProbationRegion()
        .produce()

      val application = ApprovedPremisesApplicationEntityFactory()
        .withCreatedByUser(user)
        .produce()

      val placementRequest = createValidPlacementRequest(application, user)
      val placementRequestId = placementRequest.id

      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }

      val providedReason = PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST

      assertThatThrownBy {
        placementRequestService.withdrawPlacementRequest(
          placementRequestId,
          providedReason,
          WithdrawalContext(
            user,
            triggeringEntity,
            placementRequestId,
          ),
        )
      }.hasMessage("Internal Server Error: Withdrawing a ${triggeringEntity.name} should not cascade to PlacementRequests")
    }

    @Test
    fun `withdrawPlacementRequest is idempotent if placement request already withdrawn`() {
      placementRequest.isWithdrawn = true

      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.WITHDRAWN_BY_PP,
        WithdrawalContext(
          user,
          WithdrawableEntityType.PlacementRequest,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      verify { placementRequestRepository.save(any()) wasNot called }
    }

    @Test
    fun `withdrawPlacementRequest success if user directly requested withdrawal`() {
      every { userAccessService.userMayWithdrawPlacementRequest(user, placementRequest) } returns true
      every { placementRequestRepository.findByIdOrNull(placementRequestId) } returns placementRequest
      every { placementRequestRepository.save(any()) } answers { it.invocation.args[0] as PlacementRequestEntity }
      every { placementRequestRepository.findByApplication(application) } returns listOf(placementRequest)
      every {
        applicationService.updateApprovedPremisesApplicationStatus(application.id, PENDING_PLACEMENT_REQUEST)
      } returns Unit
      every { cas1PlacementRequestEmailService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cas1PlacementRequestDomainEventService.placementRequestWithdrawn(any(), any()) } returns Unit
      every { cancellationRepository.getCancellationsForApplicationId(any()) } returns emptyList()

      val result = placementRequestService.withdrawPlacementRequest(
        placementRequestId,
        PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST,
        WithdrawalContext(
          user,
          WithdrawableEntityType.PlacementRequest,
          placementRequestId,
        ),
      )

      assertThat(result is CasResult.Success).isTrue

      verify {
        placementRequestRepository.save(
          match {
            it.id == placementRequestId &&
              it.isWithdrawn &&
              it.withdrawalReason == PlacementRequestWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST
          },
        )
      }
    }
  }

  @Test
  fun `getAllActive returns results and no metadata when a page number is not provided`() {
    val placementRequests = createPlacementRequests(2)
    val page = mockk<Page<PlacementRequestEntity>>()

    every { page.content } returns placementRequests

    every { placementRequestRepository.allForDashboard(status = PlacementRequestStatus.matched) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        PlacementRequestStatus.matched,
      ),
      PageCriteria(page = null, sortBy = PlacementRequestSortField.createdAt, sortDirection = SortDirection.asc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata).isNull()
  }

  @Test
  fun `getAllActive returns a page and metadata when a page number is provided`() {
    val placementRequests = createPlacementRequests(2)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("created_at").ascending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(status = PlacementRequestStatus.matched, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        PlacementRequestStatus.matched,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.createdAt, sortDirection = SortDirection.asc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  @Test
  fun `getAllActive returns a page and metadata when a page number, sort field and direction is provided`() {
    val placementRequests = createPlacementRequests(2)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("expected_arrival").descending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(status = PlacementRequestStatus.matched, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        PlacementRequestStatus.matched,
        null,
        null,
        null,
        null,
        null,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.expectedArrival, sortDirection = SortDirection.desc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  @Test
  fun `getAllActive returns only results for CRN when provided`() {
    val crn = "CRN456"
    val placementRequests = createPlacementRequests(2, crn)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("expected_arrival").descending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(crn = crn, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        crn = crn,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.expectedArrival, sortDirection = SortDirection.desc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  @Test
  fun `getAllActive returns only results for tier when provided`() {
    val tier = "A2"

    val placementRequests = createPlacementRequests(2, tier = tier)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("expected_arrival").descending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(tier = tier, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        tier = tier,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.expectedArrival, sortDirection = SortDirection.desc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  @Test
  fun `getAllActive returns only results with arrival date after or equal to start when provided`() {
    val startDate = LocalDate.parse("2023-08-08")

    val placementRequests = createPlacementRequests(2, arrivalDate = startDate)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("expected_arrival").descending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(arrivalDateFrom = startDate, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        arrivalDateStart = startDate,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.expectedArrival, sortDirection = SortDirection.desc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  @Test
  fun `getAllActive returns only results with arrival date before or equal to end when provided`() {
    val endDate = LocalDate.parse("2023-08-08")

    val placementRequests = createPlacementRequests(2, arrivalDate = endDate)
    val page = mockk<Page<PlacementRequestEntity>>()
    val pageRequest = mockk<PageRequest>()

    mockkStatic(PageRequest::class)

    every { PageRequest.of(0, 10, Sort.by("expected_arrival").descending()) } returns pageRequest
    every { page.content } returns placementRequests
    every { page.totalPages } returns 10
    every { page.totalElements } returns 100

    every { placementRequestRepository.allForDashboard(arrivalDateTo = endDate, pageable = pageRequest) } returns page

    val (requests, metadata) = placementRequestService.getAllActive(
      PlacementRequestService.AllActiveSearchCriteria(
        arrivalDateEnd = endDate,
      ),
      PageCriteria(page = 1, sortBy = PlacementRequestSortField.expectedArrival, sortDirection = SortDirection.desc),
    )

    assertThat(requests).isEqualTo(placementRequests)
    assertThat(metadata?.currentPage).isEqualTo(1)
    assertThat(metadata?.pageSize).isEqualTo(10)
    assertThat(metadata?.totalPages).isEqualTo(10)
    assertThat(metadata?.totalResults).isEqualTo(100)
  }

  private fun createPlacementRequests(num: Int, crn: String? = null, tier: String? = null, arrivalDate: LocalDate? = null): List<PlacementRequestEntity> {
    return List(num) {
      val user = UserEntityFactory()
        .withUnitTestControlProbationRegion()
        .produce()

      val application = ApprovedPremisesApplicationEntityFactory()
        .withCreatedByUser(user)
        .apply {
          if (crn != null) this.withCrn(crn)

          if (tier != null) {
            this.withRiskRatings(
              PersonRisksFactory()
                .withTier(
                  RiskWithStatus(
                    RiskTier(
                      level = tier,
                      lastUpdated = LocalDate.now(),
                    ),
                  ),
                )
                .produce(),
            )
          }
        }
        .produce()

      val assessment = ApprovedPremisesAssessmentEntityFactory()
        .withApplication(application)
        .withAllocatedToUser(user)
        .produce()

      PlacementRequestEntityFactory()
        .withPlacementRequirements(
          PlacementRequirementsEntityFactory()
            .withApplication(application)
            .withAssessment(assessment)
            .produce(),
        )
        .withApplication(application)
        .withAssessment(assessment)
        .withAllocatedToUser(assigneeUser)
        .apply {
          if (arrivalDate != null) {
            this.withExpectedArrival(arrivalDate)
          }
        }
        .produce()
    }
  }

  private fun createValidPlacementRequest(
    application: ApprovedPremisesApplicationEntity,
    user: UserEntity,
    placementApplication: PlacementApplicationEntity? = null,
  ): PlacementRequestEntity {
    val placementRequestId = UUID.fromString("49f3eef9-4770-4f00-8f31-8e6f4cb4fd9e")

    val assessment = ApprovedPremisesAssessmentEntityFactory()
      .withApplication(application)
      .withAllocatedToUser(user)
      .produce()

    val placementRequest = PlacementRequestEntityFactory()
      .withId(placementRequestId)
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(application)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(application)
      .withAssessment(assessment)
      .withAllocatedToUser(assigneeUser)
      .withPlacementApplication(placementApplication)
      .produce()

    return placementRequest
  }
}
