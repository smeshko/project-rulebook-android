# Epic 9: Settings & Platform Polish

**Goal:** Complete the settings functionality and add Android platform enhancements. After this epic, users can customize theme/haptics, access support, and enjoy native Android features.

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

**Technical Notes:**
- Segmented control or radio group
- DataStore `themeMode` preference
- RulebookTheme reads preference
- Use `isSystemInDarkTheme()` for System mode

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

**Technical Notes:**
- Switch composable with brutalist styling
- DataStore `hapticsEnabled` preference
- Check preference before any VibrationEffect

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

**Technical Notes:**
- Intent.ACTION_SENDTO for email
- Intent.ACTION_VIEW for Play Store
- Include version: "Rulebook v${BuildConfig.VERSION_NAME}"

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

**Technical Notes:**
- `BuildConfig.VERSION_NAME` and `VERSION_CODE`
- Intent.ACTION_VIEW for policy URLs
- WebView or external browser

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

**Technical Notes:**
- RulebookDatabase.clearAllTables()
- DataStore clear or reset to defaults
- Decide on credit handling (preserve or reset)
- Navigate to appropriate screen

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

**Technical Notes:**
- Static shortcut in `res/xml/shortcuts.xml`
- ShortcutManager for dynamic shortcuts (optional)
- Deep link to camera route
- Handle credit check on shortcut launch

**Prerequisites:** Epic 4 (Camera screen)

---

## Story 9.7: Predictive Back Gesture Refinement

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

**Technical Notes:**
- Verify BackHandler usage across all screens
- Test gesture on Android 14+ devices
- Ensure proper NavController back stack

**Prerequisites:** Epic 2 (Story 2.8)

---

## Story 9.8: Edge-to-Edge Polish

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

**Technical Notes:**
- Review all screens for WindowInsets handling
- Ensure header bars have status bar padding
- Ensure bottom nav has nav bar padding
- Test on devices with different bar heights

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

**Technical Notes:**
- Events fired from SettingsViewModel
- Include relevant property values
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 9 Complete: Settings & Platform Polish**

**Stories Created:** 9
**FR Coverage:** FR42-50
**Architecture Sections Referenced:** feature/settings, core/datastore, App Shortcuts
**UX Patterns Incorporated:** Theme switching, toggles, confirmation dialogs, deep links

---
