import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        // The base URL comes from Config.xcconfig (per configuration) via Info.plist.
        guard let baseUrl = Bundle.main.object(forInfoDictionaryKey: "APIBaseURL") as? String, !baseUrl.isEmpty else {
            fatalError("APIBaseURL is missing from Info.plist; set API_BASE_URL in Config.xcconfig")
        }
        #if DEBUG
        let logNetwork = true
        #else
        let logNetwork = false
        #endif
        KoinInitializer.shared.start(baseUrl: baseUrl, logNetwork: logNetwork)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
