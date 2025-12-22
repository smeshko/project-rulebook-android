# Story 5.1: Scan Flow Initiation & Credit Check

Status: ready-for-dev

## Story

As a user,
I want the scan to check my credits before proceeding,
So that I don't waste time if I can't complete the scan.

## Acceptance Criteria

**Given** the user captures/selects a photo
**When** initiating the scan flow
**Then** credit balance is checked
**And** if credits > 0, proceed to image analysis
**And** if credits = 0, show paywall (FR38, handled in Epic 8)

**And** credit is NOT deducted until scan succeeds

## Tasks / Subtasks

- [x] Task 1: Implement Credit Check Flow (AC: #1)
  - [x] Check credit balance from DataStore in CameraViewModel
  - [x] If credits > 0, proceed to image analysis
  - [x] If credits = 0, navigate to paywall
  - [x] Ensure credit check happens before navigation

- [x] Task 2: Navigate to Progress Screen (AC: #1)
  - [x] Define progress route in navigation
  - [x] Pass image URI to progress screen
  - [x] Set up navigation from camera to progress

- [x] Task 3: Credit Deduction on Success Only (AC: #2)
  - [x] DO NOT deduct credit at scan initiation
  - [x] Credit deduction happens in Story 5.7 (save rules)
  - [x] Document this in code comments

- [x] Task 4: Navigate to Paywall on Zero Credits (AC: #1)
  - [x] Define paywall route placeholder
  - [x] Navigate to paywall when credits = 0
  - [x] Note: Full paywall implementation in Epic 8

- [ ] Task 5: Test Credit Check Flow (AC: #1, #2)
  - [ ] Test with credits > 0 proceeds to processing
  - [ ] Test with credits = 0 navigates to paywall
  - [ ] Verify no credit deduction at initiation

## Dev Notes

### Implementation Context

This is the **first story in Epic 5 (Game Recognition & Rules Generation)** and the entry point for the entire AI-powered scanning pipeline. This story establishes the credit check gate before any costly API operations occur.

**Critical Business Logic:**
- Credit check MUST happen before navigation to processing
- Credit is NOT deducted at scan initiation (deduction happens on successful rules generation in Story 5.7)
- Zero credits navigates to paywall (Epic 8 placeholder)
- This prevents users from wasting time if they can't complete the scan

### Architecture Integration

**Module Locations:**
- Credit check logic: `feature/camera/CameraViewModel.kt`
- Navigation to processing: `app/navigation/RulebookNavHost.kt`
- Credit repository: Already implemented in `core/data/repository/CreditRepository.kt`
- Credit UI display: Already implemented in `core/designsystem/component/CreditsDisplay.kt`

### Existing Credit System Implementation

The credit system is **already fully implemented** from Epic 1 and Epic 4:

**DataStore Layer** (`core/datastore/RulebookPreferences.kt:88-135`):
```kotlin
val CREDIT_BALANCE = intPreferencesKey("credit_balance")

override val creditBalance: Flow<Int> = context.dataStore.data
    .map { preferences -> preferences[Keys.CREDIT_BALANCE] ?: 0 }

override suspend fun deductCredit(): Boolean {
    var success = false
    context.dataStore.edit { preferences ->
        val currentBalance = preferences[Keys.CREDIT_BALANCE] ?: 0
        if (currentBalance > 0) {
            preferences[Keys.CREDIT_BALANCE] = currentBalance - 1
            success = true
        }
    }
    return success
}
```

**Repository Layer** (`core/data/repository/CreditRepositoryImpl.kt:15-33`):
```kotlin
class CreditRepositoryImpl(
    private val preferencesSource: CreditPreferencesSource
) : CreditRepository {

    override val creditBalance: Flow<Int> =
        preferencesSource.creditBalance

    override suspend fun hasCredits(): Boolean {
        return creditBalance.first() > 0
    }

    override suspend fun deductCredit(): Boolean {
        return preferencesSource.deductCredit()
    }
}
```

**ViewModel Integration** (`feature/camera/CameraViewModel.kt:44-51`):
```kotlin
class CameraViewModel(
    creditRepository: CreditRepository
) : ViewModel() {

    init {
        // Observe credit balance changes and update UI state (Story 4.8)
        creditRepository.creditBalance
            .catch { emit(0) }
            .onEach { balance ->
                _uiState.update { it.copy(creditBalance = balance) }
            }
            .launchIn(viewModelScope)
    }
}
```

**UI Display** - Credits already shown in camera top bar (Story 4.8):
```kotlin
CreditsDisplay(creditCount = uiState.creditBalance)
```

### What Needs to Be Implemented

This story adds **credit check gating logic** to the existing system:

1. **Add Credit Check Method** to CameraViewModel:
```kotlin
// Add to CameraViewModel.kt
fun checkCreditsAndNavigate(): NavigationAction {
    val currentBalance = _uiState.value.creditBalance
    return if (currentBalance > 0) {
        NavigationAction.ProceedToProcessing
    } else {
        NavigationAction.ShowPaywall
    }
}
```

2. **Define Navigation Routes** for processing screen:
```kotlin
// Add to NavigationDestination.kt
data object Processing : Route("processing/{${RulebookNavArgs.IMAGE_URI}}") {
    fun createRoute(imageUri: String): String {
        val encodedUri = Uri.encode(imageUri)
        return "processing/$encodedUri"
    }
}

object RulebookNavArgs {
    const val IMAGE_URI = "imageUri"
    // ... existing args
}
```

3. **Update Camera Screen Navigation**:
```kotlin
// Update CameraScreen.kt onPhotoCaptured callback
onPhotoCaptured = { imageUri ->
    val action = viewModel.checkCreditsAndNavigate()
    when (action) {
        NavigationAction.ProceedToProcessing -> {
            onNavigateToProcessing(imageUri)
        }
        NavigationAction.ShowPaywall -> {
            onNavigateToPaywall()
        }
    }
}
```

4. **Add Navigation Routes** in NavHost:
```kotlin
// Update RulebookNavHost.kt
composable(
    route = Route.Camera.route,
    // ... existing config
) {
    CameraScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToProcessing = { imageUri ->
            navController.navigate(Route.Processing.createRoute(imageUri))
        },
        onNavigateToPaywall = {
            navController.navigate(Route.Purchase.route)
        }
    )
}

// Add Processing route (placeholder for Story 5.2)
composable(
    route = Route.Processing.route,
    arguments = listOf(
        navArgument(RulebookNavArgs.IMAGE_URI) {
            type = NavType.StringType
        }
    )
) { backStackEntry ->
    val imageUri = backStackEntry.arguments?.getString(RulebookNavArgs.IMAGE_URI) ?: ""
    // Placeholder for Story 5.2
    ProcessingPlaceholder(imageUri = Uri.decode(imageUri))
}
```

### Navigation Flow

```
Camera Screen (photo captured)
    ↓
CameraViewModel.checkCreditsAndNavigate()
    ↓
Check creditBalance from uiState
    ↓
If credits > 0:
    NavigationAction.ProceedToProcessing
    → Navigate to Route.Processing with imageUri
    ↓
If credits = 0:
    NavigationAction.ShowPaywall
    → Navigate to Route.Purchase (Epic 8)
```

### Credit Balance Source

The credit balance is **already available** in CameraViewModel's uiState:
- Credit balance flows from DataStore → Repository → ViewModel → UiState
- No additional credit fetching needed
- Current balance is always in `_uiState.value.creditBalance`

### Important: Credit Deduction Timing

**DO NOT DEDUCT CREDITS IN THIS STORY**

Credit deduction happens in **Story 5.7 (Save Rules to Database)** after:
1. Image analysis succeeds
2. Rules generation succeeds
3. Rules are saved to database

This prevents credit loss on failures and gives users the advertised 60-second experience without surprise charges.

### Navigation Callbacks

**CameraScreen.kt** needs two new navigation callbacks:

```kotlin
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: (imageUri: String) -> Unit,  // NEW
    onNavigateToPaywall: () -> Unit,                     // NEW
    viewModel: CameraViewModel = koinViewModel()
) {
    // ... implementation
}
```

### Testing Strategy

**Unit Tests** (CameraViewModelTest.kt):
```kotlin
@Test
fun `checkCreditsAndNavigate with credits returns ProceedToProcessing`() {
    // Given: ViewModel with credits > 0
    val viewModel = createViewModel(initialCredits = 3)

    // When: Check credits
    val action = viewModel.checkCreditsAndNavigate()

    // Then: Returns proceed action
    assertEquals(NavigationAction.ProceedToProcessing, action)
}

@Test
fun `checkCreditsAndNavigate with zero credits returns ShowPaywall`() {
    // Given: ViewModel with 0 credits
    val viewModel = createViewModel(initialCredits = 0)

    // When: Check credits
    val action = viewModel.checkCreditsAndNavigate()

    // Then: Returns paywall action
    assertEquals(NavigationAction.ShowPaywall, action)
}
```

**Integration Tests** (CameraScreenTest.kt):
```kotlin
@Test
fun `photo capture with credits navigates to processing`() {
    // Given: Camera screen with credits
    // When: Photo captured
    // Then: Navigation to processing screen
}

@Test
fun `photo capture without credits navigates to paywall`() {
    // Given: Camera screen with 0 credits
    // When: Photo captured
    // Then: Navigation to purchase screen
}
```

### Error Handling

**DataStore Error Handling** - Already implemented:
- Credit balance Flow has `.catch { emit(0) }` in ViewModel
- On DataStore IOException, defaults to 0 credits
- Zero credits naturally routes to paywall

**No Network Required:**
- This story is 100% offline
- Credit balance is local DataStore read
- Navigation is local route change

### References

- [Source: docs/prd.md#FR34-FR38] - Credit system functional requirements
- [Source: docs/architecture.md#DataStore] - DataStore preferences architecture
- [Source: docs/epics/epic-5-game-recognition-rules-generation.md#Story 5.1] - Story definition
- [Source: feature/camera/CameraViewModel.kt:44-51] - Existing credit balance observation
- [Source: core/data/repository/CreditRepository.kt] - Credit repository interface
- [Source: core/designsystem/component/CreditsDisplay.kt] - Credit UI component

### Previous Story Intelligence

**From Story 4.10 (Camera Close/Back Navigation):**
- CameraScreen already has proper cleanup with `BackHandler` and `DisposableEffect`
- Navigation callbacks pattern: `onNavigateBack: () -> Unit`
- Camera resources are properly released on navigation
- Predictive back gesture is configured and working

**From Story 4.8 (Credit Balance Display):**
- Credit balance is already flowing to CameraViewModel
- CreditsDisplay component is already in camera top bar
- Credit state observation is error-resilient with `.catch { emit(0) }`

**Established Patterns:**
```kotlin
// Navigation callback pattern (from Story 4.10)
CameraScreen(
    onNavigateBack = { navController.popBackStack() },
    onNewCallback = { /* navigation action */ }
)

// State observation pattern (from Story 4.8)
creditRepository.creditBalance
    .catch { emit(0) }
    .onEach { balance ->
        _uiState.update { it.copy(creditBalance = balance) }
    }
    .launchIn(viewModelScope)
```

### Git Intelligence Summary

**Recent commits show:**
- Sprint status updates (38fd255, 5385a3d)
- Epic 4 completion and retrospective (21c7300)
- ADW integration updates (c5cd090)

**Key patterns from recent work:**
- All stories follow the same structure with Dev Notes, Testing, File List
- Stories mark tasks complete with completion notes
- Navigation changes are tested with both unit and integration tests
- Story files track agent model used and completion details

### Architecture Compliance

**This story must follow established patterns:**

1. **MVI State Management** (from architecture.md):
   - Single immutable `CameraUiState` data class
   - State updates via `_uiState.update { it.copy(...) }`
   - Navigation decisions based on current state snapshot

2. **Navigation Pattern** (from architecture.md):
   - Route definitions in `NavigationDestination.kt`
   - NavHost composable configuration in `RulebookNavHost.kt`
   - Screen-level callbacks for navigation actions

3. **Naming Conventions** (from architecture.md):
   - Files: `PascalCase.kt`
   - Functions: `camelCase`
   - Constants: `SCREAMING_SNAKE_CASE`
   - Navigation args: `camelCase` (e.g., `imageUri`, not `image_uri`)

4. **Testing Requirements** (from architecture.md):
   - Unit tests in `src/test/kotlin/` (co-located)
   - Integration tests in `src/androidTest/kotlin/`
   - Test naming: `ClassName + Test.kt`

### Library & Framework Requirements

**Dependencies** (from architecture.md):
- Kotlin 2.x
- Compose BOM (latest stable)
- Koin 4.x for DI
- Lifecycle ViewModel (androidx.lifecycle)
- Navigation Compose 2.8+ (androidx.navigation)

**Key Libraries for This Story:**
```gradle
// Already in project from Epic 1
implementation(libs.androidx.lifecycle.runtime.compose)  // StateFlow observation
implementation(libs.androidx.navigation.compose)         // Navigation
implementation(libs.koin.androidx.compose)               // ViewModel injection
implementation(libs.androidx.datastore.preferences)      // Credit storage
```

### File Structure Requirements

**Files to Modify:**
```
feature/camera/src/main/kotlin/com/rulebook/feature/camera/
├── CameraViewModel.kt              # Add checkCreditsAndNavigate()
├── CameraScreen.kt                 # Add navigation callbacks
└── navigation/
    └── CameraNavigation.kt         # Add new callback parameters

app/src/main/kotlin/com/rulebook/navigation/
├── NavigationDestination.kt        # Add Processing route
└── RulebookNavHost.kt              # Wire up navigation

feature/camera/src/test/kotlin/com/rulebook/feature/camera/
└── CameraViewModelTest.kt          # Add credit check tests

feature/camera/src/androidTest/kotlin/com/rulebook/feature/camera/
└── CameraScreenTest.kt             # Add integration tests
```

**No New Modules Needed:**
- All work is in existing `feature/camera` and `app` modules
- No new dependencies required
- No new design system components needed

### Project Context Notes

**From project context and brownfield analysis:**

1. **Module Structure** - This is a multi-module project:
   - `app` module for navigation aggregation
   - `feature/*` modules for UI features
   - `core/*` modules for shared infrastructure

2. **Existing Patterns to Follow**:
   - ViewModel factory via Koin `koinViewModel()`
   - State observation via `collectAsStateWithLifecycle()`
   - Navigation via callback lambdas (not direct NavController access in composables)

3. **Epic 5 Context**:
   - This is the entry point for the AI pipeline
   - Following stories (5.2-5.10) will build on this navigation foundation
   - Processing screen placeholder will be replaced in Story 5.2

4. **Epic 8 Dependency**:
   - Paywall screen is implemented in Epic 8
   - For now, navigation to `Route.Purchase` is sufficient
   - Purchase screen already exists as placeholder from Epic 1

### Latest Technical Information

**Kotlin Coroutines & Flows:**
- Using `StateFlow` for reactive state (standard in Jetpack Compose)
- `Flow.first()` suspends to get current value (already used in CreditRepository)
- `launchIn(viewModelScope)` for lifecycle-aware collection

**Compose Navigation 2.8+:**
- NavArgument type `NavType.StringType` for URI passing
- URI encoding/decoding for safe route parameters
- Deep link support available (not needed for this story)

**Android DataStore 1.1.x:**
- Preferences API for key-value storage
- Atomic transactions via `edit { }` blocks
- Flow-based observation for reactive updates

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

**Task 1: Implement Credit Check Flow** - Created NavigationAction sealed class with ProceedToProcessing and ShowPaywall variants. Implemented checkCreditsAndNavigate() method in CameraViewModel that reads current creditBalance from uiState and returns appropriate navigation action. Credit check uses existing credit balance flow from Story 4.8 - no additional repository calls needed. Wrote 3 unit tests covering: credits > 0 returns ProceedToProcessing, credits = 0 returns ShowPaywall, and boundary case with 1 credit. All tests pass.

**Task 2: Navigate to Progress Screen** - Added Route.Processing with imageUri parameter to NavigationDestination.kt. Implemented URI encoding/decoding for safe route parameter passing. Created ProcessingPlaceholder composable for Story 5.2 implementation. Wired up navigation in RulebookNavHost with proper navArgument configuration and slide transitions. Navigation triggered from both onPhotoCaptured and onGalleryImageSelected callbacks after credit check passes.

**Task 3: Credit Deduction on Success Only** - Added code documentation in CameraViewModel.checkCreditsAndNavigate() KDoc stating credit is NOT deducted at scan initiation. Added inline comments in RulebookNavHost navigation callbacks explaining deduction happens in Story 5.7 on successful rules save. This prevents credit loss on API failures and ensures users get the advertised 60-second experience.

**Task 4: Navigate to Paywall on Zero Credits** - Utilized existing Route.Purchase from Epic 1 for paywall navigation. When checkCreditsAndNavigate() returns NavigationAction.ShowPaywall, navigation routes to Route.Purchase.route. PurchasePlaceholder already exists from Epic 1. Full paywall screen implementation will be completed in Epic 8 as noted in PRD.

### File List

**Task 1:**
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/NavigationAction.kt (created)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)

**Task 2:**
- app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt (modified)
- app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt (modified)
- app/src/main/kotlin/com/rulebook/navigation/PlaceholderScreens.kt (modified)

**Task 3:**
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified - comments)
- app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt (modified - comments)

## Epic Dependencies

- **Depends On:** Story 4.8 (credit balance display), Story 4.10 (camera navigation), Epic 1 (DataStore, credit repository)
- **Blocks:** Story 5.2 (progress screen), Story 5.3 (API integration)
- **Can Parallel With:** None (entry point for Epic 5)

### Dependency Rationale
- Story 4.8: Requires credit balance already flowing to CameraViewModel
- Story 4.10: Requires working camera navigation pattern
- Epic 1: Requires DataStore and credit repository implementation
- Blocks Story 5.2: Processing screen needs this navigation route
