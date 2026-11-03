---
name: sdd-tasks
description: "SDD · Break down the plan into small, verifiable tasks"
---

## Mode: PLAN (Spec-Only)
- **DO NOT** create or edit any application source code or test files (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`).
- **Allowed writes**: ONLY `specs/$1/tasks.md` and `MEMORY.md`.

## Instructions
Based on `specs/$1/spec.md` and `specs/$1/plan.md`, generate `specs/$1/tasks.md` following the format for the `sdd` skill:
- Small tasks (max. 20–30 minutes each), ordered by dependency.
- Each task must include the functional requirements (FR/RF) it covers and a verifiable "Done when:" criterion (e.g., passing `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest`).
- Use checkboxes.
- Aim for no more than 10 tasks; if there are more, propose splitting the spec.
