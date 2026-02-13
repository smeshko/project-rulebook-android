package com.rulebook.core.model

/**
 * Represents a purchasable in-app product (credit pack).
 *
 * @property productId The Google Play product identifier (SKU)
 * @property title The localized product title from Play Store
 * @property price The formatted localized price string (e.g., "$0.99")
 * @property credits The number of credits awarded upon purchase
 */
data class ProductInfo(
    val productId: String,
    val title: String,
    val price: String,
    val credits: Int
)
