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

class GetRaceScheduleUseCaseTest {

    private val repository = FakeRaceRepository()
    private val getUseCase = GetRaceScheduleUseCase(repository)
    private val observeUseCase = ObserveRaceScheduleUseCase(repository)

    private fun race(id: String, status: String) = Race(
        id = id, name = "GP $id", circuit = "C", country = "Country",
        city = "City", dateTime = "2025-03-16T15:00:00Z", round = 1, status = status
    )

    @Test
    fun `returns empty list when the cache has no races`() = runTest {
        assertEquals(emptyList(), getUseCase())
    }

    @Test
    fun `returns all cached races in order`() = runTest {
        val races = (1..5).map { race("r$it", if (it < 3) "COMPLETED" else "UPCOMING") }
        repository.raceSchedule.value = races
        assertEquals(races, getUseCase())
    }

    @Test
    fun `observe emits the cached races and emits again when the cache changes`() = runTest {
        repository.raceSchedule.value = listOf(race("r1", "UPCOMING"))
        assertEquals(listOf(race("r1", "UPCOMING")), observeUseCase().first())

        repository.raceSchedule.value = listOf(race("r1", "COMPLETED"), race("r2", "UPCOMING"))
        assertEquals(2, observeUseCase().first().size)
    }
}
