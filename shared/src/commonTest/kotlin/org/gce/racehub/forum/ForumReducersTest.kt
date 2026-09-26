package org.gce.racehub.forum

import org.gce.racehub.fake.fakeThread
import org.gce.racehub.forum.presentation.CreateThreadMutation
import org.gce.racehub.forum.presentation.CreateThreadReducer
import org.gce.racehub.forum.presentation.CreateThreadState
import org.gce.racehub.forum.presentation.ForumMutation
import org.gce.racehub.forum.presentation.ForumReducer
import org.gce.racehub.forum.presentation.ForumState
import org.gce.racehub.forum.presentation.ThreadDetailMutation
import org.gce.racehub.forum.presentation.ThreadDetailReducer
import org.gce.racehub.forum.presentation.ThreadDetailState
import org.gce.racehub.race.domain.model.ForumCategories
import org.gce.racehub.race.domain.model.ThreadComment
import org.gce.racehub.race.domain.model.ThreadSort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The three forum reducers are pure: plain (state, mutation) -> state assertions. */
class ForumReducersTest {

    // ── Forum list ──────────────────────────────────────────────────────────

    @Test
    fun `forum load failure keeps the threads already shown`() {
        val shown = ForumReducer.reduce(ForumState(), ForumMutation.Loaded(listOf(fakeThread())))
        val state = ForumReducer.reduce(ForumReducer.reduce(shown, ForumMutation.LoadStarted), ForumMutation.LoadFailed)
        assertFalse(state.isLoading)
        assertEquals(shown.threads, state.threads)
    }

    @Test
    fun `forum filters change together`() {
        val state = ForumReducer.reduce(ForumState(), ForumMutation.FiltersChanged(ThreadSort.Popular, "Race Weekends"))
        assertEquals(ThreadSort.Popular, state.selectedSort)
        assertEquals("Race Weekends", state.selectedCategory)
    }

    // ── Thread detail ───────────────────────────────────────────────────────

    @Test
    fun `opening a different thread resets everything`() {
        val used = ThreadDetailState(threadId = "a", likes = 9, isLiked = true, commentInput = "draft",
            postedComments = listOf(ThreadComment("hi", "me")))
        assertEquals(ThreadDetailState(threadId = "b", likes = 3), ThreadDetailReducer.reduce(used, ThreadDetailMutation.Opened("b", 3)))
    }

    @Test
    fun `reopening the same thread keeps its state`() {
        val used = ThreadDetailState(threadId = "a", likes = 9, commentInput = "draft")
        assertEquals(used, ThreadDetailReducer.reduce(used, ThreadDetailMutation.Opened("a", 4)))
    }

    @Test
    fun `optimistic like adds one and unlike never goes below zero`() {
        val liked = ThreadDetailReducer.reduce(ThreadDetailState(threadId = "a", likes = 2), ThreadDetailMutation.LikeToggled)
        assertTrue(liked.isLiked); assertEquals(3, liked.likes); assertTrue(liked.isLiking)

        val unliked = ThreadDetailReducer.reduce(ThreadDetailState(threadId = "a", likes = 0, isLiked = true), ThreadDetailMutation.LikeToggled)
        assertFalse(unliked.isLiked); assertEquals(0, unliked.likes)
    }

    @Test
    fun `reverted like restores the previous values`() {
        val optimistic = ThreadDetailState(threadId = "a", likes = 3, isLiked = true, isLiking = true)
        val state = ThreadDetailReducer.reduce(optimistic, ThreadDetailMutation.LikeReverted(isLiked = false, likes = 2))
        assertEquals(ThreadDetailState(threadId = "a", likes = 2, isLiked = false, isLiking = false), state)
    }

    @Test
    fun `posted comment is appended and input cleared while failure keeps the draft`() {
        val submitting = ThreadDetailState(threadId = "a", commentInput = "Nice", isSubmitting = true)
        val posted = ThreadDetailReducer.reduce(submitting, ThreadDetailMutation.CommentPosted(ThreadComment("Nice", "me")))
        assertEquals("", posted.commentInput); assertEquals(listOf(ThreadComment("Nice", "me")), posted.postedComments)

        val failed = ThreadDetailReducer.reduce(submitting, ThreadDetailMutation.CommentFailed)
        assertEquals("Nice", failed.commentInput); assertFalse(failed.isSubmitting)
    }

    @Test
    fun `comment can only be submitted when not blank and idle`() {
        assertFalse(ThreadDetailState(commentInput = "   ").canSubmit)
        assertFalse(ThreadDetailState(commentInput = "hi", isSubmitting = true).canSubmit)
        assertTrue(ThreadDetailState(commentInput = "hi").canSubmit)
    }

    // ── Create thread ───────────────────────────────────────────────────────

    @Test
    fun `successful post resets the form while failure keeps it`() {
        val filled = CreateThreadState(title = "T", category = "Race Weekends", content = "C", isSubmitting = true)
        assertEquals(CreateThreadState(), CreateThreadReducer.reduce(filled, CreateThreadMutation.Submitted))
        assertEquals(filled.copy(isSubmitting = false), CreateThreadReducer.reduce(filled, CreateThreadMutation.SubmitFailed))
    }

    @Test
    fun `new form defaults to the default category and needs title and content`() {
        assertEquals(ForumCategories.DEFAULT, CreateThreadState().category)
        assertFalse(CreateThreadState(title = "T").canSubmit)
        assertTrue(CreateThreadState(title = "T", content = "C").canSubmit)
    }
}
