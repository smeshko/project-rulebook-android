# Component Library - Android Migration

This document provides specifications for all reusable UI components to implement in Android/Jetpack Compose.

---

## Component Overview

| Component | Category | Priority | Complexity |
|-----------|----------|----------|------------|
| RulebookButton | Buttons | P0 | Medium |
| RulebookIconButton | Buttons | P0 | Low |
| RulebookCard | Cards | P0 | High |
| RulebookEmptyState | Cards | P0 | Medium |
| RulebookFeatureCard | Cards | P1 | Medium |
| RulebookTextField | Forms | P2 | Medium |
| RulebookCheckbox | Forms | P2 | Low |
| RulebookToggle | Forms | P0 | Medium |
| RulebookPicker | Forms | P0 | Medium |
| RulebookListRow | Lists | P0 | Medium |
| RulebookBadge | Feedback | P0 | Low |
| RulebookProgressIndicator | Feedback | P0 | Medium |
| RulebookHeaderBar | Navigation | P0 | Medium |
| RulebookSectionHeader | Typography | P0 | Low |
| RulebookCollapsibleSection | Layout | P0 | High |
| RulebookTabView | Navigation | P0 | High |

---

## 1. RulebookButton

Primary action button with brutalist styling.

### Specifications

```
Dimensions:
- Height: 56dp (touch target)
- Padding: 20dp horizontal, 16dp vertical
- Min width: 120dp

Visual:
- Background: Configurable (default: brutalistPink)
- Border: 3dp solid black
- Shadow: 4dp offset (black rectangle behind)
- Corner radius: 0dp (sharp corners)
- Text: Uppercase, black weight (900)
- Font size: 14sp

States:
- Normal: Full shadow, scale 1.0
- Pressed: Shadow 2dp, scale 0.98
- Disabled: 40% opacity, no interaction
```

### Props/Parameters

```kotlin
@Composable
fun RulebookButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalistColors.pink,
    foregroundColor: Color = Color.Black, // Auto-calculated if null
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    autoContrastText: Boolean = true
)
```

### Animation

```kotlin
// Press animation
val scale by animateFloatAsState(
    targetValue = if (pressed) 0.98f else 1f,
    animationSpec = tween(150)
)
val shadowOffset by animateDpAsState(
    targetValue = if (pressed) 2.dp else 4.dp,
    animationSpec = tween(150)
)
```

---

## 2. RulebookIconButton

Compact button with icon only.

### Specifications

```
Size Variants:
- Small: 32x32dp
- Medium: 44x44dp (default)
- Large: 56x56dp

Visual:
- Background: Configurable (default: surfacePrimary)
- Border: 2dp solid black
- Shadow: 2dp offset
- Corner radius: 0dp
- Icon size: 50% of button size
```

### Props/Parameters

```kotlin
@Composable
fun RulebookIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: IconButtonSize = IconButtonSize.Medium,
    backgroundColor: Color = SurfaceColors.primary,
    foregroundColor: Color = ContentColors.primary,
    borderColor: Color = BorderColors.primary,
    contentDescription: String? = null
)

enum class IconButtonSize(val dp: Dp) {
    Small(32.dp),
    Medium(44.dp),
    Large(56.dp)
}
```

### Presets

```kotlin
// Factory methods for common icons
RulebookIconButton.back(onClick)
RulebookIconButton.close(onClick)
RulebookIconButton.settings(onClick)
RulebookIconButton.add(onClick)
RulebookIconButton.share(onClick)
```

---

## 3. RulebookCard

Game card for library grid display.

### Specifications

```
Dimensions:
- Min height: 200dp
- Aspect ratio: Flexible (grid determines width)
- Padding: 12dp internal

Visual:
- Background: surfacePrimary
- Border: 4dp solid black
- Shadow: 6dp offset
- Corner radius: 0dp

Content Layout:
+---------------------------+
|     Cover Pattern Area    |
|    (diagonal stripes,     |
|     dots, or plain)       |
|---------------------------|
|  GAME TITLE               |
|  [Players] [Time] [Diff]  |
+---------------------------+
```

### Props/Parameters

```kotlin
@Composable
fun RulebookCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    playerCount: String? = null,
    duration: String? = null,
    difficulty: String? = null,
    coverPattern: CoverPattern = CoverPattern.Plain,
    backgroundColor: Color = SurfaceColors.primary,
    borderWidth: Dp = 4.dp,
    shadowOffset: Dp = 6.dp
)

sealed class CoverPattern {
    object Plain : CoverPattern()
    data class DiagonalStripe(val colors: List<Color>) : CoverPattern()
    data class ColorDot(val color: Color) : CoverPattern()
    data class BorderAccent(val color: Color) : CoverPattern()
    data class GeometricCorner(val color: Color) : CoverPattern()
}
```

### Cover Pattern Implementation

```kotlin
// Diagonal stripes pattern
Canvas(modifier = Modifier.fillMaxSize()) {
    val stripeWidth = 20.dp.toPx()
    var x = -size.height
    while (x < size.width + size.height) {
        drawLine(
            color = stripeColor,
            start = Offset(x, 0f),
            end = Offset(x + size.height, size.height),
            strokeWidth = stripeWidth / 2
        )
        x += stripeWidth
    }
}
```

---

## 4. RulebookEmptyState

Empty state placeholder with icon, message, and optional action.

### Specifications

```
Layout:
- Centered content
- Icon: 120dp
- Spacing: 24dp between elements
- Max width: 300dp

Visual:
- Background: surfaceSecondary (optional)
- Icon color: contentSecondary
- Title: brutalistTitle font
- Description: body font, secondary color
```

### Props/Parameters

```kotlin
@Composable
fun RulebookEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = "GET STARTED",
    actionColor: Color = BrutalistColors.orange,
    tips: List<String> = emptyList(),
    onAction: (() -> Unit)? = null
)
```

### Tips Section

```kotlin
// Tips appear as bullet points below description
Column {
    tips.forEach { tip ->
        Row {
            Text("*", style = caption)
            Spacer(Modifier.width(8.dp))
            Text(tip, style = caption, color = contentSecondary)
        }
    }
}
```

---

## 5. RulebookFeatureCard

Feature highlight card for onboarding and paywall.

### Specifications

```
Dimensions:
- Min height: 120dp
- Aspect ratio: 1.2 (width:height)
- Padding: 16dp

Layout:
+------------------+
|  [Icon]          |
|  Title           |
|  Description     |
+------------------+

Visual:
- Background: surfacePrimary
- Border: 2dp solid black
- Shadow: 4dp offset
- Icon: 32dp, accent color
```

### Props/Parameters

```kotlin
@Composable
fun RulebookFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    iconColor: Color = BrutalistColors.orange,
    backgroundColor: Color = SurfaceColors.primary
)
```

### Presets

```kotlin
// Semantic factory methods
RulebookFeatureCard.instantScanning()
RulebookFeatureCard.clearInstructions()
RulebookFeatureCard.gameLibrary()
RulebookFeatureCard.offlineAccess()
RulebookFeatureCard.unlimitedScans()
```

---

## 6. RulebookToggle

Custom toggle switch with brutalist styling.

### Specifications

```
Dimensions:
- Width: 60dp
- Height: 32dp
- Knob size: 24dp

Visual:
- Track background: surfacePrimary (off) / brutalistGreen (on)
- Knob: surfacePrimary with black border
- Border: 3dp solid black
- Shadow: 4dp offset on track
```

### Props/Parameters

```kotlin
@Composable
fun RulebookToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onColor: Color = BrutalistColors.green,
    offColor: Color = SurfaceColors.primary,
    enabled: Boolean = true
)
```

### Animation

```kotlin
val knobOffset by animateDpAsState(
    targetValue = if (checked) 28.dp else 4.dp,
    animationSpec = tween(200, easing = EaseInOut)
)
```

---

## 7. RulebookPicker

Dropdown picker for selection options.

### Specifications

```
Dimensions:
- Height: 56dp
- Padding: 16dp horizontal

Visual:
- Background: surfacePrimary
- Border: 2dp solid black
- Shadow: 4dp offset
- Chevron icon on right
- Selected value displayed

Dropdown:
- Appears below picker
- Same border style
- Checkmark on selected item
```

### Props/Parameters

```kotlin
@Composable
fun <T> RulebookPicker(
    title: String,
    selectedValue: T,
    options: List<PickerOption<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = BrutalistColors.orange
)

data class PickerOption<T>(
    val value: T,
    val title: String,
    val subtitle: String? = null
)
```

---

## 8. RulebookListRow

List row for settings and menus.

### Specifications

```
Dimensions:
- Min height: 64dp
- Padding: 16dp horizontal

Layout:
+--------------------------------------------+
| [Icon Box] Title                [Accessory]|
|            Subtitle (optional)             |
+--------------------------------------------+

Accessories:
- None
- Chevron (navigation)
- Toggle
- Value text
- Button
```

### Props/Parameters

```kotlin
@Composable
fun RulebookListRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accessory: ListRowAccessory = ListRowAccessory.None,
    accentColor: Color = BrutalistColors.orange,
    backgroundColor: Color = SurfaceColors.primary
)

sealed class ListRowAccessory {
    object None : ListRowAccessory()
    object Chevron : ListRowAccessory()
    data class Toggle(val checked: Boolean, val onCheckedChange: (Boolean) -> Unit) : ListRowAccessory()
    data class Value(val text: String) : ListRowAccessory()
    data class Button(val text: String, val onClick: () -> Unit) : ListRowAccessory()
}
```

---

## 9. RulebookBadge

Metadata badge for game info.

### Specifications

```
Dimensions:
- Padding: 8dp horizontal, 4dp vertical

Visual:
- Background: accent color
- Border: 2dp solid black
- Text: Uppercase, bold, 11sp
- Corner radius: 0dp
```

### Props/Parameters

```kotlin
@Composable
fun RulebookBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalistColors.orange,
    textColor: Color = Color.Black
)
```

### Presets

```kotlin
// Semantic badges with predefined colors
RulebookBadge.playerCount("2-4") // Orange
RulebookBadge.duration("30 min") // Blue
RulebookBadge.difficulty("Medium") // Green
```

---

## 10. RulebookProgressIndicator

Loading and progress display.

### Specifications

```
Types:
1. Animated Dots - 3 pulsing dots
2. Linear Bar - Progress bar 0-100%
3. Indeterminate - Moving indicator

Sizes:
- Small: Dots 8dp, Bar height 8dp
- Medium: Dots 12dp, Bar height 12dp
- Large: Dots 16dp, Bar height 16dp

Visual:
- Color: brutalistBlue (default)
- Border: 2dp solid black
- Shadow: 4dp offset
```

### Props/Parameters

```kotlin
@Composable
fun RulebookProgressIndicator(
    modifier: Modifier = Modifier,
    type: ProgressType = ProgressType.AnimatedDots,
    progress: Float = 0f, // For LinearBar
    label: String? = null,
    size: ProgressSize = ProgressSize.Medium,
    color: Color = BrutalistColors.blue
)

enum class ProgressType {
    AnimatedDots,
    LinearBar,
    Indeterminate
}
```

### Dot Animation

```kotlin
// Staggered pulsing animation
val infiniteTransition = rememberInfiniteTransition()
dots.forEachIndexed { index, _ ->
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 200)
        )
    )
}
```

---

## 11. RulebookHeaderBar

Screen header with navigation.

### Specifications

```
Layout:
+--------------------------------------------+
| [Back]        TITLE           [Action]     |
+--------------------------------------------+
| ========================================== | <- 3dp border

Dimensions:
- Height: 56dp + safe area
- Border: 3dp bottom

Visual:
- Background: surfacePrimary
- Title: Centered, brutalistTitle font
```

### Props/Parameters

```kotlin
@Composable
fun RulebookHeaderBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    backAction: (() -> Unit)? = null,
    backLabel: String = "BACK",
    backColor: Color = BrutalistColors.orange,
    trailingContent: @Composable () -> Unit = {}
)
```

---

## 12. RulebookSectionHeader

Section header for content organization.

### Specifications

```
Variants:
- Standard: Title only
- Prominent: With background color
- Compact: Smaller spacing
- Card: For card content
- Section: With bottom divider

Visual:
- Font: brutalistSectionTitle (16sp, black)
- Optional leading/trailing icons
- Optional subtitle
```

### Props/Parameters

```kotlin
@Composable
fun RulebookSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    variant: SectionHeaderVariant = SectionHeaderVariant.Standard,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null
)

enum class SectionHeaderVariant {
    Standard,
    Prominent,
    Compact,
    Card,
    Section
}
```

### Presets

```kotlin
// Semantic section headers with colors
RulebookSectionHeader.ruleSection("Setup") // Orange
RulebookSectionHeader.setupSection("Setup") // Blue
RulebookSectionHeader.gameplaySection("Gameplay") // Green
RulebookSectionHeader.scoringSection("Scoring") // Purple
```

---

## 13. RulebookCollapsibleSection

Expandable/collapsible content section.

### Specifications

```
Layout:
+--------------------------------------------+
| [Accent] SECTION TITLE           [Chevron] |
+--------------------------------------------+
|                                            |
|    Content (when expanded)                 |
|                                            |
+--------------------------------------------+

Animation:
- Height: 300ms ease-in-out
- Chevron rotation: 180deg
- Shadow opacity change
```

### Props/Parameters

```kotlin
@Composable
fun RulebookCollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = BrutalistColors.orange,
    content: @Composable () -> Unit
)
```

### Section Types

```kotlin
enum class SectionType(val color: Color, val title: String) {
    GameOverview(BrutalistColors.orange, "GAME OVERVIEW"),
    SetupInstructions(BrutalistColors.blue, "SETUP"),
    FirstRoundWalkthrough(BrutalistColors.yellow, "FIRST ROUND"),
    AdvancedRules(BrutalistColors.purple, "ADVANCED RULES")
}
```

---

## 14. RulebookTabView

Bottom tab navigation.

### Specifications

```
Layout:
+--------------------------------------------+
|              Content Area                  |
+--------------------------------------------+
| [Tab1]      [FAB]      [Tab2]             |
+--------------------------------------------+

Dimensions:
- Tab bar height: 60dp + safe area
- FAB size: 56dp (elevated above bar)

Visual:
- Background: surfacePrimary
- Border: 3dp top
- Selected indicator: accent color underline
```

### Props/Parameters

```kotlin
@Composable
fun RulebookTabView(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Tab) -> Unit
)

enum class Tab(val icon: ImageVector, val label: String) {
    Library(Icons.Default.LibraryBooks, "Library"),
    Settings(Icons.Default.Settings, "Settings")
}
```

---

## Implementation Checklist

### Foundation (Week 1)
- [ ] Color tokens as Compose Color values
- [ ] Typography tokens as TextStyle
- [ ] Spacing values as Dp constants
- [ ] Shadow modifier extension
- [ ] Border modifier extension

### Core Components (Week 2)
- [ ] RulebookButton
- [ ] RulebookIconButton
- [ ] RulebookBadge
- [ ] RulebookSectionHeader

### Form Components (Week 3)
- [ ] RulebookToggle
- [ ] RulebookPicker
- [ ] RulebookListRow
- [ ] RulebookTextField (if needed)

### Complex Components (Week 4)
- [ ] RulebookCard
- [ ] RulebookEmptyState
- [ ] RulebookFeatureCard
- [ ] RulebookProgressIndicator

### Layout Components (Week 5)
- [ ] RulebookHeaderBar
- [ ] RulebookCollapsibleSection
- [ ] RulebookTabView

---

## Compose Utilities

### Brutalist Shadow Modifier

```kotlin
fun Modifier.brutalistShadow(
    offset: Dp = 4.dp,
    color: Color = Color.Black
) = this.drawBehind {
    drawRect(
        color = color,
        topLeft = Offset(offset.toPx(), offset.toPx()),
        size = size
    )
}
```

### Brutalist Border Modifier

```kotlin
fun Modifier.brutalistBorder(
    width: Dp = 3.dp,
    color: Color = Color.Black
) = this.border(width, color, RectangleShape)
```

### Press Animation Modifier

```kotlin
@Composable
fun Modifier.pressAnimation(
    pressedScale: Float = 0.98f
): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(150)
    )
    return this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                }
            )
        }
}
```
