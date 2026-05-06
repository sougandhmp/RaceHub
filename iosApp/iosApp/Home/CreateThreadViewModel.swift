import Foundation
import Combine
import Shared

/// ViewModel for the Create Thread screen following the MVI pattern.
///
/// - Exposes `state` as a `@Published` property the View observes.
/// - Accepts user actions via `send(_:)` and processes them into state mutations.
/// - Emits one-time `CreateThreadEffect` events through `effectPublisher`.
@MainActor
final class CreateThreadViewModel: ObservableObject {

    @Published private(set) var state = CreateThreadState()

    private let effectSubject = PassthroughSubject<CreateThreadEffect, Never>()
    var effectPublisher: AnyPublisher<CreateThreadEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let createThreadUseCase: CreateThreadUseCase
    private let userSession: UserSession

    init() {
        let provider = RaceDependencyProvider.companion.shared
        createThreadUseCase = provider.createCreateThreadUseCase()
        userSession = provider.userSession
    }

    func send(_ intent: CreateThreadIntent) {
        switch intent {
        case .titleChanged(let title):
            state.title = title

        case .categoryChanged(let category):
            state.category = category

        case .contentChanged(let content):
            state.content = content

        case .submit:
            submit()

        case .dismissError:
            state.errorMessage = nil
        }
    }

    private func submit() {
        guard state.canSubmit else { return }

        guard let userId = userSession.userId, !userId.isEmpty else {
            state.errorMessage = "You must be signed in to post."
            return
        }

        state.isSubmitting = true
        state.errorMessage = nil

        let titleSnapshot = state.title
        let categorySnapshot = state.category
        let contentSnapshot = state.content

        Task {
            do {
                _ = try await createThreadUseCase.invoke(
                    userId: userId,
                    title: titleSnapshot,
                    category: categorySnapshot,
                    content: contentSnapshot
                )
                state.isSubmitting = false
                effectSubject.send(.threadCreated)
            } catch {
                state.isSubmitting = false
                state.errorMessage = error.localizedDescription
            }
        }
    }
}
