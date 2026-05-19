import SwiftUI
import Combine
import Shared

private let t = AppColorTokens.shared

struct ForgotPasswordView: View {

    @StateObject private var viewModel = ForgotPasswordViewModel()
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

                    Text(viewModel.state.step == .request ? "Forgot Password?" : "Reset Password")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(AppColors.racingRed)

                    Spacer(minLength: 8)

                    Text(viewModel.state.step == .request
                         ? "Enter your email and we'll send you a reset code."
                         : "Code sent to \(viewModel.state.email). Enter it below with your new password.")
                        .font(.footnote)
                        .foregroundColor(Color(hex: t.authMuted))
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)

                    Spacer(minLength: 40)

                    if viewModel.state.step == .request {
                        RequestStepView(state: viewModel.state, onIntent: viewModel.send)
                    } else {
                        ConfirmStepView(state: viewModel.state, onIntent: viewModel.send)
                    }

                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(AppColors.racingRed)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 10)
                    }

                    Spacer(minLength: 32)

                    Button(action: {
                        if viewModel.state.step == .request {
                            viewModel.send(.requestReset)
                        } else {
                            viewModel.send(.confirmReset)
                        }
                    }) {
                        ZStack {
                            if viewModel.state.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text(viewModel.state.step == .request ? "Send Reset Code" : "Reset Password")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(AppColors.racingRed.opacity(viewModel.state.isLoading ? 0.4 : 1.0))
                    )
                    .disabled(viewModel.state.isLoading)

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 28)
            }
        }
        .onReceive(viewModel.effectPublisher) { effect in
            switch effect {
            case .passwordResetSuccess:
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
            placeholder: "driver@racehub.com",
            text: Binding(
                get: { state.email },
                set: { onIntent(.emailChanged($0)) }
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
                set: { onIntent(.otpChanged($0)) }
            ),
            keyboardType: .numberPad
        )

        Spacer(minLength: 14)

        PasswordFieldView(
            label: "New Password",
            text: Binding(
                get: { state.newPassword },
                set: { onIntent(.newPasswordChanged($0)) }
            ),
            isVisible: state.isPasswordVisible,
            onToggle: { onIntent(.togglePasswordVisibility) }
        )

        Spacer(minLength: 14)

        PasswordFieldView(
            label: "Confirm New Password",
            text: Binding(
                get: { state.confirmPassword },
                set: { onIntent(.confirmPasswordChanged($0)) }
            ),
            isVisible: state.isPasswordVisible,
            onToggle: { onIntent(.togglePasswordVisibility) }
        )
    }
}

private struct AuthTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: t.authMuted))

            TextField(placeholder, text: $text)
                .keyboardType(keyboardType)
                .autocapitalization(.none)
                .autocorrectionDisabled()
                .foregroundColor(.white)
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

private struct PasswordFieldView: View {
    let label: String
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
                    Text(isVisible ? "Hide" : "Show")
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
