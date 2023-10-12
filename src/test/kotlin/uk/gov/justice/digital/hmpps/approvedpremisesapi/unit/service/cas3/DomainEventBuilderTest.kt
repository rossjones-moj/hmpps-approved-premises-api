package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.service.cas3

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApAreaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ArrivalEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.BookingEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.LocalAuthorityEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ProbationRegionEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.TemporaryAccommodationApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.TemporaryAccommodationPremisesEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.UserEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas3.DomainEventBuilder
import java.time.Instant
import java.time.LocalDate

class DomainEventBuilderTest {
  private val domainEventBuilder = DomainEventBuilder()

  @Test
  fun `getPersonArrivedDomainEvent transforms the booking and arrival information correctly`() {
    val arrivalDateTime = Instant.parse("2023-07-15T00:00:00Z")
    val expectedDepartureDate = LocalDate.parse("2023-10-15")
    val notes = "Some notes about the arrival"

    val probationRegion = ProbationRegionEntityFactory()
      .withApArea(
        ApAreaEntityFactory().produce(),
      )
      .produce()

    val premises = TemporaryAccommodationPremisesEntityFactory()
      .withProbationRegion(probationRegion)
      .withLocalAuthorityArea(
        LocalAuthorityEntityFactory().produce(),
      )
      .produce()

    val user = UserEntityFactory()
      .withProbationRegion(probationRegion)
      .produce()

    val application = TemporaryAccommodationApplicationEntityFactory()
      .withCreatedByUser(user)
      .withProbationRegion(probationRegion)
      .produce()

    val booking = BookingEntityFactory()
      .withPremises(premises)
      .withApplication(application)
      .produce()

    booking.arrival = ArrivalEntityFactory()
      .withBooking(booking)
      .withArrivalDateTime(arrivalDateTime)
      .withExpectedDepartureDate(expectedDepartureDate)
      .withNotes(notes)
      .produce()

    val event = domainEventBuilder.getPersonArrivedDomainEvent(booking)

    assertThat(event).matches {
      val data = it.data.eventDetails

      it.applicationId == application.id &&
        it.crn == booking.crn &&
        data.personReference.crn == booking.crn &&
        data.personReference.noms == booking.nomsNumber &&
        data.deliusEventNumber == application.eventNumber &&
        data.bookingId == booking.id &&
        data.premises.addressLine1 == premises.addressLine1 &&
        data.premises.addressLine2 == premises.addressLine2 &&
        data.premises.postcode == premises.postcode &&
        data.premises.town == premises.town &&
        data.premises.region == premises.probationRegion.name &&
        data.arrivedAt == arrivalDateTime &&
        data.expectedDepartureOn == expectedDepartureDate &&
        data.notes == notes &&
        data.applicationId == application.id
    }
  }
}
