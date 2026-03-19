# Server-Side Receipt Validation Network Layer

**Date:** 2026-03-20
**Related Files:** `core/network/src/main/kotlin/com/rulebook/core/network/api/ReceiptValidationApi.kt`, `core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepository.kt`

## Overview

Implemented a server-side receipt validation network layer that sends purchase tokens to a backend API for verification. This enables the app to transition from interim client-side validation to a secure, server-verified purchase verification system.

## What Was Built

- **ReceiptValidationApi**: Retrofit interface with two endpoints for receipt and refund validation
- **Request/Response DTOs**: Serializable models for network communication with `@SerialName` annotations
- **Domain Models**: `ValidationStatus`, `ValidationResult`, `RefundStatus` representing business logic
- **ReceiptRepository**: Clean abstraction layer providing `validatePurchase()` and `checkRefundStatus()` methods
- **Response Mapping**: `ValidationResultMapper` converts API responses to domain models
- **Error Handling**: Network errors mapped to user-friendly messages via `NetworkErrorMapper`
- **DI Registration**: Koin singleton configuration for dependency injection
- **Unit Tests**: 11 repository tests + 9 mapper tests covering all success/error paths

## Technical Implementation

### Key Files

- `core/network/src/main/kotlin/com/rulebook/core/network/api/ReceiptValidationApi.kt`: Retrofit interface defining two POST endpoints
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepository.kt`: Interface defining repository contract
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepositoryImpl.kt`: Implementation with `safeCall` wrapper and error mapping
- `core/network/src/main/kotlin/com/rulebook/core/network/mapper/ValidationResultMapper.kt`: DTO-to-domain mapper
- `core/data/src/main/kotlin/com/rulebook/core/data/di/DataModule.kt`: Koin configuration

### Key Patterns

**Separate API Interface Pattern**: `ReceiptValidationApi` is a standalone interface (not extending `RulebookApi`), enabling:
- Independent configuration and lifecycle management
- Cleaner separation of concerns
- Easier to test with fake implementations
- Can be swapped for different implementations without affecting other APIs

**Testable Repository with String Parameters**: `ReceiptRepositoryImpl` accepts `packageName: String` instead of `Context`, enabling:
- JVM unit testing without Android framework dependencies
- Compliance with the project's fake-based test strategy
- Clear dependency declaration (no hidden Android context dependencies)
- Runtime behavior preserved: `androidContext().packageName` injected in Koin binding

**Request/Response Mapping Pattern**: Two-way mapping via extension functions:
- `ValidateReceiptResponse.toDomain()` returns `ValidationResult`
- `CheckRefundResponse.toDomain()` returns `List<RefundStatus>`
- Provides a clean DTO ↔ domain model boundary
- Encapsulates serialization/deserialization details

**Network Error Mapping**: Consistent error handling via `NetworkErrorMapper`:
- `SocketTimeoutException` → "The request took too long. Please try again."
- `UnknownHostException` → "No internet connection. Please check your network."
- `HttpException (5xx)` → "Server error. Please try again later."
- All errors wrapped in `Result.Error`

### Code Examples

**Using the Repository**:
```kotlin
// In a ViewModel or use case
suspend fun verifyPurchase(token: String, productId: String): Result<ValidationResult> {
    return receiptRepository.validatePurchase(token, productId)
}

// Response handling
when (val result = repository.validatePurchase(token, productId)) {
    is Result.Success -> {
        val (status, credits) = result.data
        if (status == ValidationStatus.VALID) {
            grantCredits(credits)
        }
    }
    is Result.Error -> showError(result.message)
}
```

**Testing with Fake**:
```kotlin
val fakeApi = FakeReceiptValidationApi().apply {
    validateReceiptResult = ValidateReceiptResponse(
        status = "valid",
        creditsGranted = 5
    )
}
val repository = ReceiptRepositoryImpl(api = fakeApi, packageName = "com.rulebook.app")
```

## How to Use

1. **Inject the repository** into your ViewModel:
   ```kotlin
   class MyViewModel(private val receiptRepository: ReceiptRepository) : ViewModel() { }
   ```

2. **Call validation** when a purchase needs verification:
   ```kotlin
   val result = receiptRepository.validatePurchase(purchaseToken, productId)
   ```

3. **Handle the result**:
   - `Result.Success(ValidationResult)` → Extract status and creditsGranted
   - `Result.Error(message)` → Show user-friendly error message

4. **Check refund status** for refund reconciliation:
   ```kotlin
   val refunds = receiptRepository.checkRefundStatus(listOf(token1, token2))
   ```

## Configuration

| Component | Configuration | Details |
|-----------|---------------|---------|
| API Endpoint | `POST /api/v1/receipts/validate` | Validates a single receipt |
| Refund Endpoint | `POST /api/v1/receipts/refund-status` | Checks refund status for multiple receipts |
| Timeout | 10 seconds | Shorter than scanning (30s) — validation should be fast |
| Package Name | `androidContext().packageName` | Injected at DI time via Koin |
| OkHttp Client | Shared across app | Uses TLS 1.3, logging interceptor |

## Notes

- **Interim Solution**: This enables transition from client-side to server-side validation. The client-side `PurchaseVerifier` (RULE-230) will be replaced once server-side validation is fully deployed.
- **No Message Field in Success**: `ValidateReceiptResponse` only includes optional `message` on error responses, not success (design via backend API contract).
- **Testing Strategy**: Comprehensive fake-based unit tests validate all paths without mocking; enables JVM testing without Android framework.
- **Error Messages**: Generic user-friendly messages prevent exposing internal system details; see `NetworkErrorMapper` for mapping rules.
- **Future Enhancement**: Will integrate with `BillingRepository` when client-side validation is sunset and refund reconciliation is required.
