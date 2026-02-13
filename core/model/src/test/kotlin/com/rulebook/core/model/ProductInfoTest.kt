package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProductInfoTest {

    @Test
    fun `ProductInfo creation with valid data`() {
        val productInfo = ProductInfo(
            productId = "credit_pack_1",
            title = "1 Credit Pack",
            price = "$0.99",
            credits = 1
        )

        assertEquals("credit_pack_1", productInfo.productId)
        assertEquals("1 Credit Pack", productInfo.title)
        assertEquals("$0.99", productInfo.price)
        assertEquals(1, productInfo.credits)
    }

    @Test
    fun `ProductInfo equality comparison`() {
        val product1 = ProductInfo("id1", "Title", "$1.00", 1)
        val product2 = ProductInfo("id1", "Title", "$1.00", 1)
        val product3 = ProductInfo("id2", "Title", "$1.00", 1)

        assertEquals(product1, product2)
        assert(product1 != product3)
    }

    @Test
    fun `ProductInfo copy with modifications`() {
        val original = ProductInfo("id1", "Original", "$1.00", 1)
        val modified = original.copy(price = "$1.50")

        assertEquals("$1.50", modified.price)
        assertEquals(original.productId, modified.productId)
        assertEquals(original.title, modified.title)
        assertEquals(original.credits, modified.credits)
    }
}
