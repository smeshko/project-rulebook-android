# Product Cards with Badges System

**Date:** 2026-03-15
**Related Files:** `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/components/`

## Overview

Implemented a reusable product card system for displaying credit pack options in the purchase paywall. Establishes a pattern for badge management and psychological anchoring through elevation styling. Products are fetched from Google Play Billing with real localized pricing.

## What Was Built

The product cards system comprises four primary composables and a badge mapping pattern:

- **ProductCard** - Feature-specific brutalist card displaying credit amount, price, and optional badge with press animations
- **ProductCardSkeleton** - Loading placeholder with shimmer animation matching card dimensions
- **RulebookBadge** - Reusable badge composable with configurable color and rotation
- **ProductCardsRow** - Container composable that manages card layout, loading state, and product selection
- **ProductBadge** sealed class - Immutable badge type definitions with pure mapping functions

## Technical Implementation

### Key Files

- `components/ProductCard.kt` - Main card composable with animation and badge rendering
- `components/ProductCardSkeleton.kt` - Loading state with shimmer animation
- `components/RulebookBadge.kt` - Reusable badge component
- `components/ProductBadge.kt` - Badge type definitions and mapping functions
- `components/ProductCardsRow.kt` - Row container with automatic badge/elevation assignment
- `ProductBadgeTest.kt` - Comprehensive unit tests for badge logic

### Key Patterns

**Sealed Class + Pure Functions Pattern**
The `ProductBadge` sealed class defines badge variants, with pure utility functions (`badgeForProduct`, `isElevatedProduct`) that map product IDs to badges and elevation. This pattern makes the mapping logic testable without requiring mocks or dependencies.

```kotlin
sealed class ProductBadge(val text: String, val rotation: Float) {
    data object MostPopular : ProductBadge("Most Popular", -5f)
    data object BestValue : ProductBadge("Best Value", 0f)
}

fun badgeForProduct(productId: String): ProductBadge? = when (productId) {
    "credits_3" -> ProductBadge.MostPopular
    "credits_10" -> ProductBadge.BestValue
    else -> null
}
```

**Animation Layer Pattern**
ProductCard uses Compose's `animateFloatAsState` to animate scale and shadow independently. This provides tactile feedback on press while maintaining a smooth, coordinated visual transition.

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1.0f,
    animationSpec = tween(100)
)
val shadowOffset by animateFloatAsState(
    targetValue = when {
        isPressed -> 2.dp  // Collapse shadow on press
        isElevated -> 8.dp // Elevated shadow for 3-pack
        else -> 6.dp       // Normal shadow
    },
    animationSpec = tween(100)
)
```

**Skeleton + Shimmer Pattern**
ProductCardSkeleton uses `InfiniteTransition` with color lerp to create a shimmer effect. The skeleton matches the real card's layout structure (credit count, label, divider, price) for seamless transition when loading completes.

### Code Examples

**Using ProductCardsRow (consumer code)**
```kotlin
ProductCardsRow(
    products = viewModel.products,
    isLoading = viewModel.isLoading,
    onProductSelected = { productId ->
        viewModel.onProductSelected(productId)
    },
    modifier = Modifier.fillMaxWidth()
)
```

**Integrating with PurchaseScreen**
```kotlin
// Badge and elevation are applied automatically based on product ID
ProductCardsRow(
    products = products.sortedBy { it.credits },
    isLoading = isLoading,
    onProductSelected = { selectedProductId ->
        viewModel.onProductSelected(selectedProductId)
    }
)
```

## How to Use

### 1. Display Product Cards
Use `ProductCardsRow` to display all products with automatic badge and elevation assignment:

```kotlin
ProductCardsRow(
    products = myProducts,  // List<ProductInfo> from ViewModel
    isLoading = loadingState,
    onProductSelected = { productId -> handleSelection(productId) }
)
```

### 2. Extend Badge System
To add a new badge type:
1. Add new `data object` to `ProductBadge` sealed class
2. Add new case to `badgeForProduct()` function
3. Update `BadgeWithColor()` composable to map to theme color
4. Add test case to `ProductBadgeTest`

### 3. Customize Card Styling
ProductCard shadow, scale, and spacing values are defined as constants at the top of the file:
- `CardShadowNormal = 6.dp`
- `CardShadowElevated = 8.dp`
- `PressScaleTarget = 0.98f`

### 4. Handle Loading State
Show skeleton placeholders automatically while prices load:

```kotlin
ProductCardsRow(
    products = emptyList(),
    isLoading = true,  // Shows 3 shimmer placeholders
    onProductSelected = {}
)
```

## Configuration

| Parameter | Type | Values | Purpose |
|-----------|------|--------|---------|
| `credits_3` elevation | Boolean | `true` (8dp shadow) | Psychological anchoring of middle option |
| `credits_3` badge | ProductBadge | `MostPopular` with -5° rotation | Market psychology |
| `credits_10` badge | ProductBadge | `BestValue` (no rotation) | Clear value signaling |
| `credits_1` badge | ProductBadge | `null` (no badge) | Baseline option |
| Press scale | Float | `0.98f` | Tactile feedback magnitude |
| Shimmer duration | Int | `900ms` | Loading placeholder animation |

## Architecture & Design Decisions

**Why Sealed Class + Pure Functions?**
- Sealed class ensures type safety (only MostPopular/BestValue are valid)
- Pure functions are deterministic and testable without mocks
- Easy to understand the complete set of badge types at a glance
- Pattern can be extended by adding new `data object` instances

**Why Separate ProductCard and ProductCardSkeleton?**
- Loading state needs different layout logic (shimmer bars vs content)
- Keeps components focused and testable in isolation
- Skeleton matches card dimensions for seamless layout during loading

**Why Elevation on 3-pack?**
Research shows users often choose the middle option due to anchoring bias. Subtle visual elevation (8dp shadow vs 6dp) reinforces this without being manipulative.

**Why Rotate Only MostPopular Badge?**
Slight rotation (-5°) draws the eye and indicates visual hierarchy. BestValue badge uses no rotation to maintain clarity for value-conscious shoppers.

## Notes and Future Considerations

- **Product sorting**: ProductCardsRow sorts products by credits count automatically. Change this in the `sortedBy { it.credits }` call if different ordering is needed.
- **Price formatting**: Prices come from Play Store `ProductDetails.oneTimePurchaseOfferDetails.formattedPrice` and are pre-localized. No additional formatting needed.
- **Accessibility**: Badges use `color = Color.Black` for high contrast. Ensure text sizing meets WCAG standards (currently using `brutalistButtonText` style).
- **Extensibility**: To add a fourth product pack (e.g., a premium tier), simply add it to the badge mapping and theme colors.
- **Testing**: Badge mapping logic is pure and fully tested. ProductCard press animation can be tested via `assertIsPressedAsState()` in future test cases.
