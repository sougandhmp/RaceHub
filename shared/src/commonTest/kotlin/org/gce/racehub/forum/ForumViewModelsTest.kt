package org.gce.racehub.forum

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.gce.racehub.auth.domain.model.User
import org.gce.racehub.auth.domain.session.UserSession
import org.gce.racehub.core.domain.DataError
import org.gce.racehub.fake.FakeHomeRepository
import org.gce.racehub.fake.FakeSessionStorage
import org.gce.racehub.fake.fakeThread
import org.gce.racehub.forum.presentation.CreateThreadEffect
import org.gce.racehub.forum.presentation.CreateThreadIntent
import org.gce.racehub.forum.presentation.CreateThreadState
import org.gce.racehub.forum.presentation.CreateThreadViewModel
import org.gce.racehub.forum.presentation.ForumEffect
import org.gce.racehub.forum.presentation.ForumIntent
import org.gce.racehub.forum.presentation.ForumViewModel
import org.gce.racehub.forum.presentation.ThreadDetailEffect
import org.gce.racehub.forum.presentation.ThreadDetailIntent
import org.gce.racehub.forum.presentation.ThreadDetailViewModel
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.model.ThreadSort
import org.gce.racehub.race.domain.usecase.AddCommentUseCase
import org.gce.racehub.race.domain.usecase.CreateThreadUseCase
import org.gce.racehub.race.domain.usecase.GetThreadsUseCase
import org.gce.racehub.race.domain.usecase.LikeThreadUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** Orchestration and effects of the forum ViewModels; state transitions are in [ForumReducersTest]. */
@OptIn(ExperimentalCoroutinesApi::class)
class ForumViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeHomeRepository()
    private val signedIn = UserSession(FakeSessionStorage(User(id = "u1", email = "a@b.c", name = "Ann", username = "ann")))
    private val signedOut = UserSession(FakeSessionStorage())

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // ── Forum list ──────────────────────────────────────────────────────────

    @Test
    fun `forum loads with the signed-in user id so bookmarks resolve`() = runTest(dispatcher) {
        repo.threadsResult = listOf(fakeThread())
        val vm = ForumViewModel(GetThreadsUseCase(repo), signedIn)
        advanceUntilIdle()
        assertEquals(Triple(ThreadSort.Latest, null, "u1"), repo.lastThreadsArgs)
        assertEquals(1, vm.state.value.threads.size)
    }

    @Test
    fun `a slow response for an old filter never overwrites the new one`() = runTest(dispatcher) {
        val vm = ForumViewModel(GetThreadsUseCase(repo), signedIn)
        advanceUntilIdle()
        repo.threadsDelayMs = mapOf(ThreadSort.Popular to 5_000L, ThreadSort.MostCommented to 100L)

        repo.threadsResult = listOf(fakeThread().copy(id = "popular"))
        vm.onIntent(ForumIntent.SelectSort(ThreadSort.Popular))
        dispatcher.scheduler.advanceTimeBy(50)
        repo.threadsResult = listOf(fakeThread().copy(id = "commented"))
        vm.onIntent(ForumIntent.SelectSort(ThreadSort.MostCommented))
        advanceUntilIdle()

        assertEquals(ThreadSort.MostCommented, vm.state.value.selectedSort)
        assertEquals(listOf("commented"), vm.state.value.threads.map { it.id })
    }

    @Test
    fun `forum load failure emits an error effect`() = runTest(dispatcher) {
        repo.threadsError = DataError.Server
        val vm = ForumViewModel(GetThreadsUseCase(repo), signedIn)
        advanceUntilIdle()
        assertEquals(ForumEffect.ShowLoadError(DataError.Server), vm.effects.first())
    }

    // ── Thread detail ───────────────────────────────────────────────────────

    private fun detailVm(session: UserSession = signedIn) =
        ThreadDetailViewModel(AddCommentUseCase(repo), LikeThreadUseCase(repo), session)

    @Test
    fun `like is confirmed with the server count`() = runTest(dispatcher) {
        repo.likeThreadResult = 10
        val vm = detailVm()
        vm.onIntent(ThreadDetailIntent.Open("t1", likes = 4))
        vm.onIntent(ThreadDetailIntent.ToggleLike)
        advanceUntilIdle()
        assertEquals("t1", repo.lastLikedThreadId)
        assertEquals(10, vm.state.value.likes)
        assertFalse(vm.state.value.isLiking)
    }

    @Test
    fun `failed like reverts and emits an effect`() = runTest(dispatcher) {
        repo.likeThreadError = DataError.Network
        val vm = detailVm()
        vm.onIntent(ThreadDetailIntent.Open("t1", likes = 4))
        vm.onIntent(ThreadDetailIntent.ToggleLike)
        advanceUntilIdle()
        assertEquals(4, vm.state.value.likes)
        assertFalse(vm.state.value.isLiked)
        assertEquals(ThreadDetailEffect.LikeFailed(DataError.Network), vm.effects.first())
    }

    @Test
    fun `comment is posted with the signed-in author`() = runTest(dispatcher) {
        val vm = detailVm()
        vm.onIntent(ThreadDetailIntent.Open("t1", likes = 0))
        vm.onIntent(ThreadDetailIntent.CommentInputChanged("  Great race  "))
        vm.onIntent(ThreadDetailIntent.SubmitComment)
        advanceUntilIdle()
        assertEquals(Triple("u1", "t1", "Great race"), repo.lastAddCommentArgs)
        assertEquals(listOf(ThreadComment("Great race", "ann")), vm.state.value.postedComments)
    }

    @Test
    fun `commenting signed out emits NotSignedIn and calls nothing`() = runTest(dispatcher) {
        val vm = detailVm(signedOut)
        vm.onIntent(ThreadDetailIntent.Open("t1", likes = 0))
        vm.onIntent(ThreadDetailIntent.CommentInputChanged("hi"))
        vm.onIntent(ThreadDetailIntent.SubmitComment)
        advanceUntilIdle()
        assertEquals(ThreadDetailEffect.NotSignedIn, vm.effects.first())
        assertEquals(null, repo.lastAddCommentArgs)
    }

    // ── Create thread ───────────────────────────────────────────────────────

    @Test
    fun `successful post emits ThreadCreated and resets the form`() = runTest(dispatcher) {
        val vm = CreateThreadViewModel(CreateThreadUseCase(repo), signedIn)
        vm.onIntent(CreateThreadIntent.TitleChanged("Title"))
        vm.onIntent(CreateThreadIntent.ContentChanged("Body"))
        vm.onIntent(CreateThreadIntent.Submit)
        advanceUntilIdle()
        assertEquals(CreateThreadEffect.ThreadCreated, vm.effects.first())
        assertEquals(CreateThreadState(), vm.state.value)
        assertEquals("Title", repo.lastCreateThreadArgs?.title)
    }

    @Test
    fun `failed post keeps the form and emits SubmitFailed`() = runTest(dispatcher) {
        repo.createThreadError = DataError.Server
        val vm = CreateThreadViewModel(CreateThreadUseCase(repo), signedIn)
        vm.onIntent(CreateThreadIntent.TitleChanged("Title"))
        vm.onIntent(CreateThreadIntent.ContentChanged("Body"))
        vm.onIntent(CreateThreadIntent.Submit)
        advanceUntilIdle()
        assertEquals(CreateThreadEffect.SubmitFailed(DataError.Server), vm.effects.first())
        assertEquals("Title", vm.state.value.title)
        assertFalse(vm.state.value.isSubmitting)
    }
}
