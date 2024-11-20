package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.GarethApiDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.*
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.*

@Service
class GarethController(
  private val httpAuthService: HttpAuthService,
  private val offenderService: OffenderService,
  private val personTransformer: PersonTransformer,
  private val risksTransformer: RisksTransformer,
  private val prisonCaseNoteTransformer: PrisonCaseNoteTransformer,
  private val adjudicationTransformer: AdjudicationTransformer,
  private val alertTransformer: AlertTransformer,
  private val needsDetailsTransformer: NeedsDetailsTransformer,
  private val oaSysSectionsTransformer: OASysSectionsTransformer,
  private val offenceTransformer: OffenceTransformer,
  private val userService: UserService,
  private val applicationService: ApplicationService,
  private val personalTimelineTransformer: PersonalTimelineTransformer,
  private val featureFlagService: FeatureFlagService,
) : GarethApiDelegate {


  override fun garethGet(xServiceName: ServiceName, month: Int): ResponseEntity<String> {
    return ResponseEntity.ok("Hello Gareth")
  }
}