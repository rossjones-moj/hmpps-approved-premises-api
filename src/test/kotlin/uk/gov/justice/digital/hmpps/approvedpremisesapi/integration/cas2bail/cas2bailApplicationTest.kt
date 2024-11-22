package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.cas2bail

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenACas2Assessor

class cas2bailApplicationTest {


  @Nested
  inner class GetCas2BailApplicationsTest : IntegrationTestBase() {



    /*
    * 1. checks authorization - the JWT
    * 2.
    * */


    @Test
    fun `Get gareth endpoint without JWT returns 401`() {
      webTestClient.get()
        .uri("/cas2bail/toby?month=12")
        .header("X-Service-Name", ServiceName.approvedPremises.value)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }


    @Test
    fun `Get toby endpoint returns 200`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby?month=12")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isOk
      }
    }

  }


}

