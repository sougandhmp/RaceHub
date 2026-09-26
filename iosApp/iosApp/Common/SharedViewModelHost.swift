import Foundation
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore
import Shared

/// Hosts one shared Kotlin ViewModel for a SwiftUI screen.
///
/// Republishes the ViewModel's `StateFlow` (exposed by KMP-NativeCoroutines as a
/// `<name>` value plus a `<name>Flow` native flow) as `@Published state`, and clears the ViewModel
/// when the host is released — running `onCleared()` and cancelling
/// `viewModelScope`, as Android does when a screen leaves the back stack.
/// Own it with `@StateObject` so it lives exactly as long as the screen.
@MainActor
final class SharedViewModelHost<VM: AnyObject, State>: ObservableObject {
    let viewModel: VM
    @Published private(set) var state: State

    private let lifetime: Lifetime

    init(
        create: (ViewModelOwner) -> VM,
        value: (VM) -> State,
        flow: (VM) -> NativeFlow<State, Error, KotlinUnit>
    ) {
        let owner = ViewModelOwner()
        let viewModel = create(owner)
        let stateFlow = flow(viewModel)
        self.viewModel = viewModel
        self.state = value(viewModel)
        self.lifetime = Lifetime(owner: owner)
        lifetime.observation = Task { [weak self] in
            do {
                for try await value in asyncSequence(for: stateFlow) {
                    self?.state = value
                }
            } catch {
                // A StateFlow never completes with an error; cancellation ends the loop.
            }
        }
    }
}

/// Tears the ViewModel down when its host deinitialises. Kept separate so the
/// cleanup runs from a plain (non-actor-isolated) deinit.
private final class Lifetime {
    let owner: ViewModelOwner
    var observation: Task<Void, Never>?

    init(owner: ViewModelOwner) {
        self.owner = owner
    }

    deinit {
        observation?.cancel()
        owner.clear()
    }
}

typealias RaceModel = SharedViewModelHost<RaceViewModel, RaceState>

extension SharedViewModelHost where VM == RaceViewModel, State == RaceState {
    /// The shared Race tab ViewModel, created through Koin.
    static func race() -> RaceModel {
        RaceModel(
            create: { SharedViewModels.shared.race(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow }
        )
    }

    func send(_ intent: RaceIntent) {
        viewModel.onIntent(intent: intent)
    }
}
