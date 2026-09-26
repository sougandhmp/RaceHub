import SwiftUI
import Shared

private enum Screen {
    case login, signUp, forgotPassword, home
    /// Verify `email`; back returns to whichever screen opened it.
    case emailVerification(email: String, backTo: AuthOrigin)
}

private enum AuthOrigin { case login, signUp }

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
                    onLoginSuccess:             { screen = .home          },
                    onNavigateToEmailVerification: { screen = .emailVerification(email: $0, backTo: .login) },
                    onNavigateToSignUp:         { screen = .signUp        },
                    onNavigateToForgotPassword: { screen = .forgotPassword }
                )
            case .signUp:
                SignUpView(
                    onSignUpSuccess:   { screen = .emailVerification(email: $0, backTo: .signUp) },
                    onNavigateToLogin: { screen = .login }
                )
            case .forgotPassword:
                ForgotPasswordView(
                    onBack:                 { screen = .login },
                    onPasswordResetSuccess: { screen = .login }
                )
            case .emailVerification(let email, let origin):
                EmailVerificationView(
                    email: email,
                    onBack: { screen = origin == .login ? .login : .signUp },
                    // Verified, but not signed in: sign in with the verified account.
                    onEmailVerified: { screen = .login }
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
