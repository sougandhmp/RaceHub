import Foundation

enum ForgotPasswordIntent {
    case emailChanged(String)
    case otpChanged(String)
    case newPasswordChanged(String)
    case confirmPasswordChanged(String)
    case togglePasswordVisibility
    case requestReset
    case confirmReset
    case dismissError
}
