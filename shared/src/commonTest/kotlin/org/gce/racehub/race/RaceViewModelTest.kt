package org.gce.racehub.race

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase
import org.gce.racehub.race.presentation.RaceError
import org.gce.racehub.race.presentation.RaceIntent
import org.gce.racehub.race.presentation.RaceViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class RaceViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeHomeRepository()

    private fun race(round: Int, status: String) = Race(
        id = "race-$round", name = "Race $round", circuit = "C$round", country = "X", city = "Y",
        dateTime = "2026-05-${10 + round}T20:00", round = round, status = status
    )

    private fun detail(name: String, vararg sessions: RaceSession) =
        RaceDetail(name, "Circuit", null, null, sessions.toList(), emptyList(), null)

    private fun viewModel() = RaceViewModel(
        GetRaceScheduleUseCase(repo), GetDriverStandingsUseCase(repo), GetConstructorStandingsUseCase(repo),
        GetTrendingThreadsUseCase(repo), GetRaceDetailUseCase(repo)
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads once on creation and sorts by round and picks the first uncompleted race`() = runTest(dispatcher) {
        repo.raceSchedule = listOf(race(3, "Upcoming"), race(1, "Completed"), race(2, "Upcoming"))
        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(1, repo.raceScheduleCalls)
        assertEquals(listOf(1, 2, 3), state.raceSchedule.map { it.round })
        assertEquals("race-2", state.nextRace?.id)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `next race sessions switch from estimate to real once detail loads`() = runTest(dispatcher) {
        repo.raceSchedule = listOf(race(1, "Upcoming"))
        repo.raceDetailBySlug = mapOf("race-1" to detail("GP 1", RaceSession("Sprint", "2026-05-10T12:00")))
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(listOf("SPRINT"), vm.state.value.nextRaceSessions.map { it.label })
        assertEquals("GP 1", vm.state.value.nextRaceDetail?.grandPrix)
    }

    @Test
    fun `failed load reports an error and dismiss clears it`() = runTest(dispatcher) {
        repo.raceScheduleError = IllegalStateException("offline")
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(RaceError.LoadFailed, vm.state.value.error)

        vm.onIntent(RaceIntent.DismissError)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `slow detail for a previously selected race never overwrites the current one`() = runTest(dispatcher) {
        repo.raceSchedule = listOf(race(1, "Upcoming"), race(2, "Upcoming"))
        repo.raceDetailBySlug = mapOf("race-1" to detail("Slow GP"), "race-2" to detail("Fast GP"))
        val vm = viewModel()
        advanceUntilIdle()
        repo.raceDetailDelayMs = mapOf("race-1" to 5_000L, "race-2" to 100L)

        vm.onIntent(RaceIntent.SelectRace("race-1"))
        vm.onIntent(RaceIntent.SelectRace("race-2"))
        advanceUntilIdle()

        assertEquals("race-2", vm.state.value.selectedRace?.id)
        assertEquals("Fast GP", vm.state.value.selectedRaceDetail?.grandPrix)
        assertFalse(vm.state.value.isLoadingSelectedDetail)
    }
}
