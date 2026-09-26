package org.gce.racehub.fake

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.model.ThreadAuthor
import org.gce.racehub.forum.domain.model.ThreadComment
import org.gce.racehub.forum.domain.model.ThreadSort
import org.gce.racehub.forum.domain.repository.ForumRepository

internal class FakeForumRepository : ForumRepository {
    var threadsResult: List<Thread> = emptyList()
    var createThreadResult: Thread = fakeThread()
    var addCommentResult: ThreadComment = ThreadComment("comment", "user")
    var likeThreadResult: Int = 1

    /** Returned as a Failure by the matching call when set. */
    var threadsError: DataError? = null
    var createThreadError: DataError? = null
    var addCommentError: DataError? = null
    var likeThreadError: DataError? = null
    var threadsDelayMs: Map<ThreadSort?, Long> = emptyMap()

    var lastThreadsArgs: Triple<ThreadSort?, String?, String?>? = null
    var lastAddCommentArgs: Triple<String, String, String>? = null
    var lastCreateThreadArgs: CreateThreadArgs? = null
    var lastLikedThreadId: String? = null

    override suspend fun getThreads(sort: ThreadSort?, category: String?, userId: String?): DataResult<List<Thread>> {
        lastThreadsArgs = Triple(sort, category, userId)
        threadsDelayMs[sort]?.let { kotlinx.coroutines.delay(it) }
        threadsError?.let { return DataResult.Failure(it) }
        return DataResult.Success(threadsResult)
    }

    override suspend fun createThread(userId: String, title: String, category: String, content: String): DataResult<Thread> {
        lastCreateThreadArgs = CreateThreadArgs(userId, title, category, content)
        createThreadError?.let { return DataResult.Failure(it) }
        return DataResult.Success(createThreadResult)
    }

    override suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment> {
        lastAddCommentArgs = Triple(userId, threadId, content)
        addCommentError?.let { return DataResult.Failure(it) }
        return DataResult.Success(addCommentResult)
    }

    override suspend fun likeThread(id: String): DataResult<Int> {
        lastLikedThreadId = id
        likeThreadError?.let { return DataResult.Failure(it) }
        return DataResult.Success(likeThreadResult)
    }

    data class CreateThreadArgs(val userId: String, val title: String, val category: String, val content: String)
}

fun fakeThread() = Thread(
    id = "t1",
    title = "Test Thread",
    category = "General Discussion",
    author = ThreadAuthor("user1", "U"),
    excerpt = null,
    content = "Content",
    createdAt = "2025-01-01",
    likes = 0,
    bookmarked = false,
    comments = emptyList()
)
