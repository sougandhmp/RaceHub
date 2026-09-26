import Foundation
import Shared

extension DataError {
    /// Generic user-facing message for a failed data operation.
    var userMessage: String {
        if self == DataError.network {
            return String(localized: "Can't reach the server. Check your connection and try again.")
        } else if self == DataError.server {
            return String(localized: "The server had a problem. Please try again.")
        }
        return String(localized: "Something went wrong. Please try again.")
    }
}
