import SwiftUI
import Shared

struct ScheduleView: View {
    let schedule: [Race]
    let latestThread: TrendingThread?
    @Environment(\.dismiss) var dismiss

    var body: some View {
        let nextRace = schedule.first { !$0.isCompleted }

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
                        if let thread = latestThread {
                            ScheduleLatestThreadCard(thread: thread)
                            Divider()
                                .background(Color(hex: "2A2A2A"))
                                .padding(.vertical, 8)
                        }
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

private struct ScheduleLatestThreadCard: View {
    let thread: TrendingThread

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("LATEST DISCUSSION")
                    .font(.system(size: 11, weight: .heavy))
                    .kerning(1)
                    .foregroundColor(Color(hex: "E10600"))
                Spacer()
                Text("TRENDING")
                    .font(.system(size: 9, weight: .black))
                    .foregroundColor(Color(hex: "E10600"))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color(hex: "E10600").opacity(0.1))
                    .cornerRadius(4)
            }

            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(Color(hex: "E10600"))
                        .frame(width: 28, height: 28)
                    Text("💬")
                        .font(.system(size: 14))
                }

                VStack(alignment: .leading, spacing: 4) {
                    Text(thread.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.white)
                        .lineLimit(2)
                    HStack(spacing: 4) {
                        Text("❤️")
                            .font(.system(size: 12))
                        Text("\(Int(thread.likes)) likes")
                            .font(.system(size: 11))
                            .foregroundColor(Color(hex: "888888"))
                    }
                }
                Spacer()
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "1A1A1A"))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "2A2A2A"), lineWidth: 1)
        )
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

            Text(race.circuit)
                .font(.system(size: 12))
                .foregroundColor(Color(hex: "888888"))

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


