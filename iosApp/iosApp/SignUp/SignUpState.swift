import Foundation

/// Immutable snapshot of everything the Sign-Up screen needs to render itself.
///
/// A new value is published by `SignUpViewModel` for every state change;
/// the View never mutates this struct directly.
struct SignUpState {
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""
    var country: String = ""
    var isPasswordVisible: Bool = false
    var isConfirmPasswordVisible: Bool = false
    var isLoading: Bool = false
    var errorMessage: String? = nil
}
