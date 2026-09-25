import Foundation

/// Every user interaction targeted at the Forum tab expressed as an explicit event.
/// The View dispatches intents; `ForumViewModel` is the sole handler.
enum ForumIntent {

    /// User triggered a pull-to-refresh, or a thread was just created.
    case refresh

    /// Clears the active error message without changing any other state.
    case dismissError

    /// User selected a sort tab (e.g. "latest", "top", "commented").
    case selectSort(String)

    /// User selected a category filter; nil means all categories.
    case selectCategory(String?)
}
