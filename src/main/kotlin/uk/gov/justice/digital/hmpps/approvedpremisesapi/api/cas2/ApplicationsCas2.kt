/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.7.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Application
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2ApplicationSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.NewApplication
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UpdateApplication
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ValidationError

interface ApplicationsCas2 {

  fun getDelegate(): ApplicationsCas2Delegate = object : ApplicationsCas2Delegate {}

  @Operation(
    tags = ["Operations on CAS2 applications"],
    summary = "Abandons an in progress CAS2 application",
    operationId = "applicationsApplicationIdAbandonPut",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation"),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "409", description = "The application has been submitted", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/applications/{applicationId}/abandon"],
    produces = ["application/json"],
  )
  fun applicationsApplicationIdAbandonPut(@Parameter(description = "ID of the application", required = true) @PathVariable("applicationId") applicationId: java.util.UUID): ResponseEntity<Unit> {
    return getDelegate().applicationsApplicationIdAbandonPut(applicationId)
  }

  @Operation(
    tags = ["Operations on CAS2 applications"],
    summary = "Gets a single CAS2 application by its ID",
    operationId = "applicationsApplicationIdGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Application::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/applications/{applicationId}"],
    produces = ["application/json"],
  )
  fun applicationsApplicationIdGet(@Parameter(description = "ID of the application", required = true) @PathVariable("applicationId") applicationId: java.util.UUID): ResponseEntity<Application> {
    return getDelegate().applicationsApplicationIdGet(applicationId)
  }

  @Operation(
    tags = ["Operations on CAS2 applications"],
    summary = "Updates a CAS2 application",
    operationId = "applicationsApplicationIdPut",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Application::class))]),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.PUT],
    value = ["/applications/{applicationId}"],
    produces = ["application/json", "application/problem+json"],
    consumes = ["application/json"],
  )
  fun applicationsApplicationIdPut(@Parameter(description = "ID of the application", required = true) @PathVariable("applicationId") applicationId: java.util.UUID, @Parameter(description = "Information to update the application with", required = true) @RequestBody body: UpdateApplication): ResponseEntity<Application> {
    return getDelegate().applicationsApplicationIdPut(applicationId, body)
  }

  @Operation(
    tags = ["Operations on CAS2 applications"],
    summary = "List summaries of all CAS2 applications authorised for the logged in user",
    operationId = "applicationsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = Cas2ApplicationSummary::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/applications"],
    produces = ["application/json"],
  )
  fun applicationsGet(@Parameter(description = "Returns submitted applications if true, un submitted applications if false, and all applications if absent") @RequestParam(value = "isSubmitted", required = false) isSubmitted: kotlin.Boolean?, @Parameter(description = "Page number of results to return.  If blank, returns all results") @RequestParam(value = "page", required = false) page: kotlin.Int?, @Parameter(description = "Prison code of applications to return.  If blank, returns all results.") @RequestParam(value = "prisonCode", required = false) prisonCode: kotlin.String?): ResponseEntity<List<Cas2ApplicationSummary>> {
    return getDelegate().applicationsGet(isSubmitted, page, prisonCode)
  }

  @Operation(
    tags = ["Operations on CAS2 applications"],
    summary = "Creates a CAS2 application",
    operationId = "applicationsPost",
    description = """""",
    responses = [
      ApiResponse(responseCode = "201", description = "successful operation", content = [Content(schema = Schema(implementation = Application::class))]),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.POST],
    value = ["/applications"],
    produces = ["application/json", "application/problem+json"],
    consumes = ["application/json"],
  )
  fun applicationsPost(@Parameter(description = "Information to create a blank application with", required = true) @RequestBody body: NewApplication): ResponseEntity<Application> {
    return getDelegate().applicationsPost(body)
  }
}
