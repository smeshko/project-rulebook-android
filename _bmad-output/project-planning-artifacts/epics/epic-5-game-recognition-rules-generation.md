# Epic 5: Game Recognition & Rules Generation

**Goal:** Implement the AI recognition pipeline and rules generation. After this epic, users can capture a photo and receive generated rules with progress feedback and confidence handling.

---

## Story 5.1: Scan Flow Initiation & Credit Check

As a user,
I want the scan to check my credits before proceeding,
So that I don't waste time if I can't complete the scan.

**Acceptance Criteria:**

**Given** the user captures/selects a photo
**When** initiating the scan flow
**Then** credit balance is checked
**And** if credits > 0, proceed to image analysis
**And** if credits = 0, show paywall (FR38, handled in Epic 8)

**And** credit is NOT deducted until scan succeeds

**Technical Notes:**
- CameraViewModel checks credits before starting
- Navigate to paywall route if credits = 0
- Credit deduction happens only on successful rules generation

**Prerequisites:** Epic 4 (photo capture), Epic 1 (DataStore)

---

## Story 5.2: Progress Screen with Phase Indicator

As a user,
I want to see progress while my game is being analyzed,
So that I know the app is working and how long to wait.

**Acceptance Criteria:**

**Given** a scan is in progress (FR26)
**When** the progress screen is displayed
**Then** it shows 5 phases with current status:
1. Processing Image (0-15%)
2. Analyzing Image (15-40%)
3. Identifying Game (40-60%)
4. Generating Rules (60-90%)
5. Saving Rules (90-100%)

**And** current phase is highlighted with animation
**And** phase-specific message explains current action
**And** overall progress bar shows percentage
**And** cancel button allows aborting

**Technical Notes:**
- GenerationViewModel tracks phase state
- ProgressPhaseIndicator composable from design system
- Animated transitions between phases
- Cancel triggers coroutine cancellation

**Prerequisites:** Story 5.1, Epic 1 (design system)

---

## Story 5.3: Image Analysis API Integration

As a developer,
I want to call the image analysis API,
So that the game box can be identified.

**Acceptance Criteria:**

**Given** a compressed image is ready (FR12)
**When** calling the analyze endpoint
**Then** image is sent as multipart/form-data or base64
**And** response includes:
- Identified game title
- Confidence score (0.0-1.0)
- Thumbnail URL (optional)

**And** timeout is handled (30s)
**And** errors return Result.Error with user-friendly message

**Technical Notes:**
- Retrofit `@Multipart` or `@Body` with base64
- `RulebookApi.analyzeImage()` from core/network
- Map API response to `ScanResult` domain model

**Prerequisites:** Epic 1 (network client)

---

## Story 5.4: Confidence Display & Auto-Proceed Logic

As a user,
I want to see how confident the app is about the game identification,
So that I can verify it's correct before generating rules.

**Acceptance Criteria:**

**Given** the image analysis returns a result (FR13)
**When** confidence is >= 80%
**Then** auto-proceed to rules generation (no confirmation needed)
**And** briefly show identified game name

**When** confidence is < 80%
**Then** show confirmation screen with:
- Identified game name
- Confidence percentage badge
- "Is this your game?" prompt
- "Yes, continue" and "No, enter manually" buttons (FR14)

**Technical Notes:**
- ConfidenceBadge composable (color-coded: green >80%, yellow 50-80%, red <50%)
- Threshold configurable (start with 80%)
- Analytics event for confidence level

**Prerequisites:** Story 5.3

---

## Story 5.5: Manual Game Name Entry

As a user,
I want to enter the game name manually if the AI is wrong,
So that I can still get rules for obscure or misidentified games.

**Acceptance Criteria:**

**Given** the user rejects the AI suggestion or confidence is very low (FR15)
**When** manual entry is shown
**Then** a text field allows typing the game name
**And** "Generate Rules" button submits the manual name
**And** keyboard appears automatically
**And** entry supports autocomplete (optional, future)

**And** manual entry still consumes a credit

**Technical Notes:**
- TextField with brutalist styling
- CameraViewModel handles manual name submission
- Same rules generation flow after manual entry

**Prerequisites:** Story 5.4

---

## Story 5.6: Rules Generation API Integration

As a developer,
I want to call the rules generation API,
So that structured rules are created for the identified game.

**Acceptance Criteria:**

**Given** a game is identified (auto or manual) (FR18)
**When** calling the generate endpoint
**Then** request includes game name and optional thumbnail
**And** response includes structured rules:
- Overview (summary, win condition)
- Setup (step-by-step instructions)
- First Round (how to play initial turns)
- Advanced (detailed rules, edge cases)

**And** response is parsed into `Rules` domain model
**And** generation completes in <45s (allows 60s total with analysis)

**Technical Notes:**
- `RulebookApi.generateRules()` from core/network
- Parse JSON into RuleSection objects
- Handle streaming response if API supports it (future)

**Prerequisites:** Story 5.3

---

## Story 5.7: Save Rules to Database

As a user,
I want my generated rules saved automatically,
So that I can access them later without re-scanning.

**Acceptance Criteria:**

**Given** rules are successfully generated
**When** saving to database
**Then** a new `Game` record is created with:
- Generated UUID
- Game title
- Thumbnail URL
- Created timestamp
- Last accessed timestamp

**And** associated `Rules` record is created
**And** credit is deducted (FR35)
**And** user is navigated to Rules display screen

**Technical Notes:**
- GameRepository.saveGame() with transaction
- Room insert operations
- Update DataStore credit balance
- Navigate with gameId argument

**Prerequisites:** Story 5.6, Epic 1 (Room, DataStore)

---

## Story 5.8: Fallback AI Model Handling

As a user,
I want the app to try alternative AI models for obscure games,
So that I can get rules even for rare or international games.

**Acceptance Criteria:**

**Given** the primary AI model fails or returns very low confidence (FR16)
**When** fallback is triggered
**Then** secondary model is attempted
**And** user sees "Trying alternative recognition..." message
**And** if fallback succeeds, continue normal flow
**And** if fallback fails, offer manual entry

**Technical Notes:**
- Backend handles model fallback (may be transparent to client)
- Client may need to retry with different endpoint/flag
- Track fallback usage in analytics

**Prerequisites:** Story 5.3

---

## Story 5.9: Retry Failed Recognition

As a user,
I want to retry if recognition fails,
So that I can try a better photo without starting over.

**Acceptance Criteria:**

**Given** recognition fails (network error, AI error) (FR17)
**When** error screen is displayed
**Then** it shows:
- Friendly error message (not technical)
- "Try Again" button (returns to camera)
- "Enter Manually" button (go to manual entry)

**And** original photo can be retried or new photo taken
**And** no credit is consumed on failure

**Technical Notes:**
- Error state in GenerationViewModel
- Map specific errors to user-friendly messages
- Preserve compressed image for retry option

**Prerequisites:** Story 5.2

---

## Story 5.10: Scan Analytics Events

As a product owner,
I want to track scan funnel metrics,
So that I can understand conversion and failure points.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** scan events occur
**Then** the following events are tracked:
- `scan_started` - Photo captured/selected
- `scan_analysis_complete` - With confidence level
- `scan_confirmed` - User confirmed game (if prompted)
- `scan_manual_entry` - User entered name manually
- `scan_generation_complete` - Rules generated successfully
- `scan_failed` - With error type
- `scan_cancelled` - User cancelled

**Technical Notes:**
- Events fired from GenerationViewModel
- Include relevant properties (confidence, error_type, duration)
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 5 Complete: Game Recognition & Rules Generation**

**Stories Created:** 10
**FR Coverage:** FR12-18, FR26, FR35
**Architecture Sections Referenced:** feature/rules, core/network, core/database
**UX Patterns Incorporated:** Progress phases, confidence display, error recovery

---
