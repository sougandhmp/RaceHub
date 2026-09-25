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

class GetConstructorStandingsUseCaseTest {

    private val repository = FakeRaceRepository()
    private val getUseCase = GetConstructorStandingsUseCase(repository)
    private val observeUseCase = ObserveConstructorStandingsUseCase(repository)

    private val standings = listOf(
        ConstructorStanding(1, "McLaren", 666, 6),
        ConstructorStanding(2, "Ferrari", 652, 5)
    )

    @Test
    fun `returns empty list when the cache has no standings`() = runTest {
        assertEquals(emptyList(), getUseCase())
    }

    @Test
    fun `returns cached constructor standings`() = runTest {
        repository.constructorStandings.value = standings
        assertEquals(standings, getUseCase())
    }

    @Test
    fun `observe emits cached constructor standings`() = runTest {
        repository.constructorStandings.value = standings
        assertEquals(standings, observeUseCase().first())
    }
}
