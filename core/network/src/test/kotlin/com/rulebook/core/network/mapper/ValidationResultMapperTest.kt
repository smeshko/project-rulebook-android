package com.rulebook.core.network.mapper

import com.rulebook.core.model.ValidationStatus
import com.rulebook.core.network.model.CheckRefundResponse
import com.rulebook.core.network.model.ValidateReceiptResponse
import org.junit.Test
import kotlin.test.assertEquals

class ValidationResultMapperTest {

    // --- ValidateReceiptResponse.toDomain() ---

    @Test
    fun `maps valid status string to VALID enum`() {
        val response = ValidateReceiptResponse(status = "valid", creditsGranted = 3)
        val result = response.toDomain()
        assertEquals(ValidationStatus.VALID, result.status)
        assertEquals(3, result.creditsGranted)
    }

    @Test
    fun `maps invalid status string to INVALID enum`() {
        val response = ValidateReceiptResponse(status = "invalid")
        val result = response.toDomain()
        assertEquals(ValidationStatus.INVALID, result.status)
    }

    @Test
    fun `maps already_processed status string to ALREADY_PROCESSED enum`() {
        val response = ValidateReceiptResponse(status = "already_processed", creditsGranted = 0)
        val result = response.toDomain()
        assertEquals(ValidationStatus.ALREADY_PROCESSED, result.status)
    }

    @Test
    fun `maps unknown status string to INVALID enum as fallback`() {
        val response = ValidateReceiptResponse(status = "unknown_future_status")
        val result = response.toDomain()
        assertEquals(ValidationStatus.INVALID, result.status)
    }

    @Test
    fun `maps null creditsGranted to 0`() {
        val response = ValidateReceiptResponse(status = "valid", creditsGranted = null)
        val result = response.toDomain()
        assertEquals(0, result.creditsGranted)
    }

    @Test
    fun `maps non-null creditsGranted correctly`() {
        val response = ValidateReceiptResponse(status = "valid", creditsGranted = 10)
        val result = response.toDomain()
        assertEquals(10, result.creditsGranted)
    }

    // --- CheckRefundResponse.toDomain() ---

    @Test
    fun `maps refund status dto with isRefunded true`() {
        val dto = CheckRefundResponse.RefundStatusDto(purchaseToken = "tok_abc", isRefunded = true)
        val result = dto.toDomain()
        assertEquals("tok_abc", result.purchaseToken)
        assertEquals(true, result.isRefunded)
    }

    @Test
    fun `maps refund status dto with isRefunded false`() {
        val dto = CheckRefundResponse.RefundStatusDto(purchaseToken = "tok_xyz", isRefunded = false)
        val result = dto.toDomain()
        assertEquals("tok_xyz", result.purchaseToken)
        assertEquals(false, result.isRefunded)
    }

    @Test
    fun `maps CheckRefundResponse list correctly`() {
        val response = CheckRefundResponse(
            refundStatuses = listOf(
                CheckRefundResponse.RefundStatusDto(purchaseToken = "tok1", isRefunded = true),
                CheckRefundResponse.RefundStatusDto(purchaseToken = "tok2", isRefunded = false),
            )
        )
        val result = response.toDomain()
        assertEquals(2, result.size)
        assertEquals("tok1", result[0].purchaseToken)
        assertEquals(true, result[0].isRefunded)
        assertEquals("tok2", result[1].purchaseToken)
        assertEquals(false, result[1].isRefunded)
    }

    @Test
    fun `maps empty CheckRefundResponse list to empty list`() {
        val response = CheckRefundResponse(refundStatuses = emptyList())
        val result = response.toDomain()
        assertEquals(0, result.size)
    }
}
