package org.gce.racehub.race.domain.repository

import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.model.ThreadSort
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.model.UserProfile

/**
 * Contract for race, standings, forum and profile data. Implementations are the
 * error boundary: failures come back as [DataResult.Failure], never as exceptions.
 */
interface HomeRepository {

    // ── Race reads: offline-first, errors as values ─────────────────────────
    // Each returns cached rows when a refresh fails, and a Failure only when
    // there is nothing to show.

    /** The full race calendar for the current season. */
    @Throws(Exception::class)
    suspend fun getRaceSchedule(): DataResult<List<Race>>

    /** The current Drivers' Championship standings table. */
    @Throws(Exception::class)
    suspend fun getDriverStandings(): DataResult<List<DriverStanding>>

    /** The current Constructors' Championship standings table. */
    @Throws(Exception::class)
    suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>>

    /** The trending threads from the forum. */
    @Throws(Exception::class)
    suspend fun getTrendingThreads(): DataResult<List<TrendingThread>>

    /** Full detail for the race identified by [slug]. Not cached. */
    @Throws(Exception::class)
    suspend fun getRaceDetail(slug: String): DataResult<RaceDetail>

    /**
     * Returns full forum threads matching the provided filters.
     *
     * @param sort Thread order. `null` lets the server decide.
     * @param category Restricts to a single category. `null` returns all categories.
     * @param userId Caller's user id; used to populate per-thread `bookmarked` flag.
     */
    @Throws(Exception::class)
    suspend fun getThreads(
        sort: ThreadSort? = null,
        category: String? = null,
        userId: String? = null
    ): DataResult<List<Thread>>

    /**
     * Creates a new forum thread on behalf of [userId].
     *
     * @return The newly created thread (id, title, createdAt populated by the server).
     */
    @Throws(Exception::class)
    suspend fun createThread(
        userId: String,
        title: String,
        category: String,
        content: String
    ): DataResult<Thread>

    /**
     * Fetches the full profile for the signed-in user from the backend.
     *
     * @param userId The signed-in user's id.
     * @param token  The user's auth token, sent as `Authorization: Bearer <token>`.
     * @return The user's profile data including post/saved counts and thread lists.
     */
    @Throws(Exception::class)
    suspend fun getMyProfile(userId: String, token: String): DataResult<UserProfile>

    /**
     * Posts a new comment on the thread identified by [threadId] on behalf of [userId].
     *
     * @return The newly posted comment (content populated by the server).
     */
    @Throws(Exception::class)
    suspend fun addComment(
        userId: String,
        threadId: String,
        content: String
    ): DataResult<ThreadComment>

    /**
     * Toggles the like on the thread identified by [id].
     *
     * @return The updated like count from the server.
     */
    @Throws(Exception::class)
    suspend fun likeThread(id: String): DataResult<Int>
}
