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

class GetDriverStandingsUseCaseTest {

    private val repository = FakeRaceRepository()
    private val getUseCase = GetDriverStandingsUseCase(repository)
    private val observeUseCase = ObserveDriverStandingsUseCase(repository)

    private val standings = listOf(
        DriverStanding(1, "Max Verstappen", "Red Bull", 575, 19),
        DriverStanding(2, "Lando Norris", "McLaren", 356, 3)
    )

    @Test
    fun `returns empty list when the cache has no standings`() = runTest {
        assertEquals(emptyList(), getUseCase())
    }

    @Test
    fun `returns cached standings`() = runTest {
        repository.driverStandings.value = standings
        assertEquals(standings, getUseCase())
    }

    @Test
    fun `observe emits cached standings`() = runTest {
        repository.driverStandings.value = standings
        assertEquals(standings, observeUseCase().first())
    }
}
