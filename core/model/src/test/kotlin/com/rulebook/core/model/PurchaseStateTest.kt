package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseStateTest {

    @Test
    fun `PurchaseState Idle is singleton`() {
        val state1 = PurchaseState.Idle
        val state2 = PurchaseState.Idle

        assertEquals(state1, state2)
    }

    @Test
    fun `PurchaseState Processing holds SKU`() {
        val state = PurchaseState.Processing("credit_pack_1")

        assertTrue(state is PurchaseState.Processing)
        assertEquals("credit_pack_1", state.sku)
    }

    @Test
    fun `PurchaseState Success holds credits added`() {
        val state = PurchaseState.Success(3)

        assertTrue(state is PurchaseState.Success)
        assertEquals(3, state.creditsAdded)
    }

    @Test
    fun `PurchaseState Error holds message`() {
        val state = PurchaseState.Error("Payment declined")

        assertTrue(state is PurchaseState.Error)
        assertEquals("Payment declined", state.message)
    }

    @Test
    fun `PurchaseState Pending is singleton`() {
        val state1 = PurchaseState.Pending
        val state2 = PurchaseState.Pending

        assertEquals(state1, state2)
    }

    @Test
    fun `PurchaseState sealed class hierarchy`() {
        val states: List<PurchaseState> = listOf(
            PurchaseState.Idle,
            PurchaseState.Processing("sku"),
            PurchaseState.Success(1),
            PurchaseState.Error("error"),
            PurchaseState.Pending
        )

        // All should be instances of PurchaseState
        states.forEach { state ->
            assertTrue(state is PurchaseState)
        }
    }
}
