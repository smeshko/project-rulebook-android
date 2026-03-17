package com.rulebook.feature.purchase

import com.rulebook.feature.purchase.components.ProductBadge
import com.rulebook.feature.purchase.components.badgeForProduct
import com.rulebook.feature.purchase.components.isElevatedProduct
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductBadgeTest {

    @Test
    fun `credits_3 product gets MostPopular badge`() {
        val badge = badgeForProduct("credits_3")

        assertEquals(ProductBadge.MostPopular, badge)
    }

    @Test
    fun `credits_10 product gets BestValue badge`() {
        val badge = badgeForProduct("credits_10")

        assertEquals(ProductBadge.BestValue, badge)
    }

    @Test
    fun `credits_1 product gets no badge`() {
        val badge = badgeForProduct("credits_1")

        assertNull(badge)
    }

    @Test
    fun `unknown product id gets no badge`() {
        val badge = badgeForProduct("unknown_product")

        assertNull(badge)
    }

    @Test
    fun `credits_3 product is elevated`() {
        assertTrue(isElevatedProduct("credits_3"))
    }

    @Test
    fun `credits_1 product is not elevated`() {
        assertFalse(isElevatedProduct("credits_1"))
    }

    @Test
    fun `credits_10 product is not elevated`() {
        assertFalse(isElevatedProduct("credits_10"))
    }

    @Test
    fun `MostPopular badge has correct text and rotation`() {
        val badge = ProductBadge.MostPopular

        assertEquals("Most Popular", badge.text)
        assertEquals(-5f, badge.rotation)
    }

    @Test
    fun `BestValue badge has correct text and zero rotation`() {
        val badge = ProductBadge.BestValue

        assertEquals("Best Value", badge.text)
        assertEquals(0f, badge.rotation)
    }
}
