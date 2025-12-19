# Story 1.2: Dependency Injection Setup with Koin

Status: in-review

## Linear Issue

- **ID:** RULE-109
- **URL:** https://linear.app/project-rulebook/issue/RULE-109/story-12-dependency-injection-setup-with-koin

## Story

As a developer,
I want Koin dependency injection configured across all modules,
So that dependencies are properly scoped and testable.

## Acceptance Criteria

1. **Given** the multi-module project structure
   **When** Koin is configured
   **Then** `RulebookApplication.kt` initializes Koin with all modules

2. **And** each feature module has a `di/` package with module definition:
   ```kotlin
   val libraryModule = module {
       viewModel { LibraryViewModel(get()) }
   }
   ```

3. **And** core modules expose their dependencies via Koin modules

4. **And** the app module aggregates all Koin modules

## Tasks / Subtasks

- [x] Task 1: Add Koin dependencies to version catalog (AC: #1)
  - [x] Add koin-bom version
  - [x] Add koin-core library
  - [x] Add koin-android library
  - [x] Add koin-compose library
  - [x] Add koin-test library
- [x] Task 2: Create RulebookApplication class (AC: #1)
  - [x] Create RulebookApplication.kt in app module
  - [x] Initialize Koin with startKoin {}
  - [x] Add androidContext(this)
  - [x] Register application in AndroidManifest.xml
- [x] Task 3: Create core module DI definitions (AC: #3)
  - [x] Create core/common/di/CommonModule.kt
  - [x] Create core/database/di/DatabaseModule.kt (placeholder)
  - [x] Create core/network/di/NetworkModule.kt (placeholder)
  - [x] Create core/datastore/di/DataStoreModule.kt (placeholder)
  - [x] Create core/analytics/di/AnalyticsModule.kt (placeholder)
  - [x] Create core/data/di/DataModule.kt (placeholder)
- [x] Task 4: Create feature module DI definitions (AC: #2)
  - [x] Create feature/library/di/LibraryModule.kt
  - [x] Create feature/camera/di/CameraModule.kt
  - [x] Create feature/rules/di/RulesModule.kt
  - [x] Create feature/settings/di/SettingsModule.kt
  - [x] Create feature/onboarding/di/OnboardingModule.kt
  - [x] Create feature/purchase/di/PurchaseModule.kt
- [x] Task 5: Aggregate modules in app (AC: #4)
  - [x] Create app/di/AppModule.kt
  - [x] Combine all core and feature modules
  - [x] Load modules in RulebookApplication

## Dev Notes

### Architecture Patterns

- Use Koin 4.x with Compose integration
- ViewModels scoped to navigation destinations
- Repositories as singletons
- Use `koinViewModel()` in Composables

### Implementation Pattern

```kotlin
// Feature module DI
val libraryModule = module {
    viewModel { LibraryViewModel(get()) }
}

// Core module DI
val databaseModule = module {
    single { provideDatabase(get()) }
    single { provideGameDao(get()) }
}

// App module aggregation
val appModules = listOf(
    commonModule,
    databaseModule,
    networkModule,
    dataStoreModule,
    analyticsModule,
    dataModule,
    libraryModule,
    cameraModule,
    rulesModule,
    settingsModule,
    onboardingModule,
    purchaseModule
)
```

### Koin vs Hilt Decision

**Framework:** Koin 4.x was chosen because:
- Simpler learning curve for iOS developer
- Pure Kotlin DSL (no annotations/code generation)
- Sufficient for project complexity
- Faster initial development

### Project Structure Notes

- Each module exposes its dependencies via a single Koin module
- App module is the only place where all modules are aggregated
- Use `get()` for dependency resolution within modules

### References

- [Source: docs/architecture.md#Dependency Injection]
- [Source: docs/architecture.md#Project Setup & Foundation]

## Dev Agent Record

### Context Reference

- docs/architecture.md - Dependency Injection section
- libs.versions.toml - Existing Koin dependencies

### Agent Model Used

claude-opus-4-5-20251101

### Debug Log References

- Build environment issue: Java 25.0.1 compatibility with Gradle 8.9 (pre-existing, not related to changes)

### Completion Notes List

- Added koin-test to version catalog (was missing)
- Added Koin dependencies to core/database and core/common build.gradle.kts
- Created placeholder DI modules for all core modules (common, database, datastore, analytics)
- Created placeholder DI modules for all feature modules (library, camera, rules, settings, onboarding, purchase)
- Created app/di/AppModule.kt aggregating all modules
- Updated RulebookApplication.kt to load appModules
- Note: networkModule and dataModule already existed

### File List

- gradle/libs.versions.toml (modified - added koin-test)
- app/build.gradle.kts (modified - added koin-test dependency)
- app/src/main/kotlin/com/rulebook/RulebookApplication.kt (modified)
- app/src/main/kotlin/com/rulebook/di/AppModule.kt (created)
- core/common/build.gradle.kts (modified - added Koin)
- core/common/src/main/kotlin/com/rulebook/core/common/di/CommonModule.kt (created)
- core/database/build.gradle.kts (modified - added Koin)
- core/database/src/main/kotlin/com/rulebook/core/database/di/DatabaseModule.kt (created)
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/di/DataStoreModule.kt (created)
- core/analytics/src/main/kotlin/com/rulebook/core/analytics/di/AnalyticsModule.kt (created)
- feature/library/src/main/kotlin/com/rulebook/feature/library/di/LibraryModule.kt (created)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/di/CameraModule.kt (created)
- feature/rules/src/main/kotlin/com/rulebook/feature/rules/di/RulesModule.kt (created)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/di/SettingsModule.kt (created)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/di/OnboardingModule.kt (created)
- feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/di/PurchaseModule.kt (created)

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.3, Story 1.4, Story 1.5, Story 1.6, Story 1.7, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: Koin setup requires multi-module structure to exist with all modules created
