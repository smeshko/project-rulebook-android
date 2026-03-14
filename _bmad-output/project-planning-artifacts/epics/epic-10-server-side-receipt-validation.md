# Epic 10: Server-Side Receipt Validation

**Goal:** Harden the purchase flow by moving receipt validation from client-side to server-side. After this epic, credits are only granted after the backend confirms the purchase with Google Play Developer API, and refunded purchases are detected and revoked.

**PRD:** `docs/product/prd-server-side-receipts-validation.md`
**FRs covered:** FR1.1-1.7, FR3.1-3.7, FR4.1-4.5

---

## Story 10.1: Receipt Validation Network Layer

As a developer,
I want a network layer for server-side receipt validation,
So that the app can send purchase tokens to the backend for verification.

**Acceptance Criteria:**

**Given** a successful Google Play purchase
**When** the app needs to validate the receipt
**Then** it sends the purchase token, product ID, and package name to `POST /api/v1/receipts/validate`
**And** parses the response: `valid`, `invalid`, or `already_processed`
**And** handles network errors with `Result.Error`

**Architecture requirements:**
- `ReceiptValidationApi` interface in `core/network` with Retrofit annotation:
  ```kotlin
  @POST("api/v1/receipts/validate")
  suspend fun validateReceipt(@Body request: ValidateReceiptRequest): ValidateReceiptResponse
  ```
- `ValidateReceiptRequest` in `core/network`: `purchaseToken: String`, `productId: String`, `packageName: String`
- `ValidateReceiptResponse` in `core/network`: `status: String` (`valid`, `invalid`, `already_processed`), `creditsGranted: Int?`, `message: String?`
- `ReceiptRepository` interface in `core/data`:
  ```kotlin
  interface ReceiptRepository {
      suspend fun validatePurchase(purchaseToken: String, productId: String): Result<ValidationResult>
      suspend fun checkRefundStatus(purchaseTokens: List<String>): Result<List<RefundStatus>>
  }
  ```
- `ValidationResult` domain model in `core/model`: `status: ValidationStatus`, `creditsGranted: Int`
- `ValidationStatus` enum: `VALID`, `INVALID`, `ALREADY_PROCESSED`
- Error mapping in repository: network errors → user-friendly messages (same pattern as `ScanRepository`)
- Timeout: 10s for validation (shorter than scan — this should be fast)
- Koin provides `ReceiptRepository` as singleton

**Technical notes:**
- Retrofit interface added to existing `core/network` module
- Response mapping via `@Serializable` and `@SerialName` annotations
- Same OkHttp client as other API calls (TLS 1.3, logging interceptor)
- Package name from `BuildConfig.APPLICATION_ID`

**Prerequisites:** Epic 1 (network client), Epic 8 (billing module exists)

---

## Story 10.2: Modify Purchase Flow for Server Validation

As a user,
I want my purchase verified by the server before receiving credits,
So that the purchase is guaranteed legitimate.

**Acceptance Criteria:**

**Given** a Google Play purchase completes with `PURCHASED` state
**When** the purchase listener fires
**Then** the app sends the purchase token to the backend (NOT `consumeAsync()`)
**And** credits are granted ONLY after backend returns `valid`
**And** `already_processed` is treated as success (idempotent)
**And** `invalid` shows an error and does NOT grant credits
**And** the app does NOT call `consumeAsync()` or `acknowledgePurchase()` locally

**Architecture requirements:**
- Modify `PurchasesUpdatedListener` in `BillingClientWrapper` to emit purchase to a `Flow` instead of triggering local consume
- `PurchaseViewModel` state machine updated:
  - `Processing(sku)` → `Validating(sku)` → `Success(creditsAdded)` | `Error(message)`
- New `PurchaseState.Validating(sku: String)` added to sealed class
- `PurchaseViewModel` calls `ReceiptRepository.validatePurchase()` instead of `BillingRepository.consumePurchase()`
- On `VALID`: deliver credits via `RulebookPreferences.addCredits(amount)`, update UI
- On `VALID`: save purchase token + productId to `PurchaseHistoryStore` for refund tracking (Story 10.5)
- On `ALREADY_PROCESSED`: treat as success, deliver credits if not already delivered locally
- On `INVALID`: show error dialog, do NOT grant credits
- Remove `consumePurchase()` call from client purchase flow

**UX/Component specifications:**
- New "Validating..." state shown after Google Play sheet closes:
  - `RulebookProgressIndicator` `AnimatedDots` type centered on paywall
  - Text: "Verifying purchase..." in `body.body` (17sp)
- Success/error states unchanged from Story 8.6
- If validation takes >5s, show subtle "This is taking longer than expected..." message

**Technical notes:**
- `BillingRepository.launchPurchaseFlow()` unchanged — only post-purchase handling changes
- `PurchaseViewModel` orchestrates: purchase listener → validate with server → deliver credits
- Analytics: add `purchase_validated` event with `status` property

**Prerequisites:** Story 10.1, Epic 8 (Stories 8.4-8.7 complete)

---

## Story 10.3: Pending Validation Queue

As a user,
I want my purchase saved locally if the server is unreachable,
So that I don't lose my purchase when connectivity is restored.

**Acceptance Criteria:**

**Given** a purchase completes but the backend is unreachable
**When** the validation request fails (timeout, network error)
**Then** the purchase token is stored in EncryptedSharedPreferences
**And** the user sees "Purchase saved — will be verified shortly"
**And** up to 3 retry attempts are made with exponential backoff (2s, 4s, 8s)
**And** if all retries fail, the pending validation persists for app-launch recovery (Story 10.4)
**And** credits are NOT granted until validation succeeds

**Architecture requirements:**
- `PendingValidationStore` in `core/billing`:
  ```kotlin
  interface PendingValidationStore {
      suspend fun save(pendingValidation: PendingValidation)
      suspend fun getAll(): List<PendingValidation>
      suspend fun remove(purchaseToken: String)
      suspend fun clear()
  }
  ```
- `PendingValidation` data class: `purchaseToken: String`, `productId: String`, `timestamp: Long`, `retryCount: Int`
- Implementation uses `EncryptedSharedPreferences` from AndroidX Security library
- Retry logic in `PurchaseViewModel`: `retry(maxAttempts = 3, delayMs = { attempt -> 2000L * 2.pow(attempt) })`
- After max retries exhausted: persist to `PendingValidationStore`, show pending UI
- `PendingValidation` entries expire after 72 hours (matches Google's 3-day acknowledgment window)

**UX/Component specifications:**
- Pending state: `Snackbar` with "Purchase saved — will be verified shortly" message
- No error styling — this is a graceful degradation, not failure
- Credit balance does NOT change while pending
- Paywall can be dismissed — user returns to normal app flow

**Technical notes:**
- `EncryptedSharedPreferences.create()` with `AES256_SIV` key scheme
- Store as JSON array of `PendingValidation` objects
- Retry uses `kotlinx.coroutines.delay()` with exponential backoff
- Add `androidx.security:security-crypto` dependency to `core/billing`

**Prerequisites:** Story 10.2

---

## Story 10.4: App Launch Validation Recovery

As a user,
I want pending purchases validated when I reopen the app,
So that I eventually receive my credits even after connectivity issues.

**Acceptance Criteria:**

**Given** the app launches
**When** there are pending validations in EncryptedSharedPreferences
**Then** each pending validation is retried with the backend
**And** on success, credits are granted and pending entry removed
**And** on failure, pending entry remains for next launch
**And** expired entries (>72h) are removed and logged as lost

**Given** the app launches
**When** `queryPurchasesAsync()` returns PURCHASED but unacknowledged purchases
**Then** those purchases are also sent to the backend for validation
**And** this catches purchases lost to process death or crashes

**Architecture requirements:**
- `ValidationRecoveryWorker` or logic in `App.onCreate()` / `MainViewModel.init`:
  1. Read `PendingValidationStore.getAll()`
  2. For each: call `ReceiptRepository.validatePurchase()`
  3. On success: `RulebookPreferences.addCredits()` + `PendingValidationStore.remove()`
  4. On failure: increment retryCount, keep in store
  5. Remove expired entries (timestamp + 72h < now)
- Additionally: `BillingRepository.queryUnconsumedPurchases()` → filter for PURCHASED state → validate any not in pending store
- Recovery runs on `Dispatchers.IO`, non-blocking to app startup
- Silent operation — no UI unless credits are actually recovered

**UX/Component specifications:**
- Silent recovery — no loading indicator on launch
- If credits recovered: `Snackbar` "X credits from a previous purchase have been added!"
- If expired entries found: silent removal, analytics event only

**Technical notes:**
- Run in `viewModelScope` of `MainViewModel` or as `WorkManager` one-time task
- Non-blocking: `launch(Dispatchers.IO)` so app startup isn't delayed
- Analytics: `purchase_recovery_attempted` with `pending_count`, `recovered_count`, `expired_count`
- Edge case: if user reinstalls, EncryptedSharedPreferences is wiped — `queryPurchasesAsync()` catches this

**Prerequisites:** Story 10.3, Story 8.8 (queryPurchasesAsync pattern)

---

## Story 10.5: Refund Sync (Pull-Based)

As a developer,
I want the app to detect refunded purchases,
So that credits from refunded transactions can be revoked.

**Acceptance Criteria:**

**Given** the backend tracks refunded purchases via RTDN
**When** the app launches or resumes from background
**Then** it calls a refund status endpoint with recent purchase tokens
**And** if any are marked as refunded, those credits are revoked
**And** the user is informed via a non-intrusive message

**Architecture requirements:**
- Backend exposes: `POST /api/v1/receipts/status` accepting `{ tokens: [String] }`
- Response: `List<RefundStatus>` where `RefundStatus`: `purchaseToken: String`, `status: String` (`active`, `refunded`)
- `ReceiptRepository.checkRefundStatus()` calls this endpoint
- Refund check in `MainViewModel.init` or `onResume`:
  1. Read recent purchase tokens from local history (DataStore or Room)
  2. Send to backend
  3. If any refunded: `RulebookPreferences.decrementCredits(amount)`
  4. Store refund acknowledgment locally to avoid repeat revocations
- `PurchaseHistoryStore` in `core/billing`: stores last N purchase tokens + productIds for refund checking
- Purchase history saved when credits are successfully granted (Story 10.2)

**UX/Component specifications:**
- Refund revocation: `Snackbar` "A previous purchase was refunded. X credits removed."
- Non-aggressive — informational tone, no error styling
- Credit balance updates reactively via existing DataStore Flow

**Technical notes:**
- Purchase history: simple DataStore `stringSetPreferencesKey` of recent tokens (last 20)
- Refund check throttled: max once per app launch, not on every resume
- Analytics: `purchase_refund_detected` with `tokens_refunded` count
- If credits go negative after revocation, clamp to 0

**Prerequisites:** Story 10.1, Story 10.2 (purchase history tracking)

---

## Story 10.6: Remove Client-Side Consume/Acknowledge

As a developer,
I want to clean up the billing module to remove client-side consume/acknowledge,
So that the codebase reflects the server-side validation architecture.

**Acceptance Criteria:**

**Given** server-side validation is fully operational (Stories 10.1-10.4)
**When** reviewing the billing module
**Then** `BillingRepository.consumePurchase()` is removed or deprecated
**And** `BillingClientWrapper` no longer calls `consumeAsync()` or `acknowledgePurchase()`
**And** `BillingRepository` interface is updated:
  ```kotlin
  interface BillingRepository {
      val products: Flow<List<ProductInfo>>
      suspend fun queryProducts(): Result<List<ProductInfo>>
      suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit>
      suspend fun queryUnacknowledgedPurchases(): Result<List<PurchaseInfo>>
  }
  ```
**And** all call sites are updated
**And** existing tests updated to reflect new flow

**Architecture requirements:**
- Remove `consumePurchase()` from `BillingRepository` interface and implementation
- Rename `queryUnconsumedPurchases()` to `queryUnacknowledgedPurchases()` for clarity (server acknowledges, not client)
- Update Story 8.8 (Restore Purchases) to use server validation path instead of local consume
- Update Story 8.9 (Pending Purchase Handling) — pending → purchased transition now sends to server, not local consume

**Technical notes:**
- Breaking change to `BillingRepository` interface — update all Koin modules and tests
- Restore flow (Story 8.8): query unconsumed → send each to backend → credits on valid response
- Pending purchase (Story 8.9): on PURCHASED transition → send to backend (same as Story 10.2)
- Remove `ConsumeParams` and `AcknowledgePurchaseParams` usage from client code

**Prerequisites:** Stories 10.1-10.4 validated and stable

---

**Epic 10 Complete: Server-Side Receipt Validation**

**Stories Created:** 6
**PRD Coverage:** FR1.1-1.7 (backend, informed by client), FR3.1-3.7, FR4.1-4.5 (pull-based)
**Architecture Sections Referenced:** core/network, core/data, core/billing, core/model, core/datastore, feature/purchase
**Dependencies:** Epic 8 must be complete before starting Epic 10

---

## Story 10.7: Credit Balance Reconciliation

As a user,
I want my local credit balance to stay in sync with the server,
So that I always see the correct number of credits.

**Acceptance Criteria:**

**Given** the server tracks credits granted and refunded
**When** the app launches and the backend is reachable
**Then** the app fetches the authoritative credit balance from the server
**And** if the server balance differs from local DataStore, the local balance is updated
**And** the user is NOT notified unless credits were revoked (handled by Story 10.5)

**Architecture requirements:**
- Backend exposes: `GET /api/v1/credits/balance` (or include in an existing sync endpoint)
- Response: `{ balance: Int }`
- `ReceiptRepository.getServerBalance(): Result<Int>`
- Reconciliation in `MainViewModel.init` (after validation recovery, Story 10.4):
  1. Fetch server balance
  2. Compare with `RulebookPreferences.creditBalance`
  3. If different: update local DataStore to match server
- Server is authoritative — local balance is a cache
- Throttled: max once per app launch, only when online
- Silent operation — no UI for upward corrections

**Technical notes:**
- Runs after Story 10.4 recovery and Story 10.5 refund check
- If server unreachable: skip, local balance is best-effort
- Analytics: `credit_balance_reconciled` with `local_balance`, `server_balance`, `delta`
- Edge cases: new install (server has history, local is 0) — server balance wins

**Prerequisites:** Story 10.1, Stories 10.2-10.5 (reconciliation is the final sync step)
