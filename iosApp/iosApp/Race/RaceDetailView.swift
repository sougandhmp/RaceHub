import SwiftUI
import Shared

struct RaceDetailView: View {

    let race: Race
    @Environment(\.dismiss) var dismiss
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                detailHeader

                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        heroCard
                        circuitSection
                        scheduleSection
                        infoSection
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .padding(.bottom, 48)
                }
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: - Header

    private var detailHeader: some View {
        HStack {
            Button(action: { dismiss() }) {
                Image(systemName: "arrow.left")
                    .foregroundColor(colors.primaryText)
                    .font(.system(size: 20, weight: .bold))
            }
            Spacer()
            Text("RACE DETAIL")
                .font(.system(size: 16, weight: .black))
                .kerning(2)
                .foregroundColor(colors.primaryText)
            Spacer()
            Image(systemName: "arrow.left").opacity(0)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 20)
        .background(colors.background)
    }

    // MARK: - Hero Card

    private var heroCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 8) {
                        Text("R\(race.round)")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(.white)
                            .padding(.horizontal, 9)
                            .padding(.vertical, 3)
                            .background(AppColors.racingRed)
                            .cornerRadius(5)

                        Text(race.status.capitalized)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(statusColor)
                            .padding(.horizontal, 9)
                            .padding(.vertical, 3)
                            .background(statusColor.opacity(0.12))
                            .cornerRadius(5)
                    }

                    Text(race.name)
                        .font(.system(size: 24, weight: .black))
                        .foregroundColor(colors.primaryText)
                        .lineSpacing(2)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer()

                Text(countryFlag(race.country))
                    .font(.system(size: 36))
            }

            Divider().background(colors.cardBorder)

            VStack(alignment: .leading, spacing: 6) {
                DetailInfoRow(icon: "road.lanes", label: race.circuit, colors: colors)
                DetailInfoRow(icon: "mappin.circle.fill", label: "\(race.city), \(race.country)", colors: colors)
                DetailInfoRow(icon: "calendar", label: formattedRaceDate, colors: colors)
                DetailInfoRow(icon: "clock", label: "Race starts \(formattedRaceTime) local", colors: colors)
                if let weather = race.weather, !weather.isEmpty {
                    DetailInfoRow(icon: "cloud.sun.fill", label: "\(weatherIcon(weather))  \(weather)", colors: colors)
                }
            }
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.card)
        .cornerRadius(20)
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(colors.cardBorder, lineWidth: 1))
    }

    // MARK: - Circuit Image

    private var circuitSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(title: "CIRCUIT MAP", colors: colors)

            ZStack {
                RoundedRectangle(cornerRadius: 20)
                    .fill(colors.card)
                RoundedRectangle(cornerRadius: 20)
                    .stroke(colors.cardBorder, lineWidth: 1)

                if let imageName = circuitImageName(circuit: race.circuit, grandPrix: race.name) {
                    Image(imageName)
                        .resizable()
                        .scaledToFit()
                        .padding(24)
                        .frame(maxWidth: .infinity)
                } else {
                    Text("🏁")
                        .font(.system(size: 48))
                        .opacity(0.3)
                        .padding(32)
                }
            }
            .frame(height: 200)
        }
    }

    // MARK: - Session Schedule

    private var scheduleSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(title: "WEEKEND SCHEDULE", colors: colors)

            VStack(spacing: 0) {
                let chips = buildSessionChips(from: race.dateTime)
                ForEach(Array(chips.enumerated()), id: \.element.label) { index, chip in
                    let isRace = chip.label == "RACE"
                    let isLast = index == chips.count - 1

                    HStack(spacing: 14) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 6)
                                .fill(isRace ? AppColors.racingRed : colors.surface)
                            Text(chip.label)
                                .font(.system(size: 10, weight: .black))
                                .foregroundColor(isRace ? .white : colors.mutedText)
                                .kerning(0.3)
                        }
                        .frame(width: 44, height: 28)

                        Text(chip.date)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(colors.primaryText)

                        Spacer()

                        Text(chip.time)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(isRace ? AppColors.racingRed : colors.mutedText)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(isRace ? AppColors.racingRed.opacity(0.1) : Color.clear)
                            .cornerRadius(6)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(isRace ? AppColors.racingRed.opacity(0.04) : Color.clear)

                    if !isLast {
                        Divider()
                            .background(colors.cardBorder)
                            .padding(.horizontal, 16)
                    }
                }
            }
            .background(colors.card)
            .cornerRadius(20)
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(colors.cardBorder, lineWidth: 1))
        }
    }

    // MARK: - Race Info

    private var infoSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(title: "RACE INFO", colors: colors)

            VStack(spacing: 0) {
                InfoTableRow(label: "Status", value: race.status.capitalized, valueColor: statusColor, colors: colors)
                InfoDivider(colors: colors)
                InfoTableRow(label: "Round", value: "#\(race.round) of the season", colors: colors)
                InfoDivider(colors: colors)
                InfoTableRow(label: "Date", value: formattedRaceDate, colors: colors)
                InfoDivider(colors: colors)
                InfoTableRow(label: "Local time", value: formattedRaceTime, colors: colors)
                InfoDivider(colors: colors)
                InfoTableRow(label: "Circuit", value: race.circuit, colors: colors)
                InfoDivider(colors: colors)
                InfoTableRow(label: "Location", value: "\(race.city), \(race.country)", colors: colors)
                if let weather = race.weather, !weather.isEmpty {
                    InfoDivider(colors: colors)
                    InfoTableRow(label: "Weather", value: "\(weatherIcon(weather)) \(weather)", colors: colors)
                }
            }
            .background(colors.card)
            .cornerRadius(20)
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(colors.cardBorder, lineWidth: 1))
        }
    }

    // MARK: - Helpers

    private var statusColor: Color {
        switch race.status.lowercased() {
        case "live":      return .green
        case "completed": return colors.mutedText
        default:          return AppColors.racingRed
        }
    }

    private var formattedRaceDate: String {
        guard let date = ISO8601DateFormatter().date(from: race.dateTime) else { return race.dateTime }
        let fmt = DateFormatter()
        fmt.dateStyle = .long
        fmt.timeStyle = .none
        return fmt.string(from: date)
    }

    private var formattedRaceTime: String {
        guard let date = ISO8601DateFormatter().date(from: race.dateTime) else { return "" }
        let fmt = DateFormatter()
        fmt.dateStyle = .none
        fmt.timeStyle = .short
        return fmt.string(from: date)
    }

    private func weatherIcon(_ weather: String) -> String {
        let w = weather.lowercased()
        if w.contains("sun") || w.contains("clear") { return "☀️" }
        if w.contains("cloud") { return "⛅" }
        if w.contains("rain") || w.contains("wet") { return "🌧️" }
        if w.contains("wind") { return "💨" }
        return "🌡️"
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
        if c.contains("madrid") || c.contains("spain")       { return "🇪🇸" }
        return "🏁"
    }
}

// MARK: - Reusable sub-components

private struct SectionLabel: View {
    let title: LocalizedStringKey
    let colors: AppColors

    var body: some View {
        Text(title)
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(colors.mutedText)
            .kerning(1.5)
    }
}

private struct DetailInfoRow: View {
    let icon: String
    let label: String
    let colors: AppColors

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundColor(AppColors.racingRed)
                .frame(width: 18)
            Text(label)
                .font(.system(size: 14))
                .foregroundColor(colors.mutedText)
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}

private struct InfoTableRow: View {
    let label: String
    let value: String
    var valueColor: Color? = nil
    let colors: AppColors

    var body: some View {
        HStack(alignment: .top) {
            Text(label)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(colors.mutedText)
                .frame(width: 90, alignment: .leading)
            Text(value)
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(valueColor ?? colors.primaryText)
                .fixedSize(horizontal: false, vertical: true)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 13)
    }
}

private struct InfoDivider: View {
    let colors: AppColors
    var body: some View {
        Divider()
            .background(colors.cardBorder)
            .padding(.horizontal, 16)
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        RaceDetailView(race: Race(
            id: "australia-2025",
            name: "Australian Grand Prix",
            circuit: "Albert Park Circuit",
            country: "Australia",
            city: "Melbourne",
            dateTime: "2025-03-16T05:00:00Z",
            round: 3,
            status: "UPCOMING",
            weather: "Sunny"
        ))
    }
}
