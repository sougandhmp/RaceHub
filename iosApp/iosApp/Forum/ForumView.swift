import SwiftUI
import Shared

private let sortTabs: [(label: LocalizedStringKey, value: String)] = [
    ("Latest", "latest"),
    ("Most popular", "top"),
    ("Most commented", "commented")
]

private let categoryTabs: [(label: LocalizedStringKey, value: String?)] = [
    ("All", nil),
    ("General Discussion", "General Discussion"),
    ("Race Weekends", "Race Weekends"),
    ("Teams & Drivers", "Teams & Drivers"),
    ("Technical / Cars", "Technical / Cars")
]

struct ForumView: View {

    @ObservedObject var viewModel: ForumViewModel
    let onCreateThread: () -> Void
    let onThreadTap: (Shared.Thread) -> Void
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                FilterRow(
                    selectedSort: viewModel.state.selectedSort,
                    selectedCategory: viewModel.state.selectedCategory,
                    onSortSelected: { viewModel.send(.selectSort($0)) },
                    onCategorySelected: { viewModel.send(.selectCategory($0)) },
                    colors: colors
                )

                if viewModel.state.isLoading && viewModel.state.threads.isEmpty {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: AppColors.racingRed))
                        .frame(maxWidth: .infinity)
                    Spacer()
                } else if viewModel.state.threads.isEmpty {
                    Spacer()
                    VStack(spacing: 12) {
                        Text("No threads yet")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(colors.primaryText)
                        Text("Tap + to start the first conversation.")
                            .font(.system(size: 14))
                            .foregroundColor(colors.mutedText)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 20)
                    Spacer()
                } else {
                    ScrollView {
                        VStack(spacing: 12) {
                            ForEach(viewModel.state.threads, id: \.id) { thread in
                                ThreadCard(thread: thread, colors: colors)
                                    .contentShape(Rectangle())
                                    .onTapGesture { onThreadTap(thread) }
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)
                        .padding(.bottom, 100)
                    }
                    .refreshable {
                        await viewModel.refresh()
                    }
                }
            }
            .background(colors.background)

            Button(action: onCreateThread) {
                Image(systemName: "plus")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 56, height: 56)
                    .background(AppColors.racingRed)
                    .clipShape(Circle())
                    .shadow(color: Color.black.opacity(0.4), radius: 6, x: 0, y: 3)
            }
            .padding(.trailing, 20)
            .padding(.bottom, 110)
        }
    }
}

// MARK: - Filter Row

private struct FilterRow: View {
    let selectedSort: String
    let selectedCategory: String?
    let onSortSelected: (String) -> Void
    let onCategorySelected: (String?) -> Void
    let colors: AppColors

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(sortTabs, id: \.value) { tab in
                    FilterPill(
                        label: tab.label,
                        isSelected: selectedSort == tab.value,
                        colors: colors
                    ) {
                        onSortSelected(tab.value)
                    }
                }
                Spacer().frame(width: 4)
                ForEach(0..<categoryTabs.count, id: \.self) { i in
                    let tab = categoryTabs[i]
                    FilterPill(
                        label: tab.label,
                        isSelected: selectedCategory == tab.value,
                        colors: colors
                    ) {
                        onCategorySelected(tab.value)
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
        .background(colors.background)
    }
}

private struct FilterPill: View {
    let label: LocalizedStringKey
    let isSelected: Bool
    let colors: AppColors
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(isSelected ? .white : colors.primaryText)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(
                    Capsule()
                        .fill(isSelected ? AppColors.racingRed : Color.clear)
                )
                .overlay(
                    Capsule()
                        .stroke(isSelected ? Color.clear : colors.cardBorder, lineWidth: 1)
                )
        }
    }
}

// MARK: - Thread Card

private struct ThreadCard: View {
    let thread: Shared.Thread
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .center) {
                ZStack {
                    Circle().fill(AppColors.racingRed)
                    Text(thread.author.avatar.prefix(2).uppercased())
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                }
                .frame(width: 36, height: 36)

                VStack(alignment: .leading, spacing: 2) {
                    Text(thread.author.username)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(colors.primaryText)
                    Text(formatRelative(thread.createdAt))
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
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(colors.primaryText)
                .lineLimit(2)

            if let excerpt = thread.excerpt, !excerpt.isEmpty {
                Text(excerpt)
                    .font(.system(size: 14))
                    .foregroundColor(colors.mutedText)
                    .lineLimit(3)
            } else if !thread.content.isEmpty {
                let preview = thread.content.prefix(160)
                Text(String(preview) + (thread.content.count > 160 ? "…" : ""))
                    .font(.system(size: 14))
                    .foregroundColor(colors.mutedText)
                    .lineLimit(3)
            }

            HStack(spacing: 16) {
                HStack(spacing: 4) {
                    Text("❤️").font(.system(size: 14))
                    Text("\(thread.likes)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(colors.mutedText)
                }

                HStack(spacing: 4) {
                    Text("💬").font(.system(size: 14))
                    Text("\(thread.comments.count)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(colors.mutedText)
                }

                Spacer()

                if thread.bookmarked {
                    Text("★ Saved")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(AppColors.racingRed)
                }
            }
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

private func formatRelative(_ createdAt: String) -> String {
    let datePart = createdAt.split(separator: "T").first ?? Substring(createdAt)
    let timePart = createdAt.split(separator: "T").last?.split(separator: ".").first?.prefix(5) ?? ""
    return timePart.isEmpty ? String(datePart) : "\(datePart) · \(timePart)"
}

#Preview {
    ForumView(
        viewModel: ForumViewModel(),
        onCreateThread: {},
        onThreadTap: { _ in }
    )
}
