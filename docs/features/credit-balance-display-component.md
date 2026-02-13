# Credit Balance Display Component

**Date:** 2026-02-13
**Story:** RULE-224 (Story 8.1: Credit Balance Display Component)
**Related Files:**
- `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/CreditsDisplay.kt`
- `core/designsystem/src/main/res/drawable/ic_credit_coin.xml`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryViewModel.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsCreditRow.kt`

## Overview

Story 8.1 implements a reusable credit balance display component that shows users their remaining scan credits across the app. The component supports multiple visual variants (Camera overlay, Library header, Settings row) and uses DataStore Preferences to reactively display credit balance changes. A review cycle added defensive error handling, accessibility improvements, and proper string localization.

## What Was Built

### 1. **Multi-Variant CreditsDisplay Component**
A reusable composable in the core design system with three visual variants:
- **Camera variant**: Semi-transparent pill with white text and warning icon overlay (Story 4.8 - existing behavior)
- **Header variant**: Compact display for Library screen - coin icon + credit count using caption typography
- **Full variant**: Extensible for future Settings/Payment flows

### 2. **Credit Balance in Library Screen**
The Library header bar displays user's credit balance reactively:
- Compact variant with small coin icon and number in the header trailing actions
- Updates automatically when credits change via DataStore

### 3. **Credit Balance in Settings Screen**
A dedicated Settings Credit Row component:
- Displays "X Credits Remaining" with coin icon in a `SettingsLinkRow` container
- Tappable row that navigates to Paywall/Purchase screen
- Reuses the same underlying DataStore Flow as Library

### 4. **Typography Token and Accessibility**
- New `creditBalance` typography token (24sp Bold) for prominent display
- Semantic accessibility labels describing credit state
- Icon tint that reflects credit state (orange normal, red when empty)

### 5. **Error Handling and Robustness**
- DataStore IOException recovery - emits 0 credits to keep UI functional
- Proper string pluralization for internationalization
- Fallback behavior ensures library and settings always display something

## Technical Implementation

### Key Files

**Design System Component:**
- `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/CreditsDisplay.kt` - Main reusable composable with variant enum and state-based styling

**Feature Integration:**
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryViewModel.kt` - Collects `creditBalance` Flow from DataStore with error handling
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt` - Same pattern for Settings
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsCreditRow.kt` - Settings-specific row composable

**Design System Assets:**
- `core/designsystem/src/main/res/drawable/ic_credit_coin.xml` - Vector drawable coin icon
- `core/designsystem/src/main/res/values/strings.xml` - String plurals for proper localization

### Key Patterns

**Multi-Variant Component Pattern:**
```kotlin
enum class CreditsDisplayVariant {
    Camera,      // Overlay pill style
    Header,      // Compact header variant
    Full         // Full row style (for future)
}

@Composable
fun CreditsDisplay(
    creditCount: Int,
    variant: CreditsDisplayVariant = CreditsDisplayVariant.Camera,
    modifier: Modifier = Modifier
)
```

This approach keeps related UI variants unified in one composable rather than creating separate components. All variants share the same credit state logic (Normal/Low/Empty) and icon tinting behavior.

**Reactive DataStore Integration Pattern:**
```kotlin
// In ViewModel
viewModelScope.launch {
    creditPreferencesSource.creditBalance
        .catch { emit(0) }  // Fallback on IOException
        .collect { balance ->
            _uiState.update { it.copy(creditBalance = balance) }
        }
}
```

This pattern ensures:
- Flow observation is lifecycle-aware (via `viewModelScope`)
- IOException from DataStore doesn't crash the app
- UI always displays a credit value (0 if DataStore fails)
- Updates are reactive (any DataStore change triggers UI update)

**State-Based Icon Tinting:**
```kotlin
val iconTint = when (creditState) {
    CreditState.Empty -> RulebookTheme.colors.red
    CreditState.Low -> RulebookTheme.colors.orange
    CreditState.Normal -> RulebookTheme.colors.orange
}
```

Icon color indicates credit urgency without text:
- Orange: Normal or low balance (1 credit)
- Red: Empty (0 credits)

**Accessibility with Semantic Content Description:**
```kotlin
val accessibilityLabel = pluralStringResource(
    id = R.plurals.credits_count,
    count = creditCount,
    creditCount
)
Row(modifier = modifier.semantics { contentDescription = accessibilityLabel })
```

Provides screen reader support with natural language plural handling ("1 credit" vs "5 credits").

### String Localization Pattern

```xml
<plurals name="credits_remaining">
    <item quantity="one">%d Credit Remaining</item>
    <item quantity="other">%d Credits Remaining</item>
</plurals>
```

Using plural resources ensures proper grammar for all languages (English singular/plural, and supports internationalization for languages with different plural rules).

## How to Use

### In a Screen (Library, Settings, Camera)

**Option 1: Inject in ViewModel**
```kotlin
class MyViewModel(
    private val creditPreferencesSource: CreditPreferencesSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            creditPreferencesSource.creditBalance
                .catch { emit(0) }
                .collect { balance ->
                    _uiState.update { it.copy(creditBalance = balance) }
                }
        }
    }
}
```

**Option 2: Collect in Composable (Direct Observation)**
```kotlin
@Composable
fun MyScreen(creditPreferencesSource: CreditPreferencesSource) {
    val creditBalance by creditPreferencesSource.creditBalance
        .catch { emit(0) }
        .collectAsStateWithLifecycle(initialValue = 0)

    CreditsDisplay(
        creditCount = creditBalance,
        variant = CreditsDisplayVariant.Header
    )
}
```

**Option 3: Use as Composable (Pre-collected)**
```kotlin
@Composable
fun MyHeader(creditBalance: Int) {
    Row {
        CreditsDisplay(
            creditCount = creditBalance,
            variant = CreditsDisplayVariant.Header
        )
    }
}
```

### Choosing the Right Variant

- **Camera**: Use for camera screen overlay (Story 4.8) - pill-shaped with semi-transparent background
- **Header**: Use in app bars or headers - compact icon + number, no background, space-efficient
- **Full**: Use in list rows or settings - full row height with title, subtitle, accessories (future)

## Configuration

### Credit State Colors
| State | Icon Color | Text Color | Condition |
|-------|-----------|-----------|-----------|
| Normal | Orange | Primary | 2+ credits |
| Low | Orange | Orange | 1 credit |
| Empty | Red | Red | 0 credits |

### Icon Sizing
- Large variant (Camera): 20dp
- Header variant: 20dp (compact row)
- Full variant: 20dp (icon) with 40dp container (future)

### Typography
- `special.creditBalance`: 24sp Bold (primary display)
- `detail.caption`: 12sp (header variant, compact)
- `body`: Regular (Settings row title)

## Notes

### Error Handling
The `catch { emit(0) }` pattern in both LibraryViewModel and SettingsViewModel ensures the app remains functional if DataStore experiences I/O errors. Showing 0 credits is better than crashing or showing stale data. In production, you might log this error for monitoring.

### Pluralization
String resources use `pluralStringResource()` function which handles:
- English: "1 credit" vs "2 credits"
- Other languages: Automatic correct plural forms based on language rules

If you're using this pattern in a new feature, always use plurals for count-based strings.

### Reactivity and Lifecycle
The credit balance updates automatically because:
1. DataStore uses Kotlin Flows
2. ViewModels collect the Flow using `viewModelScope` (lifecycle-aware)
3. Any DataStore change emits a new value
4. The UI State updates reactively
5. Compose recomposes with new value

This means when a user earns or spends credits, ALL screens showing credit balance update automatically without requiring navigation or manual refresh.

### Future Enhancements

**Story 8.2+:** The Settings Credit Row could:
- Show earned credits total this month
- Display purchase history
- Offer quick purchase of credit packs
- Show promotional offers (e.g., "Buy 5, Get 1 Free")

The current `CreditsDisplayVariant.Full` enum value exists for future extensibility without requiring refactoring.

### Testing Patterns

**Unit Testing ViewModels:**
Use `FakeCreditPreferencesSource` in tests to control credit balance:
```kotlin
val fakeSource = FakeCreditPreferencesSource()
fakeSource.setCredit(5)
val viewModel = MyViewModel(fakeSource)
```

**Composable Testing:**
Preview with different credit states:
```kotlin
@Preview
@Composable
fun CreditsDisplayPreview_Normal() {
    CreditsDisplay(creditCount = 5, variant = CreditsDisplayVariant.Header)
}

@Preview
@Composable
fun CreditsDisplayPreview_Low() {
    CreditsDisplay(creditCount = 1, variant = CreditsDisplayVariant.Header)
}

@Preview
@Composable
fun CreditsDisplayPreview_Empty() {
    CreditsDisplay(creditCount = 0, variant = CreditsDisplayVariant.Header)
}
```

### Related Stories
- **Story 4.8**: Created original `CreditsDisplay` for camera overlay
- **Story 3.4**: Credit awarding during onboarding
- **Story 8.2+**: Purchase flow and credit buying

### Related Docs
- `docs/features/credit-gated-navigation.md` - Credit checking patterns
- Design System Typography documentation - Token usage
- DataStore integration patterns - Preferences Kotlin Flow observation
