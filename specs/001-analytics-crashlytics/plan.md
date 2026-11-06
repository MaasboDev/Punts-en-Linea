# Plan 001 — Analytics & Crash Reporting

## 1. Architectural Overview & Requirement Traceability

This plan implements **Spec 001 (`specs/001-analytics-crashlytics/spec.md`)** in strict compliance with [`docs/constitution.md`](../../docs/constitution.md) and [`AGENTS.md`](../../AGENTS.md):
- **Clean Architecture & SOLID in `:sharedLogic`**:
  - **`domain` layer**: Pure, platform-agnostic telemetry models (`SanitizedEvent`, `SanitizedNonFatalError`), deterministic validation/PII-sanitization rules (`TelemetrySanitizer`), repository/service contracts (`AnalyticsService`, `CrashReportingService`), and focused coordinator (`TelemetryCoordinator`).
  - **`data` layer**: Platform/bridge implementations of `AnalyticsService` and `CrashReportingService` that delegate to native Firebase SDKs on Android and iOS.
- **UI & Localization (`:sharedUI`, `:androidApp`, `iosApp`)**:
  - Zero hardcoded user-facing strings; 100% externalized in Catalan (`ca`, default) and Spanish (`es`).
  - The main screen adheres to Light and Dark mode color tokens (`Primary`, `OnPrimary`, `Background`, `OnBackground`, `Surface`, `OnSurface`) and does not render or expose any manual test crash button (`FR-8`).

| Requirement | Covered By |
| :--- | :--- |
| **FR-1** (Auto-init analytics & crash reporting on startup) | `:androidApp` (`google-services` + `firebase-crashlytics` plugins, `MainActivity.kt`), `iosApp` (`iOSApp.swift` calling `FirebaseApp.configure()` and registering `FirebaseTelemetryBridge`) |
| **FR-2** (Baseline telemetry: app launch, session start, screen views) | Firebase Analytics SDK auto-collection + initial main screen view logged via `TelemetryCoordinator` on `App.kt` / `ContentView.swift` appearance |
| **FR-3** (Fatal crash capture, symbolication metadata & upload) | Android `firebase-crashlytics` Gradle plugin + SDK; iOS `FirebaseCrashlytics` SPM + `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"` + Xcode Crashlytics dSYM upload Run Script phase |
| **FR-4** (Custom event name validation & forwarding) | `TelemetrySanitizer.sanitizeEvent(...)` + `TelemetryCoordinator.logEvent(...)` in `:sharedLogic` |
| **FR-5** (Deterministic parameter sanitization & PII stripping) | `TelemetrySanitizer.sanitizeParameters(...)` in `:sharedLogic` |
| **FR-6** (Non-fatal error message sanitization, email redaction & forwarding) | `TelemetrySanitizer.sanitizeNonFatalError(...)` + `TelemetryCoordinator.recordNonFatalError(...)` in `:sharedLogic` |
| **FR-7** (User ID validation & lifecycle across analytics and crash reporting) | `TelemetrySanitizer.sanitizeUserId(...)` + `TelemetryCoordinator.setUserId(...)` in `:sharedLogic` |
| **FR-8** (No manual test crash button or forced-crash UI control on main screen) | `:sharedLogic` (`TelemetryServices.kt`, `TelemetryCoordinator.kt`, `PlatformTelemetry.*`), `:sharedUI` (`App.kt`, `values/strings.xml`, `values-es/strings.xml`), and `iosApp` (`ContentView.swift`, `FirebaseTelemetryBridge.swift`, `Localizable.xcstrings`) |
| **NFR-1** (Zero PII in telemetry) | Enforced by `TelemetrySanitizer` blocklist, `@` detection, and `[REDACTED_EMAIL]` replacement |
| **NFR-2** (100% `ca` default and `es` localization, zero hardcoded strings) | Compose Multiplatform string resources (`values`/`values-es`) and iOS String Catalog (`Localizable.xcstrings`) |
| **NFR-3** (Theme & visual token compliance) | Main screen styled with Light/Dark tokens (`Primary`, `OnPrimary`, `Background`, `OnBackground`) |
| **NFR-4** (Offline resilience) | Native Firebase Analytics & Crashlytics disk persistence and automatic retry on reconnect |

---

## 2. Files to Create or Modify

### Build & Configuration
1. **`gradle/libs.versions.toml` (Modify)** — *Covers FR-1, FR-2, FR-3, NFR-4*
   - Add approved versions and libraries for Android Firebase:
     - `firebase-bom = "33.10.0"`, `google-services = "4.4.2"`, `firebase-crashlytics-plugin = "3.0.3"`
     - `firebase-bom` (`com.google.firebase:firebase-bom`), `firebase-analytics` (`com.google.firebase:firebase-analytics`), `firebase-crashlytics` (`com.google.firebase:firebase-crashlytics`)
     - Plugins: `googleServices` (`com.google.gms.google-services`) and `firebaseCrashlytics` (`com.google.firebase.crashlytics`).
2. **`build.gradle.kts` (Modify)** — *Covers FR-1, FR-3*
   - Declare `alias(libs.plugins.googleServices) apply false` and `alias(libs.plugins.firebaseCrashlytics) apply false`.
3. **`androidApp/build.gradle.kts` (Modify)** — *Covers FR-1, FR-2, FR-3*
   - Apply `alias(libs.plugins.googleServices)` and `alias(libs.plugins.firebaseCrashlytics)`.
   - Add `implementation(platform(libs.firebase.bom))`, `implementation(libs.firebase.analytics)`, and `implementation(libs.firebase.crashlytics)` to `dependencies`.
4. **`sharedLogic/build.gradle.kts` (Modify)** — *Covers FR-1, FR-2, FR-3*
   - In `androidMain.dependencies`, add `implementation(project.dependencies.platform(libs.firebase.bom))`, `implementation(libs.firebase.analytics)`, and `implementation(libs.firebase.crashlytics)`.

### `:sharedLogic` Module (`com.maasbodev.puntsenlinea.telemetry`)
5. **`sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/TelemetryModels.kt` (Create)** — *Covers FR-4, FR-5, FR-6*
   - Value/data classes:
     - `data class SanitizedEvent(val name: String, val parameters: Map<String, String>)`
     - `data class SanitizedNonFatalError(val message: String, val metadata: Map<String, String>)`
6. **`sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/TelemetrySanitizer.kt` (Create)** — *Covers FR-4, FR-5, FR-6, FR-7, NFR-1*
   - Pure, stateless object containing deterministic validation and PII-stripping functions:
     - `fun isValidIdentifier(raw: String): Boolean`
     - `fun normalizeKey(rawKey: String): String`
     - `fun isPiiKey(rawKey: String): Boolean`
     - `fun sanitizeParameters(rawParams: Map<String, Any?>): Map<String, String>`
     - `fun sanitizeEvent(rawName: String, rawParams: Map<String, Any?> = emptyMap()): SanitizedEvent?`
     - `fun sanitizeNonFatalError(rawMessage: String, rawMetadata: Map<String, Any?> = emptyMap()): SanitizedNonFatalError?`
     - `fun sanitizeUserId(rawUserId: String?): String?`
7. **`sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/TelemetryServices.kt` (Create/Modify)** — *Covers FR-2, FR-4, FR-6, FR-7, FR-8*
   - Lean interfaces following Interface Segregation (ISP) and Dependency Inversion (DIP) with no forced-crash method:
     - `interface AnalyticsService { fun logEvent(event: SanitizedEvent); fun setUserId(userId: String?) }`
     - `interface CrashReportingService { fun recordNonFatalError(error: SanitizedNonFatalError); fun setUserId(userId: String?) }`
8. **`sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/TelemetryCoordinator.kt` (Create/Modify)** — *Covers FR-2, FR-4, FR-5, FR-6, FR-7, FR-8*
   - Domain facade/use-case coordinator injected with `AnalyticsService` and `CrashReportingService` (without `triggerTestCrash()` or `isDebugBuild`):
     - `fun logEvent(name: String, parameters: Map<String, Any?> = emptyMap()): Boolean`
     - `fun logScreenView(screenName: String, screenClass: String)`
     - `fun recordNonFatalError(message: String, metadata: Map<String, Any?> = emptyMap()): Boolean`
     - `fun setUserId(userId: String?): String?`
9. **`sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/data/PlatformTelemetry.kt` (Create/Modify)** — *Covers FR-1, FR-8*
   - `expect fun createPlatformTelemetryCoordinator(): TelemetryCoordinator`
   - Holds a shared singleton accessor `object TelemetryRegistry` so `:sharedUI` and `iosApp` can access the active `TelemetryCoordinator`.
10. **`sharedLogic/src/androidMain/kotlin/com/maasbodev/puntsenlinea/telemetry/data/PlatformTelemetry.android.kt` (Create/Modify)** — *Covers FR-1, FR-2, FR-3, FR-4, FR-6, FR-7, FR-8, NFR-4*
    - `AndroidFirebaseAnalyticsService` using `com.google.firebase.analytics.FirebaseAnalytics` (`Firebase.analytics`) to log `SanitizedEvent` bundles and set user ID.
    - `AndroidFirebaseCrashReportingService` using `com.google.firebase.crashlytics.FirebaseCrashlytics` (`Firebase.crashlytics`) to set custom keys, record non-fatal `RuntimeException(error.message)`, and set user ID.
11. **`sharedLogic/src/iosMain/kotlin/com/maasbodev/puntsenlinea/telemetry/data/PlatformTelemetry.ios.kt` (Create/Modify)** — *Covers FR-1, FR-2, FR-3, FR-4, FR-6, FR-7, FR-8, NFR-4*
    - Defines `fun registerIOSTelemetryBridge(analytics: AnalyticsService, crashReporting: CrashReportingService)` so `iosApp` can register Swift implementations backed by `FirebaseAnalytics` and `FirebaseCrashlytics`, with safe queuing/no-op fallback before registration.

### `:sharedUI` & `:androidApp` Modules
12. **`sharedUI/src/commonMain/composeResources/values/strings.xml` (Create/Modify)** — *Covers FR-8, NFR-2*
    - Catalan (`ca` default) strings for the main screen (without `test_crash_button`):
      - `toggle_content_button`: `"Mostra / oculta el contingut"`
      - `greeting_prefix`: `"Salutacions des de %1$s"`
      - `brand_logo_content_description`: `"Logotip de Punts en Línia"`
13. **`sharedUI/src/commonMain/composeResources/values-es/strings.xml` (Create/Modify)** — *Covers FR-8, NFR-2*
    - Spanish (`es`) translations (without `test_crash_button`):
      - `toggle_content_button`: `"Mostrar / ocultar el contenido"`
      - `greeting_prefix`: `"Saludos desde %1$s"`
      - `brand_logo_content_description`: `"Logotipo de Punts en Línia"`
14. **`sharedUI/src/commonMain/kotlin/com/maasbodev/puntsenlinea/App.kt` (Modify)** — *Covers FR-2, FR-8, NFR-2, NFR-3*
    - Replace all hardcoded strings with `stringResource(Res.string.*)`.
    - Accept `telemetry: TelemetryCoordinator = TelemetryRegistry.coordinator` as a parameter with default value for testability and previews.
    - Log screen view on initial composition (`LaunchedEffect(telemetry) { telemetry.logScreenView("main_screen", "App") }`).
    - Ensure no manual test crash button is rendered (`FR-8`).
15. **`androidApp/src/main/kotlin/com/maasbodev/puntsenlinea/MainActivity.kt` (Modify)** — *Covers FR-1, FR-8*
    - Initialize `TelemetryRegistry.initializeAndroid()` in `onCreate` before `setContent { App() }`.

### `iosApp` (SwiftUI & Xcode Project)
16. **`iosApp/iosApp/Localizable.xcstrings` (Create/Modify)** — *Covers FR-8, NFR-2*
    - Xcode String Catalog with default language `ca` (Catalan) and translation `es` (Spanish) for all user-facing keys in `ContentView.swift` (removing `test_crash_button`):
      - `toggle_content_button`: `ca` = `"Mostra / oculta el contingut"`, `es` = `"Mostrar / ocultar el contenido"`
      - `greeting_prefix`: `ca` = `"Salutacions des de %@"`, `es` = `"Saludos desde %@"`
      - `brand_logo_content_description`: `ca` = `"Logotip de Punts en Línia"`, `es` = `"Logotipo de Punts en Línia"`
17. **`iosApp/iosApp/FirebaseTelemetryBridge.swift` (Create/Modify)** — *Covers FR-1, FR-2, FR-3, FR-4, FR-6, FR-7, FR-8, NFR-4*
    - Swift classes conforming to `SharedLogic.AnalyticsService` and `SharedLogic.CrashReportingService` (without `triggerFatalCrash()`):
      - Calls `FirebaseAnalytics.Analytics.logEvent(event.name, parameters: event.parameters)` and `Analytics.setUserID(userId)`.
      - Calls `Crashlytics.crashlytics().setCustomValue(...)`, `Crashlytics.crashlytics().record(error: NSError(...))`, and `Crashlytics.crashlytics().setUserID(userId)`.
18. **`iosApp/iosApp/iOSApp.swift` (Modify)** — *Covers FR-1, FR-8*
    - Import `FirebaseCore` and `SharedLogic`.
    - In `init()`, call `FirebaseApp.configure()` and register `FirebaseTelemetryBridge` with `TelemetryRegistry`.
19. **`iosApp/iosApp/ContentView.swift` (Modify)** — *Covers FR-2, FR-8, NFR-2, NFR-3*
    - Use localized strings from `Localizable.xcstrings`.
    - Apply Light/Dark theme tokens matching `AGENTS.md` (`Background`: `#F4F5F7` Light / `#181412` Dark; `OnBackground`: `#241E1A` Light / `#EDE4DF` Dark; `Primary`: `#B57850` Light / `#E5A982` Dark; `OnPrimary`: `#FFFFFF` Light / `#44230D` Dark).
    - Log main screen view on `.onAppear`.
    - Ensure no manual test crash button is rendered (`FR-8`).
20. **`iosApp/iosApp.xcodeproj/project.pbxproj` (Modify)** — *Covers FR-3, NFR-2*
    - Keep `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";` in both `Debug` and `Release` configurations and the `"Upload Crashlytics dSYMs"` `PBXShellScriptBuildPhase`.
    - Keep `ca` and `es` in `knownRegions` with `developmentRegion = ca`.

### Automated Unit Tests (`kotlin.test`)
21. **`sharedLogic/src/commonTest/kotlin/com/maasbodev/puntsenlinea/telemetry/TelemetrySanitizerTest.kt` (Create)** — *Covers FR-4, FR-5, FR-6, FR-7, NFR-1*
    - Comprehensive unit tests for every validation, normalization, PII blocklist, truncation, parameter count cap, and email redaction rule.
22. **`sharedLogic/src/commonTest/kotlin/com/maasbodev/puntsenlinea/telemetry/TelemetryCoordinatorTest.kt` (Create/Modify)** — *Covers FR-2, FR-4, FR-5, FR-6, FR-7, FR-8*
    - Unit tests using handwritten fakes (`FakeAnalyticsService`, `FakeCrashReportingService`) verifying event forwarding, screen view logging, dropped invalid events, non-fatal error recording, and user ID setting/clearing across both services.

---

## 3. Pure Domain Functions & Algorithm in Pseudocode (`:sharedLogic`)

All validation and PII filtering resides in `TelemetrySanitizer` (`sharedLogic/src/commonMain/.../telemetry/domain/TelemetrySanitizer.kt`) with zero external dependencies:

```text
CONSTANT PII_BLOCKLIST = setOf(
    "username", "name", "email", "mealdescription", "description",
    "weight", "startofweekweight", "endofweekweight",
    "password", "token", "accesstoken", "secret", "phone"
)

FUNCTION isValidIdentifier(raw: String): Boolean
    trimmed = raw.trim()
    IF trimmed.length NOT IN 1..40 THEN RETURN false
    firstChar = trimmed[0]
    IF firstChar NOT IN ('a'..'z' UNION 'A'..'Z') THEN RETURN false
    FOR char IN trimmed:
        IF char NOT IN ('a'..'z' UNION 'A'..'Z' UNION '0'..'9' UNION {'_'}) THEN
            RETURN false
    RETURN true

FUNCTION normalizeKey(rawKey: String): String
    RETURN rawKey.lowercase()
                 .filter { char -> char != '_' AND char != '-' AND !char.isWhitespace() }

FUNCTION isPiiKey(rawKey: String): Boolean
    RETURN normalizeKey(rawKey) IN PII_BLOCKLIST

FUNCTION sanitizeParameters(rawParams: Map<String, Any?>): Map<String, String>
    result = LinkedHashMap<String, String>()
    FOR (rawKey, rawValue) IN rawParams:
        IF result.size >= 25 THEN BREAK
        IF rawValue == null THEN CONTINUE
        trimmedKey = rawKey.trim()
        IF NOT isValidIdentifier(trimmedKey) THEN CONTINUE
        IF isPiiKey(trimmedKey) THEN CONTINUE

        trimmedValue = rawValue.toString().trim()
        IF '@' IN trimmedValue THEN CONTINUE

        sanitizedValue = IF trimmedValue.length > 100
                         THEN trimmedValue.substring(0, 100)
                         ELSE trimmedValue
        result[trimmedKey] = sanitizedValue
    RETURN result

FUNCTION sanitizeEvent(rawName: String, rawParams: Map<String, Any?>): SanitizedEvent?
    trimmedName = rawName.trim()
    IF NOT isValidIdentifier(trimmedName) THEN RETURN null
    sanitizedParams = sanitizeParameters(rawParams)
    RETURN SanitizedEvent(name = trimmedName, parameters = sanitizedParams)

FUNCTION sanitizeNonFatalError(rawMessage: String, rawMetadata: Map<String, Any?>): SanitizedNonFatalError?
    trimmedMessage = rawMessage.trim()
    IF trimmedMessage.isEmpty() THEN RETURN null

    // Replace any whitespace-delimited token containing '@' with "[REDACTED_EMAIL]"
    redactedMessage = trimmedMessage
        .splitByWhitespacePreservingSingleSpaces()
        .map { token -> IF '@' IN token THEN "[REDACTED_EMAIL]" ELSE token }
        .joinToString(" ")

    truncatedMessage = IF redactedMessage.length > 500
                       THEN redactedMessage.substring(0, 500)
                       ELSE redactedMessage

    sanitizedMetadata = sanitizeParameters(rawMetadata)
    RETURN SanitizedNonFatalError(message = truncatedMessage, metadata = sanitizedMetadata)

FUNCTION sanitizeUserId(rawUserId: String?): String?
    IF rawUserId == null THEN RETURN null
    trimmed = rawUserId.trim()
    IF trimmed.isEmpty() OR trimmed.length > 128 OR '@' IN trimmed THEN
        RETURN null
    RETURN trimmed
```

---

## 4. UI Rendering Strategy (`:sharedUI` & `iosApp`)

### `:sharedUI` (Compose Multiplatform)
- `App(telemetry: TelemetryCoordinator = TelemetryRegistry.coordinator)` applies `LightColorScheme` / `DarkColorScheme` matching `AGENTS.md` (`NFR-3`).
- On initial composition, `LaunchedEffect(telemetry) { telemetry.logScreenView(screenName = "main_screen", screenClass = "App") }` records the baseline screen view (`FR-2`).
- All strings are loaded via `stringResource(Res.string.*)` from `composeResources/values/strings.xml` (`ca` default) and `composeResources/values-es/strings.xml` (`es`) (`NFR-2`).
- No manual test crash button is rendered in any build configuration (`FR-8`).

### `iosApp` (SwiftUI)
- `ContentView` observes `@Environment(\.colorScheme)` to apply the exact hex tokens from `AGENTS.md`:
  - `Background`: `#F4F5F7` (Light) / `#181412` (Dark)
  - `OnBackground`: `#241E1A` (Light) / `#EDE4DF` (Dark)
  - `Primary`: `#B57850` (Light) / `#E5A982` (Dark)
  - `OnPrimary`: `#FFFFFF` (Light) / `#44230D` (Dark)
- On `.onAppear`, calls `TelemetryRegistry.shared.coordinator.logScreenView(screenName: "main_screen", screenClass: "ContentView")` (`FR-2`).
- All user-facing strings use `Localizable.xcstrings` (`ca` default, `es` Spanish) (`NFR-2`).
- No manual test crash button is rendered in any build configuration (`FR-8`).

---

## 5. Technical Decisions & Discarded Alternatives

1. **Pure Kotlin `TelemetrySanitizer` + Interface Bridge in `:sharedLogic` vs. Third-Party KMP Firebase Wrappers (`gitlive-firebase`)**
   - *Decision*: Use the official Google Firebase Android BoM (`com.google.firebase:firebase-bom`) in `:androidApp`/`:sharedLogic` (`androidMain`) and the already-installed official `firebase-ios-sdk` SPM packages in `iosApp`, bridged via clean Kotlin interfaces (`AnalyticsService`, `CrashReportingService`) in `:sharedLogic`.
   - *Discarded Alternative*: Adding a third-party KMP wrapper library such as `dev.gitlive:firebase-*`.
   - *Rationale*: Principle 1 (Stack Simplicity) forbids unapproved third-party libraries; `iosApp` already links official `FirebaseAnalytics` and `FirebaseCrashlytics` via SPM, and keeping `:sharedLogic/commonMain` free of platform SDK dependencies allows 100% deterministic unit testing with `kotlin.test` on both Android and iOS targets.
2. **Removing Forced Crash Methods from Domain Contracts (`FR-8`)**
   - *Decision*: Now that Firebase Console verification is complete on both Android and iOS, remove `triggerFatalCrash()` / `triggerTestCrash()` and the UI test crash button from `:sharedLogic`, `:sharedUI`, and `iosApp`.
   - *Discarded Alternative*: Keeping a hidden or debug-flagged test crash button in the main UI.
   - *Rationale*: Keeps the domain interfaces (`CrashReportingService`, `TelemetryCoordinator`) and main UI clean and free of artificial crash triggers (`FR-8`).
3. **Enabling `dwarf-with-dsym` for Both Debug and Release in Xcode + Crashlytics `run` Script Phase**
   - *Decision*: Keep `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"` and the Crashlytics Run Script build phase referencing `${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run`.
   - *Discarded Alternative*: Generating dSYMs only in `Release` builds.
   - *Rationale*: Ensures all unhandled crashes across both development and release builds are automatically symbolicated in Firebase Console (`FR-3`).

---

## 6. Testing Strategy (`kotlin.test`)

In compliance with Principle 4 (Zero-Dependency Testing), all tests use only `kotlin.test` and handwritten fakes (`FakeAnalyticsService`, `FakeCrashReportingService`):

1. **`TelemetrySanitizerTest`** (`sharedLogic/src/commonTest`):
   - Validates `FR-4`: trims valid event names (`" app_open "` -> `"app_open"`), boundary lengths (`1` char `"a"`, `40` chars), rejects blank (`""`, `"   "`), leading digit (`"1event"`), hyphen/special char (`"event-name"`), and `41`-char event names.
   - Validates `FR-5`: strips invalid parameter keys, strips all 13 normalized PII blocklist keys regardless of casing/underscores/hyphens (`"User_Name"`, `"start-of-week-weight"`, `"ACCESS_TOKEN"`), strips parameters whose values contain `@` (`"user@domain.com"`), truncates string values of `>100` chars to `100` chars, and caps parameter count at the first `25` valid entries in iteration order.
   - Validates `FR-6`: drops blank error messages, redacts email tokens containing `@` with `"[REDACTED_EMAIL]"`, truncates messages `>500` chars to `500` chars, and applies `FR-5` parameter sanitization to metadata.
   - Validates `FR-7`: trims valid opaque user IDs (`1..128` chars without `@`), and returns `null` for `null`, blank, `129`-char strings, or strings containing `@`.
2. **`TelemetryCoordinatorTest`** (`sharedLogic/src/commonTest`):
   - Uses `FakeAnalyticsService` and `FakeCrashReportingService` to verify:
     - Valid events and `logScreenView` calls are sanitized and forwarded; invalid events are dropped without invoking `AnalyticsService`.
     - Valid non-fatal errors are sanitized and forwarded; blank error messages are dropped.
     - `setUserId` updates both `AnalyticsService` and `CrashReportingService` with the sanitized ID or clears both with `null`.
3. **Verification Commands**:
   - `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest`
   - `./gradlew :sharedLogic:iosSimulatorArm64Test`
