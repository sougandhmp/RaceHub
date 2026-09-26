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

class LikeThreadUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = LikeThreadUseCase(repository)

    @Test
    fun `blank threadId is rejected without calling the repository`() = runTest {
        assertIs<DataError.InvalidInput>(useCase("").error)
        assertNull(repository.lastLikedThreadId)
    }

    @Test
    fun `whitespace threadId is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(useCase("   ").error)
    }

    @Test
    fun `valid threadId delegates to repository and returns updated count`() = runTest {
        repository.likeThreadResult = DataResult.success(42)
        assertEquals(42, useCase("t1").data)
        assertEquals("t1", repository.lastLikedThreadId)
    }

    @Test
    fun `a network failure is returned as an error instead of thrown`() = runTest {
        repository.likeThreadResult = DataResult.failure(DataError.NoConnection)
        assertEquals(DataError.NoConnection, useCase("t1").error)
    }
}
