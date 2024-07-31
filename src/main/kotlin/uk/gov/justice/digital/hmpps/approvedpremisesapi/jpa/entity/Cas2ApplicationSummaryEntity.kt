package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface ApplicationSummaryRepository : JpaRepository<Cas2ApplicationSummaryEntity, String> {
  fun findByUserId(userId: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findByUserIdAndSubmittedAtIsNotNull(userId: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findByUserIdAndSubmittedAtIsNull(userId: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findByPrisonCode(prisonCode: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findByPrisonCodeAndSubmittedAtIsNotNull(prisonCode: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findByPrisonCodeAndSubmittedAtIsNull(prisonCode: String, pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>

  fun findBySubmittedAtIsNotNull(pageable: Pageable?): Page<Cas2ApplicationSummaryEntity>
}

@Entity
@Table(name = "cas_2_application_live_summary")
data class Cas2ApplicationSummaryEntity(
  @Id
  val id: UUID,
  val crn: String,
  @Column(name = "noms_number")
  var nomsNumber: String,
  @Column(name = "created_by_user_id")
  val userId: String,
  @Column(name = "name")
  val userName: String,
  @Column(name = "created_at")
  val createdAt: OffsetDateTime,
  @Column(name = "submitted_at")
  var submittedAt: OffsetDateTime?,
  @Column(name = "abandoned_at")
  var abandonedAt: OffsetDateTime? = null,
  @Column(name = "hdc_eligibility_date")
  var hdcEligibilityDate: LocalDate? = null,
  @Column(name = "label")
  var latestStatusUpdateLabel: String? = null,
  @Column(name = "status_id")
  var latestStatusUpdateStatusId: String? = null,
  @Column(name = "referring_prison_code")
  val prisonCode: String,
)
