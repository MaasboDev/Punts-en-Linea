---
name: sdd-clarify
description: "SDD · Review the spec from a QA perspective (identify issues only; do not provide solutions)"
---

## Mode: PLAN (Read-Only QA)
- **DO NOT** create, edit, or delete any files in the workspace (`write_file`, `edit_file`, and `run_command` are strictly forbidden).
- **ONLY** use read-only tools (`read_file`, `ask_question`) to inspect `specs/$1/spec.md`, `docs/constitution.md`, `AGENTS.md`, and `MEMORY.md`.

## Instructions
Review `specs/$1/spec.md` as a highly professional QA specialist. Use the `sdd` skill.
List:
1. Remaining ambiguities (requirements that cannot be verified).
2. Contradictions between requirements.
3. Uncovered edge cases.
4. Conflicts with `docs/constitution.md`.
Do not propose solutions yet—only identify the issues. Use a numbered list format.
