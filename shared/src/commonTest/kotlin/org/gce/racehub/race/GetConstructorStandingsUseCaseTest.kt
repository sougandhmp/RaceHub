package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.usecase.GetConstructorStandingsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetConstructorStandingsUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = GetConstructorStandingsUseCase(repository)

    @Test
    fun `returns empty list when repository has no standings`() = runTest {
        assertEquals(DataResult.Success(emptyList()), useCase())
    }

    @Test
    fun `returns constructor standings from repository`() = runTest {
        val standings = listOf(
            ConstructorStanding(1, "McLaren", 666, 6),
            ConstructorStanding(2, "Ferrari", 584, 5)
        )
        repository.constructorStandings = standings
        assertEquals(DataResult.Success(standings), useCase())
    }
}
