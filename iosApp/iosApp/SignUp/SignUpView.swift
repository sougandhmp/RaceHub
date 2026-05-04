import SwiftUI
import Combine

struct SignUpView: View {

    @StateObject private var viewModel = SignUpViewModel()
    let onSignUpSuccess: () -> Void
    let onNavigateToLogin: () -> Void

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
                    Spacer(minLength: 64)

                    Text("🏁")
                        .font(.system(size: 56))

                    Text("Join RaceHub")
                        .font(.system(size: 32, weight: .heavy))
                        .foregroundColor(Color(hex: "E63946"))
                        .padding(.top, 8)

                    Text("Start your racing journey")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: "8D99AE"))
                        .padding(.top, 4)

                    Spacer(minLength: 40)

                    // Name
                    inputField(
                        label: "Full Name",
                        placeholder: "John Doe",
                        text: Binding(
                            get: { viewModel.state.name },
                            set: { viewModel.send(.nameChanged($0)) }
                        ),
                        keyboardType: .default
                    )

                    Spacer(minLength: 14)

                    // Email
                    inputField(
                        label: "Email",
                        placeholder: "you@racehub.com",
                        text: Binding(
                            get: { viewModel.state.email },
                            set: { viewModel.send(.emailChanged($0)) }
                        ),
                        keyboardType: .emailAddress
                    )

                    Spacer(minLength: 14)

                    // Password
                    passwordField(
                        label: "Password",
                        text: Binding(
                            get: { viewModel.state.password },
                            set: { viewModel.send(.passwordChanged($0)) }
                        ),
                        isVisible: viewModel.state.isPasswordVisible,
                        onToggle: { viewModel.send(.togglePasswordVisibility) }
                    )

                    Spacer(minLength: 14)

                    // Confirm Password
                    passwordField(
                        label: "Confirm Password",
                        text: Binding(
                            get: { viewModel.state.confirmPassword },
                            set: { viewModel.send(.confirmPasswordChanged($0)) }
                        ),
                        isVisible: viewModel.state.isConfirmPasswordVisible,
                        onToggle: { viewModel.send(.toggleConfirmPasswordVisibility) }
                    )

                    // Error
                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(Color(hex: "E63946"))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    }

                    Spacer(minLength: 32)

                    // Create Account Button
                    Button(action: { viewModel.send(.signUp) }) {
                        ZStack {
                            if viewModel.state.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Create Account")
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

                    Spacer(minLength: 20)

                    HStack(spacing: 4) {
                        Text("Already have an account?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: "8D99AE"))

                        Button(action: onNavigateToLogin) {
                            Text("Sign In")
                                .font(.footnote.bold())
                                .foregroundColor(Color(hex: "E63946"))
                        }
                    }

                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 28)
            }
        }
        .onReceive(viewModel.effectPublisher) { effect in
            switch effect {
            case .navigateToHome:
                onSignUpSuccess()
            }
        }
    }

    // MARK: - Reusable field builders

    private func inputField(
        label: String,
        placeholder: String,
        text: Binding<String>,
        keyboardType: UIKeyboardType
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: "8D99AE"))

            TextField(placeholder, text: text)
                .keyboardType(keyboardType)
                .autocapitalization(.none)
                .autocorrectionDisabled()
                .foregroundColor(.white)
                .padding()
                .background(Color.white.opacity(0.05))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(
                            text.wrappedValue.isEmpty ? Color(hex: "444444") : Color(hex: "E63946"),
                            lineWidth: 1.5
                        )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private func passwordField(
        label: String,
        text: Binding<String>,
        isVisible: Bool,
        onToggle: @escaping () -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: "8D99AE"))

            HStack {
                Group {
                    if isVisible {
                        TextField("••••••", text: text)
                    } else {
                        SecureField("••••••", text: text)
                    }
                }
                .foregroundColor(.white)
                .autocorrectionDisabled()
                .autocapitalization(.none)

                Button(action: onToggle) {
                    Text(isVisible ? "Hide" : "Show")
                        .font(.caption)
                        .foregroundColor(Color(hex: "8D99AE"))
                }
            }
            .padding()
            .background(Color.white.opacity(0.05))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        text.wrappedValue.isEmpty ? Color(hex: "444444") : Color(hex: "E63946"),
                        lineWidth: 1.5
                    )
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

#Preview {
    SignUpView(onSignUpSuccess: {}, onNavigateToLogin: {})
}
