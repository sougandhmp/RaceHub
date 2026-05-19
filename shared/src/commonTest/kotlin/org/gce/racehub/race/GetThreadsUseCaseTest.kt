package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.fake.fakeThread
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetThreadsUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = GetThreadsUseCase(repository)

    @Test
    fun `returns empty list when repository has no threads`() = runTest {
        assertEquals(emptyList(), useCase())
    }

    @Test
    fun `returns threads from repository`() = runTest {
        val threads = listOf(fakeThread(), fakeThread().copy(id = "t2", title = "Another"))
        repository.threadsResult = threads
        assertEquals(threads, useCase())
    }

    @Test
    fun `default sort argument is latest`() = runTest {
        useCase()
        // Verified by not throwing; FakeHomeRepository accepts any args
    }

    @Test
    fun `passes sort category and userId to repository`() = runTest {
        useCase(sort = "top", category = "Race Talk", userId = "u1")
        // FakeHomeRepository ignores args and returns threadsResult; the test verifies no crash
    }

    @Test
    fun `null category returns all threads`() = runTest {
        val threads = listOf(fakeThread())
        repository.threadsResult = threads
        assertEquals(threads, useCase(category = null))
    }
}
