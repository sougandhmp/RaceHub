package org.gce.racehub.race

import kotlinx.datetime.TimeZone
import org.gce.racehub.race.domain.model.ScheduledSession
import org.gce.racehub.race.presentation.WeekendSession
import org.gce.racehub.race.presentation.formatSessions
import org.gce.racehub.race.presentation.raceHeaderDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** Presentation formatting only; the rules themselves are covered by [RaceWeekendTest]. */
class RaceFormattingTest {

    private val sydney = TimeZone.of("Australia/Sydney") // UTC+10 in May

    @Test
    fun `header date is the UTC race day`() {
        assertEquals("SUN MAY 24", raceHeaderDate("2026-05-24T20:00"))
        assertEquals("", raceHeaderDate("bad"))
    }

    @Test
    fun `sessions are formatted in the given zone with short labels`() {
        val sessions = listOf(
            ScheduledSession("PRACTICE 1", Instant.parse("2026-05-22T16:30:00Z")),
            ScheduledSession("SPRINT QUAL", Instant.parse("2026-05-23T14:00:00Z")),
            ScheduledSession("RACE", Instant.parse("2026-05-24T20:00:00Z")),
        )
        assertEquals(
            listOf(
                WeekendSession("PRACTICE 1", "FP1", "May 23", "02:30"),
                WeekendSession("SPRINT QUAL", "SQ", "May 24", "00:00"),
                WeekendSession("RACE", "RACE", "May 25", "06:00"),
            ),
            formatSessions(sessions, sydney)
        )
    }

    @Test
    fun `unknown time renders placeholders`() {
        assertEquals(
            listOf(WeekendSession("QUALIFYING", "QUAL", "—", "—")),
            formatSessions(listOf(ScheduledSession("QUALIFYING", null)), sydney)
        )
    }
}
