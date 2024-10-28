package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceBookingRequirements
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceCharacteristic
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CharacteristicEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PlacementRequirementsEntity

@Component
class Cas1SpaceBookingRequirementsTransformer {
  fun transformJpaToApi(jpa: PlacementRequirementsEntity, criteria: List<CharacteristicEntity>) = Cas1SpaceBookingRequirements(
    apType = jpa.apType,
    gender = jpa.gender,
    essentialCharacteristics = criteria.mapNotNull { it.asCas1SpaceCharacteristic() },
    desirableCharacteristics = emptyList(),
  )

  private fun CharacteristicEntity.asCas1SpaceCharacteristic() = try {
    Cas1SpaceCharacteristic.valueOf(this.propertyName!!)
  } catch (_: Exception) {
    null
  }
}
