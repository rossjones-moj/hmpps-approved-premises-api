package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas3

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3BookingProvisionallyMadeEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3BookingProvisionallyMadeEventDetails
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3PersonArrivedEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3PersonArrivedEventDetails
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3PersonDepartedEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.CAS3PersonDepartedEventDetails
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.EventType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.MoveOnCategory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.PersonReference
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.cas3.model.Premises
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.TemporaryAccommodationApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.DomainEvent
import java.net.URI
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Component
class DomainEventBuilder(
  @Value("\${url-templates.api.cas3.application}") private val applicationUrlTemplate: String,
  @Value("\${url-templates.api.cas3.booking}") private val bookingUrlTemplate: String,
) {
  fun getBookingProvisionallyMadeDomainEvent(
    booking: BookingEntity,
  ): DomainEvent<CAS3BookingProvisionallyMadeEvent> {
    val domainEventId = UUID.randomUUID()

    val application = booking.application as? TemporaryAccommodationApplicationEntity

    return DomainEvent(
      id = domainEventId,
      applicationId = application?.id,
      bookingId = booking.id,
      crn = booking.crn,
      occurredAt = booking.createdAt.toInstant(),
      data = CAS3BookingProvisionallyMadeEvent(
        id = domainEventId,
        timestamp = Instant.now(),
        eventType = EventType.bookingProvisionallyMade,
        eventDetails = CAS3BookingProvisionallyMadeEventDetails(
          applicationId = application?.id,
          applicationUrl = application.toUrl(),
          bookingId = booking.id,
          bookingUrl = booking.toUrl(),
          personReference = PersonReference(
            crn = booking.crn,
            noms = booking.nomsNumber,
          ),
          expectedArrivedAt = booking.arrivalDate.atStartOfDay().toInstant(ZoneOffset.UTC),
          notes = "",
        ),
      ),
    )
  }

  fun getPersonArrivedDomainEvent(
    booking: BookingEntity,
  ): DomainEvent<CAS3PersonArrivedEvent> {
    val domainEventId = UUID.randomUUID()

    val arrival = booking.arrival!!
    val application = booking.application as? TemporaryAccommodationApplicationEntity

    return DomainEvent(
      id = domainEventId,
      applicationId = application?.id,
      bookingId = booking.id,
      crn = booking.crn,
      occurredAt = arrival.arrivalDateTime,
      data = CAS3PersonArrivedEvent(
        id = domainEventId,
        timestamp = Instant.now(),
        eventType = EventType.personArrived,
        eventDetails = CAS3PersonArrivedEventDetails(
          applicationId = application?.id,
          applicationUrl = application.toUrl(),
          bookingId = booking.id,
          bookingUrl = booking.toUrl(),
          personReference = PersonReference(
            crn = booking.crn,
            noms = booking.nomsNumber,
          ),
          deliusEventNumber = application?.eventNumber ?: "",
          premises = Premises(
            addressLine1 = booking.premises.addressLine1,
            addressLine2 = booking.premises.addressLine2,
            postcode = booking.premises.postcode,
            town = booking.premises.town,
            region = booking.premises.probationRegion.name,
          ),
          arrivedAt = arrival.arrivalDateTime,
          expectedDepartureOn = arrival.expectedDepartureDate,
          notes = arrival.notes ?: "",
        ),
      ),
    )
  }

  fun getPersonDepartedDomainEvent(
    booking: BookingEntity,
  ): DomainEvent<CAS3PersonDepartedEvent> {
    val domainEventId = UUID.randomUUID()

    val departure = booking.departure!!
    val application = booking.application as? TemporaryAccommodationApplicationEntity

    return DomainEvent(
      id = domainEventId,
      applicationId = application?.id,
      bookingId = booking.id,
      crn = booking.crn,
      occurredAt = departure.dateTime.toInstant(),
      data = CAS3PersonDepartedEvent(
        id = domainEventId,
        timestamp = Instant.now(),
        eventType = EventType.personDeparted,
        eventDetails = CAS3PersonDepartedEventDetails(
          personReference = PersonReference(
            crn = booking.crn,
            noms = booking.nomsNumber,
          ),
          deliusEventNumber = application?.eventNumber ?: "",
          bookingId = booking.id,
          bookingUrl = booking.toUrl(),
          premises = Premises(
            addressLine1 = booking.premises.addressLine1,
            addressLine2 = booking.premises.addressLine2,
            postcode = booking.premises.postcode,
            town = booking.premises.town,
            region = booking.premises.probationRegion.name,
          ),
          departedAt = departure.dateTime.toInstant(),
          reason = departure.reason.name,
          notes = departure.notes ?: "",
          moveOnCategory = MoveOnCategory(
            description = departure.moveOnCategory.name,
            label = departure.moveOnCategory.legacyDeliusCategoryCode ?: "",
          ),
          applicationId = application?.id,
          applicationUrl = application.toUrl(),
          reasonDetail = null,
        ),
      ),
    )
  }

  private fun TemporaryAccommodationApplicationEntity?.toUrl(): URI? =
    this?.let { URI(applicationUrlTemplate.replace("#applicationId", it.id.toString())) }

  private fun BookingEntity.toUrl(): URI =
    URI(bookingUrlTemplate.replace("#premisesId", this.premises.id.toString()).replace("#bookingId", this.id.toString()))
}
