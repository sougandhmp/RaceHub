package org.gce.racehub.home.presentation

// MVI contract for the home shell (bottom tabs). No effects: tab changes have no side effects.

enum class HomeTab { Race, Forum, Profile }

/** Immutable snapshot of the home shell. Only [HomeReducer] produces new values. */
data class HomeState(
    val selectedTab: HomeTab = HomeTab.Race
)

sealed class HomeIntent {
    data class TabSelected(val tab: HomeTab) : HomeIntent()
}

internal sealed interface HomeMutation {
    data class TabSelected(val tab: HomeTab) : HomeMutation
}

/** Pure: the only place [HomeState] changes. */
internal object HomeReducer {
    fun reduce(state: HomeState, mutation: HomeMutation): HomeState = when (mutation) {
        is HomeMutation.TabSelected -> state.copy(selectedTab = mutation.tab)
    }
}
