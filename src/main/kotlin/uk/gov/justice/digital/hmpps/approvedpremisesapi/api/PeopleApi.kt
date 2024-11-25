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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ActiveOffence
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Adjudication
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysRiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysRiskToSelf
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysSection
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysSections
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Person
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PersonAcctAlert
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PersonRisks
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PersonalTimeline
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PrisonCaseNote
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Problem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ValidationError

interface PeopleApi {

  fun getDelegate(): PeopleApiDelegate = object : PeopleApiDelegate {}

  @Operation(
    tags = ["default"],
    summary = "Returns the ACCT alerts for a Person",
    operationId = "peopleCrnAcctAlertsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = PersonAcctAlert::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/acct-alerts"],
    produces = ["application/json"],
  )
  fun peopleCrnAcctAlertsGet(@Parameter(description = "CRN of the Person to fetch ACCT alerts for", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<List<PersonAcctAlert>> {
    return getDelegate().peopleCrnAcctAlertsGet(crn)
  }

  @Operation(
    tags = ["default"],
    summary = "Returns the adjudications for a Person",
    operationId = "peopleCrnAdjudicationsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = Adjudication::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/adjudications"],
    produces = ["application/json"],
  )
  fun peopleCrnAdjudicationsGet(@Parameter(description = "CRN of the Person to fetch adjudications for", required = true) @PathVariable("crn") crn: kotlin.String, @Parameter(description = "CAS1 requests may be limited to adjudications for last 12 months only", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<List<Adjudication>> {
    return getDelegate().peopleCrnAdjudicationsGet(crn, xServiceName)
  }

  @Operation(
    tags = ["OASys"],
    summary = "Returns the Risk To Individual (known as Risk to Self on frontend) section of an OASys.",
    operationId = "peopleCrnOasysRiskToSelfGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = OASysRiskToSelf::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/oasys/risk-to-self"],
    produces = ["application/json"],
  )
  fun peopleCrnOasysRiskToSelfGet(@Parameter(description = "CRN of the Person to fetch latest OASys", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<OASysRiskToSelf> {
    return getDelegate().peopleCrnOasysRiskToSelfGet(crn)
  }

  @Operation(
    tags = ["OASys"],
    summary = "Returns the Risk of Serious Harm section of an OASys.",
    operationId = "peopleCrnOasysRoshGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = OASysRiskOfSeriousHarm::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/oasys/rosh"],
    produces = ["application/json"],
  )
  fun peopleCrnOasysRoshGet(@Parameter(description = "CRN of the Person to fetch latest OASys", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<OASysRiskOfSeriousHarm> {
    return getDelegate().peopleCrnOasysRoshGet(crn)
  }

  @Operation(
    tags = ["OASys"],
    summary = "Returns OASys sections to support an Application.  The Supporting Information sections are returned if linked to harm and optionally if their section number appears in the selected-sections query parameter.",
    operationId = "peopleCrnOasysSectionsGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = OASysSections::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/oasys/sections"],
    produces = ["application/json"],
  )
  fun peopleCrnOasysSectionsGet(@Parameter(description = "CRN of the Person to fetch latest OASys selection", required = true) @PathVariable("crn") crn: kotlin.String, @Parameter(description = "Section numbers for optional (not Linked to Harm) suppprting information") @RequestParam(value = "selected-sections", required = false) selectedSections: kotlin.collections.List<kotlin.Int>?): ResponseEntity<OASysSections> {
    return getDelegate().peopleCrnOasysSectionsGet(crn, selectedSections)
  }

  @Operation(
    tags = ["OASys"],
    summary = "Returns the importable sections of OASys including details of links to harm and reoffending",
    operationId = "peopleCrnOasysSelectionGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = OASysSection::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/oasys/selection"],
    produces = ["application/json"],
  )
  fun peopleCrnOasysSelectionGet(@Parameter(description = "CRN of the Person to fetch latest OASys selection", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<List<OASysSection>> {
    return getDelegate().peopleCrnOasysSelectionGet(crn)
  }

  @Operation(
    tags = ["default"],
    summary = "Returns all active offences for a Person.",
    operationId = "peopleCrnOffencesGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = ActiveOffence::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/offences"],
    produces = ["application/json"],
  )
  fun peopleCrnOffencesGet(@Parameter(description = "CRN of the Person to fetch active offences for", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<List<ActiveOffence>> {
    return getDelegate().peopleCrnOffencesGet(crn)
  }

  @Operation(
    tags = ["default"],
    summary = "Returns the prison case notes for a Person",
    operationId = "peopleCrnPrisonCaseNotesGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(array = ArraySchema(schema = Schema(implementation = PrisonCaseNote::class)))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/prison-case-notes"],
    produces = ["application/json"],
  )
  fun peopleCrnPrisonCaseNotesGet(@Parameter(description = "CRN of the Person to fetch prison case notes for", required = true) @PathVariable("crn") crn: kotlin.String, @Parameter(description = "CAS1 requests may limit returned case note types", `in` = ParameterIn.HEADER, required = true, schema = Schema(allowableValues = ["approved-premises", "cas2", "temporary-accommodation"])) @RequestHeader(value = "X-Service-Name", required = true) xServiceName: ServiceName): ResponseEntity<List<PrisonCaseNote>> {
    return getDelegate().peopleCrnPrisonCaseNotesGet(crn, xServiceName)
  }

  @Operation(
    tags = ["default"],
    summary = "Returns the risks for a Person",
    operationId = "peopleCrnRisksGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = PersonRisks::class))]),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/risks"],
    produces = ["application/json", "application/problem+json"],
  )
  fun peopleCrnRisksGet(@Parameter(description = "CRN of the Person to fetch risks for", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<PersonRisks> {
    return getDelegate().peopleCrnRisksGet(crn)
  }

  @Operation(
    tags = ["default"],
    summary = "Returns a timeline of all applications for a Person.",
    operationId = "peopleCrnTimelineGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = PersonalTimeline::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/{crn}/timeline"],
    produces = ["application/json"],
  )
  fun peopleCrnTimelineGet(@Parameter(description = "CRN of the Person to fetch the timeline for", required = true) @PathVariable("crn") crn: kotlin.String): ResponseEntity<PersonalTimeline> {
    return getDelegate().peopleCrnTimelineGet(crn)
  }

  @Operation(
    tags = ["default"],
    summary = "Searches for a Person by their CRN",
    operationId = "peopleSearchGet",
    description = """""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = Person::class))]),
      ApiResponse(responseCode = "400", description = "invalid params", content = [Content(schema = Schema(implementation = ValidationError::class))]),
      ApiResponse(responseCode = "404", description = "invalid CRN", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "401", description = "not authenticated", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "403", description = "unauthorised", content = [Content(schema = Schema(implementation = Problem::class))]),
      ApiResponse(responseCode = "500", description = "unexpected error", content = [Content(schema = Schema(implementation = Problem::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/people/search"],
    produces = ["application/json", "application/problem+json"],
  )
  fun peopleSearchGet(@Parameter(description = "CRN to search for", required = true) @RequestParam(value = "crn", required = true) crn: kotlin.String): ResponseEntity<Person> {
    return getDelegate().peopleSearchGet(crn)
  }
}
