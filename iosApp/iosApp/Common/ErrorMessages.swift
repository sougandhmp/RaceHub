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

extension AuthFailure {
    /// Localized text for an auth failure; a server refusal shows the server's own message.
    /// Switches on the Kotlin enum name (Kotlin/Native lowercases multi-word entries in Swift).
    var userMessage: String {
        switch reason.name {
        case "EmailRequired": return String(localized: "Enter your email.")
        case "EmailAndPasswordRequired": return String(localized: "Enter your email and password.")
        case "InvalidEmail": return String(localized: "Enter a valid email address.")
        case "PasswordTooShort": return String(localized: "Password must be at least 6 characters.")
        case "PasswordsDoNotMatch": return String(localized: "Passwords don't match.")
        case "UsernameRequired": return String(localized: "Choose a username.")
        case "CountryRequired": return String(localized: "Select your country.")
        case "CodeRequired": return String(localized: "Enter the code we emailed you.")
        case "Rejected": return serverMessage ?? String(localized: "The request was declined. Please check your details.")
        case "Network": return DataError.network.userMessage
        case "Server": return DataError.server.userMessage
        default: return DataError.unknown.userMessage
        }
    }
}
