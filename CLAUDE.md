# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BeekeepLog ("Матковод") is an offline-first, voice-controlled Android app for beekeepers managing queen bee nuclei. All UI text is in Russian; all code (variables, identifiers, KDoc comments) is in English.

The full specification lives in `BeekeepLog/task01.md` (2300+ lines, Russian). The JSX prototype in `BeekeepLog/disain.jsx` is the visual reference for the Compose UI.

## Build Commands

```bash
./gradlew assembleDebug       # Build debug APK (run after each implementation block)
./gradlew test                # Unit tests
./gradlew connectedAndroidTest # Instrumented tests
./gradlew lint                # Lint checks
```

## Architecture

**Clean Architecture + MVVM with Unidirectional Data Flow**

```
UI (Compose Screens)
  → ViewModels (@HiltViewModel)
    → Use Cases (business logic, 11 total)
      → Repositories (interfaces)
        → Room DAOs → SQLite
```

Package root: `com.beekeeplog.app`

Entry points:
- `BeekeepLogApp.kt` — `@HiltAndroidApp` Application class
- `MainActivity.kt` — `@AndroidEntryPoint`, hosts the Compose NavGraph

**Two main screens:**
1. `VoiceScreen` — 3-zone layout: status bar / recording content+controls / alerts
2. `AnalyticsScreen` — KPI display with filter chips and hive card list

## Key Technology

| Concern | Library | Version |
|---------|---------|---------|
| UI | Jetpack Compose + Material 3 | BOM 2024.02.00 |
| Navigation | Navigation Compose | 2.7.7 |
| Database | Room + Kotlin Flow | 2.6.1 |
| DI | Hilt (Dagger) | 2.50 |
| Async | Coroutines | 1.7.3 |
| Speech | Android `SpeechRecognizer` (platform API) | — |

Min SDK 26, Target/Compile SDK 34. Portrait locked. Screen stays on during listening.

## Database Schema (6 tables)

- **nucs** — 50 seed nuclei (id 1–50, sector A/B/C/D, row, position, current_queen_id)
- **queens** — 35 seed queens (UUID id, genetics enum, line_name, stage, lifecycle_status, nuc_id, elite/reserved flags, aggression_score)
- **tasks** — auto-generated from seed data (task_type: HATCHING/MATING_FLIGHT/CHECK_EGGS/FEEDING/TREATMENT)
- **inspection_sessions** — voice recording sessions
- **inspection_segments** — individual parsed voice segments (raw_text, normalized_text, intent_type, process_status)
- **events** — append-only audit journal (payload_json)

## NLP Pipeline

Speech → `SpeechEngine` (wraps `SpeechRecognizer`, emits `Flow<SpeechEvent>`) → `Normalizer` (Levenshtein ≤2, ≤1 for breeding lines) → `IntentExtractor` (regex/keyword → 16 intent types + entities) → Use Cases → Room

Russian numeral parsing (1–999) is handled by `NumberParser`.

## Confirmation Flow (MVP requirement)

**No DB writes happen without explicit user confirmation.** Every detected intent is displayed in a confirmation card; the user must say "ВЕРНО" (confirm) or "ОТМЕНА" (cancel) before `ApplyIntentUseCase` writes to Room.

Use cases for this flow: `UC-10: ConfirmIntentUseCase`, `UC-11: CancelIntentUseCase`.

## Code Rules

- No `TODO` comments, empty methods, or `NotImplementedError` — all code must compile and run
- Use Coroutines everywhere (no `Thread`, `Handler`, `AsyncTask`)
- Immutable UI state: `data class` + `MutableStateFlow` in ViewModels; only `val` exposed to UI
- Room is the single source of truth; all data access goes through DAOs
- SOLID principles strictly; no business logic in ViewModels (delegate to Use Cases)
- KDoc on all public classes and functions

## UI Design

OLED-optimized brutalist dark theme:
- Background: `#000000`, Surfaces: `#121212`/`#1E1E1E`/`#2A2A2A`
- Accents: NeonGreen `#00FF41`, NeonYellow `#FFD600`, NeonRed `#FF1744`, NeonBlue `#448AFF`, NeonOrange `#FF9100`

The MIC button is a 150dp circle. Analytics KPI numbers display at 72sp. See `disain.jsx` for exact layout references.

## Offline Requirement

The app must work 100% offline. STT uses `EXTRA_PREFER_OFFLINE=true`. No network permission is declared. `AndroidManifest.xml` requires a `<queries>` block for `SpeechRecognizer` (API 30+).
