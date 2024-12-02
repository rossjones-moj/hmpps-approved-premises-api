package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.DatePeriod
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementApplication
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementApplicationType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementDates
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.WithdrawPlacementRequestReason
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Withdrawable
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.WithdrawableType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementApplicationWithdrawalReason

@Component
class PlacementApplicationTransformer(
  private val objectMapper: ObjectMapper,
) {
  fun transformJpaToApi(jpa: PlacementApplicationEntity): PlacementApplication {
    val assessment = jpa.application.getLatestAssessment()!!
    val application = jpa.application

    return PlacementApplication(
      id = jpa.id,
      applicationId = application.id,
      applicationCompletedAt = application.submittedAt!!.toInstant(),
      assessmentId = assessment.id,
      assessmentCompletedAt = assessment.submittedAt!!.toInstant(),
      createdByUserId = jpa.createdByUser.id,
      schemaVersion = jpa.schemaVersion.id,
      createdAt = jpa.createdAt.toInstant(),
      data = if (jpa.data != null) objectMapper.readTree(jpa.data) else null,
      document = if (jpa.document != null) objectMapper.readTree(jpa.document) else null,
      outdatedSchema = !jpa.schemaUpToDate,
      submittedAt = jpa.submittedAt?.toInstant(),
      canBeWithdrawn = jpa.isInWithdrawableState(),
      isWithdrawn = jpa.isWithdrawn,
      withdrawalReason = getWithdrawalReason(jpa.withdrawalReason),
      type = PlacementApplicationType.additional,
      placementDates = jpa.placementDates.map { PlacementDates(it.expectedArrival, it.duration) },
    )
  }

  fun transformToWithdrawable(placementApplication: PlacementApplicationEntity): Withdrawable = Withdrawable(
    placementApplication.id,
    WithdrawableType.PLACEMENT_APPLICATION,
    placementApplication.placementDates.map {
      DatePeriod(it.expectedArrival, it.expectedDeparture())
    },
  )

  fun getWithdrawalReason(withdrawalReason: PlacementApplicationWithdrawalReason?): WithdrawPlacementRequestReason? = when (withdrawalReason) {
    PlacementApplicationWithdrawalReason.DUPLICATE_PLACEMENT_REQUEST -> WithdrawPlacementRequestReason.DUPLICATE_PLACEMENT_REQUEST
    PlacementApplicationWithdrawalReason.ALTERNATIVE_PROVISION_IDENTIFIED -> WithdrawPlacementRequestReason.ALTERNATIVE_PROVISION_IDENTIFIED
    PlacementApplicationWithdrawalReason.WITHDRAWN_BY_PP -> WithdrawPlacementRequestReason.WITHDRAWN_BY_PP
    PlacementApplicationWithdrawalReason.CHANGE_IN_CIRCUMSTANCES -> WithdrawPlacementRequestReason.CHANGE_IN_CIRCUMSTANCES
    PlacementApplicationWithdrawalReason.CHANGE_IN_RELEASE_DECISION -> WithdrawPlacementRequestReason.CHANGE_IN_RELEASE_DECISION
    PlacementApplicationWithdrawalReason.NO_CAPACITY_DUE_TO_LOST_BED -> WithdrawPlacementRequestReason.NO_CAPACITY_DUE_TO_LOST_BED
    PlacementApplicationWithdrawalReason.NO_CAPACITY_DUE_TO_PLACEMENT_PRIORITISATION -> WithdrawPlacementRequestReason.NO_CAPACITY_DUE_TO_PLACEMENT_PRIORITISATION
    PlacementApplicationWithdrawalReason.NO_CAPACITY -> WithdrawPlacementRequestReason.NO_CAPACITY
    PlacementApplicationWithdrawalReason.ERROR_IN_PLACEMENT_REQUEST -> WithdrawPlacementRequestReason.ERROR_IN_PLACEMENT_REQUEST
    PlacementApplicationWithdrawalReason.RELATED_APPLICATION_WITHDRAWN -> WithdrawPlacementRequestReason.RELATED_APPLICATION_WITHDRAWN
    null -> null
  }
}
