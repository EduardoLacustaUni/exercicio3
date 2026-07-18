# AI Agent Guidelines (AGENTS.md)

Welcome! This file defines the technical guidelines, code style conventions, and architectural rules that any AI agent must strictly adhere to when maintaining or expanding the **Kegel & Posture Reminder App** codebase.

---

## 1. Stack & Tools Overview

* **Language:** Kotlin (JVM Target `1.8`, Compiler `2.0.0`)
* **Gradle Build System:** Kotlin DSL (`*.gradle.kts`) with central Gradle Version Catalog (`gradle/libs.versions.toml`)
* **UI Framework:** Jetpack Compose (Material 3)
* **Background Tasks:** Android WorkManager (`2.9.0`)
* **JSON Serialization:** Gson (`2.10.1`)
* **Local Persistence:** SharedPreferences (via `PreferencesManager`)
* **Target SDK:** 34 | **Min SDK:** 26

---

## 2. Architecture & Design Principles

The project follows a clean **MVVM (Model-View-ViewModel)** architectural pattern. 

### Model
* **Events & Status:** The core models are `ScheduledEvent`, `EventType` (`KEGEL`, `MEDITATION`), and `EventStatus` (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `MISSED`).
* Keep model definitions simple and immutable (use `data class` copy methods to transition states).

### View (UI)
* **Jetpack Compose:** No XML layouts. All UI is built using composable functions inside `ui/screens/` and styled using Material 3 themes (`ui/theme/`).
* **State Consumption:** Collect UI state from the ViewModel using `collectAsState()` in Composables. Keep components modular and reusable.
* **Navigation:** Currently, state-driven page swapping is used (e.g., `AppPage.HOME`, `AppPage.SETTINGS`, `AppPage.SESSION`).

### ViewModel
* **State Management:** Expose states as immutable `StateFlow` via `asStateFlow()`.
* **Side Effects / Jobs:** All triggering of background tasks and updates of SharedPreferences happen in the ViewModel.
* Views must never call `PreferencesManager` or `WorkManager` directly.

### Data & Helpers
* **PreferencesManager:** Centrally stores user configurations (start hour, end hour, counts, durations) and serialized lists of events (`scheduled_events` and `history_events`).
* **EventSchedulerHelper:** Handles the chronological and randomized spacing of exercise intervals within the user's active hours.

---

## 3. Code Style & Formatting Guidelines

* **Null Safety:** Leverage Kotlin's null safety features. Avoid using `!!` double-bang operators; prefer safe calls (`?.`), Elvis operators (`?:`), or `let`.
* **Coroutines & Flows:** Use `viewModelScope` for coroutine dispatching. Use structured concurrency when executing tasks asynchronously.
* **Naming Conventions:**
  * **Classes & Composables:** `PascalCase` (e.g., `MainScreen`, `ReminderWorker`)
  * **Variables & Functions:** `camelCase` (e.g., `generateAndSchedulePlan()`, `prefs`)
  * **Enums & Constants:** `UPPER_SNAKE_CASE` (e.g., `EventType.KEGEL`, `TAG_DAILY_PLAN`)
* **Theme Styling:** Use predefined color and text style values from the Material Theme. Avoid hardcoded color values (e.g., use `MaterialTheme.colorScheme.background` or custom themed imports like `Slate900`).

---

## 4. Maintenance & Evolution Checklist

When adding new features or modifying the codebase:

1. **Adding Dependencies:**
   * Do **NOT** hardcode versions in `app/build.gradle.kts`. Add libraries to the central catalog at `gradle/libs.versions.toml` and reference them via alias.
2. **Notification Permissions:**
   * Maintain support for API level 33+ (Tiramisu) notification permissions. Any modification to notification workflows must respect the runtime permission check implemented in `MainActivity.kt`.
3. **Background Jobs / Workers:**
   * When registering new background routines, use `WorkManager` with descriptive tags.
   * Always double-check tags when canceling works (e.g., `cancelAllWorkByTag`).
4. **Local Testing:**
   * If modifying time/scheduling algorithms in `EventSchedulerHelper`, ensure you account for edge cases (e.g., `startHour >= endHour`, extremely high repetition counts, etc.).
