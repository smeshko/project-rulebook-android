# Epic 9: Settings & Platform Polish

**Goal:** Complete the settings functionality and add Android platform enhancements. After this epic, users can customize theme/haptics, access support, and enjoy native Android features.

**FRs covered:** FR42-52

---

## Story 9.1: Theme Selection (Light/Dark/System)

As a user,
I want to choose my preferred app theme,
So that the app matches my preference or system setting.

**Acceptance Criteria:**

**Given** the user is in Settings (FR42)
**When** viewing Appearance section
**Then** theme options are shown:
- Light
- Dark
- System (default)

**And** selecting an option applies immediately
**And** preference persists across sessions
**And** "System" follows device dark mode setting

**Architecture requirements:**
- Theme state from `core/datastore`: `RulebookPreferences.themeMode: Flow<ThemeMode>`
- `ThemeMode` enum in `core/model`: `LIGHT`, `DARK`, `SYSTEM`
- Theme applied at root level: `RulebookTheme(darkTheme = when(themeMode) { SYSTEM -> isSystemInDarkTheme(), LIGHT -> false, DARK -> true })`
- `MainActivity` collects `themeMode` Flow and passes to `RulebookTheme` wrapper
- Preference update: `RulebookPreferences.setThemeMode(mode: ThemeMode)` — suspend function
- Change is instant — no app restart needed (Compose recomposition handles it)

**UX/Component specifications:**
- Appearance section header: `RulebookSectionHeader` with "APPEARANCE" title, `SectionHeaderVariant.Section` variant
- Theme selector: `RulebookPicker` composable or segmented control:
  - Options: "System", "Light", "Dark" — displayed as `RulebookPicker` dropdown or horizontal radio group
  - Currently selected option shown with checkmark or highlighted state
  - Picker styling: border 2dp black, shadow 4dp, background `surface.primary`
- Alternative: 3 `RulebookListRow` items with radio-button accessory:
  - Each row: icon box with sun/moon/auto icon in `brutalist.orange` (#FF6B35/#FF8C5F), title text, radio indicator
  - Selected row: filled radio circle in `brutalist.pink` (#E91E63/#F06292)
  - Row spec: min height 64dp, padding 16dp horizontal, border 3dp black bottom
- Instant apply: Theme switches on tap with no animation delay (immediate recomposition)
- Color transition: Colors animate via `animateColorAsState` in `RulebookTheme` for smooth switch

**Technical notes:**
- `RulebookPicker` or radio group for theme selection
- DataStore `themeMode` preference: `stringPreferencesKey("theme_mode")`
- `RulebookTheme` reads preference and applies `rulebookDarkColors` or `rulebookLightColors`
- Use `isSystemInDarkTheme()` for System mode detection

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (DataStore, Theme)

---

## Story 9.2: Haptic Feedback Toggle

As a user,
I want to enable or disable haptic feedback,
So that I can control the app's vibration behavior.

**Acceptance Criteria:**

**Given** the user is in Settings (FR43)
**When** viewing Feedback section
**Then** a toggle shows "Haptic Feedback" with current state
**And** toggling updates immediately
**And** preference persists across sessions
**And** all haptics in app respect this setting

**Architecture requirements:**
- Haptic state from `core/datastore`: `RulebookPreferences.hapticsEnabled: Flow<Boolean>` (default: `true`)
- Update: `RulebookPreferences.setHapticsEnabled(enabled: Boolean)`
- All haptic callsites check this preference before triggering VibrationEffect
- Utility function in `core/common`: `HapticHelper.vibrate(context, effect, hapticsEnabled)` — no-op if disabled
- Preference observed in ViewModels that trigger haptics (CameraViewModel, RulesViewModel, etc.)

**UX/Component specifications:**
- Feedback section header: `RulebookSectionHeader` with "FEEDBACK" title
- Toggle row: `RulebookListRow` with toggle accessory:
  - Icon: vibration icon in `brutalist.orange` icon box (40dp, `surface.tertiary` background, 3dp black border)
  - Title: "Haptic Feedback" in `body.body` (17sp, Regular)
  - Subtitle: "Vibration on interactions" in `detail.caption` (12sp, Regular), `content.secondary`
  - Accessory: `RulebookToggle` composable:
    - Width: 60dp, height: 32dp, knob: 24dp
    - On: `brutalist.green` (#2ECC71/#58D68D) track
    - Off: `surface.primary` track
    - Border: 3dp solid black, shadow: 4dp offset
    - Animation: knob slides 200ms `EaseInOut`
- Row spec: min height 64dp, border 3dp black bottom, padding 16dp horizontal
- Toggling gives haptic feedback itself (ironic but expected — one last vibration before disabling)

**Technical notes:**
- `RulebookToggle` composable (or Material 3 `Switch` with brutalist styling)
- DataStore `hapticsEnabled` preference: `booleanPreferencesKey("haptics_enabled")`
- Check preference before any `VibrationEffect` — `if (hapticsEnabled) vibrator.vibrate(...)`

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (DataStore)

---

## Story 9.3: Support Links Section

As a user,
I want to access support resources,
So that I can get help or report issues.

**Acceptance Criteria:**

**Given** the user is in Settings (FR44)
**When** viewing Support section
**Then** links are shown:
- "Contact Support" → Opens email composer
- "Report a Bug" → Opens email or form
- "Rate on Play Store" → Opens Play Store listing

**And** links open in external apps/browser
**And** email includes app version in subject

**Architecture requirements:**
- Support actions handled directly in `SettingsViewModel` — no repository needed
- Uses Android Intents:
  - Email: `Intent.ACTION_SENDTO` with `Uri.parse("mailto:support@rulebook.app")`
  - Play Store: `Intent.ACTION_VIEW` with `Uri.parse("market://details?id=${context.packageName}")`
- Version string from `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE`
- Email subject pre-populated: "Rulebook Android v${VERSION_NAME} (${VERSION_CODE}) - [Support/Bug]"
- Intent launched from composable via `LocalContext.current.startActivity(intent)`

**UX/Component specifications:**
- Support section header: `RulebookSectionHeader` with "SUPPORT" title
- Link rows using `RulebookListRow` with chevron accessory:
  - "Contact Us":
    - Icon: mail icon in `brutalist.blue` (#3498DB/#5DADE2) icon box
    - Accessory: `ListRowAccessory.Chevron`
  - "Report a Bug":
    - Icon: bug icon in `brutalist.orange` (#FF6B35/#FF8C5F) icon box
    - Accessory: `ListRowAccessory.Chevron`
  - "Rate the App":
    - Icon: star icon in `brutalist.yellow` (#FFD23F/#FFE066) icon box
    - Accessory: `ListRowAccessory.Chevron`
- Row styling: `RulebookListRow` spec — min height 64dp, padding 16dp horizontal, border 3dp black bottom
- Icon box: 40dp, `surface.tertiary` background, 3dp black border
- Tap opens external app — no in-app browser

**Technical notes:**
- `Intent.ACTION_SENDTO` for email with `mailto:` URI
- `Intent.ACTION_VIEW` for Play Store with `market://` URI (fallback to `https://play.google.com/...`)
- Include version: "Rulebook v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
- Play Store fallback: if `market://` fails, try HTTPS URL

**Prerequisites:** Epic 2 (Settings shell)

---

## Story 9.4: About Section (Version & Legal)

As a user,
I want to see app version and legal information,
So that I can verify my app version and access policies.

**Acceptance Criteria:**

**Given** the user is in Settings (FR45)
**When** viewing About section
**Then** it shows:
- App version: "Version X.Y.Z (build N)"
- "Privacy Policy" link → Opens URL
- "Terms of Service" link → Opens URL

**And** version is dynamically read from BuildConfig

**Architecture requirements:**
- Version info from `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE` — available at compile time
- Legal links use `Intent.ACTION_VIEW` with HTTPS URLs
- URLs configured as constants in `core/common` or `BuildConfig` fields
- No network calls — links open in system browser

**UX/Component specifications:**
- About section header: `RulebookSectionHeader` with "ABOUT" title
- Version display: `RulebookListRow` with value accessory:
  - Icon: info icon in `brutalist.gray` (#95A5A6/#B2BEC3) icon box
  - Title: "Version" in `body.body` (17sp, Regular)
  - Accessory: `ListRowAccessory.Value("X.Y.Z (N)")` in `detail.caption` (12sp, Regular), `content.secondary`
  - Non-tappable (no chevron, no click handler)
- Legal links: `RulebookListRow` with chevron accessory:
  - "Privacy Policy": Icon: shield icon, `brutalist.blue` icon box, chevron
  - "Terms of Service": Icon: document icon, `brutalist.blue` icon box, chevron
- Row styling: Standard `RulebookListRow` spec per other sections

**Technical notes:**
- `BuildConfig.VERSION_NAME` and `BuildConfig.VERSION_CODE` for version display
- `Intent.ACTION_VIEW` for policy URLs — opens in Chrome or default browser
- Policy URLs: hardcoded constants (e.g., `https://rulebook.app/privacy`, `https://rulebook.app/terms`)

**Prerequisites:** Epic 2 (Settings shell)

---

## Story 9.5: Clear App Data with Confirmation

As a user,
I want to reset the app to a fresh state,
So that I can start over if needed.

**Acceptance Criteria:**

**Given** the user is in Settings (FR46)
**When** tapping "Clear All Data"
**Then** a confirmation dialog appears (FR47)
**And** dialog warns: "This will delete all saved games and reset settings"
**And** "Clear Data" (destructive) and "Cancel" buttons

**When** user confirms
**Then** all Room data is deleted
**And** DataStore preferences are reset (except credits? TBD)
**And** App returns to onboarding or library
**And** success toast confirms

**Architecture requirements:**
- Clear operation orchestrated in `SettingsViewModel`:
  1. `RulebookDatabase.clearAllTables()` — Room wipe
  2. `RulebookPreferences.reset()` — DataStore reset to defaults (preserves credits purchased with real money)
  3. Navigate to onboarding or library based on `hasCompletedOnboarding` (which was just reset)
- Credit handling decision: **Preserve purchased credits** — clearing data should not steal money
  - `RulebookPreferences.reset()` resets all except `creditBalance`
  - Alternative: reset credits to 3 (re-grant free credits) — TBD product decision
- Confirmation state: `SettingsUiState(showClearConfirmation: Boolean)`
- Navigation after clear: emit `SettingsEvent.NavigateToOnboarding` via Channel

**UX/Component specifications:**
- Clear data button: `RulebookButton` destructive variant positioned at bottom of settings:
  - Red fill: `brutalist.red` (#E74C3C/#EC7063), 3dp black border, 4dp shadow
  - Text: "CLEAR ALL DATA" in `brutalist.buttonText` (14sp, Black 900)
  - Full width minus `spacing.md` (16dp) margins
- Data section header: `RulebookSectionHeader` with "DATA" title
- Confirmation dialog: `AlertDialog` with brutalist styling:
  - Title: "CLEAR ALL DATA?" in `brutalist.title` (24sp, Black 900)
  - Body: "This will delete all saved games and reset your settings. Your purchased credits will be preserved." in `body.body` (17sp)
  - Warning icon: `brutalist.red` (#E74C3C) triangle
  - "Clear Data" button: `RulebookButton` destructive variant
  - "Cancel" button: `RulebookButton` secondary variant
- Success feedback: Snackbar "All data cleared" — 2s auto-dismiss
- Haptic feedback: Medium click on clear confirmation

**Technical notes:**
- `RulebookDatabase.clearAllTables()` for Room wipe
- DataStore `edit { prefs -> prefs.clear(); prefs[CREDIT_BALANCE] = preservedBalance }` to keep credits
- Navigate to appropriate screen after clear
- Dialog uses same brutalist styling as delete confirmation (Story 7.4)

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (Room, DataStore)

---

## Story 9.6: App Shortcuts for Quick Scan

As a user,
I want to long-press the app icon for quick actions,
So that I can start scanning faster.

**Acceptance Criteria:**

**Given** the app is installed (FR48)
**When** the user long-presses the launcher icon
**Then** a shortcut menu appears with:
- "Scan Game" → Opens camera directly

**And** shortcut works even if app is not running
**And** shortcut icon matches app branding

**Architecture requirements:**
- Static shortcut defined in `res/xml/shortcuts.xml`:
  ```xml
  <shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
      <shortcut android:shortcutId="scan_game"
          android:enabled="true"
          android:icon="@drawable/ic_shortcut_camera"
          android:shortcutShortLabel="@string/shortcut_scan"
          android:shortcutLongLabel="@string/shortcut_scan_long">
          <intent android:action="android.intent.action.VIEW"
              android:targetPackage="com.rulebook.app"
              android:targetClass="com.rulebook.app.MainActivity"
              android:data="rulebook://camera" />
      </intent>
      </shortcut>
  </shortcuts>
  ```
- Deep link handling in `RulebookNavHost`: `rulebook://camera` → navigate to Camera route
- Credit check on shortcut launch: If credits = 0, redirect to paywall instead of camera
- Manifest: `<meta-data android:name="android.app.shortcuts" android:resource="@xml/shortcuts" />`

**UX/Component specifications:**
- Shortcut label: "Scan Game" (shortLabel), "Scan a Board Game" (longLabel)
- Shortcut icon: Camera icon matching app branding — `ic_shortcut_camera` drawable
  - Use adaptive icon format for consistency across launchers
  - Color: `brutalist.pink` (#E91E63) background with white camera icon
- Shortcut reduces friction by 2+ taps for repeat users (UX §Design Opportunities: "App Shortcuts")
- Behavior identical to tapping FAB — same credit check, same camera flow

**Technical notes:**
- Static shortcut in `res/xml/shortcuts.xml` (preferred over dynamic for reliability)
- Deep link to camera route: `rulebook://camera`
- Handle credit check on shortcut launch — may redirect to paywall
- Test on multiple launchers (Pixel, Samsung, etc.)

**Prerequisites:** Epic 4 (Camera screen)

---

## Story 9.7: Predictive Back Gesture Refinement (QA Polish Pass)

> **Note:** This is a QA/polish pass over the predictive back gesture support implemented in **Story 2.8**. This story focuses on per-screen verification and edge case testing, not reimplementation.

As a user,
I want back gestures to feel native and predictable,
So that navigation matches my Android expectations.

**Acceptance Criteria:**

**Given** predictive back is enabled (FR49)
**When** on any screen with back navigation
**Then** back gesture preview shows previous screen
**And** all screens properly handle back:
- Camera → Library
- Rules → Library
- Onboarding → Exit app (or nothing)
- Paywall → Previous screen

**And** no screens trap the user

**Architecture requirements:**
- `android:enableOnBackInvokedCallback="true"` in `AndroidManifest.xml`
- Compose Navigation handles predictive back automatically with `AnimatedNavHost`
- Custom `BackHandler` composables only where non-standard behavior needed:
  - Onboarding: Back on first screen → exit app (system default)
  - Camera: Back → release camera resources + navigate back (via `DisposableEffect`)
  - Paywall bottom sheet: Back → dismiss sheet (handled by `ModalBottomSheet` internally)
- No screen should consume back without navigating — prevents "trapped" state

**UX/Component specifications:**
- Predictive back preview: System-provided animation showing previous screen shrinking behind current (UX §Design Opportunities: "Predictive Back Gesture")
- All screens: Standard back gesture from left edge
- Full-screen modals (Camera, Generation): Back preview shows Library beneath
- Bottom sheets (Paywall): Back dismisses sheet with standard animation
- Back on main tabs (Library/Settings): System handles — exits app or goes to home
- No custom back animations — use system defaults for familiarity (UX §Anti-Patterns: "Standard Android patterns")

**Technical notes:**
- Verify `BackHandler` usage across all screens — ensure no conflicts
- Test gesture on Android 14+ devices (API 34+)
- Ensure proper NavController back stack configuration
- `DisposableEffect` in Camera screen for resource cleanup on back

**Prerequisites:** Epic 2 (Story 2.8)

---

## Story 9.8: Edge-to-Edge Polish (QA Polish Pass)

> **Note:** This is a QA/polish pass over the edge-to-edge setup implemented in **Story 2.1**. This story focuses on per-screen verification and device-specific testing, not reimplementation.

As a user,
I want the app to look polished edge-to-edge,
So that the interface feels modern and immersive.

**Acceptance Criteria:**

**Given** edge-to-edge is enabled (FR50)
**When** viewing any screen
**Then** content extends properly under system bars
**And** no content is obscured by status/nav bars
**And** status bar icons contrast properly on all screens
**And** navigation bar blends with bottom nav

**Architecture requirements:**
- `enableEdgeToEdge()` called in `MainActivity.onCreate()` (already set up in Story 2.1)
- WindowInsets handling per screen type:
  - Main screens (Library, Settings): `Scaffold` handles `WindowInsets.systemBars` padding
  - Full-screen (Camera): Custom `WindowInsets` handling — status bar padding on overlay controls only
  - Bottom sheet (Paywall): `ModalBottomSheet` handles insets internally
- Status bar icon color: Managed by `WindowInsetsController.setAppearanceLightStatusBars()` based on theme
- Navigation bar: Transparent, blends with `surface.primary` or `surface.secondary` depending on screen

**UX/Component specifications:**
- Status bar: Transparent, icons adapt to theme:
  - Light theme: Dark icons (`setAppearanceLightStatusBars(true)`)
  - Dark theme: Light icons (`setAppearanceLightStatusBars(false)`)
- Navigation bar: Transparent, edge-to-edge — bottom nav extends under it with proper padding
- Camera screen: Status bar fully transparent, overlay controls have own padding
- `RulebookHeaderBar`: Already includes `WindowInsets.statusBars` padding (Story 1.11)
- Bottom navigation: Includes `WindowInsets.navigationBars` padding
- Brutalist aesthetic benefits from full-screen presence (UX §Design Opportunities: "Edge-to-Edge Display")
- Per-screen verification checklist:
  - Library: Header under status bar ✓, grid above nav bar ✓
  - Settings: Header under status bar ✓, scroll content above nav bar ✓
  - Camera: Full bleed ✓, overlay controls padded ✓
  - Rules: Header under status bar ✓, content scrolls freely ✓
  - Paywall: Sheet respects bottom inset ✓

**Technical notes:**
- Review all screens for `WindowInsets` handling correctness
- Ensure `RulebookHeaderBar` uses `WindowInsets.statusBars` top padding
- Ensure bottom nav uses `WindowInsets.navigationBars` bottom padding
- Test on devices with different bar heights (gesture nav vs. 3-button nav)
- Camera screen: Use `WindowInsets.displayCutout` for notch-aware layout

**Prerequisites:** Epic 2 (Story 2.1)

---

## Story 9.9: Settings Analytics Events

As a product owner,
I want to track settings changes,
So that I can understand user preferences.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** settings events occur
**Then** the following events are tracked:
- `settings_theme_changed` - With new theme value
- `settings_haptics_changed` - With enabled/disabled
- `settings_data_cleared` - User cleared data
- `settings_support_tapped` - Which link tapped

**Architecture requirements:**
- Events fired from `SettingsViewModel` using `AnalyticsManager` from `core/analytics`
- Event names MUST match iOS event names for cross-platform consistency
- No PII in event properties — only preference values and link identifiers

**Technical notes:**
- Events fired from SettingsViewModel on each preference change
- Properties per event:
  - `settings_theme_changed`: `theme` (light/dark/system), `previous_theme`
  - `settings_haptics_changed`: `enabled` (true/false)
  - `settings_data_cleared`: `games_count` (number of games deleted)
  - `settings_support_tapped`: `link` (contact/bug/rate/privacy/terms)
- Match iOS event names from TelemetryDeck integration

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 9 Complete: Settings & Platform Polish**

**Stories Created:** 9
**FR Coverage:** FR42-52
**Architecture Sections Referenced:** feature/settings, core/datastore, core/common, core/analytics, core/designsystem, app module (shortcuts, manifest)
**UX Patterns Incorporated:** Theme switching, toggles, confirmation dialogs, deep links, app shortcuts, predictive back, edge-to-edge, haptic feedback
**Design Tokens Referenced:** brutalist.orange, brutalist.blue, brutalist.yellow, brutalist.green, brutalist.red, brutalist.pink, brutalist.gray, surface.primary, surface.tertiary, typography.brutalist.title, typography.brutalist.buttonText, typography.body.body, typography.detail.caption
