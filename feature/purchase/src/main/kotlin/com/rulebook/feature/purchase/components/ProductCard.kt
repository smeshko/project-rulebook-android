package com.rulebook.feature.purchase.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.BrutalistBorderWidth
import com.rulebook.core.designsystem.theme.BrutalistShadowOffsetMedium
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.ProductInfo

private val CardShadowNormal = 6.dp
private val CardShadowElevated = BrutalistShadowOffsetMedium // 8dp
private val CardShadowPressed = 2.dp
private val CardMinHeight = 160.dp
private val PressScaleTarget = 0.98f
private val NormalScaleTarget = 1.0f
private val BadgeOffsetX = 8.dp
private val BadgeOffsetY = (-12).dp

/**
 * ProductCard - Feature-specific brutalist card for a single credit pack.
 *
 * Displays the credit count, label, price, and an optional badge (e.g. "Most Popular").
 * Press animation scales down to 0.98f and reduces the shadow for tactile feedback.
 *
 * @param product The product data to display (id, title, price, credits).
 * @param badge Optional badge to overlay on the top-end corner of the card.
 * @param isElevated When true, applies an 8dp shadow for psychological anchoring (3-pack).
 * @param enabled When false, the card is non-interactive and rendered at 40% opacity.
 * @param onClick Callback invoked when the card is tapped.
 * @param modifier Modifier to be applied to the card container.
 */
@Composable
fun ProductCard(
    product: ProductInfo,
    badge: ProductBadge? = null,
    isElevated: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) PressScaleTarget else NormalScaleTarget,
        animationSpec = tween(durationMillis = 100),
        label = "pressScale"
    )

    val shadowOffset by animateFloatAsState(
        targetValue = when {
            !enabled -> CardShadowNormal.value
            isPressed -> CardShadowPressed.value
            isElevated -> CardShadowElevated.value
            else -> CardShadowNormal.value
        },
        animationSpec = tween(durationMillis = 100),
        label = "pressedShadow"
    )

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .then(if (!enabled) Modifier.alpha(0.4f) else Modifier)
            .brutalistShadow(offset = shadowOffset.dp)
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .heightIn(min = CardMinHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RulebookTheme.spacing.md, vertical = RulebookTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${product.credits}",
                style = RulebookTheme.typography.displayTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "credits",
                style = RulebookTheme.typography.callout,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
            HorizontalDivider(
                thickness = BrutalistBorderWidth,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
            Text(
                text = product.price,
                style = RulebookTheme.typography.displayTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (badge != null) {
            BadgeWithColor(
                badge = badge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = BadgeOffsetX)
                    .then(Modifier.graphicsLayer { translationY = BadgeOffsetY.toPx() })
            )
        }
    }
}

@Composable
private fun BadgeWithColor(
    badge: ProductBadge,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (badge) {
        is ProductBadge.MostPopular -> RulebookTheme.colors.pink
        is ProductBadge.BestValue -> RulebookTheme.colors.green
    }
    RulebookBadge(
        text = badge.text,
        backgroundColor = backgroundColor,
        rotation = badge.rotation,
        modifier = modifier
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Product Card 1-pack - Light")
@Composable
private fun ProductCard1PackLightPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCard(
            product = ProductInfo("credits_1", "1 Credit", "$0.99", 1),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Card 3-pack Most Popular - Light")
@Composable
private fun ProductCard3PackLightPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCard(
            product = ProductInfo("credits_3", "3 Credits", "$2.49", 3),
            badge = ProductBadge.MostPopular,
            isElevated = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Card 10-pack Best Value - Light")
@Composable
private fun ProductCard10PackLightPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCard(
            product = ProductInfo("credits_10", "10 Credits", "$6.99", 10),
            badge = ProductBadge.BestValue,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Card 3-pack - Dark")
@Composable
private fun ProductCard3PackDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ProductCard(
            product = ProductInfo("credits_3", "3 Credits", "$2.49", 3),
            badge = ProductBadge.MostPopular,
            isElevated = true,
            onClick = {}
        )
    }
}
