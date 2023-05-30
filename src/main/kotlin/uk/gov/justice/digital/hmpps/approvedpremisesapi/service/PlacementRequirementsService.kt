package uk.gov.justice.digital.hmpps.approvedpremisesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementRequirements
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CharacteristicRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequirementsEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequirementsRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PostcodeDistrictRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.ValidationErrors
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.validated
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.ValidatableActionResult
import java.time.OffsetDateTime
import java.util.UUID

@Service
class PlacementRequirementsService(
  private val postcodeDistrictRepository: PostcodeDistrictRepository,
  private val characteristicRepository: CharacteristicRepository,
  private val placementRequirementsRepository: PlacementRequirementsRepository,
) {
  fun createPlacementRequirements(assessment: AssessmentEntity, requirements: PlacementRequirements): ValidatableActionResult<PlacementRequirementsEntity> = validated {
    val postcodeDistrict = postcodeDistrictRepository.findByOutcode(requirements.location)
      ?: return@validated ValidatableActionResult.FieldValidationError(
        ValidationErrors().apply {
          this["$.postcodeDistrict"] = "doesNotExist"
        },
      )
    val desirableCriteria =
      characteristicRepository.findAllWherePropertyNameIn(requirements.desirableCriteria.map { it.toString() })
    val essentialCriteria =
      characteristicRepository.findAllWherePropertyNameIn(requirements.essentialCriteria.map { it.toString() })

    val application = (assessment.application as? ApprovedPremisesApplicationEntity)
      ?: throw RuntimeException("Only Approved Premises Assessments are currently supported for Placement Requests")

    val placementRequirementsEntity = placementRequirementsRepository.save(
      PlacementRequirementsEntity(
        id = UUID.randomUUID(),
        apType = requirements.type,
        gender = requirements.gender,
        postcodeDistrict = postcodeDistrict!!,
        radius = requirements.radius,
        desirableCriteria = desirableCriteria,
        essentialCriteria = essentialCriteria,
        createdAt = OffsetDateTime.now(),
        application = application,
        assessment = assessment,
      ),
    )

    return success(placementRequirementsEntity)
  }
}
