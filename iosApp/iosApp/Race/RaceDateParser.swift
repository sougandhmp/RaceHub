import Foundation
import Shared

/// Parses an API timestamp via the shared Kotlin parser (zone-less values are UTC),
/// so iOS and Android always agree on what a race time means.
func parseRaceDate(_ dateTime: String) -> Date? {
    guard let instant = RaceDatesKt.parseRaceInstant(dateTime: dateTime) else { return nil }
    return Date(timeIntervalSince1970: Double(instant.toEpochMilliseconds()) / 1000)
}
