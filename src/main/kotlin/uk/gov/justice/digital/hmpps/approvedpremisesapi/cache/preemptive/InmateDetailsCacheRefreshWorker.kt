package uk.gov.justice.digital.hmpps.approvedpremisesapi.cache.preemptive

import redis.lock.redlock.RedLock
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.PreemptiveCacheEntryStatus
import uk.gov.justice.digital.hmpps.approvedpremisesapi.client.PrisonsApiClient
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.ApplicationRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BookingRepository

class InmateDetailsCacheRefreshWorker(
  private val applicationRepository: ApplicationRepository,
  private val bookingRepository: BookingRepository,
  private val prisonsApiClient: PrisonsApiClient,
  private val loggingEnabled: Boolean,
  private val delayMs: Long,
  redLock: RedLock,
  lockDurationMs: Int,
) : CacheRefreshWorker(redLock, "inmateDetails", lockDurationMs) {
  override fun work(checkShouldStop: () -> Boolean) {
    val distinctNomsNumbers = (applicationRepository.getDistinctNomsNumbers() + bookingRepository.getDistinctNomsNumbers()).distinct()

    if (loggingEnabled) { log.info("Got $distinctNomsNumbers to refresh for Inmate Details") }

    distinctNomsNumbers.forEach {
      if (checkShouldStop()) return

      interruptableSleep(50)

      val cacheEntryStatus = prisonsApiClient.getInmateDetailsCacheEntryStatus(it)

      if (cacheEntryStatus == PreemptiveCacheEntryStatus.EXISTS) {
        if (loggingEnabled) {
          log.info("No upstream call made when refreshing Inmate Details for $it, stored result still within soft TTL")
        }

        return@forEach
      }

      val prisonsApiResult = prisonsApiClient.getInmateDetailsWithCall(it)
      if (prisonsApiResult is ClientResult.Failure.StatusCode) {
        if (!prisonsApiResult.isPreemptivelyCachedResponse) {
          log.error("Unable to refresh Inmate Details for $it, response status: ${prisonsApiResult.status}")
        }
      }

      if (prisonsApiResult is ClientResult.Failure.Other) {
        log.error("Unable to refresh Inmate Details for $it: ${prisonsApiResult.exception.message}")
      }

      if (prisonsApiResult is ClientResult.Success) {
        if (loggingEnabled) {
          log.info("Successfully refreshed Inmate Details for $it")
        }
      }
    }

    interruptableSleep(delayMs)
  }
}
