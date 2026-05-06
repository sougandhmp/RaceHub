import Foundation
import Combine
import Shared

@MainActor
final class ThreadDetailViewModel: ObservableObject {

    @Published private(set) var state = ThreadDetailState()

    private let addCommentUseCase: AddCommentUseCase
    private let userSession: UserSession

    init() {
        let provider = RaceDependencyProvider.companion.shared
        addCommentUseCase = provider.createAddCommentUseCase()
        userSession = provider.userSession
    }

    func send(_ intent: ThreadDetailIntent) {
        switch intent {
        case .commentInputChanged(let text):
            state.commentInput = text

        case .submitComment(let threadId):
            submitComment(threadId: threadId)

        case .dismissError:
            state.errorMessage = nil
        }
    }

    private func submitComment(threadId: String) {
        guard state.canSubmit else { return }

        guard let userId = userSession.userId, !userId.isEmpty else {
            state.errorMessage = "You must be signed in to comment."
            return
        }

        let text = state.commentInput.trimmingCharacters(in: .whitespacesAndNewlines)
        let authorUsername = userSession.username ?? "you"

        state.isSubmitting = true
        state.errorMessage = nil

        Task {
            do {
                _ = try await addCommentUseCase.invoke(
                    userId: userId,
                    threadId: threadId,
                    content: text
                )
                state.isSubmitting = false
                state.commentInput = ""
                state.postedComments.append(
                    ThreadComment(content: text, authorUsername: authorUsername)
                )
            } catch {
                state.isSubmitting = false
                state.errorMessage = error.localizedDescription
            }
        }
    }
}
