package com.maasbodev.puntsenlinea.telemetry

import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TelemetryCoordinatorTest {

    private class FakeAnalyticsService : AnalyticsService {
        val loggedEvents = mutableListOf<SanitizedEvent>()
        var currentUserId: String? = "initial_unset"
        var setUserIdCallCount: Int = 0

        override fun logEvent(event: SanitizedEvent) {
            loggedEvents.add(event)
        }

        override fun setUserId(userId: String?) {
            currentUserId = userId
            setUserIdCallCount++
        }
    }

    private class FakeCrashReportingService : CrashReportingService {
        val recordedErrors = mutableListOf<SanitizedNonFatalError>()
        var currentUserId: String? = "initial_unset"
        var setUserIdCallCount: Int = 0

        override fun recordNonFatalError(error: SanitizedNonFatalError) {
            recordedErrors.add(error)
        }

        override fun setUserId(userId: String?) {
            currentUserId = userId
            setUserIdCallCount++
        }
    }

    // --- FR-4 & FR-5: Custom Event Logging ---

    @Test
    fun logEvent_withValidNameAndParameters_sanitizesAndForwardsToAnalyticsService() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        val result = coordinator.logEvent(
            name = "  button_click  ",
            parameters = mapOf(
                "screen_name" to "main_screen",
                "user_name" to "Anna",
                "email_hint" to "user@example.com",
                "step" to 2
            )
        )

        assertTrue(result)
        assertEquals(1, analytics.loggedEvents.size)
        val logged = analytics.loggedEvents.single()
        assertEquals("button_click", logged.name)
        assertEquals(
            mapOf("screen_name" to "main_screen", "step" to "2"),
            logged.parameters
        )
    }

    @Test
    fun logEvent_withInvalidOrBlankName_dropsEventWithoutCallingAnalyticsService() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        assertFalse(coordinator.logEvent(""))
        assertFalse(coordinator.logEvent("   "))
        assertFalse(coordinator.logEvent("123_invalid"))
        assertFalse(coordinator.logEvent("invalid-event"))
        assertFalse(coordinator.logEvent("a".repeat(41)))

        assertTrue(analytics.loggedEvents.isEmpty())
    }

    // --- FR-2: Baseline Screen View Logging ---

    @Test
    fun logScreenView_logsStandardScreenViewEventWithSanitizedParameters() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        coordinator.logScreenView(screenName = "main_screen", screenClass = "App")

        assertEquals(1, analytics.loggedEvents.size)
        val event = analytics.loggedEvents.single()
        assertEquals("screen_view", event.name)
        assertEquals(
            mapOf("screen_name" to "main_screen", "screen_class" to "App"),
            event.parameters
        )
    }

    // --- FR-5 & FR-6: Non-Fatal Error Recording ---

    @Test
    fun recordNonFatalError_withValidMessage_redactsEmailsSanitizesMetadataAndForwards() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        val recorded = coordinator.recordNonFatalError(
            message = "  Sync failed for account anna@example.com  ",
            metadata = mapOf(
                "retry_count" to 3,
                "access_token" to "secret-jwt",
                "contact" to "anna@example.com"
            )
        )

        assertTrue(recorded)
        assertEquals(1, crashReporting.recordedErrors.size)
        val error = crashReporting.recordedErrors.single()
        assertEquals("Sync failed for account [REDACTED_EMAIL]", error.message)
        assertEquals(mapOf("retry_count" to "3"), error.metadata)
    }

    @Test
    fun recordNonFatalError_withBlankMessage_dropsErrorWithoutCallingCrashReportingService() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        assertFalse(coordinator.recordNonFatalError(""))
        assertFalse(coordinator.recordNonFatalError("   \t\n "))
        assertTrue(crashReporting.recordedErrors.isEmpty())
    }

    // --- FR-7: Opaque User Identifier Lifecycle ---

    @Test
    fun setUserId_withValidOpaqueId_setsTrimmedIdOnBothAnalyticsAndCrashReporting() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        val sanitized = coordinator.setUserId("  uid_987654321  ")

        assertEquals("uid_987654321", sanitized)
        assertEquals("uid_987654321", analytics.currentUserId)
        assertEquals("uid_987654321", crashReporting.currentUserId)
        assertEquals(1, analytics.setUserIdCallCount)
        assertEquals(1, crashReporting.setUserIdCallCount)
    }

    @Test
    fun setUserId_withNullBlankOverlongOrEmailId_clearsUserIdAcrossBothServices() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        val invalidInputs = listOf(
            null,
            "",
            "   ",
            "user@example.com",
            "u".repeat(129)
        )

        for (input in invalidInputs) {
            analytics.currentUserId = "previous_id"
            crashReporting.currentUserId = "previous_id"

            val result = coordinator.setUserId(input)

            assertNull(result, "Expected null userId for input '$input'")
            assertNull(analytics.currentUserId)
            assertNull(crashReporting.currentUserId)
        }
    }
}
