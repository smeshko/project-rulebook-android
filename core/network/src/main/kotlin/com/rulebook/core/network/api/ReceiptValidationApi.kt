package com.rulebook.core.network.api

import com.rulebook.core.network.model.CheckRefundRequest
import com.rulebook.core.network.model.CheckRefundResponse
import com.rulebook.core.network.model.ValidateReceiptRequest
import com.rulebook.core.network.model.ValidateReceiptResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReceiptValidationApi {

    @POST("api/v1/receipts/validate")
    suspend fun validateReceipt(@Body request: ValidateReceiptRequest): ValidateReceiptResponse

    @POST("api/v1/receipts/refund-status")
    suspend fun checkRefundStatus(@Body request: CheckRefundRequest): CheckRefundResponse
}
