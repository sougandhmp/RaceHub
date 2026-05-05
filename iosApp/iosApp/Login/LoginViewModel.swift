import Foundation
import Combine
import Shared

/// ViewModel for the Login screen following the MVI pattern.
///
/// - Exposes `state` as a `@Published` property the View observes.
/// - Accepts user actions via `send(_:)` and processes them into state mutations.
/// - Emits one-time navigation events through `effectPublisher`.
///
/// `@MainActor` ensures all state mutations happen on the main thread,
/// keeping SwiftUI updates safe without manual `DispatchQueue.main` calls.
@MainActor
final class LoginViewModel: ObservableObject {

    /// Observable UI state. Collected by the View via `@StateObject` + `viewModel.state`.
    @Published private(set) var state = LoginState()

    // PassthroughSubject is used so each effect fires exactly once and is not
    // replayed to late subscribers (unlike CurrentValueSubject).
    private let effectSubject = PassthroughSubject<LoginEffect, Never>()

    /// Stream of one-time side effects (navigation, etc.) for the View to react to.
    var effectPublisher: AnyPublisher<LoginEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    /// Use case that validates credentials and delegates to the repository.
    private let loginUseCase: LoginUseCase

    init() {
        // Use dependency injection to get the network repository
        // Koin is already initialized in iOSApp.swift with the correct base URL
        loginUseCase = AuthDependencyProvider.companion.shared.createLoginUseCase()
    }

    /// Entry point for all View interactions.
    /// Maps each `LoginIntent` to a state mutation or a command.
    func send(_ intent: LoginIntent) {
        switch intent {
        case .emailChanged(let email):
            state.email = email

        case .passwordChanged(let password):
            state.password = password

        case .togglePasswordVisibility:
            state.isPasswordVisible.toggle()

        case .login:
            performLogin()

        case .dismissError:
            state.errorMessage = nil
        }
    }

    /// Runs the login use case and updates state based on the result.
    /// Wrapped in a `Task` so the synchronous use case call does not block the
    /// main thread when replaced with a real async implementation.
    private func performLogin() {
        state.isLoading = true
        state.errorMessage = nil

        Task {
            do {
                let result = try await loginUseCase.execute(email: state.email, password: state.password)
                state.isLoading = false

                if result.isSuccess {
                    effectSubject.send(.navigateToHome)
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
