import Foundation

/// One-time side effects produced by `ForumViewModel` — typically navigation
/// events that must not be replayed when the View re-renders.
enum ForumEffect {

    /// Navigate to the detail view for a specific thread.
    case navigateToThreadDetail(threadId: String)
}
