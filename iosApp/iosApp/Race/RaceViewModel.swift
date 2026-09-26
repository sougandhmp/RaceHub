import Foundation
import Combine
import Shared

/// ViewModel for the Race tab following the MVI pattern.
///
/// Shows the cached race schedule, standings and trending threads, refreshes
/// them through the shared `RefreshRaceDataUseCase` (the calendar and the
/// dashboard in parallel, one request each), then shows the refreshed cache.
@MainActor
final class RaceViewModel: ObservableObject {

    @Published private(set) var state = RaceState()

    private let effectSubject = PassthroughSubject<RaceEffect, Never>()
    var effectPublisher: AnyPublisher<RaceEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let refreshRaceData: RefreshRaceDataUseCase
    private let getRaceSchedule: GetRaceScheduleUseCase
    private let getDriverStandings: GetDriverStandingsUseCase
    private let getConstructorStandings: GetConstructorStandingsUseCase
    private let getTrendingThreads: GetTrendingThreadsUseCase

    init() {
        let provider = RaceDependencyProvider.companion.shared
        refreshRaceData = provider.createRefreshRaceDataUseCase()
        getRaceSchedule = provider.createGetRaceScheduleUseCase()
        getDriverStandings = provider.createGetDriverStandingsUseCase()
        getConstructorStandings = provider.createGetConstructorStandingsUseCase()
        getTrendingThreads = provider.createGetTrendingThreadsUseCase()
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

    /// Shows the cache straight away, refreshes the calendar and dashboard (in parallel, on the
    /// Kotlin side), then shows the refreshed cache. If the refresh fails, the cached data stays
    /// on screen and `errorMessage` says why.
    private func performLoad() async {
        state.isLoading = true
        state.errorMessage = nil

        do {
            try await showCachedData()
            let refresh = try await refreshRaceData.invoke()
            try await showCachedData()
            state.errorMessage = refresh.error?.message
        } catch {
            state.errorMessage = error.localizedDescription
        }
        state.isLoading = false
    }

    private func showCachedData() async throws {
        state.raceSchedule = try await getRaceSchedule.invoke()
        state.driverStandings = try await getDriverStandings.invoke()
        state.constructorStandings = try await getConstructorStandings.invoke()
        state.trendingThreads = try await getTrendingThreads.invoke()
    }
}
