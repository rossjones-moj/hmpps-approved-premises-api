package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface Cas2BailDemandReportRepository : JpaRepository<DomainEventEntity, UUID> {
  @Query(
    """
      SELECT
        CAST(identifier AS TEXT) AS identifier

      FROM cas2_demand  
      ORDER BY decided_at DESC;
    """,
    nativeQuery = true,
  )
  fun generateBailDemandReport(): List<Cas2BailDemandReportRow>
}

interface Cas2BailDemandReportRow {
  fun getIdentifier(): String
}
