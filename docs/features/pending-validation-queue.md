# Pending Validation Queue with Exponential Backoff Retry

**Date:** 2026-03-22
**Story:** RULE-257 Story 10.3: Pending Validation Queue
**Related Files:**
- `core/billing/src/main/kotlin/com/rulebook/core/billing/pending/PendingValidation.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/pending/PendingValidationStore.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/pending/PendingValidationStoreImpl.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseViewModel.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseScreen.kt`

## Overview

When a user completes a purchase but the server is unreachable, the purchase token is saved locally with exponential backoff retry logic. If all retries fail, the pending validation persists in encrypted storage for recovery on the next app launch. This pattern implements graceful degradation for offline purchase scenarios while ensuring credits are never granted until server validation succeeds.

## What Was Built

- **PendingValidation data class**: Immutable storage for failed purchase tokens with metadata (timestamp, retry count)
- **PendingValidationStore interface and implementation**: Encrypted SharedPreferences-backed storage with automatic expiry
- **Exponential backoff retry logic**: Initial attempt + 3 retries with delays of 2s, 4s, and 8s (total 14 seconds)
- **ValidationPending event**: One-time event for UI to show "Purchase saved — will be verified shortly" message
- **72-hour expiry window**: Matches Google Play's 3-day purchase acknowledgment requirement
- **Selective retry logic**: Retries on transient errors (network, timeout) but not on `PurchaseValidationException` (server-side rejection)

## Technical Implementation

### Key Components

1. **PendingValidation** (`core/billing/src/main/kotlin/com/rulebook/core/billing/pending/PendingValidation.kt`)
   - `@Serializable` data class for JSON serialization
   - Fields: `purchaseToken`, `productId`, `timestamp`, `retryCount`
   - `isExpired()`: Returns true if older than 72 hours
   - `EXPIRY_DURATION_MS`: 72 * 60 * 60 * 1000L (exact match for Google's 3-day window)

2. **PendingValidationStore** (Interface)
   - `suspend fun save()`: Add/replace entry, filter expired
   - `suspend fun getAll()`: Return non-expired entries
   - `suspend fun remove()`: Delete specific entry by token
   - `suspend fun clear()`: Remove all entries

3. **PendingValidationStoreImpl** (EncryptedSharedPreferences)
   - Uses `MasterKey` with `AES256_GCM` key scheme
   - `EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV` for key encryption
   - `EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM` for value encryption
   - Stores as JSON array under single key `"pending_validations"`
   - Factory method `create(context: Context)` for production
   - Constructor accepts `SharedPreferences` for test compatibility

4. **Retry Logic in PurchaseViewModel**
   - Loop: `for (attempt in 0..MAX_VALIDATION_RETRY_ATTEMPTS)` (0-3, total 4 attempts)
   - Delay calculation: `delay(2000L * (1L shl (attempt - 1)))` (bitwise left shift for exponential backoff)
   - Attempt 0: Immediate (no delay)
   - Attempt 1: 2s delay
   - Attempt 2: 4s delay
   - Attempt 3: 8s delay

5. **Error Classification in Retry Loop**
   - **PurchaseValidationException** (invalid purchase): Fail immediately, no retry
   - **CancellationException**: Rethrow to preserve coroutine cancellation
   - **Other Throwable** (network, timeout): Retry with backoff
   - After all 4 attempts fail: Save to `PendingValidationStore`, emit `ValidationPending` event

### Key Patterns

**Pattern: Encrypted Storage for Financial Data**
- Use `EncryptedSharedPreferences` with `AES256_GCM` for all sensitive data (purchase tokens, credentials)
- Always use both key and value encryption schemes (not just one)
- Store as JSON arrays for structured data with multiple entries
- Include automatic expiry logic in the store interface (not UI layer)

**Pattern: Exponential Backoff for Transient Failures**
- First attempt is immediate (no delay)
- Subsequent attempts use exponential delays: `2^(n-1) * base_delay_ms`
- Use bitwise left shift (`1L shl n`) for efficient power-of-2 calculation
- Always include a maximum retry count (3 retries, 4 total attempts)
- Log each retry attempt at WARN level, final exhaustion at ERROR level

**Pattern: Selective Retry Based on Error Type**
- Retry only transient errors (network, timeout, IOException, SocketTimeoutException)
- Do NOT retry application/business errors (marked with custom exception types)
- Use exception type checking with `is` to differentiate categories
- Preserve coroutine cancellation by rethrowing `CancellationException`

**Pattern: Graceful Degradation with User Feedback**
- After retry exhaustion, persist to local store (don't fail the UI)
- Show non-error message ("saved — will be verified shortly") not error message
- Reset UI state to null (no error styling) to allow user to continue app flow
- Emit one-time event for UI to show snackbar with appropriate messaging

**Pattern: Testability with Factory Methods**
- Accept `SharedPreferences` in constructor (not Context)
- Provide static factory `create(context: Context)` for production
- Test double can pass `FakeSharedPreferences` (in-memory implementation)
- Avoids need for Robolectric or mock framework initialization

### Code Examples

**Creating and saving a pending validation:**
```kotlin
// In PurchaseViewModel after all retries exhausted
val pending = PendingValidation(
    purchaseToken = "goog.xxx",
    productId = "credits_3",
    timestamp = System.currentTimeMillis(),
    retryCount = MAX_VALIDATION_RETRY_ATTEMPTS
)
pendingValidationStore.save(pending)  // Filters expired, replaces duplicates
_events.send(PurchaseEvent.ValidationPending)
```

**Querying pending validations:**
```kotlin
// In Story 10.4 app-launch recovery (future)
val pending = pendingValidationStore.getAll()  // Filters expired automatically
pending.forEach { validation ->
    // Attempt verification again using validation.purchaseToken
}
```

**Test double implementation:**
```kotlin
class FakePendingValidationStore : PendingValidationStore {
    val savedValidations = mutableListOf<PendingValidation>()

    override suspend fun save(pendingValidation: PendingValidation) {
        savedValidations.removeAll { it.purchaseToken == pendingValidation.purchaseToken }
        savedValidations.add(pendingValidation)
    }

    override suspend fun getAll() = savedValidations.filter { !it.isExpired() }
}
```

## How to Use

### For Offline-First Purchase Handling

1. **In PurchaseViewModel or similar**: Inject `PendingValidationStore`
2. **On transient validation failure**: Save entry to store after retry exhaustion
3. **Emit ValidationPending event**: UI shows graceful degradation message
4. **User can dismiss**: Normal app flow continues, no error state

### For Retry Logic in Other Contexts

Apply the same exponential backoff pattern to other transient failures:

1. Define `MAX_RETRY_ATTEMPTS` constant (typically 3)
2. Use `for (attempt in 0..MAX_RETRY_ATTEMPTS)` loop
3. Calculate delay: `2000L * (1L shl (attempt - 1))` (or adjust base delay as needed)
4. On first attempt (attempt == 0), don't delay
5. Classify errors: retry on transient, fail immediately on business errors

### For EncryptedSharedPreferences Storage

1. Create in `create(context: Context)` factory method
2. Use `MasterKey.Builder(context).setKeyScheme(AES256_GCM).build()`
3. Specify both key and value encryption schemes
4. Store structured data as JSON with serialization framework
5. Implement automatic cleanup (expiry, deduplication) in store methods

## Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| MAX_VALIDATION_RETRY_ATTEMPTS | 3 | Number of retry attempts after initial failure |
| Retry Delay Base | 2000 ms | First retry delay; subsequent delays are 4x, 8x |
| EXPIRY_DURATION_MS | 72 * 60 * 60 * 1000 | Matches Google's 3-day purchase acknowledgment window |
| Encryption Scheme | AES256_GCM + AES256_SIV | Production security; test doubles can use plain SharedPreferences |
| Snackbar Duration | Short (4s) | Time to display "Purchase saved" message |

## Notes

- **Credits are NOT granted until validation succeeds**: Even after pending entry is saved, balance remains unchanged
- **Retry happens silently**: No error shown to user, graceful degradation with snackbar feedback
- **72-hour expiry is hard requirement**: Ensures compliance with Google Play's acknowledgment window
- **Story 10.4 dependency**: Pending entries must be recovered on app launch (not in scope of this story)
- **Bitwise left shift trick**: `1L shl n` is more efficient than `Math.pow(2.0, n)` for exponential backoff
- **CancellationException handling is critical**: Must rethrow to preserve cancellation semantics in coroutine scope
- **Test doubles are required**: Real EncryptedSharedPreferences initialization is slow in tests; use in-memory doubles
- **JSON serialization overhead**: Storing as JSON string is acceptable; alternative: use Parcel or Protocol Buffers for larger datasets
