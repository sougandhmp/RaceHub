import Foundation
import Combine

/// Owns the Home shell state — currently just the selected tab. Each tab's
/// content (Race, Forum, Profile) is loaded by its own feature ViewModel.
@MainActor
final class HomeViewModel: ObservableObject {

    @Published private(set) var state = HomeState()

    func send(_ intent: HomeIntent) {
        switch intent {
        case .tabSelected(let tab):
            state.selectedTab = tab
        }
    }
}
