import SwiftUI
import Shared

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

private let t = AppColorTokens.shared

struct AppColors {
    let background: Color
    let card: Color
    let cardBorder: Color
    let surface: Color
    let mutedText: Color
    let primaryText: Color
    let navBar: Color
    let headerBg: Color
    let rowAltBg: Color
    let isDark: Bool

    static let racingRed = Color(hex: t.racingRed)
    static let f1Red     = Color(hex: t.f1Red)

    static func forScheme(_ scheme: ColorScheme) -> AppColors {
        switch scheme {
        case .dark:
            return AppColors(
                background: Color(hex: t.darkBackground),
                card:        Color(hex: t.darkCard),
                cardBorder:  Color(hex: t.darkCardBorder),
                surface:     Color(hex: t.darkSurface),
                mutedText:   Color(hex: t.darkMutedText),
                primaryText: .white,
                navBar:      Color(hex: t.darkNavBar),
                headerBg:    Color(hex: t.darkHeaderBg),
                rowAltBg:    Color(hex: t.darkRowAltBg),
                isDark: true
            )
        default:
            return AppColors(
                background: Color(hex: t.lightBackground),
                card:        Color(hex: t.lightCard),
                cardBorder:  Color(hex: t.lightCardBorder),
                surface:     Color(hex: t.lightSurface),
                mutedText:   Color(hex: t.lightMutedText),
                primaryText: .black,
                navBar:      Color(hex: t.lightNavBar),
                headerBg:    Color(hex: t.lightHeaderBg),
                rowAltBg:    Color(hex: t.lightRowAltBg),
                isDark: false
            )
        }
    }

    // MARK: - Team colors (sourced from AppColorTokens)
    static var teamColor: (String) -> Color = { team in
        let lower = team.lowercased()
        if lower.contains("mercedes") { return Color(hex: t.teamMercedes) }
        if lower.contains("mclaren")  { return Color(hex: t.teamMcLaren)  }
        if lower.contains("red bull") { return Color(hex: t.teamRedBull)  }
        if lower.contains("ferrari")  { return Color(hex: t.teamFerrari)  }
        if lower.contains("aston")    { return Color(hex: t.teamAston)    }
        if lower.contains("alpine")   { return Color(hex: t.teamAlpine)   }
        if lower.contains("williams") { return Color(hex: t.teamWilliams) }
        if lower.contains("rb")       { return Color(hex: t.teamRb)       }
        if lower.contains("haas")     { return Color(hex: t.teamHaas)     }
        if lower.contains("sauber")   { return Color(hex: t.teamSauber)   }
        return Color(hex: t.teamDefault)
    }

    // MARK: - Podium accents (sourced from AppColorTokens)
    static func podiumAccent(position: Int, fallback: Color) -> Color {
        switch position {
        case 1:  return Color(hex: t.gold)
        case 2:  return Color(hex: t.silver)
        case 3:  return Color(hex: t.bronze)
        default: return fallback
        }
    }
}
