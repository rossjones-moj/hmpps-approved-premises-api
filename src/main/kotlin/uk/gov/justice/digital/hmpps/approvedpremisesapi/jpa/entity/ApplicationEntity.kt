package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.model.PersonRisks
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.util.UUID
import javax.persistence.Convert
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.LockModeType
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

@Repository
interface ApplicationRepository : JpaRepository<ApplicationEntity, UUID> {
  @Query("SELECT a FROM ApplicationEntity a WHERE TYPE(a) = :type AND a.createdByUser.id = :id")
  fun <T : ApplicationEntity> findAllByCreatedByUser_Id(id: UUID, type: Class<T>): List<ApplicationEntity>

  @Query(
    "SELECT a FROM ApplicationEntity a " +
      "LEFT JOIN ApplicationTeamCodeEntity atc ON a = atc.application " +
      "WHERE TYPE(a) = :type AND atc.teamCode IN (:managingTeamCodes)"
  )
  fun <T : ApplicationEntity> findAllByManagingTeam(managingTeamCodes: List<String>, type: Class<T>): List<ApplicationEntity>

  @Query("SELECT a FROM ApplicationEntity a WHERE TYPE(a) = :type AND a.crn = :crn")
  fun <T : ApplicationEntity> findByCrn(crn: String, type: Class<T>): List<ApplicationEntity>

  @Query("SELECT a FROM ApplicationEntity a WHERE a.id = :id")
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  fun findByIdOrNullWithWriteLock(id: UUID): ApplicationEntity?

  @Query(
    """
SELECT
    CAST(a.id AS TEXT) as id,
    a.crn,
    CAST(a.created_by_user_id AS TEXT) as createdByUserId,
    a.created_at as createdAt,
    a.submitted_at as submittedAt,
    ass.submitted_at as latestAssessmentSubmittedAt,
    ass.decision as latestAssessmentDecision,
    (SELECT COUNT(1) FROM assessment_clarification_notes acn WHERE acn.assessment_id = ass.id AND acn.response IS NULL) > 0 as latestAssessmentHasClarificationNotesWithoutResponse,
    (SELECT COUNT(1) FROM placement_requests pr WHERE pr.application_id = apa.id) > 0 as hasPlacementRequest,
    (SELECT COUNT(1) FROM bookings b WHERE b.application_id = apa.id) > 0 as hasBooking,
    apa.is_womens_application as isWomensApplication,
    apa.is_pipe_application as isPipeApplication,
    apa.arrival_date as arrivalDate,
    CAST(apa.risk_ratings AS TEXT) as riskRatings
FROM approved_premises_applications apa
LEFT JOIN applications a ON a.id = apa.id
LEFT JOIN assessments ass ON ass.application_id = apa.id AND ass.reallocated_at IS NULL;
""",
    nativeQuery = true
  )
  fun findAllApprovedPremisesSummaries(): List<ApprovedPremisesApplicationSummary>

  @Query(
    """
SELECT
    CAST(a.id AS TEXT) as id,
    a.crn,
    CAST(a.created_by_user_id AS TEXT) as createdByUserId,
    a.created_at as createdAt,
    a.submitted_at as submittedAt,
    ass.submitted_at as latestAssessmentSubmittedAt,
    ass.decision as latestAssessmentDecision,
    (SELECT COUNT(1) FROM assessment_clarification_notes acn WHERE acn.assessment_id = ass.id AND acn.response IS NULL) > 0 as latestAssessmentHasClarificationNotesWithoutResponse,
    (SELECT COUNT(1) FROM placement_requests pr WHERE pr.application_id = apa.id) > 0 as hasPlacementRequest,
    (SELECT COUNT(1) FROM bookings b WHERE b.application_id = apa.id) > 0 as hasBooking,
    apa.is_womens_application as isWomensApplication,
    apa.is_pipe_application as isPipeApplication,
    apa.arrival_date as arrivalDate,
    CAST(apa.risk_ratings AS TEXT) as riskRatings
FROM approved_premises_applications apa
LEFT JOIN applications a ON a.id = apa.id
LEFT JOIN assessments ass ON ass.application_id = apa.id AND ass.reallocated_at IS NULL 
WHERE (SELECT COUNT(1) FROM approved_premises_application_team_codes apatc WHERE apatc.application_id = apa.id AND apatc.team_code IN :teamCodes) > 0
""",
    nativeQuery = true
  )
  fun findApprovedPremisesSummariesForManagingTeams(teamCodes: List<String>): List<ApprovedPremisesApplicationSummary>

  @Query(
    """
SELECT
    CAST(a.id AS TEXT) as id,
    a.crn,
    CAST(a.created_by_user_id AS TEXT) as createdByUserId,
    a.created_at as createdAt,
    a.submitted_at as submittedAt,
    ass.submitted_at as latestAssessmentSubmittedAt,
    ass.decision as latestAssessmentDecision,
    (SELECT COUNT(1) FROM assessment_clarification_notes acn WHERE acn.assessment_id = ass.id AND acn.response IS NULL) > 0 as latestAssessmentHasClarificationNotesWithoutResponse,
    (SELECT COUNT(1) FROM bookings b WHERE b.application_id = taa.id) > 0 as hasBooking
FROM temporary_accommodation_applications taa 
LEFT JOIN applications a ON a.id = taa.id 
LEFT JOIN assessments ass ON ass.application_id = taa.id AND ass.reallocated_at IS NULL 
WHERE a.created_by_user_id = :userId
""",
    nativeQuery = true
  )
  fun findAllTemporaryAccommodationSummariesCreatedByUser(userId: UUID): List<TemporaryAccommodationApplicationSummary>
}

@Entity
@Table(name = "applications")
@DiscriminatorColumn(name = "service")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class ApplicationEntity(
  @Id
  val id: UUID,

  val crn: String,

  @ManyToOne
  @JoinColumn(name = "created_by_user_id")
  val createdByUser: UserEntity,

  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  var data: String?,

  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  var document: String?,

  @ManyToOne
  @JoinColumn(name = "schema_version")
  var schemaVersion: JsonSchemaEntity,
  val createdAt: OffsetDateTime,
  var submittedAt: OffsetDateTime?,

  @Transient
  var schemaUpToDate: Boolean,

  @OneToMany(mappedBy = "application")
  var assessments: MutableList<AssessmentEntity>,
) {
  fun getLatestAssessment(): AssessmentEntity? = this.assessments.maxByOrNull { it.createdAt }
  abstract fun getRequiredQualifications(): List<UserQualification>
}

@Entity
@DiscriminatorValue("approved-premises")
@Table(name = "approved_premises_applications")
@PrimaryKeyJoinColumn(name = "id")
class ApprovedPremisesApplicationEntity(
  id: UUID,
  crn: String,
  createdByUser: UserEntity,
  data: String?,
  document: String?,
  schemaVersion: JsonSchemaEntity,
  createdAt: OffsetDateTime,
  submittedAt: OffsetDateTime?,
  schemaUpToDate: Boolean,
  assessments: MutableList<AssessmentEntity>,
  var isWomensApplication: Boolean?,
  var isPipeApplication: Boolean?,
  var isInapplicable: Boolean?,
  val convictionId: Long,
  val eventNumber: String,
  val offenceId: String,
  @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
  @Convert(disableConversion = true)
  val riskRatings: PersonRisks?,
  @OneToMany(mappedBy = "application")
  val teamCodes: MutableList<ApplicationTeamCodeEntity>,
  @OneToMany(mappedBy = "application")
  var placementRequests: MutableList<PlacementRequestEntity>,
  var releaseType: String?,
  var arrivalDate: OffsetDateTime?,
) : ApplicationEntity(
  id,
  crn,
  createdByUser,
  data,
  document,
  schemaVersion,
  createdAt,
  submittedAt,
  schemaUpToDate,
  assessments,
) {
  fun hasTeamCode(code: String) = teamCodes.any { it.teamCode == code }
  fun hasAnyTeamCode(codes: List<String>) = codes.any(::hasTeamCode)
  override fun getRequiredQualifications(): List<UserQualification> {
    val requiredQualifications = mutableListOf<UserQualification>()

    if (isPipeApplication == true) {
      requiredQualifications += UserQualification.PIPE
    }

    if (isWomensApplication == true) {
      requiredQualifications += UserQualification.WOMENS
    }

    return requiredQualifications
  }

  fun getLatestPlacementRequest(): PlacementRequestEntity? = this.placementRequests.maxByOrNull { it.createdAt }
  fun getLatestBooking(): BookingEntity? = getLatestPlacementRequest()?.booking
}

@Repository
interface ApplicationTeamCodeRepository : JpaRepository<ApplicationTeamCodeEntity, UUID>

@Entity
@Table(name = "approved_premises_application_team_codes")
data class ApplicationTeamCodeEntity(
  @Id
  val id: UUID,
  @ManyToOne
  @JoinColumn(name = "application_id")
  val application: ApprovedPremisesApplicationEntity,
  val teamCode: String
)

@Entity
@DiscriminatorValue("temporary-accommodation")
@Table(name = "temporary_accommodation_applications")
@PrimaryKeyJoinColumn(name = "id")
class TemporaryAccommodationApplicationEntity(
  id: UUID,
  crn: String,
  createdByUser: UserEntity,
  data: String?,
  document: String?,
  schemaVersion: JsonSchemaEntity,
  createdAt: OffsetDateTime,
  submittedAt: OffsetDateTime?,
  schemaUpToDate: Boolean,
  assessments: MutableList<AssessmentEntity>,
) : ApplicationEntity(
  id,
  crn,
  createdByUser,
  data,
  document,
  schemaVersion,
  createdAt,
  submittedAt,
  schemaUpToDate,
  assessments
) {
  override fun getRequiredQualifications(): List<UserQualification> = emptyList()
}

interface ApplicationSummary {
  fun getId(): UUID
  fun getCrn(): String
  fun getCreatedByUserId(): UUID
  fun getCreatedAt(): Timestamp
  fun getSubmittedAt(): Timestamp?
  fun getLatestAssessmentSubmittedAt(): Timestamp?
  fun getLatestAssessmentDecision(): AssessmentDecision?
  fun getLatestAssessmentHasClarificationNotesWithoutResponse(): Boolean
  fun getHasBooking(): Boolean
}

interface ApprovedPremisesApplicationSummary : ApplicationSummary {
  fun getHasPlacementRequest(): Boolean
  fun getIsWomensApplication(): Boolean?
  fun getIsPipeApplication(): Boolean?
  fun getArrivalDate(): Timestamp?
  fun getRiskRatings(): String?
}

interface TemporaryAccommodationApplicationSummary : ApplicationSummary
