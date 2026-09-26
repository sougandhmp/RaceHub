package org.gce.racehub.forum

import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.fake.FakeForumRepository
import org.gce.racehub.forum.domain.usecase.CreateThreadUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateThreadUseCaseTest {

    private val repository = FakeForumRepository()
    private val useCase = CreateThreadUseCase(repository)

    private suspend fun invoke(
        userId: String = "u1",
        title: String = "My thread",
        category: String = "Race Talk",
        content: String = "Some content"
    ) = useCase(userId, title, category, content)

    @Test
    fun `blank userId throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> { invoke(userId = "") }
    }

    @Test
    fun `blank title throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> { invoke(title = "") }
    }

    @Test
    fun `blank content throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> { invoke(content = "") }
    }

    @Test
    fun `blank category defaults to General Discussion`() = runTest {
        invoke(category = "")
        assertEquals("General Discussion", repository.lastCreateThreadArgs?.category)
    }

    @Test
    fun `whitespace category defaults to General Discussion`() = runTest {
        invoke(category = "   ")
        assertEquals("General Discussion", repository.lastCreateThreadArgs?.category)
    }

    @Test
    fun `title is trimmed before passing to repository`() = runTest {
        invoke(title = "  My Thread  ")
        assertEquals("My Thread", repository.lastCreateThreadArgs?.title)
    }

    @Test
    fun `content is trimmed before passing to repository`() = runTest {
        invoke(content = "  Some content  ")
        assertEquals("Some content", repository.lastCreateThreadArgs?.content)
    }

    @Test
    fun `non-blank category is preserved`() = runTest {
        invoke(category = "Race Talk")
        assertEquals("Race Talk", repository.lastCreateThreadArgs?.category)
    }

    @Test
    fun `successful call returns repository result`() = runTest {
        assertEquals(DataResult.Success(repository.createThreadResult), invoke())
    }
}
