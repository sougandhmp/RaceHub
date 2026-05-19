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
                        onViewAllSchedule: onViewAllSchedule
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
                .padding(.top, 8)
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
    let onViewAllSchedule: () -> Void

    var body: some View {
        ZStack(alignment: .trailing) {
            // Card background
            RoundedRectangle(cornerRadius: 20)
                .fill(colors.card)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(colors.cardBorder, lineWidth: 1)
                )

            // Faded circuit image on the right
            if let race = race,
               let imageName = circuitImageName(circuit: race.circuit, grandPrix: race.name) {
                Image(imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 160, height: 160)
                    .opacity(0.10)
                    .padding(.trailing, 4)
            }

            // Content
            VStack(alignment: .leading, spacing: 0) {
                // Header: ● UP NEXT + days
                HStack {
                    HStack(spacing: 6) {
                        Circle()
                            .fill(AppColors.racingRed)
                            .frame(width: 7, height: 7)
                        Text("UP NEXT")
                            .font(.system(size: 11, weight: .heavy))
                            .foregroundColor(colors.mutedText)
                            .kerning(1.5)
                    }
                    Spacer()
                    if let dateTimeStr = race?.dateTime,
                       let raceDate = ISO8601DateFormatter().date(from: dateTimeStr) {
                        let days = Calendar.current.dateComponents([.day], from: Date(), to: raceDate).day ?? 0
                        if days > 0 {
                            Text("\(days) DAYS")
                                .font(.system(size: 12, weight: .heavy))
                                .foregroundColor(AppColors.racingRed)
                        }
                    }
                }

                Spacer().frame(height: 12)

                // Race name
                Text(race?.name ?? "No Upcoming Race")
                    .font(.system(size: 26, weight: .black))
                    .foregroundColor(colors.primaryText)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(maxWidth: .infinity, alignment: .leading)

                if let race = race {
                    Spacer().frame(height: 2)
                    Text("R\(Int(race.round)) · \(race.circuit)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(colors.mutedText)
                }

                Spacer().frame(height: 16)

                // Stats row: LIGHTS OUT | LENGTH | LAPS
                HStack(spacing: 0) {
                    StatItem(label: "LIGHTS OUT", value: race.map { lightsOutLabel($0.dateTime) } ?? "—", colors: colors)
                    StatItem(label: "LENGTH", value: "—", colors: colors)
                    StatItem(label: "LAPS", value: "—", colors: colors)
                }

                Spacer().frame(height: 16)

                // Session strip: FP1 | FP2 | FP3 | QUAL | RACE
                SessionStrip(raceDate: race?.dateTime, colors: colors)

                Spacer().frame(height: 12)

                Divider()
                    .background(colors.cardBorder)

                Button(action: onViewAllSchedule) {
                    HStack(spacing: 4) {
                        Text("Full schedule")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(AppColors.racingRed)
                        Image(systemName: "arrow.forward")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(AppColors.racingRed)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 36)
                }
            }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func lightsOutLabel(_ dateTime: String) -> String {
        guard let date = ISO8601DateFormatter().date(from: dateTime) else { return "—" }
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d"
        return formatter.string(from: date)
    }
}

private struct StatItem: View {
    let label: String
    let value: String
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.system(size: 9, weight: .heavy))
                .foregroundColor(colors.mutedText)
                .kerning(0.5)
            Text(value)
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(colors.primaryText)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct SessionStrip: View {
    let raceDate: String?
    let colors: AppColors

    private var month: String {
        guard let dateTime = raceDate,
              let date = ISO8601DateFormatter().date(from: dateTime) else { return "—" }
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM"
        return formatter.string(from: date)
    }

    private let labels = ["FP1", "FP2", "FP3", "QUAL", "RACE"]

    var body: some View {
        HStack(spacing: 6) {
            ForEach(labels, id: \.self) { label in
                let isRace = label == "RACE"
                VStack(spacing: 2) {
                    Text(label)
                        .font(.system(size: 9, weight: .heavy))
                        .foregroundColor(isRace ? .white : colors.mutedText)
                        .kerning(0.3)
                    Text(month)
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(isRace ? Color.white.opacity(0.9) : colors.primaryText)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(isRace ? AppColors.racingRed : colors.cardBorder.opacity(0.4))
                )
            }
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
                if drivers.isEmpty {
                    StandingsPlaceholder(colors: colors)
                } else {
                    VStack(spacing: 8) {
                        ForEach(drivers, id: \.position) { standing in
                            DriverStandingCard(standing: standing, colors: colors)
                        }
                    }
                }
            case .constructors:
                if constructors.isEmpty {
                    StandingsPlaceholder(colors: colors)
                } else {
                    VStack(spacing: 8) {
                        ForEach(constructors, id: \.position) { standing in
                            ConstructorStandingCard(standing: standing, colors: colors)
                        }
                    }
                }
            }
        }
    }
}

private struct StandingsPlaceholder: View {
    let colors: AppColors

    var body: some View {
        Text("Standings loading…")
            .font(.system(size: 13))
            .foregroundColor(colors.mutedText)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
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
        HStack(spacing: 0) {
            // Team color bar (clipped by the outer cornerRadius)
            AppColors.teamColor(standing.team)
                .frame(width: 4)

            HStack(spacing: 12) {
                Text("\(Int(standing.position))")
                    .font(.system(size: 14, weight: .heavy))
                    .foregroundColor(colors.mutedText)
                    .frame(width: 20, alignment: .leading)

                VStack(alignment: .leading, spacing: 2) {
                    Text(standing.driverName)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(colors.primaryText)
                    Text(standing.team)
                        .font(.system(size: 12))
                        .foregroundColor(colors.mutedText)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(Int(standing.points))")
                        .font(.system(size: 20, weight: .heavy))
                        .foregroundColor(colors.primaryText)
                    Text("PTS")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(colors.mutedText)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 14)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 56)
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
        HStack(spacing: 0) {
            // Team color bar (clipped by the outer cornerRadius)
            AppColors.teamColor(standing.name)
                .frame(width: 4)

            HStack(spacing: 12) {
                Text("\(Int(standing.position))")
                    .font(.system(size: 14, weight: .heavy))
                    .foregroundColor(colors.mutedText)
                    .frame(width: 20, alignment: .leading)

                VStack(alignment: .leading, spacing: 2) {
                    Text(standing.name)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(colors.primaryText)
                    Text("\(Int(standing.wins)) wins")
                        .font(.system(size: 12))
                        .foregroundColor(colors.mutedText)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(Int(standing.points))")
                        .font(.system(size: 20, weight: .heavy))
                        .foregroundColor(colors.primaryText)
                    Text("PTS")
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(colors.mutedText)
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 14)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 56)
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

#Preview {
    RaceView(
        viewModel: RaceViewModel(),
        onViewAllSchedule: {},
        onViewAllStandings: {}
    )
}
