# Story 3.1: Onboarding Flow Detection

Status: ready-for-dev

## Story

As a returning user,
I want to skip onboarding after completing it once,
So that I go directly to the library on subsequent launches.

## Acceptance Criteria

1. **Given** the app is launched
   **When** checking onboarding status
   **Then** if `hasCompletedOnboarding` is false, navigate to Onboarding (FR4)
   **And** if `hasCompletedOnboarding` is true, navigate to Library

2. **And** the check happens before any UI is shown

3. **And** there is no flash of wrong screen during navigation

## Tasks / Subtasks

- [ ] Task 1: Create OnboardingRepository for state management (AC: #1)
  - [ ] Create `OnboardingRepository` interface in `core/data`
  - [ ] Implement `OnboardingRepositoryImpl` using DataStore
  - [ ] Add `hasCompletedOnboarding` Flow<Boolean> property
  - [ ] Add `setOnboardingCompleted(Boolean)` suspend function
  - [ ] Register repository in Koin module

- [ ] Task 2: Implement startup destination determination (AC: #1, #2)
  - [ ] Create `StartupDestination` sealed class (Onboarding, Library)
  - [ ] Create `StartupViewModel` to determine initial route
  - [ ] Read `hasCompletedOnboarding` during splash hold
  - [ ] Expose `startupDestination: StateFlow<StartupDestination?>`

- [ ] Task 3: Configure splash screen to hold during check (AC: #2, #3)
  - [ ] Configure `SplashScreen.setKeepOnScreenCondition` in MainActivity
  - [ ] Hold splash until `startupDestination` is determined
  - [ ] Release splash once navigation target is known

- [ ] Task 4: Update navigation to use dynamic start destination (AC: #1, #3)
  - [ ] Modify `RulebookNavHost` to accept start destination parameter
  - [ ] Add `Onboarding` route to navigation destinations
  - [ ] Set NavHost startDestination based on StartupViewModel state
  - [ ] Ensure no flash by setting destination before NavHost renders

- [ ] Task 5: Add onboarding feature module structure (AC: #1)
  - [ ] Create `feature/onboarding` module
  - [ ] Add module to `settings.gradle.kts`
  - [ ] Create `OnboardingNavigation.kt` with route definition
  - [ ] Create placeholder `OnboardingScreen.kt` composable

## Dev Notes

### Architecture Context

- **Modules:** `core/data`, `core/datastore`, `feature/onboarding`, `app`
- **Pattern:** Repository pattern with DataStore backend
- **Key Consideration:** Must not show wrong screen even briefly

### Implementation Pattern

```kotlin
// core/data - OnboardingRepository.kt
interface OnboardingRepository {
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}

// core/data/impl - OnboardingRepositoryImpl.kt
class OnboardingRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : OnboardingRepository {
    override val hasCompletedOnboarding: Flow<Boolean> =
        preferencesDataStore.hasCompletedOnboarding

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataStore.setHasCompletedOnboarding(completed)
    }
}
```

### Splash Screen Hold Pattern

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    private val startupViewModel: StartupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep splash screen visible until we know where to navigate
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.isLoading.value
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val startupState by startupViewModel.startupDestination.collectAsStateWithLifecycle()

            RulebookTheme {
                startupState?.let { destination ->
                    RulebookApp(startDestination = destination)
                }
            }
        }
    }
}
```

### Navigation Destination

```kotlin
// feature/onboarding/navigation/OnboardingNavigation.kt
@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    composable<OnboardingRoute> {
        OnboardingScreen(onComplete = onOnboardingComplete)
    }
}

fun NavController.navigateToOnboarding() {
    navigate(OnboardingRoute) {
        popUpTo(0) { inclusive = true }
    }
}
```

### DataStore Keys (from Epic 1)

The `hasCompletedOnboarding` preference was defined in Story 1.4 (DataStore Preferences):

```kotlin
// Already in core/datastore/PreferencesDataStore.kt
companion object {
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
}

val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
    .map { preferences -> preferences[HAS_COMPLETED_ONBOARDING] ?: false }
```

### Startup Destination Sealed Class

```kotlin
// app/src/main/kotlin/.../StartupDestination.kt
sealed class StartupDestination {
    data object Onboarding : StartupDestination()
    data object Library : StartupDestination()
}
```

### Koin Module Registration

```kotlin
// core/data/di/DataModule.kt
val dataModule = module {
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
}

// app/di/AppModule.kt
val appModule = module {
    viewModel { StartupViewModel(get()) }
}
```

### Testing Notes

- Test cold start with `hasCompletedOnboarding = false` → goes to Onboarding
- Test cold start with `hasCompletedOnboarding = true` → goes to Library
- Verify no screen flash during startup
- Test splash screen timing (should be <500ms with warm DataStore)

### Project Structure Notes

- `feature/onboarding` module created as per architecture
- OnboardingRepository in `core/data` following repository pattern
- StartupViewModel in `app` module (app-level orchestration)
- Navigation route defined using type-safe Compose Navigation

### References

- [Source: docs/architecture.md#Module Structure]
- [Source: docs/architecture.md#State Management]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.1]
- [Source: docs/sprint-artifacts/1-4-datastore-preferences.md] (DataStore setup)
- [Source: docs/sprint-artifacts/2-2-navigation-host-routes.md] (Navigation patterns)

## Dev Agent Record

### Context Reference
- Epic 1: DataStore with preferences keys
- Epic 2: Navigation host and route patterns
- Architecture: Module structure and patterns

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Epic 1 (DataStore), Epic 2 (Navigation)
- **Blocks:** Story 3.2 (Onboarding Screen 1)
- **Can Parallel With:** None (first story in epic)

### Dependency Rationale
- Epic 1: Requires PreferencesDataStore for `hasCompletedOnboarding`
- Epic 2: Requires RulebookNavHost for navigation integration
- Story 3.2: Cannot build screens without navigation foundation
