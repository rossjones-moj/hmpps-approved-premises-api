package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2bail

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.NewCas2ApplicationNote
import uk.gov.justice.digital.hmpps.approvedpremisesapi.config.NotifyConfig
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.Cas2User
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.NomisUserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailApplicationNoteEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailApplicationNoteRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailApplicationRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailAssessmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.cas2bail.Cas2BailAssessmentRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.EmailNotificationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.ExternalUserService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.HttpAuthService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.NomisUserService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2.UserAccessService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.toCas2UiFormat
import uk.gov.justice.digital.hmpps.approvedpremisesapi.util.toCas2UiFormattedHourOfDay
import java.time.OffsetDateTime
import java.util.*

@Service()
class Cas2BailApplicationNoteService(
  private val cas2BailApplicationRepository: Cas2BailApplicationRepository,
  private val cas2BailAssessmentRepository: Cas2BailAssessmentRepository,
  private val cas2BailApplicationNoteRepository: Cas2BailApplicationNoteRepository,
  private val userService: NomisUserService,
  private val externalUserService: ExternalUserService,
  private val httpAuthService: HttpAuthService,
  private val emailNotificationService: EmailNotificationService,
  private val userAccessService: UserAccessService,
  private val notifyConfig: NotifyConfig,
  @Value("\${url-templates.frontend.cas2.application-overview}") private val applicationUrlTemplate: String,
  @Value("\${url-templates.frontend.cas2.submitted-application-overview}") private val assessmentUrlTemplate: String,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Suppress("ReturnCount")
  fun createAssessmentNote(assessmentId: UUID, note: NewCas2ApplicationNote): CasResult<Cas2BailApplicationNoteEntity> {
    val assessment = cas2BailAssessmentRepository.findByIdOrNull(assessmentId)
      ?: return CasResult.NotFound()

    val application = cas2BailApplicationRepository.findByIdOrNull(assessment.application.id)
      ?: return CasResult.NotFound()

    if (application.submittedAt == null) {
      return CasResult.GeneralValidationError("This application has not been submitted")
    }

    val isExternalUser = httpAuthService.getCas2AuthenticatedPrincipalOrThrow().isExternalUser()
    val user = getCas2User(isExternalUser)

    if (!isExternalUser && !nomisUserCanAddNote(application, user as NomisUserEntity)) {
      return CasResult.Unauthorised()
    }

    val savedNote = saveNote(application, assessment, note.note, user)

    sendEmail(isExternalUser, application, savedNote)

    return CasResult.Success(savedNote)
  }

  private fun sendEmail(
    isExternalUser: Boolean,
    application: Cas2BailApplicationEntity,
    savedNote: Cas2BailApplicationNoteEntity,
  ) {
    if (isExternalUser) {
      sendEmailToReferrer(application, savedNote)
    } else {
      sendEmailToAssessors(application, savedNote)
    }
  }

  private fun sendEmailToReferrer(
    application: Cas2BailApplicationEntity,
    savedNote: Cas2BailApplicationNoteEntity,
  ) {
    if (application.createdByUser.email != null) {
      emailNotificationService.sendCas2Email(
        recipientEmailAddress = application.createdByUser.email!!,
        templateId = notifyConfig.templates.cas2NoteAddedForReferrer,
        personalisation = mapOf(
          "dateNoteAdded" to savedNote.createdAt.toLocalDate().toCas2UiFormat(),
          "timeNoteAdded" to savedNote.createdAt.toCas2UiFormattedHourOfDay(),
          "nomsNumber" to application.nomsNumber,
          "applicationType" to "Home Detention Curfew (HDC)",
          "applicationUrl" to applicationUrlTemplate.replace("#id", application.id.toString()),
        ),
      )
    } else {
      log.error("Email not found for User ${application.createdByUser.id}. Unable to send email for Note ${savedNote.id} on Application ${application.id}")
      Sentry.captureMessage("Email not found for User ${application.createdByUser.id}. Unable to send email for Note ${savedNote.id} on Application ${application.id}")
    }
  }

  private fun sendEmailToAssessors(
    application: Cas2BailApplicationEntity,
    savedNote: Cas2BailApplicationNoteEntity,
  ) {
    emailNotificationService.sendCas2Email(
      recipientEmailAddress = notifyConfig.emailAddresses.cas2Assessors,
      templateId = notifyConfig.templates.cas2NoteAddedForAssessor,
      personalisation = mapOf(
        "nacroReferenceId" to getNacroReferenceIdOrPlaceholder(application.assessment!!),
        "nacroReferenceIdInSubject" to getSubjectLineReferenceIdOrPlaceholder(application.assessment!!),
        "dateNoteAdded" to savedNote.createdAt.toLocalDate().toCas2UiFormat(),
        "timeNoteAdded" to savedNote.createdAt.toCas2UiFormattedHourOfDay(),
        "assessorName" to getAssessorNameOrPlaceholder(application.assessment!!),
        "applicationType" to "Home Detention Curfew (HDC)",
        "applicationUrl" to assessmentUrlTemplate.replace("#applicationId", application.id.toString()),
      ),
    )
  }

  private fun getSubjectLineReferenceIdOrPlaceholder(assessment: Cas2BailAssessmentEntity): String {
    if (assessment.nacroReferralId != null) {
      return "(${assessment.nacroReferralId!!})"
    }
    return ""
  }

  private fun getNacroReferenceIdOrPlaceholder(assessment: Cas2BailAssessmentEntity): String {
    if (assessment.nacroReferralId != null) {
      return assessment.nacroReferralId!!
    }
    return "Unknown. " +
      "The Nacro CAS-2 reference number has not been added to the application yet."
  }

  private fun getAssessorNameOrPlaceholder(assessment: Cas2BailAssessmentEntity): String {
    if (assessment.assessorName != null) {
      return assessment.assessorName!!
    }
    return "Unknown. " +
      "The assessor has not added their name to the application yet."
  }

  private fun getCas2User(isExternalUser: Boolean): Cas2User {
    return if (isExternalUser) {
      externalUserService.getUserForRequest()
    } else {
      userService.getUserForRequest()
    }
  }

  private fun nomisUserCanAddNote(application: Cas2BailApplicationEntity, user: NomisUserEntity): Boolean {
    return if (user.id == application.createdByUser.id) {
      true
    } else {
      userAccessService.offenderIsFromSamePrisonAsUser(application.referringPrisonCode, user.activeCaseloadId)
    }
  }

  private fun saveNote(application: Cas2BailApplicationEntity, assessment: Cas2BailAssessmentEntity, body: String, user: Cas2User): Cas2BailApplicationNoteEntity {
    val newNote = Cas2BailApplicationNoteEntity(
      id = UUID.randomUUID(),
      application = application,
      body = body,
      createdAt = OffsetDateTime.now(),
      createdByUser = user,
      assessment = assessment,
    )

    return cas2BailApplicationNoteRepository.save(newNote)
  }
}
