package uk.gov.justice.digital.hmpps.approvedpremisesapi.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.*
import java.time.OffsetDateTime
import java.util.UUID

class Cas2DemandEntityFactory : Factory<Cas2DemandEntity> {

  private var id: Yielded<UUID> = { UUID.randomUUID() }
  private var identifier: Yielded<String> = { UUID.randomUUID().toString() }
  private var locationType: Yielded<String> = { "" }
  private var location: Yielded<String> = { "" }
  private var primaryReason: Yielded<String?> = { "Primary Reason" }
  private var secondaryReason: Yielded<String?> = { null }
  private var createdAt: Yielded<OffsetDateTime?> = { OffsetDateTime.now() }
  private var decidedAt: Yielded<OffsetDateTime?> = { OffsetDateTime.now() }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = { identifier }
  }

  fun withLocationAndType(location: String, locationType: String) = apply {
    this.location = { location }
    this.locationType = { locationType }
  }

  fun withPrimaryReason(primaryReason: String) = apply {
    this.primaryReason = { primaryReason }
  }

  fun withSecondaryReason(secondaryReason: String) = apply {
    this.secondaryReason = { secondaryReason }
  }

  fun withCreatedAt(createdAt: OffsetDateTime) = apply {
    this.createdAt = { createdAt }
  }

  fun withDecidedAt(decidedAt: OffsetDateTime) = apply {
    this.decidedAt = { decidedAt }
  }

  override fun produce(): Cas2DemandEntity = Cas2DemandEntity(
    id = this.id(),
    identifier = this.identifier(),
    location = this.location(),
    locationType = this.locationType(),
    decidedAt = this.decidedAt()!!,
    createdAt = this.createdAt()!!,
    primaryReason = this.primaryReason()!!,
    secondaryReason = this.secondaryReason(),
  )
}
