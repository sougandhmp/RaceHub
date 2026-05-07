import SwiftUI

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: UInt64
        switch hex.count {
        case 6: (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        default: (r, g, b) = (0, 0, 0)
        }
        self.init(red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255)
    }
}

struct AppColors {
    let background: Color
    let card: Color
    let cardBorder: Color
    let mutedText: Color
    let primaryText: Color
    let navBar: Color
    let headerBg: Color
    let rowAltBg: Color
    let isDark: Bool

    static let racingRed = Color(hex: "E63946")

    static func forScheme(_ scheme: ColorScheme) -> AppColors {
        switch scheme {
        case .dark:
            return AppColors(
                background: Color(hex: "0A0A0A"),
                card: Color(hex: "161616"),
                cardBorder: Color(hex: "262626"),
                mutedText: Color(hex: "8E8E93"),
                primaryText: .white,
                navBar: Color(hex: "0A0A0A"),
                headerBg: Color(hex: "2A1116"),
                rowAltBg: Color(hex: "1B0E11"),
                isDark: true
            )
        default:
            return AppColors(
                background: Color(hex: "F2F2F7"),
                card: .white,
                cardBorder: Color(hex: "E5E5EA"),
                mutedText: Color(hex: "6C6C70"),
                primaryText: Color(hex: "000000"),
                navBar: .white,
                headerBg: Color(hex: "FFECEE"),
                rowAltBg: Color(hex: "FFF5F6"),
                isDark: false
            )
        }
    }
}
