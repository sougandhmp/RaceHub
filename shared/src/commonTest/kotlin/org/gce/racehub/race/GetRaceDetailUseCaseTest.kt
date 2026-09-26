package org.gce.racehub.race

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.DataError
import org.gce.racehub.core.DataResult
import org.gce.racehub.fake.*
import org.gce.racehub.race.domain.model.*
import org.gce.racehub.race.domain.usecase.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        repository.raceDetailResult = DataResult.success(detail)
        assertEquals(detail, useCase("bahrain-2025").data)
    }

    @Test
    fun `forwards slug argument to repository`() = runTest {
        useCase("monaco-2025")
        assertEquals("monaco-2025", repository.lastRaceDetailSlug)
    }

    @Test
    fun `passes a repository failure through`() = runTest {
        repository.raceDetailResult = DataResult.failure(DataError.NoConnection)
        assertEquals(DataError.NoConnection, useCase("monaco-2025").error)
    }
}
