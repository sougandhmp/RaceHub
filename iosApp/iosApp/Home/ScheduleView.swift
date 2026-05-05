import SwiftUI
import Shared

struct ScheduleView: View {
    let schedule: [Race]
    @Environment(\.dismiss) var dismiss

    var body: some View {
        let nextRace = schedule.first { !$0.isCompleted }

        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()

            VStack(spacing: 0) {
                // Header
                HStack {
                    Button(action: { dismiss() }) {
                        Image(systemName: "arrow.left")
                            .foregroundColor(.white)
                            .font(.system(size: 20, weight: .bold))
                    }
                    Spacer()
                    Text("SCHEDULE")
                        .font(.system(size: 18, weight: .black))
                        .kerning(2)
                        .foregroundColor(.white)
                    Spacer()
                    Image(systemName: "arrow.left")
                        .opacity(0)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)

                ScrollView {
                    VStack(spacing: 16) {
                        ForEach(schedule, id: \.id) { race in
                            RaceRow(race: race, isNextRace: race.id == nextRace?.id)
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

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("ROUND \(Int(race.round))")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(race.isCompleted ? Color(hex: "888888") : Color(hex: "E10600"))

                Spacer()

                if race.isCompleted {
                    Text("COMPLETED")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "888888"))
                } else {
                    if let days = race.daysRemaining {
                        Text("\(Int(truncating: days)) DAYS")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color(hex: "E10600"))
                            .padding(.trailing, isNextRace ? 4 : 0)
                    }
                    if isNextRace {
                        Text("NEXT RACE")
                            .font(.system(size: 10, weight: .black))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(hex: "E10600"))
                            .cornerRadius(4)
                    }
                }
            }

            HStack(spacing: 8) {
                Text(race.name)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
                Text(race.countryFlag)
                    .font(.system(size: 18))
            }

            Text(race.date)
                .font(.system(size: 14))
                .foregroundColor(Color(hex: "888888"))
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "1A1A1A"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isNextRace ? Color(hex: "E10600") : Color(hex: "2A2A2A"), lineWidth: isNextRace ? 2 : 1)
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: UInt64
        switch hex.count {
        case 6:
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default:
            (r, g, b) = (0, 0, 0)
        }
        self.init(
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255
        )
    }
}

