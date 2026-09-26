package org.gce.racehub.forum.presentation

import org.gce.racehub.core.domain.DataError
import org.gce.racehub.forum.domain.model.Thread
import org.gce.racehub.forum.domain.model.ThreadSort

// MVI contract for the Forum tab:
//   View ──ForumIntent──▶ ForumViewModel ──ForumMutation──▶ ForumReducer ──ForumState──▶ View
//                               └──────────ForumEffect (one-off)──────────────────────▶ View

/** Immutable snapshot of the Forum tab. Only [ForumReducer] produces new values. */
data class ForumState(
    val threads: List<Thread> = emptyList(),
    val isLoading: Boolean = false,
    val selectedSort: ThreadSort = ThreadSort.Latest,
    /** Null means all categories. */
    val selectedCategory: String? = null
)

sealed class ForumIntent {
    /** Pull-to-refresh or retry; also after posting a thread. Supersedes a load in flight. */
    data object Refresh : ForumIntent()
    data class SelectSort(val sort: ThreadSort) : ForumIntent()
    data class SelectCategory(val category: String?) : ForumIntent()
}

sealed class ForumEffect {
    data class ShowLoadError(val error: DataError) : ForumEffect()
}

internal sealed interface ForumMutation {
    data class FiltersChanged(val sort: ThreadSort, val category: String?) : ForumMutation
    data object LoadStarted : ForumMutation
    data class Loaded(val threads: List<Thread>) : ForumMutation
    data object LoadFailed : ForumMutation
}
