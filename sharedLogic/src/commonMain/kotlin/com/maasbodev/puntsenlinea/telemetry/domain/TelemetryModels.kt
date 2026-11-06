package com.maasbodev.puntsenlinea.telemetry.domain

/**
 * Represents a validated, PII-sanitized custom analytics event ready to be forwarded
 * to the platform analytics service (`FR-4`, `FR-5`).
 */
data class SanitizedEvent(
    val name: String,
    val parameters: Map<String, String>
)

/**
 * Represents a validated, PII-sanitized non-fatal error report ready to be forwarded
 * to the platform crash reporting service (`FR-5`, `FR-6`).
 */
data class SanitizedNonFatalError(
    val message: String,
    val metadata: Map<String, String>
)
