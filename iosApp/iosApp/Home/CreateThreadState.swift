import Foundation

/// Snapshot of the New Thread form. The View renders from this state and
/// never mutates it directly; all changes go through `CreateThreadIntent`.
struct CreateThreadState {
    var title: String = ""
    var category: String = "General Discussion"
    var content: String = ""
    var isSubmitting: Bool = false
    var errorMessage: String? = nil

    var canSubmit: Bool {
        !isSubmitting
            && !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
