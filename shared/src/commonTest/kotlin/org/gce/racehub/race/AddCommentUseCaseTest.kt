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

class AddCommentUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = AddCommentUseCase(repository)

    @Test
    fun `blank userId is rejected without calling the repository`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "", threadId = "t1", content = "Hello").error)
        assertNull(repository.lastAddCommentArgs)
    }

    @Test
    fun `blank threadId is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "u1", threadId = "", content = "Hello").error)
    }

    @Test
    fun `blank content is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "u1", threadId = "t1", content = "").error)
    }

    @Test
    fun `whitespace-only content is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(useCase(userId = "u1", threadId = "t1", content = "   ").error)
    }

    @Test
    fun `content is trimmed before being passed to repository`() = runTest {
        useCase(userId = "u1", threadId = "t1", content = "  Hello world  ")
        assertEquals("Hello world", repository.lastAddCommentArgs?.third)
    }

    @Test
    fun `successful call returns repository result`() = runTest {
        val result = useCase(userId = "u1", threadId = "t1", content = "Great post!")
        assertEquals(ThreadComment("comment", "user"), result.data)
    }
}
