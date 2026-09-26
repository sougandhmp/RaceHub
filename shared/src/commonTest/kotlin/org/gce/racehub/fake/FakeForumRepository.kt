package org.gce.racehub.fake

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadAuthor
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.repository.ForumRepository

class FakeForumRepository : ForumRepository {
    var threadsResult: DataResult<List<Thread>> = DataResult.success(emptyList())
    var createThreadResult: DataResult<Thread> = DataResult.success(fakeThread())
    var addCommentResult: DataResult<ThreadComment> = DataResult.success(ThreadComment("comment", "user"))
    var likeThreadResult: DataResult<Int> = DataResult.success(1)

    var lastGetThreadsArgs: Triple<String?, String?, String?>? = null
    var lastAddCommentArgs: Triple<String, String, String>? = null
    var lastCreateThreadArgs: CreateThreadArgs? = null
    var lastLikedThreadId: String? = null

    override suspend fun getThreads(sort: String?, category: String?, userId: String?): DataResult<List<Thread>> {
        lastGetThreadsArgs = Triple(sort, category, userId)
        return threadsResult
    }

    override suspend fun createThread(userId: String, title: String, category: String, content: String): DataResult<Thread> {
        lastCreateThreadArgs = CreateThreadArgs(userId, title, category, content)
        return createThreadResult
    }

    override suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment> {
        lastAddCommentArgs = Triple(userId, threadId, content)
        return addCommentResult
    }

    override suspend fun likeThread(threadId: String): DataResult<Int> {
        lastLikedThreadId = threadId
        return likeThreadResult
    }

    data class CreateThreadArgs(
        val userId: String,
        val title: String,
        val category: String,
        val content: String
    )
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
