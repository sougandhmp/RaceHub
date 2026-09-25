package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.usecase.GetRaceScheduleUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRaceScheduleUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = GetRaceScheduleUseCase(repository)

    private fun race(id: String, status: String) = Race(
        id = id, name = "GP $id", circuit = "C", country = "Country",
        city = "City", dateTime = "2025-03-16T15:00:00Z", round = 1, status = status
    )

    @Test
    fun `returns empty list when repository has no races`() = runTest {
        assertEquals(emptyList(), useCase())
    }

    @Test
    fun `returns all races from repository`() = runTest {
        val races = listOf(race("r1", "COMPLETED"), race("r2", "UPCOMING"))
        repository.raceSchedule = races
        assertEquals(races, useCase())
    }

    @Test
    fun `preserves order returned by repository`() = runTest {
        val races = (1..5).map { race("r$it", "UPCOMING") }
        repository.raceSchedule = races
        assertEquals(races, useCase())
    }
}
