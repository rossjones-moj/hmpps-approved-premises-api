package uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Cas2Demand
import java.util.*

@Repository
interface Cas2DemandRepository : JpaRepository<Cas2DemandEntity, UUID> {
  @Query(
    "SELECT d FROM Cas2DemandEntity d WHERE d.id = :id",
  )
  override fun findById(id: UUID): Optional<Cas2DemandEntity>
}

@Entity
@Table(name = "cas2_demand")
data class Cas2DemandEntity(
  @Id
  val id: UUID,
  var identifier: String,
) {
  override fun toString() = "Cas2DemandEntity: $identifier with id: $id"
}
