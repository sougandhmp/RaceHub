import SwiftUI
import Shared

struct RaceView: View {

    @ObservedObject var viewModel: RaceViewModel
    @Environment(\.colorScheme) private var colorScheme
    let onViewAllSchedule: () -> Void
    let onViewAllStandings: () -> Void
    let onViewRaceDetail: (Race) -> Void

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
                        totalRaces: viewModel.state.raceSchedule.count,
                        colors: colors,
                        onViewAllSchedule: onViewAllSchedule,
                        onViewRaceDetail: onViewRaceDetail
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

struct SessionChipData {
    let label: String
    let date: String
    let time: String
}

private func shortRaceName(_ name: String) -> String {
    name.replacingOccurrences(of: "Grand Prix", with: "GP")
}

private func countryFlag(_ country: String) -> String {
    let c = country.lowercased()
    if c.contains("bahrain")                              { return "🇧🇭" }
    if c.contains("saudi")                                { return "🇸🇦" }
    if c.contains("australia")                            { return "🇦🇺" }
    if c.contains("japan")                                { return "🇯🇵" }
    if c.contains("china")                                { return "🇨🇳" }
    if c.contains("monaco")                               { return "🇲🇨" }
    if c.contains("canada")                               { return "🇨🇦" }
    if c.contains("spain")                                { return "🇪🇸" }
    if c.contains("austria")                              { return "🇦🇹" }
    if c.contains("britain") || c.contains("kingdom")    { return "🇬🇧" }
    if c.contains("hungary")                              { return "🇭🇺" }
    if c.contains("belgium")                              { return "🇧🇪" }
    if c.contains("netherlands")                          { return "🇳🇱" }
    if c.contains("singapore")                            { return "🇸🇬" }
    if c.contains("azerbaijan")                           { return "🇦🇿" }
    if c.contains("qatar")                                { return "🇶🇦" }
    if c.contains("mexico")                               { return "🇲🇽" }
    if c.contains("brazil")                               { return "🇧🇷" }
    if c.contains("abu dhabi") || c.contains("emirates") { return "🇦🇪" }
    if c.contains("united states") || c.contains("usa")  { return "🇺🇸" }
    if c.contains("italy")                                { return "🇮🇹" }
    return "🏁"
}

private func raceHeaderDate(from dateString: String) -> String {
    guard let date = ISO8601DateFormatter().date(from: dateString) else { return "" }
    let fmt = DateFormatter()
    fmt.dateFormat = "EEE MMM d"
    fmt.locale = Locale(identifier: "en_US")
    fmt.timeZone = TimeZone(identifier: "UTC")
    return fmt.string(from: date).uppercased()
}

func buildSessionChips(from raceDateString: String?) -> [SessionChipData] {
    let placeholders = ["FP1", "FP2", "FP3", "QUAL", "RACE"].map {
        SessionChipData(label: $0, date: "—", time: "—")
    }
    guard let s = raceDateString, let raceDate = ISO8601DateFormatter().date(from: s) else {
        return placeholders
    }
    var utcCal = Calendar(identifier: .gregorian)
    utcCal.timeZone = TimeZone(identifier: "UTC")!

    let dateFmt = DateFormatter()
    dateFmt.dateFormat = "MMM d"
    dateFmt.locale = Locale(identifier: "en_US")
    dateFmt.timeZone = TimeZone(identifier: "UTC")!

    let timeFmt = DateFormatter()
    timeFmt.dateFormat = "HH:mm"
    timeFmt.timeZone = .current

    func dateAt(days: Int, hours: Int = 0) -> Date {
        var c = DateComponents(); c.day = days; c.hour = hours
        return utcCal.date(byAdding: c, to: raceDate) ?? raceDate
    }

    return [
        SessionChipData(label: "FP1",  date: dateFmt.string(from: dateAt(days: -2, hours: -3)), time: timeFmt.string(from: dateAt(days: -2, hours: -3))),
        SessionChipData(label: "FP2",  date: dateFmt.string(from: dateAt(days: -2)),             time: timeFmt.string(from: dateAt(days: -2))),
        SessionChipData(label: "FP3",  date: dateFmt.string(from: dateAt(days: -1, hours: -3)), time: timeFmt.string(from: dateAt(days: -1, hours: -3))),
        SessionChipData(label: "QUAL", date: dateFmt.string(from: dateAt(days: -1)),             time: timeFmt.string(from: dateAt(days: -1))),
        SessionChipData(label: "RACE", date: dateFmt.string(from: dateAt(days: 0)),              time: timeFmt.string(from: dateAt(days: 0))),
    ]
}

private struct NextRaceSection: View {
    let race: Race?
    let totalRaces: Int
    let colors: AppColors
    let onViewAllSchedule: () -> Void
    let onViewRaceDetail: (Race) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // R9/24 · SUN MAY 24  +  🇨🇦
            HStack {
                let roundLabel = race.map { "R\(Int($0.round))/\(totalRaces)" } ?? ""
                let dateLabel = race.flatMap { raceHeaderDate(from: $0.dateTime).isEmpty ? nil : raceHeaderDate(from: $0.dateTime) } ?? ""
                Text([roundLabel, dateLabel].filter { !$0.isEmpty }.joined(separator: "  ·  "))
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(colors.mutedText)
                    .kerning(0.5)
                Spacer()
                if let race = race {
                    Text(countryFlag(race.country))
                        .font(.system(size: 22))
                }
            }

            Spacer().frame(height: 10)

            // Race name
            Text(race.map { shortRaceName($0.name) } ?? String(localized: "No Upcoming Race"))
                .font(.system(size: 26, weight: .black))
                .foregroundColor(colors.primaryText)

            if let race = race {
                Spacer().frame(height: 2)
                Text(race.circuit)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(colors.mutedText)
            }

            // Circuit image — centered
            if let race = race,
               let imageName = circuitImageName(circuit: race.circuit, grandPrix: race.name) {
                Spacer().frame(height: 16)
                Image(imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity)
                    .frame(height: 130)
                    .opacity(0.22)
            }

            Spacer().frame(height: 16)

            // Session strip
            SessionStrip(chips: buildSessionChips(from: race?.dateTime), colors: colors)

            Spacer().frame(height: 16)

            HStack(spacing: 10) {
                Button(action: { if let race { onViewRaceDetail(race) } }) {
                    Text("Weekend detail")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(race == nil ? colors.mutedText : colors.primaryText)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .overlay(
                            Capsule()
                                .stroke(colors.cardBorder, lineWidth: 1)
                        )
                }
                .disabled(race == nil)

                Button(action: onViewAllSchedule) {
                    HStack(spacing: 4) {
                        Text("Full calendar")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.white)
                        Image(systemName: "arrow.forward")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(Capsule().fill(AppColors.racingRed))
                }
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.card)
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(colors.cardBorder, lineWidth: 1))
    }
}

private struct SessionStrip: View {
    let chips: [SessionChipData]
    let colors: AppColors

    var body: some View {
        HStack(spacing: 6) {
            ForEach(chips, id: \.label) { chip in
                let isRace = chip.label == "RACE"
                VStack(spacing: 0) {
                    Text(chip.label)
                        .font(.system(size: 9, weight: .heavy))
                        .foregroundColor(isRace ? .white : colors.mutedText)
                        .kerning(0.3)
                    Spacer().frame(height: 3)
                    Text(chip.date)
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(isRace ? .white : colors.primaryText)
                    Text(chip.time)
                        .font(.system(size: 9))
                        .foregroundColor(isRace ? Color.white.opacity(0.85) : colors.mutedText)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .padding(.horizontal, 2)
                .background(
                    RoundedRectangle(cornerRadius: 10)
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
                    Text(tab == .drivers ? String(localized: "Drivers") : String(localized: "Constructors"))
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
                    Text(String(format: NSLocalizedString("label_wins", comment: ""), Int(standing.wins)))
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

                Text(thread?.title ?? String(localized: "No threads yet — be the first to post."))
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
        onViewAllStandings: {},
        onViewRaceDetail: { _ in }
    )
}
