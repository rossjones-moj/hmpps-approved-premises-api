package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller.cas2

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2.DemandCas2Delegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2Demand
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.ForbiddenProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.NotFoundProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.NomisUserService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2.DemandService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.cas2.DemandTransformer

import java.net.URI
import java.util.*

@Service("DemandController")
class DemandController(
  private val userService: NomisUserService,
  private val demandService: DemandService,
  private val demandTransformer: DemandTransformer,
) : DemandCas2Delegate {

  override fun demandPost(cas2Demand: Cas2Demand): ResponseEntity<Cas2Demand> {
    val entity = demandService.createCas2Demand(cas2Demand)

    return ResponseEntity
      .created(URI.create("/cas2/demand/${entity.id}"))
      .body(demandTransformer.transformJpaToApi(entity))
  }

  override fun demandIdGet(id: UUID): ResponseEntity<Cas2Demand> {
    val demand = when (
      val demandResult = demandService.getDemand(id)
    ) {
      is CasResult.NotFound -> null
      is CasResult.Unauthorised -> throw ForbiddenProblem()
      is CasResult.Success -> demandResult.value
      else -> null
    }

    if (demand != null) {
      return ResponseEntity.ok(demandTransformer.transformJpaToApi(demand))
    }

    throw NotFoundProblem(id, "Demand")
  }

}