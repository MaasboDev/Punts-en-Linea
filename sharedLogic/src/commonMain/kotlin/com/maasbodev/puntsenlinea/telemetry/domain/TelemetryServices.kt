package com.maasbodev.puntsenlinea.telemetry.domain

/**
 * Domain contract for forwarding sanitized usage analytics events and user identifiers
 * to the underlying platform analytics provider (`FR-2`, `FR-4`, `FR-7`).
 */
interface AnalyticsService {
    fun logEvent(event: SanitizedEvent)
    fun setUserId(userId: String?)
}

/**
 * Domain contract for recording sanitized non-fatal errors and associating user identifiers
 * (`FR-6`, `FR-7`).
 */
interface CrashReportingService {
    fun recordNonFatalError(error: SanitizedNonFatalError)
    fun setUserId(userId: String?)
}
