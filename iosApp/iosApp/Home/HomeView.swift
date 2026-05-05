import SwiftUI
import Combine
import Shared

// ── Entry point ───────────────────────────────────────────────────────────────

/// Root view for the Home screen.
///
/// Observes `HomeViewModel.state` and delegates rendering to
/// `RaceScheduleView` or `DriverStandingsView` based on the active tab.
/// One-time navigation effects are forwarded to the host via `onReceive`.
struct HomeView: View {

    @StateObject private var viewModel = HomeViewModel()

    var body: some View {
        ZStack {
            backgroundGradient.ignoresSafeArea()

            VStack(spacing: 0) {
                HomeHeaderView()
                TabSwitcherView(
                    selectedTab: viewModel.state.selectedTab,
                    onTabSelected: { viewModel.send(.tabSelected($0)) }
                )

                if viewModel.state.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "E63946")))
                        .scaleEffect(1.4)
                    Spacer()
                } else {
                    switch viewModel.state.selectedTab {
                    case .schedule:
                        RaceScheduleView(
                            races: viewModel.state.raceSchedule,
                            nextRaceId: viewModel.state.raceSchedule.first { !$0.isCompleted }?.id
                        )
                    case .standings:
                        DriverStandingsView(standings: viewModel.state.driverStandings)
                    }
                }
            }
        }
        // Handle one-time navigation effects when detail screens are added.
        .onReceive(viewModel.effectPublisher) { _ in }
    }

    private var backgroundGradient: LinearGradient {
        LinearGradient(
            colors: [Color(hex: "0A0A0A"), Color(hex: "1A1A2E"), Color(hex: "16213E")],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

/// Top bar showing the RaceHub brand and current season.
private struct HomeHeaderView: View {
    var body: some View {
        HStack(spacing: 10) {
            Text("🏎️").font(.system(size: 28))
            VStack(alignment: .leading, spacing: 1) {
                Text("RaceHub")
                    .font(.system(size: 20, weight: .heavy))
                    .foregroundColor(Color(hex: "E63946"))
                Text("2025 Season")
                    .font(.system(size: 11))
                    .foregroundColor(Color(hex: "8D99AE"))
            }
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color(hex: "0A0A0A"))
    }
}

// ── Tab switcher ──────────────────────────────────────────────────────────────

/// Custom two-button tab switcher that mirrors the Android design.
private struct TabSwitcherView: View {

    let selectedTab: HomeTab
    let onTabSelected: (HomeTab) -> Void

    var body: some View {
        HStack(spacing: 8) {
            tabButton(for: .schedule,  label: "Schedule")
            tabButton(for: .standings, label: "Standings")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color(hex: "0A0A0A"))
    }

    @ViewBuilder
    private func tabButton(for tab: HomeTab, label: String) -> some View {
        let isSelected = tab == selectedTab
        Button(action: { onTabSelected(tab) }) {
            Text(label)
                .font(.system(size: 14, weight: isSelected ? .semibold : .regular))
                .foregroundColor(isSelected ? .white : Color(hex: "8D99AE"))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(isSelected ? Color(hex: "E63946") : Color(hex: "161625"))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(isSelected ? Color(hex: "E63946") : Color(hex: "2A2A3E"), lineWidth: 1)
                )
        }
        .buttonStyle(.plain)
    }
}

// ── Race schedule tab ─────────────────────────────────────────────────────────

/// Scrollable list of race event cards.
private struct RaceScheduleView: View {

    /// Kotlin `[Race]` array built by `HomeViewModel` via index-based access.
    let races: [Race]

    /// ID of the first upcoming race; that card receives a "NEXT RACE" badge.
    let nextRaceId: String?

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(races, id: \.id) { race in
                    RaceCardView(race: race, isNextRace: race.id == nextRaceId)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 16)
        }
    }
}

/// Card for a single race event.
private struct RaceCardView: View {

    let race: Race

    /// When `true`, the card gets a red border and a "NEXT RACE" chip.
    let isNextRace: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {

            // Top row: flag + round label + status chip
            HStack {
                Text(race.countryFlag).font(.system(size: 22))
                Text("ROUND \(Int(race.round))")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(Color(hex: "8D99AE"))
                    .kerning(1)
                Spacer()
                if isNextRace {
                    StatusChip(label: "NEXT RACE", background: Color(hex: "E63946"))
                } else if race.isCompleted {
                    StatusChip(
                        label: "COMPLETED",
                        background: Color(hex: "2ECC71").opacity(0.15),
                        textColor: Color(hex: "2ECC71")
                    )
                }
            }

            // Race name
            Text(race.name.uppercased())
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(.white)
                .kerning(0.5)

            // Circuit and date
            Text(race.circuit)
                .font(.system(size: 12))
                .foregroundColor(Color(hex: "8D99AE"))
            Text(race.date)
                .font(.system(size: 11))
                .foregroundColor(Color(hex: "8D99AE").opacity(0.7))
        }
        .padding(16)
        .background(Color(hex: "161625"))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(isNextRace ? Color(hex: "E63946") : Color(hex: "2A2A3E"), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// ── Driver standings tab ──────────────────────────────────────────────────────

/// Scrollable list of driver championship standing cards.
private struct DriverStandingsView: View {

    /// Kotlin `[DriverStanding]` array built by `HomeViewModel`.
    let standings: [DriverStanding]

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(standings, id: \.position) { standing in
                    DriverStandingCardView(standing: standing)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 16)
        }
    }
}

/// Card for a single driver's championship row.
private struct DriverStandingCardView: View {

    let standing: DriverStanding

    var body: some View {
        HStack(spacing: 14) {

            // Position badge — gold/silver/bronze for top 3
            PositionBadge(position: Int(standing.position))

            // Driver info
            VStack(alignment: .leading, spacing: 3) {
                Text(standing.driverName.uppercased())
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.white)
                    .kerning(0.5)
                HStack(spacing: 6) {
                    Text(standing.team)
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "8D99AE"))
                    Text(standing.flag)
                        .font(.system(size: 12))
                }
            }

            Spacer()

            // Points
            VStack(alignment: .trailing, spacing: 0) {
                Text("\(Int(standing.points))")
                    .font(.system(size: 20, weight: .heavy))
                    .foregroundColor(.white)
                Text("PTS")
                    .font(.system(size: 10, weight: .regular))
                    .foregroundColor(Color(hex: "8D99AE"))
                    .kerning(1)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(hex: "161625"))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(hex: "2A2A3E"), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// ── Reusable small components ─────────────────────────────────────────────────

/// Circular badge showing a championship position.
/// Positions 1–3 use gold, silver, and bronze fills respectively.
private struct PositionBadge: View {

    let position: Int

    private var badgeColor: Color {
        switch position {
        case 1:  return Color(hex: "FFD700")
        case 2:  return Color(hex: "C0C0C0")
        case 3:  return Color(hex: "CD7F32")
        default: return Color(hex: "2A2A3E")
        }
    }

    private var textColor: Color {
        position <= 3 ? Color(hex: "0A0A0A") : Color(hex: "8D99AE")
    }

    var body: some View {
        ZStack {
            Circle().fill(badgeColor)
            Text("\(position)")
                .font(.system(size: 14, weight: .heavy))
                .foregroundColor(textColor)
        }
        .frame(width: 36, height: 36)
    }
}

/// Small pill-shaped label shown on race cards.
private struct StatusChip: View {

    let label: String
    let background: Color
    var textColor: Color = .white

    var body: some View {
        Text(label)
            .font(.system(size: 10, weight: .semibold))
            .foregroundColor(textColor)
            .kerning(0.5)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(background)
            .clipShape(RoundedCornerShape(radius: 4))
    }
}

/// Convenience `Shape` for a uniform-corner rectangle used by `StatusChip`.
private struct RoundedCornerShape: Shape {
    let radius: CGFloat
    func path(in rect: CGRect) -> Path {
        RoundedRectangle(cornerRadius: radius).path(in: rect)
    }
}

#Preview {
    HomeView()
}
