---
name: sdd-spec
description: "SDD · Interview and generate the spec (usage: /sdd-spec 002-initial-idea-name)"
---

## Mode: PLAN (Spec-Only)
- **DO NOT** create or edit any application source code or test files (`:sharedLogic`, `:sharedUI`, `:androidApp`, `iosApp`), and **DO NOT** run build/test commands.
- During the interview phase, use **only** read-only tools (`read_file`, `ask_question`).
- Once questions are answered, you may **only** create/edit `specs/$1/spec.md` and `MEMORY.md`.

## Instructions
DO NOT write any code. Read `docs/constitution.md` and `MEMORY.md`, and use the `sdd` skill.
Spec directory: `specs/$1/`
Initial idea (the text following the directory name): `$ARGUMENTS`
Your task:
1. Ask me questions, ONE BY ONE, to clarify any ambiguities (edge cases, error scenarios, what is out of scope for this version). Maximum of 5 questions.
2. Based on my answers, generate `specs/$1/spec.md` using the `sdd` skill template, with requirements written in EARS format and the status set to "Draft".
3. Focus only on the WHAT and the WHY: do not include details regarding the tech stack, architecture, or filenames.
