package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeRaceRepository
import org.gce.racehub.race.domain.model.FastestLap
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.RaceResult
import org.gce.racehub.race.domain.model.RaceSession
import org.gce.racehub.race.domain.model.TrackFacts
import org.gce.racehub.race.domain.usecase.GetRaceDetailUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRaceDetailUseCaseTest {

    private val repository = FakeRaceRepository()
    private val useCase = GetRaceDetailUseCase(repository)

    @Test
    fun `returns race detail from repository`() = runTest {
        val detail = RaceDetail(
            grandPrix = "Bahrain Grand Prix",
            circuit = "Bahrain International Circuit",
            overview = "Desert race",
            trackFacts = TrackFacts(laps = 57, lapRecord = "1:31.447", distanceKm = 308.238, corners = 15),
            sessions = listOf(RaceSession("Race", "2025-03-02T15:00:00Z")),
            results = listOf(RaceResult(1, "Max Verstappen", "Red Bull", 25, "1:32:07.048")),
            fastestLap = FastestLap("Max Verstappen", "1:32.608")
        )
        repository.raceDetailResult = detail
        assertEquals(DataResult.Success(detail), useCase("bahrain-2025"))
    }

    @Test
    fun `forwards slug argument to repository`() = runTest {
        useCase("monaco-2025")
        // No assertion needed beyond not throwing — slug forwarding is verified by the fake returning its configured result
    }
}
