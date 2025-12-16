# Story 2.6: Settings Screen Shell

Status: ready-for-dev

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

- [ ] Task 1: Create SettingsScreen composable (AC: #1, #4)
  - [ ] Create feature/settings module structure
  - [ ] Create SettingsScreen.kt
  - [ ] Add RulebookHeaderBar with "Settings" title
  - [ ] Implement scrollable content with LazyColumn
- [ ] Task 2: Create SettingsViewModel (AC: #2)
  - [ ] Create SettingsViewModel.kt
  - [ ] Create SettingsUiState data class
  - [ ] Track placeholder states
  - [ ] Expose StateFlow for UI observation
- [ ] Task 3: Create section header component (AC: #3)
  - [ ] Create SettingsSectionHeader composable
  - [ ] Apply brutalist styling with border
  - [ ] Use consistent typography
- [ ] Task 4: Implement Appearance section (AC: #2)
  - [ ] Add section header "Appearance"
  - [ ] Add theme toggle placeholder row
  - [ ] Use brutalist styling
- [ ] Task 5: Implement Feedback section (AC: #2)
  - [ ] Add section header "Feedback"
  - [ ] Add haptics toggle placeholder row
- [ ] Task 6: Implement Support section (AC: #2)
  - [ ] Add section header "Support"
  - [ ] Add placeholder links (Contact, Rate, Share)
- [ ] Task 7: Implement About section (AC: #2)
  - [ ] Add section header "About"
  - [ ] Add version info placeholder
  - [ ] Add legal links placeholder (Privacy, Terms)
- [ ] Task 8: Implement Data section (AC: #2)
  - [ ] Add section header "Data"
  - [ ] Add clear data placeholder button
- [ ] Task 9: Create Koin module for settings feature
  - [ ] Create SettingsModule.kt
  - [ ] Register SettingsViewModel
  - [ ] Register in app module

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
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 2.3 (Bottom Navigation Bar), Epic 1 (design system)
- **Blocks:** Story 2.7
- **Can Parallel With:** Story 2.5

### Dependency Rationale
- Story 2.3: Settings screen navigated via bottom nav tab
- Epic 1: Requires design system components
- Story 2.7: Scaffold integration requires Settings screen to exist
- Story 2.5: Can develop in parallel (different screen)
