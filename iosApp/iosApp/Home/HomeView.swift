import SwiftUI
import Combine
import Shared

// ── Entry point ───────────────────────────────────────────────────────────────

struct HomeView: View {

    @StateObject private var viewModel = HomeViewModel()
    @State private var path = NavigationPath()

    var body: some View {
        NavigationStack(path: $path) {
            ZStack(alignment: .bottom) {
                Color(hex: "0A0A0A").ignoresSafeArea()

                VStack(spacing: 0) {
                    HomeHeaderView()

                    ScrollView {
                        VStack(spacing: 24) {
                            if viewModel.state.isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "E63946")))
                                    .padding(.top, 40)
                            } else {
                                switch viewModel.state.selectedTab {
                                case .race:
                                    RaceTabView(state: viewModel.state, path: $path)
                                case .forum:
                                    PlaceholderView(title: "Forum")
                                case .profile:
                                    PlaceholderView(title: "Profile")
                                }
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 20)
                        .padding(.bottom, 100) // Space for bottom nav
                    }
                }

                HomeBottomBar(selectedTab: viewModel.state.selectedTab) { tab in
                    viewModel.send(.tabSelected(tab))
                }
            }
            .navigationDestination(for: String.self) { destination in
                if destination == "schedule" {
                    ScheduleView(schedule: viewModel.state.raceSchedule)
                } else if destination == "standings" {
                    StandingsView(standings: viewModel.state.driverStandings)
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

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

// ── Race Tab Content ─────────────────────────────────────────────────────────

private struct RaceTabView: View {
    let state: HomeState
    @Binding var path: NavigationPath

    var body: some View {
        VStack(spacing: 24) {
            NextRaceSection(
                race: state.raceSchedule.first { !$0.isCompleted },
                onViewAll: { path.append("schedule") }
            )
            StandingsTabSection(
                drivers: Array(state.driverStandings.prefix(5)),
                constructors: Array(state.constructorStandings.prefix(5)),
                onViewAll: { path.append("standings") }
            )
            LatestResultsSection(
                results: Array(state.raceSchedule.filter { $0.isCompleted }.suffix(3).reversed())
            )
            LatestThreadSection(thread: state.trendingThreads.first)
        }
    }
}

private struct NextRaceSection: View {
    let race: Race?
    let onViewAll: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("NEXT RACE")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(hex: "8E8E93"))
                    .kerning(0.5)
                Spacer()
                if let race = race, let days = race.daysRemaining {
                    Text("R\(Int(race.round)) · \(Int(truncating: days)) DAYS")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                        .padding(.trailing, 8)
                }
                Button(action: onViewAll) {
                    Text("VIEW ALL")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                HStack(alignment: .center, spacing: 8) {
                    Text(race?.name ?? "Canadian GP")
                        .font(.system(size: 28, weight: .heavy))
                        .foregroundColor(.white)
                    Text(race?.countryFlag ?? "🇨🇦")
                        .font(.system(size: 24))
                }

                Text(race?.date ?? "Sun May 24 · 8:00 PM UTC · Montreal")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8E8E93"))
                    .padding(.vertical, 4)

                HStack(spacing: 4) {
                    Text("Forecast").foregroundColor(Color(hex: "8E8E93"))
                    Text("22° · 30% rain").foregroundColor(.white)
                    Text("Length").foregroundColor(Color(hex: "8E8E93")).padding(.leading, 4)
                    Text("4.361 km").foregroundColor(.white)
                }
                .font(.system(size: 14))
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
}

private enum StandingsTab: String, CaseIterable {
    case drivers, teams
    var label: String { self == .drivers ? "Driver Standings" : "Constructor Standings" }
}

private let HeaderBgHex = "2A1116"
private let RowAltBgHex = "1B0E11"

private struct StandingsTabSection: View {
    let drivers: [DriverStanding]
    let constructors: [ConstructorStanding]
    let onViewAll: () -> Void

    @State private var selected: StandingsTab = .drivers

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(selected == .drivers ? "Driver Standings" : "Constructor Standings")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(hex: "8E8E93"))
                Spacer()
                Button(action: onViewAll) {
                    Text("VIEW ALL")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                }
            }

            HStack(spacing: 4) {
                ForEach(StandingsTab.allCases, id: \.self) { tab in
                    Button(action: { selected = tab }) {
                        Text(tab.label)
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(selected == tab ? .white : Color(hex: "8E8E93"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(selected == tab ? Color(hex: "E63946") : Color.clear)
                            .cornerRadius(8)
                    }
                }
            }
            .padding(4)
            .background(Color(hex: "161616"))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "262626"), lineWidth: 1)
            )

            switch selected {
            case .drivers:
                let displayDrivers = drivers.isEmpty
                    ? [
                        DriverStanding(position: 1, driverName: "Kimi Antonelli", team: "Mercedes", points: 72, wins: 2),
                        DriverStanding(position: 2, driverName: "George Russell", team: "Mercedes", points: 63, wins: 1),
                        DriverStanding(position: 3, driverName: "Charles Leclerc", team: "Ferrari", points: 49, wins: 0)
                      ]
                    : drivers
                DriverStandingsTable(drivers: displayDrivers)
            case .teams:
                let displayConstructors = constructors.isEmpty
                    ? [
                        ConstructorStanding(position: 1, name: "Mercedes", points: 135, wins: 3),
                        ConstructorStanding(position: 2, name: "Ferrari", points: 90, wins: 0),
                        ConstructorStanding(position: 3, name: "McLaren", points: 46, wins: 0)
                      ]
                    : constructors
                ConstructorStandingsTable(constructors: displayConstructors)
            }
        }
    }
}

private struct DriverStandingsTable: View {
    let drivers: [DriverStanding]

    var body: some View {
        VStack(spacing: 0) {
            StandingsTableHeader(columns: ["POS", "NAME", "TEAM", "POINTS", "WINS"])
            ForEach(Array(drivers.enumerated()), id: \.element.position) { index, standing in
                StandingsTableRow(
                    cells: [
                        "\(Int(standing.position))",
                        standing.driverName,
                        standing.team,
                        "\(Int(standing.points))",
                        "\(Int(standing.wins))"
                    ],
                    isAlternate: index % 2 == 1
                )
            }
        }
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct ConstructorStandingsTable: View {
    let constructors: [ConstructorStanding]

    var body: some View {
        VStack(spacing: 0) {
            StandingsTableHeader(columns: ["POS", "TEAM", "POINTS", "WINS"])
            ForEach(Array(constructors.enumerated()), id: \.element.position) { index, standing in
                StandingsTableRow(
                    cells: [
                        "\(Int(standing.position))",
                        standing.name,
                        "\(Int(standing.points))",
                        "\(Int(standing.wins))"
                    ],
                    isAlternate: index % 2 == 1
                )
            }
        }
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct StandingsTableHeader: View {
    let columns: [String]

    var body: some View {
        GeometryReader { geo in
            HStack(spacing: 0) {
                ForEach(Array(columns.enumerated()), id: \.offset) { index, label in
                    Text(label)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(Color(hex: "8E8E93"))
                        .kerning(0.5)
                        .frame(width: columnWidth(total: columns.count, index: index, available: geo.size.width - 24), alignment: .leading)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .frame(width: geo.size.width)
            .background(Color(hex: HeaderBgHex))
        }
        .frame(height: 36)
    }
}

private struct StandingsTableRow: View {
    let cells: [String]
    let isAlternate: Bool

    var body: some View {
        GeometryReader { geo in
            HStack(spacing: 0) {
                ForEach(Array(cells.enumerated()), id: \.offset) { index, value in
                    Text(value)
                        .font(.system(size: 14, weight: index == 0 ? .bold : .medium))
                        .foregroundColor(.white)
                        .lineLimit(1)
                        .frame(width: columnWidth(total: cells.count, index: index, available: geo.size.width - 24), alignment: .leading)
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 12)
            .frame(width: geo.size.width)
            .background(isAlternate ? Color(hex: RowAltBgHex) : Color.clear)
        }
        .frame(height: 44)
    }
}

private func columnWeight(total: Int, index: Int) -> Double {
    switch total {
    case 5:
        switch index { case 0: return 0.6; case 1: return 1.6; case 2: return 1.4; case 3: return 0.9; default: return 0.7 }
    case 4:
        switch index { case 0: return 0.6; case 1: return 1.8; case 2: return 0.9; default: return 0.7 }
    default:
        return 1
    }
}

private func columnWidth(total: Int, index: Int, available: CGFloat) -> CGFloat {
    let weights = (0..<total).map { columnWeight(total: total, index: $0) }
    let sum = weights.reduce(0, +)
    return available * CGFloat(columnWeight(total: total, index: index) / sum)
}

private struct LatestResultsSection: View {
    let results: [Race]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Latest Results")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(Color(hex: "8E8E93"))

            if results.isEmpty {
                Text("No completed races yet this season.")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8E8E93"))
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(hex: "161616"))
                    .cornerRadius(16)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(Color(hex: "262626"), lineWidth: 1)
                    )
            } else {
                VStack(spacing: 12) {
                    ForEach(results, id: \.id) { race in
                        LatestResultRow(race: race)
                    }
                }
            }
        }
    }
}

private struct LatestResultRow: View {
    let race: Race

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("ROUND \(Int(race.round))")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(Color(hex: "8E8E93"))
                Spacer()
                Text("FINAL")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(Color(hex: "E63946"))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color(hex: "E63946").opacity(0.1))
                    .cornerRadius(4)
            }

            HStack(spacing: 8) {
                Text(race.name)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Text(race.countryFlag)
                    .font(.system(size: 18))
            }

            Text(race.date)
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "8E8E93"))
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

private struct LatestThreadSection: View {
    let thread: TrendingThread?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Latest Thread")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(hex: "8E8E93"))
                Spacer()
                Text("FORUM")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(Color(hex: "E63946"))
            }

            VStack(alignment: .leading, spacing: 12) {
                HStack(spacing: 8) {
                    ZStack {
                        Circle().fill(Color(hex: "E63946"))
                        Text("RH").font(.system(size: 12, weight: .bold)).foregroundColor(.white)
                    }
                    .frame(width: 32, height: 32)

                    Text("Race Hub").font(.system(size: 14, weight: .bold)).foregroundColor(.white)

                    Spacer()

                    Text("PINNED")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(hex: "E63946"))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(hex: "E63946").opacity(0.1))
                        .cornerRadius(4)
                }

                Text(thread?.title ?? "No threads yet — be the first to post.")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)

                Text(thread.map { "❤️ \(Int($0.likes))" } ?? "❤️ 0")
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "8E8E93"))
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
}

// ── Bottom Bar ────────────────────────────────────────────────────────────────

private struct HomeBottomBar: View {
    let selectedTab: HomeTab
    let onTabSelected: (HomeTab) -> Void

    var body: some View {
        HStack {
            Spacer()
            TabItem(tab: .race, icon: "race_icon", label: "Race", isSelected: selectedTab == .race, action: onTabSelected)
            Spacer()
            TabItem(tab: .forum, icon: "forum_icon", label: "Forum", isSelected: selectedTab == .forum, action: onTabSelected)
            Spacer()
            TabItem(tab: .profile, icon: "profile_icon", label: "Profile", isSelected: selectedTab == .profile, action: onTabSelected)
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
    let icon: String
    let label: String
    let isSelected: Bool
    let action: (HomeTab) -> Void

    var body: some View {
        Button(action: { action(tab) }) {
            VStack(spacing: 4) {
                // Using SF Symbols as proxies for the custom icons in the screenshot
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

private struct PlaceholderView: View {
    let title: String
    var body: some View {
        Text(title).font(.largeTitle).foregroundColor(.white).padding(.top, 100)
    }
}

#Preview {
    HomeView()
}
