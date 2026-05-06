import Foundation
import Combine
import Shared

/// ViewModel for the Forum tab following the MVI pattern.
@MainActor
final class ForumViewModel: ObservableObject {

    @Published private(set) var state = ForumState()

    private let effectSubject = PassthroughSubject<ForumEffect, Never>()
    var effectPublisher: AnyPublisher<ForumEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let repository: HomeRepository = RaceDependencyProvider.companion.shared.homeRepository

    init() {
        loadThreads()
    }

    func send(_ intent: ForumIntent) {
        switch intent {
        case .refresh:
            loadThreads()
        case .dismissError:
            state.errorMessage = nil
        }
    }

    func loadThreads() {
        Task { await performLoad() }
    }

    func refresh() async {
        await performLoad()
    }

    private func performLoad() async {
        state.isLoading = true
        state.errorMessage = nil

        do {
            let threads = try await repository.getThreads(sort: "latest", category: nil, userId: nil)
            state.isLoading = false
            state.threads = threads
        } catch {
            state.isLoading = false
            state.errorMessage = error.localizedDescription
        }
    }
}
