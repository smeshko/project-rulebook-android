# Epic 8: Credit System & Purchases

**Goal:** Implement the monetization flow with paywall and Google Play Billing. After this epic, users can purchase credit packs, restore purchases, and have credits gated appropriately.

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

**Technical Notes:**
- CreditsDisplay composable
- Observe creditBalance Flow from DataStore
- Reusable across screens

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

**Technical Notes:**
- Transaction in GameRepository
- DataStore update in same logical transaction
- Credit check before scan start (Story 5.1)

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

**Technical Notes:**
- PaywallScreen in `feature/purchase` module
- ModalBottomSheet or full-screen dialog
- BillingClient queries for product details

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

**Technical Notes:**
- Play Billing Library 7.x (Architecture section)
- BillingClient.newBuilder() with listener
- ProductType.INAPP for consumables
- BillingRepository wraps client

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

**Technical Notes:**
- ProductCard composable
- Price from ProductDetails.oneTimePurchaseOfferDetails
- Highlight middle option for psychological anchoring

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

**Technical Notes:**
- BillingClient.launchBillingFlow()
- Handle PurchasesUpdatedListener callback
- Consume purchase immediately (consumable)
- Add credits to DataStore

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

**Technical Notes:**
- Check Purchase.purchaseState
- BillingClient.consumeAsync() for consumables
- Map SKU to credit amount
- Log purchase events for analytics

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

**Technical Notes:**
- BillingClient.queryPurchasesAsync()
- Process any PURCHASED but not consumed
- Note: Consumables can only be restored if not yet consumed

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

**Technical Notes:**
- Check Purchase.purchaseState == PENDING
- Store pending purchase reference
- Check on app resume/launch
- Handle PENDING → PURCHASED transition

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

**Technical Notes:**
- Events fired from PurchaseViewModel
- Include SKU, price, and credit amount properties
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 8 Complete: Credit System & Purchases**

**Stories Created:** 10
**FR Coverage:** FR34-41
**Architecture Sections Referenced:** feature/purchase, core/billing, core/datastore
**UX Patterns Incorporated:** Paywall bottom sheet, product cards, purchase flow

---
