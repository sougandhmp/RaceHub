package org.gce.racehub.forum

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeForumRepository
import org.gce.racehub.forum.domain.usecase.LikeThreadUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LikeThreadUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = LikeThreadUseCase(repository)

    @Test
    fun `blank threadId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("")
        }
    }

    @Test
    fun `whitespace threadId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("   ")
        }
    }

    @Test
    fun `valid threadId delegates to repository and returns updated count`() = runTest {
        repository.likeThreadResult = 42
        val result = useCase("t1")
        assertEquals(DataResult.Success(42), result)
        assertEquals("t1", repository.lastLikedThreadId)
    }
}
