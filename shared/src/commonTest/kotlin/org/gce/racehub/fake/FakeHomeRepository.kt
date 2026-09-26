package org.gce.racehub.fake

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.core.domain.DataResult
import org.gce.racehub.race.domain.model.ConstructorStanding
import org.gce.racehub.race.domain.model.DriverStanding
import org.gce.racehub.race.domain.model.Race
import org.gce.racehub.race.domain.model.RaceDetail
import org.gce.racehub.race.domain.model.Thread
import org.gce.racehub.race.domain.model.ThreadAuthor
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.model.ThreadSort
import org.gce.racehub.race.domain.model.TrendingThread
import org.gce.racehub.race.domain.model.UserProfile
import org.gce.racehub.race.domain.repository.HomeRepository

class FakeHomeRepository : HomeRepository {
    var raceSchedule: List<Race> = emptyList()
    var driverStandings: List<DriverStanding> = emptyList()
    var constructorStandings: List<ConstructorStanding> = emptyList()
    var trendingThreads: List<TrendingThread> = emptyList()
    var raceDetailResult: RaceDetail = RaceDetail("GP", "Circuit", null, null, emptyList(), emptyList(), null)
    var threadsResult: List<Thread> = emptyList()
    var createThreadResult: Thread = fakeThread()
    var profileResult: UserProfile = UserProfile("user", "u@e.com", "", 0, 0, emptyList(), emptyList())
    var addCommentResult: ThreadComment = ThreadComment("comment", "user")
    var likeThreadResult: Int = 1
    /** Returned as a Failure by the matching forum call when set. */
    var threadsError: DataError? = null
    var createThreadError: DataError? = null
    var addCommentError: DataError? = null
    var likeThreadError: DataError? = null
    var threadsDelayMs: Map<ThreadSort?, Long> = emptyMap()
    var lastThreadsArgs: Triple<ThreadSort?, String?, String?>? = null

    var lastAddCommentArgs: Triple<String, String, String>? = null
    var lastCreateThreadArgs: CreateThreadArgs? = null
    var lastLikedThreadId: String? = null

    /** Returned as a Failure by [getRaceSchedule] / [getRaceDetail] when set. */
    var raceScheduleError: DataError? = null
    var raceDetailError: DataError? = null
    var raceScheduleCalls = 0
    /** Per-slug detail and artificial latency, overriding [raceDetailResult]. */
    var raceDetailBySlug: Map<String, RaceDetail> = emptyMap()
    var raceDetailDelayMs: Map<String, Long> = emptyMap()

    override suspend fun getRaceSchedule(): DataResult<List<Race>> {
        raceScheduleCalls++
        raceScheduleError?.let { return DataResult.Failure(it) }
        return DataResult.Success(raceSchedule)
    }
    override suspend fun getDriverStandings(): DataResult<List<DriverStanding>> = DataResult.Success(driverStandings)
    override suspend fun getConstructorStandings(): DataResult<List<ConstructorStanding>> = DataResult.Success(constructorStandings)
    override suspend fun getTrendingThreads(): DataResult<List<TrendingThread>> = DataResult.Success(trendingThreads)
    override suspend fun getRaceDetail(slug: String): DataResult<RaceDetail> {
        raceDetailDelayMs[slug]?.let { kotlinx.coroutines.delay(it) }
        raceDetailError?.let { return DataResult.Failure(it) }
        return DataResult.Success(raceDetailBySlug[slug] ?: raceDetailResult)
    }
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

    override suspend fun getMyProfile(userId: String, token: String): UserProfile = profileResult

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

    override fun getRaceCount(): Int = raceSchedule.size
    override fun getRace(index: Int): Race = raceSchedule[index]
    override fun getStandingCount(): Int = driverStandings.size
    override fun getStanding(index: Int): DriverStanding = driverStandings[index]

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
