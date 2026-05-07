import SwiftUI
import Shared

struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()
    @EnvironmentObject private var themeManager: ThemeManager
    @Environment(\.colorScheme) private var colorScheme
    let onSignedOut: () -> Void

    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            if let user = viewModel.state.user {
                ScrollView {
                    VStack(spacing: 24) {
                        ProfileHeaderView(user: user, state: viewModel.state, colors: colors)
                        ProfileStatsView(user: user, state: viewModel.state, colors: colors)
                        ProfileDetailsView(user: user, colors: colors)
                        ThemeToggleView(themeManager: themeManager, colors: colors)
                        if !viewModel.state.recentThreadTitles.isEmpty {
                            ThreadTitleSection(
                                heading: "RECENT POSTS",
                                titles: viewModel.state.recentThreadTitles,
                                colors: colors
                            )
                        }
                        if !viewModel.state.savedThreadTitles.isEmpty {
                            ThreadTitleSection(
                                heading: "SAVED",
                                titles: viewModel.state.savedThreadTitles,
                                colors: colors
                            )
                        }
                        if viewModel.state.isLoadingProfile {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: AppColors.racingRed))
                        }
                        if let message = viewModel.state.errorMessage {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundColor(AppColors.racingRed)
                        }
                        SignOutButton(
                            isSigningOut: viewModel.state.isSigningOut,
                            colors: colors,
                            action: { viewModel.send(.signOut) }
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 24)
                    .padding(.bottom, 120)
                }
                .refreshable {
                    await viewModel.refresh()
                }
            } else {
                Text("Not signed in.")
                    .font(.system(size: 16))
                    .foregroundColor(colors.mutedText)
            }
        }
        .onReceive(viewModel.effectPublisher) { effect in
            switch effect {
            case .signedOut:
                onSignedOut()
            }
        }
    }
}

// MARK: - Theme Toggle

private struct ThemeToggleView: View {
    @ObservedObject var themeManager: ThemeManager
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("APPEARANCE")
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(colors.mutedText)
                .kerning(1)

            HStack(spacing: 0) {
                ThemePill(
                    label: "Light",
                    isSelected: !themeManager.isDarkMode,
                    colors: colors,
                    action: { themeManager.isDarkMode = false }
                )
                ThemePill(
                    label: "Dark",
                    isSelected: themeManager.isDarkMode,
                    colors: colors,
                    action: { themeManager.isDarkMode = true }
                )
            }
            .padding(4)
            .background(colors.card)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(colors.cardBorder, lineWidth: 1)
            )
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct ThemePill: View {
    let label: String
    let isSelected: Bool
    let colors: AppColors
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 14, weight: isSelected ? .bold : .medium))
                .foregroundColor(isSelected ? .white : colors.mutedText)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(isSelected ? AppColors.racingRed : Color.clear)
                .cornerRadius(8)
        }
    }
}

// MARK: - Profile Header

private struct ProfileHeaderView: View {
    let user: User
    let state: ProfileState
    let colors: AppColors

    var body: some View {
        let initials: String = {
            if let av = user.avatar, !av.isEmpty {
                return String(av.prefix(2)).uppercased()
            }
            return avatarInitials(for: user)
        }()
        let displayName = user.username?.isEmpty == false ? (user.username ?? user.name) : user.name
        let handle = user.username?.isEmpty == false ? user.username : nil

        return VStack(spacing: 12) {
            ZStack {
                Circle().fill(AppColors.racingRed)
                Text(initials)
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.white)
            }
            .frame(width: 96, height: 96)

            Text(displayName)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(colors.primaryText)

            if let handle = handle {
                Text("@\(handle)")
                    .font(.system(size: 14))
                    .foregroundColor(colors.mutedText)
            }

            if let role = user.role, !role.isEmpty {
                Text(role.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .kerning(0.5)
                    .foregroundColor(AppColors.racingRed)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(AppColors.racingRed.opacity(0.12))
                    .cornerRadius(6)
            }
        }
    }
}

// MARK: - Stats

private struct ProfileStatsView: View {
    let user: User
    let state: ProfileState
    let colors: AppColors

    var body: some View {
        HStack {
            StatCell(label: "POSTS", value: "\(state.postsCount > 0 ? state.postsCount : Int(user.postsCount))", colors: colors)
            Spacer()
            StatCell(label: "SAVED", value: state.isLoadingProfile ? "…" : "\(state.savedCount)", colors: colors)
            Spacer()
            StatCell(label: "COUNTRY", value: user.country?.uppercased() ?? "—", colors: colors)
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 16)
        .frame(maxWidth: .infinity)
        .background(colors.card)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct StatCell: View {
    let label: String
    let value: String
    let colors: AppColors

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.system(size: 18, weight: .heavy))
                .foregroundColor(colors.primaryText)
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .kerning(1)
                .foregroundColor(colors.mutedText)
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Thread Titles

private struct ThreadTitleSection: View {
    let heading: String
    let titles: [String]
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(heading)
                .font(.system(size: 10, weight: .bold))
                .kerning(1)
                .foregroundColor(colors.mutedText)
            ForEach(titles, id: \.self) { title in
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(colors.primaryText)
                    .lineLimit(2)
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

// MARK: - Details

private struct ProfileDetailsView: View {
    let user: User
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            DetailRow(label: "Email", value: user.email, colors: colors)
            if let username = user.username, !username.isEmpty {
                DetailRow(label: "Username", value: username, colors: colors)
            }
            if let joined = user.joinedAt, !joined.isEmpty {
                DetailRow(label: "Joined", value: formatJoined(joined), colors: colors)
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

private struct DetailRow: View {
    let label: String
    let value: String
    let colors: AppColors

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(colors.mutedText)
                .frame(width: 96, alignment: .leading)
            Text(value)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(colors.primaryText)
            Spacer()
        }
    }
}

// MARK: - Sign Out

private struct SignOutButton: View {
    let isSigningOut: Bool
    let colors: AppColors
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isSigningOut {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("Sign Out")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(AppColors.racingRed)
            .cornerRadius(12)
        }
        .disabled(isSigningOut)
    }
}

// MARK: - Helpers

private func avatarInitials(for user: User) -> String {
    let source: String = {
        if let avatar = user.avatar, !avatar.isEmpty { return avatar }
        if !user.name.isEmpty { return user.name }
        return user.email
    }()
    return source.trimmingCharacters(in: .whitespaces)
        .split(separator: " ").prefix(2)
        .compactMap { $0.first }
        .map(String.init).joined()
        .uppercased()
}

private func formatJoined(_ joinedAt: String?) -> String {
    guard let joined = joinedAt, !joined.isEmpty else { return "—" }
    let datePart = String(joined.split(separator: "T").first ?? Substring(joined))
    let parts = datePart.split(separator: "-")
    guard parts.count >= 2, let month = Int(parts[1]) else { return datePart }
    let months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]
    guard month >= 1, month <= months.count else { return datePart }
    return "\(months[month - 1]) \(parts[0])"
}
