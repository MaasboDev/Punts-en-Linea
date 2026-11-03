# MEMORY.md — Punts en Linea
Project record across sessions. Maximum ~50 lines: summarize or remove content that is no longer relevant.

## Current Status
- Initial KMP project scaffolding in place (`:sharedLogic`, `:sharedUI`, `:androidApp`, and `iosApp`).
- Firebase configurations (`google-services.json`, `GoogleService-Info.plist`) and iOS SPM dependencies (`FirebaseAuth`, `FirebaseFirestore`, `FirebaseAnalytics`, `FirebaseCrashlytics`) added.
- Project rules, domain specifications, design tokens, and `MEMORY.md` workflow defined in `AGENTS.md`, governed by `docs/constitution.md` and active feature specs (`specs/NNN-*/`).
- Custom `/planning` skill configured at `.agents/skills/planning/SKILL.md` (accepting `$ARGUMENTS` / inline feature description after `/planning`) for read-only feature planning with interactive plan artifacts.
- Project-scoped MCP servers (`firebase-mcp-server`, `github-mcp-server`, `context7`) configured via workspace plugin at `.agents/plugins/punts-en-linea-mcp/` (`**/mcp_config.json` gitignored).

## Decisions (and rationale)
- **Project Constitution (`docs/constitution.md`)**: Established 6 concise, verifiable principles (Stack Simplicity, Spec-Driven Code, Logic–UI Separation, Zero-Dependency Testing with `kotlin.test` only, Guest-First User Data Protection, and English code / `ca`+`es` UI text).
- **Hybrid UI + Shared Logic Architecture**: All domain models, point/weight calculations, validation, and data repositories live in `:sharedLogic` (`SharedLogic` framework for iOS), while Android uses Compose Multiplatform (`:sharedUI`) and iOS uses native SwiftUI (`iosApp`).
- **Guest-First Authentication**: Primary login flow uses "Enter as Guest" (`Entrar com a convidat`) via Firebase Anonymous Auth / local persistence, with standard email/social login as secondary.
- **Bilingual Localization (`ca` / `es`)**: Catalan (`ca`) is the default language with runtime switching to Spanish (`es`); no hardcoded UI strings.
- **Interactive Plan Mode Skill (`/planning`)**: Uses an artifact (`implementation_plan.md`) with `RequestFeedback: true` and read-only rules so feature plans require explicit user approval before modifying workspace files.

## Lessons learned and pitfalls to avoid
- `:sharedLogic` and `:sharedUI` use AGP's `com.android.kotlin.multiplatform.library` plugin (`android { ... }` inside `kotlin { ... }`), not the legacy `com.android.library` plugin.
- Custom slash commands in Antigravity must be placed in `.agents/skills/<name>/SKILL.md` with `name` and `description` frontmatter (not `.antigravity/commands/`).
- Workspace-level MCP configs in Antigravity must live inside a plugin (`.agents/plugins/<name>/mcp_config.json` + `plugin.json`), and `context7` should use stdio (`npx -y @upstash/context7-mcp --api-key ...`) since `mcp_config.json` HTTP entries do not forward custom `headers`.

## Next steps
- Implement shared domain models, calculators, and repositories in `:sharedLogic`.
- Implement theme tokens, localization, and screens (Login, Profile/Settings, Daily Meals Log) in `:sharedUI` and `iosApp`.
