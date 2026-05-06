import SwiftUI
import Shared

struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()
    let onSignedOut: () -> Void

    var body: some View {
        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()

            if let user = viewModel.state.user {
                ScrollView {
                    VStack(spacing: 24) {
                        ProfileHeaderView(user: user, state: viewModel.state)
                        ProfileStatsView(user: user, state: viewModel.state)
                        ProfileDetailsView(user: user)
                        if !viewModel.state.recentThreadTitles.isEmpty {
                            ThreadTitleSection(heading: "RECENT POSTS", titles: viewModel.state.recentThreadTitles)
                        }
                        if !viewModel.state.savedThreadTitles.isEmpty {
                            ThreadTitleSection(heading: "SAVED", titles: viewModel.state.savedThreadTitles)
                        }
                        if viewModel.state.isLoadingProfile {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "E63946")))
                        }
                        if let message = viewModel.state.errorMessage {
                            Text(message)
                                .font(.system(size: 13))
                                .foregroundColor(Color(hex: "E63946"))
                        }
                        SignOutButton(
                            isSigningOut: viewModel.state.isSigningOut,
                            action: { viewModel.send(.signOut) }
                        )
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 24)
                    .padding(.bottom, 120)
                }
                .refreshable {
                    await viewModel.refresh()
                }
            } else {
                Text("Not signed in.")
                    .font(.system(size: 16))
                    .foregroundColor(Color(hex: "8E8E93"))
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

private struct ProfileHeaderView: View {
    let user: User
    let state: ProfileState

    var body: some View {
        let initials: String = {
            if !state.recentThreadTitles.isEmpty || state.savedCount > 0,
               let av = user.avatar, !av.isEmpty {
                return String(av.prefix(2)).uppercased()
            }
            return avatarInitials(for: user)
        }()
        let displayName = state.recentThreadTitles.isEmpty
            ? user.name
            : (user.username ?? user.name)
        let handle = user.username?.isEmpty == false ? user.username : nil

        return VStack(spacing: 12) {
            ZStack {
                Circle().fill(Color(hex: "E63946"))
                Text(initials)
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.white)
            }
            .frame(width: 96, height: 96)

            Text(displayName)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.white)

            if let handle = handle {
                Text("@\(handle)")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8E8E93"))
            }

            if let role = user.role, !role.isEmpty {
                Text(role.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .kerning(0.5)
                    .foregroundColor(Color(hex: "E63946"))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(Color(hex: "E63946").opacity(0.12))
                    .cornerRadius(6)
            }
        }
    }
}

private struct ProfileStatsView: View {
    let user: User
    let state: ProfileState

    var body: some View {
        HStack {
            StatCell(label: "POSTS", value: "\(state.postsCount > 0 ? state.postsCount : Int(user.postsCount))")
            Spacer()
            StatCell(label: "SAVED", value: state.isLoadingProfile ? "…" : "\(state.savedCount)")
            Spacer()
            StatCell(label: "COUNTRY", value: user.country?.uppercased() ?? "—")
        }
        .padding(.vertical, 16)
        .padding(.horizontal, 16)
        .frame(maxWidth: .infinity)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct StatCell: View {
    let label: String
    let value: String

    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.system(size: 18, weight: .heavy))
                .foregroundColor(.white)
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .kerning(1)
                .foregroundColor(Color(hex: "8E8E93"))
        }
        .frame(maxWidth: .infinity)
    }
}

private struct ThreadTitleSection: View {
    let heading: String
    let titles: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(heading)
                .font(.system(size: 10, weight: .bold))
                .kerning(1)
                .foregroundColor(Color(hex: "8E8E93"))
            ForEach(titles, id: \.self) { title in
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .lineLimit(2)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct ProfileDetailsView: View {
    let user: User

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            DetailRow(label: "Email", value: user.email)
            if let username = user.username, !username.isEmpty {
                DetailRow(label: "Username", value: username)
            }
            if let joined = user.joinedAt, !joined.isEmpty {
                DetailRow(label: "Joined", value: formatJoined(joined))
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct DetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(Color(hex: "8E8E93"))
                .frame(width: 96, alignment: .leading)
            Text(value)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(.white)
            Spacer()
        }
    }
}

private struct SignOutButton: View {
    let isSigningOut: Bool
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
            .background(Color(hex: "E63946"))
            .cornerRadius(12)
        }
        .disabled(isSigningOut)
    }
}

private func avatarInitials(for user: User) -> String {
    let source: String = {
        if let avatar = user.avatar, !avatar.isEmpty { return avatar }
        if !user.name.isEmpty { return user.name }
        return user.email
    }()
    let initials = source
        .trimmingCharacters(in: .whitespaces)
        .split(separator: " ")
        .prefix(2)
        .compactMap { $0.first }
        .map(String.init)
        .joined()
    return initials.uppercased()
}

// "2024-08-13T..." → "Aug 2024"; falls back to the raw string if parsing fails.
private func formatJoined(_ joinedAt: String?) -> String {
    guard let joined = joinedAt, !joined.isEmpty else { return "—" }
    let datePart = String(joined.split(separator: "T").first ?? Substring(joined))
    let parts = datePart.split(separator: "-")
    guard parts.count >= 2, let month = Int(parts[1]) else { return datePart }
    let months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    guard month >= 1, month <= months.count else { return datePart }
    return "\(months[month - 1]) \(parts[0])"
}
