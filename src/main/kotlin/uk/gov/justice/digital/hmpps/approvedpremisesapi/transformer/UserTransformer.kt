package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApprovedPremisesUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApprovedPremisesUserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TemporaryAccommodationUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TemporaryAccommodationUserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserQualification
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserQualificationAssignmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRoleAssignmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.UserQualification as ApiUserQualification

@Component
class UserTransformer(
  private val probationRegionTransformer: ProbationRegionTransformer,
) {
  fun transformJpaToApi(jpa: UserEntity, serviceName: ServiceName) = when (serviceName) {
    ServiceName.approvedPremises, ServiceName.cas2 -> ApprovedPremisesUser(
      id = jpa.id,
      deliusUsername = jpa.deliusUsername,
      roles = jpa.roles.mapNotNull(::transformApprovedPremisesRoleToApi),
      email = jpa.email,
      name = jpa.name,
      telephoneNumber = jpa.telephoneNumber,
      qualifications = jpa.qualifications.map(::transformQualificationToApi),
      region = probationRegionTransformer.transformJpaToApi(jpa.probationRegion),
      service = ServiceName.approvedPremises.value,
    )
    ServiceName.temporaryAccommodation -> TemporaryAccommodationUser(
      id = jpa.id,
      roles = jpa.roles.mapNotNull(::transformTemporaryAccommodationRoleToApi),
      region = probationRegionTransformer.transformJpaToApi(jpa.probationRegion),
      service = ServiceName.temporaryAccommodation.value,
    )
  }

  private fun transformApprovedPremisesRoleToApi(userRole: UserRoleAssignmentEntity): ApprovedPremisesUserRole? = when (userRole.role) {
    UserRole.CAS1_ADMIN -> ApprovedPremisesUserRole.roleAdmin
    UserRole.CAS1_ASSESSOR -> ApprovedPremisesUserRole.assessor
    UserRole.CAS1_MATCHER -> ApprovedPremisesUserRole.matcher
    UserRole.CAS1_MANAGER -> ApprovedPremisesUserRole.manager
    UserRole.CAS1_WORKFLOW_MANAGER -> ApprovedPremisesUserRole.workflowManager
    UserRole.CAS1_APPLICANT -> ApprovedPremisesUserRole.applicant
    else -> null
  }

  private fun transformTemporaryAccommodationRoleToApi(userRole: UserRoleAssignmentEntity): TemporaryAccommodationUserRole? = when (userRole.role) {
    UserRole.CAS3_ASSESSOR -> TemporaryAccommodationUserRole.assessor
    UserRole.CAS3_REFERRER -> TemporaryAccommodationUserRole.referrer
    else -> null
  }

  private fun transformQualificationToApi(userQualification: UserQualificationAssignmentEntity): ApiUserQualification = when (userQualification.qualification) {
    UserQualification.PIPE -> ApiUserQualification.pipe
    UserQualification.WOMENS -> ApiUserQualification.womens
  }
}
