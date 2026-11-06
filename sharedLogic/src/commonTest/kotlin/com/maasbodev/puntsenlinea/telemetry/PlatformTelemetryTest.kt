package com.maasbodev.puntsenlinea.telemetry

import com.maasbodev.puntsenlinea.telemetry.data.TelemetryRegistry
import com.maasbodev.puntsenlinea.telemetry.data.createPlatformTelemetryCoordinator
import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlatformTelemetryTest {

    private class RecordingAnalyticsService : AnalyticsService {
        val events = mutableListOf<SanitizedEvent>()
        var recordedUserId: String? = null

        override fun logEvent(event: SanitizedEvent) {
            events.add(event)
        }

        override fun setUserId(userId: String?) {
            this.recordedUserId = userId
        }
    }

    private class RecordingCrashReportingService : CrashReportingService {
        val errors = mutableListOf<SanitizedNonFatalError>()
        var recordedUserId: String? = null

        override fun recordNonFatalError(error: SanitizedNonFatalError) {
            errors.add(error)
        }

        override fun setUserId(userId: String?) {
            this.recordedUserId = userId
        }
    }

    @AfterTest
    fun tearDown() {
        TelemetryRegistry.reset()
    }

    @Test
    fun initializeAndroid_updatesCoordinator() {
        TelemetryRegistry.initializeAndroid()
        assertNotNull(TelemetryRegistry.coordinator)
    }

    @Test
    fun registerBridge_wiresCustomAnalyticsAndCrashServices() {
        val analytics = RecordingAnalyticsService()
        val crashReporting = RecordingCrashReportingService()

        TelemetryRegistry.registerBridge(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        TelemetryRegistry.coordinator.logScreenView(screenName = "main_screen", screenClass = "App")
        assertEquals(1, analytics.events.size)
        assertEquals("screen_view", analytics.events.single().name)

        TelemetryRegistry.coordinator.recordNonFatalError("Network timeout for user@example.com")
        assertEquals(1, crashReporting.errors.size)
        assertEquals("Network timeout for [REDACTED_EMAIL]", crashReporting.errors.single().message)

        TelemetryRegistry.coordinator.setUserId("opaque_user_42")
        assertEquals("opaque_user_42", analytics.recordedUserId)
        assertEquals("opaque_user_42", crashReporting.recordedUserId)
    }

    @Test
    fun createPlatformTelemetryCoordinator_returnsValidCoordinatorInstance() {
        val coordinator = createPlatformTelemetryCoordinator()
        assertNotNull(coordinator)
    }
}
