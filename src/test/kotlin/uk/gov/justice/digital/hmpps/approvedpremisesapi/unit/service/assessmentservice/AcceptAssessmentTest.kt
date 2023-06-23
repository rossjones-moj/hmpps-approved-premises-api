package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.service.assessmentservice

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.ApplicationAssessedAssessedBy
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.Cru
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.PersonReference
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.ProbationArea
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.events.model.StaffMember
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Gender
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementDates
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementRequirements
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.approvedpremisesapi.config.NotifyConfig
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApAreaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesApplicationEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ApprovedPremisesApplicationJsonSchemaEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.AssessmentEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.OffenderDetailsSummaryFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequestEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.PlacementRequirementsEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ProbationRegionEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.StaffUserDetailsFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.UserEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApprovedPremisesAssessmentJsonSchemaEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.AssessmentClarificationNoteRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.AssessmentDecision
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.AssessmentRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.community.OffenderDetailSummary
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.community.StaffUserDetails
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.ValidatableActionResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.AssessmentService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.CruService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.DomainEventService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.EmailNotificationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.JsonSchemaService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.OffenderService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequestService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.PlacementRequirementsService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserService
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class AcceptAssessmentTest {
  private val userServiceMock = mockk<UserService>()
  private val assessmentRepositoryMock = mockk<AssessmentRepository>()
  private val assessmentClarificationNoteRepositoryMock = mockk<AssessmentClarificationNoteRepository>()
  private val jsonSchemaServiceMock = mockk<JsonSchemaService>()
  private val domainEventServiceMock = mockk<DomainEventService>()
  private val offenderServiceMock = mockk<OffenderService>()
  private val cruServiceMock = mockk<CruService>()
  private val communityApiClientMock = mockk<CommunityApiClient>()
  private val placementRequestServiceMock = mockk<PlacementRequestService>()
  private val emailNotificationServiceMock = mockk<EmailNotificationService>()
  private val placementRequirementsServiceMock = mockk<PlacementRequirementsService>()

  private val assessmentService = AssessmentService(
    userServiceMock,
    assessmentRepositoryMock,
    assessmentClarificationNoteRepositoryMock,
    jsonSchemaServiceMock,
    domainEventServiceMock,
    offenderServiceMock,
    communityApiClientMock,
    cruServiceMock,
    placementRequestServiceMock,
    emailNotificationServiceMock,
    NotifyConfig(),
    placementRequirementsServiceMock,
    "http://frontend/applications/#id",
    "http://frontend/assessments/#id",
  )

  lateinit var user: UserEntity
  lateinit var assessmentId: UUID

  private lateinit var assessmentFactory: AssessmentEntityFactory
  private lateinit var assessmentSchema: ApprovedPremisesAssessmentJsonSchemaEntity
  private lateinit var placementRequirements: PlacementRequirements

  @BeforeEach
  fun setup() {
    user = UserEntityFactory().withYieldedProbationRegion {
      ProbationRegionEntityFactory().withYieldedApArea { ApAreaEntityFactory().produce() }.produce()
    }.produce()

    assessmentId = UUID.randomUUID()

    assessmentSchema = ApprovedPremisesAssessmentJsonSchemaEntity(
      id = UUID.randomUUID(),
      addedAt = OffsetDateTime.now(),
      schema = "{}",
    )

    assessmentFactory = AssessmentEntityFactory()
      .withId(assessmentId)
      .withApplication(
        ApprovedPremisesApplicationEntityFactory()
          .withCreatedByUser(
            UserEntityFactory()
              .withYieldedProbationRegion {
                ProbationRegionEntityFactory()
                  .withYieldedApArea { ApAreaEntityFactory().produce() }
                  .produce()
              }
              .produce(),
          )
          .produce(),
      )
      .withAllocatedToUser(user)
      .withAssessmentSchema(assessmentSchema)
      .withData("{\"test\": \"data\"}")

    placementRequirements = PlacementRequirements(
      gender = Gender.male,
      type = ApType.normal,
      location = "AB123",
      radius = 50,
      desirableCriteria = listOf(),
      essentialCriteria = listOf(),
    )
  }

  @Test
  fun `acceptAssessment returns unauthorised for Assessment not allocated to user`() {
    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessmentFactory
      .withAllocatedToUser(
        UserEntityFactory()
          .withYieldedProbationRegion {
            ProbationRegionEntityFactory()
              .withYieldedApArea { ApAreaEntityFactory().produce() }
              .produce()
          }
          .produce(),
      )
      .produce()

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns ApprovedPremisesApplicationJsonSchemaEntityFactory().produce()

    val result = assessmentService.acceptAssessment(user, assessmentId, "{}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Unauthorised).isTrue
  }

  @Test
  fun `acceptAssessment returns general validation error for Assessment where schema is outdated`() {
    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessmentFactory.produce()

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns ApprovedPremisesApplicationJsonSchemaEntityFactory().produce()

    val result = assessmentService.acceptAssessment(user, assessmentId, "{}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.GeneralValidationError)

    val generalValidationError = validationResult as ValidatableActionResult.GeneralValidationError
    assertThat(generalValidationError.message).isEqualTo("The schema version is outdated")
  }

  @Test
  fun `acceptAssessment returns general validation error for Assessment where decision has already been taken`() {
    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessmentFactory
      .withDecision(AssessmentDecision.ACCEPTED)
      .withSubmittedAt(OffsetDateTime.now())
      .produce()

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    val result = assessmentService.acceptAssessment(user, assessmentId, "{}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.GeneralValidationError)

    val generalValidationError = validationResult as ValidatableActionResult.GeneralValidationError
    assertThat(generalValidationError.message).isEqualTo("A decision has already been taken on this assessment")
  }

  @Test
  fun `acceptAssessment returns general validation error for Assessment where assessment has been deallocated`() {
    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessmentFactory
      .withReallocatedAt(OffsetDateTime.now())
      .produce()

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    val result = assessmentService.acceptAssessment(user, assessmentId, "{}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.GeneralValidationError)

    val generalValidationError = validationResult as ValidatableActionResult.GeneralValidationError
    assertThat(generalValidationError.message).isEqualTo("The application has been reallocated, this assessment is read only")
  }

  @Test
  fun `acceptAssessment returns field validation error when JSON schema not satisfied by data`() {
    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessmentFactory
      .withData("{\"test\": \"data\"}")
      .produce()

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    every { jsonSchemaServiceMock.validate(assessmentSchema, "{\"test\": \"data\"}") } returns false

    every { assessmentRepositoryMock.save(any()) } answers { it.invocation.args[0] as AssessmentEntity }

    val result = assessmentService.acceptAssessment(user, assessmentId, "{\"test\": \"data\"}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.FieldValidationError)

    val fieldValidationError = (validationResult as ValidatableActionResult.FieldValidationError)
    assertThat(fieldValidationError.validationMessages).contains(
      Assertions.entry("$.data", "invalid"),
    )
  }

  @Test
  fun `acceptAssessment returns updated assessment, emits domain event, sends email, does not create placement request when no date information provided`() {
    val assessment = assessmentFactory.produce()

    val placementRequirementEntity = PlacementRequirementsEntityFactory()
      .withApplication(assessment.application as ApprovedPremisesApplicationEntity)
      .withAssessment(assessment)
      .produce()

    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessment

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    every { jsonSchemaServiceMock.validate(assessmentSchema, "{\"test\": \"data\"}") } returns true

    every { assessmentRepositoryMock.save(any()) } answers { it.invocation.args[0] as AssessmentEntity }

    val offenderDetails = OffenderDetailsSummaryFactory().produce()

    every { offenderServiceMock.getOffenderByCrn(assessment.application.crn, user.deliusUsername) } returns AuthorisableActionResult.Success(offenderDetails)

    val staffUserDetails = StaffUserDetailsFactory()
      .withProbationAreaCode("N26")
      .produce()

    every { cruServiceMock.cruNameFromProbationAreaCode("N26") } returns "South West & South Central"

    every { communityApiClientMock.getStaffUserDetails(user.deliusUsername) } returns ClientResult.Success(HttpStatus.OK, staffUserDetails)

    every { domainEventServiceMock.saveApplicationAssessedDomainEvent(any()) } just Runs

    every { placementRequirementsServiceMock.createPlacementRequirements(assessment, placementRequirements) } returns ValidatableActionResult.Success(placementRequirementEntity)

    every { emailNotificationServiceMock.sendEmail(any(), any(), any()) } just Runs

    val result = assessmentService.acceptAssessment(user, assessmentId, "{\"test\": \"data\"}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.Success)

    val updatedAssessment = (validationResult as ValidatableActionResult.Success).entity
    assertThat(updatedAssessment.decision).isEqualTo(AssessmentDecision.ACCEPTED)
    assertThat(updatedAssessment.submittedAt).isNotNull()
    assertThat(updatedAssessment.document).isEqualTo("{\"test\": \"data\"}")

    verifyDomainEventSent(offenderDetails, staffUserDetails, assessment)

    verify(exactly = 0) {
      placementRequestServiceMock.createPlacementRequest(any(), any(), any())
    }

    verify(exactly = 1) {
      emailNotificationServiceMock.sendEmail(
        any(),
        "ddf87b15-8866-4bad-a87b-47eba69eb6db",
        match {
          it["name"] == assessment.application.createdByUser.name &&
            (it["applicationUrl"] as String).matches(Regex("http://frontend/applications/[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}"))
        },
      )
    }
  }

  @Test
  fun `acceptAssessment returns updated assessment, emits domain event, sends email, creates placement request when requirements provided`() {
    val assessment = assessmentFactory.produce()

    val placementRequirementEntity = PlacementRequirementsEntityFactory()
      .withApplication(assessment.application as ApprovedPremisesApplicationEntity)
      .withAssessment(assessment)
      .produce()

    val placementDates = PlacementDates(
      expectedArrival = LocalDate.now(),
      duration = 12,
    )

    val notes = "Some Notes"

    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessment

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    every { jsonSchemaServiceMock.validate(assessmentSchema, "{\"test\": \"data\"}") } returns true

    every { assessmentRepositoryMock.save(any()) } answers { it.invocation.args[0] as AssessmentEntity }

    every { placementRequirementsServiceMock.createPlacementRequirements(assessment, placementRequirements) } returns ValidatableActionResult.Success(placementRequirementEntity)

    every { placementRequestServiceMock.createPlacementRequest(placementRequirementEntity, placementDates, notes) } returns PlacementRequestEntityFactory()
      .withPlacementRequirements(
        PlacementRequirementsEntityFactory()
          .withApplication(assessment.application as ApprovedPremisesApplicationEntity)
          .withAssessment(assessment)
          .produce(),
      )
      .withApplication(assessment.application as ApprovedPremisesApplicationEntity)
      .withAssessment(assessment)
      .withAllocatedToUser(user)
      .produce()

    val offenderDetails = OffenderDetailsSummaryFactory().produce()

    every { offenderServiceMock.getOffenderByCrn(assessment.application.crn, user.deliusUsername) } returns AuthorisableActionResult.Success(offenderDetails)

    val staffUserDetails = StaffUserDetailsFactory()
      .withProbationAreaCode("N26")
      .produce()

    every { cruServiceMock.cruNameFromProbationAreaCode("N26") } returns "South West & South Central"

    every { communityApiClientMock.getStaffUserDetails(user.deliusUsername) } returns ClientResult.Success(HttpStatus.OK, staffUserDetails)

    every { domainEventServiceMock.saveApplicationAssessedDomainEvent(any()) } just Runs

    every { emailNotificationServiceMock.sendEmail(any(), any(), any()) } just Runs

    val result = assessmentService.acceptAssessment(user, assessmentId, "{\"test\": \"data\"}", placementRequirements, placementDates, notes)

    assertThat(result is AuthorisableActionResult.Success).isTrue

    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.Success)

    val updatedAssessment = (validationResult as ValidatableActionResult.Success).entity
    assertThat(updatedAssessment.decision).isEqualTo(AssessmentDecision.ACCEPTED)
    assertThat(updatedAssessment.submittedAt).isNotNull()
    assertThat(updatedAssessment.document).isEqualTo("{\"test\": \"data\"}")

    verifyDomainEventSent(offenderDetails, staffUserDetails, assessment)

    verify(exactly = 1) {
      placementRequestServiceMock.createPlacementRequest(placementRequirementEntity, placementDates, notes)
    }

    verify(exactly = 1) {
      emailNotificationServiceMock.sendEmail(
        any(),
        "ddf87b15-8866-4bad-a87b-47eba69eb6db",
        match {
          it["name"] == assessment.application.createdByUser.name &&
            (it["applicationUrl"] as String).matches(Regex("http://frontend/applications/[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}"))
        },
      )
    }
  }

  @Test
  fun `acceptAssessment does not emit Domain Event when failing to create Placement Requirements`() {
    val assessment = assessmentFactory.produce()

    every { assessmentRepositoryMock.findByIdOrNull(assessmentId) } returns assessment

    every { jsonSchemaServiceMock.getNewestSchema(ApprovedPremisesAssessmentJsonSchemaEntity::class.java) } returns assessmentSchema

    every { jsonSchemaServiceMock.validate(assessmentSchema, "{\"test\": \"data\"}") } returns true

    every { assessmentRepositoryMock.save(any()) } answers { it.invocation.args[0] as AssessmentEntity }

    every { placementRequirementsServiceMock.createPlacementRequirements(assessment, placementRequirements) } returns ValidatableActionResult.GeneralValidationError("Couldn't create Placement Requirements")

    val result = assessmentService.acceptAssessment(user, assessmentId, "{\"test\": \"data\"}", placementRequirements, null, null)

    assertThat(result is AuthorisableActionResult.Success).isTrue
    val validationResult = (result as AuthorisableActionResult.Success).entity
    assertThat(validationResult is ValidatableActionResult.GeneralValidationError).isTrue

    verify(exactly = 0) {
      domainEventServiceMock.saveApplicationAssessedDomainEvent(any())
    }

    verify(exactly = 1) {
      placementRequirementsServiceMock.createPlacementRequirements(assessment, placementRequirements)
    }
  }

  private fun verifyDomainEventSent(offenderDetails: OffenderDetailSummary, staffUserDetails: StaffUserDetails, assessment: AssessmentEntity) {
    verify(exactly = 1) {
      domainEventServiceMock.saveApplicationAssessedDomainEvent(
        match {
          val data = it.data.eventDetails
          val expectedPersonReference = PersonReference(
            crn = offenderDetails.otherIds.crn,
            noms = offenderDetails.otherIds.nomsNumber!!,
          )
          val expectedAssessor = ApplicationAssessedAssessedBy(
            staffMember = StaffMember(
              staffCode = staffUserDetails.staffCode,
              staffIdentifier = staffUserDetails.staffIdentifier,
              forenames = staffUserDetails.staff.forenames,
              surname = staffUserDetails.staff.surname,
              username = staffUserDetails.username,
            ),
            probationArea = ProbationArea(
              code = staffUserDetails.probationArea.code,
              name = staffUserDetails.probationArea.description,
            ),
            cru = Cru(
              name = "South West & South Central",
            ),
          )

          it.applicationId == assessment.application.id &&
            it.crn == assessment.application.crn &&
            data.applicationId == assessment.application.id &&
            data.applicationUrl == "http://frontend/applications/${assessment.application.id}" &&
            data.personReference == expectedPersonReference &&
            data.deliusEventNumber == (assessment.application as ApprovedPremisesApplicationEntity).eventNumber &&
            data.assessedBy == expectedAssessor &&
            data.decision == "ACCEPTED" &&
            data.decisionRationale == null
        },
      )
    }
  }
}
