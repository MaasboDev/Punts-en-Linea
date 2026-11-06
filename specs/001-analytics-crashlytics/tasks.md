# Tasks 001 — Analytics & Crash Reporting

Tasks are ordered by dependency (maximum 20–30 minutes each). Every task follows a tests-first workflow where applicable and ends with a verifiable check.

- [x] **T1. Implement pure domain models and `TelemetrySanitizer` with unit tests in `:sharedLogic`.** FR-4, FR-5, FR-6, FR-7, NFR-1
  - Create `TelemetryModels.kt` (`SanitizedEvent`, `SanitizedNonFatalError`) and `TelemetrySanitizer.kt` in `sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/`.
  - Write comprehensive unit tests first in `sharedLogic/src/commonTest/kotlin/com/maasbodev/puntsenlinea/telemetry/TelemetrySanitizerTest.kt` covering identifier validation (`1..40` chars, starts with ASCII letter, alphanumeric/underscore), normalized PII key blocklist stripping, `@` value stripping, 100-char string value truncation, 25-parameter cap, non-fatal error message email redaction (`[REDACTED_EMAIL]`) and 500-char truncation, and opaque User ID validation (`1..128` chars, no `@`).
  - Done when: `./gradlew :sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test` passes with 100% of `TelemetrySanitizerTest` cases green.

- [x] **T2. Implement `TelemetryServices` contracts and `TelemetryCoordinator` with unit tests in `:sharedLogic`.** FR-2, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10
  - Create `TelemetryServices.kt` (`AnalyticsService`, `CrashReportingService`) and `TelemetryCoordinator.kt` in `sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/telemetry/domain/`.
  - Write unit tests first in `sharedLogic/src/commonTest/kotlin/com/maasbodev/puntsenlinea/telemetry/TelemetryCoordinatorTest.kt` using handwritten fakes (`FakeAnalyticsService`, `FakeCrashReportingService`) to verify event logging, screen view logging, non-fatal error recording, `setUserId` propagation/clearing across both services, and `triggerTestCrash()` execution in debug mode (`isDebugBuild = true`) vs. no-op in release mode (`isDebugBuild = false`).
  - Done when: `./gradlew :sharedLogic:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test` passes with all `TelemetryCoordinatorTest` cases green.

- [x] **T3. Add Android Firebase Gradle plugins, BoM dependencies, and platform telemetry bridges in `:sharedLogic` and `:androidApp`.** FR-1, FR-2, FR-3, FR-4, FR-6, FR-7, FR-9, NFR-4
  - Declare `firebase-bom`, `firebase-analytics`, `firebase-crashlytics`, `googleServices`, and `firebaseCrashlytics` in `gradle/libs.versions.toml`, root `build.gradle.kts`, `sharedLogic/build.gradle.kts`, and `androidApp/build.gradle.kts` (enabling `buildConfig = true`).
  - Implement `PlatformTelemetry.kt` (`commonMain`), `PlatformTelemetry.android.kt` (`androidMain` backed by `FirebaseAnalytics` and `FirebaseCrashlytics`), and `PlatformTelemetry.ios.kt` (`iosMain` bridge registration and `SharedLogic` export), and initialize `TelemetryRegistry` with `BuildConfig.DEBUG` in `MainActivity.kt`.
  - Done when: `./gradlew :sharedLogic:testAndroidHostTest :sharedLogic:linkDebugFrameworkIosSimulatorArm64 :androidApp:assembleDebug` succeeds without errors.

- [x] **T4. Localize `:sharedUI` (`ca` default & `es`) and render the debug-only Test Crash button in `App.kt`.** FR-2, FR-8, FR-9, FR-10, NFR-2, NFR-3
  - Create `sharedUI/src/commonMain/composeResources/values/strings.xml` (Catalan `ca` default, including `test_crash_button` = `"Provar fallada"`) and `values-es/strings.xml` (Spanish `es`, including `test_crash_button` = `"Probar fallo"`), replacing all hardcoded strings in `App.kt`.
  - Log the initial `main_screen` view via `TelemetryCoordinator` and render the debug-only Test Crash button below the primary content when `telemetry.isDebugBuild` is `true`, styled with `Secondary` (`#8C583A` Light / `#D7BCA8` Dark) container, `OnSecondary` (`#FFFFFF` Light / `#3E2414` Dark) text, and `16.dp` rounded corners.
  - Done when: `./gradlew :sharedUI:testAndroidHostTest :androidApp:assembleDebug` succeeds and zero hardcoded UI strings remain in `App.kt`.

- [x] **T5. Integrate iOS Firebase Analytics & Crashlytics bridge, `ca`/`es` String Catalog, SwiftUI Test Crash button, and Xcode dSYM upload phase.** FR-1, FR-2, FR-3, FR-8, FR-9, FR-10, NFR-2, NFR-3, NFR-4
  - Create `iosApp/iosApp/Localizable.xcstrings` (`ca` default and `es` translations, with `"Provar fallada"` / `"Probar fallo"`) and `iosApp/iosApp/FirebaseTelemetryBridge.swift` implementing `SharedLogic`'s `AnalyticsService` and `CrashReportingService` with `FirebaseAnalytics` and `FirebaseCrashlytics`.
  - Update `iOSApp.swift` to call `FirebaseApp.configure()` and register `FirebaseTelemetryBridge`; update `ContentView.swift` to use localized strings, Light/Dark theme tokens (`Background`, `OnBackground`, `Secondary`, `OnSecondary`), `.onAppear` screen view logging, and the debug-only Test Crash button (`16pt` corner radius) below the main content.
  - Update `iosApp/iosApp.xcodeproj/project.pbxproj` to set `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"` in Debug, add `ca`/`es` to `knownRegions` (`developmentRegion = ca`), and add the `"Upload Crashlytics dSYMs"` `PBXShellScriptBuildPhase` invoking `${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run`.
  - Done when: `./gradlew :sharedLogic:iosSimulatorArm64Test` passes and `xcodebuild -project "iosApp/iosApp.xcodeproj" -scheme iosApp -destination "generic/platform=iOS Simulator" build` compiles cleanly with dSYM generation enabled.

- [x] **T6. Remove the Test Crash button, `test_crash_button` string entries, and forced-crash trigger methods across `:sharedLogic`, `:sharedUI`, `:androidApp`, and `iosApp`.** FR-8, NFR-2, NFR-3
  - Remove `triggerFatalCrash()` and `triggerTestCrash()` (and unused `isDebugBuild` plumbing) from `TelemetryServices.kt`, `TelemetryCoordinator.kt`, `PlatformTelemetry.kt`, `PlatformTelemetry.android.kt`, `PlatformTelemetry.ios.kt`, `MainActivity.kt`, `FirebaseTelemetryBridge.swift`, `iOSApp.swift`, and `TelemetryCoordinatorTest.kt`.
  - Remove the Test Crash button UI and `test_crash_button` string resources from `App.kt`, `sharedUI/src/commonMain/composeResources/values/strings.xml`, `values-es/strings.xml`, `ContentView.swift`, and `iosApp/iosApp/Localizable.xcstrings`.
  - Done when: `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest :sharedLogic:iosSimulatorArm64Test :sharedLogic:linkDebugFrameworkIosSimulatorArm64 :androidApp:assembleDebug` passes and no manual test crash button or `test_crash_button` resource remains.
