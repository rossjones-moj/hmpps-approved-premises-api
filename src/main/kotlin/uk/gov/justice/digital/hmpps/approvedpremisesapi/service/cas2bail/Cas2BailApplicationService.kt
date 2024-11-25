package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2bail

import org.springframework.stereotype.Service


@Service("Cas2BailApplicationService")
class Cas2BailApplicationService {

  fun getApplications() : List<String> {
    return listOf("Hello Toby", "Hello Gareth")
  }


}