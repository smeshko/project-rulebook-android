# User Flows & Navigation - Android Migration

This document describes the navigation structure and key user journeys for the Rulebook app.

---

## Navigation Architecture

### Pattern: Modal-over-Tab

The app uses a tab-based navigation with modal overlays:

```
+------------------------------------------+
|                                          |
|            Modal Layer                   |
|   (Photo, Progress, Rules, Paywall)     |
|                                          |
+------------------------------------------+
|                                          |
|            Content Layer                 |
|      (Library or Settings screen)        |
|                                          |
+------------------------------------------+
|  [Library]  [Camera FAB]  [Settings]    |
|              Tab Bar                     |
+------------------------------------------+
```

### Tab Structure

| Tab | Icon | Destination | Notes |
|-----|------|-------------|-------|
| 1 | `books.vertical` | Library | Start destination |
| 2 | `camera.fill` | N/A | Floating action, triggers modal flow |
| 3 | `gearshape` | Settings | - |

### Modal Stack

Only ONE modal can be active at a time. Modal precedence:
1. Onboarding (first launch only, blocks everything)
2. Camera Flow (Photo -> Progress -> Rules)
3. Paywall (shown when credits = 0)

---

## Navigation Graph

```
// Android Navigation Graph Structure

NavHost(startDestination = "library") {

    // Tab Destinations
    composable("library") { LibraryScreen() }
    composable("settings") { SettingsScreen() }

    // Library Detail (push navigation)
    composable("rules/{gameId}") { RulesScreen() }

    // Full-screen Modals
    dialog("photo_capture", dialogProperties = fullScreen) { PhotoCaptureScreen() }
    dialog("rules_generation", dialogProperties = fullScreen) { RulesGenerationScreen() }

    // Sheet Modals
    bottomSheet("rules_result") { RulesResultSheet() }
    bottomSheet("paywall") { PaywallSheet() }

    // Onboarding (separate graph or initial check)
    dialog("onboarding", dialogProperties = fullScreen) { OnboardingScreen() }
}
```

---

## User Flows

### Flow 1: First Launch (Onboarding)

**Trigger**: App first launch, `onboardingCompleted == false`

```
App Launch
    |
    v
Check SharedPreferences: onboardingCompleted
    |
    No (first launch)
    |
    v
+------------------------+
|   Onboarding Screen 1  |
|                        |
|   "SCAN & LEARN"       |
|   - Feature cards      |
|   - [NEXT] button      |
+------------------------+
    |
    [NEXT]
    |
    v
+------------------------+
|   Onboarding Screen 2  |
|                        |
|   "BUILD YOUR LIBRARY" |
|   - Feature cards      |
|   - [GET STARTED]      |
+------------------------+
    |
    [GET STARTED]
    |
    v
Set onboardingCompleted = true
    |
    v
Navigate to Library Screen
```

**Android Implementation**:
- Check `SharedPreferences` on app start
- Use `HorizontalPager` for onboarding screens
- `PageIndicator` composable for dots
- On completion, set flag and pop to main graph

---

### Flow 2: Core Scan Flow (Happy Path)

**Trigger**: User taps camera FAB

```
Library Screen
    |
    [Camera FAB tap]
    |
    v
Check credit balance
    |
    |--[credits > 0]----------+
    |                         |
    [credits == 0]            |
    |                         |
    v                         |
+----------------+            |
|    Paywall     |            |
|                |            |
| - Products     |            |
| - [Purchase]   |            |
| - [X] Close    |            |
+----------------+            |
    |                         |
    [Purchase success]        |
    |                         |
    v                         |
+------------------------+<---+
|    Photo Capture       |
|                        |
| - Camera preview       |
| - [Capture] button     |
| - [X] Close            |
| - Flash toggle         |
| - Gallery picker       |
+------------------------+
    |
    [Capture photo]
    |
    v
+------------------------+
|  Rules Generation      |
|                        |
|  Progress: 0% -> 100%  |
|  - Processing Image    |
|  - Analyzing Image     |
|  - Fetching Rules      |
|  - Saving Rules        |
|                        |
|  [Cancel] button       |
+------------------------+
    |
    [Generation complete]
    |
    v
Deduct 1 credit
    |
    v
+------------------------+
|    Rules Result        |
|    (Sheet Modal)       |
|                        |
| - Game title           |
| - Metadata badges      |
| - Collapsible sections |
| - [SAVE TO LIBRARY]    |
| - [CLOSE]              |
+------------------------+
    |
    [Save or Close]
    |
    v
Refresh Library
    |
    v
Return to Library Screen
```

**Key States**:
- Credit check happens BEFORE camera opens
- Credit deduction happens AFTER successful generation
- If generation fails, no credit deducted

---

### Flow 3: View Saved Game Rules

**Trigger**: User taps game card in Library

```
Library Screen
    |
    [Tap game card]
    |
    v
Navigate with gameId parameter
    |
    v
+------------------------+
|    Rules Screen        |
|    (Full screen)       |
|                        |
| - Back button          |
| - Game title           |
| - All rule sections    |
| - Share button         |
+------------------------+
    |
    [Back button]
    |
    v
Pop back to Library
```

**Android Implementation**:
- Use NavController `navigate("rules/$gameId")`
- Rules screen is a composable destination (not modal)
- Back button or system back returns to Library

---

### Flow 4: Delete Game

**Trigger**: Long-press game card in Library

```
Library Screen
    |
    [Long-press game card]
    |
    v
+------------------------+
|    Context Menu        |
|                        |
|  - Delete              |
|  - Share               |
+------------------------+
    |
    [Delete]
    |
    v
+------------------------+
|  Confirmation Dialog   |
|                        |
|  "Delete [Game Name]?" |
|  [CANCEL] [DELETE]     |
+------------------------+
    |
    [DELETE]
    |
    v
Remove from database
    |
    v
Refresh Library grid
```

**Android Implementation**:
- Use `combinedClickable` with `onLongClick`
- Show `DropdownMenu` or `AlertDialog`
- Animate item removal from grid

---

### Flow 5: Purchase Credits

**Trigger**: Credits exhausted OR tap credit balance in Settings

```
[Entry Point]
    |
    v
+------------------------+
|      Paywall           |
|    (Sheet Modal)       |
|                        |
| Current: X credits     |
|                        |
| Feature highlights:    |
| - Instant Scanning     |
| - Clear Instructions   |
| - Offline Access       |
|                        |
| Products:              |
| [1 Credit] [3] [10]    |
|            ^Popular    |
|                        |
| [Restore Purchases]    |
+------------------------+
    |
    [Tap product]
    |
    v
+------------------------+
|  Loading State         |
|  (on selected card)    |
+------------------------+
    |
    [StoreKit/Play Billing response]
    |
    +--[Success]-------+
    |                  |
    [Failure]          |
    |                  v
    v            Add credits to balance
+------------+         |
| Error      |         v
| Alert      |   Haptic success
+------------+         |
    |                  v
    v            Dismiss Paywall
Stay on              |
Paywall              v
               Continue to Camera
               (if from FAB)
```

**Android Implementation**:
- Use Google Play Billing Library
- `BillingClient` for product queries
- `launchBillingFlow` for purchases
- Verify purchases server-side or locally

---

### Flow 6: Change Settings

**Trigger**: Navigate to Settings tab

```
Settings Screen
    |
    +--[Theme picker]---> Theme Dialog
    |                         |
    |                    [Select theme]
    |                         |
    |                    Apply immediately
    |                         |
    +<------------------------+
    |
    +--[Haptics toggle]---> Toggle state
    |                         |
    |                    Save to preferences
    |                         |
    +<------------------------+
    |
    +--[Credit balance tap]---> Paywall Sheet
    |
    +--[Help Center]---> External browser
    |
    +--[Report Bug]---> External browser
    |
    +--[Rate App]---> Play Store
    |
    +--[Clear Data]---> Confirmation Dialog
                              |
                         [CONFIRM]
                              |
                         Clear database
                         Reset preferences
                              |
                         Restart app
```

---

## Gesture Interactions

### Library Screen
| Gesture | Action |
|---------|--------|
| Tap card | Open rules detail |
| Long-press card | Show context menu |
| Pull down | Refresh library |
| Scroll | Grid scrolling |

### Photo Capture
| Gesture | Action |
|---------|--------|
| Tap anywhere | Focus camera |
| Tap capture | Take photo |
| Tap flash | Toggle flash mode |
| Tap X | Close camera |
| Pinch | Zoom (optional) |

### Rules Screen (Sheet)
| Gesture | Action |
|---------|--------|
| Swipe down | Dismiss sheet |
| Tap section header | Expand/collapse |
| Tap Save | Save to library |

### Collapsible Sections
| Gesture | Action |
|---------|--------|
| Tap header | Toggle expand/collapse |
| Tap chevron | Toggle expand/collapse |

---

## Navigation State Management

### State to Preserve
- Library sort order
- Scroll position in Library
- Expanded sections in Rules view
- Selected tab

### State to Reset
- Camera state on close
- Generation progress on cancel
- Paywall state on dismiss

### Deep Links (Future)
```
rulebook://game/{gameId}     -> Open specific game rules
rulebook://scan              -> Open camera
rulebook://library           -> Open library
```

---

## Android-Specific Considerations

### Back Button Handling
```kotlin
// Modal screens should intercept back
BackHandler(enabled = isModalVisible) {
    dismissModal()
}

// Tab screens use default behavior
// (exit app from Library, go to Library from Settings)
```

### Process Death Recovery
- Save `currentTab` to `SavedStateHandle`
- Restore scroll position with `LazyGridState`
- Modal state: dismiss on recreation (user expectation)

### Configuration Changes
- Camera: Handle rotation, recreate session
- Other screens: Standard Compose handling

### Permissions
```kotlin
// Camera permission flow
val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

when {
    cameraPermission.status.isGranted -> ShowCamera()
    cameraPermission.status.shouldShowRationale -> ShowRationale()
    else -> RequestPermission()
}
```

---

## Transition Animations

### Tab Switching
- Crossfade between Library and Settings
- Duration: 300ms

### Modal Presentation
- Full-screen: Slide up from bottom
- Sheet: Slide up with scrim
- Duration: 300ms

### Card Interactions
- Press: Scale to 0.98, shadow offset decrease
- Duration: 150ms

### Section Expand/Collapse
- Height animation with content fade
- Duration: 300ms
- Easing: EaseInOut

### Progress Bar
- Smooth width animation
- Duration: Continuous during generation
