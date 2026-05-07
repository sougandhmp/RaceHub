import SwiftUI
import Shared

struct ThreadDetailView: View {

    let thread: Shared.Thread

    @StateObject private var viewModel = ThreadDetailViewModel()
    @State private var showComments = false
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
                        threadHeader
                            .onAppear { viewModel.initLikes(thread.likes) }

                        HStack {
                            Text("COMMENTS · \(allComments.count)")
                                .font(.system(size: 11, weight: .bold))
                                .kerning(1)
                                .foregroundColor(colors.mutedText)
                            Spacer()
                            Image(systemName: showComments ? "chevron.up" : "chevron.down")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(colors.mutedText)
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
                                    .foregroundColor(colors.mutedText)
                                    .padding(.vertical, 12)
                                    .transition(.move(edge: .top).combined(with: .opacity))
                            } else {
                                ForEach(Array(allComments.enumerated()), id: \.offset) { _, comment in
                                    CommentCard(comment: comment, colors: colors)
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
                    onSubmit: { viewModel.send(.submitComment(threadId: thread.id)) },
                    colors: colors
                )
            }
        }
        .navigationTitle("THREAD")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                ShareLink(item: shareText, subject: Text(thread.title)) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(colors.primaryText)
                }
            }
        }
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
                        .foregroundColor(colors.primaryText)
                    Text(formatTimestamp(thread.createdAt))
                        .font(.system(size: 12))
                        .foregroundColor(colors.mutedText)
                }

                Spacer()

                Text(thread.category.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(AppColors.racingRed)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AppColors.racingRed.opacity(0.12))
                    .cornerRadius(6)
            }

            Text(thread.title)
                .font(.system(size: 22, weight: .heavy))
                .foregroundColor(colors.primaryText)
                .fixedSize(horizontal: false, vertical: true)

            if !thread.content.isEmpty {
                Text(thread.content)
                    .font(.system(size: 15))
                    .foregroundColor(colors.primaryText)
                    .lineSpacing(4)
                    .fixedSize(horizontal: false, vertical: true)
            }

            HStack(spacing: 16) {
                LikeButtonView(
                    likes: viewModel.state.likes,
                    isLiked: viewModel.state.isLiked,
                    isLiking: viewModel.state.isLiking,
                    colors: colors,
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
                            .foregroundColor(showComments ? AppColors.racingRed : colors.mutedText)
                    }
                    .padding(.horizontal, 6)
                    .padding(.vertical, 4)
                    .background(showComments ? AppColors.racingRed.opacity(0.1) : Color.clear)
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
                Spacer()
                if thread.bookmarked {
                    Text("★ Saved")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(AppColors.racingRed)
                }
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

private struct CommentCard: View {
    let comment: Shared.ThreadComment
    let colors: AppColors

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AuthorBadge(initials: comment.authorUsername, size: 32)

            VStack(alignment: .leading, spacing: 4) {
                Text(comment.authorUsername.isEmpty ? "Anonymous" : comment.authorUsername)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(colors.primaryText)
                Text(comment.content)
                    .font(.system(size: 14))
                    .foregroundColor(colors.primaryText)
                    .fixedSize(horizontal: false, vertical: true)
            }

            Spacer()
        }
        .padding(14)
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
    let onSubmit: () -> Void
    let colors: AppColors

    var body: some View {
        HStack(spacing: 8) {
            TextField(
                "",
                text: $value,
                prompt: Text("Add a comment…").foregroundColor(colors.mutedText)
            )
            .foregroundColor(colors.primaryText)
            .accentColor(AppColors.racingRed)
            .disabled(isSubmitting)
            .padding(12)
            .background(colors.card)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(colors.cardBorder, lineWidth: 1)
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

private struct LikeButtonView: View {
    let likes: Int32
    let isLiked: Bool
    let isLiking: Bool
    let colors: AppColors
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 4) {
                Image(systemName: isLiked ? "heart.fill" : "heart")
                    .font(.system(size: 14))
                    .foregroundColor(isLiked ? AppColors.racingRed : colors.mutedText)
                Text("\(likes)")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(isLiked ? AppColors.racingRed : colors.mutedText)
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 4)
            .background(isLiked ? AppColors.racingRed.opacity(0.1) : Color.clear)
            .cornerRadius(8)
        }
        .disabled(isLiking)
        .buttonStyle(.plain)
    }
}

private func formatTimestamp(_ createdAt: String) -> String {
    let datePart = createdAt.split(separator: "T").first.map(String.init) ?? createdAt
    let timePart = createdAt.split(separator: "T").last?.split(separator: ".").first?.prefix(5) ?? ""
    return timePart.isEmpty ? datePart : "\(datePart) · \(timePart)"
}
