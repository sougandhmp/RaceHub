import Foundation
import Combine
import Shared

/// ViewModel for the Sign-Up screen following the MVI pattern.
///
/// - Exposes `state` as a `@Published` property the View observes.
/// - Accepts user actions via `send(_:)` and processes them into state mutations.
/// - Emits one-time navigation events through `effectPublisher`.
///
/// `@MainActor` ensures all state mutations happen on the main thread,
/// keeping SwiftUI updates safe without manual `DispatchQueue.main` calls.
@MainActor
final class SignUpViewModel: ObservableObject {

    /// Observable UI state. Collected by the View via `@StateObject` + `viewModel.state`.
    @Published private(set) var state = SignUpState()

    // PassthroughSubject is used so each effect fires exactly once and is not
    // replayed to late subscribers (unlike CurrentValueSubject).
    private let effectSubject = PassthroughSubject<SignUpEffect, Never>()

    /// Stream of one-time side effects (navigation, etc.) for the View to react to.
    var effectPublisher: AnyPublisher<SignUpEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    /// Use case that validates the sign-up form and delegates to the repository.
    private let signUpUseCase: SignUpUseCase

    init() {
        signUpUseCase = SignUpUseCase(authRepository: AuthRepositoryImpl())
    }

    /// Entry point for all View interactions.
    /// Maps each `SignUpIntent` to a state mutation or a command.
    func send(_ intent: SignUpIntent) {
        switch intent {
        case .nameChanged(let name):
            state.name = name

        case .emailChanged(let email):
            state.email = email

        case .passwordChanged(let password):
            state.password = password

        case .confirmPasswordChanged(let confirmPassword):
            state.confirmPassword = confirmPassword

        case .togglePasswordVisibility:
            state.isPasswordVisible.toggle()

        case .toggleConfirmPasswordVisibility:
            state.isConfirmPasswordVisible.toggle()

        case .signUp:
            performSignUp()
        }
    }

    /// Runs the sign-up use case and updates state based on the result.
    /// Wrapped in a `Task` so the synchronous use case call does not block the
    /// main thread when replaced with a real async implementation.
    private func performSignUp() {
        state.isLoading = true
        state.errorMessage = nil

        Task {
            let result = signUpUseCase.execute(
                name: state.name,
                email: state.email,
                password: state.password,
                confirmPassword: state.confirmPassword
            )
            state.isLoading = false

            if result.isSuccess {
                effectSubject.send(.navigateToHome)
            } else {
                state.errorMessage = result.error
            }
        }
    }
}
