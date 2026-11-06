import SwiftUI
import FirebaseCore
import SharedLogic

@main
struct iOSApp: App {
    private let telemetryBridge = FirebaseTelemetryBridge()

    init() {
        FirebaseApp.configure()
        PlatformTelemetry_iosKt.registerIOSTelemetryBridge(
            analytics: telemetryBridge.analytics,
            crashReporting: telemetryBridge.crashReporting
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
