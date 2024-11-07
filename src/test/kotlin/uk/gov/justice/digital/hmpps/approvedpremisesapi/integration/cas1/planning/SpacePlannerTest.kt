package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.cas1.planning

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.Cas1SpaceBookingEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.InitialiseDatabasePerClassTestBase
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given a Placement Request`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.integration.givens.`Given a User`
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas1SpaceBookingEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.planning.SpacePlanRenderer
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas1.planning.SpacePlanner
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.roundNanosToMillisToAccountForLossOfPrecisionInPostgres
import java.time.LocalDate
import java.time.OffsetDateTime

class SpacePlannerTest : InitialiseDatabasePerClassTestBase() {

  @Autowired
  lateinit var spacePlanner: SpacePlanner

  private fun findCharacteristic(propertyName: String) =
    characteristicRepository.findByPropertyName(propertyName)!!

  @Test
  fun `multi day planning is correct`() {
    val premises = approvedPremisesEntityFactory
      .produceAndPersist {
        withName("Premises 1")
        withProbationRegion(probationRegionEntityFactory.produceAndPersist())
        withLocalAuthorityArea(localAuthorityEntityFactory.produceAndPersist())
      }

    val room1 = roomEntityFactory.produceAndPersist {
      withYieldedPremises { premises }
      withCharacteristics(
        findCharacteristic("isArsonSuitable"),
        findCharacteristic("hasEnSuite"),
      )
    }.apply { premises.rooms.add(this) }
    room1.beds.add(
      bedEntityFactory.produceAndPersist {
        withName("Room 1 - Bed 1")
        withYieldedRoom { room1 }
      },
    )

    val room2 = roomEntityFactory.produceAndPersist { withYieldedPremises { premises } }.apply { premises.rooms.add(this) }
    room2.beds.add(
      bedEntityFactory.produceAndPersist {
        withName("Room 2 - Bed 1")
        withYieldedRoom { room2 }
      },
    )

    val room3 = roomEntityFactory.produceAndPersist { withYieldedPremises { premises } }.apply { premises.rooms.add(this) }
    room3.beds.add(
      bedEntityFactory.produceAndPersist {
        withName("Room 3 - Bed 1")
        withYieldedRoom { room3 }
      },
    )
    room3.beds.add(
      bedEntityFactory.produceAndPersist {
        withName("Room 3 - Bed 2")
        withYieldedRoom { room3 }
      },
    )

    val room3Bed3 = bedEntityFactory.produceAndPersist {
      withName("Room 3 - Bed 3")
      withYieldedRoom { room3 }
    }

    room3.beds.add(room3Bed3)

    cas1OutOfServiceBedEntityFactory.produceAndPersist {
      withCreatedAt(OffsetDateTime.now().roundNanosToMillisToAccountForLossOfPrecisionInPostgres())
      withBed(room3Bed3)
    }.apply {
      this.revisionHistory += cas1OutOfServiceBedRevisionEntityFactory.produceAndPersist {
        withCreatedAt(OffsetDateTime.now().roundNanosToMillisToAccountForLossOfPrecisionInPostgres())
        withCreatedBy(`Given a User`().first)
        withOutOfServiceBed(this@apply)
        withStartDate(LocalDate.of(2020, 5, 7))
        withEndDate(LocalDate.of(2020, 5, 8))
        withReason(cas1OutOfServiceBedReasonEntityFactory.produceAndPersist())
      }
    }

    room3.beds.add(
      bedEntityFactory.produceAndPersist {
        withName("Room 3 - Bed 4")
        withEndDate(LocalDate.of(2020, 5, 8))
        withYieldedRoom { room3 }
      },
    )

    createSpaceBooking(crn = "CRN1") {
      withPremises(premises)
      withCanonicalArrivalDate(LocalDate.of(2020, 5, 4))
      withCanonicalDepartureDate(LocalDate.of(2020, 5, 11))
      withCriteria(
        findCharacteristic("isArsonSuitable"),
      )
    }

    createSpaceBooking(crn = "CRN2") {
      withPremises(premises)
      withCanonicalArrivalDate(LocalDate.of(2020, 5, 6))
      withCanonicalDepartureDate(LocalDate.of(2020, 5, 9))
    }

    createSpaceBooking(crn = "CRN3") {
      withPremises(premises)
      withCanonicalArrivalDate(LocalDate.of(2020, 5, 7))
      withCanonicalDepartureDate(LocalDate.of(2020, 5, 20))
      withCriteria(
        findCharacteristic("isSingle"),
      )
    }

    createSpaceBooking(crn = "CRN4") {
      withPremises(premises)
      withCanonicalArrivalDate(LocalDate.of(2020, 5, 1))
      withCanonicalDepartureDate(LocalDate.of(2020, 5, 29))
      withCriteria(
        findCharacteristic("isSingle"),
      )
    }

    createSpaceBooking(crn = "CRN5") {
      withPremises(premises)
      withCanonicalArrivalDate(LocalDate.of(2020, 5, 1))
      withCanonicalDepartureDate(LocalDate.of(2020, 5, 9))
      withCriteria(
        findCharacteristic("hasEnSuite"),
      )
    }

    val criteria = SpacePlanner.PlanCriteria(
      premises = premises,
      startDate = LocalDate.of(2020, 5, 6),
      endDate = LocalDate.of(2020, 5, 10),
    )

    val plan = spacePlanner.plan(criteria)

    assertThat(
      SpacePlanRenderer.render(criteria, plan),
    ).isEqualTo(
      """
      Space Plan for Premises 1 from 2020-05-06 to 2020-05-10
      
      There are 3 days with unplanned bookings:
      
      2020-05-06 has 1
      2020-05-07 has 2
      2020-05-08 has 2
      
      | Room (6)             | **2020-05-06**<br/>capacity: 6<br/>planned: 3<br/>unplanned: 1         | **2020-05-07**<br/>capacity: 5<br/>planned: 3<br/>unplanned: 2         | **2020-05-08**<br/>capacity: 4<br/>planned: 3<br/>unplanned: 2         | **2020-05-09**<br/>capacity: 5<br/>planned: 3<br/>unplanned: 0         | **2020-05-10**<br/>capacity: 5<br/>planned: 3<br/>unplanned: 0         |
      | -------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------- |
      | **Room 1 - Bed 1**   | **CRN1**<br/>hasEnSuite(r)<br/>isArsonSuitable(rb)                     | **CRN1**<br/>hasEnSuite(r)<br/>isArsonSuitable(rb)                     | **CRN1**<br/>hasEnSuite(r)<br/>isArsonSuitable(rb)                     | **CRN1**<br/>hasEnSuite(r)<br/>isArsonSuitable(rb)                     | **CRN1**<br/>hasEnSuite(r)<br/>isArsonSuitable(rb)                     |
      | **Room 2 - Bed 1**   | **CRN4**<br/>isSingle(b)                                               | **CRN3**<br/>isSingle(b)                                               | **CRN3**<br/>isSingle(b)                                               | **CRN3**<br/>isSingle(b)                                               | **CRN3**<br/>isSingle(b)                                               |
      | **Room 3 - Bed 1**   | **CRN2**                                                               | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               |
      | **Room 3 - Bed 2**   |                                                                        | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               |
      | **Room 3 - Bed 3**   |                                                                        | OUT_OF_SERVICE                                                         | OUT_OF_SERVICE                                                         | **CRN4**<br/>isSingle(b)                                               | **CRN4**<br/>isSingle(b)                                               |
      | **Room 3 - Bed 4**   |                                                                        | **CRN4**<br/>isSingle(b)                                               | ENDED                                                                  | ENDED                                                                  | ENDED                                                                  |
      | **unplanned**        | **CRN5**<br/>hasEnSuite                                                | **CRN5**<br/>hasEnSuite<br/><br/>**CRN2**                              | **CRN5**<br/>hasEnSuite<br/><br/>**CRN2**                              |                                                                        |                                                                        |
      """.trimIndent(),
    )
  }

  private fun createSpaceBooking(
    crn: String,
    configuration: Cas1SpaceBookingEntityFactory.() -> Unit,
  ): Cas1SpaceBookingEntity {
    val (user) = `Given a User`()
    val (placementRequest) = `Given a Placement Request`(
      placementRequestAllocatedTo = user,
      assessmentAllocatedTo = user,
      createdByUser = user,
    )

    return cas1SpaceBookingEntityFactory.produceAndPersist {
      withCrn(crn)
      withPlacementRequest(placementRequest)
      withApplication(placementRequest.application)
      withCreatedBy(user)

      configuration.invoke(this)
    }
  }
}
