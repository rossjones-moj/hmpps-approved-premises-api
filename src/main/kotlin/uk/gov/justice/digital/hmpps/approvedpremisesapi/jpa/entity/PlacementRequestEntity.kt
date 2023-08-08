package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.PlacementRequestStatus
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Repository
interface PlacementRequestRepository : JpaRepository<PlacementRequestEntity, UUID> {
  fun findByApplication(application: ApplicationEntity): PlacementRequestEntity?

  fun findAllByApplication(application: ApplicationEntity): List<PlacementRequestEntity>

  fun findAllByAllocatedToUser_IdAndReallocatedAtNullAndIsWithdrawnFalse(userId: UUID): List<PlacementRequestEntity>

  fun findAllByReallocatedAtNullAndBooking_IdNullAndIsWithdrawnFalse(): List<PlacementRequestEntity>

  fun findAllByIsParoleAndReallocatedAtNullAndIsWithdrawnFalse(isParole: Boolean, pageable: Pageable?): Page<PlacementRequestEntity>

  @Query(
    """
    SELECT
      pq.*
    from
      placement_requests pq
    where
      pq.reallocated_at IS NULL
      AND pq.is_withdrawn IS FALSE
      AND (
        CASE
          WHEN (
            SELECT
              COUNT(booking)
            from
              bookings booking
              left join cancellations c on c.booking_id = booking.id
            WHERE
              booking.id = pq.booking_id
              AND c.id IS NULL
          ) > 0 THEN 'matched'
          WHEN (
            SELECT
              COUNT(bnm)
            from
              booking_not_mades bnm
            WHERE
              bnm.placement_request_id = pq.id
          ) > 0 THEN 'unableToMatch'
          ELSE 'notMatched'
        END
      ) = :#{#status.toString()} 
      AND (:crn IS NULL OR (SELECT COUNT(1) FROM applications a WHERE a.id = pq.application_id AND a.crn = UPPER(:crn)) = 1)
  """,
    nativeQuery = true,
  )
  fun allForDashboard(status: PlacementRequestStatus, crn: String?, pageable: Pageable?): Page<PlacementRequestEntity>
}

@Entity
@Table(name = "placement_requests")
data class PlacementRequestEntity(
  @Id
  val id: UUID,
  val expectedArrival: LocalDate,
  val duration: Int,

  @ManyToOne
  @JoinColumn(name = "application_id")
  val application: ApprovedPremisesApplicationEntity,

  @ManyToOne
  @JoinColumn(name = "assessment_id")
  val assessment: AssessmentEntity,

  val createdAt: OffsetDateTime,

  val notes: String?,

  @ManyToOne
  @JoinColumn(name = "booking_id")
  var booking: BookingEntity?,

  @ManyToOne
  @JoinColumn(name = "allocated_to_user_id")
  val allocatedToUser: UserEntity,

  @OneToMany(mappedBy = "placementRequest")
  var bookingNotMades: MutableList<BookingNotMadeEntity>,

  var reallocatedAt: OffsetDateTime?,

  @ManyToOne
  @JoinColumn(name = "placement_requirements_id")
  var placementRequirements: PlacementRequirementsEntity,

  var isParole: Boolean,
  var isWithdrawn: Boolean,
)
