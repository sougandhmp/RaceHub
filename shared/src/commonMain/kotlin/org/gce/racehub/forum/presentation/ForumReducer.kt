package org.gce.racehub.forum.presentation

/** Pure: the only place [ForumState] changes. */
internal object ForumReducer {
    fun reduce(state: ForumState, mutation: ForumMutation): ForumState = when (mutation) {
        is ForumMutation.FiltersChanged -> state.copy(selectedSort = mutation.sort, selectedCategory = mutation.category)
        ForumMutation.LoadStarted -> state.copy(isLoading = true)
        is ForumMutation.Loaded -> state.copy(isLoading = false, threads = mutation.threads)
        // Keep the threads already shown; the effect tells the user the refresh failed.
        ForumMutation.LoadFailed -> state.copy(isLoading = false)
    }
}
