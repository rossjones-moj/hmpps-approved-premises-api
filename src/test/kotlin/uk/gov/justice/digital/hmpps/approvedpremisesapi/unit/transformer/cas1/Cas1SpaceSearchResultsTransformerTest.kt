package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.transformer.cas1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceCharacteristic
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceSearchParameters
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceSearchRequirements
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Gender
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas1.CandidatePremises
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas1.SpaceAvailability
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.ApprovedPremisesType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.asApiType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.cas1.Cas1SpaceSearchResultsTransformer
import java.time.LocalDate
import java.util.UUID
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas1SpaceSearchResult as ApiSpaceSearchResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.Cas1SpaceSearchResult as DomainSpaceSearchResult

class Cas1SpaceSearchResultsTransformerTest {
  private val transformer = Cas1SpaceSearchResultsTransformer()

  @Test
  fun `transformDomainToApi transforms correctly`() {
    val searchParameters = Cas1SpaceSearchParameters(
      applicationId = UUID.randomUUID(),
      startDate = LocalDate.now(),
      durationInDays = 14,
      targetPostcodeDistrict = "AB1",
      requirements = Cas1SpaceSearchRequirements(
        apTypes = ApType.entries,
        spaceCharacteristics = Cas1SpaceCharacteristic.entries,
        genders = Gender.entries,
      ),
    )

    val candidatePremises1 = CandidatePremises(
      UUID.randomUUID(),
      1.0f,
      "AP1234",
      "QCODE1",
      ApprovedPremisesType.NORMAL,
      "Some AP",
      "1 The Street",
      null,
      "Townsbury",
      "TB1 2AB",
      UUID.randomUUID(),
      "Some AP Area",
      3,
    )

    val candidatePremises2 = CandidatePremises(
      UUID.randomUUID(),
      2.0f,
      "AP2345",
      "QCODE2",
      ApprovedPremisesType.NORMAL,
      "Some Other AP",
      "2 The Street",
      null,
      "Townsbury",
      "TB1 2AB",
      UUID.randomUUID(),
      "Some AP Area",
      6,
    )

    val candidatePremises3 = CandidatePremises(
      UUID.randomUUID(),
      3.0f,
      "AP3456",
      "QCODE3",
      ApprovedPremisesType.NORMAL,
      "Some AP",
      "3 The Street",
      null,
      "Townsbury",
      "TB1 2AB",
      UUID.randomUUID(),
      "Some AP Area",
      9,
    )

    val spaceAvailability1 = SpaceAvailability(candidatePremises1.premisesId)
    val spaceAvailability2 = SpaceAvailability(candidatePremises2.premisesId)
    val spaceAvailability3 = SpaceAvailability(candidatePremises3.premisesId)

    val searchResults = listOf(
      DomainSpaceSearchResult(
        candidatePremises = candidatePremises1,
        spaceAvailability = spaceAvailability1,
      ),
      DomainSpaceSearchResult(
        candidatePremises = candidatePremises2,
        spaceAvailability = spaceAvailability2,
      ),
      DomainSpaceSearchResult(
        candidatePremises = candidatePremises3,
        spaceAvailability = spaceAvailability3,
      ),
    )

    val actual = transformer.transformDomainToApi(searchParameters, searchResults)
    assertThat(actual.searchCriteria).isNotNull
    assertThat(actual.searchCriteria).isEqualTo(searchParameters)
    assertThat(actual.resultsCount).isEqualTo(3)
    assertThatTransformedResultMatches(actual.results[0], candidatePremises1)
    assertThatTransformedResultMatches(actual.results[1], candidatePremises2)
    assertThatTransformedResultMatches(actual.results[2], candidatePremises3)
  }

  private fun assertThatTransformedResultMatches(actual: ApiSpaceSearchResult, expected: CandidatePremises) {
    assertThat(actual.premises).isNotNull
    assertThat(actual.premises!!.id).isEqualTo(expected.premisesId)
    assertThat(actual.premises!!.apCode).isEqualTo(expected.apCode)
    assertThat(actual.premises!!.deliusQCode).isEqualTo(expected.deliusQCode)
    assertThat(actual.premises!!.apType).isEqualTo(expected.apType.asApiType())
    assertThat(actual.premises!!.name).isEqualTo(expected.name)
    assertThat(actual.premises!!.addressLine1).isEqualTo(expected.addressLine1)
    assertThat(actual.premises!!.addressLine2).isEqualTo(expected.addressLine2)
    assertThat(actual.premises!!.town).isEqualTo(expected.town)
    assertThat(actual.premises!!.postcode).isEqualTo(expected.postcode)
    assertThat(actual.premises!!.apArea).isNotNull
    assertThat(actual.premises!!.apArea!!.id).isEqualTo(expected.apAreaId)
    assertThat(actual.premises!!.apArea!!.name).isEqualTo(expected.apAreaName)
    assertThat(actual.premises!!.totalSpaceCount).isEqualTo(expected.totalSpaceCount)
    assertThat(actual.premises!!.premisesCharacteristics).isEmpty()
    assertThat(actual.distanceInMiles).isEqualTo(expected.distanceInMiles.toBigDecimal())
    assertThat(actual.spacesAvailable).isEmpty()
  }
}
