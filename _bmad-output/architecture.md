# Architecture Documentation: Rulebook Android

**Generated:** 2026-01-22
**Project:** project-rulebook-android
**Version:** 1.0.0

---

## Executive Summary

Rulebook is an Android mobile application that enables users to photograph board game boxes and receive AI-generated rules summaries within 60 seconds. The app follows **Clean Architecture** principles with a **multi-module MVVM** structure, using Jetpack Compose for UI, Koin for dependency injection, and a brutalist design aesthetic.

### Key Architectural Decisions

- **Multi-module structure:** 16 modules (1 app, 9 core, 6 feature) for separation of concerns
- **Unidirectional data flow:** MVVM with StateFlow for predictable state management
- **Repository pattern:** Abstracts data sources from business logic
- **Convention plugins:** DRY build configuration via Gradle convention plugins
- **Offline-first:** Room database for local persistence, DataStore for preferences

---

## Technology Stack

### Languages & Build

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.0.21 |
| Build System | Gradle | 8.9 |
| Android Gradle Plugin | AGP | 8.7.2 |
| Code Generation | KSP | 2.0.21-1.0.27 |

### Frameworks & Libraries

| Category | Technology | Version |
|----------|------------|---------|
| UI Framework | Jetpack Compose | BOM 2024.11.00 |
| Navigation | Compose Navigation | 2.8.4 |
| Dependency Injection | Koin | 4.0.0 |
| Database | Room | 2.6.1 |
| Networking | Retrofit | 2.11.0 |
| HTTP Client | OkHttp | 4.12.0 |
| Serialization | kotlinx.serialization | 1.7.3 |
| Local Storage | DataStore | 1.1.1 |
| Image Loading | Coil | 3.0.3 |
| Camera | CameraX | 1.4.1 |
| Billing | Play Billing | 7.1.1 |
| Analytics | TelemetryDeck | 6.3.0 |

### Testing

| Category | Technology | Version |
|----------|------------|---------|
| Unit Testing | JUnit | 4.13.2 |
| Android Testing | AndroidX Test | 1.2.1 |
| UI Testing | Espresso | 3.6.1 |
| Coroutines Testing | kotlinx-coroutines-test | 1.9.0 |

---

## Architecture Pattern

### Clean Architecture with MVVM

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │   Screen    │  │  ViewModel  │  │   UiState   │              │
│  │ (Composable)│──│ (StateFlow) │──│ (data class)│              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Domain Layer                             │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    Repository Interfaces                     ││
│  │  GameRepository, RulesRepository, CreditRepository          ││
│  └─────────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                      Domain Models                           ││
│  │  Game, Rules, RuleSection, ScanResult                       ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Data Layer                              │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │     Room      │  │   Retrofit    │  │   DataStore   │       │
│  │   Database    │  │   API Client  │  │  Preferences  │       │
│  └───────────────┘  └───────────────┘  └───────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### State Management Pattern

```kotlin
// ViewModel exposes immutable state
class ExampleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState: StateFlow<ExampleUiState> = _uiState.asStateFlow()

    fun onAction() {
        _uiState.update { it.copy(loading = true) }
    }
}

// Screen observes state
@Composable
fun ExampleScreen(viewModel: ExampleViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Render based on uiState
}
```

---

## Module Architecture

### Module Dependency Graph

```
                         ┌──────────┐
                         │   app    │
                         └────┬─────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ feature:     │      │ feature:     │      │ feature:     │
│ camera       │      │ library      │      │ settings     │
│ onboarding   │      │ rules        │      │ purchase     │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                             ▼
                      ┌──────────────┐
                      │  core:data   │
                      │ (Repository) │
                      └──────┬───────┘
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                     │
       ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ core:database│      │ core:network │      │core:datastore│
│ (Room)       │      │ (Retrofit)   │      │ (Prefs)      │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                             ▼
                      ┌──────────────┐
                      │ core:model   │
                      │ (Domain)     │
                      └──────┬───────┘
                             │
                             ▼
                      ┌──────────────┐
                      │ core:common  │
                      │ (Result<T>)  │
                      └──────────────┘
```

### Module Responsibilities

| Module | Type | Responsibility |
|--------|------|----------------|
| `app` | Application | Navigation, DI root, entry point |
| `core:analytics` | Core | TelemetryDeck event tracking |
| `core:billing` | Core | Google Play Billing wrapper |
| `core:common` | Core | Result<T>, utilities |
| `core:data` | Core | Repository implementations |
| `core:database` | Core | Room database, DAOs, entities |
| `core:datastore` | Core | DataStore preferences |
| `core:designsystem` | Core | UI components, theme tokens |
| `core:model` | Core | Pure Kotlin domain models |
| `core:network` | Core | Retrofit API client |
| `feature:camera` | Feature | Photo capture, CameraX |
| `feature:library` | Feature | Game list, empty state |
| `feature:onboarding` | Feature | Onboarding flow |
| `feature:purchase` | Feature | Credit purchases |
| `feature:rules` | Feature | Rules display |
| `feature:settings` | Feature | App settings |

---

## Data Architecture

### Database Schema

```
┌─────────────────────────────────┐
│         saved_games             │
├─────────────────────────────────┤
│ id: TEXT (PK)                   │
│ title: TEXT                     │
│ thumbnail_url: TEXT?            │
│ created_at: INTEGER             │
│ last_accessed_at: INTEGER       │
└─────────────────────────────────┘
            │
            │ 1:Many
            ▼
┌─────────────────────────────────┐
│            rules                │
├─────────────────────────────────┤
│ id: TEXT (PK)                   │
│ game_id: TEXT (FK → saved_games)│
│ overview: TEXT (JSON)           │
│ setup: TEXT (JSON)              │
│ first_round: TEXT (JSON)        │
│ advanced: TEXT (JSON)           │
│ raw_json: TEXT                  │
└─────────────────────────────────┘
```

**Cascade Delete:** Deleting a game automatically deletes associated rules.

### Data Flow

```
User Action → ViewModel → Repository → Data Source → Response
                ↑                                        │
                └──────────── StateFlow ←────────────────┘
```

---

## API Design

### Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/v1/analyze` | Image recognition |
| POST | `/v1/generate` | Rules generation |

### API Configuration

- **Base URLs:**
  - Debug: `https://api-staging.rulebook.app/v1/`
  - Release: `https://api.rulebook.app/v1/`
- **Serialization:** kotlinx.serialization (JSON)
- **Timeouts:** 30s connect/read/write, 60s call
- **Logging:** Body-level in debug builds only

### Request/Response Models

```kotlin
// Analyze endpoint
data class AnalyzeRequest(
    val imageData: String,      // Base64
    val imageFormat: String     // "jpeg"
)
data class AnalyzeResponse(
    val gameTitle: String,
    val confidence: Float,
    val thumbnailUrl: String?
)

// Generate endpoint
data class GenerateRequest(
    val gameTitle: String,
    val gameId: String?
)
data class GenerateResponse(
    val gameTitle: String,
    val rulesSummary: String,
    val rulesSections: List<RulesSection>
)
```

### Error Handling

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable?) : Result<Nothing>()
}

suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e  // Preserve structured concurrency
    } catch (e: Exception) {
        Result.Error(e.localizedMessage ?: "Unknown error", e)
    }
```

---

## Component Architecture

### Design System Components

| Component | Purpose | Variants |
|-----------|---------|----------|
| `RulebookButton` | Primary actions | Primary, Secondary, Destructive |
| `RulebookCard` | Content containers | Standard, Elevated |
| `RulebookFAB` | Floating action | Default (Camera) |
| `RulebookHeaderBar` | Screen headers | Title ± back ± actions |
| `CreditsDisplay` | Credit balance | Normal, Low, Empty |

### Theme System

```kotlin
@Composable
fun RulebookTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalRulebookColors provides extendedColors,
        LocalRulebookTypography provides typography,
        LocalRulebookSpacing provides spacing,
        LocalRulebookShapes provides shapes
    ) {
        MaterialTheme(colorScheme = colorScheme) { content() }
    }
}

// Usage
val colors = RulebookTheme.colors
val spacing = RulebookTheme.spacing
```

### Design Characteristics

- **Aesthetic:** Brutalist (sharp corners, bold borders, offset shadows)
- **Corner Radius:** 0dp (all shapes)
- **Border Width:** 3dp
- **Shadow Offset:** 4dp (standard), 8dp (elevated)

---

## Navigation Architecture

### Navigation Graph

```
┌─────────────┐
│   Startup   │ (Determines initial destination)
└──────┬──────┘
       │
       ├─────────────────────────────┐
       ▼                             ▼
┌─────────────┐              ┌─────────────┐
│ Onboarding  │──Complete──▶ │   Library   │ ◀─┐
└─────────────┘              └──────┬──────┘   │
                                    │          │
                         ┌──────────┼──────────┤
                         │          │          │
                         ▼          ▼          ▼
                  ┌──────────┐ ┌────────┐ ┌──────────┐
                  │  Camera  │ │ Rules  │ │ Settings │
                  └──────────┘ └────────┘ └──────────┘
```

### Bottom Navigation

| Tab | Route | Icon |
|-----|-------|------|
| Library | `library` | Home |
| Settings | `settings` | Settings |

### FAB Navigation

Camera FAB → `camera` route (modal/full-screen)

---

## Testing Strategy

### Test Coverage by Layer

| Layer | Test Type | Location |
|-------|-----------|----------|
| ViewModel | Unit | `feature/*/src/test/` |
| Repository | Unit | `core/data/src/test/` |
| DAO | Instrumented | `core/database/src/androidTest/` |
| Network | Unit | `core/network/src/test/` |
| Mappers | Unit | `core/*/src/test/` |
| Theme | Unit | `core/designsystem/src/test/` |

### Test Patterns

```kotlin
// ViewModel testing with coroutines
@Test
fun loadGames_success() = runTest {
    val viewModel = LibraryViewModel(fakeRepository)
    val uiState = viewModel.uiState.first()
    assertTrue(uiState.games.isNotEmpty())
}

// Room DAO testing
@Test
fun insert_and_retrieve_game() = runTest {
    gameDao.insert(testGame)
    val games = gameDao.getAll().first()
    assertEquals(1, games.size)
}
```

---

## Deployment Architecture

### Build Variants

| Variant | Minification | API Environment | Debuggable |
|---------|--------------|-----------------|------------|
| debug | No | Staging | Yes |
| release | Yes (R8) | Production | No |

### CI/CD Status

**Not configured.** No `.github/workflows/` or `fastlane/` detected.

### Recommended CI Pipeline

```yaml
# Future .github/workflows/android.yml
jobs:
  build:
    - Checkout
    - Setup JDK 11
    - Gradle cache
    - Run lint
    - Run unit tests
    - Build debug APK
  release:
    - Build release APK
    - Sign with keystore
    - Upload to Play Store
```

---

## Security Considerations

### Permissions

| Permission | Rationale | Required |
|------------|-----------|----------|
| INTERNET | API calls | Yes |
| CAMERA | Photo capture | No (graceful) |
| VIBRATE | Haptic feedback | Yes |
| BILLING | In-app purchases | Yes |

### Data Protection

- **API Keys:** Not stored in client (server-side AI calls)
- **Local Data:** Room database (encrypted on device by OS)
- **Network:** HTTPS only (network_security_config)
- **Credentials:** No user accounts/passwords

---

## Scalability Considerations

### Horizontal Scaling

- **Modules:** Add new features as isolated modules
- **Components:** Design system enables consistent new components
- **Localization:** String resources ready for multi-language

### Vertical Scaling

- **Database:** Room migrations for schema changes
- **API:** Versioned endpoints (`/v1/`)
- **State:** ViewModels handle complexity isolation

---

## Appendix: Key File Locations

| Purpose | Path |
|---------|------|
| Entry Point | `app/src/main/kotlin/com/rulebook/RulebookApplication.kt` |
| Navigation | `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` |
| Database | `core/database/src/main/kotlin/com/rulebook/core/database/RulebookDatabase.kt` |
| API | `core/network/src/main/kotlin/com/rulebook/core/network/api/RulebookApi.kt` |
| Theme | `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookTheme.kt` |
| Result Type | `core/common/src/main/kotlin/com/rulebook/core/common/Result.kt` |
| Versions | `gradle/libs.versions.toml` |
