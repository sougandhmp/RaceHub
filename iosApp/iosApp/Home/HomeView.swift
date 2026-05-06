import SwiftUI
import Combine
import Shared

/// Root of the Home shell. Owns navigation state and the per-tab ViewModels;
/// each tab's content lives in its own feature view (`RaceView`, `ForumView`,
/// `ProfileView`). The Race and Forum ViewModels are kept here so the same
/// instances are shared with the Schedule/Standings/CreateThread destinations.
struct HomeView: View {

    @StateObject private var viewModel = HomeViewModel()
    @StateObject private var raceViewModel = RaceViewModel()
    @StateObject private var forumViewModel = ForumViewModel()
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            ZStack(alignment: .bottom) {
                Color(hex: "0A0A0A").ignoresSafeArea()

                VStack(spacing: 0) {
                    HomeHeaderView()

                    switch viewModel.state.selectedTab {
                    case .race:
                        RaceView(
                            viewModel: raceViewModel,
                            onViewAllSchedule: { path.append("schedule") },
                            onViewAllStandings: { path.append("standings") }
                        )
                    case .forum:
                        ForumView(
                            viewModel: forumViewModel,
                            onCreateThread: { path.append("createThread") }
                        )
                    case .profile:
                        ProfileView()
                    }
                }

                HomeBottomBar(selectedTab: viewModel.state.selectedTab) { tab in
                    viewModel.send(.tabSelected(tab))
                }
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "schedule" {
                    ScheduleView(schedule: raceViewModel.state.raceSchedule)
                } else if destination == "standings" {
                    StandingsView(
                        drivers: raceViewModel.state.driverStandings,
                        constructors: raceViewModel.state.constructorStandings
                    )
                } else if destination == "createThread" {
                    CreateThreadView(onThreadCreated: {
                        forumViewModel.send(.refresh)
                    })
                }
            }
        }
    }
}

// MARK: - Header

private struct HomeHeaderView: View {
    var body: some View {
        HStack {
            Text("Race Hub")
                .font(.system(size: 32, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 24)
        .padding(.top, 20)
        .padding(.bottom, 10)
        .background(Color(hex: "0A0A0A"))
    }
}

// MARK: - Bottom Bar

private struct HomeBottomBar: View {
    let selectedTab: HomeTab
    let onTabSelected: (HomeTab) -> Void

    var body: some View {
        HStack {
            Spacer()
            TabItem(tab: .race, label: "Race", isSelected: selectedTab == .race, action: onTabSelected)
            Spacer()
            TabItem(tab: .forum, label: "Forum", isSelected: selectedTab == .forum, action: onTabSelected)
            Spacer()
            TabItem(tab: .profile, label: "Profile", isSelected: selectedTab == .profile, action: onTabSelected)
            Spacer()
        }
        .padding(.top, 12)
        .padding(.bottom, 34) // Safe area padding
        .background(Color(hex: "0A0A0A").opacity(0.95))
        .overlay(Rectangle().fill(Color(hex: "262626")).frame(height: 1), alignment: .top)
    }
}

private struct TabItem: View {
    let tab: HomeTab
    let label: String
    let isSelected: Bool
    let action: (HomeTab) -> Void

    var body: some View {
        Button(action: { action(tab) }) {
            VStack(spacing: 4) {
                Image(systemName: iconName(for: tab))
                    .font(.system(size: 24))
                Text(label)
                    .font(.system(size: 12, weight: isSelected ? .bold : .medium))
            }
            .foregroundColor(isSelected ? Color(hex: "E63946") : Color(hex: "8E8E93"))
        }
    }

    private func iconName(for tab: HomeTab) -> String {
        switch tab {
        case .race: return "hexagon.fill"
        case .forum: return "bubble.left.fill"
        case .profile: return "person.fill"
        }
    }
}

#Preview {
    HomeView()
}
