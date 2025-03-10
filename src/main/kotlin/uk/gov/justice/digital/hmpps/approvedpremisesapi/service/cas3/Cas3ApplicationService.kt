package uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas3

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApplicationEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApplicationRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.CasResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserAccessService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserService
import java.time.OffsetDateTime
import java.util.UUID

@Service
class Cas3ApplicationService(
  private val applicationRepository: ApplicationRepository,
  private val userService: UserService,
  private val userAccessService: UserAccessService,
) {
  @Transactional
  fun markApplicationAsDeleted(applicationId: UUID): CasResult<Unit> {
    val user = userService.getUserForRequest()
    val application = applicationRepository.findByIdOrNull(applicationId)
      ?: return CasResult.NotFound("TemporaryAccommodationApplication", applicationId.toString())

    if (!isUserAuthorizedToAccessApplication(user, application)) {
      return CasResult.Unauthorised()
    }

    return if (application.submittedAt == null) {
      markAsDeleted(application)
    } else {
      CasResult.GeneralValidationError("Cannot mark as deleted: temporary accommodation application already submitted.")
    }
  }

  private fun isUserAuthorizedToAccessApplication(user: UserEntity, application: ApplicationEntity): Boolean {
    return userAccessService.userCanAccessTemporaryAccommodationApplication(user, application)
  }

  private fun markAsDeleted(application: ApplicationEntity): CasResult<Unit> {
    application.deletedAt = OffsetDateTime.now()
    applicationRepository.saveAndFlush(application)
    return CasResult.Success(Unit)
  }
}
