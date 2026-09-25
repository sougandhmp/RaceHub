import Foundation

enum ForgotPasswordStep {
    case request
    case confirm
}

struct ForgotPasswordState {
    var step: ForgotPasswordStep = .request
    var email: String = ""
    var otp: String = ""
    var newPassword: String = ""
    var confirmPassword: String = ""
    var isPasswordVisible: Bool = false
    var isLoading: Bool = false
    var errorMessage: String? = nil
}
