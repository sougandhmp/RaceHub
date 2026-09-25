import Foundation
import Combine
import Shared

/// ViewModel for the Race tab following the MVI pattern.
///
/// Loads race schedule, driver/constructor standings and trending threads
/// from the shared repository. All four are awaited so a cold cache is
/// populated by the network sync before the state is published.
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
            let races = try await repository.getRaceSchedule()
            let driverStandings = try await repository.getDriverStandings()
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
