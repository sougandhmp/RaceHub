package org.gce.racehub.race

import kotlinx.datetime.TimeZone
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.presentation.WeekendSession
import org.gce.racehub.race.presentation.parseRaceInstant
import org.gce.racehub.race.presentation.raceHeaderDate
import org.gce.racehub.race.presentation.weekendSessions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class RaceDatesTest {

    private val sydney = TimeZone.of("Australia/Sydney") // UTC+10 in May
    private val race = Race(
        id = "canadian-grand-prix", name = "Canadian Grand Prix", circuit = "Circuit Gilles Villeneuve",
        country = "Canada", city = "Montreal", dateTime = "2026-05-24T20:00", round = 5, status = "Upcoming"
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
    fun `header date is the UTC race day`() {
        assertEquals("SUN MAY 24", raceHeaderDate("2026-05-24T20:00"))
        assertEquals("", raceHeaderDate("bad"))
    }

    @Test
    fun `estimated sessions are formatted in the given zone`() {
        val sessions = weekendSessions(race, detail = null, timeZone = sydney)
        assertEquals(
            listOf(
                WeekendSession("PRACTICE 1", "FP1", "May 23", "03:00"),
                WeekendSession("PRACTICE 2", "FP2", "May 23", "06:00"),
                WeekendSession("PRACTICE 3", "FP3", "May 24", "03:00"),
                WeekendSession("QUALIFYING", "QUAL", "May 24", "06:00"),
                WeekendSession("RACE", "RACE", "May 25", "06:00"),
            ),
            sessions
        )
    }

    @Test
    fun `real sessions from detail win over the estimate`() {
        val detail = RaceDetail(
            grandPrix = "Canadian Grand Prix", circuit = "Circuit Gilles Villeneuve", overview = null,
            trackFacts = null, results = emptyList(), fastestLap = null,
            sessions = listOf(RaceSession("Practice 1", "2026-05-22T16:30"), RaceSession("Race", "2026-05-24T20:00"))
        )
        assertEquals(
            listOf(
                WeekendSession("PRACTICE 1", "FP1", "May 23", "02:30"),
                WeekendSession("RACE", "RACE", "May 25", "06:00"),
            ),
            weekendSessions(race, detail, sydney)
        )
    }

    @Test
    fun `unknown race date gives placeholders`() {
        val sessions = weekendSessions(race = null, detail = null, timeZone = sydney)
        assertEquals(5, sessions.size)
        assertEquals(WeekendSession("RACE", "RACE", "—", "—"), sessions.last())
    }
}
