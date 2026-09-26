package org.gce.racehub.race

import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.model.ScheduledSession
import org.gce.racehub.race.domain.model.nextRace
import org.gce.racehub.race.domain.model.parseRaceInstant
import org.gce.racehub.race.domain.model.weekendSchedule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/** Domain rules: timestamp interpretation, next-race choice, weekend schedule. */
class RaceWeekendTest {

    private fun race(round: Int, status: String, dateTime: String = "2026-05-24T20:00") = Race(
        id = "race-$round", name = "Race $round", circuit = "C", country = "X", city = "Y",
        dateTime = dateTime, round = round, status = status
    )

    @Test
    fun `zone-less API timestamp is UTC and keeps its time`() {
        assertEquals(Instant.parse("2026-05-24T20:00:00Z"), parseRaceInstant("2026-05-24T20:00"))
    }

    @Test
    fun `offset and zulu timestamps are honoured`() {
        assertEquals(Instant.parse("2026-05-24T18:00:00Z"), parseRaceInstant("2026-05-24T20:00:00.000+02:00"))
        assertEquals(Instant.parse("2025-03-16T05:00:00Z"), parseRaceInstant("2025-03-16T05:00:00Z"))
    }

    @Test
    fun `fractional seconds and space separator and date-only parse`() {
        assertEquals(Instant.parse("2026-05-06T02:47:42.264287Z"), parseRaceInstant("2026-05-06T02:47:42.264287"))
        assertEquals(Instant.parse("2026-05-24T20:00:00Z"), parseRaceInstant("2026-05-24 20:00:00"))
        assertEquals(Instant.parse("2026-05-24T00:00:00Z"), parseRaceInstant("2026-05-24"))
    }

    @Test
    fun `garbage returns null`() {
        assertNull(parseRaceInstant("not a date"))
        assertNull(parseRaceInstant(""))
    }

    @Test
    fun `next race is the first uncompleted by round`() {
        val schedule = listOf(race(3, "Upcoming"), race(1, "Completed"), race(2, "Upcoming"))
        assertEquals("race-2", schedule.nextRace()?.id)
        assertNull(listOf(race(1, "Completed")).nextRace())
    }

    @Test
    fun `estimated weekend counts back from the race start in UTC days`() {
        assertEquals(
            listOf(
                ScheduledSession("PRACTICE 1", Instant.parse("2026-05-22T17:00:00Z")),
                ScheduledSession("PRACTICE 2", Instant.parse("2026-05-22T20:00:00Z")),
                ScheduledSession("PRACTICE 3", Instant.parse("2026-05-23T17:00:00Z")),
                ScheduledSession("QUALIFYING", Instant.parse("2026-05-23T20:00:00Z")),
                ScheduledSession("RACE", Instant.parse("2026-05-24T20:00:00Z")),
            ),
            weekendSchedule(race(5, "Upcoming"), detail = null)
        )
    }

    @Test
    fun `real sessions from detail win and labels are normalised`() {
        val detail = RaceDetail(
            grandPrix = "GP", circuit = "C", overview = null, trackFacts = null, results = emptyList(), fastestLap = null,
            sessions = listOf(RaceSession("fp1", "2026-05-22T16:30"), RaceSession("Grand Prix", "2026-05-24T20:00"))
        )
        assertEquals(
            listOf(
                ScheduledSession("PRACTICE 1", Instant.parse("2026-05-22T16:30:00Z")),
                ScheduledSession("RACE", Instant.parse("2026-05-24T20:00:00Z")),
            ),
            weekendSchedule(race(5, "Upcoming"), detail)
        )
    }

    @Test
    fun `unknown race date gives the standard sessions with no times`() {
        val schedule = weekendSchedule(race = null, detail = null)
        assertEquals(listOf("PRACTICE 1", "PRACTICE 2", "PRACTICE 3", "QUALIFYING", "RACE"), schedule.map { it.label })
        assertEquals(listOf<Instant?>(null, null, null, null, null), schedule.map { it.start })
    }
}
