package uk.gov.justice.digital.hmpps.approvedpremisesapi.integration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.test.context.event.annotation.BeforeTestMethod
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Configuration
class MutableClockConfiguration {

  val clock: MutableClock
    @Bean
    @Primary
    get() = MutableClock()

  @BeforeTestMethod
  fun reset() {
    clock.reset()
  }

  class MutableClock : Clock() {
    var fixedTime: Instant? = null

    fun reset() = run { fixedTime = null }

    fun setNow(now: LocalDateTime) {
      fixedTime = now.toInstant(ZoneOffset.UTC)
    }

    override fun instant(): Instant {
      return fixedTime ?: Instant.now()
    }

    override fun withZone(zone: ZoneId?): Clock {
      error("Not supported")
    }

    override fun getZone(): ZoneId = ZoneId.systemDefault()
  }
}
