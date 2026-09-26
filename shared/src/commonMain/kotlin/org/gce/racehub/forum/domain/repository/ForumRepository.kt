package org.gce.racehub.forum.domain.repository

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.model.ThreadComment
import org.gce.racehub.forum.domain.model.ThreadSort

/**
 * Contract for the forum. Implementations are the error boundary: failures
 * come back as [DataResult.Failure], never as exceptions.
 */
interface ForumRepository {

    /**
     * Threads matching the filters, not cached.
     *
     * @param sort Thread order. `null` lets the server decide.
     * @param category Restricts to one category. `null` returns all categories.
     * @param userId Caller's user id; used to populate each thread's `bookmarked` flag.
     */
    suspend fun getThreads(
        sort: ThreadSort? = null,
        category: String? = null,
        userId: String? = null
    ): DataResult<List<Thread>>

    /** Creates a thread on behalf of [userId]; returns it with the server's id and timestamp. */
    suspend fun createThread(userId: String, title: String, category: String, content: String): DataResult<Thread>

    /** Posts a comment on [threadId] on behalf of [userId]. */
    suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment>

    /** Toggles the like on thread [id]; returns the updated like count. */
    suspend fun likeThread(id: String): DataResult<Int>
}
