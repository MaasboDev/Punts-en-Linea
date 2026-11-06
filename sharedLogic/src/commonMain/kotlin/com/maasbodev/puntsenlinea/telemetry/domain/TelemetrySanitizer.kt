package com.maasbodev.puntsenlinea.telemetry.domain

/**
 * Pure, stateless domain sanitizer enforcing telemetry identifier validation and
 * deterministic PII exclusion rules (`FR-4`, `FR-5`, `FR-6`, `FR-7`, `NFR-1`).
 */
object TelemetrySanitizer {

    private const val MAX_IDENTIFIER_LENGTH = 40
    private const val MAX_PARAMETER_VALUE_LENGTH = 100
    private const val MAX_PARAMETER_COUNT = 25
    private const val MAX_ERROR_MESSAGE_LENGTH = 500
    private const val MAX_USER_ID_LENGTH = 128
    private const val REDACTED_EMAIL_TOKEN = "[REDACTED_EMAIL]"

    private val PII_BLOCKLIST: Set<String> = setOf(
        "username",
        "name",
        "email",
        "mealdescription",
        "description",
        "weight",
        "startofweekweight",
        "endofweekweight",
        "password",
        "token",
        "accesstoken",
        "secret",
        "phone"
    )

    /**
     * Checks whether [raw] (after trimming) is a Valid Telemetry Identifier:
     * 1..40 characters, starts with an ASCII letter (`a-z`, `A-Z`), and contains
     * only ASCII alphanumeric characters and underscores (`a-z`, `A-Z`, `0-9`, `_`).
     */
    fun isValidIdentifier(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.length !in 1..MAX_IDENTIFIER_LENGTH) return false
        val firstChar = trimmed[0]
        if (!firstChar.isAsciiLetter()) return false
        for (i in 0 until trimmed.length) {
            val ch = trimmed[i]
            if (!ch.isAsciiLetter() && !ch.isAsciiDigit() && ch != '_') {
                return false
            }
        }
        return true
    }

    /**
     * Converts [rawKey] to lowercase with all underscores (`_`), hyphens (`-`),
     * and whitespace characters removed for deterministic PII blocklist comparison.
     */
    fun normalizeKey(rawKey: String): String {
        val sb = StringBuilder(rawKey.length)
        for (ch in rawKey) {
            if (ch != '_' && ch != '-' && !ch.isWhitespace()) {
                sb.append(ch.lowercaseChar())
            }
        }
        return sb.toString()
    }

    /**
     * Returns `true` if the normalized form of [rawKey] matches the PII blocklist.
     */
    fun isPiiKey(rawKey: String): Boolean {
        return normalizeKey(rawKey) in PII_BLOCKLIST
    }

    /**
     * Deterministically sanitizes parameters in iteration order (`FR-5`):
     * 1. Trims each key and strips invalid telemetry identifiers.
     * 2. Strips any entry whose normalized key is in the PII blocklist or whose trimmed string value contains `@`.
     * 3. Trims string values and truncates values exceeding 100 characters to 100 characters.
     * 4. Retains at most the first 25 valid, non-PII parameters.
     */
    fun sanitizeParameters(rawParams: Map<String, Any?>): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        for ((rawKey, rawValue) in rawParams) {
            if (result.size >= MAX_PARAMETER_COUNT) break
            if (rawValue == null) continue

            val trimmedKey = rawKey.trim()
            if (!isValidIdentifier(trimmedKey)) continue
            if (isPiiKey(trimmedKey)) continue

            val trimmedValue = rawValue.toString().trim()
            if ('@' in trimmedValue) continue

            val sanitizedValue = if (trimmedValue.length > MAX_PARAMETER_VALUE_LENGTH) {
                trimmedValue.substring(0, MAX_PARAMETER_VALUE_LENGTH)
            } else {
                trimmedValue
            }
            result[trimmedKey] = sanitizedValue
        }
        return result
    }

    /**
     * Validates and sanitizes a custom analytics event (`FR-4`, `FR-5`).
     * Returns `null` if the trimmed event name is blank or not a Valid Telemetry Identifier.
     */
    fun sanitizeEvent(
        rawName: String,
        rawParams: Map<String, Any?> = emptyMap()
    ): SanitizedEvent? {
        val trimmedName = rawName.trim()
        if (!isValidIdentifier(trimmedName)) return null
        val sanitizedParams = sanitizeParameters(rawParams)
        return SanitizedEvent(name = trimmedName, parameters = sanitizedParams)
    }

    /**
     * Validates and sanitizes a non-fatal error message and its diagnostic metadata (`FR-5`, `FR-6`).
     * Replaces any whitespace-delimited token containing `@` with `[REDACTED_EMAIL]` and truncates
     * the resulting message to at most 500 characters. Returns `null` if the trimmed message is blank.
     */
    fun sanitizeNonFatalError(
        rawMessage: String,
        rawMetadata: Map<String, Any?> = emptyMap()
    ): SanitizedNonFatalError? {
        val trimmedMessage = rawMessage.trim()
        if (trimmedMessage.isEmpty()) return null

        val tokens = trimmedMessage.split(Regex("\\s+"))
        val redactedMessage = tokens.joinToString(" ") { token ->
            if ('@' in token) REDACTED_EMAIL_TOKEN else token
        }

        val truncatedMessage = if (redactedMessage.length > MAX_ERROR_MESSAGE_LENGTH) {
            redactedMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH)
        } else {
            redactedMessage
        }

        val sanitizedMetadata = sanitizeParameters(rawMetadata)
        return SanitizedNonFatalError(message = truncatedMessage, metadata = sanitizedMetadata)
    }

    /**
     * Validates and trims an Opaque User Identifier (`FR-7`).
     * Returns the trimmed identifier if it is 1..128 characters and does not contain `@`;
     * otherwise returns `null` to clear the active user identifier.
     */
    fun sanitizeUserId(rawUserId: String?): String? {
        if (rawUserId == null) return null
        val trimmed = rawUserId.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_USER_ID_LENGTH || '@' in trimmed) {
            return null
        }
        return trimmed
    }

    private fun Char.isAsciiLetter(): Boolean =
        this in 'a'..'z' || this in 'A'..'Z'

    private fun Char.isAsciiDigit(): Boolean =
        this in '0'..'9'
}
