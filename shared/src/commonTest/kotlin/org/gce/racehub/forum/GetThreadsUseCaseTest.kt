package org.gce.racehub.forum

import kotlinx.coroutines.test.runTest
import org.gce.racehub.forum.domain.model.ThreadSort
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeForumRepository
import org.gce.racehub.fake.fakeThread
import org.gce.racehub.forum.domain.usecase.GetThreadsUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class GetThreadsUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = GetThreadsUseCase(repository)

    @Test
    fun `returns empty list when repository has no threads`() = runTest {
        assertEquals(DataResult.Success(emptyList()), useCase())
    }

    @Test
    fun `returns threads from repository`() = runTest {
        val threads = listOf(fakeThread(), fakeThread().copy(id = "t2", title = "Another"))
        repository.threadsResult = threads
        assertEquals(DataResult.Success(threads), useCase())
    }

    @Test
    fun `default sort argument is latest`() = runTest {
        useCase()
        assertEquals(ThreadSort.Latest, repository.lastThreadsArgs?.first)
    }

    @Test
    fun `passes sort category and userId to repository`() = runTest {
        useCase(sort = ThreadSort.Popular, category = "Race Talk", userId = "u1")
        assertEquals(Triple(ThreadSort.Popular, "Race Talk", "u1"), repository.lastThreadsArgs)
    }

    @Test
    fun `null category returns all threads`() = runTest {
        val threads = listOf(fakeThread())
        repository.threadsResult = threads
        assertEquals(DataResult.Success(threads), useCase(category = null))
    }
}
