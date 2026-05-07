import SwiftUI
import Combine

enum ThemeMode: String, CaseIterable {
    case system, dark, light

    var displayName: String {
        switch self {
        case .system: return "System"
        case .dark:   return "Dark"
        case .light:  return "Light"
        }
    }
}

final class ThemeManager: ObservableObject {
    @Published var themeMode: ThemeMode {
        didSet { UserDefaults.standard.set(themeMode.rawValue, forKey: "theme_mode") }
    }

    init() {
        if let stored = UserDefaults.standard.string(forKey: "theme_mode"),
           let mode = ThemeMode(rawValue: stored) {
            themeMode = mode
        } else if UserDefaults.standard.object(forKey: "is_dark_mode") != nil {
            // migrate from old boolean key
            themeMode = UserDefaults.standard.bool(forKey: "is_dark_mode") ? .dark : .light
        } else {
            themeMode = .system
        }
    }

    // nil means "follow system" — pass directly to .preferredColorScheme()
    var preferredColorScheme: ColorScheme? {
        switch themeMode {
        case .system: return nil
        case .dark:   return .dark
        case .light:  return .light
        }
    }
}
