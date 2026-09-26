package org.gce.racehub.race

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.fake.FakeRaceRepository
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase
import org.gce.racehub.race.presentation.RaceEffect
import org.gce.racehub.race.presentation.RaceIntent
import org.gce.racehub.race.presentation.RaceViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Intent handling, async orchestration and effects; state transitions are covered by [RaceReducerTest]. */
@OptIn(ExperimentalCoroutinesApi::class)
class RaceViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeRaceRepository()

    private fun race(round: Int, status: String) = Race(
        id = "race-$round", name = "Race $round", circuit = "C$round", country = "X", city = "Y",
        dateTime = "2026-05-${10 + round}T20:00", round = round, status = status
    )

    private fun detail(name: String, vararg sessions: RaceSession) =
        RaceDetail(name, "Circuit", null, null, sessions.toList(), emptyList(), null)

    private fun viewModel() = RaceViewModel(
        GetRaceScheduleUseCase(repo), GetDriverStandingsUseCase(repo), GetConstructorStandingsUseCase(repo),
        GetTrendingThreadsUseCase(repo), GetRaceDetailUseCase(repo), TimeZone.UTC
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads once on creation and orders the schedule by round`() = runTest(dispatcher) {
        repo.raceSchedule = listOf(race(3, "Upcoming"), race(1, "Completed"), race(2, "Upcoming"))
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(1, repo.raceScheduleCalls)
        assertEquals(listOf(1, 2, 3), vm.state.value.raceSchedule.map { it.round })
        assertEquals("race-2", vm.state.value.nextRace?.id)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `fetches the next race detail after loading`() = runTest(dispatcher) {
        repo.raceSchedule = listOf(race(1, "Upcoming"))
        repo.raceDetailBySlug = mapOf("race-1" to detail("GP 1", RaceSession("Sprint", "2026-05-10T12:00")))
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals("GP 1", vm.state.value.nextRaceDetail?.grandPrix)
        assertEquals(listOf("SPRINT"), vm.state.value.nextRaceSessions.map { it.label })
    }

    @Test
    fun `failed load emits a one-off error effect and stops loading`() = runTest(dispatcher) {
        repo.raceScheduleError = DataError.Network
        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(RaceEffect.ShowLoadError(DataError.Network), vm.effects.first())
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `refresh reloads`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onIntent(RaceIntent.Refresh)
        advanceUntilIdle()
        assertEquals(2, repo.raceScheduleCalls)
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
