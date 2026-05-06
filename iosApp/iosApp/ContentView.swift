import SwiftUI
import Shared

/// Top-level navigation destinations.
private enum Screen {
    case login, signUp, home
}

/// Root view. Owns navigation state and routes to the correct screen.
struct ContentView: View {

    @State private var screen: Screen

    init() {
        let hasSession = RaceDependencyProvider.companion.shared.userSession.currentUser.value is User
        _screen = State(initialValue: hasSession ? .home : .login)
    }

    var body: some View {
        switch screen {
        case .login:
            LoginView(
                onLoginSuccess:    { screen = .home   },
                onNavigateToSignUp: { screen = .signUp }
            )
        case .signUp:
            SignUpView(
                onSignUpSuccess:    { screen = .home  },
                onNavigateToLogin: { screen = .login  }
            )
        case .home:
            HomeView(onSignedOut: { screen = .login })
        }
    }
}

#Preview {
    ContentView()
}
