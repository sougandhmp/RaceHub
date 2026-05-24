import SwiftUI
import Combine
import Shared

struct HomeView: View {

    let onSignedOut: () -> Void

    @StateObject private var viewModel = HomeViewModel()
    @StateObject private var raceViewModel = RaceViewModel()
    @StateObject private var forumViewModel = ForumViewModel()
    @State private var path = NavigationPath()
    @State private var selectedThread: Shared.Thread? = nil
    @State private var selectedRace: Race? = nil
    @Environment(\.colorScheme) private var colorScheme

    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        NavigationStack(path: $path) {
            ZStack(alignment: .bottom) {
                colors.background.ignoresSafeArea()

                VStack(spacing: 0) {
                    HomeHeaderView(colors: colors)

                    switch viewModel.state.selectedTab {
                    case .race:
                        RaceView(
                            viewModel: raceViewModel,
                            onViewAllSchedule: { path.append("schedule") },
                            onViewAllStandings: { path.append("standings") },
                            onViewRaceDetail: { race in
                                selectedRace = race
                                path.append("raceDetail")
                            }
                        )
                    case .forum:
                        ForumView(
                            viewModel: forumViewModel,
                            onCreateThread: { path.append("createThread") },
                            onThreadTap: { thread in
                                selectedThread = thread
                                path.append("threadDetail")
                            }
                        )
                    case .profile:
                        ProfileView(onSignedOut: onSignedOut)
                    }
                }

                HomeBottomBar(
                    selectedTab: viewModel.state.selectedTab,
                    colors: colors,
                    onTabSelected: { viewModel.send(.tabSelected($0)) }
                )
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "schedule" {
                    ScheduleView(
                        schedule: raceViewModel.state.raceSchedule
                    )
                } else if destination == "standings" {
                    StandingsView(
                        drivers: raceViewModel.state.driverStandings,
                        constructors: raceViewModel.state.constructorStandings
                    )
                } else if destination == "createThread" {
                    CreateThreadView(onThreadCreated: {
                        forumViewModel.send(.refresh)
                    })
                } else if destination == "threadDetail", let thread = selectedThread {
                    ThreadDetailView(thread: thread)
                } else if destination == "raceDetail", let race = selectedRace {
                    RaceDetailView(race: race)
                }
            }
        }
    }
}

// MARK: - Header

private struct HomeHeaderView: View {
    let colors: AppColors

    var body: some View {
        HStack {
            Text("Race Hub")
                .font(.system(size: 30, weight: .bold))
                .foregroundColor(colors.primaryText)
            Spacer()
            Image(systemName: "ellipsis")
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(colors.primaryText)
                .rotationEffect(.degrees(90))
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
        .padding(.bottom, 10)
        .background(colors.background)
    }
}

// MARK: - Bottom Bar

private struct HomeBottomBar: View {
    let selectedTab: HomeTab
    let colors: AppColors
    let onTabSelected: (HomeTab) -> Void

    var body: some View {
        HStack {
            Spacer()
            TabItem(tab: .race, label: "Race", isSelected: selectedTab == .race, colors: colors, action: onTabSelected)
            Spacer()
            TabItem(tab: .forum, label: "Forum", isSelected: selectedTab == .forum, colors: colors, action: onTabSelected)
            Spacer()
            TabItem(tab: .profile, label: "Profile", isSelected: selectedTab == .profile, colors: colors, action: onTabSelected)
            Spacer()
        }
        .padding(.top, 12)
        .padding(.bottom, 34)
        .background(colors.navBar.opacity(0.97))
        .overlay(
            Rectangle()
                .fill(colors.cardBorder)
                .frame(height: 1),
            alignment: .top
        )
    }
}

private struct TabItem: View {
    let tab: HomeTab
    let label: LocalizedStringKey
    let isSelected: Bool
    let colors: AppColors
    let action: (HomeTab) -> Void

    var body: some View {
        Button(action: { action(tab) }) {
            VStack(spacing: 4) {
                Image(systemName: iconName(for: tab))
                    .font(.system(size: 22))
                Text(label)
                    .font(.system(size: 12, weight: isSelected ? .bold : .medium))
            }
            .foregroundColor(isSelected ? AppColors.racingRed : colors.mutedText)
        }
    }

    private func iconName(for tab: HomeTab) -> String {
        switch tab {
        case .race:    return "hexagon.fill"
        case .forum:   return "bubble.left.fill"
        case .profile: return "person.fill"
        }
    }
}

#Preview {
    HomeView(onSignedOut: {})
}
