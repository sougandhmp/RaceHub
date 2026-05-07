import SwiftUI
import Shared

private enum Screen {
    case login, signUp, home
}

struct ContentView: View {

    @StateObject private var themeManager = ThemeManager()
    @State private var screen: Screen

    init() {
        let hasSession = RaceDependencyProvider.companion.shared.userSession.currentUser.value is User
        _screen = State(initialValue: hasSession ? .home : .login)
    }

    var body: some View {
        Group {
            switch screen {
            case .login:
                LoginView(
                    onLoginSuccess:     { screen = .home   },
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
        .preferredColorScheme(themeManager.preferredColorScheme)
        .environmentObject(themeManager)
    }
}

#Preview {
    ContentView()
}
