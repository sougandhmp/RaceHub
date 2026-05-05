import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        // Initialize Koin with the same API base URL as Android
        // Using HTTP IP address like Android for development/testing
        KoinInitializer.shared.start(baseUrl: "http://140.245.233.203:30018")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
