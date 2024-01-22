package uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ApplicationTimelineNote
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TimelineEvent
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.TimelineEventType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApplicationTimelineNoteEntity

@Component
class ApplicationTimelineNoteTransformer(
  private val userTransformer: UserTransformer,
) {

  fun transformJpaToApi(jpa: ApplicationTimelineNoteEntity) = ApplicationTimelineNote(
    id = jpa.id,
    createdAt = jpa.createdAt.toInstant(),
    createdByUser = userTransformer.transformJpaToApi(jpa.createdBy, ServiceName.approvedPremises),
    note = jpa.body,
  )

  fun transformToTimelineEvents(jpa: ApplicationTimelineNoteEntity) = TimelineEvent(
    type = TimelineEventType.applicationTimelineNote,
    id = jpa.id.toString(),
    occurredAt = jpa.createdAt.toInstant(),
    content = jpa.body,
    createdBy = userTransformer.transformJpaToApi(jpa.createdBy, ServiceName.approvedPremises),
    associatedUrls = emptyList(),
  )
}
