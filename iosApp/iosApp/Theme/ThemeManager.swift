import SwiftUI
import Combine

final class ThemeManager: ObservableObject {
    @Published var isDarkMode: Bool {
        didSet { UserDefaults.standard.set(isDarkMode, forKey: "is_dark_mode") }
    }

    init() {
        if UserDefaults.standard.object(forKey: "is_dark_mode") != nil {
            isDarkMode = UserDefaults.standard.bool(forKey: "is_dark_mode")
        } else {
            isDarkMode = true
        }
    }

    var colorScheme: ColorScheme { isDarkMode ? .dark : .light }
}
