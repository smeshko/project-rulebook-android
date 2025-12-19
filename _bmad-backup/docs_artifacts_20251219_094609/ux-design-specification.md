---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
workflowComplete: true
inputDocuments:
  - path: 'docs/prd.md'
    type: 'prd'
    description: 'Complete Product Requirements Document with user journeys, functional requirements'
  - path: 'docs/analysis/product-brief-project-rulebook-android-2025-12-02.md'
    type: 'product-brief'
    description: 'Product Brief with vision, users, metrics, and MVP scope'
  - path: 'docs/ios/android-migration-product-overview.md'
    type: 'ios-reference'
    description: 'Complete iOS product specification for Android migration'
  - path: 'docs/ios/android-migration-screens.md'
    type: 'ios-reference'
    description: 'Screen inventory and layout specifications'
  - path: 'docs/ios/android-migration-components.md'
    type: 'ios-reference'
    description: 'Component library specifications for Jetpack Compose'
  - path: 'docs/ios/android-migration-navigation.md'
    type: 'ios-reference'
    description: 'Navigation patterns and user flows'
  - path: 'docs/ios/android-migration-prd.md'
    type: 'ios-reference'
    description: 'iOS migration-specific PRD'
  - path: 'docs/ios/design-system-android.json'
    type: 'design-tokens'
    description: 'Design system tokens (colors, typography, spacing) exported for Android'
  - path: 'docs/ios/screenshots/'
    type: 'visual-reference'
    description: '17 iOS screenshots covering all screens and states'
workflowType: 'ux-design'
workflowContext: 'ios-to-android-migration'
lastStep: 1
project_name: 'project-rulebook-android'
user_name: 'Ivo'
date: '2025-12-03'
---

# UX Design Specification - project-rulebook-android

**Author:** Ivo
**Date:** 2025-12-03

---

<!-- UX design content will be appended sequentially through collaborative workflow steps -->

## Project Context

This UX design specification documents the **Android adaptation** of an established iOS application. The design language ("Brutalist") and core user experience have been proven on iOS - this document focuses on:

1. **Platform Translation**: Adapting iOS patterns to Android conventions
2. **Design System Validation**: Ensuring brutalist aesthetics work on Android/Material
3. **Interaction Adaptations**: Android-specific gestures, navigation, and behaviors
4. **Component Mapping**: Translating SwiftUI components to Jetpack Compose equivalents

### Reference Documentation

| Document | Purpose |
|----------|---------|
| iOS Product Overview | Complete feature & architecture reference |
| iOS Screen Inventory | Layout specifications for all screens |
| iOS Component Library | Component specs with Compose examples |
| iOS Navigation Flows | User journeys and navigation patterns |
| Design System JSON | Tokenized design values for direct import |
| iOS Screenshots (17) | Visual reference for all screens and states |

---

## Executive Summary

### Project Vision

**"From box to playing in 60 seconds"** - Rulebook eliminates the friction of learning board games through AI-powered photo recognition and progressive rule generation.

This Android version is a **platform translation** of a proven iOS application, maintaining complete feature parity while adapting to Android-native conventions. The goal is to feel like a "native Android app that happens to have the Rulebook brand" rather than "an iOS app running on Android."

### Target Users

**Primary: Board Game Collectors & Hosts**
- Own 10+ games, many unplayed
- Host game nights and want to start playing immediately
- Value efficiency over comprehensive rule mastery
- Comfortable with AI-assisted tools

**Secondary: Game Night Guests**
- Don't own the games but want to participate
- Feel overwhelmed by lengthy rulebooks
- Need quick reference during gameplay
- Appreciate progressive, digestible information

**Tertiary: Club Organizers**
- Run meetups with rotating game selections
- Need to maximize gameplay time
- Support newcomers without slowing down regulars
- Often work in low-connectivity environments (libraries, cafes)

### Key Design Challenges

1. **Platform Translation Fidelity**
   - Maintain brutalist visual identity while respecting Android interaction patterns
   - Adapt iOS navigation paradigms (sheets, full-screen covers) to Android equivalents
   - Handle predictive back gesture with modal stack management

2. **Tab Bar + Floating Action Button Hybrid**
   - iOS centers the camera as a tab item
   - Android convention separates FAB from bottom navigation
   - Need elegant solution that feels native without losing discoverability

3. **Permission & Onboarding Flow**
   - Android camera permission requires explicit rationale UI
   - Balance onboarding completeness with time-to-first-scan metric

4. **Purchase Flow Adaptation**
   - Google Play Billing has different UX patterns than StoreKit
   - "Ask to Buy" family sharing has Android equivalents to support

### Design Opportunities

1. **App Shortcuts**
   - Long-press launcher icon → "Scan Game" quick action
   - Reduces friction for repeat users by 2+ taps

2. **Predictive Back Gesture**
   - Modern Android users expect preview animations
   - Opportunity to show library/settings peek during back gesture

3. **Edge-to-Edge Display**
   - Brutalist aesthetic benefits from bold full-screen presence
   - Status bar can adopt screen colors for immersive feel

4. **Share Target Integration**
   - Receive game box images directly from camera or gallery apps
   - "Open with Rulebook" creates alternative entry point

5. **Brutalist Brand Differentiation**
   - Material Design apps tend toward soft, rounded aesthetics
   - Sharp corners and bold shadows will stand out in Play Store
   - Opportunity to own a distinctive visual niche on Android

---

## Core User Experience

### Defining Experience

The core experience of Rulebook is **Photo → Rules → Play** - a single, focused flow that transforms an unknown game box into playable knowledge in under 60 seconds.

**Primary User Action:** Photograph a game box and receive digestible, progressive rules
**Core Loop:** Scan → Learn → Play → Save → Reference

This action is so central that everything else in the app exists to support it:
- Library stores results for offline reference
- Settings customize the experience
- Credits enable continued scanning
- Onboarding teaches this one action

### Platform Strategy

**Platform:** Native Android with Jetpack Compose
**Input:** Touch-first, camera-centric interaction model
**Connectivity:** Scan requires network; library fully offline
**Target Devices:** Android 14+ (API 34), phones in portrait orientation

**Android Capabilities Leveraged:**

| Capability | Usage |
|------------|-------|
| CameraX | Consistent camera experience across devices |
| Edge-to-Edge | Immersive brutalist visual presence |
| Predictive Back | Modern navigation with gesture previews |
| App Shortcuts | "Scan Game" long-press launcher action |
| DataStore | Persistent preferences and credit balance |
| Room | Offline-first rules storage |

### Effortless Interactions

**Zero-Friction Moments:**
1. **Camera Access** - FAB always visible, one tap to camera
2. **Photo Capture** - Point and tap, no adjustment needed
3. **Rule Reading** - Progressive sections, start immediately
4. **Library Access** - Instant, works offline, visual grid
5. **Credit Awareness** - Balance visible, no surprises

**Eliminated Friction:**
- No loading gates before camera
- No image editing required
- No account creation
- No tutorial before first action
- No manual game searching

### Critical Success Moments

| Moment | Success Indicator | Design Response |
|--------|------------------|-----------------|
| **First Scan** | "It works!" | Fast progress, clear feedback |
| **Game Identified** | "That's my game!" | Confidence score, easy correction |
| **Rules Understood** | "I get it" | Progressive disclosure, clear hierarchy |
| **Game Night Starts** | "Let's play!" | Setup checklist, quick reference |
| **Return Visit** | "There it is!" | Visual library, recent first |

**First-Time Success Definition:** User completes scan and reads game overview within 60 seconds of first camera tap.

### Experience Principles

1. **Speed Above All**
   - Every interaction should accelerate path to playing
   - Minimize modal interruptions and confirmation dialogs
   - Optimize transition animations for perceived speed

2. **Progressive Clarity**
   - Never overwhelm with information density
   - Reveal complexity in layers (Overview → Setup → First Round → Advanced)
   - Collapsible sections default to most useful state

3. **Confidence Through Feedback**
   - Users always know what's happening and what's next
   - Progress indicators for all async operations
   - Haptic confirmation for key actions (capture, save, purchase)

4. **Offline Resilience**
   - Saved games work everywhere (basements, cabins, cafes)
   - Clear visual distinction between online/offline states
   - Graceful degradation when network unavailable

5. **Bold but Familiar**
   - Brutalist aesthetics make the app distinctive and memorable
   - Android-native interactions make the app feel at home
   - Visual brand is strong; behavioral patterns are platform-standard

---

## Desired Emotional Response

### Primary Emotional Goals

| Emotion | Moment | Description |
|---------|--------|-------------|
| **Relief** | Game identified | "I don't have to figure this out alone" - the weight of learning lifts |
| **Empowerment** | Reading rules | "I understand this. I can teach others." - knowledge creates confidence |
| **Confidence** | Hosting game night | "I've got this. Let's play!" - from anxious to assured |
| **Playfulness** | Throughout app | Bold, fun aesthetic matches the joy of board gaming |
| **Trust** | AI interactions | "This app knows what it's doing" - reliable, competent, honest |

**Advocacy-Creating Moment:** When friends ask "How did you learn that so fast?" and the user shows them the app - pride in the tool becomes word-of-mouth growth.

### Emotional Journey Mapping

| Stage | Starting Emotion | Target Emotion | Design Response |
|-------|------------------|----------------|-----------------|
| **Discovery** | Skepticism | Curiosity | Bold visuals, immediate value |
| **First Scan** | Anticipation + Anxiety | Amazement | Fast, confident feedback |
| **Rules Display** | Relief | Empowerment | Clear hierarchy, progressive disclosure |
| **Game Night** | Confidence | Pride | Quick reference, shareable |
| **Error Recovery** | Frustration | Reassurance | Clear paths, friendly tone |
| **Return Visit** | Familiarity | Comfort | Instant access, visual recognition |

### Micro-Emotions

**Positive States to Cultivate:**
- **Ready** - Camera opens instantly, no setup anxiety
- **Satisfied** - Haptic feedback on capture confirms action
- **Engaged** - Progress animation maintains interest
- **Validated** - Game identification feels impressive
- **Informed** - Low confidence handled as user empowerment, not failure
- **Curious** - Section expansion invites exploration

**Negative States to Prevent:**
- **Anxious** - About "getting the photo right"
- **Uncertain** - "Did anything happen?"
- **Bored** - During processing wait
- **Skeptical** - About AI accuracy
- **Overwhelmed** - By information density
- **Trapped** - By paywall or credit limits

### Design Implications

| Emotional Goal | UX Design Approach |
|----------------|-------------------|
| **Relief** | Immediate feedback; no dead-ends; always a clear next step |
| **Empowerment** | Progressive disclosure; toggleable checklists; visible progress |
| **Confidence** | Explained confidence scores; easy manual correction; forgiving UI |
| **Playfulness** | Brutalist aesthetics; bold color; personality in microcopy |
| **Trust** | Consistent behavior; transparent credit system; no dark patterns |

### Emotional Design Principles

1. **Progress Over Perfection**
   - Show incremental progress, never leave users wondering
   - Animated states during AI processing maintain engagement
   - Even errors show forward momentum ("Try again" not "Failed")

2. **Confidence is Contagious**
   - App's bold aesthetic projects confidence onto users
   - Clear, decisive UI language ("SCAN YOUR FIRST GAME" not "Maybe try scanning?")
   - Haptic feedback confirms: "Yes, that worked"

3. **Friction = Anxiety**
   - Every tap that doesn't advance the user creates stress
   - Eliminate confirmation dialogs except for destructive actions
   - Credit balance visible to prevent surprise anxiety

4. **Delight in Details**
   - Chunky brutalist shadows add personality
   - Color-coded sections create visual recognition
   - Micro-animations celebrate success moments

5. **Recovery = Reassurance**
   - Errors are opportunities to demonstrate competence
   - "Not your game? Let's find the right one" - collaborative tone
   - Never blame the user ("Invalid image" → "Let's try another angle")

---

## UX Pattern Analysis & Inspiration

### Inspiring Products Analysis

#### Google Lens - Camera Recognition Excellence
- Instant visual feedback during camera operation
- Smooth transition from capture to results
- Confidence indicators for recognition accuracy
- **Key Learning:** Camera preview should feel alive, not like a static viewfinder

#### Duolingo - Playful Progress
- Progressive complexity disclosure
- Celebratory micro-interactions
- Bold, colorful visual design
- Clear resource/credit systems
- **Key Learning:** Bold design can be functional, not just decorative

#### Shazam - Recognition Magic
- One-tap initiation
- Engaging "processing" animation
- Library of past results
- Instant delight on identification
- **Key Learning:** The recognition moment should feel like magic

#### iOS Rulebook - Direct Reference
- Brutalist aesthetic with personality
- Progressive rule sections
- Credit system transparency
- **Key Learning:** Proven design, adapt interactions not aesthetics

### Transferable UX Patterns

**Navigation:**
- Bottom Navigation + FAB (Material standard for Library/Settings + Camera action)
- Sheet modals with proper Android drag behavior
- Predictive back gesture support

**Interaction:**
- Tap-anywhere camera focus
- Pull-to-refresh for library
- Long-press context menus for game actions
- Swipe-to-dismiss for all sheets

**Visual:**
- Card-based grid for game library
- Color-coded rule sections
- Phase-based progress indication
- Bold, characterful empty states

**Feedback:**
- Haptic confirmation on capture
- Animated success celebrations
- Skeleton loading for async content

### Anti-Patterns to Avoid

| Avoid | Do Instead |
|-------|------------|
| Splash screen loading delays | Android 12 SplashScreen (instant) |
| Permission requests on launch | Request at point of use |
| iOS-style back button | System back + gesture nav |
| Non-dismissible modals | Always allow swipe-dismiss |
| Unnecessary confirmation dialogs | Only confirm destructive actions |
| Hidden credit balance | Always visible |
| Centered tab bar action | Separate FAB |
| Custom/hidden gestures | Standard Android patterns |

### Design Inspiration Strategy

**Adopt (unchanged):**
- Brutalist visual system (brand consistency)
- Progressive rule disclosure (validated IA)
- Color-coded sections (recognition)
- Empty state personality (engagement)

**Adapt (platform-specific):**
- Tab camera → FAB (Android convention)
- iOS sheets → ModalBottomSheet (native behavior)
- Full-screen covers → Dialog full-screen (predictive back)
- Navigation stack → Compose Navigation (lifecycle)
- UIKit haptics → VibrationEffect (platform API)

**Avoid (conflicts with goals):**
- iOS-specific gestures
- Tab bar as navigation replacement
- SwiftUI animation curves
- iOS permission patterns

---

## Design System Foundation

### Design System Choice

**Approach:** Themed Material 3 with Rulebook Custom Layer

The Rulebook Android app uses a **three-layer design system**:

1. **Material 3 Foundation** - Provides accessibility, Android conventions, Compose integration
2. **Rulebook Theme** - Custom colors, typography, shapes from iOS design tokens
3. **Rulebook Components** - Custom composables implementing brutalist aesthetic

This approach gives us the visual uniqueness of a custom system with the reliability of Material's proven patterns.

### Rationale for Selection

| Factor | Decision Driver |
|--------|-----------------|
| **Brand Consistency** | Must match iOS brutalist aesthetic exactly |
| **Design Tokens Available** | `design-system-android.json` ready for import |
| **Platform Expectations** | Android users expect certain behaviors (ripples, back gesture) |
| **Accessibility Requirements** | Material 3 provides WCAG compliance foundations |
| **Development Speed** | Themed M3 faster than fully custom |
| **Future Flexibility** | Material You support available when desired |

### Implementation Approach

**Layer 1: Material 3 Foundation**
- Use `MaterialTheme` as base wrapper
- Keep accessibility features (touch targets, contrast)
- Keep Android-native behaviors (ripple, motion)
- Keep edge-to-edge and system UI handling

**Layer 2: Rulebook Theme**
```kotlin
@Composable
fun RulebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) rulebookDarkColors else rulebookLightColors,
        typography = rulebookTypography,
        shapes = rulebookShapes, // Sharp corners
        content = content
    )
}
```

**Layer 3: Rulebook Components**
- `RulebookButton` - Brutalist styling with offset shadow
- `RulebookCard` - Thick borders, pattern backgrounds
- `RulebookHeaderBar` - Bold title, bottom border
- `RulebookCollapsibleSection` - Color-coded, animated
- `RulebookProgressIndicator` - Phased progress with animation
- (Full component library defined in iOS documentation)

### Customization Strategy

**Override (Brutalist Identity):**

| Material Default | Rulebook Style |
|------------------|----------------|
| Rounded shapes (12dp) | Sharp corners (0dp) |
| Elevation shadows | Offset rectangle shadows |
| Muted colors | Saturated accent palette |
| Light font weights | Black weight (900) for titles |

**Preserve (Android Conventions):**

| Material Behavior | Reason |
|-------------------|--------|
| Touch ripple | User expectation |
| Accessibility | TalkBack, scaling |
| Predictive back | Modern Android |
| Edge-to-edge | System integration |
| Standard motion | Familiar feel |

### Design Token Architecture

Tokens imported from `design-system-android.json`:

| Category | Source | Compose Implementation |
|----------|--------|----------------------|
| Colors | `colors.*` | `Color` objects in theme |
| Typography | `typography.*` | `TextStyle` definitions |
| Spacing | `spacing.*` | `Dp` constants |
| Borders | `spacing.brutalist.*` | Border width constants |
| Shadows | `shadows.offsets.*` | Custom shadow modifier |
| Animation | `animation.*` | Duration/easing specs |

---

## Detailed Experience Design

### Defining Experience

**"Point at game box → know how to play"**

The Rulebook defining experience is the moment when a user photographs an unknown game box and, within 60 seconds, understands enough to start playing. This is "Shazam for board games" - instant recognition and comprehension.

**How users describe it:** "I just took a photo and it told me how to play. We were playing in a minute."

### User Mental Model

**Current approaches and pain points:**

| Approach | Time | Pain Point |
|----------|------|------------|
| Read rulebook | 20-60 min | Intimidating, boring |
| YouTube tutorial | 15-30 min | Have to find good one |
| Ask game owner | Variable | They might not know |
| BoardGameGeek | 10-20 min | Text-heavy, hard to parse |

**Mental model users bring:**
- "I want to play, not study"
- "I'll learn advanced rules as I go"
- "Just tell me enough to start"

**Expectation:** Like Google Translate for a menu - point, tap, understand.

### Success Criteria

| Criterion | Target | Indicator |
|-----------|--------|-----------|
| **Speed** | <60 seconds | From FAB tap to reading rules |
| **Accuracy** | >85% correct | Game identification |
| **Comprehension** | User can teach | Explains to others |
| **Confidence** | "I get it" | Ready to start playing |

**The "I Get It" Moment:** When the user reads Overview and Win Condition and thinks: "Oh, that's what this game is about. I can do this."

### Pattern Innovation

**Established patterns used:**
- Camera viewfinder (every camera app)
- Progress indicators (universal)
- Expandable sections (settings, FAQs)
- Card grid (photos, Pinterest)
- Bottom sheet (Android standard)

**Novel combination:**
- Photo → AI-interpreted structured content
- Progressive rule format (not full rulebook)
- Confidence-based flow (auto vs. confirmation)
- Interactive setup checklist

**No user education needed:** Users know "point camera at thing" from QR codes, Lens, Shazam.

### Experience Mechanics

#### Initiation
- **Trigger:** FAB tap (always visible on main screens)
- **Gate:** Credit check (paywall if credits = 0)
- **Transition:** Slide up to full-screen camera

#### Interaction
- **Camera:** Live preview with flash/gallery controls
- **Capture:** Single tap, haptic confirmation
- **Flexibility:** Gallery picker as alternative

#### Feedback
- **Progress phases:**
  1. Processing Image (0-15%)
  2. Analyzing Image (15-40%)
  3. Fetching Rules (40-70%)
  4. Saving Rules (70-90%)
  5. Complete (100%)
- **Messaging:** Phase-specific copy explains current action
- **Control:** Cancel available throughout

#### Completion
- **Reveal:** Game title, metadata badges, confidence indicator
- **Layout:** Overview expanded, other sections collapsed
- **Actions:** Read sections, save to library, share

---

## Visual Design Foundation

### Color System

**Theme Architecture:** Light and Dark modes with full token parity

#### Surface Palette

| Role | Light | Dark |
|------|-------|------|
| Primary (cards, buttons) | `#FFFFFF` | `#1C1C1E` |
| Secondary (backgrounds) | `#FFF9F0` | `#2C2C2E` |
| Tertiary (nested) | `#F5E6D3` | `#3A3A3C` |

#### Content Palette

| Role | Light | Dark |
|------|-------|------|
| Primary (text, icons) | `#000000` | `#FFFFFF` |
| Secondary (70% opacity) | `#000000B2` | `#FFFFFFB2` |
| Tertiary (40% opacity) | `#00000066` | `#FFFFFF66` |

#### Brutalist Accent Palette

| Color | Light | Dark | Usage |
|-------|-------|------|-------|
| Orange | `#FF6B35` | `#FF8C5F` | Game Overview, warnings |
| Blue | `#3498DB` | `#5DADE2` | Setup, info |
| Yellow | `#FFD23F` | `#FFE066` | First Round |
| Purple | `#7209B7` | `#9D4EDD` | Advanced Rules |
| Pink | `#E91E63` | `#F06292` | Actions, buttons |
| Green | `#2ECC71` | `#58D68D` | Success, checkboxes |
| Red | `#E74C3C` | `#EC7063` | Errors, destructive |

### Typography System

**Font:** System default (Roboto on Android)

| Category | Style | Size | Weight |
|----------|-------|------|--------|
| **Display** | Large Title | 34sp | Bold |
| | Title | 28sp | Bold |
| | Title 2 | 22sp | SemiBold |
| **Brutalist** | Title | 24sp | Black (900) |
| | Section Title | 16sp | Black |
| | Button Text | 14sp | Black |
| **Body** | Body | 17sp | Regular |
| | Callout | 16sp | Regular |
| **Detail** | Caption | 12sp | Regular |

**Key Principle:** Black weight (900) for brutalist emphasis, regular weight for readable content.

### Spacing & Layout Foundation

#### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4dp | Micro gaps |
| sm | 8dp | List spacing, tight padding |
| md | 16dp | Standard padding |
| lg | 24dp | Section spacing |
| xl | 32dp | Large sections |

#### Brutalist Specifications

| Element | Value |
|---------|-------|
| Border width | 3dp standard, 4dp thick |
| Shadow offset | 4dp (normal) to 12dp (elevated) |
| Corner radius | 0dp (sharp corners) |
| Button padding | 20dp horizontal |

#### Layout Grid

- Screen margin: 16dp
- Card padding: 16dp
- Grid spacing: 16dp (2-column library)
- Tab bar height: 60dp + safe area

### Accessibility Considerations

| Requirement | Status |
|-------------|--------|
| Touch targets | 44-48dp minimum |
| Color contrast | WCAG AA compliant |
| Text scaling | sp units throughout |
| Dark mode | Full token support |
| Screen readers | Semantic structure |

**Brutalist Accessibility Advantage:** Thick borders and bold shadows provide natural visual separation, aiding users with visual impairments.

---

## Design Direction

### Design Direction: Brutalist

**Status:** Established and proven on iOS - translate faithfully to Android

The Rulebook design language is a distinctive **brutalist aesthetic** characterized by:
- Thick black borders (3dp)
- Sharp corners (0dp radius)
- Bold offset shadows (4-12dp black rectangles)
- High-saturation accent colors
- Black weight (900) typography
- Warm cream backgrounds

This visual identity is non-negotiable - it IS the Rulebook brand.

### Visual Reference

Primary reference: iOS screenshots in `docs/ios/screenshots/`

| Screen | Key Visual Elements |
|--------|---------------------|
| Library | 2-column card grid, thick borders, offset shadows |
| Settings | Grouped sections with colored headers |
| Camera | Dark overlay, central capture button |
| Rules | Color-coded collapsible sections |
| Paywall | Product cards with brutalist styling |
| Onboarding | Full-bleed illustrations, bold CTAs |

### Android Platform Adaptations

| Element | iOS Pattern | Android Adaptation |
|---------|-------------|-------------------|
| Main navigation | Tab bar with centered camera | Bottom Navigation + FAB |
| Modal sheets | `.sheet` modifier | `ModalBottomSheet` |
| Full-screen modals | `fullScreenCover` | Dialog with full-screen style |
| Back navigation | Swipe from edge | Predictive back gesture |
| Status bar | Light/dark automatic | Dynamic `WindowInsetsController` |
| Safe areas | SafeAreaInsets | Edge-to-edge + WindowInsets |

### Design Rationale

1. **Brand Consistency** - iOS users may also use Android; experience should feel familiar
2. **Proven Aesthetic** - User feedback on iOS validates the brutalist approach
3. **Market Differentiation** - Stand out from Material Design sameness
4. **Emotional Goals** - Bold design projects confidence, matches "playful but competent" tone

### Implementation Notes

- Import all design tokens from `design-system-android.json`
- Build Rulebook component library matching iOS specifications
- Use Material 3 for behavior, custom styling for visuals
- Test visual parity with iOS screenshots as reference

---

## User Journey Flows

### Journey 1: First Scan Success

**Entry:** FAB tap from Library/Settings
**Goal:** Photo → Rules → Ready to play in <60 seconds
**User:** Any user with credits

**Flow:**
1. TAP FAB → Credit check (paywall if 0)
2. CAMERA → Point at game box, capture
3. PROCESSING → 5-phase progress animation
4. CONFIDENCE CHECK → Auto-proceed (>80%) or confirm (<80%)
5. RULES DISPLAY → Overview expanded, sections collapsed
6. SUCCESS → User reads overview, starts playing

**Critical Moments:**
- Camera must open instantly (no loading)
- Progress must feel fast and confident
- Rules must be immediately readable

### Journey 2: Guest Quick Reference

**Entry:** Handed phone with rules open
**Goal:** Find specific rule answer, return phone
**User:** Non-owner during game

**Flow:**
1. RECEIVE PHONE → Rules already displayed
2. SCAN SECTIONS → Find relevant header
3. TAP TO EXPAND → Section animates open
4. READ ANSWER → Find the rule
5. RETURN PHONE → No navigation needed

**Critical Moments:**
- Clear section labels for quick scanning
- Large tap targets (phone may be unfamiliar)
- No accidental back navigation

### Journey 3: Error Recovery

**Entry:** Low confidence AI result
**Goal:** Get correct rules despite recognition difficulty
**User:** Any user with obscure/damaged game box

**Flow:**
1. LOW CONFIDENCE → Show tentative result with warning
2. USER CHOICE → "Is this your game?" Yes/No
3. IF NO → Manual entry field appears
4. TYPE NAME → Search/generate rules
5. RULES DISPLAY → Same success state

**Critical Moments:**
- Non-blaming language ("Is this your game?")
- Easy keyboard access for manual entry
- Clear that manual path still "counts"

### Journey 4: Multi-Game Pre-Scan

**Entry:** Organizer preparing for group
**Goal:** Scan multiple games, let group choose
**User:** Meetup organizer, game cafe worker

**Flow:**
1. SCAN GAME 1 → Rules saved to library
2. BACK TO LIBRARY → See saved game
3. REPEAT → Scan games 2, 3, etc.
4. GROUP ARRIVES → Show library options
5. GROUP PICKS → Tap card, read rules together

**Critical Moments:**
- Quick return to library after save
- Visual indicator for newly scanned
- Offline access for all saved games

### Journey Patterns

**Navigation:**
- FAB → Full-screen camera (slide up)
- Back → Library (auto or manual)
- Section tap → Expand/collapse (animated)
- Sheet swipe → Dismiss

**Decision Points:**
- Confidence gate (<80% → confirm)
- Credit gate (0 → paywall)
- Destructive gate (delete → confirm)

**Feedback:**
- Phase progress (visual + message)
- Capture confirmation (haptic)
- Save confirmation (inline/toast)

### Flow Optimization Principles

1. **Minimize taps to value** - 3 taps max: FAB → Capture → Read
2. **No dead ends** - Every screen has forward path
3. **Progressive commitment** - Credit spent only on success
4. **Graceful degradation** - Manual entry always available
5. **Context preservation** - Return to library, not home

---

## Component Strategy

### Design System Components

**Material 3 with Rulebook Theming:**

| Component | M3 Base | Customization Level |
|-----------|---------|---------------------|
| Buttons | `Button` | Heavy (brutalist border/shadow) |
| Cards | `Card` | Heavy (sharp corners, offset shadow) |
| Navigation | `NavigationBar` | Moderate (custom styling) |
| FAB | `FloatingActionButton` | Light (camera icon) |
| Sheets | `ModalBottomSheet` | Moderate (brutalist header) |
| Text Fields | `TextField` | Moderate (thick border) |
| Toggles | `Switch` | Moderate (custom track color) |
| Progress | `LinearProgressIndicator` | Heavy (segmented phases) |

### Custom Components

#### RulebookHeaderBar
Screen title bar with thick bottom border. Supports back navigation and action buttons.

#### CollapsibleSection
Expandable content sections with color-coded headers. Used for rule categories (Overview, Setup, First Round, Advanced). Animates shadow depth on expand.

#### GameCard
Library grid item displaying game thumbnail, title, and metadata. Supports tap, long-press, and selection states.

#### ProgressPhaseIndicator
Full-screen progress display for scan flow. Shows 5 phases with animated progress, phase messaging, and cancel option.

#### ProductCard
Paywall product offering with title, description, and price. Supports popular highlighting and purchase state feedback.

#### BrutalistShadow
Modifier extension applying offset rectangle shadow. Configurable offset (4-12dp) and color.

### Implementation Strategy

**Approach:**
1. Build design tokens first (RulebookColors, RulebookTypography, RulebookSpacing)
2. Create Modifier extensions (brutalistShadow, brutalistBorder)
3. Build atomic components (buttons, cards)
4. Compose screen-level layouts
5. Test against iOS screenshots for parity

**Component Ownership:**
- All components live in `:core:designsystem` module
- Screens compose from design system components only
- No raw Material components in feature modules

### Implementation Roadmap

**Phase 1 - Foundation:**
- [ ] RulebookTheme wrapper
- [ ] Design tokens (colors, typography, spacing)
- [ ] BrutalistShadow modifier
- [ ] RulebookButton component

**Phase 2 - Core Components:**
- [ ] RulebookHeaderBar
- [ ] GameCard
- [ ] CollapsibleSection
- [ ] CreditsDisplay

**Phase 3 - Flow Components:**
- [ ] ProgressPhaseIndicator
- [ ] ConfidenceBadge
- [ ] Camera overlay components

**Phase 4 - Monetization:**
- [ ] ProductCard
- [ ] Paywall layout
- [ ] Purchase feedback states

---

## UX Consistency Patterns

### Button Hierarchy

| Type | Usage | Style |
|------|-------|-------|
| **Primary** | Main CTA (Scan, Purchase) | Filled pink, border, shadow |
| **Secondary** | Cancel, alternative actions | Outlined, no fill |
| **Destructive** | Delete, clear data | Filled red, requires confirmation |
| **Text** | Tertiary actions, links | Text only, underline on press |

**Rule:** Maximum one primary button per screen/modal.

### Feedback Patterns

**Loading:**
- Skeleton shimmer for content loading
- Progress indicator with phase messaging for actions
- No spinners (use determinate progress)

**Success:**
- Toast notifications (2s auto-dismiss)
- Green checkmark for confirmations
- Haptic feedback for key completions

**Error:**
- Dialog with clear message and recovery action
- Red color coding
- Always provide retry or dismiss option

**Warning:**
- Yellow/orange color coding
- Non-blocking (badge or inline)
- Clear consequence messaging

### Modal Patterns

| Type | Usage | Dismiss |
|------|-------|---------|
| **Bottom Sheet** | Paywall, options | Swipe down, tap scrim |
| **Full-Screen** | Camera, progress, rules | Back gesture, close button |
| **Dialog** | Confirmations only | Button tap only |

**Rule:** No confirmation dialogs for non-destructive actions.

### Empty & Loading States

**Empty States:**
- Encouraging tone ("No games yet" not "Nothing here")
- Clear CTA to resolve empty state
- Relevant illustration or icon

**Loading States:**
- Skeleton shapes matching expected content
- Shimmer animation (not spinner)
- No loading gates before camera

### Navigation Patterns

**Tab Navigation:**
- Instant switch, state preserved
- FAB visible on all main tabs
- No animation between tabs

**Back Navigation:**
- Predictive back with preview
- Optional toolbar back button
- Clear destination (previous screen or library)

**Modal Dismiss:**
- Sheets: Swipe or tap scrim
- Full-screen: Back or close button
- Confirm only if data would be lost

### Haptic Patterns

| Action | Feedback |
|--------|----------|
| Photo capture | Medium click |
| Button press | Light click |
| Scan complete | Success vibration |
| Error | Double tap |
| Toggle | Light click |

**Rule:** Haptics respect system setting (can be disabled in Settings).

---

## Responsive Design & Accessibility

### Responsive Strategy

**MVP Scope:** Phone-only, portrait orientation

| Parameter | Value |
|-----------|-------|
| Target devices | Android phones, 360-430dp width |
| Orientation | Portrait only |
| Window class | Compact only |
| Tablet/Foldable | Out of scope (v1.2) |

### Accessibility (MVP)

**Approach:** Rely on Material 3 defaults, no custom accessibility work

**What we get for free:**
- 48dp touch targets (M3 default)
- Reasonable color contrast (design tokens verified)
- Basic TalkBack from Compose semantics
- Text scaling via sp units

**Deferred to post-MVP:**
- Custom TalkBack descriptions
- Accessibility testing
- Reduced motion support
- Screen reader optimization

**Note:** No accessibility blockers created - can enhance later without refactoring.

---
