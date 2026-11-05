# MEMORY.md — Punts en Linea
Project record across sessions. Maximum ~50 lines: summarize or remove content that is no longer relevant.

## Current Status
- Initial KMP project scaffolding in place (`:sharedLogic`, `:sharedUI`, `:androidApp`, and `iosApp`), with `local.properties` pointing to the local Android SDK.
- Brand logo (`logo.svg`) integrated across platforms:
  - Compose Multiplatform (`:sharedUI`): canonical `files/logo.svg`, theme-aware VectorDrawables (`ic_logo_light.xml` with `#A8C030` and `ic_logo_dark.xml` with `#B8D443`), and Material 3 Light/Dark color tokens applied in `App.kt`.
  - Android (`:androidApp`): Adaptive icon (`ic_launcher` / `ic_launcher_round`) with Light (`#FFFFFF`) & Night (`#241F1C`) backgrounds, Light (`#A8C030`) & Night (`#B8D443`) foregrounds, Android 13+ monochrome silhouette (`ic_launcher_monochrome.xml`), and legacy `mipmap-*` PNGs.
  - iOS (`iosApp`): 1024×1024 `AppIcon.appiconset` (`AppIcon-Light.png`, `AppIcon-Dark.png`, `AppIcon-Tinted.png`) and vector `BrandLogo.imageset` (`logo-light.svg`, `logo-dark.svg`) displayed in `ContentView.swift`.
- Firebase configurations (`google-services.json`, `GoogleService-Info.plist`) and iOS SPM dependencies (`FirebaseAuth`, `FirebaseFirestore`, `FirebaseAnalytics`, `FirebaseCrashlytics`) added.
- Project rules, domain specifications, design tokens, and `MEMORY.md` workflow defined in `AGENTS.md`, governed by `docs/constitution.md` and active feature specs (`specs/NNN-*/`).
- Custom `/planning`, `/coordinator`, and Spec-Driven Development (`sdd`, `/sdd-constitution`, `/sdd-spec`, `/sdd-clarify`, `/sdd-plan`, `/sdd-tasks`, `/sdd-implement`, `/sdd-validate`, `/sdd-change`, `/sdd-status`) skills under `.agents/skills/` and SDD subagents (`planner`, `implementer`, `reviewer` in `.agents/agents/`) configured.

## Decisions (and rationale)
- **Project Constitution (`docs/constitution.md`)**: Established 6 concise, verifiable principles (Stack Simplicity, Spec-Driven Code, Logic–UI Separation, Zero-Dependency Testing with `kotlin.test` only, Guest-First User Data Protection, and English code / `ca`+`es` UI text).
- **Hybrid UI + Shared Logic Architecture**: All domain models, point/weight calculations, validation, and data repositories live in `:sharedLogic` (`SharedLogic` framework for iOS), while Android uses Compose Multiplatform (`:sharedUI`) and iOS uses native SwiftUI (`iosApp`).
- **Theme-Aware Brand Logo & Icons**: Light mode uses `#A8C030` badge on `#FFFFFF` surface; Dark mode shifts the badge to `#B8D443` (`Tertiary` Dark token) on `#241F1C` dark surface, with knocked-out even-odd monochrome/tinted silhouettes for Android 13+ and iOS 18+.
- **Guest-First Authentication & Bilingual Localization (`ca` / `es`)**: Primary login uses "Enter as Guest" (`Entrar com a convidat`), with Catalan (`ca`) default and runtime switching to Spanish (`es`).

## Lessons learned and pitfalls to avoid
- `:sharedLogic` and `:sharedUI` use AGP's `com.android.kotlin.multiplatform.library` plugin (`android { ... }` inside `kotlin { ... }`), not the legacy `com.android.library` plugin.
- Custom slash commands in Antigravity live in `.agents/skills/<name>/SKILL.md`; workspace-level MCP configs live inside `.agents/plugins/<name>/mcp_config.json` + `plugin.json`.
- For Android 13+ monochrome adaptive icons and iOS 18+ tinted icons, combining the circular badge path and text/divider paths with `fillType="evenOdd"` cleanly knocks out the interior typography.

## Next steps
- Create and execute the SDD spec for the Guest-First Login Screen (`specs/001-*/`).
- Implement shared domain models, calculators, and repositories in `:sharedLogic`.
- Implement Profile/Settings and Daily Meals Log screens in `:sharedUI` and `iosApp`.
