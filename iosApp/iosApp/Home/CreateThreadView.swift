import SwiftUI
import Combine

private let threadTags = [
    "General Discussion",
    "Race Weekends",
    "Teams & Drivers",
    "Technical / Cars"
]

struct CreateThreadView: View {

    let onThreadCreated: () -> Void

    @StateObject private var viewModel = CreateThreadViewModel()
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
                                get: { viewModel.state.title },
                                set: { viewModel.send(.titleChanged($0)) }
                            )
                        )

                        fieldLabel("TAG")
                        categoryPicker

                        fieldLabel("CONTENT")
                        contentEditor

                        if let message = viewModel.state.errorMessage {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundColor(AppColors.racingRed)
                                .padding(.top, 4)
                        }

                        submitButton
                            .padding(.top, 16)
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                }
            }
        }
        .navigationBarHidden(true)
        .onReceive(viewModel.effectPublisher) { effect in
            switch effect {
            case .threadCreated:
                onThreadCreated()
                dismiss()
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
            .disabled(viewModel.state.isSubmitting)

            Spacer()

            Text("New Thread")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(colors.primaryText)

            Spacer()

            Color.clear.frame(width: 64, height: 1)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }

    private func fieldLabel(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 11, weight: .bold))
            .kerning(0.5)
            .foregroundColor(colors.mutedText)
    }

    private func textField(placeholder: String, text: Binding<String>) -> some View {
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
            .disabled(viewModel.state.isSubmitting)
    }

    private var categoryPicker: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(threadTags, id: \.self) { tag in
                    let isSelected = viewModel.state.category == tag
                    Button(action: { viewModel.send(.categoryChanged(tag)) }) {
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
                    .disabled(viewModel.state.isSubmitting)
                }
            }
        }
    }

    private var contentEditor: some View {
        ZStack(alignment: .topLeading) {
            if viewModel.state.content.isEmpty {
                Text("Share your thoughts…")
                    .foregroundColor(colors.mutedText)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
            }

            TextEditor(text: Binding(
                get: { viewModel.state.content },
                set: { viewModel.send(.contentChanged($0)) }
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
        .disabled(viewModel.state.isSubmitting)
    }

    private var submitButton: some View {
        Button(action: { viewModel.send(.submit) }) {
            ZStack {
                if viewModel.state.isSubmitting {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("Post Thread")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(viewModel.state.canSubmit ? .white : colors.mutedText)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(viewModel.state.canSubmit ? AppColors.racingRed : colors.card)
            .cornerRadius(12)
        }
        .disabled(!viewModel.state.canSubmit)
    }
}

#Preview {
    CreateThreadView(onThreadCreated: {})
}
