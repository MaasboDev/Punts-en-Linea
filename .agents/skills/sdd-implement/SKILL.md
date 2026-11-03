---
name: sdd-implement
description: "SDD · Implement a task, tests first (usage: /sdd-implement 002-name T1)"
---

## Mode: BUILD (Implementation & Verification)
- You are authorized to create and edit source/test files (`write_file`, `edit_file`) and execute KMP Gradle test commands (`run_command`).
- Strictly scope all code changes to the single specified task (`$2`) in `specs/$1/tasks.md`.

## Instructions
Implement ONLY task `$2` from `specs/$1/tasks.md`, following `specs/$1/plan.md`, `docs/constitution.md`, `AGENTS.md`, and the `sdd` skill.
1. Write the unit tests first (using `kotlin.test`) and verify that they fail.
2. Write the implementation code until the tests pass.
3. Run `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest` (and `./gradlew :sharedLogic:iosSimulatorArm64Test` when applicable) and show me the result.
4. Mark `$2` as completed in `specs/$1/tasks.md` (`[x]`), indicate which functional requirement (FR/RF) it covers, and update `MEMORY.md`.
Then STOP. Do not begin the next task.
