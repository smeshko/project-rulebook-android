package com.rulebook.feature.purchase.components

/**
 * Sealed class representing the badge variants for product cards.
 *
 * Each subclass corresponds to a distinct marketing callout displayed
 * on a specific credit pack card.
 *
 * @property text The label text to display on the badge.
 * @property rotation Degrees to rotate the badge for visual effect.
 */
sealed class ProductBadge(
    val text: String,
    val rotation: Float
) {
    /** "Most Popular" badge shown on the 3-credit pack, tilted -5°. */
    data object MostPopular : ProductBadge(text = "Most Popular", rotation = -5f)

    /** "Best Value" badge shown on the 10-credit pack, no rotation. */
    data object BestValue : ProductBadge(text = "Best Value", rotation = 0f)
}

/**
 * Returns the appropriate [ProductBadge] for a given product ID, or null if no badge applies.
 *
 * - `credits_3` → [ProductBadge.MostPopular]
 * - `credits_10` → [ProductBadge.BestValue]
 * - All others → null
 *
 * @param productId The product identifier (SKU) from Google Play Billing.
 */
fun badgeForProduct(productId: String): ProductBadge? = when (productId) {
    "credits_3" -> ProductBadge.MostPopular
    "credits_10" -> ProductBadge.BestValue
    else -> null
}

/**
 * Returns true if the card for this product ID should appear visually elevated
 * (larger shadow) to create psychological anchoring.
 *
 * Only the middle option (3-credit pack) is elevated.
 *
 * @param productId The product identifier (SKU) from Google Play Billing.
 */
fun isElevatedProduct(productId: String): Boolean = productId == "credits_3"
