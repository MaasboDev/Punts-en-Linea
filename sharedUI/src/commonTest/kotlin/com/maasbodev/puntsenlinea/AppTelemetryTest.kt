package com.maasbodev.puntsenlinea

import com.maasbodev.puntsenlinea.telemetry.domain.AnalyticsService
import com.maasbodev.puntsenlinea.telemetry.domain.CrashReportingService
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetryCoordinator
import puntsenlinea.sharedui.generated.resources.Res
import puntsenlinea.sharedui.generated.resources.brand_logo_content_description
import puntsenlinea.sharedui.generated.resources.greeting_prefix
import puntsenlinea.sharedui.generated.resources.toggle_content_button
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppTelemetryTest {

    private class FakeAnalyticsService : AnalyticsService {
        val loggedEvents = mutableListOf<SanitizedEvent>()
        var activeUserId: String? = null

        override fun logEvent(event: SanitizedEvent) {
            loggedEvents.add(event)
        }

        override fun setUserId(userId: String?) {
            activeUserId = userId
        }
    }

    private class FakeCrashReportingService : CrashReportingService {
        val recordedErrors = mutableListOf<SanitizedNonFatalError>()
        var activeUserId: String? = null

        override fun recordNonFatalError(error: SanitizedNonFatalError) {
            recordedErrors.add(error)
        }

        override fun setUserId(userId: String?) {
            activeUserId = userId
        }
    }

    @Test
    fun `onMainScreenDisplayed logs main_screen view event with App class`() {
        val analytics = FakeAnalyticsService()
        val crashReporting = FakeCrashReportingService()
        val coordinator = TelemetryCoordinator(
            analyticsService = analytics,
            crashReportingService = crashReporting
        )

        onMainScreenDisplayed(coordinator)

        assertEquals(1, analytics.loggedEvents.size)
        val event = analytics.loggedEvents.single()
        assertEquals("screen_view", event.name)
        assertEquals("main_screen", event.parameters["screen_name"])
        assertEquals("App", event.parameters["screen_class"])
    }

    @Test
    fun `localized string resources are generated for main screen`() {
        assertNotNull(Res.string.toggle_content_button)
        assertNotNull(Res.string.greeting_prefix)
        assertNotNull(Res.string.brand_logo_content_description)
    }
}
