import Foundation

/// One-time side effects produced by `RaceViewModel` — typically navigation
/// events that must not be replayed when the View re-renders.
enum RaceEffect {

    /// Navigate to the detail view for a specific race.
    case navigateToRaceDetail(raceId: String)

    /// Navigate to the detailed profile of a driver.
    case navigateToDriverDetail(driverName: String)
}
