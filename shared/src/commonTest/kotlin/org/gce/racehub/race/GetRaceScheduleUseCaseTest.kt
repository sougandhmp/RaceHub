package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeRaceRepository
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRaceScheduleUseCaseTest {

    private val repository = FakeRaceRepository()
    private val useCase = GetRaceScheduleUseCase(repository)

    private fun race(id: String, status: String, round: Int = 1) = Race(
        id = id, name = "GP $id", circuit = "C", country = "Country",
        city = "City", dateTime = "2025-03-16T15:00:00Z", round = round, status = status
    )

    @Test
    fun `returns empty list when repository has no races`() = runTest {
        assertEquals(DataResult.Success(emptyList()), useCase())
    }

    @Test
    fun `returns all races from repository`() = runTest {
        val races = listOf(race("r1", "COMPLETED"), race("r2", "UPCOMING"))
        repository.raceSchedule = races
        assertEquals(DataResult.Success(races), useCase())
    }

    @Test
    fun `orders races by round`() = runTest {
        repository.raceSchedule = listOf(race("r3", "UPCOMING", 3), race("r1", "COMPLETED", 1), race("r2", "UPCOMING", 2))
        assertEquals(listOf("r1", "r2", "r3"), (useCase() as DataResult.Success).data.map { it.id })
    }

    @Test
    fun `passes a repository failure through`() = runTest {
        repository.raceScheduleError = DataError.Network
        assertEquals(DataResult.Failure(DataError.Network), useCase())
    }
}
