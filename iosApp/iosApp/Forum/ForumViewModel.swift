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

    private let getThreads: GetThreadsUseCase = RaceDependencyProvider.companion.shared.createGetThreadsUseCase()

    init() {
        loadThreads()
    }

    func send(_ intent: ForumIntent) {
        switch intent {
        case .refresh:
            loadThreads()
        case .dismissError:
            state.errorMessage = nil
        case .selectSort(let sort):
            state.selectedSort = sort
            loadThreads()
        case .selectCategory(let category):
            state.selectedCategory = category
            loadThreads()
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
            let result = try await getThreads.invoke(
                sort: state.selectedSort,
                category: state.selectedCategory,
                userId: nil
            )
            state.isLoading = false
            // On failure, keep the threads already on screen and show why.
            if let threads = result.data as? [Shared.Thread] {
                state.threads = threads
            }
            state.errorMessage = result.error?.message
        } catch {
            state.isLoading = false
            state.errorMessage = error.localizedDescription
        }
    }
}
