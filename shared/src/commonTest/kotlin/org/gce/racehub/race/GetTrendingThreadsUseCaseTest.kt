package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.usecase.GetTrendingThreadsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetTrendingThreadsUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = GetTrendingThreadsUseCase(repository)

    @Test
    fun `returns empty list when repository has no threads`() = runTest {
        assertEquals(DataResult.Success(emptyList()), useCase())
    }

    @Test
    fun `returns trending threads from repository`() = runTest {
        val threads = listOf(
            TrendingThread("t1", "Hot take on qualifying", 120, "2025-05-01"),
            TrendingThread("t2", "Race predictions", 85, "2025-05-02")
        )
        repository.trendingThreads = threads
        assertEquals(DataResult.Success(threads), useCase())
    }
}
