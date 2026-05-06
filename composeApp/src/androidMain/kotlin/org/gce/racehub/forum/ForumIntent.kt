package org.gce.racehub.forum

/**
 * Every user interaction targeted at the Forum tab expressed as an explicit event.
 * The View dispatches intents; [ForumViewModel] is the sole handler.
 */
sealed class ForumIntent {

    /** User triggered a pull-to-refresh, or a thread was just created. */
    data object Refresh : ForumIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : ForumIntent()
}
