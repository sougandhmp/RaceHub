import SwiftUI
import Shared

/// Forum tab content. Owns its own `ForumViewModel` by default but accepts an
/// injected one so the parent (HomeView) can refresh it after creating a thread.
struct ForumView: View {

    @ObservedObject var viewModel: ForumViewModel
    let onCreateThread: () -> Void

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            if viewModel.state.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "E63946")))
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.state.threads.isEmpty {
                VStack(spacing: 12) {
                    Spacer()
                    Text("No threads yet")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                    Text("Tap + to start the first conversation.")
                        .font(.system(size: 14))
                        .foregroundColor(Color(hex: "8E8E93"))
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding(.horizontal, 24)
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(viewModel.state.threads, id: \.id) { thread in
                            ThreadCard(thread: thread)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.vertical, 16)
                    .padding(.bottom, 100)
                }
            }

            Button(action: onCreateThread) {
                Image(systemName: "plus")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(Color(hex: "E63946"))
                    .clipShape(Circle())
                    .shadow(color: Color.black.opacity(0.4), radius: 6, x: 0, y: 3)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 110)
        }
    }
}

private struct ThreadCard: View {
    let thread: Shared.Thread

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .center) {
                ZStack {
                    Circle().fill(Color(hex: "E63946"))
                    Text(thread.author.avatar.prefix(2).uppercased())
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                }
                .frame(width: 36, height: 36)

                VStack(alignment: .leading, spacing: 2) {
                    Text(thread.author.username)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                    Text(formatRelative(thread.createdAt))
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
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
                .lineLimit(2)

            if let excerpt = thread.excerpt, !excerpt.isEmpty {
                Text(excerpt)
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8E8E93"))
                    .lineLimit(3)
            } else if !thread.content.isEmpty {
                let preview = thread.content.prefix(160)
                Text(String(preview) + (thread.content.count > 160 ? "…" : ""))
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8E8E93"))
                    .lineLimit(3)
            }

            HStack(spacing: 16) {
                HStack(spacing: 4) {
                    Text("❤️").font(.system(size: 14))
                    Text("\(thread.likes)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(Color(hex: "8E8E93"))
                }

                HStack(spacing: 4) {
                    Text("💬").font(.system(size: 14))
                    Text("\(thread.comments.count)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(Color(hex: "8E8E93"))
                }

                Spacer()

                if thread.bookmarked {
                    Text("★ Saved")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "161616"))
        .cornerRadius(20)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

// Best-effort formatter for ISO-ish timestamps coming from the GraphQL response.
// Falls back to the date portion if anything goes wrong.
private func formatRelative(_ createdAt: String) -> String {
    let datePart = createdAt.split(separator: "T").first ?? Substring(createdAt)
    let timePart = createdAt.split(separator: "T").last?.split(separator: ".").first?.prefix(5) ?? ""
    return timePart.isEmpty ? String(datePart) : "\(datePart) · \(timePart)"
}
