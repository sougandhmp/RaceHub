import Foundation

/// One-time side effects produced by `HomeViewModel`.
///
/// Effects represent navigation events that must not be replayed when the
/// view re-renders. They are delivered via a `PassthroughSubject` so they
/// are consumed exactly once by the View.
///
/// Detail-screen navigation is defined here even though the destination
/// screens do not exist yet — the ViewModel can emit these effects and
/// `ContentView` can route them once the detail screens are ready.
enum HomeEffect {

    /// Navigate to the detail view for a specific race.
    case navigateToRaceDetail(raceId: String)

    /// Navigate to the detailed profile of a driver.
    case navigateToDriverDetail(driverName: String)
}
