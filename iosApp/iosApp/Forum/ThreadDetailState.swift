import Foundation
import Shared

struct ThreadDetailState {
    var commentInput: String = ""
    var isSubmitting: Bool = false
    var errorMessage: String? = nil
    var postedComments: [Shared.ThreadComment] = []

    var canSubmit: Bool {
        !isSubmitting && !commentInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
}
