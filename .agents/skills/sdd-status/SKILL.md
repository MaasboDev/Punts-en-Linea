---
name: sdd-status
description: "SDD · Where we stand – current phase and next step for a spec (usage: /sdd-status 002-name)"
---

## Mode: PLAN (Read-Only)
- **DO NOT** create, edit, or delete any files in the workspace (`write_file`, `edit_file`, and `run_command` are strictly forbidden).
- **ONLY** use read-only tools (`read_file`) to inspect `specs/$1/` and `MEMORY.md`.

## Instructions
Read `specs/$1/` (`spec.md`, `plan.md`, and `tasks.md`—whichever exist), `docs/constitution.md`, and `MEMORY.md`.
Briefly state:
1. Which phase of the SDD workflow (`Constitution → Spec → Clarification → Plan → Tasks → Implementation → Validation → Change`) this spec is in, and its current status (`draft | approved | implemented`).
2. Completed and pending tasks (`x` out of `y`).
3. The specific next step, including the exact `/sdd-*` command to execute next.
Do not modify any files.
