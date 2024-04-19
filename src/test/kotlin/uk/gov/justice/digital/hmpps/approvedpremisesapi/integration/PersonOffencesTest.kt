package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.CaseDetailFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ConvictionFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.DeliusContextOffenceFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.OffenceFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given a User`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given an Offender`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.httpmocks.APDeliusContext_mockSuccessfulCaseDetailCall
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.httpmocks.ApDeliusContext_mockNotFoundCaseDetailCall
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.httpmocks.CommunityAPI_mockNotFoundOffenderDetailsCall
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.httpmocks.CommunityAPI_mockSuccessfulConvictionsCall
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.ConvictionTransformer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.OffenceTransformer

class PersonOffencesTest : InitialiseDatabasePerClassTestBase() {
  @Autowired
  lateinit var convictionTransformer: ConvictionTransformer

  @Autowired
  lateinit var offenceTransformer: OffenceTransformer

  @Nested
  inner class GetOffence {

    @Test
    fun `Getting offences by CRN without a JWT returns 401`() {
      webTestClient.get()
        .uri("/people/CRN/offences")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Getting offences for a CRN with a non-Delius JWT returns 403`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
      )

      webTestClient.get()
        .uri("/people/CRN/offences")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `Getting offences for a CRN without ROLE_PROBATION returns 403`() {
      val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
        subject = "username",
        authSource = "delius",
      )

      webTestClient.get()
        .uri("/people/CRN/offences")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `Getting offences for a CRN that does not exist returns 404`() {
      `Given a User` { userEntity, jwt ->
        val crn = "CRN123"

        CommunityAPI_mockNotFoundOffenderDetailsCall(crn)
        loadPreemptiveCacheForOffenderDetails(crn)

        webTestClient.get()
          .uri("/people/$crn/offences")
          .header("Authorization", "Bearer $jwt")
          .exchange()
          .expectStatus()
          .isNotFound
      }
    }

    @Test
    fun `Getting offences for a CRN returns OK with correct body`() {
      `Given a User` { userEntity, jwt ->
        `Given an Offender` { offenderDetails, inmateDetails ->

          val activeConviction = ConvictionFactory()
            .withConvictionId(12345)
            .withIndex("5")
            .withActive(true)
            .withOffences(
              listOf(
                OffenceFactory()
                  .withOffenceId("1")
                  .withMainCategoryDescription("Main Category 1")
                  .withSubCategoryDescription("Sub Category 1")
                  .produce(),
                OffenceFactory()
                  .withOffenceId("2")
                  .withMainCategoryDescription("Main Category 2")
                  .withSubCategoryDescription("Sub Category 2")
                  .produce(),
              ),
            )
            .produce()

          val inactiveConviction = ConvictionFactory()
            .withConvictionId(6789)
            .withIndex("2")
            .withActive(false)
            .withOffences(
              listOf(
                OffenceFactory()
                  .withOffenceId("3")
                  .withMainCategoryDescription("Main Category 1")
                  .withSubCategoryDescription("Sub Category 1")
                  .produce(),
                OffenceFactory()
                  .withOffenceId("4")
                  .withMainCategoryDescription("Main Category 2")
                  .withSubCategoryDescription("Sub Category 2")
                  .produce(),
              ),
            )
            .produce()

          CommunityAPI_mockSuccessfulConvictionsCall(
            offenderDetails.otherIds.crn,
            listOf(activeConviction, inactiveConviction),
          )

          webTestClient.get()
            .uri("/people/${offenderDetails.otherIds.crn}/offences")
            .header("Authorization", "Bearer $jwt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
              objectMapper.writeValueAsString(convictionTransformer.transformToApi(activeConviction)),
            )
        }
      }
    }
  }

  @Nested
  inner class GetOffenceV2 {

    @Test
    fun `Getting offences by CRN without a JWT returns 401`() {
      webTestClient.get()
        .uri("/people/CRN/offences/v2")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `Getting offences for a CRN with a non-Delius JWT returns 403`() {
      val jwt = jwtAuthHelper.createClientCredentialsJwt(
        username = "username",
        authSource = "nomis",
      )

      webTestClient.get()
        .uri("/people/CRN/offences/v2")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `Getting offences for a CRN without ROLE_PROBATION returns 403`() {
      val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
        subject = "username",
        authSource = "delius",
      )

      webTestClient.get()
        .uri("/people/CRN/offences/v2")
        .header("Authorization", "Bearer $jwt")
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `Getting offences for a CRN that does not exist returns 404`() {
      `Given a User` { userEntity, jwt ->
        val crn = "CRN123"

        ApDeliusContext_mockNotFoundCaseDetailCall(crn)
        loadPreemptiveCacheForOffenderDetails(crn)

        webTestClient.get()
          .uri("/people/$crn/offences/v2")
          .header("Authorization", "Bearer $jwt")
          .exchange()
          .expectStatus()
          .isNotFound
      }
    }

    @Test
    fun `Getting offences for a CRN returns OK with correct body`() {
      `Given a User` { _, jwt ->
        `Given an Offender` { offenderDetails, _ ->

          val crn = offenderDetails.otherIds.crn

          val offence1 = DeliusContextOffenceFactory().produce()
          val offence2 = DeliusContextOffenceFactory().produce()

          APDeliusContext_mockSuccessfulCaseDetailCall(
            crn = crn,
            response = CaseDetailFactory()
              .withOffences(listOf(offence1, offence2))
              .produce(),
          )

          webTestClient.get()
            .uri("/people/$crn/offences/v2")
            .header("Authorization", "Bearer $jwt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
              objectMapper.writeValueAsString(
                listOf(
                  offenceTransformer.transformToApi(offence1),
                  offenceTransformer.transformToApi(offence2),
                ),
              ),
            )
        }
      }
    }
  }
}
