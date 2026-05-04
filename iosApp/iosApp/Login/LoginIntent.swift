import Foundation

/// Every user interaction on the Login screen expressed as an explicit event.
///
/// The View dispatches intents; `LoginViewModel` is the sole handler.
/// This one-way data flow makes state changes auditable and testable.
enum LoginIntent {

    /// Fired on every keystroke in the email field.
    case emailChanged(String)

    /// Fired on every keystroke in the password field.
    case passwordChanged(String)

    /// Toggles the password field between masked (dots) and plain text.
    case togglePasswordVisibility

    /// Submits the current credentials to the login use case.
    case login

    /// Clears the active error message without changing any other state.
    case dismissError
}
