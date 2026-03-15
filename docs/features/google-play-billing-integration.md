# Google Play Billing Integration (RULE-227)

**Date:** 2026-03-14
**Related Files:**
- `core/billing/src/main/kotlin/com/rulebook/core/billing/BillingClientWrapper.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/BillingRepositoryImpl.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/di/BillingModule.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/repository/BillingRepository.kt`
- `core/billing/src/test/kotlin/com/rulebook/core/billing/BillingRepositoryImplTest.kt`

## Overview

This story implements the complete Google Play Billing Library integration for in-app purchases. The implementation provides a robust, testable wrapper around the Google Play Billing Client with exponential backoff retry logic, connection lifecycle management, and Product Details caching. This foundation enables users to purchase credit packs and establish the payment flow for the application.

## What Was Built

- **BillingClientWrapper interface + BillingClientWrapperImpl**: Abstraction over Google Play BillingClient with coroutine-based APIs, lifecycle management, and error handling
- **Connection Management**: StateFlow-based connection state tracking with automatic retry on SERVICE_DISCONNECTED
- **Product Querying**: SKU-mapped product queries (credits_1 → 1 credit, credits_3 → 3 credits, credits_10 → 10 credits) with ProductDetails in-memory caching
- **Purchase Consumption**: Safe consumption of purchases with proper error handling
- **Unconsumed Purchase Queries**: Filtering and retrieval of purchased but unconsumed purchases for credit fulfillment
- **BillingRepositoryImpl**: Real implementation delegating to wrapper with error handling and Result<T> pattern
- **Comprehensive Testing**: 18 unit tests using FakeBillingClientWrapper (test fake pattern) and RetryWithExponentialBackoffTest

## Technical Implementation

### Key Files

- **BillingClientWrapper.kt**:
  - Interface defining contract for testability
  - BillingClientWrapperImpl: Real Android implementation wrapping Google Play Billing Library 7.x
  - Uses billing-ktx suspend function extensions (queryProductDetails, consumePurchase, queryPurchasesAsync)
  - PurchaseUpdate data class for emitting purchase updates via SharedFlow
  - Manages connection lifecycle with StateFlow<Boolean> and PurchasesUpdatedListener

- **BillingRepositoryImpl.kt**:
  - Delegates to BillingClientWrapper
  - Implements BillingRepository interface (core/data domain contract)
  - Maps domain types (ProductInfo, PurchaseInfo) to avoid Android billing types leaking
  - Updates _products StateFlow on successful queries

- **BillingModule.kt**:
  - Koin DI module providing BillingClientWrapper as single { } (Application-scoped)
  - Provides BillingRepository binding
  - Ensures singleton lifecycle tied to Application

### Key Patterns

#### 1. **Wrapper Abstraction for Testability**
Isolates Google Play Billing Client behind an interface, enabling fake implementations for testing without Play Store connectivity:

```kotlin
interface BillingClientWrapper {
    val connectionState: StateFlow<Boolean>
    val purchaseUpdates: SharedFlow<PurchaseUpdate>
    suspend fun ensureConnected(): Result<Unit>
    suspend fun queryProducts(): Result<List<ProductInfo>>
    fun launchBillingFlow(activity: Activity, productId: String): Result<Unit>
    suspend fun consumePurchase(purchaseToken: String): Result<Unit>
    suspend fun queryPurchases(): Result<List<PurchaseInfo>>
    fun disconnect()
}
```

Implementation can be swapped with FakeBillingClientWrapper in tests.

#### 2. **Exponential Backoff Retry Logic**
Generic, reusable function for retrying operations with exponential backoff (max 3 attempts):

```kotlin
internal suspend fun <T> retryWithExponentialBackoff(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1_000L,
    block: suspend (attempt: Int) -> T
): T
```

Delays: 1s, 2s, 4s between retries. Preserves CancellationException for proper coroutine cancellation.

**Use case**: Transient failures like SERVICE_DISCONNECTED can be recovered by reconnecting and retrying.

#### 3. **Connection State Management**
StateFlow<Boolean> tracks connection status in real-time:
- Updated when billingClient.isReady status changes
- Triggers reconnection on SERVICE_DISCONNECTED/SERVICE_UNAVAILABLE responses
- Exposed for UI/ViewModel to react to connection state

#### 4. **ProductDetails In-Memory Caching**
ProductDetails are cached after successful query to avoid repeated Play Store requests:

```kotlin
private val cachedProductDetails = mutableMapOf<String, ProductDetails>()
```

Cache is cleared on each queryProducts() call to ensure freshness. Used by launchBillingFlow() to avoid additional queries.

#### 5. **Error Handling with Result<T>**
All suspend functions return Result<T>, providing:
- Type-safe success/failure handling
- Non-throwing API (no exception propagation to callers)
- Clear success/failure path visible in calling code

#### 6. **PurchasesUpdatedListener via SharedFlow**
Purchase updates flow through a SharedFlow pattern for non-blocking event distribution:

```kotlin
private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(
    replay = 0,
    extraBufferCapacity = 10
)
```

Available for future use in PurchaseViewModel or other observers.

### Code Examples

**Using BillingClientWrapper to Query Products:**
```kotlin
class BillingRepositoryImpl(private val wrapper: BillingClientWrapper) {
    suspend fun queryProducts(): Result<List<ProductInfo>> {
        val result = wrapper.queryProducts()
        if (result.isSuccess) {
            _products.value = result.getOrNull() ?: emptyList()
        }
        return result
    }
}
```

**Testing with FakeBillingClientWrapper:**
```kotlin
@Before
fun setup() {
    fakeWrapper = FakeBillingClientWrapper()
    repository = BillingRepositoryImpl(fakeWrapper)
}

@Test
fun `queryProducts returns mapped ProductInfo list from wrapper`() = runTest {
    val productInfoList = listOf(
        ProductInfo("credits_1", "1 Credit", "$0.99", 1),
        ProductInfo("credits_3", "3 Credits", "$1.99", 3)
    )
    fakeWrapper.productsToReturn = Result.success(productInfoList)

    val result = repository.queryProducts()

    assertTrue(result.isSuccess)
    assertEquals(productInfoList, result.getOrNull())
}
```

**Launching Purchase Flow:**
```kotlin
fun launchPurchase(activity: Activity, productId: String): Result<Unit> {
    return billingRepository.launchPurchaseFlow(activity, productId)
}
```

## How to Use

### 1. Inject BillingRepository in ViewModel
```kotlin
class PurchaseViewModel(
    private val billingRepository: BillingRepository
) : ViewModel()
```

### 2. Query Available Products
```kotlin
viewModelScope.launch {
    val result = billingRepository.queryProducts()
    if (result.isSuccess) {
        val products = result.getOrNull() ?: emptyList()
        // Display products in UI
    } else {
        // Handle error
    }
}
```

### 3. Observe Products Flow
```kotlin
billingRepository.products
    .onEach { productList ->
        _uiState.update { it.copy(products = productList) }
    }
    .launchIn(viewModelScope)
```

### 4. Launch Purchase Flow
```kotlin
Button(
    onClick = {
        billingRepository.launchPurchaseFlow(activity, productId)
    }
)
```

### 5. Consume Purchases
```kotlin
viewModelScope.launch {
    val result = billingRepository.consumePurchase(purchaseToken)
    if (result.isSuccess) {
        // Purchase consumed, credits added
    }
}
```

### 6. Query Unconsumed Purchases (for Restore)
```kotlin
viewModelScope.launch {
    val result = billingRepository.queryUnconsumedPurchases()
    if (result.isSuccess) {
        result.getOrNull()?.forEach { purchase ->
            // Process each unconsumed purchase
        }
    }
}
```

## Configuration

| Component | Responsibility | Key Features |
|-----------|-----------------|--------------|
| BillingClientWrapper | Abstract Google Play Billing client | Connection lifecycle, retry logic, error handling |
| BillingClientWrapperImpl | Real Android implementation | Coroutine-based APIs, ProductDetails caching |
| BillingRepositoryImpl | Domain repository layer | Result<T> error handling, Flow<List<ProductInfo>> updates |
| FakeBillingClientWrapper | Test fake implementation | Configurable return values for all operations |

**SKU Configuration:**
```kotlin
internal val SKU_CREDIT_MAP: Map<String, Int> = mapOf(
    "credits_1" to 1,
    "credits_3" to 3,
    "credits_10" to 10
)
```

**Retry Configuration:**
```kotlin
private const val MAX_RETRIES = 3
internal const val INITIAL_RETRY_DELAY_MS = 1_000L
// Delays: 1s, 2s, 4s
```

**Koin Injection:**
```kotlin
single { BillingClientWrapperImpl(androidContext()) }
single<BillingRepository> {
    BillingRepositoryImpl(get<BillingClientWrapper>())
}
```

## Notes

- **Lazy Connection**: BillingClient connection happens on first billingRepository operation call, not at Application startup
- **Exception Preservation**: CancellationException is re-thrown immediately in retry logic to respect coroutine cancellation
- **Service Disconnected Handling**: Both SERVICE_DISCONNECTED and SERVICE_UNAVAILABLE trigger connection state reset and retry
- **Thread Safety**: All StateFlow and SharedFlow operations are thread-safe; no manual synchronization needed
- **Testing**: All 18 unit tests pass without Play Store connectivity; FakeBillingClientWrapper can be configured per test
- **Future Enhancements**:
  - PurchasesUpdatedListener (SharedFlow<PurchaseUpdate>) ready for real-time purchase notifications
  - Connection retry on SERVICE_DISCONNECTED supports resilience during network transitions
  - SKU_CREDIT_MAP centralized for easy feature pack addition
- **Breaking Changes**: None - new integration only
- **Next Steps**: Complete PurchaseViewModel purchase flow (onProductSelected, onRestorePurchases); integrate with UI layer

