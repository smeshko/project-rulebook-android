package com.rulebook.feature.purchase

import com.rulebook.core.model.ProductInfo
import com.rulebook.core.model.PurchaseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseUiStateTest {

    @Test
    fun `default state has correct initial values`() {
        val state = PurchaseUiState()

        assertTrue(state.products.isEmpty())
        assertEquals(0, state.currentBalance)
        assertTrue(state.isLoading)
        assertNull(state.purchaseState)
        assertNull(state.error)
    }

    @Test
    fun `state copy with products updates correctly`() {
        val products = listOf(
            ProductInfo("id1", "Title 1", "$0.99", 1),
            ProductInfo("id2", "Title 2", "$1.99", 3)
        )
        val state = PurchaseUiState().copy(products = products, isLoading = false)

        assertEquals(2, state.products.size)
        assertEquals(false, state.isLoading)
        assertEquals(0, state.currentBalance)
    }

    @Test
    fun `state copy with balance updates correctly`() {
        val state = PurchaseUiState().copy(currentBalance = 5)

        assertEquals(5, state.currentBalance)
        assertTrue(state.isLoading)
        assertTrue(state.products.isEmpty())
    }

    @Test
    fun `state copy with purchase state updates correctly`() {
        val purchaseState = PurchaseState.Processing("sku123")
        val state = PurchaseUiState().copy(purchaseState = purchaseState)

        assertEquals(purchaseState, state.purchaseState)
    }

    @Test
    fun `state copy with error updates correctly`() {
        val state = PurchaseUiState().copy(error = "Network error")

        assertEquals("Network error", state.error)
    }
}
