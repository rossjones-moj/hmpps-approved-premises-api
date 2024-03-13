package uk.gov.justice.digital.hmpps.approvedpremisesapi.reporting.generator

import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationEntityReportRow
import uk.gov.justice.digital.hmpps.approvedpremisesapi.reporting.model.PlacementApplicationReportRow
import uk.gov.justice.digital.hmpps.approvedpremisesapi.reporting.properties.PlacementApplicationReportProperties

class PlacementApplicationReportGenerator :
  ReportGenerator<PlacementApplicationEntityReportRow, PlacementApplicationReportRow, PlacementApplicationReportProperties>(PlacementApplicationReportRow::class) {

  override fun filter(properties: PlacementApplicationReportProperties): (PlacementApplicationEntityReportRow) -> Boolean = {
    true
  }

  override val convert: PlacementApplicationEntityReportRow.(properties: PlacementApplicationReportProperties) -> List<PlacementApplicationReportRow> = { properties ->
    listOf(
      PlacementApplicationReportRow(
        placementRequestId = this.getId(),
        crn = this.getCrn(),
        tier = this.getTier(),
        placementRequestSubmittedAt = this.getPlacementApplicationSubmittedAt()?.toLocalDateTime()?.toLocalDate(),
        requestedArrivalDate = this.getRequestedArrivalDate()?.toLocalDate(),
        requestedDurationDays = this.getRequestedDurationDays(),
        decision = this.getDecision(),
        decisionMadeAt = this.getDecisionMadeAt()?.toLocalDateTime()?.toLocalDate(),
        applicationSubmittedAt = this.getApplicationSubmittedAt()?.toLocalDateTime()?.toLocalDate(),
        applicationAssessedDate = this.getApplicationAssessedDate()?.toLocalDate(),
        assessorCru = this.getAssessorCru(),
        assessmentDecision = this.deriveAssessmentDecision(),
        assessmentDecisionRationale = this.getAssessmentDecisionRationale(),
        ageInYears = this.getAgeInYears()?.toInt(),
        gender = this.getGender(),
        mappa = this.getMappa() ?: "Not found",
        offenceId = this.getOffenceId(),
        noms = this.getNoms(),
        sentenceType = this.getSentenceType(),
        releaseType = this.getReleaseType(),
        referralLdu = this.getReferralLdu(),
        referralTeam = this.getReferralTeam(),
        referralRegion = this.getReferralRegion(),
        referrerUsername = this.getReferrerUsername(),
        targetLocation = this.getTargetLocation(),
        applicationWithdrawalDate = this.getApplicationWithdrawalDate()?.toLocalDate(),
        applicationWithdrawalReason = this.getApplicationWithdrawalReason(),
        placementRequestType = this.getPlacementRequestType(),
        assessmentAppealCount = this.getAssessmentAppealCount(),
        lastAssessmentAppealedDecision = this.getLastAssessmentAppealedDecision(),
        lastAssessmentAppealedDate = this.getLastAssessmentAppealedDate()?.toLocalDate(),
        assessmentAppealedFromStatus = this.getAssessmentAppealedFromStatus(),
      ),
    )
  }

  private fun PlacementApplicationEntityReportRow.deriveAssessmentDecision() = when (this.getAssessmentAppealCount()) {
    0, null -> this.getAssessmentDecision()
    else -> this.getLastAssessmentAppealedDecision()
  }
}
