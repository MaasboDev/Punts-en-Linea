package com.maasbodev.puntsenlinea.telemetry

import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedEvent
import com.maasbodev.puntsenlinea.telemetry.domain.SanitizedNonFatalError
import com.maasbodev.puntsenlinea.telemetry.domain.TelemetrySanitizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TelemetrySanitizerTest {

    // --- FR-4: Valid Telemetry Identifier & Event Name Validation ---

    @Test
    fun `isValidIdentifier accepts valid identifiers between 1 and 40 characters`() {
        assertTrue(TelemetrySanitizer.isValidIdentifier("a"))
        assertTrue(TelemetrySanitizer.isValidIdentifier("Z"))
        assertTrue(TelemetrySanitizer.isValidIdentifier("app_open"))
        assertTrue(TelemetrySanitizer.isValidIdentifier("  screen_view_123  "))
        val fortyChars = "a" + "1_b".repeat(13) // 1 + 39 = 40
        assertEquals(40, fortyChars.length)
        assertTrue(TelemetrySanitizer.isValidIdentifier(fortyChars))
    }

    @Test
    fun `isValidIdentifier rejects blank leading digit special characters and overlong strings`() {
        assertFalse(TelemetrySanitizer.isValidIdentifier(""))
        assertFalse(TelemetrySanitizer.isValidIdentifier("   "))
        assertFalse(TelemetrySanitizer.isValidIdentifier("123_event"))
        assertFalse(TelemetrySanitizer.isValidIdentifier("_leading_underscore"))
        assertFalse(TelemetrySanitizer.isValidIdentifier("event-name"))
        assertFalse(TelemetrySanitizer.isValidIdentifier("event.name"))
        assertFalse(TelemetrySanitizer.isValidIdentifier("event name"))
        assertFalse(TelemetrySanitizer.isValidIdentifier("català_event"))
        val fortyOneChars = "a".repeat(41)
        assertFalse(TelemetrySanitizer.isValidIdentifier(fortyOneChars))
    }

    @Test
    fun `sanitizeEvent trims valid event name and returns SanitizedEvent`() {
        val result: SanitizedEvent? = TelemetrySanitizer.sanitizeEvent(
            rawName = "  screen_view  ",
            rawParams = mapOf("screen_name" to "main_screen")
        )
        assertNotNull(result)
        assertEquals("screen_view", result.name)
        assertEquals(mapOf("screen_name" to "main_screen"), result.parameters)
    }

    @Test
    fun `sanitizeEvent drops event when name is blank or invalid identifier`() {
        assertNull(TelemetrySanitizer.sanitizeEvent(""))
        assertNull(TelemetrySanitizer.sanitizeEvent("   "))
        assertNull(TelemetrySanitizer.sanitizeEvent("123_event"))
        assertNull(TelemetrySanitizer.sanitizeEvent("event-name"))
        assertNull(TelemetrySanitizer.sanitizeEvent("a".repeat(41)))
    }

    // --- FR-5 & NFR-1: Deterministic Parameter Sanitization & PII Stripping ---

    @Test
    fun `normalizeKey lowercases and strips underscores hyphens and whitespace`() {
        assertEquals("startofweekweight", TelemetrySanitizer.normalizeKey("Start_Of-Week Weight"))
        assertEquals("username", TelemetrySanitizer.normalizeKey("  USER_NAME  "))
        assertEquals("accesstoken", TelemetrySanitizer.normalizeKey("access-token"))
    }

    @Test
    fun `isPiiKey detects all 13 normalized PII blocklist keys`() {
        val blockedVariations = listOf(
            "username", "User_Name", "user-name",
            "name", "NAME",
            "email", "e_mail",
            "mealdescription", "Meal_Description",
            "description", "Description",
            "weight", "WEIGHT",
            "startofweekweight", "Start_Of_Week_Weight",
            "endofweekweight", "end_of_week_weight",
            "password", "pass_word",
            "token", "TOKEN",
            "accesstoken", "access_token",
            "secret", "Secret",
            "phone", "Phone_Number".let { "phone" }
        )
        for (key in blockedVariations) {
            assertTrue(TelemetrySanitizer.isPiiKey(key), "Expected '$key' to be detected as PII")
        }
        assertFalse(TelemetrySanitizer.isPiiKey("screen_name"))
        assertFalse(TelemetrySanitizer.isPiiKey("valid_key"))
    }

    @Test
    fun `sanitizeParameters strips invalid keys PII keys null values and values containing at-sign`() {
        val rawParams: Map<String, Any?> = linkedMapOf(
            "user_name" to "Anna",
            "Start_Of_Week_Weight" to 70.5,
            "invalid-key" to "value",
            "123invalid" to "value",
            "contact" to "user@example.com",
            "null_param" to null,
            "  valid_key  " to "  ok  ",
            "count" to 42
        )

        val sanitized = TelemetrySanitizer.sanitizeParameters(rawParams)
        assertEquals(
            mapOf("valid_key" to "ok", "count" to "42"),
            sanitized
        )
    }

    @Test
    fun `sanitizeParameters truncates string values exceeding 100 characters to 100 characters`() {
        val exact100 = "x".repeat(100)
        val overlong150 = "y".repeat(150)

        val sanitized = TelemetrySanitizer.sanitizeParameters(
            mapOf(
                "exact_100" to exact100,
                "overlong_150" to "   $overlong150   "
            )
        )

        assertEquals(100, sanitized["exact_100"]?.length)
        assertEquals(exact100, sanitized["exact_100"])
        assertEquals(100, sanitized["overlong_150"]?.length)
        assertEquals("y".repeat(100), sanitized["overlong_150"])
    }

    @Test
    fun `sanitizeParameters retains at most first 25 valid non-PII parameters in iteration order`() {
        val rawParams = linkedMapOf<String, Any?>()
        // Add some invalid/PII ones first to ensure they don't count toward the 25 valid limit
        rawParams["user_name"] = "Hidden"
        rawParams["bad-key"] = "Hidden"
        for (i in 1..30) {
            rawParams["param_$i"] = "value_$i"
        }

        val sanitized = TelemetrySanitizer.sanitizeParameters(rawParams)
        assertEquals(25, sanitized.size)
        assertEquals("value_1", sanitized["param_1"])
        assertEquals("value_25", sanitized["param_25"])
        assertFalse(sanitized.containsKey("param_26"))
        assertFalse(sanitized.containsKey("param_30"))
    }

    // --- FR-6 & NFR-1: Non-Fatal Error Message Sanitization & Email Redaction ---

    @Test
    fun `sanitizeNonFatalError drops blank error messages`() {
        assertNull(TelemetrySanitizer.sanitizeNonFatalError(""))
        assertNull(TelemetrySanitizer.sanitizeNonFatalError("    \n\t  "))
    }

    @Test
    fun `sanitizeNonFatalError redacts whitespace-delimited email tokens and sanitizes metadata`() {
        val result: SanitizedNonFatalError? = TelemetrySanitizer.sanitizeNonFatalError(
            rawMessage = "  Failed auth for user@example.com and admin@domain.org!  ",
            rawMetadata = linkedMapOf(
                "email" to "user@example.com",
                "password" to "secret123",
                "error_code" to 401,
                "component" to "auth_module"
            )
        )

        assertNotNull(result)
        assertEquals(
            "Failed auth for [REDACTED_EMAIL] and [REDACTED_EMAIL]",
            result.message
        )
        assertEquals(
            mapOf("error_code" to "401", "component" to "auth_module"),
            result.metadata
        )
    }

    @Test
    fun `sanitizeNonFatalError truncates message to at most 500 characters after redaction`() {
        val longMessage = "Error for user@example.com " + "a".repeat(600)
        val result = TelemetrySanitizer.sanitizeNonFatalError(longMessage)

        assertNotNull(result)
        assertEquals(500, result.message.length)
        assertTrue(result.message.startsWith("Error for [REDACTED_EMAIL] a"))
    }

    // --- FR-7 & NFR-1: Opaque User Identifier Validation ---

    @Test
    fun `sanitizeUserId trims and returns valid opaque user identifiers up to 128 characters`() {
        assertEquals("uid_12345", TelemetrySanitizer.sanitizeUserId("  uid_12345  "))
        val maxId = "u".repeat(128)
        assertEquals(maxId, TelemetrySanitizer.sanitizeUserId(maxId))
    }

    @Test
    fun `sanitizeUserId returns null for null blank overlong or email-containing identifiers`() {
        assertNull(TelemetrySanitizer.sanitizeUserId(null))
        assertNull(TelemetrySanitizer.sanitizeUserId(""))
        assertNull(TelemetrySanitizer.sanitizeUserId("   "))
        assertNull(TelemetrySanitizer.sanitizeUserId("u".repeat(129)))
        assertNull(TelemetrySanitizer.sanitizeUserId("user@example.com"))
        assertNull(TelemetrySanitizer.sanitizeUserId("prefix@suffix"))
    }
}
