package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2Demand
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.*
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service("Cas2DemandService")
class DemandService(
  private val demandRepository: Cas2DemandRepository,
) {

  @Transactional
  fun createCas2Demand(cas2Demand: Cas2Demand): Cas2DemandEntity =
    demandRepository.save(
      Cas2DemandEntity(
        id = UUID.randomUUID(),
        identifier = cas2Demand.identifier,
        locationType = cas2Demand.locationType,
        location = cas2Demand.location,
        primaryReason = cas2Demand.primaryReason,
        secondaryReason = cas2Demand.secondaryReason,
        createdAt = convertInstant(cas2Demand.createdAt),
        decidedAt = convertInstant(cas2Demand.decidedAt),
      ),
    )

  fun getDemand(id: UUID): CasResult<Cas2DemandEntity> {
    val demandEntity = demandRepository.findByIdOrNull(id)
      ?: return CasResult.NotFound()

    return CasResult.Success(demandEntity)
  }
}

fun convertInstant(instant: Instant?): OffsetDateTime {
  return instant?.let {
    OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
  } ?: run {
    OffsetDateTime.now()
  }
}