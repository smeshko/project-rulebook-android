# Story 2.6: Settings Screen Shell

Status: in-progress

## Story

As a user,
I want to access the settings screen,
So that I can view app options (full functionality in Epic 9).

## Acceptance Criteria

1. **Given** the user taps the Settings tab
   **When** the Settings screen is displayed
   **Then** the header bar shows "Settings" with brutalist styling

2. **And** placeholder sections are visible:
   - Appearance (theme toggle placeholder)
   - Feedback (haptics toggle placeholder)
   - Support (links placeholder)
   - About (version, legal placeholder)
   - Data (clear data placeholder)

3. **And** sections are grouped with headers

4. **And** the screen scrolls if content exceeds viewport

## Tasks / Subtasks

- [x] Task 1: Create SettingsScreen composable (AC: #1, #4)
  - [x] Create feature/settings module structure
  - [x] Create SettingsScreen.kt
  - [x] Add RulebookHeaderBar with "Settings" title
  - [x] Implement scrollable content with LazyColumn
- [x] Task 2: Create SettingsViewModel (AC: #2)
  - [x] Create SettingsViewModel.kt
  - [x] Create SettingsUiState data class
  - [x] Track placeholder states
  - [x] Expose StateFlow for UI observation
- [x] Task 3: Create section header component (AC: #3)
  - [x] Create SettingsSectionHeader composable
  - [x] Apply brutalist styling with border
  - [x] Use consistent typography
- [x] Task 4: Implement Appearance section (AC: #2)
  - [x] Add section header "Appearance"
  - [x] Add theme toggle placeholder row
  - [x] Use brutalist styling
- [x] Task 5: Implement Feedback section (AC: #2)
  - [x] Add section header "Feedback"
  - [x] Add haptics toggle placeholder row
- [x] Task 6: Implement Support section (AC: #2)
  - [x] Add section header "Support"
  - [x] Add placeholder links (Contact, Rate, Share)
- [x] Task 7: Implement About section (AC: #2)
  - [x] Add section header "About"
  - [x] Add version info placeholder
  - [x] Add legal links placeholder (Privacy, Terms)
- [x] Task 8: Implement Data section (AC: #2)
  - [x] Add section header "Data"
  - [x] Add clear data placeholder button
- [x] Task 9: Create Koin module for settings feature
  - [x] Create SettingsModule.kt
  - [x] Register SettingsViewModel
  - [x] Register in app module (already registered)

## Dev Notes

### Architecture Context

- **Module:** feature/settings
- **Package:** com.rulebook.feature.settings
- **Key Files:** SettingsScreen.kt, SettingsViewModel.kt, SettingsUiState.kt

### Module Structure

```
feature/settings/
├── src/main/kotlin/com/rulebook/feature/settings/
│   ├── SettingsScreen.kt
│   ├── SettingsViewModel.kt
│   ├── SettingsUiState.kt
│   ├── components/
│   │   ├── SettingsSectionHeader.kt
│   │   ├── SettingsToggleRow.kt
│   │   └── SettingsLinkRow.kt
│   ├── navigation/
│   │   └── SettingsNavigation.kt
│   └── di/
│       └── SettingsModule.kt
└── build.gradle.kts
```

### Implementation Pattern

```kotlin
// SettingsUiState.kt
data class SettingsUiState(
    val isDarkTheme: Boolean = false, // Placeholder
    val isHapticsEnabled: Boolean = true, // Placeholder
    val appVersion: String = "1.0.0"
)

// SettingsViewModel.kt
class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Placeholder actions - will be implemented in Epic 9
    fun onThemeToggle() { /* TODO */ }
    fun onHapticsToggle() { /* TODO */ }
    fun onClearData() { /* TODO */ }
}
```

```kotlin
// SettingsScreen.kt
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        RulebookHeaderBar(title = "Settings")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader(title = "Appearance")
            }
            item {
                SettingsToggleRow(
                    label = "Dark Mode",
                    checked = uiState.isDarkTheme,
                    onCheckedChange = { viewModel.onThemeToggle() }
                )
            }

            // Feedback Section
            item {
                SettingsSectionHeader(title = "Feedback")
            }
            item {
                SettingsToggleRow(
                    label = "Haptic Feedback",
                    checked = uiState.isHapticsEnabled,
                    onCheckedChange = { viewModel.onHapticsToggle() }
                )
            }

            // Support Section
            item {
                SettingsSectionHeader(title = "Support")
            }
            item {
                SettingsLinkRow(label = "Contact Us", onClick = { /* TODO */ })
            }
            item {
                SettingsLinkRow(label = "Rate the App", onClick = { /* TODO */ })
            }

            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }
            item {
                SettingsInfoRow(label = "Version", value = uiState.appVersion)
            }
            item {
                SettingsLinkRow(label = "Privacy Policy", onClick = { /* TODO */ })
            }
            item {
                SettingsLinkRow(label = "Terms of Service", onClick = { /* TODO */ })
            }

            // Data Section
            item {
                SettingsSectionHeader(title = "Data")
            }
            item {
                RulebookButton(
                    text = "Clear All Data",
                    onClick = viewModel::onClearData,
                    variant = ButtonVariant.Destructive,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
```

### Component Patterns

```kotlin
// SettingsSectionHeader.kt
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = RulebookTypography.labelLarge,
        color = RulebookTheme.colors.onSurface.copy(alpha = 0.6f),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

// SettingsToggleRow.kt
@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(RulebookTheme.colors.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RulebookTypography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// SettingsLinkRow.kt
@Composable
fun SettingsLinkRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(RulebookTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RulebookTypography.bodyLarge
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null
        )
    }
}
```

### Section Layout

| Section | Items |
|---------|-------|
| Appearance | Dark Mode toggle |
| Feedback | Haptic Feedback toggle |
| Support | Contact Us, Rate the App, Share |
| About | Version, Privacy Policy, Terms |
| Data | Clear All Data button |

### Brutalist Styling

- Section headers: Uppercase, 60% alpha
- Rows: 3dp black border, surface background
- Spacing: 16dp between sections, 8dp between items
- Clear button: Destructive variant (red)

### References

- [Source: docs/architecture.md#Feature Module Structure]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.6]
- [Source: docs/ux-design-specification.md#Settings Screen]

## Dev Agent Record

### Context Reference
- RulebookHeaderBar from Story 1.11
- RulebookButton from Story 1.9
- Brutalist modifiers from Story 1.8
- MVI pattern from architecture

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List
- Implemented complete Settings screen shell with all 5 sections (Appearance, Feedback, Support, About, Data)
- Created SettingsViewModel with StateFlow-based state management (placeholder actions for Epic 9)
- Created SettingsUiState data class with default values for theme, haptics, and version
- Created reusable components: SettingsSectionHeader, SettingsToggleRow, SettingsLinkRow, SettingsInfoRow
- Used RulebookHeaderBar with "Settings" title for brutalist header styling
- Implemented scrollable LazyColumn with 16dp padding and 8dp item spacing
- Applied brutalist styling with 3dp borders on all row components
- Section headers use uppercase text with 60% alpha for visual hierarchy
- Clear All Data button uses ButtonVariant.Destructive for red styling
- Added unit tests for SettingsViewModel initial state verification
- Updated Koin module to register SettingsViewModel
- All tests pass, build successful

### File List
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsScreen.kt (modified)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsUiState.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsSectionHeader.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsToggleRow.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsLinkRow.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsInfoRow.kt (new)
- feature/settings/src/main/kotlin/com/rulebook/feature/settings/di/SettingsModule.kt (modified)
- feature/settings/src/test/kotlin/com/rulebook/feature/settings/SettingsViewModelTest.kt (new)
- feature/settings/build.gradle.kts (modified)

## Dependencies

- **Depends On:** Story 2.3 (Bottom Navigation Bar), Epic 1 (design system)
- **Blocks:** Story 2.7
- **Can Parallel With:** Story 2.5

### Dependency Rationale
- Story 2.3: Settings screen navigated via bottom nav tab
- Epic 1: Requires design system components
- Story 2.7: Scaffold integration requires Settings screen to exist
- Story 2.5: Can develop in parallel (different screen)
