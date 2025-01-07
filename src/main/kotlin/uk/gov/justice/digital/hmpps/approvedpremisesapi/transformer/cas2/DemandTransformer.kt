package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.cas2

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2Demand
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas2DemandEntity

@Component("Cas2DemandTransformer")
class DemandTransformer(
) {

  fun transformJpaToApi(jpa: Cas2DemandEntity): Cas2Demand {
    return Cas2Demand(
      id = jpa.id,
      identifier = jpa.identifier,
      locationType = jpa.locationType,
      location = jpa.location,
      primaryReason = jpa.primaryReason,
      secondaryReason = jpa.secondaryReason,
      createdAt = jpa.createdAt.toInstant(),
      decidedAt = jpa.createdAt.toInstant(),
    ) }
}
