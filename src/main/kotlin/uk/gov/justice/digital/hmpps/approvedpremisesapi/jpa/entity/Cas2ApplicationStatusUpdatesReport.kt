package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface Cas2ApplicationStatusUpdatesReportRepository : JpaRepository<DomainEventEntity, UUID> {
  @Query(
    """
      SELECT
        CAST(events.id AS TEXT) AS id,
        CAST(events.application_id AS TEXT) AS applicationId,
        events.data -> 'eventDetails' -> 'personReference' ->> 'noms' AS personNoms,
        events.data -> 'eventDetails' -> 'personReference' ->> 'crn' AS personCrn,
        events.data -> 'eventDetails' -> 'newStatus' ->> 'name' AS newStatus,
        events.data -> 'eventDetails' -> 'updatedBy' ->> 'username' AS updatedBy,
        COALESCE(string_agg(details ->> 'name', '|'), '') as statusDetails,
        TO_CHAR(
          CAST(events.data -> 'eventDetails' ->> 'updatedAt' AS TIMESTAMP),
          'YYYY-MM-DD"T"HH24:MI:SS'
         ) AS updatedAt

      FROM domain_events events
      LEFT JOIN LATERAL json_array_elements(events.data -> 'eventDetails' -> 'newStatus' -> 'statusDetails') as details ON true
      WHERE events.type = 'CAS2_APPLICATION_STATUS_UPDATED'
        AND events.occurred_at  > CURRENT_DATE - 365
      GROUP BY events.id  
      ORDER BY updatedAt DESC;
    """,
    nativeQuery = true,
  )
  fun generateApplicationStatusUpdatesReportRows(): List<Cas2ApplicationStatusUpdatedReportRow>
}

interface Cas2ApplicationStatusUpdatedReportRow {
  fun getId(): String
  fun getApplicationId(): String
  fun getUpdatedBy(): String
  fun getUpdatedAt(): String
  fun getPersonNoms(): String
  fun getPersonCrn(): String
  fun getNewStatus(): String
  fun getStatusDetails(): String
}
