import SwiftUI
import Combine
import Shared

private let t = AppColorTokens.shared

struct LoginView: View {

    @StateObject private var model = LoginModel.login()
    let onLoginSuccess: () -> Void
    let onNavigateToEmailVerification: (String) -> Void
    let onNavigateToSignUp: () -> Void
    let onNavigateToForgotPassword: () -> Void

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    Color(hex: t.darkBackground),
                    Color(hex: t.authDarkBlue),
                    Color(hex: t.authDeepBlue)
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
                        .foregroundColor(AppColors.racingRed)
                        .padding(.top, 8)

                    Text("Your Racing Universe")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: t.authMuted))
                        .padding(.top, 4)

                    Spacer(minLength: 52)

                    // Email Field
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Email")
                            .font(.caption)
                            .foregroundColor(Color(hex: t.authMuted))

                        TextField("driver@racehub.com", text: Binding(
                            get: { model.state.email },
                            set: { model.send(LoginIntent.EmailChanged(email: $0)) }
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
                                    model.state.email.isEmpty
                                        ? Color(hex: t.authDimBorder)
                                        : AppColors.racingRed,
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
                            .foregroundColor(Color(hex: t.authMuted))

                        HStack {
                            Group {
                                if model.state.isPasswordVisible {
                                    TextField("••••••", text: Binding(
                                        get: { model.state.password },
                                        set: { model.send(LoginIntent.PasswordChanged(password: $0)) }
                                    ))
                                } else {
                                    SecureField("••••••", text: Binding(
                                        get: { model.state.password },
                                        set: { model.send(LoginIntent.PasswordChanged(password: $0)) }
                                    ))
                                }
                            }
                            .foregroundColor(.white)
                            .autocorrectionDisabled()
                            .autocapitalization(.none)

                            Button(action: { model.send(LoginIntent.TogglePasswordVisibility.shared) }) {
                                Text(model.state.isPasswordVisible ? String(localized: "Hide") : String(localized: "Show"))
                                    .font(.caption)
                                    .foregroundColor(Color(hex: t.authMuted))
                            }
                        }
                        .padding()
                        .background(Color.white.opacity(0.05))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(
                                    model.state.password.isEmpty
                                        ? Color(hex: t.authDimBorder)
                                        : AppColors.racingRed,
                                    lineWidth: 1.5
                                )
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    // Error Message
                    if let error = model.state.error?.userMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(AppColors.racingRed)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    }

                    Spacer(minLength: 36)

                    // Sign In Button
                    Button(action: { model.send(LoginIntent.Submit.shared) }) {
                        ZStack {
                            if model.state.isLoading {
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
                            .fill(AppColors.racingRed.opacity(model.state.isLoading ? 0.4 : 1.0))
                    )
                    .disabled(model.state.isLoading)

                    Button(action: onNavigateToForgotPassword) {
                        Text("Forgot Password?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: t.authMuted))
                    }
                    .padding(.top, 16)

                    Spacer(minLength: 16)

                    HStack(spacing: 4) {
                        Text("Don't have an account?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: t.authMuted))

                        Button(action: onNavigateToSignUp) {
                            Text("Sign Up")
                                .font(.footnote.bold())
                                .foregroundColor(AppColors.racingRed)
                        }
                    }

                    Spacer(minLength: 32)

                    Text("Use driver@racehub.com / race123")
                        .font(.caption2)
                        .foregroundColor(Color(hex: t.authMuted).opacity(0.5))

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 20)
            }
        }
        // One-off effects from the shared ViewModel.
        .onReceive(model.effects) { effect in
            if effect is LoginEffect.NavigateToHome {
                onLoginSuccess()
            } else if let verify = effect as? LoginEffect.NavigateToEmailVerification {
                onNavigateToEmailVerification(verify.email)
            }
        }
    }
}


#Preview {
    LoginView(onLoginSuccess: {}, onNavigateToEmailVerification: { _ in }, onNavigateToSignUp: {}, onNavigateToForgotPassword: {})
}
