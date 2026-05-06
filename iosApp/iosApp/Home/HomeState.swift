import Foundation

/// Identifies which content tab is active on the Home screen.
/// Toggled via `HomeIntent.tabSelected`.
enum HomeTab {
    case race
    case forum
    case profile
}

/// Immutable snapshot of the Home shell. Owns only the currently selected tab;
/// each tab's content state lives in its own feature ViewModel.
struct HomeState {

    /// Which tab the user is currently viewing.
    var selectedTab: HomeTab = .race
}
