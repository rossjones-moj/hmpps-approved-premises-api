package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.specification

import org.springframework.data.jpa.domain.Specification
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserQualification
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserQualificationAssignmentEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRole
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRoleAssignmentEntity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

fun hasQualificationsAndRoles(qualifications: List<UserQualification>?, roles: List<UserRole>?, showOnlyActive: Boolean = false): Specification<UserEntity> {
  return Specification { root: Root<UserEntity>, _: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder ->
    val predicates = mutableListOf<Predicate>()

    if (qualifications?.isNotEmpty() == true) {
      val userQualifications = root
        .join<UserEntity, MutableList<UserQualificationAssignmentEntity>>(UserEntity::qualifications.name)
        .get<UserQualificationAssignmentEntity>(UserQualificationAssignmentEntity::qualification.name)

      predicates.add(
        criteriaBuilder.and(
          userQualifications.`in`(qualifications),
        ),
      )
    }

    if (roles?.isNotEmpty() == true) {
      val userRoles = root
        .join<UserEntity, MutableList<UserEntity>>(UserEntity::roles.name)
        .get<UserRole>(UserRoleAssignmentEntity::role.name)

      predicates.add(
        criteriaBuilder.and(
          userRoles.`in`(roles),
        ),
      )
    }

    if (showOnlyActive) {
      predicates.add(
        criteriaBuilder.and(
          criteriaBuilder.isTrue(root.get("isActive")),
        ),
      )
    }

    criteriaBuilder.and(*predicates.toTypedArray())
  }
}
