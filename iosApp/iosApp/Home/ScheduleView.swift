import SwiftUI
import Shared

struct ScheduleView: View {
    let schedule: [Race]
    @Environment(\.dismiss) var dismiss
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }

    var body: some View {
        let nextRace = schedule.first { !$0.isCompleted }

        ZStack {
            colors.background.ignoresSafeArea()

            VStack(spacing: 0) {
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "arrow.left")
                            .foregroundColor(colors.primaryText)
                            .font(.system(size: 20, weight: .bold))
                    }
                    Spacer()
                    Text("SCHEDULE")
                        .font(.system(size: 18, weight: .black))
                        .kerning(2)
                        .foregroundColor(colors.primaryText)
                    Spacer()
                    Image(systemName: "arrow.left")
                        .opacity(0)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)

                ScrollView {
                    VStack(spacing: 16) {
                        ForEach(schedule, id: \.id) { race in
                            RaceRow(race: race, isNextRace: race.id == nextRace?.id, colors: colors)
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }
}


struct RaceRow: View {
    let race: Race
    let isNextRace: Bool
    let colors: AppColors

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(String(format: NSLocalizedString("label_round", comment: ""), Int(race.round)))
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(race.isCompleted ? colors.mutedText : AppColors.racingRed)

                Spacer()

                if race.isCompleted {
                    Text("COMPLETED")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(colors.mutedText)
                } else {
                    let days = daysUntil(race.dateTime)
                    if let days, days > 0 {
                        Text(String(format: NSLocalizedString("label_days", comment: ""), days))
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(AppColors.racingRed)
                            .padding(.trailing, isNextRace ? 4 : 0)
                    }
                    if isNextRace {
                        Text("NEXT RACE")
                            .font(.system(size: 10, weight: .black))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(AppColors.racingRed)
                            .cornerRadius(4)
                    }
                }
            }

            HStack(spacing: 8) {
                Text(race.name)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(colors.primaryText)
                Text(countryFlag(race.country))
                    .font(.system(size: 18))
            }

            Text(race.circuit)
                .font(.system(size: 12))
                .foregroundColor(colors.mutedText)

            Text(formattedDate(race.dateTime))
                .font(.system(size: 14))
                .foregroundColor(colors.mutedText)

            CircuitImageView(
                circuitName: race.circuit,
                grandPrixName: race.name,
                colors: colors
            )
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(colors.card)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isNextRace ? AppColors.racingRed : colors.cardBorder,
                        lineWidth: isNextRace ? 2 : 1)
        )
    }
}

private func daysUntil(_ dateTime: String) -> Int? {
    guard let date = ISO8601DateFormatter().date(from: dateTime) else { return nil }
    return Calendar.current.dateComponents([.day], from: Date(), to: date).day
}

private func formattedDate(_ dateTime: String) -> String {
    guard let date = ISO8601DateFormatter().date(from: dateTime) else { return dateTime }
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .none
    return formatter.string(from: date)
}

private func countryFlag(_ country: String) -> String {
    let codes: [String: String] = [
        "australia": "AU", "bahrain": "BH", "saudi arabia": "SA",
        "japan": "JP", "china": "CN", "united states": "US",
        "monaco": "MC", "canada": "CA", "spain": "ES",
        "austria": "AT", "great britain": "GB", "hungary": "HU",
        "belgium": "BE", "netherlands": "NL", "italy": "IT",
        "azerbaijan": "AZ", "singapore": "SG", "mexico": "MX",
        "brazil": "BR", "qatar": "QA", "abu dhabi": "AE",
        "united arab emirates": "AE"
    ]
    guard let code = codes[country.lowercased()] else { return "" }
    return code.unicodeScalars.reduce("") { $0 + String(UnicodeScalar(127397 + $1.value)!) }
}

#Preview {
    ScheduleView(schedule: [
        Race(
            id: "bahrain-2025",
            name: "Bahrain Grand Prix",
            circuit: "Bahrain International Circuit",
            country: "Bahrain",
            city: "Sakhir",
            dateTime: "2025-03-02T15:00:00Z",
            round: 1,
            status: "COMPLETED",
            weather: nil
        ),
        Race(
            id: "australia-2025",
            name: "Australian Grand Prix",
            circuit: "Albert Park Circuit",
            country: "Australia",
            city: "Melbourne",
            dateTime: "2025-03-16T05:00:00Z",
            round: 2,
            status: "UPCOMING",
            weather: nil
        )
    ])
}
