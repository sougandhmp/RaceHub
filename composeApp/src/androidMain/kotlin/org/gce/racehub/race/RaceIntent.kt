package org.gce.racehub.race

/**
 * Every user interaction targeted at the Race tab expressed as an explicit event.
 * The View dispatches intents; [RaceViewModel] is the sole handler.
 */
sealed class RaceIntent {

    /** User triggered a pull-to-refresh or tapped a retry button. */
    data object Refresh : RaceIntent()

    /** Clears the active error message without changing any other state. */
    data object DismissError : RaceIntent()
}
