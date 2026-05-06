import Foundation
import Shared

/// Immutable snapshot of the Race tab and the screens that hang off of it
/// (Schedule, Standings). Produced by `RaceViewModel` on every state change;
/// the View never mutates this struct directly.
struct RaceState {

    /// Ordered list of race events for the current season. Empty until loaded.
    var raceSchedule: [Race] = []

    /// Driver championship standings sorted by position.
    var driverStandings: [DriverStanding] = []

    /// Constructor standings sorted by position.
    var constructorStandings: [ConstructorStanding] = []

    /// Trending forum threads displayed inside the Race tab.
    var trendingThreads: [TrendingThread] = []

    /// `true` while data is being fetched; drives the loading indicator.
    var isLoading: Bool = false

    /// Non-`nil` when a data-fetch error should be shown to the user.
    var errorMessage: String? = nil
}
