package com.rulebook.feature.purchase.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistCard
import com.rulebook.core.designsystem.theme.RulebookTheme

private val SkeletonMinHeight = 160.dp
private val SkeletonCardShadow = 6.dp
private val ShimmerBarHeight = 20.dp
private val ShimmerBarHeightSmall = 14.dp
private const val ShimmerDurationMs = 900

/**
 * ProductCardSkeleton - Loading placeholder matching ProductCard dimensions.
 *
 * Displays animated shimmer rectangles while product prices are loading from Play Store.
 * Matches the 6dp shadow and min-height of the real ProductCard for layout consistency.
 *
 * @param modifier Modifier to be applied to the skeleton container.
 */
@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonShimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ShimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    val shimmerColor = lerp(
        start = MaterialTheme.colorScheme.surfaceVariant,
        stop = MaterialTheme.colorScheme.surface,
        fraction = shimmerAlpha
    )

    Box(
        modifier = modifier
            .brutalistCard(shadowOffset = SkeletonCardShadow)
            .background(MaterialTheme.colorScheme.surface)
            .heightIn(min = SkeletonMinHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RulebookTheme.spacing.md,
                    vertical = RulebookTheme.spacing.md
                )
        ) {
            // Credit count placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(ShimmerBarHeight)
                    .background(shimmerColor)
            )
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.xs))
            // "credits" label placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(ShimmerBarHeightSmall)
                    .background(shimmerColor)
            )
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
            // Divider placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(shimmerColor)
            )
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
            // Price placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(ShimmerBarHeight)
                    .background(shimmerColor)
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Product Card Skeleton - Light")
@Composable
private fun ProductCardSkeletonLightPreview() {
    RulebookTheme(darkTheme = false) {
        ProductCardSkeleton()
    }
}

@Preview(showBackground = true, name = "Product Card Skeleton - Dark")
@Composable
private fun ProductCardSkeletonDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ProductCardSkeleton()
    }
}
