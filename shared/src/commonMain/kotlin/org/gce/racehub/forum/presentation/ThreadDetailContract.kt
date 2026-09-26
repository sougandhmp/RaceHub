package org.gce.racehub.forum.presentation

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.forum.domain.model.ThreadComment

// MVI contract for a thread's detail screen (likes and comments).

/** Immutable snapshot of one thread's detail screen. Only [ThreadDetailReducer] produces new values. */
data class ThreadDetailState(
    /** Thread on screen; set by [ThreadDetailIntent.Open]. */
    val threadId: String = "",
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val isLiking: Boolean = false,
    val commentInput: String = "",
    val isSubmitting: Boolean = false,
    /** Comments posted in this session, shown after the thread's own comments. */
    val postedComments: List<ThreadComment> = emptyList()
) {
    val canSubmit: Boolean get() = !isSubmitting && commentInput.isNotBlank()
}

sealed class ThreadDetailIntent {
    /** The screen opened [threadId]; resets any state left from a previous thread. */
    data class Open(val threadId: String, val likes: Int) : ThreadDetailIntent()
    data class CommentInputChanged(val text: String) : ThreadDetailIntent()
    data object SubmitComment : ThreadDetailIntent()
    data object ToggleLike : ThreadDetailIntent()
}

sealed class ThreadDetailEffect {
    data object NotSignedIn : ThreadDetailEffect()
    data class CommentFailed(val error: DataError) : ThreadDetailEffect()
    data class LikeFailed(val error: DataError) : ThreadDetailEffect()
}

internal sealed interface ThreadDetailMutation {
    data class Opened(val threadId: String, val likes: Int) : ThreadDetailMutation
    data class InputChanged(val text: String) : ThreadDetailMutation
    /** Optimistic like/unlike, applied before the server answers. */
    data object LikeToggled : ThreadDetailMutation
    data class LikeConfirmed(val likes: Int) : ThreadDetailMutation
    /** Server rejected the like: restore the state from before [LikeToggled]. */
    data class LikeReverted(val isLiked: Boolean, val likes: Int) : ThreadDetailMutation
    data object CommentSubmitted : ThreadDetailMutation
    data class CommentPosted(val comment: ThreadComment) : ThreadDetailMutation
    data object CommentFailed : ThreadDetailMutation
}
