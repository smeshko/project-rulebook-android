package com.rulebook.core.billing.verification

import com.rulebook.core.common.Result as AppResult
import com.rulebook.core.data.repository.ReceiptRepository
import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult
import com.rulebook.core.model.ValidationStatus
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ServerSidePurchaseVerifierTest {

    private fun createVerifier(repo: ReceiptRepository) = ServerSidePurchaseVerifier(repo)

    @Test
    fun `VALID response returns success with correct credits`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Success(ValidationResult(ValidationStatus.VALID, creditsGranted = 3))
        )
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-valid", "credits_3")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.credits)
    }

    @Test
    fun `ALREADY_PROCESSED response returns success with credits`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Success(ValidationResult(ValidationStatus.ALREADY_PROCESSED, creditsGranted = 3))
        )
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-already", "credits_3")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.credits)
    }

    @Test
    fun `INVALID response returns failure with PurchaseValidationException`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Success(ValidationResult(ValidationStatus.INVALID, creditsGranted = 0))
        )
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-invalid", "credits_3")

        assertTrue(result.isFailure)
        assertIs<PurchaseValidationException>(result.exceptionOrNull())
    }

    @Test
    fun `network error returns failure with error message`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Error("Network error: Unable to connect")
        )
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-network-err", "credits_3")

        assertTrue(result.isFailure)
        assertEquals("Network error: Unable to connect", result.exceptionOrNull()?.message)
    }

    @Test
    fun `VALID response with 1 credit returns 1 credit`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Success(ValidationResult(ValidationStatus.VALID, creditsGranted = 1))
        )
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-1", "credits_1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.credits)
    }

    @Test
    fun `validatePurchase called with correct token and productId`() = runTest {
        val repo = FakeReceiptRepository(
            AppResult.Success(ValidationResult(ValidationStatus.VALID, creditsGranted = 10))
        )
        val verifier = createVerifier(repo)

        verifier.verifyAndConsume("my-token", "credits_10")

        assertEquals("my-token", repo.lastToken)
        assertEquals("credits_10", repo.lastProductId)
    }
}

// ======================================================================
// Fake ReceiptRepository for verifier tests
// ======================================================================

class FakeReceiptRepository(
    private val response: AppResult<ValidationResult>
) : ReceiptRepository {

    var lastToken: String? = null
    var lastProductId: String? = null

    override suspend fun validatePurchase(
        purchaseToken: String,
        productId: String
    ): AppResult<ValidationResult> {
        lastToken = purchaseToken
        lastProductId = productId
        return response
    }

    override suspend fun checkRefundStatus(
        purchaseTokens: List<String>
    ): AppResult<List<RefundStatus>> {
        return AppResult.Success(emptyList())
    }
}
