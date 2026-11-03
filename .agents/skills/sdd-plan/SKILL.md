---
name: sdd-plan
description: "SDD · Generates the technical plan of an approved spec"
---

## Mode: PLAN (Spec-Only)
- **DO NOT** create or edit any application source code or test files (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`).
- **Allowed writes**: ONLY `specs/$1/plan.md` and `MEMORY.md` (only if `specs/$1/spec.md` is approved and there are no open questions).

## Instructions
Read `docs/constitution.md`, `AGENTS.md`, and `specs/$1/spec.md`. Use the `sdd` skill.
DO NOT write code.
Generate `specs/$1/plan.md` with:
- Files to be created or modified and the responsibility of each one (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`).
- Pure domain/calculation functions in `:sharedLogic` (passing current date/"today" as a parameter where applicable).
- Algorithm in pseudocode.
- How state is rendered in the UI (`:sharedUI` Compose Multiplatform and `iosApp` SwiftUI).
- Justified technical decisions (including discarded alternatives).
- Testing strategy using `kotlin.test` via `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest` (and `./gradlew :sharedLogic:iosSimulatorArm64Test`).

Everything must respect `docs/constitution.md` and cover all functional requirements (FR/RF). Mark which requirement each part covers. If the spec is not approved or you have open questions, stop and let me know.
