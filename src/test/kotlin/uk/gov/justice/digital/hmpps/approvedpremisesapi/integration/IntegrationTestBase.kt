package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApAreaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ArrivalEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.BookingEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.CancellationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.CancellationReasonEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.DepartureEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.DepartureReasonEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.DestinationProviderEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.KeyWorkerEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.LocalAuthorityEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.MoveOnCategoryEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.NonArrivalEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PersistedFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PremisesEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ProbationRegionEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApAreaEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ArrivalEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CancellationReasonEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.DepartureEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.DepartureReasonEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.DestinationProviderEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.KeyWorkerEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.MoveOnCategoryEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.NonArrivalEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ProbationRegionEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.ApAreaTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.ArrivalTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.BookingTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.CancellationReasonTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.CancellationTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.DepartureReasonTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.DepartureTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.DestinationProviderTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.KeyWorkerTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.LocalAuthorityAreaTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.MoveOnCategoryTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.NonArrivalTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.PremisesTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.repository.ProbationRegionTestRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.JwtAuthHelper
import java.util.UUID

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  private lateinit var flyway: Flyway

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  @Autowired
  lateinit var probationRegionRepository: ProbationRegionTestRepository

  @Autowired
  lateinit var apAreaRepository: ApAreaTestRepository

  @Autowired
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaTestRepository

  @Autowired
  lateinit var premisesRepository: PremisesTestRepository

  @Autowired
  lateinit var bookingRepository: BookingTestRepository

  @Autowired
  lateinit var keyWorkerRepository: KeyWorkerTestRepository

  @Autowired
  lateinit var arrivalRepository: ArrivalTestRepository

  @Autowired
  lateinit var departureRepository: DepartureTestRepository

  @Autowired
  lateinit var destinationProviderRepository: DestinationProviderTestRepository

  @Autowired
  lateinit var nonArrivalRepository: NonArrivalTestRepository

  @Autowired
  lateinit var cancellationRepository: CancellationTestRepository

  @Autowired
  lateinit var departureReasonRepository: DepartureReasonTestRepository

  @Autowired
  lateinit var moveOnCategoryRepository: MoveOnCategoryTestRepository

  @Autowired
  lateinit var cancellationReasonRepository: CancellationReasonTestRepository

  lateinit var probationRegionEntityFactory: PersistedFactory<ProbationRegionEntity, UUID, ProbationRegionEntityFactory>
  lateinit var apAreaEntityFactory: PersistedFactory<ApAreaEntity, UUID, ApAreaEntityFactory>
  lateinit var localAuthorityEntityFactory: PersistedFactory<LocalAuthorityAreaEntity, UUID, LocalAuthorityEntityFactory>
  lateinit var premisesEntityFactory: PersistedFactory<PremisesEntity, UUID, PremisesEntityFactory>
  lateinit var bookingEntityFactory: PersistedFactory<BookingEntity, UUID, BookingEntityFactory>
  lateinit var keyWorkerEntityFactory: PersistedFactory<KeyWorkerEntity, UUID, KeyWorkerEntityFactory>
  lateinit var arrivalEntityFactory: PersistedFactory<ArrivalEntity, UUID, ArrivalEntityFactory>
  lateinit var departureEntityFactory: PersistedFactory<DepartureEntity, UUID, DepartureEntityFactory>
  lateinit var destinationProviderEntityFactory: PersistedFactory<DestinationProviderEntity, UUID, DestinationProviderEntityFactory>
  lateinit var departureReasonEntityFactory: PersistedFactory<DepartureReasonEntity, UUID, DepartureReasonEntityFactory>
  lateinit var moveOnCategoryEntityFactory: PersistedFactory<MoveOnCategoryEntity, UUID, MoveOnCategoryEntityFactory>
  lateinit var nonArrivalEntityFactory: PersistedFactory<NonArrivalEntity, UUID, NonArrivalEntityFactory>
  lateinit var cancellationEntityFactory: PersistedFactory<CancellationEntity, UUID, CancellationEntityFactory>
  lateinit var cancellationReasonEntityFactory: PersistedFactory<CancellationReasonEntity, UUID, CancellationReasonEntityFactory>

  @BeforeEach
  fun beforeEach() {
    flyway.clean()
    flyway.migrate()
  }

  @BeforeEach
  fun setupFactories() {
    probationRegionEntityFactory = PersistedFactory(ProbationRegionEntityFactory(), probationRegionRepository)
    apAreaEntityFactory = PersistedFactory(ApAreaEntityFactory(), apAreaRepository)
    localAuthorityEntityFactory = PersistedFactory(LocalAuthorityEntityFactory(), localAuthorityAreaRepository)
    premisesEntityFactory = PersistedFactory(PremisesEntityFactory(), premisesRepository)
    bookingEntityFactory = PersistedFactory(BookingEntityFactory(), bookingRepository)
    keyWorkerEntityFactory = PersistedFactory(KeyWorkerEntityFactory(), keyWorkerRepository)
    arrivalEntityFactory = PersistedFactory(ArrivalEntityFactory(), arrivalRepository)
    departureEntityFactory = PersistedFactory(DepartureEntityFactory(), departureRepository)
    destinationProviderEntityFactory = PersistedFactory(DestinationProviderEntityFactory(), destinationProviderRepository)
    departureReasonEntityFactory = PersistedFactory(DepartureReasonEntityFactory(), departureReasonRepository)
    moveOnCategoryEntityFactory = PersistedFactory(MoveOnCategoryEntityFactory(), moveOnCategoryRepository)
    nonArrivalEntityFactory = PersistedFactory(NonArrivalEntityFactory(), nonArrivalRepository)
    cancellationEntityFactory = PersistedFactory(CancellationEntityFactory(), cancellationRepository)
    cancellationReasonEntityFactory = PersistedFactory(CancellationReasonEntityFactory(), cancellationReasonRepository)
  }
}
