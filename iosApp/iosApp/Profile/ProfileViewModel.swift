import Foundation
import Combine
import Shared

@MainActor
final class ProfileViewModel: ObservableObject {

    @Published private(set) var state = ProfileState()

    private let effectSubject = PassthroughSubject<ProfileEffect, Never>()
    var effectPublisher: AnyPublisher<ProfileEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    private let userSession: UserSession
    private let logoutUseCase: LogoutUseCase
    private let getMyProfileUseCase: GetMyProfileUseCase

    init() {
        let raceProvider = RaceDependencyProvider.companion.shared
        let authProvider = AuthDependencyProvider.companion.shared
        userSession = raceProvider.userSession
        logoutUseCase = authProvider.logoutUseCase
        getMyProfileUseCase = raceProvider.createGetMyProfileUseCase()

        state.user = userSession.currentUser.value as? User
        fetchProfile()
    }

    func send(_ intent: ProfileIntent) {
        switch intent {
        case .signOut:
            signOut()
        case .refreshProfile:
            fetchProfile()
        case .dismissError:
            state.errorMessage = nil
        }
    }

    func fetchProfile() {
        Task { await performFetch() }
    }

    func refresh() async {
        await performFetch()
    }

    private func performFetch() async {
        guard let user = state.user,
              let token = user.token, !token.isEmpty else { return }

        state.isLoadingProfile = true
        state.errorMessage = nil

        do {
            let profile = try await getMyProfileUseCase.invoke(
                userId: user.id,
                token: token
            )
            state.isLoadingProfile = false
            state.postsCount = Int(profile.postsCount)
            state.savedCount = Int(profile.savedCount)
            state.recentThreadTitles = profile.recentThreadTitles
            state.savedThreadTitles = profile.savedThreadTitles
        } catch {
            state.isLoadingProfile = false
            state.errorMessage = error.localizedDescription
        }
    }

    private func signOut() {
        state.isSigningOut = true
        state.errorMessage = nil

        Task {
            do {
                let success = try await logoutUseCase.invoke()
                state.isSigningOut = false
                if success.boolValue {
                    effectSubject.send(.signedOut)
                } else {
                    state.errorMessage = "Sign out failed. Please try again."
                }
            } catch {
                state.isSigningOut = false
                state.errorMessage = error.localizedDescription
            }
        }
    }
}
