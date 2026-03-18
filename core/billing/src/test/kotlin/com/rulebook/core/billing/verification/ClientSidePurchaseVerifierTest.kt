package com.rulebook.core.billing.verification

import android.app.Activity
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClientSidePurchaseVerifierTest {

    private fun createVerifier(repo: BillingRepository) = ClientSidePurchaseVerifier(repo)

    @Test
    fun `successful consume for credits_1 returns 1 credit`() = runTest {
        val repo = FakeVerifierBillingRepository()
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-1", "credits_1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.credits)
        assertEquals("token-1", repo.consumedTokens.first())
    }

    @Test
    fun `successful consume for credits_3 returns 3 credits`() = runTest {
        val repo = FakeVerifierBillingRepository()
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-3", "credits_3")

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.credits)
    }

    @Test
    fun `successful consume for credits_10 returns 10 credits`() = runTest {
        val repo = FakeVerifierBillingRepository()
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-10", "credits_10")

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.credits)
    }

    @Test
    fun `failed consume returns failure and no credits`() = runTest {
        val repo = FakeVerifierBillingRepository(shouldFailConsume = true)
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-fail", "credits_3")

        assertTrue(result.isFailure)
        // Verify consume was attempted but no credits in result
        assertEquals("token-fail", repo.consumedTokens.first())
    }

    @Test
    fun `unknown product ID returns failure`() = runTest {
        val repo = FakeVerifierBillingRepository()
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("token-unknown", "credits_999")

        assertTrue(result.isFailure)
        assertIs<IllegalArgumentException>(result.exceptionOrNull())
    }

    @Test
    fun `consume is called before credits are resolved`() = runTest {
        // Verifier must consume first — even for unknown SKUs the consume happens before lookup
        // But our implementation does consume first, then resolves credits
        // Verify: if consume fails, credits lookup is never reached
        val repo = FakeVerifierBillingRepository(shouldFailConsume = true)
        val verifier = createVerifier(repo)

        val result = verifier.verifyAndConsume("fail-token", "credits_3")

        assertTrue(result.isFailure)
        // Consume was attempted (token recorded in failed list)
        assertEquals("fail-token", repo.consumedTokens.first())
        // But credits lookup was not attempted (repo.creditsLookupCount == 0)
        assertEquals(0, repo.creditsLookupCount)
    }
}

// ======================================================================
// Fake BillingRepository for verifier tests
// ======================================================================

class FakeVerifierBillingRepository(
    private val shouldFailConsume: Boolean = false
) : BillingRepository {

    val consumedTokens = mutableListOf<String>()
    var creditsLookupCount = 0

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>()

    override val products: Flow<List<ProductInfo>> = _products
    override val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>> = Result.success(emptyList())

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        consumedTokens.add(purchaseToken)
        return if (shouldFailConsume) {
            Result.failure(RuntimeException("Consume failed"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> =
        Result.success(emptyList())

    override suspend fun checkPendingPurchases(pendingToken: String): Result<PendingPurchaseResolution> =
        Result.success(PendingPurchaseResolution.NotFound)

    override fun creditsForProduct(productId: String): Int? {
        creditsLookupCount++
        return when (productId) {
            "credits_1" -> 1
            "credits_3" -> 3
            "credits_10" -> 10
            else -> null
        }
    }
}
