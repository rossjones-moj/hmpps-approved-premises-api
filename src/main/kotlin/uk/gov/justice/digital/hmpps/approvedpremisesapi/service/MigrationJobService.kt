package uk.gov.justice.digital.hmpps.approvedpremisesapi.service

import io.sentry.Sentry
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.MigrationJobType
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.UserRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.BookingStatusMigrationJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.MigrationJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.MigrationLogger
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.UpdateAllUsersFromCommunityApiJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.UpdateSentenceTypeAndSituationJob
import uk.gov.justice.digital.hmpps.approvedpremisesapi.migration.UpdateSentenceTypeAndSituationRepository
import javax.persistence.EntityManager

@Service
class MigrationJobService(
  private val applicationContext: ApplicationContext,
  private val transactionTemplate: TransactionTemplate,
  private val migrationLogger: MigrationLogger,
) {
  @Async
  fun runMigrationJobAsync(migrationJobType: MigrationJobType) = runMigrationJob(migrationJobType, 50)

  fun runMigrationJob(migrationJobType: MigrationJobType, pageSize: Int = 10) {
    migrationLogger.info("Starting migration job request: $migrationJobType")

    try {
      val job: MigrationJob = when (migrationJobType) {
        MigrationJobType.allUsersFromCommunityApi -> UpdateAllUsersFromCommunityApiJob(
          applicationContext.getBean(UserRepository::class.java),
          applicationContext.getBean(UserService::class.java),
        )
        MigrationJobType.sentenceTypeAndSituation -> UpdateSentenceTypeAndSituationJob(
          applicationContext.getBean(UpdateSentenceTypeAndSituationRepository::class.java),
        )

        MigrationJobType.bookingStatus -> BookingStatusMigrationJob(
          applicationContext.getBean(BookingRepository::class.java),
          applicationContext.getBean(EntityManager::class.java),
          pageSize,
        )
      }

      if (job.shouldRunInTransaction) {
        transactionTemplate.executeWithoutResult { job.process() }
      } else {
        job.process()
      }

      migrationLogger.info("Finished migration job: $migrationJobType")
    } catch (exception: Exception) {
      Sentry.captureException(exception)
      migrationLogger.error("Unable to complete Migration Job", exception)
    }
  }
}
