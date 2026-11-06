import Foundation
import FirebaseAnalytics
import FirebaseCrashlytics
import SharedLogic

/// Swift implementation of `SharedLogic.AnalyticsService` backed by `FirebaseAnalytics`
/// (`FR-1`, `FR-2`, `FR-4`, `FR-7`, `NFR-4`).
final class FirebaseAnalyticsBridge: AnalyticsService {

    func logEvent(event: SanitizedEvent) {
        let parameters: [String: Any]? = event.parameters.isEmpty
            ? nil
            : Dictionary(uniqueKeysWithValues: event.parameters.map { ($0.key, $0.value as Any) })
        Analytics.logEvent(event.name, parameters: parameters)
    }

    func setUserId(userId: String?) {
        Analytics.setUserID(userId)
    }
}

/// Swift implementation of `SharedLogic.CrashReportingService` backed by `FirebaseCrashlytics`
/// (`FR-1`, `FR-3`, `FR-6`, `FR-7`, `NFR-4`).
final class FirebaseCrashReportingBridge: CrashReportingService {

    private static let nonFatalErrorDomain = "com.maasbodev.puntsenlinea.nonfatal"

    func recordNonFatalError(error: SanitizedNonFatalError) {
        let crashlytics = Crashlytics.crashlytics()
        var userInfo: [String: Any] = [NSLocalizedDescriptionKey: error.message]

        for (key, value) in error.metadata {
            crashlytics.setCustomValue(value, forKey: key)
            userInfo[key] = value
        }

        let nsError = NSError(
            domain: Self.nonFatalErrorDomain,
            code: 0,
            userInfo: userInfo
        )
        crashlytics.record(error: nsError)
    }

    func setUserId(userId: String?) {
        Crashlytics.crashlytics().setUserID(userId ?? "")
    }
}

/// Combined iOS Firebase telemetry bridge conforming to `SharedLogic`'s `IOSTelemetryBridge`
/// (`AnalyticsService` and `CrashReportingService`) (`FR-1`, `FR-2`, `FR-3`, `FR-4`, `FR-6`, `FR-7`, `NFR-4`).
final class FirebaseTelemetryBridge: IOSTelemetryBridge {

    let analytics = FirebaseAnalyticsBridge()
    let crashReporting = FirebaseCrashReportingBridge()

    func logEvent(event: SanitizedEvent) {
        analytics.logEvent(event: event)
    }

    func recordNonFatalError(error: SanitizedNonFatalError) {
        crashReporting.recordNonFatalError(error: error)
    }

    func setUserId(userId: String?) {
        analytics.setUserId(userId: userId)
        crashReporting.setUserId(userId: userId)
    }
}
