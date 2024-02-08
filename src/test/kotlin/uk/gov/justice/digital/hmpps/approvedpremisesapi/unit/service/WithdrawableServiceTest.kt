package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApAreaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesAssessmentEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.BookingEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.LocalAuthorityEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequestEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequirementsEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ProbationRegionEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.UserEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationReasonRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationWithdrawalReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequestWithdrawalReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.ValidatableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.ApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.BookingService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequestService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.WithdrawableService
import java.time.LocalDate

class WithdrawableServiceTest {
  private val mockPlacementRequestService = mockk<PlacementRequestService>()
  private val mockBookingService = mockk<BookingService>()
  private val mockPlacementApplicationService = mockk<PlacementApplicationService>()
  private val mockUserAccessService = mockk<UserAccessService>()
  private val mockApplicationService = mockk<ApplicationService>()

  private val withdrawableService = WithdrawableService(
    mockPlacementRequestService,
    mockBookingService,
    mockPlacementApplicationService,
    mockUserAccessService,
    mockApplicationService,
  )

  val probationRegion = ProbationRegionEntityFactory()
    .withYieldedApArea { ApAreaEntityFactory().produce() }
    .produce()

  val user = UserEntityFactory().withProbationRegion(probationRegion).produce()

  val application = ApprovedPremisesApplicationEntityFactory()
    .withCreatedByUser(user)
    .produce()

  val assessment = ApprovedPremisesAssessmentEntityFactory()
    .withAllocatedToUser(user)
    .withApplication(application)
    .produce()

  val placementRequirements = PlacementRequirementsEntityFactory()
    .withApplication(application)
    .withAssessment(assessment)
    .produce()

  val premises = ApprovedPremisesEntityFactory()
    .withProbationRegion(probationRegion)
    .withYieldedLocalAuthorityArea { LocalAuthorityEntityFactory().produce() }
    .produce()

  val placementRequests = PlacementRequestEntityFactory()
    .withApplication(application)
    .withAssessment(assessment)
    .withPlacementRequirements(placementRequirements)
    .produceMany()
    .take(5)
    .toList()

  val placementApplications = PlacementApplicationEntityFactory()
    .withCreatedByUser(user)
    .withApplication(application)
    .produceMany()
    .take(2)
    .toList()

  val bookings = BookingEntityFactory()
    .withYieldedPremises {
      ApprovedPremisesEntityFactory()
        .withProbationRegion(probationRegion)
        .withYieldedLocalAuthorityArea { LocalAuthorityEntityFactory().produce() }
        .produce()
    }
    .produceMany()
    .take(3)
    .toList()

  @BeforeEach
  fun setup() {
    every {
      mockPlacementRequestService.getWithdrawablePlacementRequests(application)
    } returns placementRequests
    every {
      mockPlacementApplicationService.getWithdrawablePlacementApplications(application)
    } returns placementApplications
    every {
      mockBookingService.getCancelleableCas1Bookings(user, application)
    } returns bookings
  }

  @Test
  fun `allWithdrawables returns all withdrawable information`() {
    every { mockApplicationService.isWithdrawable(application, user) } returns true
    every { mockUserAccessService.currentUserCanManagePremisesBookings(any()) } returns true
    val result = withdrawableService.allWithdrawables(application, user)

    assertThat(result.application).isEqualTo(true)
    assertThat(result.bookings).isEqualTo(bookings)
    assertThat(result.placementRequests).isEqualTo(placementRequests)
    assertThat(result.placementApplications).isEqualTo(placementApplications)

    bookings.forEach {
      verify(exactly = 1) {
        mockUserAccessService.currentUserCanManagePremisesBookings(it.premises)
      }
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `allWithdrawables returns if application can't be withdrawn`(canBeWithdrawn: Boolean) {
    every { mockApplicationService.isWithdrawable(application, user) } returns canBeWithdrawn
    every { mockUserAccessService.currentUserCanManagePremisesBookings(any()) } returns true
    val result = withdrawableService.allWithdrawables(application, user)

    assertThat(result.application).isEqualTo(canBeWithdrawn)
  }

  @Test
  fun `allWithdrawables filters out bookings that the user cannot manage by default`() {
    every { mockApplicationService.isWithdrawable(application, user) } returns true
    every { mockUserAccessService.currentUserCanManagePremisesBookings(bookings[0].premises) } returns true
    every { mockUserAccessService.currentUserCanManagePremisesBookings(bookings[1].premises) } returns false
    every { mockUserAccessService.currentUserCanManagePremisesBookings(bookings[2].premises) } returns false

    val result = withdrawableService.allWithdrawables(application, user)

    assertThat(result.bookings).isEqualTo(listOf(bookings[0]))
    assertThat(result.placementRequests).isEqualTo(placementRequests)
    assertThat(result.placementApplications).isEqualTo(placementApplications)

    bookings.forEach {
      verify(exactly = 1) {
        mockUserAccessService.currentUserCanManagePremisesBookings(it.premises)
      }
    }
  }

  @Test
  fun `allWithdrawables doesnt filter out bookings when onlyUserManageableBookings is false`() {
    every { mockApplicationService.isWithdrawable(application, user) } returns true
    val result = withdrawableService.allWithdrawables(application, user, false)

    assertThat(result.bookings).isEqualTo(bookings)
    assertThat(result.placementRequests).isEqualTo(placementRequests)
    assertThat(result.placementApplications).isEqualTo(placementApplications)

    verify(exactly = 0) {
      mockUserAccessService.currentUserCanManagePremisesBookings(any())
    }
  }

  @Test
  fun `withdrawAllForApplication withdraws all withdrawable entities`() {
    every { mockApplicationService.isWithdrawable(application, user) } returns true
    every { mockUserAccessService.currentUserCanManagePremisesBookings(any()) } returns true

    every {
      mockBookingService.createCancellation(
        user,
        any(),
        any(),
        CancellationReasonRepository.CAS1_WITHDRAWN_BY_PP_ID,
        "Automatically withdrawn as application was withdrawn",
      )
    } returns mockk<ValidatableActionResult<CancellationEntity>>()

    every {
      mockPlacementRequestService.withdrawPlacementRequest(any(), user, PlacementRequestWithdrawalReason.WITHDRAWN_BY_PP)
    } returns mockk<AuthorisableActionResult<Unit>>()

    every {
      mockPlacementApplicationService.withdrawPlacementApplication(any(), PlacementApplicationWithdrawalReason.WITHDRAWN_BY_PP)
    } returns mockk<AuthorisableActionResult<ValidatableActionResult<PlacementApplicationEntity>>>()

    withdrawableService.withdrawAllForApplication(application, user)

    bookings.forEach {
      verify {
        mockBookingService.createCancellation(
          user,
          it,
          LocalDate.now(),
          CancellationReasonRepository.CAS1_WITHDRAWN_BY_PP_ID,
          "Automatically withdrawn as application was withdrawn",
        )
      }
    }

    placementRequests.forEach {
      verify {
        mockPlacementRequestService.withdrawPlacementRequest(it.id, user, PlacementRequestWithdrawalReason.WITHDRAWN_BY_PP)
      }
    }

    placementApplications.forEach {
      verify {
        mockPlacementApplicationService.withdrawPlacementApplication(it.id, PlacementApplicationWithdrawalReason.WITHDRAWN_BY_PP)
      }
    }
  }
}
