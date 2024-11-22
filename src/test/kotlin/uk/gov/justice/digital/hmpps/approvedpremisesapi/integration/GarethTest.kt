package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenACas2Assessor
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenACas2PomUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenAUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRole

class GarethTest {

  @Nested
  inner class GetGarethTest : IntegrationTestBase() {

    @Test
    fun `Get gareth endpoint without JWT returns 401`() {
      webTestClient.get()
        .uri("/gareth?month=12")
        .header("X-Service-Name", ServiceName.approvedPremises.value)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Get gareth endpoint returns 200`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/gareth?month=12")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.approvedPremises.value)
          .exchange()
          .expectStatus()
          .isOk
      }
    }

    @Test
    fun `Get gareth endpoint returns 400 if no X-Service-Name`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/gareth?month=12")
          .header("Authorization", "Bearer $jwt")
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get gareth endpoint returns 400 if no month parameter included`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/gareth")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get gareth endpoint returns 400 if month parameter is not an Int`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/gareth?month=February")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get gareth endpoint returns 403 if different role to Cas2Assessor`() {
      givenACas2PomUser { _, jwt ->
        webTestClient.get()
          .uri("/gareth?month=February")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isForbidden
      }
    }

    @Test
    fun `Get gareth endpoint returns 403 service is not cas2`() {
      givenACas2PomUser { _, jwt ->
        webTestClient.get()
          .uri("/gareth?month=February")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.approvedPremises.value)
          .exchange()
          .expectStatus()
          .isForbidden
      }
    }

  }
}
