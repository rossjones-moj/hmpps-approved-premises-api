/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.7.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas1

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1AssignKeyWorker
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1NewArrival
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1NewDeparture
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1NonArrival
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBooking
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingResidency
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingSummarySortField
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.NewCas1SpaceBooking
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.NewCas1SpaceBookingCancellation
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SortDirection
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TimelineEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ValidationError
import kotlin.collections.List

interface SpaceBookingsCas1 {

  fun getDelegate(): SpaceBookingsCas1Delegate = object : SpaceBookingsCas1Delegate {}

  @Operation(
    tags = ["space bookings"],
    summary = "Assign a keyworker to the space booking",
    operationId = "assignKeyworker",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid key worker staff code", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/keyworker"],
    produces = ["application/problem+json", "application/json"],
    consumes = ["application/json"],
  )
  fun assignKeyworker(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID, @Parameter(description = "", required = true) @RequestBody cas1AssignKeyWorker: Cas1AssignKeyWorker): ResponseEntity<Unit> {
    return getDelegate().assignKeyworker(premisesId, bookingId, cas1AssignKeyWorker)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Cancels a space booking",
    operationId = "cancelSpaceBooking",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/cancellations"],
    produces = ["application/problem+json", "application/json"],
    consumes = ["application/json"],
  )
  fun cancelSpaceBooking(@Parameter(description = "ID of the premises the space booking is related to", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "space booking id", required = true) @PathVariable("bookingId") bookingId: java.util.UUID, @Parameter(description = "details of the cancellation", required = true) @RequestBody body: NewCas1SpaceBookingCancellation): ResponseEntity<Unit> {
    return getDelegate().cancelSpaceBooking(premisesId, bookingId, body)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Create a booking for a space in premises, associated with a given placement request",
    operationId = "createSpaceBooking",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Cas1SpaceBooking::class))]),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/placement-requests/{placementRequestId}/space-bookings"],
    produces = ["application/json", "application/problem+json"],
    consumes = ["application/json"],
  )
  fun createSpaceBooking(@Parameter(description = "ID of the placement request from which the matching requirements originate", required = true) @PathVariable("placementRequestId") placementRequestId: java.util.UUID, @Parameter(description = "details of the space booking to be created", required = true) @RequestBody body: NewCas1SpaceBooking): ResponseEntity<Cas1SpaceBooking> {
    return getDelegate().createSpaceBooking(placementRequestId, body)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Returns space booking information for a given id",
    operationId = "getSpaceBookingById",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Cas1SpaceBooking::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/space-bookings/{bookingId}"],
    produces = ["application/json"],
  )
  fun getSpaceBookingById(@Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID): ResponseEntity<Cas1SpaceBooking> {
    return getDelegate().getSpaceBookingById(bookingId)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Returns space booking information for a given id",
    operationId = "getSpaceBookingByPremiseAndId",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Cas1SpaceBooking::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}"],
    produces = ["application/json"],
  )
  fun getSpaceBookingByPremiseAndId(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID): ResponseEntity<Cas1SpaceBooking> {
    return getDelegate().getSpaceBookingByPremiseAndId(premisesId, bookingId)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Returns timeline of a specific space booking with a given ID",
    operationId = "getSpaceBookingTimeline",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = TimelineEvent::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/timeline"],
    produces = ["application/json"],
  )
  fun getSpaceBookingTimeline(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID): ResponseEntity<List<TimelineEvent>> {
    return getDelegate().getSpaceBookingTimeline(premisesId, bookingId)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Lists space bookings for the premises, given optional filtering criteria",
    operationId = "getSpaceBookings",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = Cas1SpaceBookingSummary::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/premises/{premisesId}/space-bookings"],
    produces = ["application/json"],
  )
  fun getSpaceBookings(@Parameter(description = "ID of the premises to show space bookings for", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "Residency status to filter results on. 'Current' means marked as arrived and not departed. 'Upcoming' means not marked as arrived.", schema = Schema(allowableValues = ["upcoming", "current", "historic"])) @RequestParam(value = "residency", required = false) residency: Cas1SpaceBookingResidency?, @Parameter(description = "Partial search on CRN or Name") @RequestParam(value = "crnOrName", required = false) crnOrName: kotlin.String?, @Parameter(description = "Staff Code of the key worker to show bookings for") @RequestParam(value = "keyWorkerStaffCode", required = false) keyWorkerStaffCode: kotlin.String?, @Parameter(description = "The direction to sort the results by. If not defined, will sort in descending order", schema = Schema(allowableValues = ["asc", "desc"])) @RequestParam(value = "sortDirection", required = false) sortDirection: SortDirection?, @Parameter(description = "The field to sort the results by. If not defined, will sort on person name", schema = Schema(allowableValues = ["personName", "canonicalArrivalDate", "canonicalDepartureDate", "keyWorkerName", "tier"])) @RequestParam(value = "sortBy", required = false) sortBy: Cas1SpaceBookingSummarySortField?, @Parameter(description = "Page number of results to return. If not provided results will not be paged") @RequestParam(value = "page", required = false) page: kotlin.Int?, @Parameter(description = "Number of items to return per page (defaults to 20)") @RequestParam(value = "perPage", required = false) perPage: kotlin.Int?): ResponseEntity<List<Cas1SpaceBookingSummary>> {
    return getDelegate().getSpaceBookings(premisesId, residency, crnOrName, keyWorkerStaffCode, sortDirection, sortBy, page, perPage)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Posts an arrival to a specified space booking",
    operationId = "recordArrival",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/arrival"],
    produces = ["application/problem+json", "application/json"],
    consumes = ["application/json"],
  )
  fun recordArrival(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID, @Parameter(description = "", required = true) @RequestBody cas1NewArrival: Cas1NewArrival): ResponseEntity<Unit> {
    return getDelegate().recordArrival(premisesId, bookingId, cas1NewArrival)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Posts a departure to a specified space booking",
    operationId = "recordDeparture",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/departure"],
    produces = ["application/problem+json", "application/json"],
    consumes = ["application/json"],
  )
  fun recordDeparture(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID, @Parameter(description = "", required = true) @RequestBody cas1NewDeparture: Cas1NewDeparture): ResponseEntity<Unit> {
    return getDelegate().recordDeparture(premisesId, bookingId, cas1NewDeparture)
  }

  @Operation(
    tags = ["space bookings"],
    summary = "Posts a non arrival to a specified space booking",
    operationId = "recordNonArrival",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid premises ID or booking ID", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/premises/{premisesId}/space-bookings/{bookingId}/non-arrival"],
    produces = ["application/problem+json", "application/json"],
    consumes = ["application/json"],
  )
  fun recordNonArrival(@Parameter(description = "ID of the corresponding premises", required = true) @PathVariable("premisesId") premisesId: java.util.UUID, @Parameter(description = "ID of the space booking", required = true) @PathVariable("bookingId") bookingId: java.util.UUID, @Parameter(description = "", required = true) @RequestBody cas1NonArrival: Cas1NonArrival): ResponseEntity<Unit> {
    return getDelegate().recordNonArrival(premisesId, bookingId, cas1NonArrival)
  }
}
