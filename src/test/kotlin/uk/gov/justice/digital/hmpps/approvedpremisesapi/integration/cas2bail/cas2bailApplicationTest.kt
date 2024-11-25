package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.cas2bail

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
//import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.test.web.reactive.server.returnResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementApplication

import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TimelineEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenACas2Assessor
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.givenACas2PomUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.bodyAsListOfObjects


class cas2bailApplicationTest {


  @Nested
  inner class GetCas2BailApplicationsTest : IntegrationTestBase() {


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

    @Test
    fun `Get toby endpoint returns 400 if no X-Service-Name`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby?month=12")
          .header("Authorization", "Bearer $jwt")
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get toby endpoint returns 400 if no month parameter included`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get toby endpoint returns 400 if month parameter is not an Int`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby?month=February")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isBadRequest
      }
    }

    @Test
    fun `Get toby endpoint returns 403 if different role to Cas2Assessor`() {
      givenACas2PomUser { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby?month=12")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isForbidden
      }
    }

    @Test
    fun `Get toby endpoint returns 403 service is not cas2`() {
      givenACas2Assessor { _, jwt ->
        webTestClient.get()
          .uri("/cas2bail/toby?month=12")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.approvedPremises.value)
          .exchange()
          .expectStatus()
          .isForbidden
      }
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `Get toby endpoint returns correct text`() {
      givenACas2Assessor { _, jwt ->
        val rawResponseBody = webTestClient.get()
          .uri("/cas2bail/toby?month=12")
          .header("Authorization", "Bearer $jwt")
          .header("X-Service-Name", ServiceName.cas2.value)
          .exchange()
          .expectStatus()
          .isOk
          .bodyAsListOfObjects<String>()

        log.info("rawResponseBody: ${rawResponseBody.size}")
        log.info("rawResponseBody: ${rawResponseBody[0]}")

        val firstItem = rawResponseBody.first()
        val lastItem = rawResponseBody.last()
        assertThat(firstItem).isEqualTo("Hello Toby")
        assertThat(lastItem).isEqualTo("Hello Gareth")


      }
    }

  }


}

