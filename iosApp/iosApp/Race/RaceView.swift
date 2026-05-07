import SwiftUI
import Shared

struct RaceView: View {

    @ObservedObject var viewModel: RaceViewModel
    @Environment(\.colorScheme) private var colorScheme
    let onViewAllSchedule: () -> Void
    let onViewAllStandings: () -> Void

    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        ScrollView {
            if viewModel.state.isLoading && viewModel.state.raceSchedule.isEmpty {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: AppColors.racingRed))
                    .frame(maxWidth: .infinity)
                    .padding(.top, 40)
            } else {
                VStack(spacing: 24) {
                    NextRaceSection(
                        race: viewModel.state.raceSchedule.first { !$0.isCompleted },
                        colors: colors,
                        onViewAll: onViewAllSchedule
                    )
                    StandingsSection(
                        drivers: Array(viewModel.state.driverStandings.prefix(3)),
                        constructors: Array(viewModel.state.constructorStandings.prefix(3)),
                        colors: colors,
                        onViewAll: onViewAllStandings
                    )
                    FeaturedSection(
                        thread: viewModel.state.trendingThreads.first,
                        colors: colors
                    )
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 100)
            }
        }
        .refreshable {
            await viewModel.refresh()
        }
    }
}

// MARK: - Next Race

private struct NextRaceSection: View {
    let race: Race?
    let colors: AppColors
    let onViewAll: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("NEXT RACE")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(colors.mutedText)
                    .kerning(1)
                Spacer()
                if let race = race, let days = race.daysRemaining {
                    Text("RD \(Int(race.round)) · \(Int(truncating: days)) DAYS")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(AppColors.racingRed)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(AppColors.racingRed.opacity(0.1))
                        .cornerRadius(6)
                }
            }

            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .center, spacing: 8) {
                    Text(race?.name ?? "Canadian GP")
                        .font(.system(size: 26, weight: .heavy))
                        .foregroundColor(colors.primaryText)
                    Text(race?.countryFlag ?? "🇨🇦")
                        .font(.system(size: 22))
                }

                Text(race?.date ?? "Sun May 24 · 8:00 PM UTC · Montreal")
                    .font(.system(size: 13))
                    .foregroundColor(colors.mutedText)

                HStack {
                    HStack(spacing: 4) {
                        Text("Circuit")
                            .foregroundColor(colors.mutedText)
                        Text(race?.circuit ?? "—")
                            .foregroundColor(colors.primaryText)
                            .fontWeight(.medium)
                    }
                    .font(.system(size: 13))
                    Spacer()
                    Button(action: onViewAll) {
                        Text("See all")
                            .font(.system(size: 13, weight: .semibold))
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
}

// MARK: - Standings

private enum StandingsTab: String, CaseIterable {
    case drivers, constructors
}

private struct StandingsSection: View {
    let drivers: [DriverStanding]
    let constructors: [ConstructorStanding]
    let colors: AppColors
    let onViewAll: () -> Void

    @State private var selected: StandingsTab = .drivers

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Standings")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(colors.primaryText)
                Spacer()
                Button(action: onViewAll) {
                    Text("See all")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(AppColors.racingRed)
                }
            }

            StandingsTabPills(selected: $selected, colors: colors)

            switch selected {
            case .drivers:
                let displayDrivers: [DriverStanding] = drivers.isEmpty
                    ? [
                        DriverStanding(position: 1, driverName: "George Russell", team: "Mercedes", points: 142, wins: 3),
                        DriverStanding(position: 2, driverName: "Max Verstappen", team: "Red Bull", points: 134, wins: 2),
                        DriverStanding(position: 3, driverName: "Lando Norris", team: "McLaren", points: 121, wins: 1)
                      ]
                    : drivers
                VStack(spacing: 8) {
                    ForEach(displayDrivers, id: \.position) { standing in
                        DriverStandingCard(standing: standing, colors: colors)
                    }
                }
            case .constructors:
                let displayConstructors: [ConstructorStanding] = constructors.isEmpty
                    ? [
                        ConstructorStanding(position: 1, name: "Mercedes", points: 276, wins: 4),
                        ConstructorStanding(position: 2, name: "Red Bull", points: 207, wins: 2),
                        ConstructorStanding(position: 3, name: "McLaren", points: 170, wins: 1)
                      ]
                    : constructors
                VStack(spacing: 8) {
                    ForEach(displayConstructors, id: \.position) { standing in
                        ConstructorStandingCard(standing: standing, colors: colors)
                    }
                }
            }
        }
    }
}

private struct StandingsTabPills: View {
    @Binding var selected: StandingsTab
    let colors: AppColors

    var body: some View {
        HStack(spacing: 0) {
            ForEach(StandingsTab.allCases, id: \.self) { tab in
                let isSelected = tab == selected
                Button(action: { selected = tab }) {
                    Text(tab == .drivers ? "Drivers" : "Constructors")
                        .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                        .foregroundColor(isSelected ? .white : colors.mutedText)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(isSelected ? AppColors.racingRed : Color.clear)
                        .cornerRadius(8)
                }
            }
        }
        .padding(3)
        .background(colors.card)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct DriverStandingCard: View {
    let standing: DriverStanding
    let colors: AppColors

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(standing.position == 1
                          ? AppColors.racingRed.opacity(0.12)
                          : colors.cardBorder.opacity(0.5))
                Text("\(Int(standing.position))")
                    .font(.system(size: 14, weight: .heavy))
                    .foregroundColor(standing.position == 1 ? AppColors.racingRed : colors.mutedText)
            }
            .frame(width: 32, height: 32)

            VStack(alignment: .leading, spacing: 2) {
                Text(standing.driverName)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(colors.primaryText)
                Text(standing.team)
                    .font(.system(size: 12))
                    .foregroundColor(colors.mutedText)
            }

            Spacer()

            Text("\(Int(standing.points))")
                .font(.system(size: 20, weight: .heavy))
                .foregroundColor(colors.primaryText)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .frame(maxWidth: .infinity)
        .background(colors.card)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct ConstructorStandingCard: View {
    let standing: ConstructorStanding
    let colors: AppColors

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(standing.position == 1
                          ? AppColors.racingRed.opacity(0.12)
                          : colors.cardBorder.opacity(0.5))
                Text("\(Int(standing.position))")
                    .font(.system(size: 14, weight: .heavy))
                    .foregroundColor(standing.position == 1 ? AppColors.racingRed : colors.mutedText)
            }
            .frame(width: 32, height: 32)

            VStack(alignment: .leading, spacing: 2) {
                Text(standing.name)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(colors.primaryText)
                Text("\(Int(standing.wins)) wins")
                    .font(.system(size: 12))
                    .foregroundColor(colors.mutedText)
            }

            Spacer()

            Text("\(Int(standing.points))")
                .font(.system(size: 20, weight: .heavy))
                .foregroundColor(colors.primaryText)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .frame(maxWidth: .infinity)
        .background(colors.card)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

// MARK: - Featured

private struct FeaturedSection: View {
    let thread: TrendingThread?
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("FEATURED")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(colors.mutedText)
                    .kerning(1)
                Spacer()
                Text("Forum")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(AppColors.racingRed)
            }

            VStack(alignment: .leading, spacing: 10) {
                HStack {
                    Text("TRENDING")
                        .font(.system(size: 10, weight: .black))
                        .foregroundColor(AppColors.racingRed)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(AppColors.racingRed.opacity(0.1))
                        .cornerRadius(6)
                    Spacer()
                    if let date = thread?.createdAt {
                        Text(date)
                            .font(.system(size: 11))
                            .foregroundColor(colors.mutedText)
                    }
                }

                Text(thread?.title ?? "No threads yet — be the first to post.")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(colors.primaryText)
                    .lineSpacing(4)

                Text("♥ \(thread.map { Int($0.likes) } ?? 0)")
                    .font(.system(size: 12))
                    .foregroundColor(colors.mutedText)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(colors.card)
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(colors.cardBorder, lineWidth: 1)
            )
        }
    }
}
