import Foundation
import Shared

/// Identifies which content tab is active on the Home screen.
/// Toggled via `HomeIntent.tabSelected`.
enum HomeTab {
    case race
    case forum
    case profile
}

/// Immutable snapshot of everything the Home screen needs to render itself.
///
/// A new value is published by `HomeViewModel` on every state change;
/// the View never mutates this struct directly.
struct HomeState {

    /// Ordered list of race events for the current season. Empty until loaded.
    var raceSchedule: [Race] = []

    /// Championship standings sorted by position. Empty until loaded.
    var driverStandings: [DriverStanding] = []

    /// Constructor standings sorted by position.
    var constructorStandings: [ConstructorStanding] = []

    /// Trending forum threads.
    var trendingThreads: [TrendingThread] = []

    /// Which tab the user is currently viewing.
    var selectedTab: HomeTab = .race

    /// `true` while data is being fetched; drives the loading indicator.
    var isLoading: Bool = false

    /// Non-`nil` when a data-fetch error should be shown to the user.
    var errorMessage: String? = nil
}
