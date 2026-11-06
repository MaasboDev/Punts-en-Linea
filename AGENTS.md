# AGENTS.md — Punts en Linea

## Project Overview

**Punts en Linea** (`PuntsenLinea`) is a **Kotlin Multiplatform (KMP)** nutritional tracking application targeting **Android** and **iOS** under the base package `com.maasbodev.puntsenlinea`.

The project follows **Clean Architecture** and **SOLID principles** within a hybrid UI / shared-logic architecture:
- **Shared Business Logic (`:sharedLogic`)**: Shared across both Android and iOS (`commonMain`, `androidMain`, `iosMain`), exported as `SharedLogic.framework` for iOS. **All domain models, use cases, point/weight calculations, validation, repository interfaces, and data layer implementations must reside here.**
- **Compose UI (`:sharedUI`)**: Compose Multiplatform UI module (Material 3) consumed by `:androidApp`.
- **Android App (`:androidApp`)**: Android application entry point (`MainActivity`) hosting Compose UI from `:sharedUI`.
- **iOS App (`iosApp/`)**: Native SwiftUI application (`iOSApp.swift`, `ContentView.swift`) consuming the `SharedLogic` KMP framework and integrated with Firebase iOS SDKs via Swift Package Manager.

---

## Domain & Functional Specifications

Agents implementing features must adhere to the following core domain rules:

### 1. Authentication (Guest-First Flow)
- **Login Screen**: Displays the brand logo (`logo.svg`).
- **Primary Path**: "Enter as Guest" (`Entrar com a convidat`) must be the primary, highlighted action (utilizing Firebase Anonymous Auth / local persistence).
- **Secondary Path**: Standard authentication (Email/Social via Firebase Auth).

### 2. User Profile & Program Settings
- **Language Selection**: In-app toggle between **Catalan (`ca`)** and **Spanish (`es`)**.
- **Program Data**:
  - `userName`: String
  - `programStartDate`: LocalDate
  - `dailyAssignedPoints`: Number ("Punts Enlínia" assigned by nutritionist)
  - `weeklyExtraPoints`: Number (Weekly allowance of extra points)
- **Weekly Weight Tracking**:
  - `startOfWeekWeight`: Double (kg)
  - `endOfWeekWeight`: Double (kg)
  - `weightDifferential`: Automatically calculated in `:sharedLogic` (`endOfWeekWeight - startOfWeekWeight`).
- **Weekly Satisfaction**: Interactive mood/satisfaction icon selector.

### 3. Daily Meals Log (Llistat de Menjades)
- **Meal Entries**: Chronological list of meals for a selected date. Each item contains:
  - `timestamp`: Date and time of the meal.
  - `description`: Text describing what was eaten.
  - `pointsCost`: Points value of the meal.
  - `remainingDailyPoints`: Running balance calculated as `dailyAssignedPoints - cumulativePointsConsumed`.
- **Daily Summary Footer**:
  - Displays total `dailyAssignedPoints` for that day.
  - Numeric selector/stepper for `extraPointsConsumed` on that day.
  - Daily satisfaction/mood icon selector.

---

## Design System, Theming & Adaptive UI

1. **Color Palette & Tokens (Material 3 & SwiftUI)**:
   Agents must use these exact hex values extracted from `profile.jpg`, `dia.jpg`, and `logo.svg`:

   | Token Name | Light Mode | Dark Mode | Usage |
   | :--- | :--- | :--- | :--- |
   | `Primary` | `#B57850` | `#E5A982` | Top app bars, primary buttons ("Enter as Guest"), active states, day badges (`DIA 1`). |
   | `OnPrimary` | `#FFFFFF` | `#44230D` | Text and icons on top of `Primary` surfaces. |
   | `PrimaryContainer` | `#F5DFD0` | `#60371B` | Highlighted point badges, subtle header accents, selected mood icon background. |
   | `OnPrimaryContainer` | `#3B1E08` | `#F5DFD0` | Text/icons inside `PrimaryContainer`. |
   | `Secondary` | `#8C583A` | `#D7BCA8` | Form field labels (`Nom`, `Data`, `Punts Enlínia diaris`), secondary icons, mood face outlines. |
   | `OnSecondary` | `#FFFFFF` | `#3E2414` | Text on secondary elements. |
   | `Tertiary` (Logo Accent) | `#9EB928` | `#B8D443` | Brand green from `logo.svg`; use sparingly for positive weight progress badges or active indicators. |
   | `Background` | `#F4F5F7` | `#181412` | Main screen background. |
   | `OnBackground` | `#241E1A` | `#EDE4DF` | Primary body text and user input values. |
   | `Surface` | `#FFFFFF` | `#241F1C` | Rounded cards (Profile form cards, Meal entry cards, Sticky bottom summary bar). |
   | `OnSurface` | `#241E1A` | `#EDE4DF` | Primary text inside cards and inputs. |
   | `SurfaceVariant` | `#EAECEF` | `#322B27` | Alternating table/list rows, disabled inputs, unselected counter chips. |
   | `OnSurfaceVariant` | `#6E5A4F` | `#CFC3BC` | Secondary/hint text, timestamps, and subtitles. |
   | `Outline` | `#D5D8DC` | `#4D443E` | Table cell dividers, input borders, and card strokes. |

2. **Visual & Component Styling (Matching Physical Booklet)**:
   - **Cards**: Use generous rounded corners (`16.dp` / `16pt`) matching the white container blocks on `profile.jpg`.
   - **Satisfaction / Mood Selector**: 5-step face icon row ranging from Very Happy (`:D`), Happy (`:)`), Neutral (`:|`), Sad (`:(`), to Very Sad (`D:`), styled with `Secondary` (`#8C583A` in Light Mode) outlines and `PrimaryContainer` fill when selected.
   - **Extra Weekly Points Tracker**: Interactive counter or numbered badge grid (`Extra setmanal`) inspired by the top bar of `dia.jpg`.

3. **Dark & Light Mode**:
   - Every screen and custom component in both `:sharedUI` (Compose Material 3) and `iosApp` (SwiftUI) must support both Light and Dark themes using the token table above.

4. **Responsive & Adaptive Layouts (Mobile & Tablet)**:
   - **Android (`:sharedUI`)**: Support compact, medium, and expanded window size classes. Use single-column layouts on phones and two-pane / adaptive grid layouts on tablets.
   - **iOS (`iosApp`)**: Support both iPhone (`compact` size class) and iPad (`regular` size class) using adaptive SwiftUI containers (`NavigationSplitView`, adaptive grids).

5. **Localization (i18n)**:
   - **Never hardcode user-facing strings** in Compose or SwiftUI.
   - All text must be localized in **Catalan (`ca` - default)** and **Spanish (`es`)**, supporting runtime switching from the Profile screen.

---

## Module Structure

| Module / Directory | Gradle Path / Target | Namespace / Bundle | Purpose |
| :--- | :--- | :--- | :--- |
| [`sharedLogic/`](sharedLogic/build.gradle.kts) | `:sharedLogic` | `com.maasbodev.puntsenlinea.sharedLogic` | Shared KMP domain models, point/weight calculators, state holders, and Firebase/local data logic (`commonMain`, `androidMain`, `iosMain`). Produces static `SharedLogic` framework for `iosArm64` and `iosSimulatorArm64`. |
| [`sharedUI/`](sharedUI/build.gradle.kts) | `:sharedUI` | `com.maasbodev.puntsenlinea.sharedUI` | Shared Compose UI (`App.kt`), Compose resources, Material 3 theming, and AndroidX Lifecycle ViewModel/Runtime Compose. Depends on `:sharedLogic` via `api(project(":sharedLogic"))`. |
| [`androidApp/`](androidApp/build.gradle.kts) | `:androidApp` | `com.maasbodev.puntsenlinea` | Android app module (`com.android.application`) launching `App()` in `MainActivity.kt`. Contains `google-services.json`. |
| [`iosApp/`](iosApp/iosApp.xcodeproj/project.pbxproj) | Xcode project (`iosApp`) | `Punts en Linea.app` | SwiftUI app (`ContentView.swift`) importing `SharedLogic`. Linked with Firebase SPM packages (`FirebaseAnalytics`, `FirebaseAuth`, `FirebaseCrashlytics`, `FirebaseFirestore`) and `GoogleService-Info.plist`. |

---

## Tech Stack & Build Configuration

All versions and dependencies are centralized in [`gradle/libs.versions.toml`](gradle/libs.versions.toml):
- **Kotlin**: `2.4.20` (`kotlin.code.style=official`)
- **Android Gradle Plugin (AGP)**: `9.1.1`
  - Uses `com.android.kotlin.multiplatform.library` (`androidMultiplatformLibrary`) for KMP modules (`:sharedLogic`, `:sharedUI`) rather than the legacy `com.android.library` plugin.
  - Android SDK levels: `minSdk = 24`, `compileSdk = 37`, `targetSdk = 37`, `JvmTarget.JVM_11`.
- **Compose Multiplatform**: `1.12.1` (`material3 = 1.12.0-alpha03`)
- **Gradle Settings**: Configuration cache and build caching are enabled in [`gradle.properties`](gradle.properties).

---

## Common Commands

### Build
- **Assemble Android Debug APK**:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- **Compile Shared KMP Framework for iOS Simulator**:
  ```bash
  ./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64
  ```
- **Build iOS App**:
  Open `iosApp/iosApp.xcodeproj` in Xcode and build/run for a simulator or device.

### Test
- **Run Android Host Unit Tests**:
  ```bash
  ./gradlew :sharedUI:testAndroidHostTest :sharedLogic:testAndroidHostTest
  ```
- **Run iOS Simulator Unit Tests**:
  ```bash
  ./gradlew :sharedLogic:iosSimulatorArm64Test
  ```

---

## Development Conventions & Guidelines

0. **Project Constitution & Active Spec**:
   - Read [`docs/constitution.md`](docs/constitution.md) and the active spec (`specs/NNN-*/`) before touching any code.
   - All work must strictly adhere to the 6 non-negotiable principles in [`docs/constitution.md`](docs/constitution.md) (Stack Simplicity, Spec-Driven Code, Logic–UI Separation, Zero-Dependency Testing, User Data Protection, and Language Discipline).
1. **Dependency Management**:
   - Always declare new dependency versions and libraries in [`gradle/libs.versions.toml`](gradle/libs.versions.toml) and reference them via the `libs.*` version catalog accessor in `build.gradle.kts`.
2. **AGP KMP Library Plugin**:
   - `:sharedLogic` and `:sharedUI` use `alias(libs.plugins.androidMultiplatformLibrary)`. Configure Android settings inside the `kotlin { android { ... } }` block rather than a top-level `android { ... }` block.
3. **Platform-Specific Code (`expect` / `actual`)**:
   - Place shared abstractions in `sharedLogic/src/commonMain/kotlin/com/maasbodev/puntsenlinea/`.
   - Place platform implementations in `sharedLogic/src/androidMain/kotlin/...` and `sharedLogic/src/iosMain/kotlin/...`.
4. **Clean Architecture & SOLID Principles**:
   - Structure code following **Clean Architecture** with strict layer boundaries (`domain`, `data`, and `presentation` / `ui`) where dependencies point inward toward the domain layer.
   - Apply **SOLID principles** across all modules: Single Responsibility (focused classes/use cases), Open-Closed, Liskov Substitution, Interface Segregation (lean repository/service contracts in `domain`), and Dependency Inversion (depend on abstractions/interfaces in `domain`, implemented in `data` and injected into presentation/UI).
   - Keep platform-agnostic business rules, domain models, use cases, and repositories in `:sharedLogic` so both Android (`:sharedUI` / `:androidApp`) and iOS (`iosApp` SwiftUI) remain thin, declarative UI layers.
5. **Firebase Integration**:
   - Firebase config files exist at `androidApp/google-services.json` and `iosApp/GoogleService-Info.plist`.
   - When adding or modifying Xcode Swift packages or files in `iosApp/iosApp.xcodeproj`, preserve synchronized root groups (`PBXFileSystemSynchronizedRootGroup`) and existing Firebase SPM product dependencies.
6. **Task Lifecycle & Memory Synchronization**:
   - Read [`MEMORY.md`](MEMORY.md) before starting any task, and **always update [`MEMORY.md`](MEMORY.md) upon completing each task** (following the rules in the [Memory](#memory) section below).

---

## Memory
- **Before starting a task**: Read [`MEMORY.md`](MEMORY.md) to understand the project's current status and the decisions made so far.
- **Upon completing each task (Mandatory)**: Update [`MEMORY.md`](MEMORY.md) with the current status, key decisions (along with the reasoning behind them), lessons learned / pitfalls to avoid, and next steps.
- **Conciseness**: Keep [`MEMORY.md`](MEMORY.md) concise (maximum ~50 lines); summarize or remove content that is no longer relevant.
- **Permanent Rules**: If a point becomes a permanent rule, propose moving it to [`AGENTS.md`](AGENTS.md) rather than keeping it in [`MEMORY.md`](MEMORY.md).
- **Security**: Never store sensitive data (such as keys, tokens, or personal information) in [`MEMORY.md`](MEMORY.md).


