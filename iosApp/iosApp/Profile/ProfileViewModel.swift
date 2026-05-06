import Foundation
import Combine

/// ViewModel for the Profile tab. Currently a placeholder — fields and
/// behaviors will be added as the profile feature is built out.
@MainActor
final class ProfileViewModel: ObservableObject {

    @Published private(set) var state = ProfileState()

    private let effectSubject = PassthroughSubject<ProfileEffect, Never>()
    var effectPublisher: AnyPublisher<ProfileEffect, Never> {
        effectSubject.eraseToAnyPublisher()
    }

    func send(_ intent: ProfileIntent) {
        switch intent {
        case .dismissError:
            state.errorMessage = nil
        }
    }
}
