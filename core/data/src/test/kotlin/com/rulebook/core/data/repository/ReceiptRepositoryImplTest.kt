package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.ValidationStatus
import com.rulebook.core.network.api.ReceiptValidationApi
import com.rulebook.core.network.model.CheckRefundRequest
import com.rulebook.core.network.model.CheckRefundResponse
import com.rulebook.core.network.model.CreditBalanceResponse
import com.rulebook.core.network.model.ValidateReceiptRequest
import com.rulebook.core.network.model.ValidateReceiptResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReceiptRepositoryImplTest {

    private lateinit var fakeApi: FakeReceiptValidationApi
    private lateinit var repository: ReceiptRepositoryImpl

    @Before
    fun setup() {
        fakeApi = FakeReceiptValidationApi()
        repository = ReceiptRepositoryImpl(api = fakeApi, packageName = "com.rulebook.app")
    }

    // --- validatePurchase success paths ---

    @Test
    fun `validatePurchase with valid status returns Result Success with VALID`() = runTest {
        fakeApi.validateReceiptResult = ValidateReceiptResponse(status = "valid", creditsGranted = 5)

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Success<*>>(result)
        val data = (result as Result.Success).data
        assertEquals(ValidationStatus.VALID, data.status)
        assertEquals(5, data.creditsGranted)
    }

    @Test
    fun `validatePurchase with already_processed status returns ALREADY_PROCESSED`() = runTest {
        fakeApi.validateReceiptResult = ValidateReceiptResponse(status = "already_processed", creditsGranted = 0)

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Success<*>>(result)
        val data = (result as Result.Success).data
        assertEquals(ValidationStatus.ALREADY_PROCESSED, data.status)
        assertEquals(0, data.creditsGranted)
    }

    @Test
    fun `validatePurchase with invalid status returns INVALID`() = runTest {
        fakeApi.validateReceiptResult = ValidateReceiptResponse(status = "invalid")

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Success<*>>(result)
        val data = (result as Result.Success).data
        assertEquals(ValidationStatus.INVALID, data.status)
        assertEquals(0, data.creditsGranted)
    }

    @Test
    fun `validatePurchase with null creditsGranted defaults to 0`() = runTest {
        fakeApi.validateReceiptResult = ValidateReceiptResponse(status = "valid", creditsGranted = null)

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Success<*>>(result)
        assertEquals(0, (result as Result.Success).data.creditsGranted)
    }

    @Test
    fun `validatePurchase sends correct request with packageName`() = runTest {
        fakeApi.validateReceiptResult = ValidateReceiptResponse(status = "valid", creditsGranted = 1)

        repository.validatePurchase("tok_abc", "prod_xyz")

        val captured = fakeApi.lastValidateRequest
        assertEquals("tok_abc", captured?.purchaseToken)
        assertEquals("prod_xyz", captured?.productId)
        assertEquals("com.rulebook.app", captured?.packageName)
    }

    // --- validatePurchase error paths ---

    @Test
    fun `validatePurchase with SocketTimeoutException returns Result Error with timeout message`() = runTest {
        fakeApi.validateReceiptError = SocketTimeoutException("timeout")

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Error>(result)
        assertEquals("The request took too long. Please try again.", (result as Result.Error).message)
    }

    @Test
    fun `validatePurchase with UnknownHostException returns Result Error with no internet message`() = runTest {
        fakeApi.validateReceiptError = UnknownHostException("no host")

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Error>(result)
        assertEquals("No internet connection. Please check your network.", (result as Result.Error).message)
    }

    @Test
    fun `validatePurchase with HttpException 500 returns Result Error with server error message`() = runTest {
        fakeApi.validateReceiptError = HttpException(Response.error<Any>(500, "".toResponseBody(null)))

        val result = repository.validatePurchase("token123", "product_a")

        assertIs<Result.Error>(result)
        assertEquals("Server error. Please try again later.", (result as Result.Error).message)
    }

    // --- checkRefundStatus success paths ---

    @Test
    fun `checkRefundStatus returns list of RefundStatus on success`() = runTest {
        fakeApi.checkRefundStatusResult = CheckRefundResponse(
            refundStatuses = listOf(
                CheckRefundResponse.RefundStatusDto(purchaseToken = "token1", isRefunded = true),
                CheckRefundResponse.RefundStatusDto(purchaseToken = "token2", isRefunded = false),
            )
        )

        val result = repository.checkRefundStatus(listOf("token1", "token2"))

        assertIs<Result.Success<*>>(result)
        val data = (result as Result.Success).data
        assertEquals(2, data.size)
        assertEquals("token1", data[0].purchaseToken)
        assertEquals(true, data[0].isRefunded)
        assertEquals("token2", data[1].purchaseToken)
        assertEquals(false, data[1].isRefunded)
    }

    @Test
    fun `checkRefundStatus returns empty list when response is empty`() = runTest {
        fakeApi.checkRefundStatusResult = CheckRefundResponse(refundStatuses = emptyList())

        val result = repository.checkRefundStatus(emptyList())

        assertIs<Result.Success<*>>(result)
        assertEquals(0, (result as Result.Success).data.size)
    }

    @Test
    fun `checkRefundStatus sends correct request with purchase tokens`() = runTest {
        fakeApi.checkRefundStatusResult = CheckRefundResponse(
            refundStatuses = listOf(
                CheckRefundResponse.RefundStatusDto(purchaseToken = "tok1", isRefunded = false),
            )
        )

        repository.checkRefundStatus(listOf("tok1", "tok2"))

        val captured = fakeApi.lastCheckRefundRequest
        assertEquals(listOf("tok1", "tok2"), captured?.purchaseTokens)
    }

    // --- checkRefundStatus error paths ---

    @Test
    fun `checkRefundStatus with UnknownHostException returns Result Error with no internet message`() = runTest {
        fakeApi.checkRefundStatusError = UnknownHostException("no host")

        val result = repository.checkRefundStatus(listOf("token1"))

        assertIs<Result.Error>(result)
        assertEquals("No internet connection. Please check your network.", (result as Result.Error).message)
    }

    @Test
    fun `checkRefundStatus with SocketTimeoutException returns Result Error with timeout message`() = runTest {
        fakeApi.checkRefundStatusError = SocketTimeoutException("timeout")

        val result = repository.checkRefundStatus(listOf("token1"))

        assertIs<Result.Error>(result)
        assertEquals("The request took too long. Please try again.", (result as Result.Error).message)
    }
}

class FakeReceiptValidationApi : ReceiptValidationApi {
    var validateReceiptResult: ValidateReceiptResponse? = null
    var validateReceiptError: Exception? = null
    var lastValidateRequest: ValidateReceiptRequest? = null

    var checkRefundStatusResult: CheckRefundResponse? = null
    var checkRefundStatusError: Exception? = null
    var lastCheckRefundRequest: CheckRefundRequest? = null

    override suspend fun validateReceipt(request: ValidateReceiptRequest): ValidateReceiptResponse {
        lastValidateRequest = request
        validateReceiptError?.let { throw it }
        return validateReceiptResult ?: error("No validateReceiptResult configured")
    }

    override suspend fun checkRefundStatus(request: CheckRefundRequest): CheckRefundResponse {
        lastCheckRefundRequest = request
        checkRefundStatusError?.let { throw it }
        return checkRefundStatusResult ?: error("No checkRefundStatusResult configured")
    }

    var getBalanceResult: CreditBalanceResponse? = null
    var getBalanceError: Exception? = null

    override suspend fun getBalance(): CreditBalanceResponse {
        getBalanceError?.let { throw it }
        return getBalanceResult ?: error("No getBalanceResult configured")
    }
}
