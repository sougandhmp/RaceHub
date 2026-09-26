package org.gce.racehub.forum.presentation

/** Pure: the only place [CreateThreadState] changes. */
internal object CreateThreadReducer {
    fun reduce(state: CreateThreadState, mutation: CreateThreadMutation): CreateThreadState = when (mutation) {
        is CreateThreadMutation.TitleChanged -> state.copy(title = mutation.title)
        is CreateThreadMutation.CategoryChanged -> state.copy(category = mutation.category)
        is CreateThreadMutation.ContentChanged -> state.copy(content = mutation.content)
        CreateThreadMutation.SubmitStarted -> state.copy(isSubmitting = true)
        CreateThreadMutation.Submitted -> CreateThreadState()
        // Keep what the user typed so they can retry.
        CreateThreadMutation.SubmitFailed -> state.copy(isSubmitting = false)
    }
}
