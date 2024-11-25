/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.7.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package uk.gov.justice.digital.hmpps.approvedpremisesapi.api

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.enums.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.security.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApArea
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.CancellationReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Characteristic
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.DepartureReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.DestinationProvider
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.LocalAuthorityArea
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.LostBedReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.MoveOnCategory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.NonArrivalReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ProbationRegion
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ReferralRejectionReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SupervisingOfficer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SupervisingProvider
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SupervisingTeam
import kotlin.collections.List

interface ReferenceDataApi {

  fun getDelegate(): ReferenceDataApiDelegate = object : ReferenceDataApiDelegate {}

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all probation regions",
    operationId = "referenceDataApAreasGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = ApArea::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/ap-areas"],
    produces = ["application/json"],
  )
  fun referenceDataApAreasGet(): ResponseEntity<List<ApArea>> {
    return getDelegate().referenceDataApAreasGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all cancellation reasons",
    operationId = "referenceDataCancellationReasonsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = CancellationReason::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/cancellation-reasons"],
    produces = ["application/json"],
  )
  fun referenceDataCancellationReasonsGet(@Parameter(description = "If given, only departure reasons for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?): ResponseEntity<List<CancellationReason>> {
    return getDelegate().referenceDataCancellationReasonsGet(xServiceName)
  }

  @Operation(
    tags = ["Characteristics"],
    summary = "Lists all available characteristics",
    operationId = "referenceDataCharacteristicsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = Characteristic::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/characteristics"],
    produces = ["application/json"],
  )
  fun referenceDataCharacteristicsGet(@Parameter(description = "If given, only characteristics for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?, @Parameter(description = "Specifies whether inactive characteristics should be provided. Defaults to `false`.") @RequestParam(value = "includeInactive", required = false) includeInactive: kotlin.Boolean?): ResponseEntity<List<Characteristic>> {
    return getDelegate().referenceDataCharacteristicsGet(xServiceName, includeInactive)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all departure reasons",
    operationId = "referenceDataDepartureReasonsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = DepartureReason::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/departure-reasons"],
    produces = ["application/json"],
  )
  fun referenceDataDepartureReasonsGet(@Parameter(description = "If given, only departure reasons for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?, @Parameter(description = "Specifies whether inactive departure reasons should be provided. Defaults to `false`.") @RequestParam(value = "includeInactive", required = false) includeInactive: kotlin.Boolean?): ResponseEntity<List<DepartureReason>> {
    return getDelegate().referenceDataDepartureReasonsGet(xServiceName, includeInactive)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all destination providers for departures",
    operationId = "referenceDataDestinationProvidersGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = DestinationProvider::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/destination-providers"],
    produces = ["application/json"],
  )
  fun referenceDataDestinationProvidersGet(): ResponseEntity<List<DestinationProvider>> {
    return getDelegate().referenceDataDestinationProvidersGet()
  }

  @Operation(
    tags = ["Local Authorities"],
    summary = "Lists all local authorities",
    operationId = "referenceDataLocalAuthorityAreasGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = LocalAuthorityArea::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/local-authority-areas"],
    produces = ["application/json"],
  )
  fun referenceDataLocalAuthorityAreasGet(): ResponseEntity<List<LocalAuthorityArea>> {
    return getDelegate().referenceDataLocalAuthorityAreasGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all reasons for losing beds",
    operationId = "referenceDataLostBedReasonsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = LostBedReason::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/lost-bed-reasons"],
    produces = ["application/json"],
  )
  fun referenceDataLostBedReasonsGet(@Parameter(description = "If given, only lost bed reasons for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?): ResponseEntity<List<LostBedReason>> {
    return getDelegate().referenceDataLostBedReasonsGet(xServiceName)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all move-on categories for departures",
    operationId = "referenceDataMoveOnCategoriesGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = MoveOnCategory::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/move-on-categories"],
    produces = ["application/json"],
  )
  fun referenceDataMoveOnCategoriesGet(@Parameter(description = "If given, only move-on categories for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?, @Parameter(description = "Specifies whether inactive move-on categories should be provided. Defaults to `false`.") @RequestParam(value = "includeInactive", required = false) includeInactive: kotlin.Boolean?): ResponseEntity<List<MoveOnCategory>> {
    return getDelegate().referenceDataMoveOnCategoriesGet(xServiceName, includeInactive)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists reasons for non-arrivals",
    operationId = "referenceDataNonArrivalReasonsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = NonArrivalReason::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/non-arrival-reasons"],
    produces = ["application/json"],
  )
  fun referenceDataNonArrivalReasonsGet(): ResponseEntity<List<NonArrivalReason>> {
    return getDelegate().referenceDataNonArrivalReasonsGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists probation delivery units, optionally filtered by region",
    operationId = "referenceDataProbationDeliveryUnitsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = ProbationDeliveryUnit::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/probation-delivery-units"],
    produces = ["application/json"],
  )
  fun referenceDataProbationDeliveryUnitsGet(@Parameter(description = "If given, only probation delivery units for this region will be returned") @RequestParam(value = "probationRegionId", required = false) probationRegionId: java.util.UUID?): ResponseEntity<List<ProbationDeliveryUnit>> {
    return getDelegate().referenceDataProbationDeliveryUnitsGet(probationRegionId)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all probation regions",
    operationId = "referenceDataProbationRegionsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = ProbationRegion::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/probation-regions"],
    produces = ["application/json"],
  )
  fun referenceDataProbationRegionsGet(): ResponseEntity<List<ProbationRegion>> {
    return getDelegate().referenceDataProbationRegionsGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all referral rejection reasons",
    operationId = "referenceDataReferralRejectionReasonsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = ReferralRejectionReason::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/referral-rejection-reasons"],
    produces = ["application/json"],
  )
  fun referenceDataReferralRejectionReasonsGet(@Parameter(description = "If given, only referral rejection reasons for this service will be returned", `in` = ParameterIn.HEADER, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = false) xServiceName: ServiceName?): ResponseEntity<List<ReferralRejectionReason>> {
    return getDelegate().referenceDataReferralRejectionReasonsGet(xServiceName)
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all supervising officers for arrivals",
    operationId = "referenceDataSupervisingOfficersGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = SupervisingOfficer::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/supervising-officers"],
    produces = ["application/json"],
  )
  fun referenceDataSupervisingOfficersGet(): ResponseEntity<List<SupervisingOfficer>> {
    return getDelegate().referenceDataSupervisingOfficersGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all supervising providers for arrivals",
    operationId = "referenceDataSupervisingProvidersGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = SupervisingProvider::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/supervising-providers"],
    produces = ["application/json"],
  )
  fun referenceDataSupervisingProvidersGet(): ResponseEntity<List<SupervisingProvider>> {
    return getDelegate().referenceDataSupervisingProvidersGet()
  }

  @Operation(
    tags = ["Reference Data"],
    summary = "Lists all supervising teams for arrivals",
    operationId = "referenceDataSupervisingTeamsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = SupervisingTeam::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/reference-data/supervising-teams"],
    produces = ["application/json"],
  )
  fun referenceDataSupervisingTeamsGet(): ResponseEntity<List<SupervisingTeam>> {
    return getDelegate().referenceDataSupervisingTeamsGet()
  }
}
