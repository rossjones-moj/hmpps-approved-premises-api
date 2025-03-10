package uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.SeedJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.SeedLogger
import uk.gov.justice.digital.hmpps.approvedpremisesapi.service.UserService
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
/**
 * Seeds users, without touching roles and qualifications.
 *
 *  If you want to set roles and qualifications as part of
 *  the seeding then look at UsersSeedJob.
 */
@Component
class ApStaffUsersSeedJob(
  private val userService: UserService,
  private val seedLogger: SeedLogger,
) : SeedJob<ApStaffUserSeedCsvRow>(
  requiredHeaders = setOf(
    "deliusUsername",
  ),
) {

  override fun deserializeRow(columns: Map<String, String>) = ApStaffUserSeedCsvRow(
    deliusUsername = columns["deliusUsername"]!!.trim().uppercase(),
  )

  @SuppressWarnings("TooGenericExceptionThrown", "TooGenericExceptionCaught")
  override fun processRow(row: ApStaffUserSeedCsvRow) {
    seedLogger.info("Processing AP Staff seeding for ${row.deliusUsername}")

    val user = try {
      userService.getExistingUserOrCreateDeprecated(row.deliusUsername)
    } catch (exception: Exception) {
      throw RuntimeException("Could not get user ${row.deliusUsername}", exception)
    }
    seedLogger.info(seedingReport(user))
  }

  private fun seedingReport(user: UserEntity): String {
    val timestamp = user.updatedAt ?: user.createdAt
    val ageInMinutes = ChronoUnit.MINUTES.between(timestamp, OffsetDateTime.now())
    return "-> User record for: ${user.deliusUsername} last updated $ageInMinutes mins ago"
  }
}

data class ApStaffUserSeedCsvRow(
  val deliusUsername: String,
)
