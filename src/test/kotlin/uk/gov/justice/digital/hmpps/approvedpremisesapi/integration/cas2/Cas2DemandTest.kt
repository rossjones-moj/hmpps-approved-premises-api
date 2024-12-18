package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.cas2

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2Demand
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.IntegrationTestBase


class Cas2DemandTest : IntegrationTestBase() {

  @Nested
  inner class ControlsOnExternalUsers {
    @Test
    fun `accessing demand endpoint is forbidden`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "auth",
        roles = listOf("ROLE_CAS2_ASSESSOR"),
      )

      webTestClient.get()
        .uri("/cas2/demand")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isForbidden
    }
  }

  @Nested
  inner class ControlsOnInternalUsers {
    @Test
    fun `submitting demand is not allowed for assessor`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
        roles = listOf("ROLE_CAS2_ASSESSOR"),
      )

      webTestClient.post()
        .uri("/cas2/demand")
        .header("Authorization", "Bearer $jwt")
        .bodyValue(
          Cas2Demand(
            identifier = "1",
            location = "C1",
            locationType = "court",
            primaryReason = "Just because",
          ),
        )
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `submitting demand is allowed for POM`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
        roles = listOf("ROLE_POM"),
      )

      webTestClient.post()
        .uri("/cas2/demand")
        .header("Authorization", "Bearer $jwt")
        .bodyValue(
          Cas2Demand(
            identifier = "1",
            location = "C1",
            locationType = "court",
            primaryReason = "Just because",
          ),
        )
        .exchange()
        .expectStatus()
        .isCreated
    }
  }

  @Nested
  inner class SubmitDemand {
    @Test
    fun `submitting a demand works and shows what was saved`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
        roles = listOf("ROLE_POM"),
      )

      val returnedDemand = webTestClient.post()
        .uri("/cas2/demand")
        .header("Authorization", "Bearer $jwt")
        .bodyValue(
          Cas2Demand(
            identifier = "1",
            location = "C1",
            locationType = "court",
            primaryReason = "Just because",
          ),
        )
        .exchange()
        .expectStatus()
        .isCreated
        .returnResult(Cas2Demand::class.java)
        .responseBody
        .blockFirst()

      Assertions.assertThat(returnedDemand).matches {
        it.identifier == "1"
      }

      val retrievedDemand = webTestClient.get()
        .uri("/cas2/demand/${returnedDemand?.id}")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isOk
        .returnResult(Cas2Demand::class.java)
        .responseBody
        .blockFirst()

      Assertions.assertThat(retrievedDemand).matches {
        it.identifier == "1"
      }

    }
  }

  @Nested
  inner class GetDemand {
    @Test
    fun `getting a non-existent demand gives 404`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
        roles = listOf("ROLE_POM"),
      )

      val nullUUID = "00000000-0000-0000-0000-000000000000"

      val returnedDemand = webTestClient.get()
        .uri("/cas2/demand/${nullUUID}")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }
}
