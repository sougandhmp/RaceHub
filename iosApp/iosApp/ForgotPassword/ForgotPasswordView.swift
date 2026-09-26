import SwiftUI
import Combine
import Shared

private let t = AppColorTokens.shared

struct ForgotPasswordView: View {

    @StateObject private var model = ForgotPasswordModel.forgotPassword()
    let onBack: () -> Void
    let onPasswordResetSuccess: () -> Void

    var body: some View {
        ZStack(alignment: .topLeading) {
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

            Button(action: onBack) {
                Image(systemName: "arrow.left")
                    .foregroundColor(.white)
                    .padding(16)
            }
            .padding(.top, 8)

            ScrollView {
                VStack(spacing: 0) {
                    Spacer(minLength: 80)

                    Text("🔑")
                        .font(.system(size: 56))

                    Spacer(minLength: 16)

                    Text(model.state.step == ForgotPasswordStep.request ? String(localized: "Forgot Password?") : String(localized: "Reset Password"))
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(AppColors.racingRed)

                    Spacer(minLength: 8)

                    Text(model.state.step == ForgotPasswordStep.request
                         ? String(localized: "forgot_password_request_subtitle")
                         : String(format: NSLocalizedString("forgot_password_confirm_subtitle", comment: ""), model.state.email))
                        .font(.footnote)
                        .foregroundColor(Color(hex: t.authMuted))
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)

                    Spacer(minLength: 40)

                    if model.state.step == ForgotPasswordStep.request {
                        RequestStepView(state: model.state, onIntent: model.send)
                    } else {
                        ConfirmStepView(state: model.state, onIntent: model.send)
                    }

                    if let error = model.state.error?.userMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(AppColors.racingRed)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 10)
                    }

                    Spacer(minLength: 32)

                    Button(action: {
                        if model.state.step == ForgotPasswordStep.request {
                            model.send(ForgotPasswordIntent.RequestReset.shared)
                        } else {
                            model.send(ForgotPasswordIntent.ConfirmReset.shared)
                        }
                    }) {
                        ZStack {
                            if model.state.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text(model.state.step == ForgotPasswordStep.request ? String(localized: "Send Reset Code") : String(localized: "Reset Password"))
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

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 20)
            }
        }
        // One-off effects from the shared ViewModel.
        .onReceive(model.effects) { effect in
            if effect is ForgotPasswordEffect.PasswordResetSuccess {
                onPasswordResetSuccess()
            }
        }
    }
}

private struct RequestStepView: View {
    let state: ForgotPasswordState
    let onIntent: (ForgotPasswordIntent) -> Void

    var body: some View {
        AuthTextField(
            label: "Email",
            placeholder: "you@racehub.com",
            text: Binding(
                get: { state.email },
                set: { onIntent(ForgotPasswordIntent.EmailChanged(email: $0)) }
            ),
            keyboardType: .emailAddress
        )
    }
}

private struct ConfirmStepView: View {
    let state: ForgotPasswordState
    let onIntent: (ForgotPasswordIntent) -> Void

    var body: some View {
        AuthTextField(
            label: "Reset Code",
            placeholder: "6-digit code",
            text: Binding(
                get: { state.otp },
                set: { onIntent(ForgotPasswordIntent.OtpChanged(otp: $0)) }
            ),
            keyboardType: .numberPad
        )

        Spacer(minLength: 14)

        PasswordFieldView(
            label: "New Password",
            text: Binding(
                get: { state.newPassword },
                set: { onIntent(ForgotPasswordIntent.NewPasswordChanged(password: $0)) }
            ),
            isVisible: state.isPasswordVisible,
            onToggle: { onIntent(ForgotPasswordIntent.TogglePasswordVisibility.shared) }
        )

        Spacer(minLength: 14)

        PasswordFieldView(
            label: "Confirm New Password",
            text: Binding(
                get: { state.confirmPassword },
                set: { onIntent(ForgotPasswordIntent.ConfirmPasswordChanged(password: $0)) }
            ),
            isVisible: state.isPasswordVisible,
            onToggle: { onIntent(ForgotPasswordIntent.TogglePasswordVisibility.shared) }
        )
    }
}

private struct PasswordFieldView: View {
    let label: LocalizedStringKey
    @Binding var text: String
    let isVisible: Bool
    let onToggle: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: t.authMuted))

            HStack {
                Group {
                    if isVisible {
                        TextField("••••••", text: $text)
                    } else {
                        SecureField("••••••", text: $text)
                    }
                }
                .foregroundColor(.white)
                .autocorrectionDisabled()
                .autocapitalization(.none)

                Button(action: onToggle) {
                    Text(isVisible ? String(localized: "Hide") : String(localized: "Show"))
                        .font(.caption)
                        .foregroundColor(Color(hex: t.authMuted))
                }
            }
            .padding()
            .background(Color.white.opacity(0.05))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        text.isEmpty ? Color(hex: t.authDimBorder) : AppColors.racingRed,
                        lineWidth: 1.5
                    )
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

#Preview {
    ForgotPasswordView(onBack: {}, onPasswordResetSuccess: {})
}
