---
name: sdd-validate
description: "SDD · Validate the FR/RF spec (KMP unit tests + Android/iOS UI & build verification)"
---

## Mode: BUILD (Verification & Test Execution)
- You are authorized to run build, test, and UI preview/inspection commands (`run_command`).
- **DO NOT** modify application source or test code to fix failures during validation—only report uncovered or failing requirements.
- If all completion criteria and requirements pass, you may update `specs/$1/spec.md` (`Status: implemented`) and `MEMORY.md`.

## Instructions
Go through `specs/$1/spec.md`, requirement by requirement (FR/RF), and verify compliance with `docs/constitution.md` and `AGENTS.md`:
1. **Domain & Logic Requirements**: For each requirement, indicate which unit test covers it and run `./gradlew :sharedLogic:testAndroidHostTest :sharedUI:testAndroidHostTest` (and `./gradlew :sharedLogic:iosSimulatorArm64Test`).
2. **UI & Platform Requirements**: For interface-related requirements that cannot be verified by unit tests alone:
   - Verify builds compile cleanly (`./gradlew :androidApp:assembleDebug` and `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64`).
   - Verify `:sharedUI` (Compose Multiplatform) and `iosApp` (SwiftUI) against `AGENTS.md` (Light/Dark tokens, `ca`/`es` localization with zero hardcoded strings, and compact mobile / expanded tablet adaptive layouts), using Compose Previews (`compose-preview`) or Android Device Hub (`android-device-hub`) when applicable.
3. If any FR/RF is not covered or fails, state this clearly. **Do NOT fix anything yet.**
4. Finally, check the completion criteria and provide a verdict: **Has the spec been met?**
