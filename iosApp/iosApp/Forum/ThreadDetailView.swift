import SwiftUI
import Shared

/// Detail view for a single forum thread. Shows the full content, the comment
/// list, an inline composer for new comments, and a system share sheet.
struct ThreadDetailView: View {

    let thread: Shared.Thread

    @StateObject private var viewModel = ThreadDetailViewModel()
    @State private var showComments = false

    private var allComments: [Shared.ThreadComment] {
        thread.comments + viewModel.state.postedComments
    }

    private var shareText: String {
        "\(thread.title)\n\n\(thread.content)\n\n— shared from RaceHub"
    }

    var body: some View {
        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()

            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        threadHeader
                            .onAppear { viewModel.initLikes(thread.likes) }

                        HStack {
                            Text("COMMENTS · \(allComments.count)")
                                .font(.system(size: 11, weight: .bold))
                                .kerning(1)
                                .foregroundColor(Color(hex: "8E8E93"))
                            Spacer()
                            Image(systemName: showComments ? "chevron.up" : "chevron.down")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color(hex: "8E8E93"))
                        }
                        .padding(.top, 8)
                        .padding(.bottom, 4)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                showComments.toggle()
                            }
                        }

                        if showComments {
                            if allComments.isEmpty {
                                Text("No comments yet. Be the first to reply.")
                                    .font(.system(size: 13))
                                    .foregroundColor(Color(hex: "8E8E93"))
                                    .padding(.vertical, 12)
                                    .transition(.move(edge: .top).combined(with: .opacity))
                            } else {
                                ForEach(Array(allComments.enumerated()), id: \.offset) { _, comment in
                                    CommentCard(comment: comment)
                                        .transition(.move(edge: .top).combined(with: .opacity))
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                }

                CommentInputBar(
                    value: Binding(
                        get: { viewModel.state.commentInput },
                        set: { viewModel.send(.commentInputChanged($0)) }
                    ),
                    canSubmit: viewModel.state.canSubmit,
                    isSubmitting: viewModel.state.isSubmitting,
                    onSubmit: { viewModel.send(.submitComment(threadId: thread.id)) }
                )
            }
        }
        .navigationTitle("THREAD")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                ShareLink(item: shareText, subject: Text(thread.title)) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(.white)
                }
            }
        }
        .toolbarBackground(Color(hex: "0A0A0A"), for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .alert(
            "Error",
            isPresented: Binding(
                get: { viewModel.state.errorMessage != nil },
                set: { if !$0 { viewModel.send(.dismissError) } }
            ),
            actions: { Button("OK") { viewModel.send(.dismissError) } },
            message: { Text(viewModel.state.errorMessage ?? "") }
        )
    }

    private var threadHeader: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .center) {
                AuthorBadge(
                    initials: thread.author.avatar.isEmpty ? thread.author.username : thread.author.avatar,
                    size: 40
                )

                VStack(alignment: .leading, spacing: 2) {
                    Text(thread.author.username.isEmpty ? "Anonymous" : thread.author.username)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                    Text(formatTimestamp(thread.createdAt))
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "8E8E93"))
                }

                Spacer()

                Text(thread.category.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(Color(hex: "E63946"))
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(hex: "E63946").opacity(0.12))
                    .cornerRadius(6)
            }

            Text(thread.title)
                .font(.system(size: 22, weight: .heavy))
                .foregroundColor(.white)
                .fixedSize(horizontal: false, vertical: true)

            if !thread.content.isEmpty {
                Text(thread.content)
                    .font(.system(size: 15))
                    .foregroundColor(Color(hex: "E5E5E5"))
                    .lineSpacing(4)
                    .fixedSize(horizontal: false, vertical: true)
            }

            HStack(spacing: 16) {
                LikeButtonView(
                    likes: viewModel.state.likes,
                    isLiked: viewModel.state.isLiked,
                    isLiking: viewModel.state.isLiking,
                    onTap: { viewModel.send(.toggleLike(threadId: thread.id)) }
                )
                Button(action: {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        showComments.toggle()
                    }
                }) {
                    HStack(spacing: 4) {
                        Text("💬").font(.system(size: 14))
                        Text("\(thread.comments.count)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(showComments ? Color(hex: "E63946") : Color(hex: "8E8E93"))
                    }
                    .padding(.horizontal, 6)
                    .padding(.vertical, 4)
                    .background(showComments ? Color(hex: "E63946").opacity(0.1) : Color.clear)
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
                Spacer()
                if thread.bookmarked {
                    Text("★ Saved")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                }
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "161616"))
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }

}

private struct CommentCard: View {
    let comment: Shared.ThreadComment

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AuthorBadge(initials: comment.authorUsername, size: 32)

            VStack(alignment: .leading, spacing: 4) {
                Text(comment.authorUsername.isEmpty ? "Anonymous" : comment.authorUsername)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
                Text(comment.content)
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "D0D0D0"))
                    .fixedSize(horizontal: false, vertical: true)
            }

            Spacer()
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct CommentInputBar: View {
    @Binding var value: String
    let canSubmit: Bool
    let isSubmitting: Bool
    let onSubmit: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            TextField(
                "",
                text: $value,
                prompt: Text("Add a comment…").foregroundColor(Color(hex: "8E8E93"))
            )
            .foregroundColor(.white)
            .accentColor(Color(hex: "E63946"))
            .disabled(isSubmitting)
            .padding(12)
            .background(Color(hex: "161616"))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: "262626"), lineWidth: 1)
            )

            Button(action: onSubmit) {
                if isSubmitting {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "E63946")))
                        .frame(width: 44, height: 44)
                } else {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(canSubmit ? Color(hex: "E63946") : Color(hex: "8E8E93"))
                        .frame(width: 44, height: 44)
                }
            }
            .disabled(!canSubmit)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(hex: "0A0A0A"))
    }
}

private struct AuthorBadge: View {
    let initials: String
    let size: CGFloat

    var body: some View {
        ZStack {
            Circle().fill(Color(hex: "E63946"))
            Text(String(initials.prefix(2)).uppercased())
                .font(.system(size: size / 3, weight: .bold))
                .foregroundColor(.white)
        }
        .frame(width: size, height: size)
    }
}

private struct LikeButtonView: View {
    let likes: Int32
    let isLiked: Bool
    let isLiking: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 4) {
                Image(systemName: isLiked ? "heart.fill" : "heart")
                    .font(.system(size: 14))
                    .foregroundColor(isLiked ? Color(hex: "E63946") : Color(hex: "8E8E93"))
                Text("\(likes)")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(isLiked ? Color(hex: "E63946") : Color(hex: "8E8E93"))
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 4)
            .background(isLiked ? Color(hex: "E63946").opacity(0.1) : Color.clear)
            .cornerRadius(8)
        }
        .disabled(isLiking)
        .buttonStyle(.plain)
    }
}

private struct Metric: View {
    let icon: String
    let value: String

    var body: some View {
        HStack(spacing: 4) {
            Text(icon).font(.system(size: 14))
            Text(value)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color(hex: "8E8E93"))
        }
    }
}

private func formatTimestamp(_ createdAt: String) -> String {
    let datePart = createdAt.split(separator: "T").first.map(String.init) ?? createdAt
    let timePart = createdAt.split(separator: "T").last?.split(separator: ".").first?.prefix(5) ?? ""
    return timePart.isEmpty ? datePart : "\(datePart) · \(timePart)"
}
