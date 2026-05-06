import SwiftUI
import Combine

struct CreateThreadView: View {

    let onThreadCreated: () -> Void

    @StateObject private var viewModel = CreateThreadViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()

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
                            ),
                            singleLine: true
                        )

                        fieldLabel("CATEGORY")
                        textField(
                            placeholder: "Category",
                            text: Binding(
                                get: { viewModel.state.category },
                                set: { viewModel.send(.categoryChanged($0)) }
                            ),
                            singleLine: true
                        )

                        fieldLabel("CONTENT")
                        contentEditor

                        if let message = viewModel.state.errorMessage {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundColor(Color(hex: "E63946"))
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
                    .foregroundColor(Color(hex: "8E8E93"))
                    .font(.system(size: 14))
            }
            .disabled(viewModel.state.isSubmitting)

            Spacer()

            Text("New Thread")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.white)

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
            .foregroundColor(Color(hex: "8E8E93"))
    }

    private func textField(placeholder: String, text: Binding<String>, singleLine: Bool) -> some View {
        TextField("", text: text, prompt: Text(placeholder).foregroundColor(Color(hex: "8E8E93")))
            .foregroundColor(.white)
            .accentColor(Color(hex: "E63946"))
            .padding(12)
            .background(Color(hex: "161616"))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: "262626"), lineWidth: 1)
            )
            .disabled(viewModel.state.isSubmitting)
    }

    private var contentEditor: some View {
        ZStack(alignment: .topLeading) {
            if viewModel.state.content.isEmpty {
                Text("Share your thoughts…")
                    .foregroundColor(Color(hex: "8E8E93"))
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
            }

            TextEditor(text: Binding(
                get: { viewModel.state.content },
                set: { viewModel.send(.contentChanged($0)) }
            ))
            .scrollContentBackground(.hidden)
            .foregroundColor(.white)
            .accentColor(Color(hex: "E63946"))
            .padding(8)
            .frame(height: 220)
        }
        .background(Color(hex: "161616"))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(hex: "262626"), lineWidth: 1)
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
                        .foregroundColor(viewModel.state.canSubmit ? .white : Color(hex: "8E8E93"))
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(viewModel.state.canSubmit ? Color(hex: "E63946") : Color(hex: "161616"))
            .cornerRadius(12)
        }
        .disabled(!viewModel.state.canSubmit)
    }
}
