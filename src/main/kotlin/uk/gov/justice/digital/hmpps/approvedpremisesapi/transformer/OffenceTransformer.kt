package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ActiveOffence
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.community.Conviction
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.deliuscontext.CaseDetail

@Component
class OffenceTransformer {
  fun transformToApi(conviction: Conviction) = conviction.offences?.map {
    ActiveOffence(
      deliusEventNumber = conviction.index,
      offenceDescription = nonRedundantDescription(it.detail.mainCategoryDescription, it.detail.subCategoryDescription),
      offenceId = it.offenceId,
      convictionId = conviction.convictionId,
      offenceDate = it.offenceDate?.toLocalDate(),
    )
  } ?: emptyList()

  fun transformToApi(caseDetail: CaseDetail) = caseDetail.offences.map {
    ActiveOffence(
      deliusEventNumber = it.eventNumber,
      offenceDescription = nonRedundantDescription(it.mainCategoryDescription, it.subCategoryDescription),
      offenceId = it.id,
      convictionId = it.eventId,
      offenceDate = it.date,
    )
  }

  private fun nonRedundantDescription(mainCategoryDescription: String, subCategoryDescription: String) = if (mainCategoryDescription != subCategoryDescription) {
    "$mainCategoryDescription - $subCategoryDescription"
  } else {
    mainCategoryDescription
  }
}
