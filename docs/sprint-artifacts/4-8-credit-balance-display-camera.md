# Story 4.8: Credit Balance Display on Camera

Status: Done

## Story

As a user,
I want to see my remaining credits while in the camera,
so that I know if I can complete a scan.

## Acceptance Criteria

1. **Given** the camera screen is displayed (FR11)
   **When** the credit balance is shown
   **Then** it displays the current credit count (e.g., "3 credits")

2. **Given** the camera screen is displayed
   **When** viewing the layout
   **Then** the credit display is positioned unobtrusively (top corner)

3. **Given** credits are being observed
   **When** credits change
   **Then** the display updates reactively

4. **Given** the user has 1 credit remaining
   **When** viewing the credit display
   **Then** low credit warning (1 credit) uses warning color

5. **Given** the user has 0 credits
   **When** viewing the credit display
   **Then** zero credits shows different state (handled in Epic 8)

## Tasks / Subtasks

- [x] Task 1: Observe Credit Balance from DataStore (AC: #1, #3)
  - [x] Inject `UserPreferencesRepository` into CameraViewModel
  - [x] Collect `creditBalance` Flow in ViewModel
  - [x] Add `creditBalance: Int` to CameraUiState
  - [x] Update state when credits change

- [x] Task 2: Create CreditsDisplay Composable (AC: #1, #2)
  - [x] Create `CreditsDisplay.kt` in `core/designsystem/components/`
  - [x] Display credit count with appropriate text
  - [x] Handle plural form ("1 credit" vs "3 credits")
  - [x] Apply semi-transparent background for readability

- [x] Task 3: Style Credit Display for Camera Overlay (AC: #2, #4)
  - [x] Position in top-right corner (opposite flash toggle)
  - [x] Use semi-transparent background (black/white at 50%)
  - [x] Apply rounded corners for pill shape
  - [x] Ensure readable over camera preview

- [x] Task 4: Implement Low Credit Warning State (AC: #4)
  - [x] Define low credit threshold (1 credit)
  - [x] Apply warning color (orange/amber) for low state
  - [x] Optionally add warning icon
  - [x] Maintain readability over preview

- [x] Task 5: Handle Zero Credits State (AC: #5)
  - [x] Show "0 credits" in error/disabled color
  - [x] Note: Full zero-credit handling in Epic 8 (paywall)
  - [x] Camera should still open but capture blocked (future)

- [x] Task 6: Add CreditsDisplay to Camera Screen (AC: #1, #2)
  - [x] Add CreditsDisplay to camera overlay
  - [x] Position in top-right area
  - [x] Ensure doesn't overlap with other controls

## Dev Notes

### Observing Credits from DataStore
```kotlin
class CameraViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<CameraUiState> = combine(
        // ... other flows
        userPreferencesRepository.creditBalance
    ) { /* args */ creditBalance ->
        CameraUiState(
            // ... other state
            creditBalance = creditBalance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CameraUiState()
    )
}
```

### CreditsDisplay Composable
```kotlin
@Composable
fun CreditsDisplay(
    creditCount: Int,
    modifier: Modifier = Modifier
) {
    val isLow = creditCount == 1
    val isEmpty = creditCount == 0

    val backgroundColor = when {
        isEmpty -> Color.Red.copy(alpha = 0.7f)
        isLow -> Color(0xFFFF9800).copy(alpha = 0.7f) // Orange
        else -> Color.Black.copy(alpha = 0.5f)
    }

    val textColor = Color.White

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isLow || isEmpty) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = pluralStringResource(
                    id = R.plurals.credits_count,
                    count = creditCount,
                    creditCount
                ),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

### Plural String Resource
```xml
<!-- res/values/strings.xml -->
<plurals name="credits_count">
    <item quantity="one">%d credit</item>
    <item quantity="other">%d credits</item>
</plurals>
```

### Camera Overlay Integration
```kotlin
@Composable
fun CameraOverlay(
    creditBalance: Int,
    hasFlashUnit: Boolean,
    flashMode: FlashMode,
    onFlashToggle: () -> Unit,
    // ... other params
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top row: Flash (left) and Credits (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (hasFlashUnit) {
                FlashToggle(
                    flashMode = flashMode,
                    onToggle = onFlashToggle
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            CreditsDisplay(creditCount = creditBalance)
        }

        // Bottom controls...
    }
}
```

### Project Structure Notes

- Credits display: `core/designsystem/components/CreditsDisplay.kt` (reusable)
- Integration: Update `CameraScreen.kt` and `CameraViewModel.kt`
- String resources: Add plural string to `strings.xml`

### References

- [Source: docs/architecture.md#DataStore] - DataStore for preferences and credit balance
- [Source: docs/prd.md#FR11] - Users can see their current credit balance while in camera view
- [Source: docs/prd.md#FR34] - System tracks remaining scan credits
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.8] - Full story definition

### Testing Requirements

- Test credits display shows correct count
- Test plural forms (1 credit vs X credits)
- Test warning state at 1 credit
- Test zero credit state styling
- Test reactive updates when credits change

### Dependencies

- **Prerequisites:** Story 4.1 (camera screen), Epic 1 (DataStore setup)
- **Related:** Epic 8 will handle zero-credit paywall flow
- **Parallel with:** Stories 4.2, 4.3, 4.4, 4.5, 4.6, 4.9, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Injected CreditRepository into CameraViewModel, added creditBalance to CameraUiState, observing credits reactively via Flow. Tests added and passing.
- Tasks 2-5: Created CreditsDisplay composable in core/designsystem with pill shape, semi-transparent backgrounds, plural string support, warning state (orange) for 1 credit, and error state (red) for 0 credits. Tests added and passing.
- Task 6: Integrated CreditsDisplay into CameraScreen at top-right position (opposite flash toggle). Credits display is always visible when camera is active.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/di/CameraModule.kt (modified)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt (modified)
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/CreditsDisplay.kt (new)
- core/designsystem/src/main/res/values/strings.xml (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/component/CreditsDisplayTest.kt (new)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** None
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.6, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera screen for credit display placement
