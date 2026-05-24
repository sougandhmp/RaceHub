import SwiftUI
import Combine
import Shared

private let t = AppColorTokens.shared

struct SignUpView: View {

    @StateObject private var viewModel = SignUpViewModel()
    let onSignUpSuccess: () -> Void
    let onNavigateToLogin: () -> Void

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
                    Spacer(minLength: 64)

                    Text("🏁")
                        .font(.system(size: 56))

                    Text("Join RaceHub")
                        .font(.system(size: 32, weight: .heavy))
                        .foregroundColor(AppColors.racingRed)
                        .padding(.top, 8)

                    Text("Start your racing journey")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: t.authMuted))
                        .padding(.top, 4)

                    Spacer(minLength: 40)

                    // Username
                    inputField(
                        label: "Username",
                        placeholder: "RaceFan",
                        text: Binding(
                            get: { viewModel.state.username },
                            set: { viewModel.send(.usernameChanged($0)) }
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

                    Spacer(minLength: 14)

                    // Country
                    countryDropdown()

                    // Error
                    if let error = viewModel.state.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(AppColors.racingRed)
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
                            .fill(AppColors.racingRed.opacity(viewModel.state.isLoading ? 0.4 : 1.0))
                    )
                    .disabled(viewModel.state.isLoading)

                    Spacer(minLength: 20)

                    HStack(spacing: 4) {
                        Text("Already have an account?")
                            .font(.footnote)
                            .foregroundColor(Color(hex: t.authMuted))

                        Button(action: onNavigateToLogin) {
                            Text("Sign In")
                                .font(.footnote.bold())
                                .foregroundColor(AppColors.racingRed)
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
        label: LocalizedStringKey,
        placeholder: LocalizedStringKey,
        text: Binding<String>,
        keyboardType: UIKeyboardType
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: t.authMuted))

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
                            text.wrappedValue.isEmpty ? Color(hex: t.authDimBorder) : AppColors.racingRed,
                            lineWidth: 1.5
                        )
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private func passwordField(
        label: LocalizedStringKey,
        text: Binding<String>,
        isVisible: Bool,
        onToggle: @escaping () -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.caption)
                .foregroundColor(Color(hex: t.authMuted))

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
                        text.wrappedValue.isEmpty ? Color(hex: t.authDimBorder) : AppColors.racingRed,
                        lineWidth: 1.5
                    )
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    @ViewBuilder
    private func countryDropdown() -> some View {
        let code = viewModel.state.country
        let displayName = Locale.current.localizedString(forRegionCode: code) ?? code
        let hasSelection = !code.isEmpty

        VStack(alignment: .leading, spacing: 6) {
            Text("Country")
                .font(.caption)
                .foregroundColor(Color(hex: t.authMuted))

            Menu {
                ForEach(isoCountries, id: \.code) { item in
                    Button(action: { viewModel.send(.countryChanged(item.code)) }) {
                        if item.code == code {
                            Label(item.name, systemImage: "checkmark")
                        } else {
                            Text(item.name)
                        }
                    }
                }
            } label: {
                HStack {
                    Text(hasSelection ? displayName : String(localized: "Select country"))
                        .foregroundColor(hasSelection ? .white : Color(hex: t.authMuted).opacity(0.6))
                    Spacer()
                    Image(systemName: "chevron.down")
                        .font(.caption)
                        .foregroundColor(Color(hex: t.authMuted))
                }
                .padding()
                .background(Color.white.opacity(0.05))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(hasSelection ? AppColors.racingRed : Color(hex: t.authDimBorder), lineWidth: 1.5)
                )
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    private var isoCountries: [(name: String, code: String)] {
        Locale.isoRegionCodes
            .compactMap { code -> (name: String, code: String)? in
                guard let name = Locale.current.localizedString(forRegionCode: code), !name.isEmpty else { return nil }
                return (name: name, code: code)
            }
            .sorted { $0.name < $1.name }
    }
}

#Preview {
    SignUpView(onSignUpSuccess: {}, onNavigateToLogin: {})
}
