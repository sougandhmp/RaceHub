import SwiftUI
import Combine

struct LoginView: View {

    @StateObject private var viewModel = LoginViewModel()
    let onLoginSuccess: () -> Void
    let onNavigateToSignUp: () -> Void

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(hex: "0A0A0A"),
                    Color(hex: "1A1A2E"),
                    Color(hex: "16213E")
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 0) {
                    Spacer(minLength: 80)

                    Text("🏎️")
                        .font(.system(size: 64))

                    Text("RaceHub")
                        .font(.system(size: 36, weight: .heavy))
                        .foregroundColor(Color(hex: "E63946"))
                        .padding(.top, 8)

                    Text("Your Racing Universe")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: "8D99AE"))
                        .padding(.top, 4)

                    Spacer(minLength: 52)

                    // Email Field
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Email")
                            .font(.caption)
                            .foregroundColor(Color(hex: "8D99AE"))

                        TextField("driver@racehub.com", text: Binding(
                            get: { viewModel.state.email },
                            set: { viewModel.send(.emailChanged($0)) }
                        ))
                        .keyboardType(.emailAddress)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.white.opacity(0.05))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(
                                    viewModel.state.email.isEmpty
                                        ? Color(hex: "444444")
                                        : Color(hex: "E63946"),
                                    lineWidth: 1.5
                                )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Spacer(minLength: 16)

                    // Password Field
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Password")
                            .font(.caption)
                            .foregroundColor(Color(hex: "8D99AE"))

                        HStack {
                            Group {
                                if viewModel.state.isPasswordVisible {
                                    TextField("••••••", text: Binding(
                                        get: { viewModel.state.password },
                                        set: { viewModel.send(.passwordChanged($0)) }
                                    ))
                                } else {
                                    SecureField("••••••", text: Binding(
                                        get: { viewModel.state.password },
                                        set: { viewModel.send(.passwordChanged($0)) }
                                    ))
                                }
                            }
                            .foregroundColor(.white)
                            .autocorrectionDisabled()
                            .autocapitalization(.none)

                            Button(action: { viewModel.send(.togglePasswordVisibility) }) {
                                Text(viewModel.state.isPasswordVisible ? "Hide" : "Show")
                                    .font(.caption)
                                    .foregroundColor(Color(hex: "8D99AE"))
                            }
                        }
                        .padding()
                        .background(Color.white.opacity(0.05))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(
                                    viewModel.state.password.isEmpty
                                        ? Color(hex: "444444")
                                        : Color(hex: "E63946"),
                                    lineWidth: 1.5
                                )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    // Error Message
                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(Color(hex: "E63946"))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    }

                    Spacer(minLength: 36)

                    // Sign In Button
                    Button(action: { viewModel.send(.login) }) {
                        ZStack {
                            if viewModel.state.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Sign In")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "E63946").opacity(viewModel.state.isLoading ? 0.4 : 1.0))
                    )
                    .disabled(viewModel.state.isLoading)

                    Button(action: {}) {
                        Text("Forgot Password?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: "8D99AE"))
                    }
                    .padding(.top, 16)

                    Spacer(minLength: 16)

                    HStack(spacing: 4) {
                        Text("Don't have an account?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: "8D99AE"))

                        Button(action: onNavigateToSignUp) {
                            Text("Sign Up")
                                .font(.footnote.bold())
                                .foregroundColor(Color(hex: "E63946"))
                        }
                    }

                    Spacer(minLength: 32)

                    Text("Use driver@racehub.com / race123")
                        .font(.caption2)
                        .foregroundColor(Color(hex: "8D99AE").opacity(0.5))

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 28)
            }
        }
        .onReceive(viewModel.effectPublisher) { effect in
            switch effect {
            case .navigateToHome:
                onLoginSuccess()
            }
        }
    }
}

// MARK: - Hex Color extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: UInt64
        switch hex.count {
        case 6:
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default:
            (r, g, b) = (0, 0, 0)
        }
        self.init(
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255
        )
    }
}

#Preview {
    LoginView(onLoginSuccess: {}, onNavigateToSignUp: {})
}
