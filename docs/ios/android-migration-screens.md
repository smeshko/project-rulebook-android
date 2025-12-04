# Screen Inventory - Android Migration

This document provides a complete inventory of all screens in the Rulebook iOS app for Android migration.

## Screen Summary

| Screen | Type | Navigation | Priority |
|--------|------|------------|----------|
| Library | Tab Screen | Tab 1 | P0 |
| Settings | Tab Screen | Tab 3 | P0 |
| Photo Capture | Full-screen Modal | From Camera Button | P0 |
| Rules Generation Progress | Full-screen Modal | After Photo Capture | P0 |
| Rules Display | Sheet Modal | After Generation | P0 |
| Paywall | Sheet Modal | When Credits = 0 | P0 |
| Onboarding (2 screens) | Full-screen Modal | First Launch Only | P1 |

---

## 1. Library Screen

**iOS File**: `rulebook-kit/Sources/Features/LibraryFeature/Presentation/Views/LibraryView.swift`

**Purpose**: Main hub displaying user's saved games

**Layout**:
- Header with "LIBRARY" title and sort picker
- 2-column grid of game cards
- Pull-to-refresh support
- Empty state when no games

**Components Used**:
- `RulebookHeaderBar` - Screen header
- `RulebookCard` - Game cards in grid
- `RulebookPicker` - Sort dropdown
- `RulebookEmptyState` - No games state

**Features**:
- Sort options: Recent, Alphabetical, Oldest, Play Time
- Long-press context menu for delete
- Tap card to view rules (NavigationStack push)
- Pull-to-refresh

**States**:
- Loading (initial load)
- Empty (no saved games)
- Populated (game grid)
- Error (load failure)

**Empty State Content**:
```
Icon: books.vertical
Title: "NO GAMES YET"
Description: "Start building your collection by scanning game boxes"
Tips:
- "Look for game boxes with clear artwork"
- "Good lighting helps recognition"
- "Try popular games first"
Action: "SCAN YOUR FIRST GAME" -> Opens camera
```

---

## 2. Settings Screen

**iOS File**: `rulebook-kit/Sources/Features/SettingsFeature/Presentation/Views/SettingsView.swift`

**Purpose**: User preferences and app settings

**Layout**:
- Header with "SETTINGS" title
- Grouped list sections
- Credit balance display at top

**Sections**:

### Account Section
- Credit balance display (tappable -> Paywall)

### Preferences Section
- Theme picker (System/Light/Dark)
- Haptics toggle

### Support Section
- Help Center (external link)
- Report a Bug (external link)
- Rate the App (App Store)

### Legal Section
- Terms of Service (external link)
- Privacy Policy (external link)

### Debug Section (Debug builds only)
- Environment picker (Dev/Staging/Prod)
- Clear All Data button

### App Info
- Version and build number

**Components Used**:
- `RulebookHeaderBar`
- `RulebookListRow` with various accessories
- `RulebookPicker` for theme
- `RulebookToggle` for haptics
- `RulebookSectionHeader`

---

## 3. Photo Capture Screen

**iOS File**: `rulebook-kit/Sources/Features/PhotoFeature/Presentation/Views/PhotoCaptureView.swift`

**Purpose**: Full-screen camera for game box photography

**Layout**:
- Full-screen camera preview
- Overlay with controls
- Safe area considerations for notch/islands

**Overlay Components** (PhotoOverlayView.swift):
- Top bar: Close button (left), Flash toggle (right)
- Center: Tap-to-focus area
- Bottom: Photo library button (left), Capture button (center), Rotate button (right)

**Camera Controls**:
- Flash/Torch toggle (on/off/auto)
- Capture button (circular, prominent)
- Photo library picker
- Photo rotation (90 degree increments)
- Tap-to-focus

**States**:
- Permission not determined -> Request prompt
- Permission denied -> Settings redirect view
- Permission granted -> Camera preview
- Capturing -> Shutter animation
- Processing -> Brief loading

**Permission View Content**:
```
Icon: camera.fill
Title: "CAMERA ACCESS NEEDED"
Description: "Rulebook needs camera access to scan game boxes"
Action: "ENABLE CAMERA" -> System settings
```

**Interactions**:
- Tap anywhere to focus
- Pinch to zoom (optional)
- Capture button triggers photo
- Close button dismisses modal

---

## 4. Rules Generation Progress Screen

**iOS File**: `rulebook-kit/Sources/Features/Rules/RulesGenerationFeature/Presentation/Views/RulesGenerationProgressView.swift`

**Purpose**: Show AI processing progress with phases

**Layout**:
- Centered content
- Large progress indicator
- Phase message
- Progress percentage
- Cancel button at bottom

**Progress Phases**:
```
Phase 1 (0-15%):   "Processing Image..."
Phase 2 (15-40%):  "Analyzing Image..."
Phase 3 (40-70%):  "Fetching Rules..."
Phase 4 (70-90%):  "Saving Rules..."
Phase 5 (90-100%): "Complete!"
```

**Components Used**:
- `RulebookProgressIndicator` (linear bar type)
- `RulebookButton` for cancel

**States**:
- Processing (animated progress)
- Error (with retry option)
- Complete (auto-transitions to Rules screen)

**Error State**:
```
Icon: exclamationmark.triangle
Title: "SOMETHING WENT WRONG"
Description: [Error message]
Actions: "TRY AGAIN" / "CANCEL"
```

---

## 5. Rules Display Screen

**iOS File**: `rulebook-kit/Sources/Features/Rules/RulesFeature/Presentation/Views/RulesView.swift`

**Purpose**: Display identified game and generated rules

**Layout**:
- Sheet modal presentation (85% height)
- Header with game title
- Metadata badges (players, duration, difficulty)
- Confidence indicator
- Collapsible rule sections
- Save/Close actions

**Sections**:
1. **Game Overview** - Header with title and metadata
2. **Setup Instructions** - Collapsible, blue accent
3. **First Round Walkthrough** - Collapsible, yellow accent
4. **Advanced Rules (Deep Dive)** - Collapsible, purple accent
5. **Win Condition** - Always visible, green accent

**Section Content Structure**:
Each collapsible section contains:
- Numbered steps
- Clear instructions
- Expandable detail

**Components Used**:
- `RulebookHeaderBar`
- `RulebookBadge` for metadata
- `RulebookCollapsibleSection` for rule sections
- `RulebookSectionHeader`
- `RulebookButton` for actions

**Confidence Display**:
- High (green): "HIGH CONFIDENCE"
- Medium (orange): "MEDIUM CONFIDENCE"
- Low (red): "LOW CONFIDENCE - VERIFY RULES"

**Actions**:
- Save to Library (if not saved)
- Close/Done

---

## 6. Paywall Screen

**iOS File**: `rulebook-kit/Sources/Features/PurchaseFeature/Views/PaywallView.swift`

**Purpose**: In-app purchase interface for scan credits

**Layout**:
- Sheet modal presentation
- Header with close button
- Current balance display
- Feature highlights
- Product cards grid
- Restore purchases link

**Content Structure**:

### Header Section
- Title: "GET MORE SCANS"
- Current balance: "X CREDITS REMAINING"

### Features Section (RulebookFeatureCard grid)
- Instant Scanning
- Clear Instructions
- Offline Access
- Game Library

### Products Section
Three product cards in a row:
```
1 Credit  | 3 Credits | 10 Credits
$X.XX     | $X.XX     | $X.XX
          | POPULAR   | BEST VALUE
```

### Footer
- "Restore Purchases" link
- Terms of Service link

**Components Used**:
- `RulebookHeaderBar`
- `RulebookFeatureCard`
- `ProductCardView` (custom)
- `CreditBalanceView` (custom)
- `RulebookButton`

**States**:
- Loading products
- Products loaded
- Purchasing (loading indicator on selected product)
- Purchase success (dismiss with success haptic)
- Purchase error (alert)

---

## 7. Onboarding Screens (2 screens)

**iOS Files**:
- `rulebook-kit/Sources/Features/Onboarding/OnboardingFeature/Presentation/Views/OnboardingView.swift`
- `OnboardingScreen.swift`

**Purpose**: First-time user experience explaining app features

**Layout**:
- Full-screen modal
- TabView with page indicators
- Feature cards
- Navigation buttons

### Screen 1: "Scan & Learn"
```
Title: "SCAN & LEARN"
Subtitle: "Point your camera at any game box"

Feature Cards:
- Instant Recognition: "AI identifies your game in seconds"
- Clear Rules: "Get easy-to-follow instructions"

Action: "NEXT" -> Screen 2
```

### Screen 2: "Your Library"
```
Title: "BUILD YOUR LIBRARY"
Subtitle: "Save games for quick reference"

Feature Cards:
- Offline Access: "Rules available anytime, anywhere"
- Organized Collection: "All your games in one place"

Action: "GET STARTED" -> Dismiss onboarding
```

**Components Used**:
- `RulebookFeatureCard`
- `RulebookButton`
- Page indicators (dots)

**Behavior**:
- Shown only on first launch
- Swipe between screens
- Page indicator shows progress
- "GET STARTED" marks onboarding complete

---

## Screen Flow Diagram

```
App Launch
    |
    v
[First Launch?]--Yes--> Onboarding Screen 1
    |                        |
    No                       v
    |                   Onboarding Screen 2
    |                        |
    v                        v
Library Screen <-------------+
    |
    +--[Tap Game Card]--> Rules Display
    |
    +--[Camera Button]--> [Has Credits?]
    |                         |
    |                         No--> Paywall
    |                         |        |
    |                         |   [Purchase]
    |                         |        |
    |                        Yes<------+
    |                         |
    |                         v
    |                   Photo Capture
    |                         |
    |                    [Capture]
    |                         |
    |                         v
    |                   Rules Generation
    |                         |
    |                    [Complete]
    |                         |
    |                         v
    +<--[Save/Close]---- Rules Display
    |
    +--[Settings Tab]--> Settings Screen
```

---

## Android Implementation Notes

### Navigation Pattern
- Use Jetpack Navigation with Bottom Navigation
- Tab 1: Library (startDestination)
- Tab 2: Floating Action Button (not a destination)
- Tab 3: Settings

### Modal Presentations
- Full-screen modals: Use `fullScreenDialog` navigation
- Sheet modals: Use `ModalBottomSheet` or `BottomSheetDialogFragment`

### Screen Sizing
- Rules Display: 85% screen height sheet
- Paywall: Full sheet or 90% height
- Photo Capture: Full screen with system UI hidden

### Orientation
- Photo Capture: Support both orientations
- Other screens: Portrait preferred

### Safe Areas
- Photo Capture: Handle camera notch/cutout
- All screens: Respect system bars and navigation
