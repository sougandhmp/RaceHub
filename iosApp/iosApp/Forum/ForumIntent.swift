import Foundation

/// Every user interaction targeted at the Forum tab expressed as an explicit event.
/// The View dispatches intents; `ForumViewModel` is the sole handler.
enum ForumIntent {

    /// User triggered a pull-to-refresh, or a thread was just created.
    case refresh

    /// Clears the active error message without changing any other state.
    case dismissError
}
