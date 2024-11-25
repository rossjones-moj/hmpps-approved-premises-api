/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.7.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package uk.gov.justice.digital.hmpps.approvedpremisesapi.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApprovedPremisesUserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.SortDirection
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.User
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UserQualification
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UserRolesAndQualifications
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UserSortField
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UserSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ValidationError

interface UsersApi {

  fun getDelegate(): UsersApiDelegate = object : UsersApiDelegate {}

  @Operation(
    tags = ["Auth"],
    summary = "Returns a user with match on name",
    operationId = "usersDeliusGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successfully retrieved user", content = [Content(schema = Schema(implementation = User::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "User not found", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/users/delius"],
    produces = ["application/json"],
  )
  fun usersDeliusGet(@Parameter(description = "Name of the user", required = true) @RequestParam(value = "name", required = true) name: kotlin.String, @Parameter(description = "Filters the user details to those relevant to the specified service.", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<User> {
    return getDelegate().usersDeliusGet(name, xServiceName)
  }

  @Operation(
    tags = ["Auth"],
    summary = "Returns a list of users. If only the user's ID and Name are required, use /users/summary",
    operationId = "usersGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successfully retrieved users", content = [Content(array = ArraySchema(schema = Schema(implementation = User::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/users"],
    produces = ["application/json"],
  )
  fun usersGet(@Parameter(description = "Filters the user details to those relevant to the specified service.", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName, @Parameter(description = "Only return users with the provided role(s)") @RequestParam(value = "roles", required = false) roles: kotlin.collections.List<ApprovedPremisesUserRole>?, @Parameter(description = "Only return users with the provided qualification(s)") @RequestParam(value = "qualifications", required = false) qualifications: kotlin.collections.List<UserQualification>?, @Parameter(description = "Probation region ID to filter results by") @RequestParam(value = "probationRegionId", required = false) probationRegionId: java.util.UUID?, @Parameter(description = "Approved premises area ID to filter results by.  Deprecated, Use cruManagementAreaId instead.") @RequestParam(value = "apAreaId", required = false) apAreaId: java.util.UUID?, @Parameter(description = "filter by CRU management area ID") @RequestParam(value = "cruManagementAreaId", required = false) cruManagementAreaId: java.util.UUID?, @Parameter(description = "Page number of results to return. If blank, returns all results") @RequestParam(value = "page", required = false) page: kotlin.Int?, @Parameter(description = "Which field to sort the results by. If blank, will sort by createdAt", schema = Schema(allowableValues = ["name"])) @RequestParam(value = "sortBy", required = false) sortBy: UserSortField?, @Parameter(description = "The direction to sort the results by. If blank, will sort in descending order", schema = Schema(allowableValues = ["asc", "desc"])) @RequestParam(value = "sortDirection", required = false) sortDirection: SortDirection?): ResponseEntity<List<User>> {
    return getDelegate().usersGet(xServiceName, roles, qualifications, probationRegionId, apAreaId, cruManagementAreaId, page, sortBy, sortDirection)
  }

  @Operation(
    tags = ["default"],
    summary = "Deletes the user. Deprecated. Instead use DELETE /cas1/user/{userid}",
    operationId = "usersIdDelete",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.DELETE],
    value = ["/users/{id}"],
    produces = ["application/problem+json", "application/json"],
  )
  fun usersIdDelete(@Parameter(description = "ID of the user", required = true) @PathVariable("id") id: java.util.UUID, @Parameter(description = "Only users for this service will be allowed to delete a user", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<Unit> {
    return getDelegate().usersIdDelete(id, xServiceName)
  }

  @Operation(
    tags = ["default"],
    summary = "Get information about a specific user",
    operationId = "usersIdGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successfully retrieved information on user", content = [Content(schema = Schema(implementation = User::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/users/{id}"],
    produces = ["application/json"],
  )
  fun usersIdGet(@Parameter(description = "Id of the user", required = true) @PathVariable("id") id: java.util.UUID, @Parameter(description = "If given, only users for this service will be returned", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<User> {
    return getDelegate().usersIdGet(id, xServiceName)
  }

  @Operation(
    tags = ["default"],
    summary = "Update information about a specific user's roles and qualifications. Deprecated. Instead use PUT /cas1/user/{userid}",
    operationId = "usersIdPut",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "Successfully added information about user roles and qualifications", content = [Content(schema = Schema(implementation = User::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/users/{id}"],
    produces = ["application/json"],
    consumes = ["application/json"],
  )
  fun usersIdPut(@Parameter(description = "If given, only users for this service will be returned", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName, @Parameter(description = "Id of the user", required = true) @PathVariable("id") id: java.util.UUID, @Parameter(description = "", required = true) @RequestBody userRolesAndQualifications: UserRolesAndQualifications): ResponseEntity<User> {
    return getDelegate().usersIdPut(xServiceName, id, userRolesAndQualifications)
  }

  @Operation(
    tags = ["Auth"],
    summary = "Returns a list of users with partial match on name",
    operationId = "usersSearchGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successfully retrieved users", content = [Content(array = ArraySchema(schema = Schema(implementation = User::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/users/search"],
    produces = ["application/json"],
  )
  fun usersSearchGet(@Parameter(description = "Name or partial name of the user", required = true) @RequestParam(value = "name", required = true) name: kotlin.String, @Parameter(description = "Filters the user details to those relevant to the specified service.", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<List<User>> {
    return getDelegate().usersSearchGet(name, xServiceName)
  }

  @Operation(
    tags = ["Auth"],
    summary = "Returns a list of user summaries (i.e. id and name only)",
    operationId = "usersSummaryGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successfully retrieved users", content = [Content(array = ArraySchema(schema = Schema(implementation = UserSummary::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/users/summary"],
    produces = ["application/json"],
  )
  fun usersSummaryGet(@Parameter(description = "Filters the user details to those relevant to the specified service.", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName, @Parameter(description = "Only return users with the provided role(s)") @RequestParam(value = "roles", required = false) roles: kotlin.collections.List<ApprovedPremisesUserRole>?, @Parameter(description = "Only return users with the provided qualification(s)") @RequestParam(value = "qualifications", required = false) qualifications: kotlin.collections.List<UserQualification>?, @Parameter(description = "Probation region ID to filter results by") @RequestParam(value = "probationRegionId", required = false) probationRegionId: java.util.UUID?, @Parameter(description = "Approved premises area ID to filter results by") @RequestParam(value = "apAreaId", required = false) apAreaId: java.util.UUID?, @Parameter(description = "Page number of results to return. If blank, returns all results") @RequestParam(value = "page", required = false) page: kotlin.Int?, @Parameter(description = "Which field to sort the results by. If blank, will sort by createdAt", schema = Schema(allowableValues = ["name"])) @RequestParam(value = "sortBy", required = false) sortBy: UserSortField?, @Parameter(description = "The direction to sort the results by. If blank, will sort in descending order", schema = Schema(allowableValues = ["asc", "desc"])) @RequestParam(value = "sortDirection", required = false) sortDirection: SortDirection?): ResponseEntity<List<UserSummary>> {
    return getDelegate().usersSummaryGet(xServiceName, roles, qualifications, probationRegionId, apAreaId, page, sortBy, sortDirection)
  }
}
