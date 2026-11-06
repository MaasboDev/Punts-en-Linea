package com.maasbodev.puntsenlinea.telemetry.data

import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator

/**
 * Creates a platform-backed [TelemetryCoordinator] instance (`FR-1`, `FR-2`, `FR-3`, `NFR-4`).
 */
expect fun createPlatformTelemetryCoordinator(): TelemetryCoordinator

/**
 * Shared singleton accessor providing the active [TelemetryCoordinator] to `:sharedUI`,
 * `:androidApp`, and `iosApp` (`FR-1`, `FR-8`).
 */
object TelemetryRegistry {

    var coordinator: TelemetryCoordinator = createPlatformTelemetryCoordinator()
        private set

    /**
     * Initializes the shared [TelemetryCoordinator] for Android (`FR-1`).
     */
    fun initializeAndroid() {
        coordinator = createPlatformTelemetryCoordinator()
    }

    /**
     * Registers platform bridge implementations of [AnalyticsService] and [CrashReportingService]
     * (`FR-1`, `NFR-4`).
     */
    fun registerBridge(
        analyticsService: AnalyticsService,
        crashReportingService: CrashReportingService
    ) {
        coordinator = TelemetryCoordinator(
            analyticsService = analyticsService,
            crashReportingService = crashReportingService
        )
    }

    /**
     * Replaces the active [coordinator] (useful for previews and testing).
     */
    fun configure(customCoordinator: TelemetryCoordinator) {
        coordinator = customCoordinator
    }

    /**
     * Resets the registry to a default platform coordinator for isolated unit tests.
     */
    fun reset() {
        coordinator = createPlatformTelemetryCoordinator()
    }
}
