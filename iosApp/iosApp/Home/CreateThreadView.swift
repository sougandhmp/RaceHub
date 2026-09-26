import SwiftUI
import Shared
import Combine

struct CreateThreadView: View {

    let onThreadCreated: () -> Void

    @StateObject private var model = CreateThreadModel.createThread()
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                header

                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Spacer().frame(height: 8)

                        fieldLabel("TITLE")
                        textField(
                            placeholder: "What's on your mind?",
                            text: Binding(
                                get: { model.state.title },
                                set: { model.send(CreateThreadIntent.TitleChanged(title: $0)) }
                            )
                        )

                        fieldLabel("TAG")
                        categoryPicker

                        fieldLabel("CONTENT")
                        contentEditor

                        if let message = errorMessage {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundColor(AppColors.racingRed)
                                .padding(.top, 4)
                        }

                        submitButton
                            .padding(.top, 16)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
            }
        }
        .navigationBarHidden(true)
        // One-off effects from the shared ViewModel: shown once, never replayed.
        .onReceive(model.effects) { effect in
            if effect is CreateThreadEffect.ThreadCreated {
                onThreadCreated()
                dismiss()
            } else if effect is CreateThreadEffect.NotSignedIn {
                errorMessage = String(localized: "Sign in to post or comment.")
            } else if let failed = effect as? CreateThreadEffect.SubmitFailed {
                errorMessage = failed.error.userMessage
            }
        }
    }

    private var header: some View {
        HStack {
            Button(action: { dismiss() }) {
                Text("Cancel")
                    .foregroundColor(colors.mutedText)
                    .font(.system(size: 14))
            }
            .disabled(model.state.isSubmitting)

            Spacer()

            Text("New Thread")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(colors.primaryText)

            Spacer()

            Color.clear.frame(width: 64, height: 1)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
    }

    private func fieldLabel(_ text: LocalizedStringKey) -> some View {
        Text(text)
            .font(.system(size: 11, weight: .bold))
            .kerning(0.5)
            .foregroundColor(colors.mutedText)
    }

    private func textField(placeholder: LocalizedStringKey, text: Binding<String>) -> some View {
        TextField("", text: text, prompt: Text(placeholder).foregroundColor(colors.mutedText))
            .foregroundColor(colors.primaryText)
            .accentColor(AppColors.racingRed)
            .padding(12)
            .background(colors.card)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(colors.cardBorder, lineWidth: 1)
            )
            .disabled(model.state.isSubmitting)
    }

    private var categoryPicker: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(model.state.categories, id: \.self) { tag in
                    let isSelected = model.state.category == tag
                    Button(action: { model.send(CreateThreadIntent.CategoryChanged(category: tag)) }) {
                        Text(tag)
                            .font(.system(size: 14, weight: isSelected ? .bold : .medium))
                            .foregroundColor(isSelected ? .white : colors.primaryText)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 9)
                            .background(
                                Capsule()
                                    .fill(isSelected ? AppColors.racingRed : colors.card)
                            )
                            .overlay(
                                Capsule()
                                    .stroke(
                                        isSelected ? AppColors.racingRed : colors.cardBorder,
                                        lineWidth: 1
                                    )
                            )
                    }
                    .disabled(model.state.isSubmitting)
                }
            }
        }
    }

    private var contentEditor: some View {
        ZStack(alignment: .topLeading) {
            if model.state.content.isEmpty {
                Text("Share your thoughts…")
                    .foregroundColor(colors.mutedText)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
            }

            TextEditor(text: Binding(
                get: { model.state.content },
                set: { model.send(CreateThreadIntent.ContentChanged(content: $0)) }
            ))
            .scrollContentBackground(.hidden)
            .foregroundColor(colors.primaryText)
            .accentColor(AppColors.racingRed)
            .padding(8)
            .frame(height: 220)
        }
        .background(colors.card)
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
        .disabled(model.state.isSubmitting)
    }

    private var submitButton: some View {
        Button(action: { model.send(CreateThreadIntent.Submit.shared) }) {
            ZStack {
                if model.state.isSubmitting {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("Post Thread")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(model.state.canSubmit ? .white : colors.mutedText)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(model.state.canSubmit ? AppColors.racingRed : colors.card)
            .cornerRadius(12)
        }
        .disabled(!model.state.canSubmit)
    }
}

#Preview {
    CreateThreadView(onThreadCreated: {})
}
