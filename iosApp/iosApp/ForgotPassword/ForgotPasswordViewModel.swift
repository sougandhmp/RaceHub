import Foundation
import Combine
import Shared

@MainActor
final class ForgotPasswordViewModel: ObservableObject {

    @Published private(set) var state = ForgotPasswordState()

    private let effectSubject = PassthroughSubject<ForgotPasswordEffect, Never>()

    var effectPublisher: AnyPublisher<ForgotPasswordEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let requestPasswordResetUseCase: RequestPasswordResetUseCase
    private let confirmPasswordResetUseCase: ConfirmPasswordResetUseCase

    init() {
        let authProvider = AuthDependencyProvider.companion.shared
        requestPasswordResetUseCase = authProvider.createRequestPasswordResetUseCase()
        confirmPasswordResetUseCase = authProvider.createConfirmPasswordResetUseCase()
    }

    func send(_ intent: ForgotPasswordIntent) {
        switch intent {
        case .emailChanged(let email):
            state.email = email

        case .otpChanged(let otp):
            state.otp = otp

        case .newPasswordChanged(let password):
            state.newPassword = password

        case .confirmPasswordChanged(let password):
            state.confirmPassword = password

        case .togglePasswordVisibility:
            state.isPasswordVisible.toggle()

        case .requestReset:
            performRequestReset()

        case .confirmReset:
            performConfirmReset()

        case .dismissError:
            state.errorMessage = nil
        }
    }

    private func performRequestReset() {
        state.isLoading = true
        state.errorMessage = nil

        Task {
            do {
                let result = try await requestPasswordResetUseCase.invoke(email: state.email)
                state.isLoading = false

                if result.isSuccess {
                    state.step = .confirm
                } else {
                    state.errorMessage = result.error
                }
            } catch {
                state.isLoading = false
                state.errorMessage = error.localizedDescription
            }
        }
    }

    private func performConfirmReset() {
        state.isLoading = true
        state.errorMessage = nil

        Task {
            do {
                let result = try await confirmPasswordResetUseCase.invoke(
                    email: state.email,
                    otp: state.otp,
                    newPassword: state.newPassword,
                    confirmPassword: state.confirmPassword
                )
                state.isLoading = false

                if result.isSuccess {
                    effectSubject.send(.passwordResetSuccess)
                } else {
                    state.errorMessage = result.error
                }
            } catch {
                state.isLoading = false
                state.errorMessage = error.localizedDescription
            }
        }
    }
}
