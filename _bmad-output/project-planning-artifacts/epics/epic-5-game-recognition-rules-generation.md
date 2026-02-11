# Epic 5: Game Recognition & Rules Generation

**Goal:** Implement the AI recognition pipeline and rules generation. After this epic, users can capture a photo and receive generated rules with progress feedback and confidence handling.

**FRs covered:** FR12-18, FR26, FR35, FR38

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

**Architecture requirements:**
- Located in `feature/camera` module — `CameraViewModel` orchestrates the credit gate before scan
- Credit balance read from `core/datastore` via `RulebookPreferences.creditBalance: Flow<Int>` injected through Koin `get()`
- Navigation to paywall route via `NavController.navigate(Route.Purchase.route)` — feature modules MUST NOT import from each other (Architecture §Module Dependencies)
- Credit gate is a UI-layer concern — the ViewModel checks balance and emits a navigation event via `Channel` for one-time side effects
- No network calls at this stage; purely local DataStore read

**UX/Component specifications:**
- Credit check must be instant (DataStore cached in memory) — no loading indicator needed
- If credits = 0, transition to paywall uses `ModalBottomSheet` presentation (UX §Modal Patterns: Bottom Sheet for paywall)
- No confirmation dialog for proceeding — tapping capture/select immediately checks and routes (UX §Experience Principles: "Minimize modal interruptions")
- Credit balance already visible on camera screen (Story 4.8) using `CreditsDisplay` composable — user has passive awareness

**Technical notes:**
- CameraViewModel checks `creditBalance.first()` before starting scan coroutine
- Navigate to paywall route if credits = 0 via sealed event: `CameraEvent.NavigateToPaywall`
- Credit deduction happens only on successful rules generation (Story 5.7)
- Compressed image `ByteArray` from Story 4.7 is held in ViewModel state for the scan flow

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

**Architecture requirements:**
- Located in `feature/rules` module — `GenerationScreen.kt` and `GenerationViewModel.kt`
- `GenerationUiState` data class tracks: `currentPhase: ScanPhase`, `progress: Float`, `phaseMessage: String`, `isLoading: Boolean`, `error: String?`
- `ScanPhase` enum in `core/model`: `PROCESSING_IMAGE`, `ANALYZING_IMAGE`, `IDENTIFYING_GAME`, `GENERATING_RULES`, `SAVING_RULES`, `COMPLETE`
- ViewModel receives compressed image bytes as navigation argument or shared state
- Cancel triggers `viewModelScope.coroutineContext.cancelChildren()` — proper coroutine cancellation with cleanup

**UX/Component specifications:**
- Full-screen presentation (no bottom nav, no FAB) — same as camera screen treatment
- Progress display uses `RulebookProgressIndicator` with `type = ProgressType.LinearBar` from `core/designsystem`
  - Bar height: 12dp (medium size), color: `brutalist.blue` (#3498DB/#5DADE2)
  - Border: 2dp solid black, shadow: 4dp offset (from component library spec)
- Phase labels use `brutalist.sectionTitle` typography (16sp, Black 900 weight)
- Phase-specific messages use `body.body` typography (17sp, Regular)
- Active phase indicator: pulsing animation using `rememberInfiniteTransition` with `animate-pulse` effect
  - Animation: `initialValue = 0.6f, targetValue = 1f, tween(600ms)` per progress indicator dot animation pattern
- Completed phases show `brutalist.green` (#2ECC71/#58D68D) checkmark icon
- Pending phases show `content.tertiary` (#00000066/#FFFFFF66) color
- Cancel button: `RulebookButton` secondary variant (outlined, no fill) positioned at bottom with `spacing.lg` (24dp) margin
- Background: `surface.secondary` (#FFF9F0/#2C2C2E) for warm/dark feel
- Phase transition animation: 300ms `EaseInOut` (from `animation.duration.medium`)
- Layout: Centered vertically, `spacing.xl` (32dp) between progress bar and phase list

**Technical notes:**
- GenerationViewModel tracks phase state via `MutableStateFlow<GenerationUiState>`
- Each API call updates phase — use `emit()` on state flow between operations
- Animated transitions between phases using `AnimatedContent` with `fadeIn + slideInVertically`
- Cancel fires `CameraEvent.ScanCancelled` and navigates back to camera
- Phase messages: "Preparing your image...", "Analyzing the game box...", "Identifying the game...", "Generating rules...", "Saving to your library..."

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

**Architecture requirements:**
- API call defined in `core/network` module — `RulebookApi.analyzeImage()` Retrofit interface
- Request/response models in `core/network`: `AnalyzeRequest` and `AnalyzeResponse` with `@Serializable` annotation
- Response mapped to `ScanResult` domain model in `core/model` via mapper function in `core/data`
- Repository pattern: `ScanRepository` in `core/data` wraps API call and returns `Result<ScanResult>`
- All network errors caught in repository layer and wrapped as `Result.Error` with user-friendly messages (Architecture §Error Handling)
- OkHttp timeout: 30s default from `RulebookApiClient` configuration (NFR16)
- API field names use `@SerialName("snake_case")` mapping (Architecture §API Conventions)
- Domain models are separate from API models — mappers in `core/data` handle conversion

**Technical notes:**
- Retrofit `@Multipart @POST("analyze")` with `@Part image: MultipartBody.Part`
- Alternative: `@POST("analyze")` with `@Body` containing base64-encoded image string
- Map API response to `ScanResult(gameTitle: String, confidence: Float, thumbnailUrl: String?)`
- Timeout produces `Result.Error("The analysis took too long. Please try again.")`
- Network error: `Result.Error("Unable to connect. Please check your internet connection.")`
- Server error: `Result.Error("Something went wrong on our end. Please try again.")`

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

**Architecture requirements:**
- Confidence gate logic in `GenerationViewModel` — evaluates `ScanResult.confidence` against threshold
- Threshold constant in `core/common`: `const val CONFIDENCE_AUTO_PROCEED_THRESHOLD = 0.80f`
- Auto-proceed emits state transition: `GenerationUiState(phase = GENERATING_RULES, gameName = result.gameTitle)`
- Low confidence emits: `GenerationUiState(showConfirmation = true, scanResult = result)`
- User choice dispatched as intent: `GenerationIntent.ConfirmGame` or `GenerationIntent.RejectGame`

**UX/Component specifications:**
- **Auto-proceed (>= 80%):** Brief toast-like overlay showing "Identified: [Game Name]" for 1.5s using `brutalist.title` (24sp, Black) typography, then auto-advances — no user action required (UX §Experience Principles: "Speed Above All")
- **Confirmation screen (< 80%):**
  - `ConfidenceBadge` composable from `core/designsystem`:
    - High (>80%): `brutalist.green` (#2ECC71/#58D68D) background, "HIGH CONFIDENCE" text
    - Medium (50-80%): `brutalist.orange` (#FF6B35/#FF8C5F) background, "MEDIUM CONFIDENCE" text
    - Low (<50%): `brutalist.red` (#E74C3C/#EC7063) background, "LOW CONFIDENCE" text
    - Badge spec: padding 8dp horizontal / 4dp vertical, border 2dp solid black, text 11sp bold uppercase, 0dp corners (from `RulebookBadge` component library)
  - Game name displayed in `display.title` typography (28sp, Bold)
  - "Is this your game?" prompt in `body.body` (17sp, Regular), `content.secondary` color
  - "Yes, continue" button: `RulebookButton` primary variant (pink fill #E91E63, 3dp border, 4dp shadow)
  - "No, enter manually" button: `RulebookButton` secondary variant (outlined, no fill)
  - Buttons stacked vertically with `spacing.sm` (8dp) gap
  - Layout: Centered content with `spacing.md` (16dp) screen margin
- Haptic feedback: Light click on button press (UX §Haptic Patterns)

**Technical notes:**
- ConfidenceBadge composable (color-coded: green >80%, yellow 50-80%, red <50%)
- Threshold configurable (start with 80%)
- Analytics events: `scan_analysis_complete` with `confidence` property
- Auto-proceed delay: `delay(1500)` to show game name before advancing

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

**Architecture requirements:**
- Manual entry is a UI state within `GenerationViewModel` — `GenerationUiState(showManualEntry = true)`
- Submitted name creates a synthetic `ScanResult(gameTitle = userInput, confidence = 1.0f, thumbnailUrl = null)` and follows the same generation pipeline
- Same `ScanRepository.generateRules()` endpoint regardless of auto or manual identification
- No additional network call for manual entry — goes straight to rules generation (Story 5.6)

**UX/Component specifications:**
- Text field: `RulebookTextField` (or Material 3 `TextField` with brutalist styling):
  - Border: 3dp solid black, 0dp corner radius
  - Background: `surface.primary` (#FFFFFF/#1C1C1E)
  - Text: `body.body` (17sp, Regular)
  - Placeholder: "Enter game name..." in `content.tertiary` color (#00000066/#FFFFFF66)
  - Padding: `spacing.md` (16dp)
- Keyboard opens automatically via `FocusRequester.requestFocus()` in `LaunchedEffect`
- "Generate Rules" button: `RulebookButton` primary variant, enabled only when text is non-empty
- Collaborative, non-blaming tone: "Not your game? Enter the name below" (UX §Emotional Design: "Recovery = Reassurance")
- Layout: TextField + button centered vertically, `spacing.lg` (24dp) between elements

**Technical notes:**
- TextField with brutalist styling from design system
- GenerationViewModel handles manual name submission via `GenerationIntent.SubmitManualName(name: String)`
- Same rules generation flow after manual entry
- Analytics event: `scan_manual_entry` with entered name

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

**Architecture requirements:**
- API call defined in `core/network` — `RulebookApi.generateRules(@Body request: GenerateRequest): GenerateResponse`
- `GenerateRequest`: `@Serializable data class(val gameName: String, val thumbnailUrl: String? = null)`
- `GenerateResponse` maps to `Rules` domain model in `core/model` via mapper in `core/data`
- `Rules` domain model structure:
  ```kotlin
  data class Rules(
      val gameId: String,
      val overview: RuleSection,     // summary + win condition
      val setup: RuleSection,        // step-by-step with items list
      val firstRound: RuleSection,   // turn structure
      val advanced: RuleSection      // edge cases
  )
  data class RuleSection(
      val title: String,
      val content: String,
      val items: List<String>? = null  // For checklist items in setup
  )
  ```
- `ScanRepository.generateRules(gameName: String, thumbnailUrl: String?): Result<Rules>` in `core/data`
- Response parsing: `@SerialName` maps API field names to Kotlin properties
- Generation timeout: 45s (stricter than default 30s — configure per-request via OkHttp `Call.timeout()`)

**Technical notes:**
- `RulebookApi.generateRules()` from core/network
- Parse JSON response into `Rules` with nested `RuleSection` objects
- Handle streaming response if API supports it (future optimization)
- Progress updates during generation: update phase from `IDENTIFYING_GAME` → `GENERATING_RULES` at API call start

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

**Architecture requirements:**
- Save operation in `core/data` — `GameRepository.saveGameWithRules(game: Game, rules: Rules): Result<String>` returning gameId
- Room `@Transaction` ensures atomic write: `GameEntity` insert + `RulesEntity` insert in single transaction
- Credit deduction via `RulebookPreferences.decrementCredits()` — happens AFTER successful Room insert
- Entity mapping: `Game` → `GameEntity` and `Rules` → `RulesEntity` via mapper functions in `core/data`
- Foreign key: `RulesEntity.gameId` references `GameEntity.id` with `CASCADE` delete (Architecture §Database Conventions)
- UUID generated via `java.util.UUID.randomUUID().toString()`
- Timestamps: `System.currentTimeMillis()` stored as `Long` (Architecture §Database Conventions)
- Navigation after save: emit `GenerationEvent.NavigateToRules(gameId)` via `Channel`

**UX/Component specifications:**
- Progress phase updates to "Saving to your library..." during save (phase 5: 90-100%)
- On completion: brief success state with `brutalist.green` checkmark animation before navigation
- Haptic feedback: Success vibration on save complete (UX §Haptic Patterns: "Scan complete → Success vibration")
- Transition to Rules screen: slide-in animation, 300ms (animation.duration.medium)

**Technical notes:**
- `GameRepository.saveGameWithRules()` uses Room `@Transaction` for atomicity
- Room insert operations for both `GameEntity` and `RulesEntity`
- Update DataStore credit balance via `RulebookPreferences.decrementCredits()`
- Navigate with gameId argument: `navController.navigate("rules/${gameId}")`
- Phase update: `GenerationUiState(phase = SAVING_RULES, progress = 0.9f)`

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

**Architecture requirements:**
- Fallback logic in `core/data` — `ScanRepository` orchestrates retry with alternate endpoint/flag
- API contract: `RulebookApi.analyzeImage(request, useFallback: Boolean = false)` or separate endpoint
- Fallback trigger: confidence < 30% OR primary model returns error
- Repository chains: primary call → check result → if fallback needed → retry with fallback flag → return combined result
- Fallback is transparent to ViewModel — `ScanRepository` handles internally and returns single `Result<ScanResult>`
- If backend handles fallback transparently, client just needs extended timeout (45s for fallback attempts)

**UX/Component specifications:**
- Phase message updates to "Trying alternative recognition..." using same `body.body` typography
- No additional UI element — reuses existing progress screen (Story 5.2)
- Progress bar may stall or slow during fallback — maintain animation to show activity
- If fallback also fails: transition to error state (Story 5.9) with option for manual entry (Story 5.5)

**Technical notes:**
- Backend may handle model fallback transparently (no client changes needed)
- If client-side: `ScanRepository` retries with `useFallback = true` parameter
- Track fallback usage in analytics: `scan_fallback_triggered` event with `primary_confidence` property
- Extended timeout for fallback: allow up to 45s total for both attempts

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

**Architecture requirements:**
- Error state in `GenerationViewModel`: `GenerationUiState(error = ErrorState(message, type))`
- `ErrorState` sealed class in `core/model`: `NetworkError`, `ServerError`, `AnalysisError`, `TimeoutError`
- "Try Again" emits `GenerationEvent.NavigateToCamera` — camera screen retains compressed image for retry
- "Enter Manually" transitions to manual entry state within same ViewModel
- No credit deduction on error — credit only consumed in Story 5.7 on successful save

**UX/Component specifications:**
- Error layout centered on screen with `surface.secondary` background
- Error icon: warning triangle icon, `brutalist.orange` (#FF6B35/#FF8C5F) color, 48dp size
- Error title: `brutalist.title` typography (24sp, Black 900) — "SOMETHING WENT WRONG"
- Error message: `body.body` (17sp, Regular), `content.secondary` color — user-friendly, non-technical:
  - Network: "Unable to connect. Check your internet and try again."
  - Timeout: "The analysis took too long. Try a clearer photo."
  - Server: "We hit a snag. Please try again in a moment."
  - Analysis: "We couldn't identify this game. Try another angle or enter the name."
- "Try Again" button: `RulebookButton` primary variant (pink fill), spacing `spacing.md` (16dp)
- "Enter Manually" button: `RulebookButton` secondary variant (outlined)
- Buttons stacked vertically with `spacing.sm` (8dp) gap
- Tone: Collaborative, non-blaming (UX §Emotional Design: "Recovery = Reassurance" — "Never blame the user")
- Haptic feedback: Double tap on error display (UX §Haptic Patterns: "Error → Double tap")

**Technical notes:**
- Error state in GenerationViewModel with `MutableStateFlow<GenerationUiState>`
- Map specific exceptions to `ErrorState` types in repository layer
- Preserve compressed image in CameraViewModel for retry option
- Analytics event: `scan_failed` with `error_type` and `error_message` properties

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

**Architecture requirements:**
- Events fired from `GenerationViewModel` — uses `AnalyticsManager` from `core/analytics` injected via Koin
- `AnalyticsManager.trackEvent(name: String, properties: Map<String, String>)` API
- Event names MUST match iOS event names exactly for cross-platform consistency (Architecture §Analytics)
- No PII in event properties — game names are OK, user identifiers are NOT

**Technical notes:**
- Events fired from GenerationViewModel at each state transition
- Properties per event:
  - `scan_started`: `source` (camera/gallery)
  - `scan_analysis_complete`: `confidence` (float as string), `game_title`
  - `scan_confirmed`: `confidence`, `game_title`
  - `scan_manual_entry`: `game_title`
  - `scan_generation_complete`: `game_title`, `duration_ms`
  - `scan_failed`: `error_type`, `phase`
  - `scan_cancelled`: `phase`
- Match iOS event names from TelemetryDeck integration

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 5 Complete: Game Recognition & Rules Generation**

**Stories Created:** 10
**FR Coverage:** FR12-18, FR26, FR35, FR38
**Architecture Sections Referenced:** feature/rules, feature/camera, core/network, core/data, core/database, core/model, core/datastore, core/analytics
**UX Patterns Incorporated:** Progress phases, confidence display, error recovery, manual fallback, haptic feedback
**Design Tokens Referenced:** brutalist.blue, brutalist.green, brutalist.orange, brutalist.red, brutalist.pink, animation.duration.medium, typography.brutalist.title, typography.brutalist.sectionTitle
