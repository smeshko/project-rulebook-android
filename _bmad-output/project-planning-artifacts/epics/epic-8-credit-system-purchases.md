# Epic 8: Credit System & Purchases

**Goal:** Implement the monetization flow with paywall and Google Play Billing. After this epic, users can purchase credit packs, restore purchases, and have credits gated appropriately.

**FRs covered:** FR34-41

---

## Story 8.1: Credit Balance Display Component

As a user,
I want to see my credit balance prominently,
So that I always know how many scans I have remaining.

**Acceptance Criteria:**

**Given** the user is in the app (FR34)
**When** viewing Library or Settings
**Then** credit balance is visible in the header or prominent location
**And** displays format: "X credits" or icon + number
**And** updates reactively when credits change

**Architecture requirements:**
- `CreditsDisplay` composable in `core/designsystem` — reusable across multiple screens
- Observes `RulebookPreferences.creditBalance: Flow<Int>` from `core/datastore`
- Placed as `trailingContent` in `RulebookHeaderBar` on Library screen and as a tappable row in Settings screen
- Tapping on Settings credit display navigates to Paywall (via navigation callback)
- Reactive: Flow from DataStore automatically updates all observers when credits change

**UX/Component specifications:**
- Display format: "[coin icon] X" using `special.creditBalance` typography (24sp, Bold, Rounded design)
- Icon: Custom coin/credit icon, `brutalist.orange` (#FF6B35/#FF8C5F) color, 20dp size
- Text color: `content.primary` (#000000/#FFFFFF)
- Low balance warning (1 credit): text color switches to `brutalist.orange` (#FF6B35/#FF8C5F) (UX §Feedback Patterns: "Warning — Yellow/orange color coding")
- Zero credits: text color `brutalist.red` (#E74C3C/#EC7063) — conveys urgency without alarm
- On Library header: compact variant — `detail.caption` (12sp) with small icon
- On Settings: full row using `RulebookListRow` with credit icon box, "X Credits Remaining" title, chevron accessory (tappable → paywall)
- Credit balance always visible to prevent surprise anxiety (UX §Experience Principles: "Friction = Anxiety")

**Technical notes:**
- `CreditsDisplay` composable with `creditCount: Int` parameter
- Observe `creditBalance` Flow from DataStore via `collectAsStateWithLifecycle()`
- Reusable across Library header, Settings row, and Camera screen (Story 4.8)

**Prerequisites:** Epic 1 (DataStore, design system)

---

## Story 8.2: Credit Consumption on Successful Scan

As a user,
I want credits deducted only when a scan succeeds,
So that I don't lose credits on failed attempts.

**Acceptance Criteria:**

**Given** a scan completes successfully (FR35)
**When** rules are saved to database
**Then** credit balance is decremented by 1
**And** if balance reaches 0, future scans show paywall
**And** credit deduction is atomic with rules save

**Architecture requirements:**
- Credit deduction in `GameRepository.saveGameWithRules()` — same method that saves to Room (Story 5.7)
- Deduction via `RulebookPreferences.decrementCredits()` — atomic DataStore update
- Deduction happens AFTER successful Room insert — if Room fails, no credit is consumed
- Logical transaction: Room write succeeds → DataStore credit decrement → navigate to rules
- If DataStore write fails (extremely rare): log error, credit may be "free" — acceptable trade-off vs. charging for failed save
- Credit check before scan in CameraViewModel (Story 5.1) prevents scanning with 0 credits

**Technical notes:**
- Transaction in `GameRepository`: Room insert → DataStore credit decrement
- DataStore `edit { prefs -> prefs[CREDIT_BALANCE] = prefs[CREDIT_BALANCE]?.minus(1) ?: 0 }`
- Credit check before scan start (Story 5.1) as first gate
- If `creditBalance` reaches 0 after deduction, next scan attempt triggers paywall

**Prerequisites:** Epic 5 (scan flow), Epic 1 (DataStore)

---

## Story 8.3: Paywall Screen Display

As a user,
I want to see purchase options when I run out of credits,
So that I can continue using the app.

**Acceptance Criteria:**

**Given** the user has 0 credits and tries to scan (FR38)
**When** the paywall is triggered
**Then** a bottom sheet or full screen shows:
- Header: "Get More Scans"
- Three product cards (1, 3, 10 credits)
- "Restore Purchases" link
- Close/dismiss option

**And** the paywall uses brutalist styling
**And** products show prices from Play Store

**Architecture requirements:**
- Located in `feature/purchase` module — `PaywallScreen.kt`, `PurchaseViewModel.kt`, `PurchaseUiState.kt`
- `PurchaseViewModel` loads product details from `BillingRepository` in `core/billing`
- `PurchaseUiState`: `products: List<ProductInfo>, currentBalance: Int, isLoading: Boolean, purchaseState: PurchaseState?, error: String?`
- `PurchaseState` sealed class: `Idle`, `Processing(sku: String)`, `Success(creditsAdded: Int)`, `Error(message: String)`, `Pending`
- Paywall presented as `ModalBottomSheet` — dismissible via swipe-down or tap-scrim (UX §Modal Patterns: "Bottom Sheet for paywall")
- Alternative: Full-screen dialog for first-time paywall trigger — TBD based on conversion testing

**UX/Component specifications:**
- Presentation: `ModalBottomSheet` with 90% screen height (per migration screens: "Paywall: Full sheet or 90% height")
  - Sheet shape: 0dp corner radius (brutalist), or small top radius for sheet affordance
  - Scrim: Default Material 3 scrim
  - Swipe-to-dismiss enabled (UX §Modal Patterns: "Sheets: Swipe down, tap scrim")
- Header section:
  - Close button (X): `RulebookIconButton.close()` top-right
  - Title: "GET MORE SCANS" in `brutalist.largeTitle` (32sp, Black 900)
  - Current balance: "X CREDITS REMAINING" in `special.creditBalance` (24sp, Bold), `content.secondary` color
- Feature highlights: Row of 4 `RulebookFeatureCard` composables (horizontal scroll):
  - "Instant Scanning" with camera icon, `brutalist.orange`
  - "Clear Instructions" with book icon, `brutalist.blue`
  - "Offline Access" with wifi-off icon, `brutalist.green`
  - "Game Library" with grid icon, `brutalist.purple`
  - Card spec: 120dp min height, border 2dp black, shadow 4dp, icon 32dp
- Products section: 3 `ProductCard` composables in horizontal row (Story 8.5)
- Footer: "Restore Purchases" as text button in `body.callout` (16sp, Regular), underlined
- Background: `surface.secondary` (#FFF9F0/#2C2C2E)
- Content padding: `spacing.brutalist.paywallContentPadding` (20dp)
- Section spacing: `spacing.lg` (24dp) between header/features/products/footer

**Technical notes:**
- `PaywallScreen` in `feature/purchase` module
- `ModalBottomSheet` or full-screen dialog
- BillingClient queries for product details on ViewModel init
- Loading state: skeleton shimmer for product cards while prices load

**Prerequisites:** Epic 1 (design system), Story 8.4

---

## Story 8.4: Google Play Billing Integration

As a developer,
I want Play Billing Library integrated,
So that users can make in-app purchases.

**Acceptance Criteria:**

**Given** the `core/billing` module
**When** BillingClient is configured
**Then** it connects to Play Store on app start
**And** queries ProductDetails for 3 SKUs:
- `credits_1` - 1 credit pack
- `credits_3` - 3 credit pack
- `credits_10` - 10 credit pack

**And** handles connection lifecycle (connect/disconnect)
**And** retries on transient failures

**Architecture requirements:**
- Located in `core/billing` module — `BillingRepository.kt`, `BillingClientWrapper.kt`
- `BillingRepository` interface in `core/data`:
  ```kotlin
  interface BillingRepository {
      val products: Flow<List<ProductInfo>>
      suspend fun queryProducts(): Result<List<ProductInfo>>
      suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit>
      suspend fun consumePurchase(purchaseToken: String): Result<Unit>
      suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>>
  }
  ```
- `ProductInfo` domain model in `core/model`: `productId: String, title: String, price: String, credits: Int`
- `PurchaseInfo` domain model: `purchaseToken: String, productId: String, purchaseState: Int`
- BillingClient lifecycle tied to Application — connect on app start, disconnect in `onTerminate()`
- Retry on `BillingResponseCode.SERVICE_DISCONNECTED` — reconnect and retry once
- Koin provides `BillingRepository` as singleton

**Technical notes:**
- Play Billing Library 7.x (Architecture §Technology Stack)
- `BillingClient.newBuilder(context).setListener(purchasesUpdatedListener).enablePendingPurchases().build()`
- `ProductType.INAPP` for consumable credit packs
- Query products: `BillingClient.queryProductDetailsAsync()`
- SKU mapping: `credits_1` → 1 credit, `credits_3` → 3 credits, `credits_10` → 10 credits
- Connection retry with exponential backoff (max 3 attempts)

**Prerequisites:** Epic 1 (module structure)

---

## Story 8.5: Product Cards with Pricing

As a user,
I want to see credit pack options with clear pricing,
So that I can choose the best value.

**Acceptance Criteria:**

**Given** the paywall is displayed (FR36)
**When** product cards are shown
**Then** each card displays:
- Credit amount (1, 3, or 10)
- Price from Play Store (localized)
- "Most Popular" badge on 3-pack (optional)

**And** cards use brutalist styling (ProductCard component)
**And** cards are tappable to initiate purchase

**Architecture requirements:**
- `ProductCard` composable in `feature/purchase/components/` — feature-specific, not in design system
- Receives `ProductInfo` from `PurchaseViewModel` which holds `BillingRepository.products` Flow
- Price always from Play Store `ProductDetails.oneTimePurchaseOfferDetails.formattedPrice` — never hardcoded
- Card tap dispatches: `PurchaseIntent.SelectProduct(productId: String)`

**UX/Component specifications:**
- Product card layout:
  - Width: Equal third of available space minus padding
  - Min height: 160dp
  - Border: 3dp solid black, shadow: 6dp offset (`shadows.offsets.subtle`)
  - Corner radius: 0dp (brutalist)
  - Background: `surface.primary` (#FFFFFF/#1C1C1E)
  - Content vertical stack:
    - Credit count: `display.title` (28sp, Bold) — "1", "3", "10"
    - Label: `detail.caption` (12sp, Regular) — "credit" / "credits"
    - Divider: 2dp black line
    - Price: `special.priceDisplay` (28sp, Bold, Rounded) — "$X.XX" from Play Store
  - Padding: `spacing.md` (16dp)
- "Most Popular" badge on 3-pack:
  - `RulebookBadge` with `brutalist.pink` (#E91E63/#F06292) background
  - Positioned top-right, rotated -5° for visual interest
  - Text: "POPULAR" in 11sp bold uppercase
- "Best Value" badge on 10-pack (optional):
  - `RulebookBadge` with `brutalist.green` (#2ECC71/#58D68D) background
  - Text: "BEST VALUE"
- Card press animation: `pressedScale = 0.98f`, shadow 6dp → 3dp (from `pressAnimation` utility)
- Cards row: `Row` with `spacing.sm` (8dp) horizontal arrangement, `Modifier.weight(1f)` per card
- Psychological anchoring: Middle option (3-pack) slightly elevated with 8dp shadow vs. 6dp for others

**Technical notes:**
- `ProductCard` composable — feature-specific
- Price from `ProductDetails.oneTimePurchaseOfferDetails.formattedPrice`
- Highlight middle option for psychological anchoring
- Loading state: Skeleton shimmer with `brutalist.gray` (#95A5A6/#B2BEC3) while prices load

**Prerequisites:** Story 8.4, Story 8.3

---

## Story 8.6: Purchase Flow Execution

As a user,
I want to complete a purchase through Google Play,
So that I receive credits after payment.

**Acceptance Criteria:**

**Given** the user taps a product card (FR37)
**When** initiating purchase
**Then** Google Play purchase sheet appears
**And** user completes payment flow
**And** on success, credits are added to balance
**And** paywall dismisses automatically
**And** success feedback shown (toast or animation)

**Architecture requirements:**
- Purchase launched via `BillingRepository.launchPurchaseFlow(activity, productId)`
- `BillingClient.launchBillingFlow()` requires `Activity` reference — passed from composable via `LocalContext.current as Activity`
- `PurchasesUpdatedListener` callback in `BillingClientWrapper` handles result
- Result forwarded to `PurchaseViewModel` via `BillingRepository` Flow or callback
- On success: verify purchase (Story 8.7) → consume → deliver credits → dismiss paywall
- Purchase state updates: `PurchaseUiState(purchaseState = PurchaseState.Processing(sku))` → `Success` or `Error`

**UX/Component specifications:**
- Initiating purchase: Selected card shows loading indicator — `RulebookProgressIndicator` `AnimatedDots` type overlaid on card
- Other cards disabled during purchase (reduced opacity 40%)
- Google Play sheet is system UI — no customization possible
- On success:
  - Haptic feedback: Success vibration (UX §Haptic Patterns: "Scan complete → Success vibration" — same pattern for purchase)
  - Brief success animation: green checkmark with `brutalist.green` color, 600ms
  - Credits count animates up (e.g., "3 → 6" using `AnimatedContent`)
  - Paywall auto-dismisses after 1.5s delay
- On error:
  - `AlertDialog` with error message: "Purchase could not be completed. Please try again."
  - Haptic: Double tap (UX §Haptic Patterns: "Error → Double tap")
  - User can retry or dismiss

**Technical notes:**
- `BillingClient.launchBillingFlow()` with `BillingFlowParams`
- Handle `PurchasesUpdatedListener` callback for result
- `BillingResponseCode.OK` → proceed to verification
- `BillingResponseCode.USER_CANCELED` → reset state silently (no error)
- Other codes → show error dialog

**Prerequisites:** Story 8.5

---

## Story 8.7: Purchase Verification & Credit Delivery

As a developer,
I want purchases verified before delivering credits,
So that fraudulent purchases don't grant credits.

**Acceptance Criteria:**

**Given** a purchase completes (FR41)
**When** verifying the purchase
**Then** purchase state is PURCHASED (not PENDING)
**And** purchase is acknowledged/consumed via BillingClient
**And** credits are delivered based on SKU:
- `credits_1` → +1 credit
- `credits_3` → +3 credits
- `credits_10` → +10 credits

**And** verification failure shows error message

**Architecture requirements:**
- Verification in `BillingRepository`: check `Purchase.purchaseState == PurchaseState.PURCHASED`
- Consumption: `BillingClient.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(token).build())`
- Credit delivery: `RulebookPreferences.addCredits(amount: Int)` in `core/datastore`
- SKU-to-credits mapping in `core/billing`: `mapOf("credits_1" to 1, "credits_3" to 3, "credits_10" to 10)`
- Consume BEFORE delivering credits — if consume fails, don't deliver (prevents double-deliver)
- If consume succeeds but credit delivery fails: log critical error — edge case, DataStore write failure is extremely rare

**Technical notes:**
- Check `Purchase.purchaseState == Purchase.PurchaseState.PURCHASED`
- `BillingClient.consumeAsync()` for consumable products
- Map SKU to credit amount via constant map
- `RulebookPreferences.addCredits()`: `edit { prefs -> prefs[CREDIT_BALANCE] = (prefs[CREDIT_BALANCE] ?: 0) + amount }`
- Log purchase events for analytics: `purchase_completed` with `sku`, `credits_added`, `new_balance`

**Prerequisites:** Story 8.6

---

## Story 8.8: Restore Purchases

As a user,
I want to restore my previous purchases,
So that I don't lose credits if I reinstall the app.

**Acceptance Criteria:**

**Given** the paywall shows "Restore Purchases" (FR39)
**When** the user taps restore
**Then** pending/unconsumed purchases are queried
**And** any found purchases are verified and consumed
**And** credits are delivered for valid purchases
**And** "No purchases to restore" shown if none found

**Architecture requirements:**
- Restore via `BillingRepository.queryUnconsumedPurchases()` → returns `List<PurchaseInfo>`
- Queries `BillingClient.queryPurchasesAsync(QueryPurchasesParams)` for INAPP products
- Any `PURCHASED` but not consumed purchases are processed: consume → deliver credits
- Restore action dispatched: `PurchaseIntent.RestorePurchases`
- Results: `PurchaseState.Success(creditsRestored)` or `PurchaseState.Error("No purchases to restore")`

**UX/Component specifications:**
- Restore button: Text button "Restore Purchases" in `body.callout` (16sp, Regular), underlined
  - Positioned in paywall footer
  - Color: `content.secondary` (#000000B2/#FFFFFFB2)
- Loading during restore: Replace text with `RulebookProgressIndicator` `AnimatedDots` type, small size
- Success: Snackbar "X credits restored!" with green checkmark
- No purchases found: Snackbar "No purchases to restore" — informational, not error
- Error: Snackbar with error message and retry action

**Technical notes:**
- `BillingClient.queryPurchasesAsync()` for `ProductType.INAPP`
- Process any `PURCHASED` but not consumed
- Note: Consumables can only be restored if not yet consumed — this is a limited window
- Analytics event: `purchase_restored` with `result` (success/none/error) and `credits_restored`

**Prerequisites:** Story 8.4

---

## Story 8.9: Pending Purchase Handling (Ask-to-Buy)

As a user with family sharing,
I want pending purchases handled gracefully,
So that Ask-to-Buy works correctly.

**Acceptance Criteria:**

**Given** a purchase requires approval (FR40)
**When** purchase state is PENDING
**Then** user sees "Purchase pending approval" message
**And** app checks for resolution on next launch
**And** credits delivered when purchase is approved

**Architecture requirements:**
- Pending detection: `Purchase.purchaseState == Purchase.PurchaseState.PENDING` in `PurchasesUpdatedListener`
- Store pending purchase token in DataStore: `RulebookPreferences.pendingPurchaseToken: Flow<String?>`
- On app resume (`onResume`/ViewModel `init`): `BillingRepository.checkPendingPurchases()` queries for resolution
- If `PENDING → PURCHASED`: consume and deliver credits
- If `PENDING → still PENDING`: no action, check again next launch
- Clear pending token after successful consumption

**UX/Component specifications:**
- Pending state UI: `AlertDialog` with informational styling:
  - Title: "Purchase Pending" in `brutalist.title` (24sp, Black 900)
  - Body: "Your purchase needs approval. Credits will be added once approved." in `body.body` (17sp, Regular)
  - Icon: Clock/hourglass icon in `brutalist.orange`
  - Single "OK" button: `RulebookButton` primary variant — dismisses dialog, returns to paywall
- No error styling — this is expected behavior for family accounts
- Paywall remains open — user can dismiss manually
- On resolution (next launch): Silent credit delivery + Snackbar "Purchase approved! X credits added"

**Technical notes:**
- Check `Purchase.purchaseState == PENDING` in listener
- Store pending purchase reference in DataStore
- Check on app resume/launch via `BillingRepository.checkPendingPurchases()`
- Handle `PENDING → PURCHASED` transition: consume → deliver

**Prerequisites:** Story 8.6

---

## Story 8.10: Purchase Analytics Events

As a product owner,
I want to track purchase funnel metrics,
So that I can optimize monetization.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** purchase events occur
**Then** the following events are tracked:
- `paywall_displayed` - Paywall shown
- `purchase_started` - User tapped product
- `purchase_completed` - Successful purchase with SKU
- `purchase_failed` - With error reason
- `purchase_restored` - Restore attempted with result

**Architecture requirements:**
- Events fired from `PurchaseViewModel` using `AnalyticsManager` from `core/analytics`
- Event names MUST match iOS event names for cross-platform consistency
- No purchase price or payment method in events (PII concern)
- SKU and credit amount are acceptable properties

**Technical notes:**
- Events fired from PurchaseViewModel at each purchase state transition
- Properties per event:
  - `paywall_displayed`: `source` (scan_gate/settings/manual), `current_balance`
  - `purchase_started`: `sku`, `credits`
  - `purchase_completed`: `sku`, `credits_added`, `new_balance`
  - `purchase_failed`: `sku`, `error_code`, `error_message`
  - `purchase_restored`: `result` (success/none/error), `credits_restored`
- Match iOS event names from TelemetryDeck integration

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 8 Complete: Credit System & Purchases**

**Stories Created:** 10
**FR Coverage:** FR34-41
**Architecture Sections Referenced:** feature/purchase, core/billing, core/data, core/datastore, core/model, core/designsystem, core/analytics
**UX Patterns Incorporated:** Paywall bottom sheet, product cards, purchase flow, credit display, restore purchases, pending states, haptic feedback
**Design Tokens Referenced:** brutalist.orange, brutalist.pink, brutalist.green, brutalist.red, brutalist.blue, brutalist.purple, brutalist.gray, surface.primary, surface.secondary, typography.brutalist.largeTitle, typography.special.priceDisplay, typography.special.creditBalance, shadows.offsets.subtle, spacing.brutalist.paywallContentPadding
