import Foundation

/// Every user interaction on the Sign-Up screen expressed as an explicit event.
///
/// The View dispatches intents; `SignUpViewModel` is the sole handler.
/// This one-way data flow makes state changes auditable and testable.
enum SignUpIntent {

    /// Fired on every keystroke in the full-name field.
    case nameChanged(String)

    /// Fired on every keystroke in the email field.
    case emailChanged(String)

    /// Fired on every keystroke in the password field.
    case passwordChanged(String)

    /// Fired on every keystroke in the confirm-password field.
    case confirmPasswordChanged(String)

    /// Toggles the password field between masked (dots) and plain text.
    case togglePasswordVisibility

    /// Toggles the confirm-password field between masked (dots) and plain text.
    case toggleConfirmPasswordVisibility

    /// Submits the form to the sign-up use case.
    case signUp
}
