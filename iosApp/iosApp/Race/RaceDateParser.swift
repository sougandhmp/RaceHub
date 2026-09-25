import Foundation

/// Parses the date-time strings the backend returns for races.
///
/// The API sends local-less timestamps such as `2026-05-24T20:00` (no seconds,
/// no zone), which `ISO8601DateFormatter` rejects. Those are treated as UTC,
/// matching the Android `parseIsoToDate`. Fully-qualified ISO 8601 strings
/// (with `Z` or an offset, optionally with fractional seconds) are also accepted.
func parseRaceDate(_ dateTime: String) -> Date? {
    let s = dateTime.trimmingCharacters(in: .whitespaces)
    if s.isEmpty { return nil }

    for formatter in isoFormatters {
        if let date = formatter.date(from: s) { return date }
    }
    for formatter in utcFormatters {
        if let date = formatter.date(from: s) { return date }
    }
    return nil
}

private let isoFormatters: [ISO8601DateFormatter] = {
    let plain = ISO8601DateFormatter()
    let fractional = ISO8601DateFormatter()
    fractional.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
    return [plain, fractional]
}()

private let utcFormatters: [DateFormatter] = [
    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd",
].map { pattern in
    let formatter = DateFormatter()
    formatter.dateFormat = pattern
    formatter.locale = Locale(identifier: "en_US_POSIX")
    formatter.timeZone = TimeZone(identifier: "UTC")
    return formatter
}
