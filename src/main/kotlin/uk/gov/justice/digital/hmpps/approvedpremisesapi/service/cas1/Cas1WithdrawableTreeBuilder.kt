package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequestEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.ApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.BookingService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequestService
import java.util.UUID

/**
 * The tree can have the following edges:
 *
 * > Application
 * ---> Request for Placement
 * ------> Match Request
 * ---------> Placement
 * ---> Match Request (For initial application dates)
 * ------> Placement
 * ---> Placement (For adhoc placements)
 *
 * Note that whilst assessments are automatically withdrawn when
 * an application is withdrawn, that is not managed by the tree
 **/
@Component
class Cas1WithdrawableTreeBuilder(
  @Lazy private val placementRequestService: PlacementRequestService,
  @Lazy private val bookingService: BookingService,
  @Lazy private val placementApplicationService: PlacementApplicationService,
  @Lazy private val applicationService: ApplicationService,
) {
  fun treeForApp(application: ApprovedPremisesApplicationEntity, user: UserEntity): WithdrawableTreeNode {
    val children = mutableListOf<WithdrawableTreeNode>()

    placementRequestService.getPlacementRequestForInitialApplicationDates(application.id).forEach {
      children.add(treeForPlacementReq(it, user))
    }

    placementApplicationService.getAllActivePlacementApplicationsForApplicationId(application.id).forEach {
      children.add(treeForPlacementApp(it, user))
    }

    bookingService.getAllAdhocOrUnknownForApplication(application).forEach {
      children.add(treeForBooking(it, user))
    }

    return WithdrawableTreeNode(
      applicationId = application.id,
      entityType = WithdrawableEntityType.Application,
      entityId = application.id,
      status = applicationService.getWithdrawableState(application, user),
      dates = emptyList(),
      children = children,
    )
  }

  fun treeForPlacementApp(placementApplication: PlacementApplicationEntity, user: UserEntity): WithdrawableTreeNode {
    val children = placementApplication.placementRequests.map { treeForPlacementReq(it, user) }

    return WithdrawableTreeNode(
      applicationId = placementApplication.application.id,
      entityType = WithdrawableEntityType.PlacementApplication,
      entityId = placementApplication.id,
      status = placementApplicationService.getWithdrawableState(placementApplication, user),
      dates = placementApplication.placementDates.map { WithdrawableDatePeriod(it.expectedArrival, it.expectedDeparture()) },
      children = children,
    )
  }

  fun treeForPlacementReq(placementRequest: PlacementRequestEntity, user: UserEntity): WithdrawableTreeNode {
    val booking = placementRequest.booking

    /*
     * Some legacy adhoc bookings were incorrectly linked to placement requests,
     * and in some cases these placement requests had completely different dates
     * from the bookings. Until these relationships are removed, we should exclude
     * this relationship for any adhoc booking as to avoid unexpected withdrawal
     * cascading.
     *
     * If adhoc is null, we treat it as 'potentially adhoc' and exclude it.
     */
    val children = if (booking != null && booking.adhoc == false) {
      listOf(treeForBooking(booking, user))
    } else {
      emptyList()
    }

    return WithdrawableTreeNode(
      applicationId = placementRequest.application.id,
      entityType = WithdrawableEntityType.PlacementRequest,
      entityId = placementRequest.id,
      status = placementRequestService.getWithdrawableState(placementRequest, user),
      dates = listOf(WithdrawableDatePeriod(placementRequest.expectedArrival, placementRequest.expectedDeparture())),
      children = children,
    )
  }

  fun treeForBooking(booking: BookingEntity, user: UserEntity): WithdrawableTreeNode {
    return WithdrawableTreeNode(
      applicationId = booking.application?.id,
      entityType = WithdrawableEntityType.Booking,
      entityId = booking.id,
      status = bookingService.getWithdrawableState(booking, user),
      dates = listOf(WithdrawableDatePeriod(booking.arrivalDate, booking.departureDate)),
      children = emptyList(),
    )
  }
}

data class WithdrawableTreeNode(
  val applicationId: UUID?,
  val entityType: WithdrawableEntityType,
  val entityId: UUID,
  val status: WithdrawableState,
  val dates: List<WithdrawableDatePeriod> = emptyList(),
  val children: List<WithdrawableTreeNode> = emptyList(),
) {
  fun flatten(): List<WithdrawableTreeNode> {
    return listOf(this) + collectDescendants()
  }

  fun collectDescendants(): List<WithdrawableTreeNode> {
    val result = mutableListOf<WithdrawableTreeNode>()
    children.forEach {
      result.add(it)
      result.addAll(it.collectDescendants())
    }
    return result
  }

  fun isBlocked(): Boolean = status.blockAncestorWithdrawals || children.any { it.isBlocked() }

  override fun toString(): String {
    return "\n\n${render(0)}\n"
  }

  fun simpleDescription(): String = "$entityType $entityId (application $applicationId)"

  @SuppressWarnings("MagicNumber")
  fun render(depth: Int, includeIds: Boolean = true): String {
    val padding = if (depth > 0) { "-".repeat(3 * depth) + "> " } else { "" }
    val abbreviatedId = if (includeIds) { entityId.toString().substring(0, 3) } else ""
    val description = "" +
      "$entityType($abbreviatedId), " +
      "withdrawable:${yesOrNo(status.withdrawable)}, " +
      "mayDirectlyWithdraw:${yesOrNo(status.userMayDirectlyWithdraw)}" +
      blockingDescription()

    return "$padding$description\n" +
      children.joinToString(separator = "") { it.render(depth + 1, includeIds) }
  }

  private fun yesOrNo(value: Boolean) = if (value) {
    "Y"
  } else {
    "N"
  }

  private fun blockingDescription() = if (status.blockAncestorWithdrawals) {
    ", BLOCKING"
  } else if (isBlocked()) {
    ", BLOCKED"
  } else {
    ""
  }
}
