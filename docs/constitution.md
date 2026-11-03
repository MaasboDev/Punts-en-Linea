# Punts en Línia — Project Constitution

Non-negotiable, verifiable principles governing all code and specifications in this repository:

1. **Stack Simplicity**: Centralize all versions in `gradle/libs.versions.toml` and existing iOS SPM packages; never add new libraries or plugins without explicit approval.
2. **Spec-Driven Code**: `AGENTS.md` and approved plans are the source of truth; no feature code is written before plan approval, and code must match spec rules and tokens 1:1.
3. **Logic–UI Separation**: Place 100% of domain models, point/weight calculations, validation, and repositories in `:sharedLogic`; `:sharedUI`, `:androidApp`, and `iosApp` remain pure UI.
4. **Zero-Dependency Testing**: Test all `:sharedLogic` domain rules and calculations using only the existing `kotlin.test` toolchain and handwritten fakes—never install third-party test or mocking libraries.
5. **User Data Protection**: Guarantee Guest-First access (`Entrar com a convidat`), isolate user data per owner, and never commit secrets, tokens, or PII to source, logs, or `MEMORY.md`.
6. **Language Discipline**: Write all code, identifiers, comments, and technical docs in English; externalize 100% of user-facing text in Catalan (`ca`, default) and Spanish (`es`) with zero hardcoded UI strings.

