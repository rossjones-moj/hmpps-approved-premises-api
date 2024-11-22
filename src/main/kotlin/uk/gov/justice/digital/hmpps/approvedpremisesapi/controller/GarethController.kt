package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.GarethApiDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.ForbiddenProblem

@Service
class GarethController : GarethApiDelegate {

  override fun garethGet(xServiceName: ServiceName, month: Int): ResponseEntity<String> {
    if (xServiceName != ServiceName.cas2) {
      throw ForbiddenProblem()
    }
    return ResponseEntity.ok("Hello Gareth")
  }
}
