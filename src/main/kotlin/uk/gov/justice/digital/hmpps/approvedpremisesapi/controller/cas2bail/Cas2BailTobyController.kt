package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller.cas2bail

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2bail.TobyCas2BailDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.problem.ForbiddenProblem
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2.ApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2bail.Cas2BailApplicationService

@Service
class Cas2BailTobyController(
  private val cas2BailApplicationService: Cas2BailApplicationService,
) : TobyCas2BailDelegate {


  override fun tobyGet(xServiceName: ServiceName, month: Int): ResponseEntity<List<String>> {
    if (xServiceName != ServiceName.cas2) {
      throw ForbiddenProblem()
    }
//    val list = listOf("Hello Toby", "Hello Gareth")
    val list = cas2BailApplicationService.getApplications()
    return ResponseEntity.ok(list)

  }
}


