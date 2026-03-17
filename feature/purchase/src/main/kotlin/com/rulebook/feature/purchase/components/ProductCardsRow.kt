package com.rulebook.feature.purchase.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.ProductInfo

/**
 * ProductCardsRow - Horizontal row of three credit-pack product cards.
 *
 * Renders all available products as equal-width [ProductCard]s or, while loading,
 * renders three [ProductCardSkeleton] placeholders.
 *
 * Badges and elevation are automatically applied based on product ID:
 * - `credits_3` → "Most Popular" badge + elevated shadow
 * - `credits_10` → "Best Value" badge
 * - `credits_1` → no badge
 *
 * @param products The list of available [ProductInfo] items to display.
 * @param isLoading When true, skeleton placeholders are shown instead of real cards.
 * @param onProductSelected Callback invoked with the productId when a card is tapped.
 * @param modifier Modifier applied to the outer Row.
 */
@Composable
fun ProductCardsRow(
    products: List<ProductInfo>,
    isLoading: Boolean,
    onProductSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isLoading) {
            repeat(3) {
                ProductCardSkeleton(modifier = Modifier.weight(1f))
            }
        } else {
            products.sortedBy { it.credits }.forEach { product ->
                ProductCard(
                    product = product,
                    badge = badgeForProduct(product.productId),
                    isElevated = isElevatedProduct(product.productId),
                    onClick = { onProductSelected(product.productId) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

private val previewProducts = listOf(
    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
)

@Preview(showBackground = true, name = "Product Cards Row - Light")
@Composable
private fun ProductCardsRowLightPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCardsRow(
            products = previewProducts,
            isLoading = false,
            onProductSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Cards Row - Dark")
@Composable
private fun ProductCardsRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ProductCardsRow(
            products = previewProducts,
            isLoading = false,
            onProductSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Cards Row - Loading")
@Composable
private fun ProductCardsRowLoadingPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCardsRow(
            products = emptyList(),
            isLoading = true,
            onProductSelected = {}
        )
    }
}
