# QR Guardian Style Guide

## 1. Visual Identity
Style direction: **Luminous Aura**.

The app should feel:
- Young
- Luminous
- Elegant
- Secure
- Clean

## 2. Color Palette
```kotlin
val GuardianPrimary = Color(0xFFB8A3FF)   // Lavender Aura
val LightBackground = Color(0xFFF8F9FA)   // Soft White
val LightSurface = Color(0xFFFFFFFF)      // Cloud

val Safe = Color(0xFF10B981)              // Mint Safety
val Warning = Color(0xFFFBBF24)           // Peach Warning
val Danger = Color(0xFFF87171)            // Coral Alert
```

Usage:
- `GuardianPrimary`: main brand identity, primary button, active states.
- `LightBackground`: global screen background.
- `LightSurface`: cards, elevated containers, dialogs.
- `Safe` / `Warning` / `Danger`: security status communication.

## 3. Theme Tokens
Current shared tokens implemented in `commonMain`:
- Primary: `#B8A3FF`
- Secondary: `#D9CCFF`
- Light background: `#F8F9FA`
- Light surface: `#FFFFFF`
- Dark mode adapted to purple-tinted neutrals for visual consistency.

## 4. Typography
Primary typeface target: **Outfit**.

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

Implementation note:
- `Outfit` is the design target.
- Until the actual font files are bundled in `composeResources/font`, the app uses a SansSerif fallback in code.

## 5. Visual Rules
- Keep layouts airy and bright.
- Avoid harsh blacks and extreme contrast.
- Use lavender as a controlled accent, not as a full-surface fill.
- Keep status colors pastel-friendly and calm.
- Preserve security clarity: dangerous actions must never be the dominant CTA.

## 6. Brand Line
**QR Guardian**  
**Secure QR & barcode scanner**  
**Luminous Aura for iOS & Android**
