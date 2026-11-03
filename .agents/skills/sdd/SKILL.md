---
name: sdd
description: >-
  Use this whenever you work with Spec-Driven Development on this project
  (docs/constitution.md or any file within specs/) — whether drafting, reviewing, or modifying
  specs, plans, and tasks, or implementing and validating tasks from a spec.
---

# Spec-Driven Development (SDD)

## Workflow
Constitution → Spec → Clarification → Plan → Tasks → Implementation → Validation → Change.
- Never move to the next phase without explicit user approval.
- The spec is the source of truth: if something isn't in the spec, it doesn't get implemented. If a decision is missing, stop and ask.
- Any change in requirements must first be applied to the spec, then to the plan and tasks, and finally to the code.
- Each spec resides in its own folder: `specs/NNN-name/`, containing `spec.md`, `plan.md`, and `tasks.md`.
- Upon completing each phase, update `MEMORY.md`.

## Spec Template (spec.md)
```markdown
# Spec NNN — <Name>
Status: draft | approved | implemented
## Context and objective
## Users
## User stories
- US-1. As a <role>, I want to <action> so that <benefit>.
## Definitions (only if terms are open to multiple interpretations)
## Functional requirements
## Non-functional requirements
## Edge cases
## Out of scope
## Completion criteria
## Open questions
- [NEEDS CLARIFICATION] <question>
```
The spec describes the WHAT and the WHY. It should not include details about the stack, architecture, or filenames.

## Requirements in EARS
- FR-x / RF-x: WHEN <event>, THE SYSTEM <response>.
- FR-x / RF-x: IF <undesirable condition>, THEN THE SYSTEM <response>.
- FR-x / RF-x: WHILE <state>, THE SYSTEM <response>.
- FR-x / RF-x: THE SYSTEM <ongoing behavior>.
Each requirement must be verifiable: avoid terms like "fast," "intense," or "nice" without a measurable criterion.

## Plan (plan.md)
Files and responsibilities (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`) · Pure functions in `:sharedLogic` (using "today" as a parameter) · Algorithm in pseudocode · Interface (`:sharedUI` Compose & `iosApp` SwiftUI) · Decisions justified, including discarded alternatives · Testing strategy using `kotlin.test` (`./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest` and `./gradlew :sharedLogic:iosSimulatorArm64Test`). Indicate which FR/RF each part covers.

## Tasks (tasks.md)
```markdown
- [ ] **Tn. <Description>.** FR-x / RF-x
  - Done when: <verifiable check>.
```
Maximum 20–30 minutes per task, in order of dependency. If there are more than 10 tasks, propose splitting the spec.

## Implementation
One task at a time: tests first (failing/red), then the code, `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest` passing (green), mark the task as complete, update `MEMORY.md`, and stop.
