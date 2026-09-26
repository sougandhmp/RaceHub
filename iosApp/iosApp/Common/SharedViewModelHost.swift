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

typealias ForumModel = SharedViewModelHost<ForumViewModel, ForumState, ForumEffect>

extension SharedViewModelHost where VM == ForumViewModel, State == ForumState, Effect == ForumEffect {
    /// The shared Forum tab ViewModel, created through Koin.
    static func forum() -> ForumModel {
        ForumModel(
            create: { SharedViewModels.shared.forum(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow },
            effects: { $0.effects }
        )
    }

    func send(_ intent: ForumIntent) {
        viewModel.onIntent(intent: intent)
    }
}

typealias ThreadDetailModel = SharedViewModelHost<ThreadDetailViewModel, ThreadDetailState, ThreadDetailEffect>

extension SharedViewModelHost where VM == ThreadDetailViewModel, State == ThreadDetailState, Effect == ThreadDetailEffect {
    /// A shared thread-detail ViewModel; send `ThreadDetailIntent.Open` when the thread appears.
    static func threadDetail() -> ThreadDetailModel {
        ThreadDetailModel(
            create: { SharedViewModels.shared.threadDetail(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow },
            effects: { $0.effects }
        )
    }

    func send(_ intent: ThreadDetailIntent) {
        viewModel.onIntent(intent: intent)
    }
}

typealias CreateThreadModel = SharedViewModelHost<CreateThreadViewModel, CreateThreadState, CreateThreadEffect>

extension SharedViewModelHost where VM == CreateThreadViewModel, State == CreateThreadState, Effect == CreateThreadEffect {
    /// A shared create-thread form ViewModel.
    static func createThread() -> CreateThreadModel {
        CreateThreadModel(
            create: { SharedViewModels.shared.createThread(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow },
            effects: { $0.effects }
        )
    }

    func send(_ intent: CreateThreadIntent) {
        viewModel.onIntent(intent: intent)
    }
}

typealias ProfileModel = SharedViewModelHost<ProfileViewModel, ProfileState, ProfileEffect>

extension SharedViewModelHost where VM == ProfileViewModel, State == ProfileState, Effect == ProfileEffect {
    /// The shared Profile tab ViewModel, created through Koin.
    static func profile() -> ProfileModel {
        ProfileModel(
            create: { SharedViewModels.shared.profile(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow },
            effects: { $0.effects }
        )
    }

    func send(_ intent: ProfileIntent) {
        viewModel.onIntent(intent: intent)
    }
}

/// The home shell has no effects, hence `Never`.
typealias HomeModel = SharedViewModelHost<HomeViewModel, HomeState, Never>

extension SharedViewModelHost where VM == HomeViewModel, State == HomeState, Effect == Never {
    /// The shared home-shell ViewModel (selected tab), created through Koin.
    static func home() -> HomeModel {
        HomeModel(
            create: { SharedViewModels.shared.home(owner: $0) },
            value: { $0.state },
            flow: { $0.stateFlow }
        )
    }

    func send(_ intent: HomeIntent) {
        viewModel.onIntent(intent: intent)
    }
}

// MARK: - Auth

typealias LoginModel = SharedViewModelHost<LoginViewModel, LoginState, LoginEffect>

extension SharedViewModelHost where VM == LoginViewModel, State == LoginState, Effect == LoginEffect {
    static func login() -> LoginModel {
        LoginModel(create: { SharedViewModels.shared.login(owner: $0) }, value: { $0.state }, flow: { $0.stateFlow }, effects: { $0.effects })
    }

    func send(_ intent: LoginIntent) { viewModel.onIntent(intent: intent) }
}

typealias SignUpModel = SharedViewModelHost<SignUpViewModel, SignUpState, SignUpEffect>

extension SharedViewModelHost where VM == SignUpViewModel, State == SignUpState, Effect == SignUpEffect {
    static func signUp() -> SignUpModel {
        SignUpModel(create: { SharedViewModels.shared.signUp(owner: $0) }, value: { $0.state }, flow: { $0.stateFlow }, effects: { $0.effects })
    }

    func send(_ intent: SignUpIntent) { viewModel.onIntent(intent: intent) }
}

typealias ForgotPasswordModel = SharedViewModelHost<ForgotPasswordViewModel, ForgotPasswordState, ForgotPasswordEffect>

extension SharedViewModelHost where VM == ForgotPasswordViewModel, State == ForgotPasswordState, Effect == ForgotPasswordEffect {
    static func forgotPassword() -> ForgotPasswordModel {
        ForgotPasswordModel(create: { SharedViewModels.shared.forgotPassword(owner: $0) }, value: { $0.state }, flow: { $0.stateFlow }, effects: { $0.effects })
    }

    func send(_ intent: ForgotPasswordIntent) { viewModel.onIntent(intent: intent) }
}

typealias EmailVerificationModel = SharedViewModelHost<EmailVerificationViewModel, EmailVerificationState, EmailVerificationEffect>

extension SharedViewModelHost where VM == EmailVerificationViewModel, State == EmailVerificationState, Effect == EmailVerificationEffect {
    /// Send `EmailVerificationIntent.Open` with the address when the screen appears.
    static func emailVerification() -> EmailVerificationModel {
        EmailVerificationModel(create: { SharedViewModels.shared.emailVerification(owner: $0) }, value: { $0.state }, flow: { $0.stateFlow }, effects: { $0.effects })
    }

    func send(_ intent: EmailVerificationIntent) { viewModel.onIntent(intent: intent) }
}
