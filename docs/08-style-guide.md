# QR Guardian Style Guide

## 1. Visual Identity
Style direction: **Luminous Aura**.

QR Guardian should feel:
- Young
- Luminous
- Elegant
- Secure
- Clean

## 2. Color Palette
### Brand
```kotlin
val GuardianPrimary = Color(0xFFB8A3FF)   // Lavender Aura
val GuardianSecondary = Color(0xFFD9CCFF) // Soft Lavender
```

### Base Surfaces (Light)
```kotlin
val LightBackground = Color(0xFFF8F9FA)   // Soft White
val LightSurface = Color(0xFFFFFFFF)      // Cloud
val LightSurfaceVariant = Color(0xFFF2F3F5)
val LightTextPrimary = Color(0xFF171717)
val LightTextSecondary = Color(0xFF4B5563)
val LightTextMuted = Color(0xFF9CA3AF)
val LightBorder = Color(0xFFE5E7EB)
```

### Base Surfaces (Dark)
```kotlin
val DarkBackground = Color(0xFF14131A)
val DarkSurface = Color(0xFF1D1B25)
val DarkSurfaceVariant = Color(0xFF292634)
val DarkTextPrimary = Color(0xFFF7F5FF)
val DarkTextSecondary = Color(0xFFD2CCE6)
val DarkTextMuted = Color(0xFFA9A1BF)
val DarkBorder = Color(0xFF3A364A)
```

### Semantic Safety
```kotlin
val Safe = Color(0xFF10B981)      // Mint Safety
val Warning = Color(0xFFFBBF24)   // Peach Warning
val Danger = Color(0xFFF87171)    // Coral Alert
val Neutral = Color(0xFF9CA3AF)
```

Usage:
- `GuardianPrimary`: main CTA, active states, highlights.
- `LightBackground`/`DarkBackground`: global page background.
- `LightSurface`/`DarkSurface`: cards, sheets, elevated sections.
- `Safe` / `Warning` / `Danger`: security state communication.

## 3. Typography
Primary typeface: **Outfit**.

Current implementation status:
- Outfit is already bundled in `shared/src/commonMain/composeResources/font`.
- Included weights: `Regular`, `Medium`, `SemiBold`, `Bold`.

Weight strategy:
- Titles: `SemiBold`
- Body: `Regular`
- Supporting labels: `Medium`

Scale:
- Display Large: `28sp / SemiBold`
- Display Medium: `22sp / SemiBold`
- Display Small: `18sp / SemiBold`
- Body Large: `16sp / Regular`
- Body Medium: `14sp / Regular`
- Body Small: `12sp / Regular`
- Label Large: `15sp / Medium`
- Label Small: `12sp / Medium`

## 4. Spacing and Radius
```kotlin
// Spacing
4 / 8 / 16 / 24 / 32 / 48 dp system

// Radius
8 / 14 / 20 / 28 dp
```

Guidance:
- Keep generous vertical rhythm.
- Prefer soft rounded corners and pill actions.

## 5. Primary Button (Implemented)
Component: `QRGuardianPrimaryButton`

Visual spec:
- Container: `#B8A3FF`
- Content: white
- Shape: full pill (`RoundedCornerShape(50.dp)`)
- Elevation: subtle (`2.dp`, pressed `0.dp`)
- Padding: `24dp` horizontal / `14dp` vertical
- Text: `16sp`, `SemiBold`, `0.5sp` letter spacing

## 6. Intro Screen Direction
Current intro screen follows the reference layout:
- Top badge: “Tu escudo digital”
- Heading split line with highlighted “seguridad”
- Centered explanatory copy
- Large primary CTA: “Empezar a Escanear”

## 7. Visual Rules
- Keep layouts airy and bright.
- Avoid harsh blacks and extreme contrast.
- Use lavender as a controlled accent, not as full-surface fill.
- Keep safety colors calm and readable.
- Dangerous actions must never be the dominant CTA.

## 8. Brand Line
**QR Guardian**  
**Secure QR & barcode scanner**  
**Luminous Aura for iOS & Android**
