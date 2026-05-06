import SwiftUI

/// Top-level navigation destinations.
private enum Screen {
    case login, signUp, home
}

/// Root view. Owns navigation state and routes to the correct screen.
///
/// Navigation is kept intentionally simple (enum + switch) here.
/// Replace with `NavigationStack` + a router when the app grows.
struct ContentView: View {

    @State private var screen: Screen = .login

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
