package com.rulebook.core.billing

import android.app.Activity
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.VerificationResult
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.datastore.PendingPurchasePreferencesSource
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PendingPurchaseCheckerTest {

    private lateinit var fakePendingPrefs: FakeCheckerPendingPrefs
    private lateinit var fakeBillingRepository: FakeCheckerBillingRepository
    private lateinit var fakePurchaseVerifier: FakeCheckerPurchaseVerifier
    private lateinit var fakeCreditRepository: FakeCheckerCreditRepository
    private lateinit var checker: PendingPurchaseChecker

    @Before
    fun setup() {
        fakePendingPrefs = FakeCheckerPendingPrefs()
        fakeBillingRepository = FakeCheckerBillingRepository()
        fakePurchaseVerifier = FakeCheckerPurchaseVerifier()
        fakeCreditRepository = FakeCheckerCreditRepository()
        checker = PendingPurchaseChecker(
            pendingPurchasePrefs = fakePendingPrefs,
            billingRepository = fakeBillingRepository,
            purchaseVerifier = fakePurchaseVerifier,
            creditRepository = fakeCreditRepository
        )
    }

    @Test
    fun `checkAndResolve returns NoPendingPurchase when no token stored`() = runTest {
        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.NoPendingPurchase>(result)
        assertEquals(0, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `checkAndResolve resolves Purchased and delivers credits`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.resolution = PendingPurchaseResolution.Purchased("pending-token", "credits_3")

        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.Resolved>(result)
        assertEquals(3, result.creditsAdded)
        assertEquals("credits_3", result.productId)
        assertEquals(3, fakeCreditRepository.addedCredits)
        assertNull(fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkAndResolve returns StillPending when purchase not yet approved`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.resolution = PendingPurchaseResolution.StillPending

        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.StillPending>(result)
        assertEquals(0, fakeCreditRepository.addedCredits)
        assertEquals("pending-token", fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkAndResolve returns Cancelled and clears token when purchase not found`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.resolution = PendingPurchaseResolution.NotFound

        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.Cancelled>(result)
        assertEquals(0, fakeCreditRepository.addedCredits)
        assertNull(fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkAndResolve returns CheckFailed when billing query fails`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.shouldFail = true

        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.CheckFailed>(result)
        assertEquals(0, fakeCreditRepository.addedCredits)
        assertEquals("pending-token", fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkAndResolve returns CheckFailed when verification fails`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.resolution = PendingPurchaseResolution.Purchased("pending-token", "credits_3")
        fakePurchaseVerifier.shouldFail = true

        val result = checker.checkAndResolve()

        assertIs<PendingCheckResult.CheckFailed>(result)
        assertEquals(0, fakeCreditRepository.addedCredits)
    }
}

// ======================================================================
// Fake dependencies for PendingPurchaseCheckerTest
// ======================================================================

class FakeCheckerPendingPrefs : PendingPurchasePreferencesSource {
    var storedToken: String? = null
    var storedProductId: String? = null

    override val pendingPurchaseToken: Flow<String?>
        get() = flow { emit(storedToken) }
    override val pendingPurchaseProductId: Flow<String?>
        get() = flow { emit(storedProductId) }

    override suspend fun setPendingPurchase(token: String, productId: String) {
        storedToken = token
        storedProductId = productId
    }

    override suspend fun clearPendingPurchase() {
        storedToken = null
        storedProductId = null
    }
}

class FakeCheckerBillingRepository : BillingRepository {
    var resolution: PendingPurchaseResolution = PendingPurchaseResolution.NotFound
    var shouldFail = false

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    override val products: Flow<List<ProductInfo>> = _products
    override val purchaseUpdates: SharedFlow<PurchaseUpdate> = MutableSharedFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>> = Result.success(emptyList())

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> =
        Result.success(emptyList())

    override suspend fun checkPendingPurchases(pendingToken: String): Result<PendingPurchaseResolution> {
        if (shouldFail) return Result.failure(RuntimeException("Query failed"))
        return Result.success(resolution)
    }

    override fun creditsForProduct(productId: String): Int? = when (productId) {
        "credits_1" -> 1
        "credits_3" -> 3
        "credits_10" -> 10
        else -> null
    }
}

class FakeCheckerPurchaseVerifier : PurchaseVerifier {
    var shouldFail = false

    override suspend fun verifyAndConsume(
        purchaseToken: String,
        productId: String
    ): Result<VerificationResult> {
        if (shouldFail) return Result.failure(RuntimeException("Consume failed"))
        val credits = when (productId) {
            "credits_1" -> 1
            "credits_3" -> 3
            "credits_10" -> 10
            else -> return Result.failure(IllegalArgumentException("Unknown product: $productId"))
        }
        return Result.success(VerificationResult(credits))
    }
}

class FakeCheckerCreditRepository : CreditRepository {
    var addedCredits = 0
    private val _balance = MutableStateFlow(0)
    override val creditBalance: Flow<Int> = _balance

    override suspend fun awardInitialCredits(amount: Int): Boolean = true
    override suspend fun deductCredit(): Boolean = true
    override suspend fun hasCredits(): Boolean = true

    override suspend fun addCredits(amount: Int): Boolean {
        addedCredits += amount
        _balance.value += amount
        return true
    }
}
