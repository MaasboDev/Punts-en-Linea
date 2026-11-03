---
name: sdd-change
description: "SDD · New requirement - spec first, then code (usage: /sdd-change 002-name <requirement>)"
---

## Mode: PLAN (Spec-Only / Approval-Gated)
- **DO NOT** create or edit any application source code or test files (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`), and **DO NOT** run build/test commands.
- **Allowed writes**: ONLY `specs/$1/spec.md` (do not modify `specs/$1/plan.md` or `specs/$1/tasks.md` until the user approves the spec change).

## Instructions
New requirement for the spec in `specs/$1/` (the text following the folder name): `$ARGUMENTS`
DO NOT touch the code. Read `docs/constitution.md` and `AGENTS.md`, and use the `sdd` skill.
1. Update `specs/$1/spec.md`: add a new requirement (or modify an existing one) using the EARS format, including edge cases and items that are out of scope.
2. Indicate which parts of `specs/$1/plan.md` and `specs/$1/tasks.md` will need to be updated subsequently.
3. Show me the diff for `specs/$1/spec.md` and wait for my approval before touching `plan.md`, `tasks.md`, or any code.
