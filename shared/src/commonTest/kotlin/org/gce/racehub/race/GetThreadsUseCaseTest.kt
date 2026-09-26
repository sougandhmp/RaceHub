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

class GetThreadsUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = GetThreadsUseCase(repository)

    @Test
    fun `returns empty list when repository has no threads`() = runTest {
        assertEquals(emptyList(), useCase().data)
    }

    @Test
    fun `returns threads from repository`() = runTest {
        val threads = listOf(fakeThread(), fakeThread().copy(id = "t2", title = "Another"))
        repository.threadsResult = DataResult.success(threads)
        assertEquals(threads, useCase().data)
    }

    @Test
    fun `default sort argument is latest`() = runTest {
        useCase()
        assertEquals("latest", repository.lastGetThreadsArgs?.first)
    }

    @Test
    fun `passes sort category and userId to repository`() = runTest {
        useCase(sort = "top", category = "Race Talk", userId = "u1")
        assertEquals(Triple("top", "Race Talk", "u1"), repository.lastGetThreadsArgs)
    }

    @Test
    fun `a failure is reported instead of an empty list`() = runTest {
        repository.threadsResult = DataResult.failure(DataError.NoConnection)
        val result = useCase()
        assertFalse(result.isSuccess)
        assertNull(result.data)
        assertEquals(DataError.NoConnection, result.error)
    }
}
