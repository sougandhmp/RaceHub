package org.gce.racehub.race.domain.repository

import org.gce.racehub.core.DataResult
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadComment

/** Forum threads, comments and likes. Not cached: always fetched from the network. */
interface ForumRepository {

    suspend fun getThreads(sort: String?, category: String?, userId: String?): DataResult<List<Thread>>

    suspend fun createThread(userId: String, title: String, category: String, content: String): DataResult<Thread>

    suspend fun addComment(userId: String, threadId: String, content: String): DataResult<ThreadComment>

    /** Toggles the like on a thread and returns the updated like count. */
    suspend fun likeThread(threadId: String): DataResult<Int>
}
