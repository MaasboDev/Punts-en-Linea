package com.maasbodev.puntsenlinea.telemetry.data

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator

/**
 * Android implementation of [AnalyticsService] backed by [com.google.firebase.analytics.FirebaseAnalytics]
 * (`FR-1`, `FR-2`, `FR-4`, `FR-7`, `NFR-4`).
 */
class AndroidFirebaseAnalyticsService : AnalyticsService {

    override fun logEvent(event: SanitizedEvent) {
        val bundle = Bundle().apply {
            for ((key, value) in event.parameters) {
                putString(key, value)
            }
        }
        Firebase.analytics.logEvent(event.name, bundle)
    }

    override fun setUserId(userId: String?) {
        Firebase.analytics.setUserId(userId)
    }
}

/**
 * Android implementation of [CrashReportingService] backed by [com.google.firebase.crashlytics.FirebaseCrashlytics]
 * (`FR-1`, `FR-3`, `FR-6`, `FR-7`, `NFR-4`).
 */
class AndroidFirebaseCrashReportingService : CrashReportingService {

    override fun recordNonFatalError(error: SanitizedNonFatalError) {
        val crashlytics = Firebase.crashlytics
        for ((key, value) in error.metadata) {
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(RuntimeException(error.message))
    }

    override fun setUserId(userId: String?) {
        Firebase.crashlytics.setUserId(userId.orEmpty())
    }
}

actual fun createPlatformTelemetryCoordinator(): TelemetryCoordinator = TelemetryCoordinator(
    analyticsService = AndroidFirebaseAnalyticsService(),
    crashReportingService = AndroidFirebaseCrashReportingService()
)
