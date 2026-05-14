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
                Text("ROUND \(Int(race.round))")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(race.isCompleted ? colors.mutedText : AppColors.racingRed)

                Spacer()

                if race.isCompleted {
                    Text("COMPLETED")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(colors.mutedText)
                } else {
                    if let days = race.daysRemaining {
                        Text("\(Int(truncating: days)) DAYS")
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
                Text(race.countryFlag)
                    .font(.system(size: 18))
            }

            Text(race.circuit)
                .font(.system(size: 12))
                .foregroundColor(colors.mutedText)

            Text(race.date)
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
