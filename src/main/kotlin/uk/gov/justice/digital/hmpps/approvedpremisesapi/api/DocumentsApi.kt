/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (7.7.0).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package uk.gov.justice.digital.hmpps.approvedpremisesapi.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem

interface DocumentsApi {

  fun getDelegate(): DocumentsApiDelegate = object : DocumentsApiDelegate {}

  @Operation(
    tags = ["Application data"],
    summary = "Downloads a document",
    operationId = "documentsCrnDocumentIdGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = org.springframework.core.io.Resource::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid applicationId or documentId", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/documents/{crn}/{documentId}"],
    produces = ["application/octet-stream", "application/json"],
  )
  fun documentsCrnDocumentIdGet(@Parameter(description = "CRN of the Person the document is associated with", required = true) @PathVariable("crn") crn: kotlin.String, @Parameter(description = "ID of the document", required = true) @PathVariable("documentId") documentId: java.util.UUID): ResponseEntity<org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody> {
    return getDelegate().documentsCrnDocumentIdGet(crn, documentId)
  }
}
