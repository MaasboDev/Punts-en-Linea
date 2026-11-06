# MEMORY.md — Punts en Linea
Project record across sessions. Maximum ~50 lines: summarize or remove content that is no longer relevant.

## Current Status
- Initial KMP project scaffolding in place (`:sharedLogic`, `:sharedUI`, `:androidApp`, and `iosApp`), with `local.properties` pointing to the local Android SDK.
- Brand logo (`logo.svg`) integrated across platforms (`:sharedUI`, `:androidApp`, and `iosApp`) with Light/Dark theme tokens.
- Firebase configurations (`google-services.json`, `GoogleService-Info.plist`) and iOS SPM dependencies (`FirebaseAuth`, `FirebaseFirestore`, `FirebaseAnalytics`, `FirebaseCrashlytics`) added.
- Project rules, domain specifications, design tokens, and `MEMORY.md` workflow defined in `AGENTS.md`, governed by `docs/constitution.md` and active feature specs (`specs/NNN-*/`).
- **Spec 001 (`001-analytics-crashlytics`)**: Approved `spec.md`, `plan.md`, and `tasks.md`. Completed all tasks **T1–T6**:
  - **T1–T2**: Pure domain `TelemetryModels`, `TelemetrySanitizer`, `TelemetryServices`, and `TelemetryCoordinator` in `:sharedLogic` with `kotlin.test` unit suites (`TelemetrySanitizerTest`, `TelemetryCoordinatorTest`).
  - **T3**: Firebase Android BoM/plugins (`google-services`, `firebase-crashlytics`), `PlatformTelemetry` (`commonMain`, `androidMain`, `iosMain`), `MainActivity.kt` initialization, and `PlatformTelemetryTest`.
  - **T4**: `ca` (default) and `es` Compose string resources in `:sharedUI`, localized `App.kt` with `main_screen` logging, and `AppTelemetryTest`.
  - **T5**: iOS `Localizable.xcstrings` (`ca`/`es`), `FirebaseTelemetryBridge.swift` (`FirebaseAnalytics` & `FirebaseCrashlytics`), `iOSApp.swift` auto-init, localized/themed `ContentView.swift`, and Xcode `dwarf-with-dsym` + `"Upload Crashlytics dSYMs"` build phase in `project.pbxproj`.
  - **T6**: Removed the manual Test Crash button UI, `test_crash_button` string entries, and forced-crash trigger methods (`triggerFatalCrash()`, `triggerTestCrash()`, `isDebugBuild`) across `:sharedLogic`, `:sharedUI`, `:androidApp`, and `iosApp` after Firebase Crashlytics symbolication verification (`FR-8`).

## Decisions (and rationale)
- **Project Constitution (`docs/constitution.md`)**: Established 6 concise, verifiable principles (Stack Simplicity, Spec-Driven Code, Logic–UI Separation, Zero-Dependency Testing with `kotlin.test` only, Guest-First User Data Protection, and English code / `ca`+`es` UI text).
- **Hybrid UI + Shared Logic with Clean Architecture & SOLID**: All domain models, use cases, point/weight calculations, validation, and data repositories live in `:sharedLogic` (`SharedLogic` framework for iOS) following Clean Architecture (`domain` / `data` / `presentation`) and SOLID principles, while Android uses Compose Multiplatform (`:sharedUI`) and iOS uses native SwiftUI (`iosApp`).
- **Theme-Aware Brand Logo & Icons**: Light mode uses `#A8C030` badge on `#FFFFFF` surface; Dark mode shifts the badge to `#B8D443` (`Tertiary` Dark token) on `#241F1C` dark surface, with knocked-out even-odd monochrome/tinted silhouettes for Android 13+ and iOS 18+.
- **Guest-First Authentication & Bilingual Localization (`ca` / `es`)**: Primary login uses "Enter as Guest" (`Entrar com a convidat`), with Catalan (`ca`) default and runtime switching to Spanish (`es`).

## Lessons learned and pitfalls to avoid
- `:sharedLogic` and `:sharedUI` use AGP's `com.android.kotlin.multiplatform.library` plugin (`android { ... }` inside `kotlin { ... }`), not the legacy `com.android.library` plugin.
- Custom slash commands in Antigravity live in `.agents/skills/<name>/SKILL.md`; workspace-level MCP configs live inside `.agents/plugins/<name>/mcp_config.json` + `plugin.json`.
- For Android 13+ monochrome adaptive icons and iOS 18+ tinted icons, combining the circular badge path and text/divider paths with `fillType="evenOdd"` cleanly knocks out the interior typography.
- In `iosApp`, Xcode uses `PBXFileSystemSynchronizedRootGroup` pointing to `iosApp/iosApp/`, so bundle resources like `GoogleService-Info.plist` must reside in `iosApp/iosApp/GoogleService-Info.plist` (with `IS_ANALYTICS_ENABLED = true`), and Xcode 16 Debug builds require `ENABLE_DEBUG_DYLIB = NO`, `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"`, and `Crashlytics/upload-symbols -gsp "${PROJECT_DIR}/iosApp/GoogleService-Info.plist" -p ios "${DWARF_DSYM_FOLDER_PATH}"` for symbolicated test crashes.

## Next steps
- Run SDD validation (`/sdd-validate 001-analytics-crashlytics`) to verify end-to-end FR/NFR compliance across KMP, Android, and iOS.
- Implement shared domain models, calculators, and repositories in `:sharedLogic`.
- Implement Profile/Settings and Daily Meals Log screens in `:sharedUI` and `iosApp`.
