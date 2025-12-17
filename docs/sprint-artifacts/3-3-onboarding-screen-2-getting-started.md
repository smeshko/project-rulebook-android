# Story 3.3: Onboarding Screen 2 - Getting Started

Status: Ready for Review

## Story

As a first-time user,
I want to know how to start using the app,
So that I can scan my first game immediately.

## Acceptance Criteria

1. **Given** the user is on onboarding screen 2 (FR1)
   **When** the screen is displayed
   **Then** it shows:
   - Bold headline: "3 free scans on us"
   - Subtext explaining: "Start building your game library today"
   - Illustration or graphic representing the gift/credits
   - "Get Started" primary button to complete onboarding
   - "Skip" text button still available (FR3)

2. **And** tapping "Get Started" completes onboarding and awards credits

3. **And** the screen uses brutalist styling

## Tasks / Subtasks

- [x] Task 1: Create OnboardingPage2Content composable (AC: #1, #3)
  - [x] Create `OnboardingPage2Content.kt` in `feature/onboarding/components`
  - [x] Add bold headline "3 free scans on us" using `displayLarge` typography
  - [x] Add subtext "Start building your game library today" with `bodyLarge`
  - [x] Add illustration area representing gift/credits concept
  - [x] Apply brutalist styling consistent with page 1

- [x] Task 2: Implement Get Started button (AC: #1, #2)
  - [x] Add "Get Started" `RulebookButton` (primary variant)
  - [x] Position button at bottom of page
  - [x] Wire button to `onComplete` callback
  - [x] Add press feedback with brutalist animation

- [x] Task 3: Wire completion to ViewModel (AC: #2)
  - [x] Add `onGetStartedClicked()` method to OnboardingViewModel
  - [x] Method calls `onboardingRepository.setOnboardingCompleted(true)`
  - [x] Method triggers credit award (handled in Story 3.4)
  - [x] Emit navigation event after completion

- [x] Task 4: Ensure Skip button still visible (AC: #1)
  - [x] Verify Skip button from OnboardingScreen is visible on page 2
  - [x] Skip triggers same completion flow as Get Started
  - [x] Skip is positioned consistently across both pages

- [x] Task 5: Add swipe gesture support (AC: #1)
  - [x] Verify HorizontalPager swipe between pages works
  - [x] Add haptic feedback on page change
  - [x] Ensure smooth page transitions

## Dev Notes

### Architecture Context

- **Module:** `feature/onboarding`
- **Package:** `com.rulebook.feature.onboarding.components`
- **Key File:** `OnboardingPage2Content.kt`
- **Reuses:** HorizontalPager from Story 3.2

### Implementation Pattern

```kotlin
// OnboardingPage2Content.kt
@Composable
fun OnboardingPage2Content(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gift/Credits illustration
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Headline - brutalist bold
        Text(
            text = "3 free scans on us",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtext
        Text(
            text = "Start building your game library today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Get Started button
        RulebookButton(
            text = "Get Started",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
```

### OnboardingScreen Integration

```kotlin
// Update in OnboardingScreen.kt
HorizontalPager(
    state = pagerState,
    modifier = Modifier.fillMaxSize()
) { page ->
    when (page) {
        0 -> OnboardingPage1Content()
        1 -> OnboardingPage2Content(onComplete = onComplete)
    }
}
```

### ViewModel Completion Flow

```kotlin
// OnboardingViewModel.kt update
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val creditRepository: CreditRepository // Added for Story 3.4
) : ViewModel() {

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onGetStartedClicked() {
        viewModelScope.launch {
            // Mark onboarding complete
            onboardingRepository.setOnboardingCompleted(true)
            // Award credits (Story 3.4)
            // creditRepository.awardInitialCredits(3)
            // Navigate to Library
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    // Skip uses same completion logic
    fun onSkipClicked() {
        onGetStartedClicked()
    }
}

sealed class OnboardingNavigationEvent {
    data object NavigateToLibrary : OnboardingNavigationEvent()
}
```

### Bottom Section with Conditional Button

```kotlin
// OnboardingBottomSection.kt update
@Composable
fun OnboardingBottomSection(
    pagerState: PagerState,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicator (Story 3.6)
        OnboardingPageIndicator(
            pageCount = 2,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Conditional button based on page
        when (pagerState.currentPage) {
            0 -> RulebookButton(
                text = "Next",
                onClick = onNextClick,
                modifier = Modifier.fillMaxWidth()
            )
            1 -> RulebookButton(
                text = "Get Started",
                onClick = onGetStartedClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### Haptic Feedback on Page Change

```kotlin
// In OnboardingScreen.kt
val haptic = LocalHapticFeedback.current

LaunchedEffect(pagerState.currentPage) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}
```

### Brutalist Styling Checklist

| Element | Implementation |
|---------|---------------|
| Headline | `displayLarge` - Bold, high contrast, no serifs |
| Body text | `bodyLarge` - Clear hierarchy |
| Illustration box | Sharp corners, solid background |
| Get Started button | `RulebookButton` primary - Bold colors |
| Colors | Secondary container for gift theme |

### Testing Notes

- Verify page 2 displays correct headline and subtext
- Verify "Get Started" button triggers completion
- Verify swipe from page 1 to page 2 works
- Verify Skip button still visible and functional
- Test navigation to Library after completion

### Project Structure Notes

```
feature/onboarding/components/
├── OnboardingPage1Content.kt  (Story 3.2)
├── OnboardingPage2Content.kt  (This story)
├── OnboardingBottomSection.kt (Updated)
└── OnboardingPageIndicator.kt (Story 3.6)
```

### References

- [Source: docs/architecture.md#Feature Module Structure]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.3]
- [Source: docs/sprint-artifacts/3-2-onboarding-screen-1-value-proposition.md] (Page 1 pattern)
- [Source: docs/sprint-artifacts/1-9-rulebookbutton.md] (Button component)

## Dev Agent Record

### Context Reference
- Story 3.2: OnboardingScreen with HorizontalPager
- Story 3.4: Credit award logic (to be integrated)
- Architecture: Feature module components

### Agent Model Used
Claude Opus 4.5

### Debug Log References
- All tests pass (17 tests total in onboarding module)
- Full regression suite passes

### Completion Notes List
- Updated OnboardingPage.GettingStarted with correct headline and subtext
- OnboardingPage2Content now uses secondaryContainer colors for gift theme
- OnboardingViewModel now accepts OnboardingRepository and emits navigation events
- Added onGetStartedClicked() and onSkipClicked() methods
- Added haptic feedback on page change using LongPress feedback type
- Koin module updated to inject OnboardingRepository
- Skip button and Get Started both trigger the same completion flow

### File List
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingPage.kt (modified)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingScreen.kt (modified)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingViewModel.kt (modified)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingPage2Content.kt (modified)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/di/OnboardingModule.kt (modified)
- feature/onboarding/src/test/kotlin/com/rulebook/feature/onboarding/OnboardingScreenTest.kt (modified)
- feature/onboarding/src/test/kotlin/com/rulebook/feature/onboarding/OnboardingViewModelTest.kt (modified)

## Dependencies

- **Depends On:** Story 3.2 (HorizontalPager setup)
- **Blocks:** Story 3.4 (Credit Award)
- **Can Parallel With:** Story 3.6 (Page Indicator)

### Dependency Rationale
- Story 3.2: Page 2 is part of HorizontalPager created in Story 3.2
- Story 3.4: Get Started button completion triggers credit award
- Story 3.6: Page indicator can be developed in parallel (same pager)
