package org.gce.racehub.home

/**
 * Every user interaction on the Home shell expressed as an explicit event.
 * Tab content interactions are dispatched to the per-tab feature ViewModels.
 */
sealed class HomeIntent {

    /** User tapped the Race, Forum or Profile tab. */
    data class TabSelected(val tab: HomeTab) : HomeIntent()
}
