import Foundation
import Shared

/// Identifies which content tab is active on the Home screen.
/// Toggled via `HomeIntent.tabSelected`.
enum HomeTab {
    /// The race calendar for the current season.
    case schedule

    /// The Drivers' Championship standings table.
    case standings
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

    /// Which tab the user is currently viewing.
    var selectedTab: HomeTab = .schedule

    /// `true` while data is being fetched; drives the loading indicator.
    var isLoading: Bool = false

    /// Non-`nil` when a data-fetch error should be shown to the user.
    var errorMessage: String? = nil
}
