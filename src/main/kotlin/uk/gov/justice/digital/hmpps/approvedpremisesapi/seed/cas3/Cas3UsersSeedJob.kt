package uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.cas3

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.AbstractUsersSeedJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserService

@Component
class Cas3UsersSeedJob(userService: UserService) : AbstractUsersSeedJob(listOf(ServiceName.temporaryAccommodation), userService)
