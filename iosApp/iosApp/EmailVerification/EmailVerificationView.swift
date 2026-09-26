import SwiftUI
import Shared

private let t = AppColorTokens.shared

/// Enter the one-time code emailed after sign-up (or an unverified login).
/// Verifying does not sign the user in; they return to login afterwards.
struct EmailVerificationView: View {

    let email: String
    let onBack: () -> Void
    let onEmailVerified: () -> Void

    @StateObject private var model = EmailVerificationModel.emailVerification()

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
            .accessibilityLabel(Text("Back"))
            .padding(.top, 8)

            ScrollView {
                VStack(spacing: 0) {
                    Spacer(minLength: 80)

                    Text("📧")
                        .font(.system(size: 56))

                    Spacer(minLength: 16)

                    Text("Verify your email")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(AppColors.racingRed)

                    Text(String(format: NSLocalizedString("email_verification_subtitle", comment: ""), model.state.email))
                        .font(.subheadline)
                        .foregroundColor(Color(hex: t.authMuted))
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)

                    Spacer(minLength: 32)

                    AuthTextField(
                        label: "Verification Code",
                        placeholder: "123456",
                        text: Binding(
                            get: { model.state.otp },
                            set: { model.send(EmailVerificationIntent.OtpChanged(otp: $0)) }
                        ),
                        keyboardType: .numberPad
                    )
                    .textContentType(.oneTimeCode)

                    if model.state.codeResent {
                        Text("A new code has been sent.")
                            .font(.caption)
                            .foregroundColor(Color(hex: t.authMuted))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    }

                    if let error = model.state.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(AppColors.racingRed)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.top, 8)
                    }

                    Spacer(minLength: 32)

                    Button(action: { model.send(EmailVerificationIntent.Verify.shared) }) {
                        ZStack {
                            if model.state.isVerifying {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Text("Verify Email")
                                    .font(.system(size: 16, weight: .semibold))
                                    .foregroundColor(.white)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(AppColors.racingRed.opacity(model.state.isVerifying ? 0.4 : 1.0))
                    )
                    .disabled(model.state.isVerifying)

                    Spacer(minLength: 16)

                    Button(action: { model.send(EmailVerificationIntent.ResendCode.shared) }) {
                        Text("Didn't get it? Resend code")
                            .font(.footnote.bold())
                            .foregroundColor(AppColors.racingRed)
                    }
                    .disabled(model.state.isResending)

                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 20)
            }
        }
        .onAppear { model.send(EmailVerificationIntent.Open(email: email)) }
        // One-off effects from the shared ViewModel.
        .onReceive(model.effects) { effect in
            if effect is EmailVerificationEffect.EmailVerified {
                onEmailVerified()
            }
        }
    }
}

#Preview {
    EmailVerificationView(email: "driver@racehub.com", onBack: {}, onEmailVerified: {})
}
