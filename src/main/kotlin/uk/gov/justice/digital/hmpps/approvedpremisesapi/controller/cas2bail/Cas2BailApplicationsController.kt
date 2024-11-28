package uk.gov.justice.digital.hmpps.approvedpremisesapi.controller.cas2bail

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2.ApplicationsCas2Delegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.cas2bail.ApplicationsCas2BailDelegate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.Application
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.HttpAuthService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.NomisUserService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2.ApplicationService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.cas2.OffenderService
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.cas2.ApplicationsTransformer
import java.util.*

//ApplicationsCas2BailDelegate

@Service
class Cas2BailApplicationsController(
  private val httpAuthService: HttpAuthService,
  private val applicationService: ApplicationService,
  private val applicationsTransformer: ApplicationsTransformer,
  private val objectMapper: ObjectMapper,
  private val offenderService: OffenderService,
  private val userService: NomisUserService,
) : ApplicationsCas2BailDelegate {


  override fun applicationsGet(isSubmitted: Boolean?, page: Int?, prisonCode: String?): ResponseEntity<List<String>> {
    val list = listOf("hello world")
    val response = ResponseEntity.ok().body(
      list
    )
    return response
  }
}