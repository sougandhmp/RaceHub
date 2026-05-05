import Foundation
import Combine
import Shared

/// ViewModel for the Home screen following the MVI pattern.
///
/// - Exposes `state` as a `@Published` property the View observes.
/// - Accepts user actions via `send(_:)` and processes them into state mutations.
/// - Emits one-time navigation events through `effectPublisher`.
///
/// Data is loaded eagerly in `init` so the screen is populated immediately
/// when first displayed after login.
///
/// **Swift / Kotlin interop note:** Kotlin's `List<T>` does not bridge
/// directly to a Swift `[T]` array in Kotlin/Native. This ViewModel therefore
/// uses the index-based accessors (`getRaceCount()`, `getRace(index:)`, etc.)
/// defined on `HomeRepositoryImpl` to copy items into native Swift arrays.
@MainActor
final class HomeViewModel: ObservableObject {

    /// Observable UI state. Bound to the View via `@StateObject`.
    @Published private(set) var state = HomeState()

    // PassthroughSubject is used so each effect fires exactly once and is not
    // replayed to late subscribers (unlike CurrentValueSubject).
    private let effectSubject = PassthroughSubject<HomeEffect, Never>()

    /// Stream of one-time side effects (navigation, etc.) for the View to react to.
    var effectPublisher: AnyPublisher<HomeEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    /// Direct repository access; index-based methods are used instead of
    /// list-returning methods to avoid Kotlin collection bridging issues.
    private let repository = HomeRepositoryImpl()

    init() {
        loadData()
    }

    /// Entry point for all View interactions.
    /// Maps each `HomeIntent` to a state mutation or a command.
    func send(_ intent: HomeIntent) {
        switch intent {
        case .tabSelected(let tab):
            state.selectedTab = tab

        case .refresh:
            loadData()
        }
    }

    /// Fetches race schedule and driver standings from the shared repository,
    /// converting Kotlin collections to Swift arrays via index-based access.
    private func loadData() {
        state.isLoading = true
        state.errorMessage = nil

        Task {
            // Build Swift [Race] array from the Kotlin repository one item at a time.
            // Kotlin `Int` maps to `Int32` in Swift/Obj-C; both conversions are needed.
            var races: [Race] = []
            let raceCount = Int(repository.getRaceCount())
            for i in 0..<raceCount {
                races.append(repository.getRace(index: Int32(i)))
            }

            // Build Swift [DriverStanding] array the same way.
            var driverStandings: [DriverStanding] = []
            let driverCount = Int(repository.getStandingCount())
            for i in 0..<driverCount {
                driverStandings.append(repository.getStanding(index: Int32(i)))
            }

            // Fetch additional data
            let constructorStandings = try await repository.getConstructorStandings()
            let trendingThreads = try await repository.getTrendingThreads()

            state.isLoading = false
            state.raceSchedule = races
            state.driverStandings = driverStandings
            state.constructorStandings = constructorStandings
            state.trendingThreads = trendingThreads
        }
    }
}
