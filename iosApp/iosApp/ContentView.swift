import SwiftUI

private enum Screen {
    case login, signUp, home
}

struct ContentView: View {

    @State private var screen: Screen = .login

    var body: some View {
        switch screen {
        case .login:
            LoginView(
                onLoginSuccess: { screen = .home },
                onNavigateToSignUp: { screen = .signUp }
            )
        case .signUp:
            SignUpView(
                onSignUpSuccess: { screen = .home },
                onNavigateToLogin: { screen = .login }
            )
        case .home:
            HomeView()
        }
    }
}

struct HomeView: View {
    var body: some View {
        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()
            Text("Welcome to RaceHub!")
                .font(.title2.bold())
                .foregroundColor(Color(hex: "E63946"))
        }
    }
}

#Preview {
    ContentView()
}
