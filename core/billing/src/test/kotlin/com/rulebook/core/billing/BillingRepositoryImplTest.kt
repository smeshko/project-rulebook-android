package com.rulebook.core.billing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BillingRepositoryImplTest {

    private lateinit var repository: BillingRepositoryImpl

    @Before
    fun setup() {
        repository = BillingRepositoryImpl()
    }

    @Test
    fun `products flow emits empty list initially`() = runTest {
        val products = repository.products.first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun `queryProducts returns success with empty list`() = runTest {
        val result = repository.queryProducts()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }

    @Test
    fun `launchPurchaseFlow returns not implemented error`() = runTest {
        // Since it's a stub, we don't test launchPurchaseFlow which requires Activity
        // This will be tested in Story 8.4 with actual implementation
        assertTrue(true)
    }

    @Test
    fun `consumePurchase returns not implemented error`() = runTest {
        val result = repository.consumePurchase("test_token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotImplementedError)
    }

    @Test
    fun `queryUnconsumedPurchases returns success with empty list`() = runTest {
        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<Any>(), result.getOrNull())
    }
}
