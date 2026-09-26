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

class GetTrendingThreadsUseCaseTest {

    private val repository = FakeRaceRepository()
    private val getUseCase = GetTrendingThreadsUseCase(repository)
    private val observeUseCase = ObserveTrendingThreadsUseCase(repository)

    private val threads = listOf(
        TrendingThread("t1", "Who wins Monaco?", 42, "2025-05-20T10:00:00Z"),
        TrendingThread("t2", "Best overtake of the season", 17, "2025-05-19T09:00:00Z")
    )

    @Test
    fun `returns empty list when the cache has no threads`() = runTest {
        assertEquals(emptyList(), getUseCase())
    }

    @Test
    fun `returns cached trending threads`() = runTest {
        repository.trendingThreads.value = threads
        assertEquals(threads, getUseCase())
    }

    @Test
    fun `observe emits cached trending threads`() = runTest {
        repository.trendingThreads.value = threads
        assertEquals(threads, observeUseCase().first())
    }
}
