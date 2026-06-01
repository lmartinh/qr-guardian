# QR Guardian Style Guide

## 1. Visual Identity
QR Guardian uses a **minimal tech / security mobile app** style.

The visual identity should communicate:
- Security
- Trust
- Speed
- Simplicity
- Technology

Design principles:
- Clean layouts with strong hierarchy
- Clear status feedback for safety decisions
- Modern, understated UI with minimal visual noise
- Professional look suitable for a portfolio project

## 2. Color Palette
Primary brand colors:

```kotlin
val GuardianPrimary = Color(0xFF00D6C9)
val GuardianSecondary = Color(0xFF1D8CFF)
```

Usage guidance:
- `GuardianPrimary`: primary buttons, active UI elements, scanner frame, scan line, and highlighted icons.
- `GuardianSecondary`: secondary accents, subtle gradients, and complementary interactive elements.

## 3. Dark Mode
Dark mode is the **main visual identity** of QR Guardian.

```kotlin
val DarkBackground = Color(0xFF07111F)
val DarkSurface = Color(0xFF0D1B2E)
val DarkSurfaceVariant = Color(0xFF13263D)
val DarkTextPrimary = Color(0xFFF4F7FB)
val DarkTextSecondary = Color(0xFFAAB6C5)
val DarkTextMuted = Color(0xFF6F7F91)
val DarkBorder = Color(0xFF22364F)
```

Recommended usage:
- `DarkBackground`: main app background.
- `DarkSurface`: cards, sheets, and elevated containers.
- `DarkSurfaceVariant`: chips, secondary panels, and subtle grouped regions.
- `DarkTextPrimary`: primary text and key labels.
- `DarkTextSecondary`: supporting text and metadata.
- `DarkTextMuted`: placeholders and low-priority labels.
- `DarkBorder`: subtle borders and separators.

## 4. Light Mode
Light mode should feel clean, calm, and professional.

```kotlin
val LightBackground = Color(0xFFF7FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEFF5FA)
val LightTextPrimary = Color(0xFF07111F)
val LightTextSecondary = Color(0xFF4B5B6B)
val LightTextMuted = Color(0xFF8A98A8)
val LightBorder = Color(0xFFDCE6EF)
```

Usage guidance:
- Keep strong contrast for readability.
- Use `LightSurfaceVariant` for gentle grouping, not heavy blocks.
- Use borders sparingly to preserve a modern, airy look.

## 5. Semantic Colors
Scan safety states:

```kotlin
val Safe = Color(0xFF1FD67A)
val SafeContainerDark = Color(0xFF0D2E22)
val SafeContainerLight = Color(0xFFE8FFF3)

val Warning = Color(0xFFFFB84D)
val WarningContainerDark = Color(0xFF35260B)
val WarningContainerLight = Color(0xFFFFF4DD)

val Danger = Color(0xFFFF5C5C)
val DangerContainerDark = Color(0xFF361414)
val DangerContainerLight = Color(0xFFFFEAEA)

val Neutral = Color(0xFF8A98A8)
```

State usage:
- `Safe`: verified or trusted result.
- `Warning`: suspicious or unclear result.
- `Danger`: malicious or risky result.
- `Neutral`: unknown, pending, or informational result.

## 6. Typography
Recommended primary font: **Inter**.

Type scale:
- Title Large: `28sp / Bold`
- Title Medium: `22sp / SemiBold`
- Title Small: `18sp / SemiBold`
- Body Large: `16sp / Regular`
- Body Medium: `14sp / Regular`
- Body Small: `12sp / Regular`
- Button: `15sp / SemiBold`
- Label: `12sp / Medium`

Usage:
- Title styles for screen headings and key status headers.
- Body styles for descriptions and scan details.
- Button and Label styles for actions, chips, and metadata.

## 7. Shapes and Radius
Radius system:

```kotlin
val RadiusSmall = 8.dp
val RadiusMedium = 14.dp
val RadiusLarge = 20.dp
val RadiusExtraLarge = 28.dp
```

Recommended usage:
- Buttons: `16.dp`
- Cards: `20.dp`
- Bottom navigation: `24.dp`
- Chips: pill shape
- Scanner frame: rounded scan corners, not a full rectangle

## 8. Spacing
Use a simple 4dp spacing system:

```kotlin
val SpaceXs = 4.dp
val SpaceS = 8.dp
val SpaceM = 16.dp
val SpaceL = 24.dp
val SpaceXl = 32.dp
val SpaceXxl = 48.dp
```

Recommended spacing:
- Screen horizontal padding: `24.dp`
- Card internal padding: `16.dp`
- Section spacing: `24.dp`
- Icon/text spacing: `8.dp`
- Primary button height: `52.dp`

## 9. Iconography
Icon direction:
- Line icons
- Medium stroke
- Rounded corners
- Simple geometry
- Consistent size and rhythm

Recommended icon set coverage:
- QR scanner
- Barcode
- Shield
- Check circle
- Warning
- Error
- History
- Link
- Copy
- Open in browser
- Flashlight

## 10. Components
### Primary Button
Use for the main action on each screen.

Examples:
- Start scanning
- Scan again
- Open link

Style:
- Height: `52.dp`
- Radius: `16.dp`
- Background: `GuardianPrimary`
- Text: high contrast

### Secondary Button
Use for secondary actions.

Examples:
- Copy result
- View history
- Cancel

Style:
- Surface-based background
- Subtle border
- Clear but lower emphasis than primary action

### Cards
Cards should remain simple and clean.

Style:
- Radius: `20.dp`
- Internal padding: `16.dp`
- Subtle border
- Avoid heavy shadows

### Chips
Use chips for metadata and status tags.

Examples:
- QR
- Barcode
- URL
- Safe
- Warning
- Danger
- Unknown

## 11. Main Screens
### Intro Screen
Purpose: explain the app quickly.

Recommended copy:
- **QR Guardian**
- **Secure QR & barcode scanner**
- **Scan, verify and open links with confidence.**

Primary CTA:
- Start scanning

Design:
- Centered app symbol
- Clear title
- Short subtitle
- One prominent button
- Clean background
- Minimal decorative elements

### Camera Screen
Purpose: scan QR codes and barcodes quickly.

Elements:
- Full-screen camera preview
- Central scanner frame
- Animated horizontal scan line
- Helper text

Recommended helper text:
- Point your camera at a QR or barcode

Optional actions:
- Close
- Flashlight
- History

Design notes:
- Scanner corners in `GuardianPrimary`
- Slight dark overlay outside scanner area
- Minimal distractions

### Result Screen
Purpose: show scanned content and safety status before opening.

#### Safe
Title:
- Looks safe

Actions:
- Open link
- Copy
- Scan again

#### Warning
Title:
- Review before opening

Description:
- This link may require extra caution.

Actions:
- Copy
- Scan again
- Open anyway

#### Danger
Title:
- Potential threat detected

Description:
- This URL may be unsafe. Opening it is not recommended.

Actions:
- Scan again
- Copy details

Rule:
- The dangerous action must never be the most visually prominent action.

### History Screen
Purpose: show previous scans.

Each item should include:
- Scanned content summary
- Type: QR / Barcode
- Status: Safe / Warning / Danger / Unknown
- Date
- Small icon

Example:
- `github.com`
- `Safe · URL · Today, 10:42`

## 12. Animations
Recommended animations:
- Scanner line moving vertically
- Small detection scale effect
- Fade-in for result screen
- Smooth state color transitions
- Short loading indicator while analyzing URL

Avoid:
- Long animations
- Heavy glow effects
- Game-like transitions
- Too many moving elements

## 13. Writing Style
The app UI should use **English** because it is intended for GitHub/CV presentation.

Tone:
- Clear
- Direct
- Professional
- Calm
- Trustworthy

Good examples:
- Secure QR & barcode scanner
- Scan, verify and open links with confidence.
- Potential threat detected
- Review before opening
- No scan history yet

Avoid exaggerated or alarmist text such as:
- Danger!!! Virus detected!!!
- This QR is hacked!!!

## 14. Compose Multiplatform Theme Example

```kotlin
@Composable
fun QRGuardianTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = GuardianPrimary,
            secondary = GuardianSecondary,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            onPrimary = Color(0xFF031018),
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary,
            onSurfaceVariant = DarkTextSecondary,
            error = Danger
        )
    } else {
        lightColorScheme(
            primary = GuardianPrimary,
            secondary = GuardianSecondary,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            onPrimary = Color.White,
            onBackground = LightTextPrimary,
            onSurface = LightTextPrimary,
            onSurfaceVariant = LightTextSecondary,
            error = Danger
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = QRGuardianTypography,
        shapes = QRGuardianShapes,
        content = content
    )
}
```

## 15. Design Tokens

```kotlin
object QRGuardianColors {
    val Primary = Color(0xFF00D6C9)
    val Secondary = Color(0xFF1D8CFF)
    val Safe = Color(0xFF1FD67A)
    val Warning = Color(0xFFFFB84D)
    val Danger = Color(0xFFFF5C5C)

    val DarkBackground = Color(0xFF07111F)
    val DarkSurface = Color(0xFF0D1B2E)
    val DarkSurfaceVariant = Color(0xFF13263D)
    val DarkBorder = Color(0xFF22364F)

    val LightBackground = Color(0xFFF7FAFC)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceVariant = Color(0xFFEFF5FA)
    val LightBorder = Color(0xFFDCE6EF)
}

object QRGuardianSpacing {
    val Xs = 4.dp
    val S = 8.dp
    val M = 16.dp
    val L = 24.dp
    val Xl = 32.dp
    val Xxl = 48.dp
}

object QRGuardianRadius {
    val Small = 8.dp
    val Medium = 14.dp
    val Large = 20.dp
    val ExtraLarge = 28.dp
}
```

## 16. Final Recommendation
QR Guardian should prioritize:
- Dark mode as the main visual identity
- A clean and professional light mode
- Few colors with clear role separation
- Strong semantic safety states
- Simple reusable components
- Minimal but meaningful animations
- English UI texts
- Polished GitHub/CV presentation

Brand sentence:

**QR Guardian**  
**Secure QR & barcode scanner**  
**KMP for iOS & Android**
