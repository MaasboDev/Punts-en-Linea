package com.maasbodev.puntsenlinea.telemetry.domain

/**
 * Domain coordinator orchestrating validation, PII sanitization, and forwarding of
 * analytics events, non-fatal errors, and opaque user identifiers
 * (`FR-2`, `FR-4`, `FR-5`, `FR-6`, `FR-7`, `FR-8`).
 */
class TelemetryCoordinator(
    private val analyticsService: AnalyticsService,
    private val crashReportingService: CrashReportingService
) {

    /**
     * Validates and sanitizes [name] and [parameters] via [TelemetrySanitizer] (`FR-4`, `FR-5`).
     * Forwards the event to [AnalyticsService] and returns `true` if valid; drops the event
     * and returns `false` if [name] is not a Valid Telemetry Identifier.
     */
    fun logEvent(
        name: String,
        parameters: Map<String, Any?> = emptyMap()
    ): Boolean {
        val sanitized = TelemetrySanitizer.sanitizeEvent(
            rawName = name,
            rawParams = parameters
        ) ?: return false
        analyticsService.logEvent(sanitized)
        return true
    }

    /**
     * Logs a baseline screen view event (`FR-2`) with sanitized `screen_name` and `screen_class`.
     */
    fun logScreenView(screenName: String, screenClass: String) {
        logEvent(
            name = SCREEN_VIEW_EVENT_NAME,
            parameters = mapOf(
                PARAM_SCREEN_NAME to screenName,
                PARAM_SCREEN_CLASS to screenClass
            )
        )
    }

    /**
     * Validates and sanitizes a non-fatal error [message] and optional [metadata] (`FR-5`, `FR-6`).
     * Forwards the sanitized error to [CrashReportingService] and returns `true` if the message
     * is non-blank; drops the report and returns `false` if the trimmed message is blank.
     */
    fun recordNonFatalError(
        message: String,
        metadata: Map<String, Any?> = emptyMap()
    ): Boolean {
        val sanitized = TelemetrySanitizer.sanitizeNonFatalError(
            rawMessage = message,
            rawMetadata = metadata
        ) ?: return false
        crashReportingService.recordNonFatalError(sanitized)
        return true
    }

    /**
     * Validates and trims [userId] via [TelemetrySanitizer.sanitizeUserId] (`FR-7`),
     * propagating the sanitized opaque identifier (or `null` when invalid/cleared) across both
     * [AnalyticsService] and [CrashReportingService].
     */
    fun setUserId(userId: String?): String? {
        val sanitizedUserId = TelemetrySanitizer.sanitizeUserId(userId)
        analyticsService.setUserId(sanitizedUserId)
        crashReportingService.setUserId(sanitizedUserId)
        return sanitizedUserId
    }

    private companion object {
        private const val SCREEN_VIEW_EVENT_NAME = "screen_view"
        private const val PARAM_SCREEN_NAME = "screen_name"
        private const val PARAM_SCREEN_CLASS = "screen_class"
    }
}
