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
    fun `blank userId is rejected without calling the repository`() = runTest {
        assertIs<DataError.InvalidInput>(invoke(userId = "").error)
        assertNull(repository.lastCreateThreadArgs)
    }

    @Test
    fun `blank title is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(invoke(title = "").error)
    }

    @Test
    fun `blank content is rejected`() = runTest {
        assertIs<DataError.InvalidInput>(invoke(content = "").error)
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
        val result = invoke()
        assertTrue(result.isSuccess)
        assertEquals(fakeThread(), result.data)
    }

    @Test
    fun `passes a repository failure through`() = runTest {
        repository.createThreadResult = DataResult.failure(DataError.Server("Title already used"))
        assertEquals("Title already used", invoke().error?.message)
    }
}
