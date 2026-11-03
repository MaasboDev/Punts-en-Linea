---
name: sdd-constitution
description: "SDD · Propose the project constitution (non-negotiable principles)"
---

## Mode: PLAN (Read-Only / Approval-Gated)
- **DO NOT** create, edit, or delete any project files in the workspace until the user explicitly approves the proposal.
- **ONLY** use read-only tools (`read_file`, `ask_question`) to inspect the codebase, `AGENTS.md`, and `MEMORY.md` before proposing.
- Present the proposed constitution in an interactive plan artifact (`UserFacing: true`, `RequestFeedback: true`) and wait for approval.

## Instructions
We are going to create (or revise, if it already exists) `docs/constitution.md`. Use the `sdd` skill.
Before proposing anything, read `AGENTS.md`, `MEMORY.md`, and the project code.
Additional context: $ARGUMENTS
Propose 6 non-negotiable principles—concise and verifiable—covering:
stack simplicity, the relationship between specification and code, the separation of logic and interface, testing policy, protection of user data, and the language used for code and text. Maximum 15 lines.
DO NOT write the file yet; wait for my approval.
