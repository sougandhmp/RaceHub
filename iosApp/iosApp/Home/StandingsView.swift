import SwiftUI
import Shared

private enum StandingsCategory: String, CaseIterable {
    case drivers, constructors
    var label: String { self == .drivers ? "DRIVERS" : "CONSTRUCTORS" }
}

struct StandingsView: View {
    let drivers: [DriverStanding]
    let constructors: [ConstructorStanding]
    @Environment(\.dismiss) var dismiss
    @State private var selected: StandingsCategory = .drivers

    var body: some View {
        ZStack {
            Color(hex: "0A0A0A").ignoresSafeArea()

            VStack(spacing: 0) {
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
                    Image(systemName: "arrow.left").opacity(0)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)

                ScrollView {
                    VStack(spacing: 12) {
                        StandingsToggle(selected: $selected)

                        switch selected {
                        case .drivers:
                            ForEach(Array(drivers.enumerated()), id: \.element.position) { index, standing in
                                DriverStandingCard(
                                    standing: standing,
                                    accent: positionAccent(Int(standing.position)),
                                    teamColor: teamColor(for: standing.team),
                                    showLeader: index == 0
                                )
                            }
                        case .constructors:
                            ForEach(constructors, id: \.position) { standing in
                                ConstructorStandingCard(
                                    standing: standing,
                                    accent: positionAccent(Int(standing.position)),
                                    teamColor: teamColor(for: standing.name)
                                )
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
    }

    private func teamColor(for team: String) -> Color {
        let lower = team.lowercased()
        if lower.contains("mercedes") { return Color(hex: "00D2BE") }
        if lower.contains("mclaren") { return Color(hex: "FF8700") }
        if lower.contains("red bull") { return Color(hex: "0600EF") }
        if lower.contains("ferrari") { return Color(hex: "DC0000") }
        if lower.contains("aston") { return Color(hex: "006F62") }
        if lower.contains("alpine") { return Color(hex: "0090FF") }
        if lower.contains("williams") { return Color(hex: "005AFF") }
        if lower.contains("rb") { return Color(hex: "1660AD") }
        if lower.contains("haas") { return Color(hex: "B6BABD") }
        if lower.contains("sauber") { return Color(hex: "52E252") }
        return Color(hex: "8E8E93")
    }

    private func positionAccent(_ position: Int) -> Color {
        switch position {
        case 1: return Color(hex: "FFD700")
        case 2: return Color(hex: "C0C0C0")
        case 3: return Color(hex: "CD7F32")
        default: return Color(hex: "8E8E93")
        }
    }
}

private struct StandingsToggle: View {
    @Binding var selected: StandingsCategory

    var body: some View {
        HStack(spacing: 4) {
            ForEach(StandingsCategory.allCases, id: \.self) { tab in
                Button(action: { selected = tab }) {
                    Text(tab.label)
                        .font(.system(size: 12, weight: .bold))
                        .kerning(1)
                        .foregroundColor(selected == tab ? .white : Color(hex: "8E8E93"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(selected == tab ? Color(hex: "E10600") : Color.clear)
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
    }
}

private struct DriverStandingCard: View {
    let standing: DriverStanding
    let accent: Color
    let teamColor: Color
    let showLeader: Bool

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(teamColor)
                .frame(width: 4)

            HStack(spacing: 14) {
                PositionBadge(position: Int(standing.position), accent: accent)

                VStack(alignment: .leading, spacing: 4) {
                    Text(standing.driverName)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                    HStack(spacing: 6) {
                        Circle()
                            .fill(teamColor)
                            .frame(width: 8, height: 8)
                        Text(standing.team)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(Color(hex: "8E8E93"))
                    }
                }

                Spacer()

                PointsBlock(points: Int(standing.points), wins: Int(standing.wins))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .frame(maxWidth: .infinity)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(showLeader ? accent.opacity(0.5) : Color(hex: "262626"),
                        lineWidth: showLeader ? 1.5 : 1)
        )
    }
}

private struct ConstructorStandingCard: View {
    let standing: ConstructorStanding
    let accent: Color
    let teamColor: Color

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(teamColor)
                .frame(width: 6)

            HStack(spacing: 14) {
                PositionBadge(position: Int(standing.position), accent: accent)

                VStack(alignment: .leading, spacing: 4) {
                    Text(standing.name)
                        .font(.system(size: 17, weight: .heavy))
                        .foregroundColor(.white)
                    Text("CONSTRUCTOR")
                        .font(.system(size: 10, weight: .bold))
                        .kerning(1)
                        .foregroundColor(teamColor)
                }

                Spacer()

                PointsBlock(points: Int(standing.points), wins: Int(standing.wins))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
        }
        .frame(maxWidth: .infinity)
        .background(Color(hex: "161616"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "262626"), lineWidth: 1)
        )
    }
}

private struct PositionBadge: View {
    let position: Int
    let accent: Color

    private var isPodium: Bool { (1...3).contains(position) }

    var body: some View {
        ZStack {
            Circle()
                .fill(isPodium ? accent.opacity(0.15) : Color(hex: "1F1F1F"))
            Circle()
                .stroke(isPodium ? accent : Color(hex: "262626"), lineWidth: 1)
            Text("\(position)")
                .font(.system(size: 16, weight: .black))
                .foregroundColor(isPodium ? accent : .white)
        }
        .frame(width: 40, height: 40)
    }
}

private struct PointsBlock: View {
    let points: Int
    let wins: Int

    var body: some View {
        VStack(alignment: .trailing, spacing: 2) {
            HStack(alignment: .lastTextBaseline, spacing: 4) {
                Text("\(points)")
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundColor(.white)
                Text("PTS")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(Color(hex: "8E8E93"))
            }
            Text(wins == 1 ? "1 WIN" : "\(wins) WINS")
                .font(.system(size: 10, weight: .bold))
                .kerning(0.5)
                .foregroundColor(wins > 0 ? Color(hex: "E10600") : Color(hex: "8E8E93"))
        }
    }
}

