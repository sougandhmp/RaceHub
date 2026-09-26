package org.gce.racehub.data

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.data.repository.ForumRepositoryNetworkImpl
import org.gce.racehub.forum.domain.model.ThreadSort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The forum repository against a fake API (MockEngine). */
class ForumRepositoryNetworkImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val api = FakeApi(dispatcher)
    private val repo = ForumRepositoryNetworkImpl(api.client, "https://api.test")

    private fun test(block: suspend () -> Unit) = runTest(dispatcher) { block() }

    @Test
    fun `threads are fetched with the API sort value and mapped`() = test {
        api.responses["GetThreads"] = """
            {"data":{"threads":[{"id":"t1","title":"Hello","category":"General Discussion",
              "author":{"username":"ann","avatar":"A"},"content":"Body","createdAt":"2026-05-06","likes":2,
              "comments":[{"content":"Nice","author":{"username":"bob"}}]}]}}
        """.trimIndent()
        val threads = (repo.getThreads(ThreadSort.Popular, null, "u1") as DataResult.Success).data
        assertEquals("ann", threads.single().author.username)
        assertEquals("bob", threads.single().comments.single().authorUsername)
        assertTrue(api.requests.contains("GetThreads"))
    }

    @Test
    fun `like offline is a Network failure`() = test {
        api.failures["UserInteractions"] = FakeApi.Failure.Offline
        assertEquals(DataResult.Failure(DataError.Network), repo.likeThread("t1"))
    }

    @Test
    fun `thread fetch failure is typed`() = test {
        api.failures["GetThreads"] = FakeApi.Failure.ServerError
        assertEquals(DataResult.Failure(DataError.Server), repo.getThreads(ThreadSort.Latest, null, null))
    }
}
