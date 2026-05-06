import Foundation
import Shared

/// Immutable snapshot of the Forum tab. Produced by `ForumViewModel` on
/// every state change; the View never mutates this struct directly.
struct ForumState {

    /// Forum threads shown on the Forum tab. Empty until loaded.
    var threads: [Shared.Thread] = []

    /// `true` while threads are being fetched; drives the loading indicator.
    var isLoading: Bool = false

    /// Non-`nil` when a data-fetch error should be shown to the user.
    var errorMessage: String? = nil
}
