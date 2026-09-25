import Foundation
import Combine
import Shared

/// ViewModel for the Race tab following the MVI pattern.
///
/// Loads race schedule, driver/constructor standings and trending threads
/// from the shared repository.
///
/// **Swift / Kotlin interop note:** Kotlin `List<T>` does not bridge directly
/// to a Swift `[T]` array in Kotlin/Native, so this ViewModel uses the
/// index-based accessors (`getRaceCount()`, `getRace(index:)`, etc.) defined
/// on `HomeRepositoryImpl` to copy items into native Swift arrays.
@MainActor
final class RaceViewModel: ObservableObject {

    @Published private(set) var state = RaceState()

    private let effectSubject = PassthroughSubject<RaceEffect, Never>()
    var effectPublisher: AnyPublisher<RaceEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let repository: HomeRepository = RaceDependencyProvider.companion.shared.homeRepository

    init() {
        loadData()
    }

    func send(_ intent: RaceIntent) {
        switch intent {
        case .refresh:
            loadData()
        case .dismissError:
            state.errorMessage = nil
        }
    }

    func loadData() {
        Task { await performLoad() }
    }

    func refresh() async {
        await performLoad()
    }

    private func performLoad() async {
        state.isLoading = true
        state.errorMessage = nil

        do {
            var races: [Race] = []
            let raceCount = Int(repository.getRaceCount())
            for i in 0..<raceCount {
                races.append(repository.getRace(index: Int32(i)))
            }

            var driverStandings: [DriverStanding] = []
            let driverCount = Int(repository.getStandingCount())
            for i in 0..<driverCount {
                driverStandings.append(repository.getStanding(index: Int32(i)))
            }

            let constructorStandings = try await repository.getConstructorStandings()
            let trendingThreads = try await repository.getTrendingThreads()

            state.isLoading = false
            state.raceSchedule = races
            state.driverStandings = driverStandings
            state.constructorStandings = constructorStandings
            state.trendingThreads = trendingThreads
        } catch {
            state.isLoading = false
            state.errorMessage = error.localizedDescription
        }
    }
}
