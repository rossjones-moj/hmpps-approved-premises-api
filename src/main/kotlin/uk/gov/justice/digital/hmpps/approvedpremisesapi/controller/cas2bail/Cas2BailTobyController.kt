package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller.cas2bail

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2bail.TobyCas2BailDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.ForbiddenProblem

@Service
class Cas2BailTobyController : TobyCas2BailDelegate {


  override fun tobyGet(xServiceName: ServiceName, month: Int): ResponseEntity<List<String>> {
    if (xServiceName != ServiceName.cas2) {
      throw ForbiddenProblem()
    }
    val list = listOf("Hello Toby")
    return ResponseEntity.ok(list)
  }
}


