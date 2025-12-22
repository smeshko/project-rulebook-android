# Story 5.1: Scan Flow Initiation & Credit Check

Status: ready-for-dev

## Story

As a user,
I want the scan to check my credits before proceeding,
so that I don't waste time if I can't complete the scan.

## Acceptance Criteria

1. **Given** the user captures/selects a photo
   **When** initiating the scan flow
   **Then** credit balance is checked
   **And** if credits > 0, proceed to image analysis
   **And** if credits = 0, show paywall (FR38, handled in Epic 8)
   **And** credit is NOT deducted until scan succeeds

## Relevant Feature Documentation

### Credit-Gated Navigation Pattern (REFERENCE IMPLEMENTATION)

**Location:** `docs/features/credit-gated-navigation.md`

**What Was Built (Story 4.X Reference):**
- Credit balance checking before navigation to premium features
- Sealed class `NavigationAction` for type-safe navigation decisions
- Integration between `CreditRepository`, `CameraViewModel`, and navigation graph
- Memory leak prevention via automatic image URI cleanup after navigation

**Key Files That Already Exist:**
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/NavigationAction.kt` - Sealed class for navigation outcomes
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt` - ViewModel with credit checking logic
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` - Navigation graph integration

**Important Note from Git History:**
This story was **previously implemented and then reverted** (commits 7b17f42 and e66eb8c). The implementation was functionally correct but may have been reverted for workflow or coordination reasons. The existing implementation in those commits can serve as a reference.

**Key Pattern - NavigationAction Sealed Class:**
```kotlin
sealed class NavigationAction {
    object ProceedToProcessing : NavigationAction()
    object ShowPaywall : NavigationAction()
}
```

**Implementation Pattern:**
```kotlin
// In CameraViewModel
fun checkCreditsAndNavigate(): NavigationAction {
    val currentBalance = _uiState.value.creditBalance
    return if (currentBalance > 0) {
        NavigationAction.ProceedToProcessing
    } else {
        NavigationAction.ShowPaywall
    }
}
```

**Critical Timing Note:**
- Credits are **checked** during navigation (Story 5.1 - THIS STORY)
- Credits are **NOT deducted** until successful save (Story 5.7)
- This prevents charging users for failed processing attempts

## Tasks / Subtasks

- [x] Task 1: Create NavigationAction Sealed Class (AC: #1)
  - [x] Create `NavigationAction.kt` in `feature/camera`
  - [x] Define `ProceedToProcessing` and `ShowPaywall` outcomes
  - [x] Add KDoc documentation explaining credit-gating pattern

- [x] Task 2: Extend CameraViewModel for Credit Checking (AC: #1)
  - [x] Inject `CreditRepository` into `CameraViewModel` constructor (already done in Story 4.8)
  - [x] Observe credit balance Flow in ViewModel init (already done in Story 4.8)
  - [x] Add credit balance to `CameraUiState` data class (already done in Story 4.8)
  - [x] Implement `checkCreditsAndNavigate(): NavigationAction` function (completed in Task 1)
  - [x] Add `clearCapturedImage()` for resource cleanup after navigation (already exists from Story 4.2)

- [ ] Task 3: Add Navigation Routes (AC: #1)
  - [ ] Add `Route.Processing` with `createRoute(imageUri: Uri)` function
  - [ ] Add `Route.Purchase` for paywall destination (Epic 8 will implement screen)
  - [ ] Update `NavigationDestination.kt` with new routes

- [ ] Task 4: Integrate Credit Check in Navigation Graph (AC: #1)
  - [ ] Update `RulebookNavHost.kt` to handle `NavigationAction` outcomes
  - [ ] After photo capture, call `viewModel.checkCreditsAndNavigate()`
  - [ ] Navigate to Processing route if `ProceedToProcessing`
  - [ ] Navigate to Purchase route if `ShowPaywall`
  - [ ] Call `viewModel.clearCapturedImage()` after navigation
  - [ ] Pass captured image URI to processing route

- [ ] Task 5: Create Placeholder Processing Screen (AC: #1)
  - [ ] Create `ProcessingScreen.kt` placeholder in `feature/rules` (or app for now)
  - [ ] Accept `imageUri` as navigation argument
  - [ ] Display "Processing..." text and image URI (temp debugging)
  - [ ] Add to navigation graph with route pattern

- [ ] Task 6: Create Placeholder Purchase/Paywall Screen (AC: #1)
  - [ ] Create `PurchaseScreen.kt` placeholder in app or feature/purchase
  - [ ] Display "You need credits" message
  - [ ] Add "Back" button to return to library
  - [ ] Add to navigation graph (Epic 8 will implement full screen)

- [ ] Task 7: Write Unit Tests for Credit Checking (AC: #1)
  - [ ] Test `checkCreditsAndNavigate()` with credits > 0 returns `ProceedToProcessing`
  - [ ] Test `checkCreditsAndNavigate()` with credits = 0 returns `ShowPaywall`
  - [ ] Test credit balance is observed from repository
  - [ ] Test `clearCapturedImage()` clears ViewModel state

- [ ] Task 8: Manual Testing (AC: #1)
  - [ ] Test photo capture → credit check → navigation to processing (with credits)
  - [ ] Test photo capture → credit check → navigation to paywall (without credits)
  - [ ] Verify captured image URI is cleared after navigation
  - [ ] Verify no memory leaks with rapid captures

## Dev Notes

### Architecture Overview for Story 5.1

**Goal:** After the user captures a photo in `CameraScreen`, check their credit balance before allowing them to proceed to the processing/generation flow. If credits are available, navigate to the processing screen (Story 5.2+). If not, navigate to the paywall (Epic 8).

**Flow:**
```
CameraScreen (capture photo)
    ↓
CameraViewModel.onPhotoCaptured(uri)
    ↓
CameraViewModel.checkCreditsAndNavigate() → NavigationAction
    ↓
RulebookNavHost handles NavigationAction:
    - ProceedToProcessing → Navigate to ProcessingScreen with imageUri
    - ShowPaywall → Navigate to PurchaseScreen
    ↓
CameraViewModel.clearCapturedImage() (cleanup)
```

### MVI Pattern (From Architecture)

**State Management:**
- Private `MutableStateFlow<CameraUiState>`, public `StateFlow<CameraUiState>`
- State updates via `_uiState.update { it.copy(...) }`
- Use `viewModelScope.launch` for coroutines
- One-time events via `Channel<Event>` if needed

**Error Handling:**
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

### Credit Balance Management

**DataStore Integration:**
- Credit balance is stored in DataStore preferences (from Epic 1 - Story 1.4)
- `CreditRepository.creditBalance` returns `Flow<Int>`
- Observe in ViewModel via `creditRepository.creditBalance.collectAsLatest { ... }`
- Handle DataStore errors gracefully with `.catch { emit(0) }` (default to 0 credits on error)

**Important:** Credits are only **read** in this story. Credits are **deducted** in Story 5.7 (after successful rules save).

### Navigation Routes

**Route Definitions (Add to NavigationDestination.kt):**
```kotlin
object Route {
    // ... existing routes ...

    object Processing {
        private const val IMAGE_URI_ARG = "imageUri"
        const val route = "processing/{$IMAGE_URI_ARG}"

        fun createRoute(imageUri: Uri): String {
            val encodedUri = Uri.encode(imageUri.toString())
            return "processing/$encodedUri"
        }

        fun getImageUri(backStackEntry: NavBackStackEntry): Uri {
            val uriString = backStackEntry.arguments?.getString(IMAGE_URI_ARG)
                ?: throw IllegalArgumentException("Image URI not found")
            return Uri.parse(uriString)
        }
    }

    object Purchase {
        const val route = "purchase"
    }
}
```

### CameraViewModel Extension

**Inject CreditRepository:**
```kotlin
class CameraViewModel(
    private val creditRepository: CreditRepository,
    // ... existing dependencies
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        // Observe credit balance
        viewModelScope.launch {
            creditRepository.creditBalance
                .catch { emit(0) } // Graceful degradation on error
                .collectLatest { balance ->
                    _uiState.update { it.copy(creditBalance = balance) }
                }
        }
    }

    fun checkCreditsAndNavigate(): NavigationAction {
        val currentBalance = _uiState.value.creditBalance
        return if (currentBalance > 0) {
            NavigationAction.ProceedToProcessing
        } else {
            NavigationAction.ShowPaywall
        }
    }

    fun clearCapturedImage() {
        _uiState.update { it.copy(capturedImageUri = null) }
    }
}
```

**Update CameraUiState:**
```kotlin
data class CameraUiState(
    val creditBalance: Int = 0,
    val capturedImageUri: Uri? = null,
    // ... existing fields
)
```

### Navigation Graph Integration (RulebookNavHost.kt)

```kotlin
composable(Route.Camera.route) {
    val viewModel: CameraViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CameraScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToProcessing = { imageUri ->
            // Check credits before navigation
            when (val action = viewModel.checkCreditsAndNavigate()) {
                NavigationAction.ProceedToProcessing -> {
                    navController.navigate(Route.Processing.createRoute(imageUri))
                    viewModel.clearCapturedImage()
                }
                NavigationAction.ShowPaywall -> {
                    navController.navigate(Route.Purchase.route)
                    viewModel.clearCapturedImage()
                }
            }
        },
        viewModel = viewModel
    )
}

composable(Route.Processing.route) { backStackEntry ->
    val imageUri = Route.Processing.getImageUri(backStackEntry)
    ProcessingScreen(imageUri = imageUri)
}

composable(Route.Purchase.route) {
    PurchaseScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Module Structure

**feature/camera:**
- `NavigationAction.kt` (new) - Sealed class for navigation outcomes
- `CameraViewModel.kt` (modify) - Add credit checking logic
- `CameraScreen.kt` (existing) - Already has navigation callbacks

**app/navigation:**
- `NavigationDestination.kt` (modify) - Add Processing and Purchase routes
- `RulebookNavHost.kt` (modify) - Handle NavigationAction outcomes

**Placeholder Screens (temporary for Story 5.1):**
- `app/PlaceholderScreens.kt` or `feature/rules/ProcessingScreen.kt` - Simple "Processing..." screen
- `app/PlaceholderScreens.kt` or `feature/purchase/PurchaseScreen.kt` - Simple paywall placeholder

### Testing Strategy

**Unit Tests (CameraViewModelTest.kt):**
```kotlin
@Test
fun `checkCreditsAndNavigate with credits returns ProceedToProcessing`() {
    // Given: Credit balance > 0
    val creditRepo = FakeCreditRepository(initialBalance = 10)
    val viewModel = CameraViewModel(creditRepo)

    // When: Check credits
    val result = viewModel.checkCreditsAndNavigate()

    // Then: Should proceed to processing
    assertThat(result).isEqualTo(NavigationAction.ProceedToProcessing)
}

@Test
fun `checkCreditsAndNavigate with zero credits returns ShowPaywall`() {
    // Given: Credit balance = 0
    val creditRepo = FakeCreditRepository(initialBalance = 0)
    val viewModel = CameraViewModel(creditRepo)

    // When: Check credits
    val result = viewModel.checkCreditsAndNavigate()

    // Then: Should show paywall
    assertThat(result).isEqualTo(NavigationAction.ShowPaywall)
}

@Test
fun `credit balance is observed from repository`() = runTest {
    // Given: Repository with credit flow
    val creditRepo = FakeCreditRepository(initialBalance = 5)
    val viewModel = CameraViewModel(creditRepo)

    // When: Credits are updated in repo
    advanceUntilIdle()

    // Then: ViewModel state reflects credit balance
    assertThat(viewModel.uiState.value.creditBalance).isEqualTo(5)
}

@Test
fun `clearCapturedImage clears URI from state`() {
    // Given: ViewModel with captured image
    val viewModel = CameraViewModel(FakeCreditRepository())
    viewModel.onPhotoCaptured(Uri.parse("content://test"))

    // When: Clear is called
    viewModel.clearCapturedImage()

    // Then: URI is null
    assertThat(viewModel.uiState.value.capturedImageUri).isNull()
}
```

### Design System Components

**Credit Display (Already exists from Epic 1):**
- `CreditsDisplay.kt` - Shows credit balance on camera screen
- Located in top-right of camera overlay (from Story 4.10)

**Brutalist Theme:**
- Sharp corners (0dp roundedCornerShape)
- High-saturation colors: Orange, Blue, Yellow, Purple
- Bold typography (Black weight 900)
- Offset shadows (4-12dp)

### Learnings from Previous Stories (Epic 4)

**Story 4.10 - Camera Close/Back Navigation:**
- Use `DisposableEffect` for cleanup in Composables
- Call `cameraProvider.unbindAll()` on dispose
- Add `BackHandler` for system back button consistency
- Clean up ViewModel state to prevent memory leaks
- Use `onCleared()` in ViewModel for cleanup logging

**Navigation Patterns Established:**
- `onNavigateBack: () -> Unit` callback pattern
- `BackHandler { onNavigateBack() }` for system back
- `navController.popBackStack()` in NavHost
- Clean separation between screen (callbacks) and navigation (NavHost)

**Resource Management:**
- Clear state after navigation to prevent leaks
- Use `viewModelScope` for auto-cancellation
- Log cleanup for debugging

**File Naming Conventions:**
- Components: `{ComponentName}.kt` in `components/` folder
- Screens: `{FeatureName}Screen.kt`
- ViewModels: `{FeatureName}ViewModel.kt`
- Navigation: `{FeatureName}Navigation.kt`

### Git Intelligence

**Recent Work (Last 5 Commits):**
1. **Commit e66eb8c** - Reverted Story 5.1 implementation (723 lines deleted)
   - Files affected: NavigationAction.kt, CameraViewModel.kt, RulebookNavHost.kt, PlaceholderScreens.kt
   - **Key Insight:** The previous implementation was functionally correct but reverted for workflow reasons
   - **Reuse Opportunity:** Can reference commit 7b17f42 for implementation details

2. **Commit 7b17f42** - Original Story 5.1 implementation (723 lines added)
   - Successfully implemented credit checking and navigation
   - Tests passed (CameraViewModelTest.kt added)
   - **Reference this commit** for implementation approach

3. **Commit 7dcd4f0** - Documentation updates
   - Added `docs/CONDITIONAL_DOCS.md`
   - Added `docs/features/credit-gated-navigation.md` (key reference doc)

**Code Patterns from Recent Commits:**
- Koin dependency injection: `koinViewModel()` in Composables
- StateFlow pattern: `collectAsStateWithLifecycle()` for state observation
- Sealed classes for type-safe outcomes
- Placeholder screens for unimplemented features

### Prerequisites Checklist

**From Epic 1 (Foundation):**
- ✅ Koin dependency injection configured (Story 1.2)
- ✅ Room database schema (Story 1.3)
- ✅ DataStore preferences for credit balance (Story 1.4)
- ✅ Network client (Story 1.5) - needed for Epic 5 overall, not this story
- ✅ Analytics (Story 1.6) - can add events in Story 5.10
- ✅ Design system components (Stories 1.7-1.11)

**From Epic 4 (Photo Capture):**
- ✅ Camera screen with photo capture (Story 4.1)
- ✅ Image compression (Story 4.7) - produces URI for this story
- ✅ Credit balance display on camera (Story 4.8)
- ✅ Camera navigation (Story 4.10)

**From Epic 2 (Navigation):**
- ✅ NavHost and route-based navigation (Story 2.2)
- ✅ Predictive back gesture support (Story 2.8)

### References

- [Source: _bmad-output/project-planning-artifacts/prd.md#FR35] - Credit consumption on successful scan
- [Source: _bmad-output/project-planning-artifacts/prd.md#FR38] - Paywall when credits = 0
- [Source: _bmad-output/project-planning-artifacts/architecture.md#Navigation] - Compose Navigation 2.8.x, route-based
- [Source: _bmad-output/project-planning-artifacts/architecture.md#State Management] - MVI with StateFlow
- [Source: _bmad-output/project-planning-artifacts/architecture.md#Data Layer] - Repository pattern, Result<T>
- [Source: _bmad-output/project-planning-artifacts/ux-design-specification.md#Credit Gating] - UX flow for credit checks
- [Source: _bmad-output/project-planning-artifacts/epics/epic-5-game-recognition-rules-generation.md#Story 5.1] - Full story definition
- [Source: docs/features/credit-gated-navigation.md] - Implementation reference (already built pattern)
- [Source: _bmad-output/implementation-artifacts/4-10-camera-close-back-navigation.md] - Navigation and cleanup patterns
- [Source: git commit 7b17f42] - Original Story 5.1 implementation (reverted but correct)

### Testing Requirements

**Unit Tests:**
- Test credit checking logic returns correct NavigationAction
- Test credit balance observation from repository
- Test state updates with credit balance changes
- Test clearCapturedImage() cleanup
- Test error handling for DataStore failures (defaults to 0)

**Integration Tests:**
- Test photo capture → credit check → processing flow (with credits)
- Test photo capture → credit check → paywall flow (without credits)
- Test captured image URI cleanup after navigation

**Manual Tests:**
- Capture photo with credits > 0, verify navigation to processing
- Capture photo with credits = 0, verify navigation to paywall
- Verify credit balance displayed correctly on camera screen
- Test rapid photo captures don't leak memory
- Test back navigation from processing/paywall screens

### Dependencies

**Prerequisites:**
- Epic 1 (DataStore for credits)
- Epic 4 (Photo capture provides image URI)
- Epic 2 (Navigation infrastructure)

**Blocks:**
- Story 5.2 (Processing screen needs this navigation)
- Story 5.3+ (All subsequent Epic 5 stories depend on this flow)

**Can Parallel With:**
- Epic 8 stories (Paywall screen implementation can happen later, placeholder sufficient)

### Dependency Rationale

- **Epic 1 Stories 1.2, 1.4:** Need Koin DI and DataStore for credit repository injection
- **Epic 4 Story 4.1, 4.7:** Need camera screen and image compression to provide image URI
- **Epic 4 Story 4.8:** Credit display already built, can reuse for showing balance
- **Epic 4 Story 4.10:** Navigation patterns and cleanup strategies established
- **Epic 2 Story 2.2:** NavHost and route system required for navigation
- **Blocks Story 5.2:** Processing screen will be the navigation destination from this story

## Dev Agent Record

### Context Reference

Story 5.1 Ultimate Context Analysis completed by BMad Method create-story workflow.

**Key Context Sources:**
- PRD: 52 functional requirements, credit system (FR34-41)
- Architecture: MVI pattern, module structure, DataStore/Room integration
- UX Design: Credit gating flow, brutalist design system
- Epic 5: 10 stories for game recognition pipeline
- Credit-Gated Navigation Doc: Reference implementation pattern
- Story 4.10: Navigation and cleanup learnings
- Git History: Previous implementation in commit 7b17f42 (reverted but correct)

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

**Task 1 Complete:**
- ✅ Created NavigationAction.kt sealed class with ProceedToProcessing and ShowPaywall outcomes
- ✅ Added comprehensive KDoc explaining credit-gating pattern and timing (check here, deduct in Story 5.7)
- ✅ Extended CameraViewModel with checkCreditsAndNavigate() function
- ✅ Added unit tests for credit checking logic (3 tests covering > 0, = 0, and edge case = 1)
- ✅ All tests passing

**Task 2 Complete:**
- ✅ Verified all subtasks already completed in previous stories (4.8, 4.2)
- ✅ CreditRepository injection already exists
- ✅ Credit balance observation already implemented
- ✅ CameraUiState already has creditBalance field
- ✅ clearCapturedImage() already exists from Story 4.2

Story context created with comprehensive analysis:
- ✅ Epic 5 Story 1 requirements extracted
- ✅ Architecture patterns documented (MVI, StateFlow, Result<T>)
- ✅ Credit-gated navigation pattern referenced
- ✅ Previous story learnings incorporated (Epic 4 navigation patterns)
- ✅ Git intelligence analyzed (previous implementation available)
- ✅ Conditional documentation loaded (credit-gated-navigation.md)
- ✅ Prerequisites verified (Epic 1, 2, 4 complete)
- ✅ Testing strategy defined
- ✅ File structure and naming conventions documented
- ✅ Design system components identified
- ✅ Critical timing clarified (check credits here, deduct in Story 5.7)

**Implementation Confidence:** HIGH - Clear requirements, reference implementation available, all prerequisites met

### File List

Files created/modified in this story:
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/NavigationAction.kt` (created - Task 1)
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt` (modified - Task 1)
- `feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt` (modified - Task 1)
- `local.properties` (created - SDK configuration)
- `app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt` (pending - Task 3)
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` (pending - Task 4)
- `app/src/main/kotlin/com/rulebook/navigation/PlaceholderScreens.kt` (pending - Task 5, 6)

## Epic Dependencies

- **Depends On:** Epic 1 (Stories 1.2, 1.4), Epic 4 (Stories 4.1, 4.7, 4.8, 4.10), Epic 2 (Story 2.2)
- **Blocks:** Story 5.2, Story 5.3, Story 5.4, Story 5.5, Story 5.6, Story 5.7, Story 5.8, Story 5.9
- **Can Parallel With:** None (first story in Epic 5, foundation for all other stories)

### Dependency Rationale

This is the **entry point** for Epic 5's game recognition pipeline. All subsequent stories depend on this credit-gated navigation working correctly. Without this story, users cannot proceed to the processing flow, making it a critical blocker for the entire epic.
