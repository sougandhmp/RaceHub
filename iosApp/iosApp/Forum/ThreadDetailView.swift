import SwiftUI
import Shared

struct ThreadDetailView: View {

    let thread: Shared.Thread

    @StateObject private var viewModel = ThreadDetailViewModel()
    @FocusState private var commentFieldFocused: Bool
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    private var allComments: [Shared.ThreadComment] {
        thread.comments + viewModel.state.postedComments
    }

    private var shareText: String {
        "\(thread.title)\n\n\(thread.content)\n\n— shared from RaceHub"
    }

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        ThreadPostCard(
                            thread: thread,
                            likes: viewModel.state.likes,
                            isLiked: viewModel.state.isLiked,
                            isLiking: viewModel.state.isLiking,
                            colors: colors,
                            onLikeClick: { viewModel.send(.toggleLike(threadId: thread.id)) },
                            onReplyClick: { commentFieldFocused = true },
                            shareText: shareText
                        )
                        .onAppear { viewModel.initLikes(thread.likes) }

                        Text(allComments.count == 1 ? "1 Reply" : "\(allComments.count) Replies")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(colors.mutedText)
                            .padding(.top, 4)
                            .padding(.bottom, 4)

                        if allComments.isEmpty {
                            Text("No replies yet. Be the first!")
                                .font(.system(size: 13))
                                .foregroundColor(colors.mutedText)
                                .padding(.vertical, 8)
                        } else {
                            ForEach(Array(allComments.enumerated()), id: \.offset) { _, comment in
                                CommentCard(comment: comment, colors: colors)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }

                CommentInputBar(
                    value: Binding(
                        get: { viewModel.state.commentInput },
                        set: { viewModel.send(.commentInputChanged($0)) }
                    ),
                    canSubmit: viewModel.state.canSubmit,
                    isSubmitting: viewModel.state.isSubmitting,
                    isFocused: $commentFieldFocused,
                    onSubmit: { viewModel.send(.submitComment(threadId: thread.id)) },
                    colors: colors
                )
            }
        }
        .navigationTitle("Thread")
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(colors.background, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(colors.isDark ? .dark : .light, for: .navigationBar)
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
}

private struct ThreadPostCard: View {
    let thread: Shared.Thread
    let likes: Int32
    let isLiked: Bool
    let isLiking: Bool
    let colors: AppColors
    let onLikeClick: () -> Void
    let onReplyClick: () -> Void
    let shareText: String

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Category · date
            Text("\(thread.category) · \(formatTimestamp(thread.createdAt))")
                .font(.system(size: 12))
                .foregroundColor(colors.mutedText)

            Spacer().frame(height: 10)

            // Title
            Text(thread.title)
                .font(.system(size: 22, weight: .heavy))
                .foregroundColor(colors.primaryText)
                .fixedSize(horizontal: false, vertical: true)

            Spacer().frame(height: 14)

            // Author row
            HStack(spacing: 10) {
                AuthorBadge(
                    initials: thread.author.avatar.isEmpty ? thread.author.username : thread.author.avatar,
                    size: 40
                )
                VStack(alignment: .leading, spacing: 2) {
                    Text(thread.author.username.isEmpty ? "Anonymous" : thread.author.username)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(colors.primaryText)
                    Text("Original poster")
                        .font(.system(size: 12))
                        .foregroundColor(colors.mutedText)
                }
            }

            if !thread.content.isEmpty {
                Spacer().frame(height: 14)
                Text(thread.content)
                    .font(.system(size: 15))
                    .foregroundColor(colors.primaryText)
                    .lineSpacing(4)
                    .fixedSize(horizontal: false, vertical: true)
            }

            Spacer().frame(height: 16)

            Divider().background(colors.cardBorder)

            Spacer().frame(height: 12)

            // Action row
            HStack(spacing: 8) {
                ActionPill(
                    systemImage: isLiked ? "heart.fill" : "heart",
                    label: "\(likes)",
                    active: isLiked,
                    enabled: !isLiking,
                    colors: colors,
                    action: onLikeClick
                )
                ActionPill(
                    systemImage: "arrowshape.turn.up.left",
                    label: "Reply",
                    active: false,
                    enabled: true,
                    colors: colors,
                    action: onReplyClick
                )
                ShareLink(item: shareText, subject: Text(thread.title)) {
                    HStack(spacing: 5) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(colors.mutedText)
                        Text("Share")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(colors.mutedText)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.clear)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(colors.cardBorder, lineWidth: 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.card)
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct ActionPill: View {
    let systemImage: String
    let label: String
    let active: Bool
    let enabled: Bool
    let colors: AppColors
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 5) {
                Image(systemName: systemImage)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(active ? AppColors.racingRed : colors.mutedText)
                Text(label)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(active ? AppColors.racingRed : colors.mutedText)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(active ? AppColors.racingRed.opacity(0.10) : Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(active ? AppColors.racingRed.opacity(0.4) : colors.cardBorder, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .disabled(!enabled)
        .buttonStyle(.plain)
    }
}

private struct CommentCard: View {
    let comment: Shared.ThreadComment
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 10) {
                AuthorBadge(
                    initials: comment.authorUsername.isEmpty ? "?" : comment.authorUsername,
                    size: 30
                )
                VStack(alignment: .leading, spacing: 1) {
                    Text(comment.authorUsername.isEmpty ? "Anonymous" : comment.authorUsername)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(colors.primaryText)
                }
                Spacer()
                Text("just now")
                    .font(.system(size: 11))
                    .foregroundColor(colors.mutedText)
            }
            Text(comment.content)
                .font(.system(size: 14))
                .foregroundColor(colors.primaryText)
                .lineSpacing(4)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.card)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct CommentInputBar: View {
    @Binding var value: String
    let canSubmit: Bool
    let isSubmitting: Bool
    @FocusState.Binding var isFocused: Bool
    let onSubmit: () -> Void
    let colors: AppColors

    var body: some View {
        HStack(spacing: 8) {
            TextField(
                "",
                text: $value,
                prompt: Text("Add a reply…").foregroundColor(colors.mutedText)
            )
            .foregroundColor(colors.primaryText)
            .accentColor(AppColors.racingRed)
            .disabled(isSubmitting)
            .focused($isFocused)
            .padding(12)
            .background(colors.card)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(isFocused ? AppColors.racingRed : colors.cardBorder, lineWidth: 1)
            )

            Button(action: onSubmit) {
                if isSubmitting {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.racingRed))
                        .frame(width: 44, height: 44)
                } else {
                    Image(systemName: "paperplane.fill")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(canSubmit ? AppColors.racingRed : colors.mutedText)
                        .frame(width: 44, height: 44)
                }
            }
            .disabled(!canSubmit)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(colors.background)
    }
}

private struct AuthorBadge: View {
    let initials: String
    let size: CGFloat

    var body: some View {
        ZStack {
            Circle().fill(AppColors.racingRed)
            Text(String(initials.prefix(2)).uppercased())
                .font(.system(size: size / 3, weight: .bold))
                .foregroundColor(.white)
        }
        .frame(width: size, height: size)
    }
}

private func formatTimestamp(_ createdAt: String) -> String {
    let datePart = createdAt.split(separator: "T").first.map(String.init) ?? createdAt
    let timePart = createdAt.split(separator: "T").last?.split(separator: ".").first?.prefix(5) ?? ""
    return timePart.isEmpty ? datePart : "\(datePart) · \(timePart)"
}

#Preview {
    let sampleThread = Shared.Thread(
        id: "t1",
        title: "Max's pace looks incredible this weekend",
        category: "Race Weekends",
        author: ThreadAuthor(username: "f1fan", avatar: "F1"),
        excerpt: "Did you catch Q2?",
        content: "The car setup looks completely different from last race. The rear is much more planted.",
        createdAt: "2025-03-16T10:00:00Z",
        likes: 42,
        bookmarked: false,
        comments: [ThreadComment(content: "Totally agree!", authorUsername: "speedfreak")]
    )
    return NavigationStack {
        ThreadDetailView(thread: sampleThread)
    }
}
