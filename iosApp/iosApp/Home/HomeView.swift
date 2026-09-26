import SwiftUI
import Combine
import Shared

struct HomeView: View {

    let onSignedOut: () -> Void

    @StateObject private var homeModel = HomeModel.home()
    @StateObject private var raceModel = RaceModel.race()
    @StateObject private var forumModel = ForumModel.forum()
    @State private var path = NavigationPath()
    @Environment(\.colorScheme) private var colorScheme

    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        NavigationStack(path: $path) {
            ZStack {
                colors.background.ignoresSafeArea()

                VStack(spacing: 0) {
                    HomeHeaderView(colors: colors)

                    switch homeModel.state.selectedTab {
                    case HomeTab.race:
                        RaceView(
                            model: raceModel,
                            onViewAllSchedule: { path.append("schedule") },
                            onViewAllStandings: { path.append("standings") },
                            onViewRaceDetail: { race in path.append(race) }
                        )
                    case HomeTab.forum:
                        ForumView(
                            model: forumModel,
                            onCreateThread: { path.append("createThread") },
                            onThreadTap: { thread in path.append(thread) }
                        )
                    default: // HomeTab.profile (Kotlin enums bridge as classes, so Swift needs a default)
                        ProfileView(onSignedOut: onSignedOut)
                    }
                }
            }
            // The bottom bar reserves its own space via safeAreaInset, so the tab
            // content's scroll views inset automatically — no magic bottom padding.
            .safeAreaInset(edge: .bottom, spacing: 0) {
                HomeBottomBar(
                    selectedTab: homeModel.state.selectedTab,
                    colors: colors,
                    onTabSelected: { homeModel.send(HomeIntent.TabSelected(tab: $0)) }
                )
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "schedule" {
                    ScheduleView(
                        schedule: raceModel.state.raceSchedule
                    )
                } else if destination == "standings" {
                    StandingsView(
                        drivers: raceModel.state.driverStandings,
                        constructors: raceModel.state.constructorStandings
                    )
                } else if destination == "createThread" {
                    CreateThreadView(onThreadCreated: {
                        forumModel.send(ForumIntent.Refresh.shared)
                    })
                }
            }
            // Push the model itself rather than a string key plus a @State
            // "selected" value: the destination closure can run with a stale
            // snapshot of that state and render nothing.
            .navigationDestination(for: Race.self) { race in
                RaceDetailView(race: race, model: raceModel)
            }
            .navigationDestination(for: Shared.Thread.self) { thread in
                ThreadDetailView(thread: thread)
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
        .padding(.bottom, 12)
        .frame(maxWidth: .infinity)
        // Bleed the bar colour behind the home indicator; the items stay above it
        // because safeAreaInset places the bar at the safe-area edge.
        .background(
            colors.navBar.opacity(0.97)
                .ignoresSafeArea(edges: .bottom)
        )
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
        case HomeTab.race:  return "hexagon.fill"
        case HomeTab.forum: return "bubble.left.fill"
        default:            return "person.fill"
        }
    }
}

#Preview {
    HomeView(onSignedOut: {})
}
