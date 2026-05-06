package org.gce.racehub.forum

/**
 * One-time side effects produced by [ForumViewModel] — typically navigation
 * events that must not be replayed on recomposition.
 */
sealed class ForumEffect {

    /** Navigate to the detail view for a specific thread. */
    data class NavigateToThreadDetail(val threadId: String) : ForumEffect()
}
