package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Person
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given a User`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given an Offender`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.prisonsapi.AssignedLivingUnit
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.prisonsapi.InOutStatus
import java.time.LocalDate

class PersonSearchTest : IntegrationTestBase() {
  @Test
  fun `Searching by CRN without a JWT returns 401`() {
    webTestClient.get()
      .uri("/people/search?crn=CRN")
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `Searching for a CRN with a non-Delius JWT returns 403`() {
    val jwt = jwtAuthHelper.createClientCredentialsJwt(
      username = "username",
      authSource = "nomis"
    )

    webTestClient.get()
      .uri("/people/search?crn=CRN")
      .header("Authorization", "Bearer $jwt")
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Searching for a CRN without ROLE_PROBATION returns 403`() {
    val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
      subject = "username",
      authSource = "delius"
    )

    webTestClient.get()
      .uri("/people/search?crn=CRN")
      .header("Authorization", "Bearer $jwt")
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Searching for a CRN that does not exist returns 404`() {
    mockClientCredentialsJwtRequest(username = "username", authSource = "delius")

    wiremockServer.stubFor(
      get(WireMock.urlEqualTo("/secure/offenders/crn/CRN"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(404)
        )
    )

    val jwt = jwtAuthHelper.createAuthorizationCodeJwt(
      subject = "username",
      authSource = "delius",
      roles = listOf("ROLE_PROBATION")
    )

    webTestClient.get()
      .uri("/people/search?crn=CRN")
      .header("Authorization", "Bearer $jwt")
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Searching for a CRN returns OK with correct body`() {
    `Given a User` { userEntity, jwt ->
      `Given an Offender`(
        offenderDetailsConfigBlock = {
          withCrn("CRN")
          withDateOfBirth(LocalDate.parse("1985-05-05"))
          withNomsNumber("NOMS321")
          withFirstName("James")
          withLastName("Someone")
          withGender("Male")
          withNationality("English")
          withReligionOrBelief("Judaism")
          withGenderIdentity("Prefer to self-describe")
          withSelfDescribedGenderIdentity("This is a self described identity")
        },
        inmateDetailsConfigBlock = {
          withOffenderNo("NOMS321")
          withInOutStatus(InOutStatus.IN)
          withAssignedLivingUnit(
            AssignedLivingUnit(
              agencyId = "BRI",
              locationId = 5,
              description = "B-2F-004",
              agencyName = "HMP Bristol"
            )
          )
        }
      ) { offenderDetails, inmateDetails ->
        webTestClient.get()
          .uri("/people/search?crn=CRN")
          .header("Authorization", "Bearer $jwt")
          .exchange()
          .expectStatus()
          .isOk
          .expectBody()
          .json(
            objectMapper.writeValueAsString(
              Person(
                crn = "CRN",
                name = "James Someone",
                dateOfBirth = LocalDate.parse("1985-05-05"),
                sex = "Male",
                status = Person.Status.inCustody,
                nomsNumber = "NOMS321",
                nationality = "English",
                religionOrBelief = "Judaism",
                genderIdentity = "This is a self described identity",
                prisonName = "HMP Bristol"
              )
            )
          )
      }
    }
  }

  @Test
  fun `Searching for a CRN caches responses`() {
    `Given a User` { userEntity, jwt ->
      `Given an Offender`(
        offenderDetailsConfigBlock = {
          withCrn("CRN")
          withDateOfBirth(LocalDate.parse("1985-05-05"))
          withNomsNumber("NOMS321")
          withFirstName("James")
          withLastName("Someone")
          withGender("Male")
          withNationality("English")
          withReligionOrBelief("Judaism")
          withGenderIdentity("Prefer to self-describe")
          withSelfDescribedGenderIdentity("This is a self described identity")
        },
        inmateDetailsConfigBlock = {
          withOffenderNo("NOMS321")
          withInOutStatus(InOutStatus.IN)
          withAssignedLivingUnit(
            AssignedLivingUnit(
              agencyId = "BRI",
              locationId = 5,
              description = "B-2F-004",
              agencyName = "HMP Bristol"
            )
          )
        }
      ) { offenderDetails, inmateDetails ->
        repeat(2) {
          webTestClient.get()
            .uri("/people/search?crn=CRN")
            .header("Authorization", "Bearer $jwt")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(
              objectMapper.writeValueAsString(
                Person(
                  crn = "CRN",
                  name = "James Someone",
                  dateOfBirth = LocalDate.parse("1985-05-05"),
                  sex = "Male",
                  status = Person.Status.inCustody,
                  nomsNumber = "NOMS321",
                  nationality = "English",
                  religionOrBelief = "Judaism",
                  genderIdentity = "This is a self described identity",
                  prisonName = "HMP Bristol"
                )
              )
            )
        }

        wiremockServer.verify(
          WireMock.exactly(1),
          WireMock.getRequestedFor(WireMock.urlEqualTo("/secure/offenders/crn/CRN"))
        )

        wiremockServer.verify(
          WireMock.exactly(1),
          WireMock.getRequestedFor(WireMock.urlEqualTo("/api/offenders/NOMS321"))
        )
      }
    }
  }
}
