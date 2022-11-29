package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.PeopleApiDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Adjudication
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysOffenceAnalysis
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysRiskManagementPlan
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysRiskOfSeriousHarmSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysRisksToTheIndividual
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.OASysSection
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Person
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PersonAcctAlert
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PersonRisks
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PrisonCaseNote
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.community.OffenderDetailSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.ForbiddenProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.InternalServerErrorProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.NotFoundProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.HttpAuthService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.OffenderService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.AdjudicationTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.AlertTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.NeedsDetailsTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.OffenceAnalysisTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.PersonTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.PrisonCaseNoteTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.RiskManagementPlanTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.RiskToTheIndividualTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.RisksTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.RoshSummaryTransformer

@Service
class PeopleController(
  private val httpAuthService: HttpAuthService,
  private val offenderService: OffenderService,
  private val personTransformer: PersonTransformer,
  private val risksTransformer: RisksTransformer,
  private val prisonCaseNoteTransformer: PrisonCaseNoteTransformer,
  private val adjudicationTransformer: AdjudicationTransformer,
  private val alertTransformer: AlertTransformer,
  private val offenceAnalysisTransformer: OffenceAnalysisTransformer,
  private val riskManagementPlanTransformer: RiskManagementPlanTransformer,
  private val roshSummaryTransformer: RoshSummaryTransformer,
  private val needsDetailsTransformer: NeedsDetailsTransformer,
  private val riskToTheIndividualTransformer: RiskToTheIndividualTransformer
) : PeopleApiDelegate {
  override fun peopleSearchGet(crn: String): ResponseEntity<Person> {
    val offenderDetails = getOffenderDetails(crn)

    if (offenderDetails.otherIds.nomsNumber == null) {
      throw InternalServerErrorProblem("No nomsNumber present for CRN")
    }

    val inmateDetail = when (val inmateDetailResult = offenderService.getInmateDetailByNomsNumber(offenderDetails.otherIds.nomsNumber)) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(offenderDetails.otherIds.nomsNumber, "Inmate")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> inmateDetailResult.entity
    }

    return ResponseEntity.ok(
      personTransformer.transformModelToApi(offenderDetails, inmateDetail)
    )
  }

  override fun peopleCrnRisksGet(crn: String): ResponseEntity<PersonRisks> {
    val principal = httpAuthService.getDeliusPrincipalOrThrow()

    val risks = when (val risksResult = offenderService.getRiskByCrn(crn, principal.token.tokenValue, principal.name)) {
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Success -> risksResult.entity
    }

    return ResponseEntity.ok(risksTransformer.transformDomainToApi(risks))
  }

  override fun peopleCrnPrisonCaseNotesGet(crn: String): ResponseEntity<List<PrisonCaseNote>> {
    val offenderDetails = getOffenderDetails(crn)

    if (offenderDetails.otherIds.nomsNumber == null) {
      throw InternalServerErrorProblem("No nomsNumber present for CRN")
    }

    val nomsNumber = offenderDetails.otherIds.nomsNumber

    val prisonCaseNotesResult = offenderService.getPrisonCaseNotesByNomsNumber(nomsNumber)
    val prisonCaseNotes = when (prisonCaseNotesResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Inmate")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> prisonCaseNotesResult.entity
    }

    return ResponseEntity.ok(prisonCaseNotes.map(prisonCaseNoteTransformer::transformModelToApi))
  }

  override fun peopleCrnAdjudicationsGet(crn: String): ResponseEntity<List<Adjudication>> {
    val offenderDetails = getOffenderDetails(crn)

    if (offenderDetails.otherIds.nomsNumber == null) {
      throw InternalServerErrorProblem("No nomsNumber present for CRN")
    }

    val nomsNumber = offenderDetails.otherIds.nomsNumber

    val adjudicationsResult = offenderService.getAdjudicationsByNomsNumber(nomsNumber)
    val adjudications = when (adjudicationsResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Inmate")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> adjudicationsResult.entity
    }

    return ResponseEntity.ok(adjudicationTransformer.transformToApi(adjudications))
  }

  override fun peopleCrnAcctAlertsGet(crn: String): ResponseEntity<List<PersonAcctAlert>> {
    val offenderDetails = getOffenderDetails(crn)

    if (offenderDetails.otherIds.nomsNumber == null) {
      throw InternalServerErrorProblem("No nomsNumber present for CRN")
    }

    val nomsNumber = offenderDetails.otherIds.nomsNumber

    val acctAlertsResult = offenderService.getAcctAlertsByNomsNumber(nomsNumber)
    val acctAlerts = when (acctAlertsResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Inmate")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> acctAlertsResult.entity
    }

    return ResponseEntity.ok(acctAlerts.map(alertTransformer::transformToApi))
  }

  override fun peopleCrnOasysSelectionGet(crn: String): ResponseEntity<List<OASysSection>> {
    getOffenderDetails(crn)

    val needsResult = offenderService.getOASysNeeds(crn)

    val needs = when (needsResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> needsResult.entity
    }

    return ResponseEntity.ok(needsDetailsTransformer.transformToApi(needs))
  }

  override fun peopleCrnOasysOffenceAnalysisGet(crn: String): ResponseEntity<OASysOffenceAnalysis> {
    getOffenderDetails(crn)

    val offenceDetailsResult = offenderService.getOASysOffenceDetails(crn)

    val offenceDetails = when (offenceDetailsResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> offenceDetailsResult.entity
    }

    return ResponseEntity.ok(offenceAnalysisTransformer.transformToApi(offenceDetails))
  }

  override fun peopleCrnOasysRiskManagementPlanGet(crn: String): ResponseEntity<OASysRiskManagementPlan> {
    getOffenderDetails(crn)

    val riskManagementResult = offenderService.getOASysRiskManagementPlan(crn)

    val riskManagement = when (riskManagementResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> riskManagementResult.entity
    }

    return ResponseEntity.ok(riskManagementPlanTransformer.transformToApi(riskManagement))
  }

  override fun peopleCrnOasysRoshSummaryGet(crn: String): ResponseEntity<OASysRiskOfSeriousHarmSummary> {
    getOffenderDetails(crn)

    val roshSummaryResult = offenderService.getOASysRoshSummary(crn)

    val roshSummary = when (roshSummaryResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> roshSummaryResult.entity
    }

    return ResponseEntity.ok(roshSummaryTransformer.transformToApi(roshSummary))
  }

  override fun peopleCrnOasysRisksToTheIndividualGet(crn: String): ResponseEntity<OASysRisksToTheIndividual> {
    getOffenderDetails(crn)

    val risksToTheIndividualResult = offenderService.getOASysRiskToTheIndividual(crn)

    val risksToTheIndividual = when (risksToTheIndividualResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> risksToTheIndividualResult.entity
    }

    return ResponseEntity.ok(riskToTheIndividualTransformer.transformToApi(risksToTheIndividual))
  }

  private fun getOffenderDetails(crn: String): OffenderDetailSummary {
    val principal = httpAuthService.getDeliusPrincipalOrThrow()
    val username = principal.name

    val offenderDetailsResult = offenderService.getOffenderByCrn(crn, username)
    return when (offenderDetailsResult) {
      is AuthorisableActionResult.NotFound -> throw NotFoundProblem(crn, "Person")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> offenderDetailsResult.entity
    }
  }
}
