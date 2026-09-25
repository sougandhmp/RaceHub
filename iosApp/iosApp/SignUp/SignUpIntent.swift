import Foundation

/// Every user interaction on the Sign-Up screen expressed as an explicit event.
///
/// The View dispatches intents; `SignUpViewModel` is the sole handler.
/// This one-way data flow makes state changes auditable and testable.
enum SignUpIntent {
    case usernameChanged(String)
    case emailChanged(String)
    case passwordChanged(String)
    case confirmPasswordChanged(String)
    case countryChanged(String)
    case togglePasswordVisibility
    case toggleConfirmPasswordVisibility
    case signUp
}
