---
name: planning
description: Plan a new feature before touching any code, adhering to AGENTS.md and MEMORY.md.
---

# Feature Planning Mode

I want to add the following feature: $ARGUMENTS
(If `$ARGUMENTS` is not expanded by the runner, use the text provided after `/planning` in the user's message; if no feature was specified, ask the user what feature they want to plan before proceeding.)

## Strict Rules (Read-Only Mode)
- **DO NOT** create, edit, or delete any project files in the workspace.
- **ONLY** use read/search tools (`read_file`, `ask_question`, etc.) to inspect the codebase, `AGENTS.md`, and `MEMORY.md`.

## Steps
1. Read `AGENTS.md` and `MEMORY.md` to understand project architecture, conventions, and current status.
2. If any requirements or edge cases are ambiguous, use `ask_question` to clarify with the user first.
3. Write a detailed plan artifact (`implementation_plan.md` in the artifact directory) with `UserFacing: true` and `RequestFeedback: true` (so the UI shows the **Proceed** button) covering:
   - How you intend to implement the feature while adhering to the rules in `AGENTS.md`.
   - Which files you will create or modify and what changes will be made to each.
   - Any edge cases or open questions to resolve before implementation.
   - What updates will be made to `AGENTS.md` and `MEMORY.md` upon completion.
4. Stop and wait for the user to review and approve the plan before modifying any workspace files.
