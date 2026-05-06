import Foundation
import Combine
import Shared

/// ViewModel for the Profile tab following the MVI pattern.
@MainActor
final class ProfileViewModel: ObservableObject {

    @Published private(set) var state = ProfileState()

    private let effectSubject = PassthroughSubject<ProfileEffect, Never>()
    var effectPublisher: AnyPublisher<ProfileEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let userSession: UserSession = RaceDependencyProvider.companion.shared.userSession

    init() {
        // The session is populated at login time and stays stable for the
        // lifetime of the Profile screen, so a snapshot read is sufficient.
        // `StateFlow.value` bridges to Swift as `Any?`, hence the cast.
        state.user = userSession.currentUser.value as? User
    }

    func send(_ intent: ProfileIntent) {
        switch intent {
        case .signOut:
            signOut()
        case .dismissError:
            state.errorMessage = nil
        }
    }

    private func signOut() {
        state.isSigningOut = true
        state.errorMessage = nil
        userSession.clear()
        state.isSigningOut = false
        effectSubject.send(.signedOut)
    }
}
