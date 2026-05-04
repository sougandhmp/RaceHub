import Foundation

/// Immutable snapshot of everything the Sign-Up screen needs to render itself.
///
/// A new value is published by `SignUpViewModel` for every state change;
/// the View never mutates this struct directly.
struct SignUpState {

    /// Current text in the full-name input field.
    var name: String = ""

    /// Current text in the email input field.
    var email: String = ""

    /// Current text in the password input field.
    var password: String = ""

    /// Current text in the confirm-password input field.
    var confirmPassword: String = ""

    /// When `true` the password field renders plain text instead of dots.
    var isPasswordVisible: Bool = false

    /// When `true` the confirm-password field renders plain text instead of dots.
    var isConfirmPasswordVisible: Bool = false

    /// `true` while the sign-up request is in-flight; drives the spinner
    /// and disables the submit button.
    var isLoading: Bool = false

    /// Non-`nil` when a validation or server error should be shown to the user.
    var errorMessage: String? = nil
}
