package org.gce.racehub.home

/**
 * Every user interaction on the Home screen expressed as an explicit event.
 *
 * The View dispatches intents; [HomeViewModel] is the sole handler.
 * This one-way data flow makes state changes auditable and testable.
 */
sealed class HomeIntent {

    /**
     * User tapped the Schedule or Standings tab.
     * @param tab The tab that was selected.
     */
    data class TabSelected(val tab: HomeTab) : HomeIntent()

    /** User triggered a pull-to-refresh or tapped a retry button. */
    data object Refresh : HomeIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : HomeIntent()
}
