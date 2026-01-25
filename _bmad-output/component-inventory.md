# Component Inventory: Rulebook Android

**Generated:** 2026-01-22
**Design System:** Brutalist on Material 3

---

## Design System Components (core/designsystem)

### Buttons

| Component | File | Variants | Usage |
|-----------|------|----------|-------|
| `RulebookButton` | `component/RulebookButton.kt` | Primary, Secondary, Destructive | CTAs, form actions, navigation |

**Characteristics:**
- 48dp minimum touch target
- Sharp corners (0dp radius)
- 3dp black border
- 4dp offset shadow (primary/destructive)
- Black weight typography

### Cards

| Component | File | Variants | Usage |
|-----------|------|----------|-------|
| `RulebookCard` | `component/RulebookCard.kt` | Standard, Elevated | Content containers, data presentation |

**Characteristics:**
- 3dp black border
- Sharp corners (0dp radius)
- 4dp shadow (standard) / 8dp shadow (elevated)
- 16dp internal padding
- Optional click handling

### Floating Action Buttons

| Component | File | Variants | Usage |
|-----------|------|----------|-------|
| `RulebookFAB` | `component/RulebookFAB.kt` | Default (Camera) | Primary action per screen |

**Characteristics:**
- 56dp size (standard FAB)
- Pink fill with black icon
- 3dp border + 4dp shadow
- Default camera icon

### Headers

| Component | File | Variants | Usage |
|-----------|------|----------|-------|
| `RulebookHeaderBar` | `component/RulebookHeaderBar.kt` | Title ± back ± actions | Screen headers |

**Characteristics:**
- 24sp Black weight title
- 3dp bottom border
- Optional back button
- Status bar padding (notch-safe)

### Indicators

| Component | File | Variants | Usage |
|-----------|------|----------|-------|
| `CreditsDisplay` | `component/CreditsDisplay.kt` | Normal, Low, Empty | Credit balance overlay |

**Characteristics:**
- Pill shape (16dp radius)
- Semi-transparent backgrounds
- State-based coloring (black/orange/red)
- Warning icon for low/empty states

---

## Theme Tokens

### Colors (`theme/RulebookColors.kt`)

**Surface Palette:**
| Token | Light | Dark |
|-------|-------|------|
| Surface Primary | #FFFFFF | #1C1C1E |
| Surface Secondary | #FFF9F0 | #2C2C2E |
| Surface Tertiary | #F5E6D3 | #3A3A3C |

**Content Palette:**
| Token | Light | Dark |
|-------|-------|------|
| Content Primary | #000000 | #FFFFFF |
| Content Secondary | 70% opacity | 70% opacity |
| Content Tertiary | 40% opacity | 40% opacity |

**Accent Palette:**
| Token | Color | Usage |
|-------|-------|-------|
| Pink | Brand accent | Buttons, actions |
| Blue | Info | Setup section |
| Orange | Warning | Low credits |
| Yellow | Highlight | First round |
| Purple | Advanced | Advanced rules |
| Green | Success | Confirmations |
| Red | Error | Destructive, errors |

### Typography (`theme/RulebookTypography.kt`)

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display Large Title | 34sp | Bold | Hero text |
| Brutalist Title | 24sp | Black (900) | Screen headers |
| Brutalist Section Title | 16sp | Black | Section headers |
| Body | 17sp | Regular | Content text |
| Caption | 12sp | Regular | Secondary info |
| Button | 14sp | Black | Button labels |

### Spacing (`theme/RulebookSpacing.kt`)

Standard spacing scale for consistent padding/margins.

### Shapes (`theme/RulebookShapes.kt`)

All shapes: **0dp corner radius** (brutalist aesthetic)

---

## Feature Components

### Camera Feature (`feature/camera/components/`)

| Component | Purpose | States |
|-----------|---------|--------|
| `CaptureButton` | Shutter button | Enabled, Loading, Disabled |
| `FlashToggle` | Flash mode control | Off, On, Auto |
| `ZoomIndicator` | Current zoom level | Visible (fade out) |
| `FocusIndicator` | Tap-to-focus point | Animating → Hidden |
| `GalleryButton` | Photo picker access | Thumbnail preview |
| `CloseButton` | Exit camera | Default |
| `PermissionRationale` | Camera permission request | Dialog |
| `PermissionDenied` | Permission blocked | Settings link |

### Library Feature (`feature/library/components/`)

| Component | Purpose | States |
|-----------|---------|--------|
| `LibraryEmptyState` | No games yet | CTA to camera |

### Settings Feature (`feature/settings/components/`)

| Component | Purpose | States |
|-----------|---------|--------|
| `SettingsToggleRow` | Boolean setting | On/Off switch |
| `SettingsLinkRow` | Tappable action | Chevron indicator |
| `SettingsSectionHeader` | Section divider | Title only |
| `SettingsInfoRow` | Read-only info | Label + value |

### Onboarding Feature (`feature/onboarding/components/`)

| Component | Purpose | States |
|-----------|---------|--------|
| `OnboardingPageIndicator` | Page dots | Active/Inactive |
| `OnboardingPage1Content` | Value proposition | Static |
| `OnboardingPage2Content` | Getting started | Static |
| `OnboardingBottomSection` | Navigation controls | Skip/Next/Get Started |

---

## Component Usage Guidelines

### When to Use Each Button Variant

| Variant | Use Case | Example |
|---------|----------|---------|
| Primary | Main action on screen | "Scan a Game", "Get Started" |
| Secondary | Alternative action | "Cancel", "Skip" |
| Destructive | Data deletion | "Clear All Data" |

### When to Use Each Card Variant

| Variant | Use Case | Example |
|---------|----------|---------|
| Standard | Content display | Game cards, rule sections |
| Elevated | Emphasis/modals | Error states, confirmations |

### Reusability Matrix

| Component | Reusable | Feature-Specific |
|-----------|----------|------------------|
| RulebookButton | ✓ | |
| RulebookCard | ✓ | |
| RulebookFAB | ✓ | |
| RulebookHeaderBar | ✓ | |
| CreditsDisplay | ✓ | |
| CaptureButton | | Camera |
| FlashToggle | | Camera |
| LibraryEmptyState | | Library |
| SettingsToggleRow | | Settings (but generalizable) |

---

## Adding New Components

### Design System Component

1. Create in `core/designsystem/src/main/kotlin/.../component/`
2. Follow existing patterns (variants via sealed class/enum)
3. Use `RulebookTheme.colors`, `.typography`, `.spacing`
4. Add unit tests in `src/test/`
5. Document in this inventory

### Feature Component

1. Create in `feature/<name>/src/main/kotlin/.../components/`
2. Keep feature-specific, avoid cross-feature dependencies
3. Use design system components internally
4. Consider promotion to design system if reused
