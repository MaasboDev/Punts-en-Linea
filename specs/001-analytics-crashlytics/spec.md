# Spec 001 — Analytics & Crash Reporting
Status: implemented

## Context and objective
To ensure application stability, monitor release health, and understand anonymized usage patterns across both mobile platforms, **Punts en Línia** requires automated usage analytics and crash reporting from launch. In addition, the project needs a foundation for recording custom domain events, non-fatal errors, and opaque user identifiers in upcoming features while keeping the main application interface free of manual crash-trigger controls.

## Users
- **End Users (Guests and Registered Users)**: Benefit from proactive crash detection and stability improvements while having their personal and nutritional data protected from telemetry leaks.
- **Product & Maintainer Team**: Need visibility into app launches, active sessions, screen views, non-fatal errors, and symbolicated crash reports to diagnose issues and monitor stability.

## User stories
- **US-1**: As a maintainer, I want the application to automatically report app launches, session starts, screen views, and unhandled crashes on startup so that I can monitor usage and stability without manual user intervention.
- **US-2**: As a maintainer, I want standardized capabilities to log custom events, record non-fatal errors with contextual keys, and associate or clear an opaque user identifier so that future features can report telemetry consistently.
- **US-3**: As an end user, I want my personal and nutritional details (such as my name, email address, weight entries, and meal descriptions) excluded from all analytics events and crash reports so that my privacy is protected.

## Definitions
- **PII (Personally Identifiable Information)**: Any data that could identify an individual or expose private health/nutritional habits or credentials, explicitly governed by a normalized key blocklist (`username`, `name`, `email`, `mealdescription`, `description`, `weight`, `startofweekweight`, `endofweekweight`, `password`, `token`, `accesstoken`, `secret`, `phone`) and email address patterns (any value containing `@`).
- **Normalized Parameter Key**: A parameter key converted to lowercase with all underscores (`_`), hyphens (`-`), and whitespace characters removed, used for deterministic comparison against the PII key blocklist.
- **Valid Telemetry Identifier**: A trimmed string of 1 to 40 characters that starts with an ASCII letter (`a-z`, `A-Z`) and contains only ASCII alphanumeric characters and underscores (`a-z`, `A-Z`, `0-9`, `_`).
- **Opaque User Identifier**: A trimmed, non-blank, non-human-readable system identifier of 1 to 128 characters that does not contain `@` (email PII).

## Functional requirements
- **FR-1**: WHEN the application starts, THE SYSTEM shall initialize usage analytics and crash reporting automatically by default.
- **FR-2**: WHILE the application is running, THE SYSTEM shall automatically record baseline usage telemetry, including application launch, session start, and screen view events.
- **FR-3**: WHEN an unhandled fatal error or crash occurs, THE SYSTEM shall capture the crash report (including symbolication metadata required to resolve human-readable stack traces) and transmit it to the monitoring service upon the next available network connection.
- **FR-4**: WHEN a feature requests to log a custom analytics event, THE SYSTEM shall trim the event name and, if it is a Valid Telemetry Identifier (`1..40` characters, starting with an ASCII letter, containing only `a-z`, `A-Z`, `0-9`, `_`), forward the event with its sanitized parameters to the analytics service; IF the trimmed event name is blank or does not match the Valid Telemetry Identifier rules, THEN THE SYSTEM shall drop the entire event without recording it.
- **FR-5**: WHEN sanitizing parameters for a custom analytics event or diagnostic metadata for a non-fatal error report, THE SYSTEM shall deterministically process entries in iteration order as follows:
  1. Trim each parameter key and strip any entry whose key is not a Valid Telemetry Identifier (`1..40` characters, starting with an ASCII letter, containing only `a-z`, `A-Z`, `0-9`, `_`).
  2. Strip any entry whose Normalized Parameter Key matches the PII blocklist (`username`, `name`, `email`, `mealdescription`, `description`, `weight`, `startofweekweight`, `endofweekweight`, `password`, `token`, `accesstoken`, `secret`, `phone`) or whose trimmed string value contains `@`.
  3. Trim string parameter values and truncate any string value exceeding 100 characters to exactly 100 characters.
  4. Retain at most the first 25 valid, non-PII parameters and strip any additional parameters beyond the 25th entry, while preserving the event or error report itself.
- **FR-6**: WHEN a feature records a non-fatal error with an error message and optional diagnostic metadata, THE SYSTEM shall trim the error message, replace any whitespace-delimited token containing `@` with `[REDACTED_EMAIL]`, truncate the resulting message to at most 500 characters, apply the parameter sanitization rules from `FR-5` to the diagnostic metadata, and forward the report to the crash reporting service without terminating the application; IF the trimmed error message is blank, THEN THE SYSTEM shall drop the non-fatal error report without recording it.
- **FR-7**: WHEN a user identifier update is requested, THE SYSTEM shall trim the provided value and, if it is a valid Opaque User Identifier (`1..128` characters and does not contain `@`), set that identifier across both usage analytics and crash reporting; IF the provided value is `null`, blank after trimming, longer than 128 characters, or contains `@`, THEN THE SYSTEM shall clear the active user identifier (`null`) across both usage analytics and crash reporting.
- **FR-8**: WHILE the application is running in any build configuration, THE SYSTEM shall not render or expose any manual test crash button or forced-crash UI control on the main screen.

## Non-functional requirements
- **NFR-1 (Privacy & Data Protection)**: THE SYSTEM shall never transmit PII (`username`, `name`, `email`, `mealdescription`, `description`, `weight`, `startofweekweight`, `endofweekweight`, `password`, `token`, `accesstoken`, `secret`, `phone`, or any string containing `@`) in analytics events, user identifiers, or crash logs, in strict compliance with Principle 5 of the Project Constitution.
- **NFR-2 (Localization)**: THE SYSTEM shall externalize 100% of user-facing strings on the main screen in both Catalan (`ca`, default) and Spanish (`es`) with zero hardcoded UI strings, in compliance with Principle 6 of the Project Constitution.
- **NFR-3 (Theme & Visual Token Compliance)**: THE SYSTEM shall style the main screen using the project's established Light and Dark mode color tokens (`Primary`, `OnPrimary`, `Background`, `OnBackground`, `Surface`, `OnSurface`).
- **NFR-4 (Offline Resilience)**: IF the device has no network connectivity when an analytics event or crash occurs, THEN THE SYSTEM shall queue the telemetry locally and transmit it automatically once connectivity is restored.

## Edge cases
- **Blank or malformed event names (`FR-4`)**: Logging an event with `""`, `"   "`, `"123_event"`, `"event-name"`, or a name of 41+ characters drops the entire event without throwing an unhandled exception.
- **Invalid or PII parameter keys and values (`FR-5`)**: Logging an event with valid name `"screen_view"` and parameters `{"user_name": "Anna", "Start-Of-Week-Weight": "70.5", "contact": "user@example.com", "valid_key": "ok"}` strips the first three entries and forwards `"screen_view"` with `{"valid_key": "ok"}`.
- **Overlong parameter values and excess parameter count (`FR-5`)**: A string value of 150 characters is truncated to 100 characters; if 30 valid parameters are supplied, only the first 25 in iteration order are retained.
- **Blank, PII-containing, or overlong non-fatal error messages (`FR-6`)**: A blank message `"   "` is dropped; a message `"Failed auth for user@example.com"` has the email token redacted to `"Failed auth for [REDACTED_EMAIL]"` and truncated to 500 characters if longer.
- **Null, blank, overlong, or email User ID (`FR-7`)**: Passing `null`, `""`, `"   "`, a 129-character string, or `"user@example.com"` clears the active user identifier (`null`) in both analytics and crash reporting.
- **Crash on launch before network availability (`FR-3`, `NFR-4`)**: If an unhandled crash occurs while the device is offline, the crash payload persists on disk and uploads on the next app launch once connectivity is available.
- **Symbolication (`FR-3`)**: Crash reports generated on both mobile platforms include debug symbol mappings so stack traces are readable in the monitoring console.

## Out of scope
- In-app manual test crash trigger UI or forced-crash actions.
- In-app opt-in/opt-out privacy toggle UI (telemetry is enabled by default with strict PII exclusion).
- Domain-specific analytics events for authentication, user profile management, weight tracking, or meal logging (these will be specified in their respective feature specs).
- Remote configuration, A/B testing, or push notification messaging.

## Completion criteria
- Analytics and crash reporting initialize automatically on startup on both supported mobile platforms.
- Deterministic domain sanitization and validation rules (`FR-4`, `FR-5`, `FR-6`, `FR-7`) are 100% verified by automated unit tests covering all boundary conditions and edge cases.
- The main screen contains zero hardcoded UI strings (100% externalized in `ca` and `es`), adheres to Light and Dark mode color tokens, and does not display or expose any manual test crash button (`FR-8`, `NFR-2`, `NFR-3`).

## Open questions
- None. All initial questions and QA clarifications have been resolved.
