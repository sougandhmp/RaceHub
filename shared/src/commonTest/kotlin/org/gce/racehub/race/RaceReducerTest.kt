package org.gce.racehub.race

import kotlinx.datetime.TimeZone
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.presentation.RaceMutation
import org.gce.racehub.race.presentation.RaceReducer
import org.gce.racehub.race.presentation.RaceState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The reducer is pure: every case is a plain (state, mutation) -> state assertion. */
class RaceReducerTest {

    private val reducer = RaceReducer(TimeZone.of("Australia/Sydney"))

    private fun race(round: Int, status: String) = Race(
        id = "race-$round", name = "Race $round", circuit = "C", country = "X", city = "Y",
        dateTime = "2026-05-24T20:00", round = round, status = status
    )

    private fun detail(vararg sessions: RaceSession) =
        RaceDetail("GP", "C", null, null, sessions.toList(), emptyList(), null)

    private fun loaded(vararg races: Race) = RaceMutation.Loaded(races.toList(), emptyList(), emptyList(), emptyList())

    @Test
    fun `load started then loaded picks the next race and estimates its sessions`() {
        val loading = reducer.reduce(RaceState(), RaceMutation.LoadStarted)
        assertTrue(loading.isLoading)

        val state = reducer.reduce(loading, loaded(race(1, "Completed"), race(2, "Upcoming")))
        assertFalse(state.isLoading)
        assertEquals("race-2", state.nextRace?.id)
        assertEquals(listOf("FP1", "FP2", "FP3", "QUAL", "RACE"), state.nextRaceSessions.map { it.shortLabel })
        assertEquals("06:00", state.nextRaceSessions.last().time)
    }

    @Test
    fun `load failure only clears the loading flag and keeps shown data`() {
        val shown = reducer.reduce(RaceState(), loaded(race(1, "Upcoming")))
        val state = reducer.reduce(shown.copy(isLoading = true), RaceMutation.LoadFailed)
        assertFalse(state.isLoading)
        assertEquals(shown.raceSchedule, state.raceSchedule)
    }

    @Test
    fun `next race detail replaces the estimate with real sessions`() {
        val shown = reducer.reduce(RaceState(), loaded(race(1, "Upcoming")))
        val state = reducer.reduce(shown, RaceMutation.NextRaceDetailLoaded(detail(RaceSession("Sprint", "2026-05-23T10:00"))))
        assertEquals(listOf("SPRINT"), state.nextRaceSessions.map { it.label })
    }

    @Test
    fun `failed detail fetch keeps the previous detail`() {
        val withDetail = reducer.reduce(
            reducer.reduce(RaceState(), loaded(race(1, "Upcoming"))),
            RaceMutation.NextRaceDetailLoaded(detail(RaceSession("Race", "2026-05-24T20:00")))
        )
        val state = reducer.reduce(withDetail, RaceMutation.NextRaceDetailLoaded(null))
        assertEquals(withDetail.nextRaceDetail, state.nextRaceDetail)
    }

    @Test
    fun `reload drops detail that belonged to a different next race`() {
        val withDetail = reducer.reduce(
            reducer.reduce(RaceState(), loaded(race(1, "Upcoming"))),
            RaceMutation.NextRaceDetailLoaded(detail(RaceSession("Race", "2026-05-24T20:00")))
        )
        val state = reducer.reduce(withDetail, loaded(race(1, "Completed"), race(2, "Upcoming")))
        assertEquals("race-2", state.nextRace?.id)
        assertNull(state.nextRaceDetail)
    }

    @Test
    fun `selecting a race resets its detail until loaded`() {
        val selected = reducer.reduce(RaceState(), RaceMutation.RaceSelected(race(3, "Upcoming")))
        assertTrue(selected.isLoadingSelectedDetail)
        assertNull(selected.selectedRaceDetail)
        assertEquals(5, selected.selectedRaceSessions.size)

        val state = reducer.reduce(selected, RaceMutation.SelectedRaceDetailLoaded(detail(RaceSession("Race", "2026-05-24T20:00"))))
        assertFalse(state.isLoadingSelectedDetail)
        assertEquals(listOf("RACE"), state.selectedRaceSessions.map { it.label })
    }
}
