package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.usecase.GetDriverStandingsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDriverStandingsUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = GetDriverStandingsUseCase(repository)

    @Test
    fun `returns empty list when repository has no standings`() = runTest {
        assertEquals(emptyList(), useCase())
    }

    @Test
    fun `returns standings from repository`() = runTest {
        val standings = listOf(
            DriverStanding(1, "Max Verstappen", "Red Bull", 575, 19),
            DriverStanding(2, "Lando Norris", "McLaren", 356, 3)
        )
        repository.driverStandings = standings
        assertEquals(standings, useCase())
    }
}
