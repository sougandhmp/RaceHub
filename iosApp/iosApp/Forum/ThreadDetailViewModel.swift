import Foundation
import Combine
import Shared

@MainActor
final class ThreadDetailViewModel: ObservableObject {

    @Published private(set) var state = ThreadDetailState()

    private let addCommentUseCase: AddCommentUseCase
    private let likeThreadUseCase: LikeThreadUseCase
    private let userSession: UserSession

    init() {
        let provider = RaceDependencyProvider.companion.shared
        addCommentUseCase = provider.createAddCommentUseCase()
        likeThreadUseCase = provider.createLikeThreadUseCase()
        userSession = provider.userSession
    }

    func initLikes(_ likes: Int32) {
        state.likes = likes
    }

    func send(_ intent: ThreadDetailIntent) {
        switch intent {
        case .commentInputChanged(let text):
            state.commentInput = text

        case .submitComment(let threadId):
            submitComment(threadId: threadId)

        case .toggleLike(let threadId):
            toggleLike(threadId: threadId)

        case .dismissError:
            state.errorMessage = nil
        }
    }

    private func toggleLike(threadId: String) {
        guard !state.isLiking else { return }

        let previousLiked = state.isLiked
        let previousLikes = state.likes
        let optimisticLiked = !previousLiked
        let optimisticLikes = optimisticLiked ? previousLikes + 1 : previousLikes - 1

        state.isLiked = optimisticLiked
        state.likes = optimisticLikes
        state.isLiking = true

        Task {
            do {
                let result = try await likeThreadUseCase.invoke(threadId: threadId)
                if let updatedLikes = result.data {
                    state.likes = updatedLikes.int32Value
                    state.isLiking = false
                } else {
                    // Revert the optimistic update
                    state.isLiked = previousLiked
                    state.likes = previousLikes
                    state.isLiking = false
                    state.errorMessage = result.error?.message
                }
            } catch {
                state.isLiked = previousLiked
                state.likes = previousLikes
                state.isLiking = false
                state.errorMessage = error.localizedDescription
            }
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
                let result = try await addCommentUseCase.invoke(
                    userId: userId,
                    threadId: threadId,
                    content: text
                )
                guard result.isSuccess else {
                    state.isSubmitting = false
                    state.errorMessage = result.error?.message
                    return
                }
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
