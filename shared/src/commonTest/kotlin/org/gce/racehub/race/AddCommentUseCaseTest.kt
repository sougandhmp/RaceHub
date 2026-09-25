package org.gce.racehub.race

import kotlinx.coroutines.test.runTest
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.race.domain.usecase.AddCommentUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddCommentUseCaseTest {

    private val repository = FakeHomeRepository()
    private val useCase = AddCommentUseCase(repository)

    @Test
    fun `blank userId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "", threadId = "t1", content = "Hello")
        }
    }

    @Test
    fun `blank threadId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "u1", threadId = "", content = "Hello")
        }
    }

    @Test
    fun `blank content throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "u1", threadId = "t1", content = "")
        }
    }

    @Test
    fun `whitespace-only content throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(userId = "u1", threadId = "t1", content = "   ")
        }
    }

    @Test
    fun `content is trimmed before being passed to repository`() = runTest {
        useCase(userId = "u1", threadId = "t1", content = "  Hello world  ")
        assertEquals("Hello world", repository.lastAddCommentArgs?.third)
    }

    @Test
    fun `successful call returns repository result`() = runTest {
        val result = useCase(userId = "u1", threadId = "t1", content = "Great post!")
        assertEquals(repository.addCommentResult, result)
    }
}
