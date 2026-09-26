package org.gce.racehub.forum.presentation

/** Pure: the only place [ThreadDetailState] changes. */
internal object ThreadDetailReducer {
    fun reduce(state: ThreadDetailState, mutation: ThreadDetailMutation): ThreadDetailState = when (mutation) {
        is ThreadDetailMutation.Opened ->
            if (mutation.threadId == state.threadId) state
            else ThreadDetailState(threadId = mutation.threadId, likes = mutation.likes)

        is ThreadDetailMutation.InputChanged -> state.copy(commentInput = mutation.text)

        ThreadDetailMutation.LikeToggled -> {
            val liked = !state.isLiked
            state.copy(
                isLiked = liked,
                likes = if (liked) state.likes + 1 else (state.likes - 1).coerceAtLeast(0),
                isLiking = true
            )
        }

        is ThreadDetailMutation.LikeConfirmed -> state.copy(likes = mutation.likes, isLiking = false)

        is ThreadDetailMutation.LikeReverted ->
            state.copy(isLiked = mutation.isLiked, likes = mutation.likes, isLiking = false)

        ThreadDetailMutation.CommentSubmitted -> state.copy(isSubmitting = true)

        is ThreadDetailMutation.CommentPosted -> state.copy(
            isSubmitting = false,
            commentInput = "",
            postedComments = state.postedComments + mutation.comment
        )

        // Keep the typed text so the user can retry.
        ThreadDetailMutation.CommentFailed -> state.copy(isSubmitting = false)
    }
}
