# Story 3.5: Skip Onboarding Functionality

Status: ready-for-dev

## Story

As an impatient user,
I want to skip onboarding at any point,
So that I can start using the app immediately.

## Acceptance Criteria

1. **Given** the user is on any onboarding screen (FR3)
   **When** the user taps "Skip"
   **Then** onboarding is marked complete

2. **And** 3 free credits are awarded (same as completing)

3. **And** user is navigated to Library

4. **And** skip button is visible on both screens

5. **And** skip does not require confirmation

## Tasks / Subtasks

- [ ] Task 1: Verify Skip button visibility on both pages (AC: #4)
  - [ ] Confirm Skip button positioned in OnboardingScreen (parent)
  - [ ] Verify button visible when on page 1
  - [ ] Verify button visible when on page 2
  - [ ] Use consistent positioning (top-right with status bar padding)

- [ ] Task 2: Wire Skip button to completion logic (AC: #1, #2, #3)
  - [ ] Call `viewModel.onSkipClicked()` from Skip button
  - [ ] `onSkipClicked()` calls same `completeOnboarding()` as Get Started
  - [ ] Ensure atomic credit award and flag setting
  - [ ] Trigger navigation to Library

- [ ] Task 3: Verify no confirmation dialog (AC: #5)
  - [ ] Skip executes immediately on tap
  - [ ] No AlertDialog or BottomSheet confirmation
  - [ ] Single tap completes action

- [ ] Task 4: Style Skip button appropriately (AC: #4)
  - [ ] Use `TextButton` style (secondary/text variant)
  - [ ] Color: `onSurfaceVariant` or subtle secondary
  - [ ] Typography: `labelLarge`
  - [ ] No heavy decoration or background

- [ ] Task 5: Add accessibility support (AC: #4)
  - [ ] Add contentDescription for Skip button
  - [ ] Ensure adequate touch target (48dp minimum)
  - [ ] Support TalkBack announcement

## Dev Notes

### Architecture Context

- **Module:** `feature/onboarding`
- **Files:** `OnboardingScreen.kt`, `OnboardingViewModel.kt`
- **Pattern:** Reuses completion logic from Story 3.4

### Implementation Pattern

```kotlin
// OnboardingScreen.kt - Skip button implementation
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    // ... pager setup

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // ... page content
        }

        // Skip button - ALWAYS visible (parent level, not per-page)
        TextButton(
            onClick = { viewModel.onSkipClicked() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .statusBarsPadding()
                .semantics {
                    contentDescription = "Skip onboarding and go to library"
                }
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bottom section
        OnboardingBottomSection(...)
    }
}
```

### ViewModel Skip Logic

```kotlin
// OnboardingViewModel.kt
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    // Skip uses SAME completion logic as Get Started
    fun onSkipClicked() {
        completeOnboarding()
    }

    fun onGetStartedClicked() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            // Atomic: both operations together
            onboardingRepository.completeOnboardingWithCredits(
                creditAmount = INITIAL_CREDITS
            )
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    companion object {
        const val INITIAL_CREDITS = 3
    }
}
```

### Skip Button Styling

```kotlin
// Skip button styling requirements
TextButton(
    onClick = { viewModel.onSkipClicked() },
    modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 8.dp, end = 8.dp)
        .statusBarsPadding()
        .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp), // Touch target
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    Text(
        text = "Skip",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

### Button Positioning Rationale

| Position | Why |
|----------|-----|
| Top-right | Standard skip button location |
| Parent level | Always visible regardless of page |
| Status bar padding | Not obscured by system UI |
| No background | Subtle, non-competing with main CTA |

### User Flow

```
Page 1                    Page 2
┌─────────────────────┐  ┌─────────────────────┐
│             [Skip]  │  │             [Skip]  │
│                     │  │                     │
│    Scan any game    │  │  3 free scans on us │
│        box          │  │                     │
│                     │  │                     │
│      [Next]         │  │    [Get Started]    │
└─────────────────────┘  └─────────────────────┘
        │                        │
        │     Tap Skip           │
        └───────────┬────────────┘
                    ▼
        ┌───────────────────────┐
        │  completeOnboarding() │
        │  • Set flag = true    │
        │  • Award 3 credits    │
        │  • Navigate Library   │
        └───────────────────────┘
```

### No Confirmation Design Decision

**Rationale for no confirmation:**
- User explicitly chose to skip (intentional action)
- Credits are still awarded (no penalty)
- Can revisit onboarding info in Settings if needed (future)
- Reduces friction for impatient users
- Industry standard for onboarding skip

### Accessibility Requirements

```kotlin
// Accessibility annotations
TextButton(
    onClick = { viewModel.onSkipClicked() },
    modifier = Modifier
        .semantics {
            contentDescription = "Skip onboarding and go to library"
            role = Role.Button
        }
        .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
) {
    Text(
        text = "Skip",
        // TalkBack will read "Skip, button"
    )
}
```

### Testing Notes

- Verify Skip visible on page 1
- Verify Skip visible on page 2
- Verify Skip awards 3 credits
- Verify Skip sets `hasCompletedOnboarding = true`
- Verify Skip navigates to Library
- Verify no confirmation dialog appears
- Test Skip accessibility with TalkBack
- Test Skip touch target size (48dp)

### Edge Cases

| Scenario | Behavior |
|----------|----------|
| Double-tap Skip | Idempotent - no double credits |
| Skip during page transition | Still works correctly |
| Back after Skip | Cannot return to onboarding |

### Project Structure Notes

No new files - updates to existing:
- `feature/onboarding/OnboardingScreen.kt` - Skip button positioning
- `feature/onboarding/OnboardingViewModel.kt` - Skip handler (already exists)

### References

- [Source: docs/architecture.md#Composable Structure]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.5]
- [Source: docs/sprint-artifacts/3-4-credit-award-on-completion.md] (completion logic)
- [Android Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)

## Dev Agent Record

### Context Reference
- Story 3.4: Credit award logic to reuse
- Story 3.2/3.3: OnboardingScreen where Skip button lives
- Architecture: ViewModel event handling pattern

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 3.4 (credit award logic)
- **Blocks:** None
- **Can Parallel With:** Story 3.7 (Analytics)

### Dependency Rationale
- Story 3.4: Skip reuses `completeOnboarding()` which includes credit award
- Story 3.7: Skip triggers `onboarding_skipped` analytics event
