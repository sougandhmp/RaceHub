package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.usecase.LikeThreadUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LikeThreadUseCaseTest {

    private val repository = FakeHomeRepository()
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
        assertEquals(42, result)
        assertEquals("t1", repository.lastLikedThreadId)
    }
}
