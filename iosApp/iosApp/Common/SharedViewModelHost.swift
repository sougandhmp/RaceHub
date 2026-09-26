import Combine
import Foundation
import KMPNativeCoroutinesAsync
import KMPNativeCoroutinesCore
import Shared

/// Hosts one shared Kotlin MVI ViewModel for a SwiftUI screen.
///
/// - `state`: the ViewModel's `StateFlow` (exposed by KMP-NativeCoroutines as a
///   `<name>` value plus a `<name>Flow` native flow), republished as `@Published`.
/// - `effects`: one-off events from the ViewModel's effect flow; views react with
///   `.onReceive(host.effects)`.
///
/// Clears the ViewModel when the host is released — running `onCleared()` and
/// cancelling `viewModelScope`, as Android does when a screen leaves the back stack.
/// Own it with `@StateObject` so it lives exactly as long as the screen.
@MainActor
final class SharedViewModelHost<VM: AnyObject, State, Effect>: ObservableObject {
    let viewModel: VM
    @Published private(set) var state: State
    let effects = PassthroughSubject<Effect, Never>()

    private let lifetime: Lifetime

    init(
        create: (ViewModelOwner) -> VM,
        value: (VM) -> State,
        flow: (VM) -> NativeFlow<State, Error, KotlinUnit>,
        effects effectFlow: ((VM) -> NativeFlow<Effect, Error, KotlinUnit>)? = nil
    ) {
        let owner = ViewModelOwner()
        let viewModel = create(owner)
        let stateFlow = flow(viewModel)
        self.viewModel = viewModel
        self.state = value(viewModel)
        self.lifetime = Lifetime(owner: owner)

        lifetime.tasks.append(Task { [weak self] in
            do {
                for try await value in asyncSequence(for: stateFlow) {
                    self?.state = value
                }
            } catch {
                // A StateFlow never completes with an error; cancellation ends the loop.
            }
        })
        if let effectFlow {
            let flow = effectFlow(viewModel)
            lifetime.tasks.append(Task { [weak self] in
                do {
                    for try await effect in asyncSequence(for: flow) {
                        self?.effects.send(effect)
                    }
                } catch {}
            })
        }
    }
}

/// Tears the ViewModel down when its host deinitialises. Kept separate so the
/// cleanup runs from a plain (non-actor-isolated) deinit.
private final class Lifetime {
    let owner: ViewModelOwner
    var tasks: [Task<Void, Never>] = []

    init(owner: ViewModelOwner) {
        self.owner = owner
    }

    deinit {
        tasks.forEach { $0.cancel() }
        owner.clear()
    }
}

typealias RaceModel = SharedViewModelHost<RaceViewModel, RaceState, RaceEffect>

extension SharedViewModelHost where VM == RaceViewModel, State == RaceState, Effect == RaceEffect {
    /// The shared Race tab ViewModel, created through Koin.
    static func race() -> RaceModel {
        RaceModel(
            create: { SharedViewModels.shared.race(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow },
            effects: { $0.effects }
        )
    }

    func send(_ intent: RaceIntent) {
        viewModel.onIntent(intent: intent)
    }
}
