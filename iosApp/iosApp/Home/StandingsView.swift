import SwiftUI
import Shared

private enum StandingsCategory: String, CaseIterable {
    case drivers, constructors
    var label: LocalizedStringKey { self == .drivers ? "DRIVERS" : "CONSTRUCTORS" }
}

struct StandingsView: View {
    let drivers: [DriverStanding]
    let constructors: [ConstructorStanding]
    @Environment(\.dismiss) var dismiss
    @Environment(\.colorScheme) private var colorScheme
    private var colors: AppColors { AppColors.forScheme(colorScheme) }
    @State private var selected: StandingsCategory = .drivers

    var body: some View {
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
                    Text("STANDINGS")
                        .font(.system(size: 18, weight: .black))
                        .kerning(2)
                        .foregroundColor(colors.primaryText)
                    Spacer()
                    Image(systemName: "arrow.left").opacity(0)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 20)

                ScrollView {
                    VStack(spacing: 12) {
                        StandingsToggle(selected: $selected, colors: colors)

                        switch selected {
                        case .drivers:
                            ForEach(Array(drivers.enumerated()), id: \.element.position) { index, standing in
                                DriverStandingCard(
                                    standing: standing,
                                    accent: AppColors.podiumAccent(position: Int(standing.position), fallback: colors.mutedText),
                                    teamColor: AppColors.teamColor(standing.team),
                                    showLeader: index == 0,
                                    colors: colors
                                )
                            }
                        case .constructors:
                            ForEach(constructors, id: \.position) { standing in
                                ConstructorStandingCard(
                                    standing: standing,
                                    accent: AppColors.podiumAccent(position: Int(standing.position), fallback: colors.mutedText),
                                    teamColor: AppColors.teamColor(standing.name),
                                    colors: colors
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
}

private struct StandingsToggle: View {
    @Binding var selected: StandingsCategory
    let colors: AppColors

    var body: some View {
        HStack(spacing: 4) {
            ForEach(StandingsCategory.allCases, id: \.self) { tab in
                Button(action: { selected = tab }) {
                    Text(tab.label)
                        .font(.system(size: 12, weight: .bold))
                        .kerning(1)
                        .foregroundColor(selected == tab ? .white : colors.mutedText)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(selected == tab ? AppColors.f1Red : Color.clear)
                        .cornerRadius(8)
                }
            }
        }
        .padding(4)
        .background(colors.card)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct DriverStandingCard: View {
    let standing: DriverStanding
    let accent: Color
    let teamColor: Color
    let showLeader: Bool
    let colors: AppColors

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(teamColor)
                .frame(width: 4)

            HStack(spacing: 14) {
                PositionBadge(position: Int(standing.position), accent: accent, colors: colors)

                VStack(alignment: .leading, spacing: 4) {
                    Text(standing.driverName)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(colors.primaryText)
                    HStack(spacing: 6) {
                        Circle()
                            .fill(teamColor)
                            .frame(width: 8, height: 8)
                        Text(standing.team)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(colors.mutedText)
                    }
                }

                Spacer()

                PointsBlock(points: Int(standing.points), wins: Int(standing.wins), colors: colors)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .frame(maxWidth: .infinity)
        .background(colors.card)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(showLeader ? accent.opacity(0.5) : colors.cardBorder,
                        lineWidth: showLeader ? 1.5 : 1)
        )
    }
}

private struct ConstructorStandingCard: View {
    let standing: ConstructorStanding
    let accent: Color
    let teamColor: Color
    let colors: AppColors

    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(teamColor)
                .frame(width: 6)

            HStack(spacing: 14) {
                PositionBadge(position: Int(standing.position), accent: accent, colors: colors)

                VStack(alignment: .leading, spacing: 4) {
                    Text(standing.name)
                        .font(.system(size: 17, weight: .heavy))
                        .foregroundColor(colors.primaryText)
                    Text("CONSTRUCTOR")
                        .font(.system(size: 10, weight: .bold))
                        .kerning(1)
                        .foregroundColor(teamColor)
                }

                Spacer()

                PointsBlock(points: Int(standing.points), wins: Int(standing.wins), colors: colors)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
        }
        .frame(maxWidth: .infinity)
        .background(colors.card)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(colors.cardBorder, lineWidth: 1)
        )
    }
}

private struct PositionBadge: View {
    let position: Int
    let accent: Color
    let colors: AppColors

    private var isPodium: Bool { (1...3).contains(position) }

    var body: some View {
        ZStack {
            Circle()
                .fill(isPodium ? accent.opacity(0.15) : colors.surface)
            Circle()
                .stroke(isPodium ? accent : colors.cardBorder, lineWidth: 1)
            Text("\(position)")
                .font(.system(size: 16, weight: .black))
                .foregroundColor(isPodium ? accent : colors.primaryText)
        }
        .frame(width: 40, height: 40)
    }
}

private struct PointsBlock: View {
    let points: Int
    let wins: Int
    let colors: AppColors

    var body: some View {
        VStack(alignment: .trailing, spacing: 2) {
            HStack(alignment: .lastTextBaseline, spacing: 4) {
                Text("\(points)")
                    .font(.system(size: 22, weight: .heavy))
                    .foregroundColor(colors.primaryText)
                Text("PTS")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(colors.mutedText)
            }
            Text(wins == 1 ? String(localized: "1 WIN") : String(format: NSLocalizedString("label_wins_caps", comment: ""), wins))
                .font(.system(size: 10, weight: .bold))
                .kerning(0.5)
                .foregroundColor(wins > 0 ? AppColors.f1Red : colors.mutedText)
        }
    }
}

#Preview {
    StandingsView(
        drivers: [
            DriverStanding(position: 1, driverName: "George Russell", team: "Mercedes", points: 142, wins: 3),
            DriverStanding(position: 2, driverName: "Max Verstappen", team: "Red Bull", points: 134, wins: 2),
            DriverStanding(position: 3, driverName: "Lando Norris", team: "McLaren", points: 121, wins: 1)
        ],
        constructors: [
            ConstructorStanding(position: 1, name: "Mercedes", points: 276, wins: 4),
            ConstructorStanding(position: 2, name: "Red Bull", points: 207, wins: 2),
            ConstructorStanding(position: 3, name: "McLaren", points: 170, wins: 1)
        ]
    )
}
