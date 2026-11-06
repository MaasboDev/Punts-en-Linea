package com.maasbodev.puntsenlinea.telemetry.data

import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator
import kotlin.concurrent.Volatile

/**
 * Combined bridge contract for iOS Swift implementations backed by `FirebaseAnalytics`
 * and `FirebaseCrashlytics` (`FR-1`, `FR-2`, `FR-3`, `FR-4`, `FR-6`, `FR-7`, `NFR-4`).
 */
interface IOSTelemetryBridge : AnalyticsService, CrashReportingService

@Volatile
private var registeredAnalyticsBridge: AnalyticsService? = null

@Volatile
private var registeredCrashReportingBridge: CrashReportingService? = null

private class IOSDelegatingAnalyticsService : AnalyticsService {
    override fun logEvent(event: SanitizedEvent) {
        registeredAnalyticsBridge?.logEvent(event)
    }

    override fun setUserId(userId: String?) {
        registeredAnalyticsBridge?.setUserId(userId)
    }
}

private class IOSDelegatingCrashReportingService : CrashReportingService {
    override fun recordNonFatalError(error: SanitizedNonFatalError) {
        registeredCrashReportingBridge?.recordNonFatalError(error)
    }

    override fun setUserId(userId: String?) {
        registeredCrashReportingBridge?.setUserId(userId)
    }
}

/**
 * Registers the Swift `FirebaseAnalytics` and `FirebaseCrashlytics` bridge implementations
 * and updates [TelemetryRegistry] (`FR-1`, `NFR-4`).
 */
fun registerIOSTelemetryBridge(
    analytics: AnalyticsService,
    crashReporting: CrashReportingService
) {
    registeredAnalyticsBridge = analytics
    registeredCrashReportingBridge = crashReporting
    TelemetryRegistry.registerBridge(
        analyticsService = analytics,
        crashReportingService = crashReporting
    )
}

actual fun createPlatformTelemetryCoordinator(): TelemetryCoordinator = TelemetryCoordinator(
    analyticsService = IOSDelegatingAnalyticsService(),
    crashReportingService = IOSDelegatingCrashReportingService()
)
