import SwiftUI
import Shared

struct StandingsView: View {
    let standings: [DriverStanding]
    @Environment(\.dismiss) var dismiss

    var body: some View {
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
                    Text("STANDINGS")
                        .font(.system(size: 18, weight: .black))
                        .kerning(2)
                        .foregroundColor(.white)
                    Spacer()
                    // Dummy button for centering
                    Image(systemName: "arrow.left")
                        .opacity(0)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)

                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(standings, id: \.position) { standing in
                            StandingRow(standing: standing, teamColor: colorForTeam(standing.team))
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }

    private func colorForTeam(_ team: String) -> Color {
        switch team {
        case "Mercedes": return Color(hex: "00D2BE")
        case "McLaren": return Color(hex: "FF8700")
        case "Red Bull": return Color(hex: "0600EF")
        default: return Color(hex: "8E8E93")
        }
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

struct StandingRow: View {
    let standing: DriverStanding
    let teamColor: Color

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(teamColor)
                .frame(width: 4)

            HStack(spacing: 16) {
                ZStack {
                    Circle().fill(Color(hex: "262626"))
                    Text("\(Int(standing.position))")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "8E8E93"))
                }
                .frame(width: 32, height: 32)

                VStack(alignment: .leading, spacing: 2) {
                    Text(standing.team)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                    Text(standing.driverName)
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "8E8E93"))
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(Int(standing.points))")
                        .font(.system(size: 20, weight: .heavy))
                        .foregroundColor(.white)
                    Text("PTS")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(hex: "8E8E93"))
                }
            }
            .padding(16)
        }
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}
