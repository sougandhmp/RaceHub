import Foundation

/// Immutable snapshot of everything the Login screen needs to render itself.
///
/// A new value is published by `LoginViewModel` for every state change;
/// the View never mutates this struct directly.
struct LoginState {

    /// Current text in the email input field.
    var email: String = ""

    /// Current text in the password input field.
    var password: String = ""

    /// When `true` the password field renders plain text instead of dots.
    var isPasswordVisible: Bool = false

    /// `true` while the login request is in-flight; drives the spinner
    /// and disables the submit button.
    var isLoading: Bool = false

    /// Non-`nil` when a validation or server error should be shown to the user.
    /// Cleared by sending `LoginIntent.dismissError`.
    var errorMessage: String? = nil
}
