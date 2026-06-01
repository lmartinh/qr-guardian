# AGENTS.md
## Project
QR Guardian is a Kotlin Multiplatform mobile application for Android and iOS.
The app scans QR codes and barcodes, detects scanned content, and helps users avoid opening potentially malicious URLs.
The project is also intended to be used as a professional portfolio/CV project for a mobile developer. Code quality, simplicity, visual polish and clear architecture are important.
Main package:
`com.lmartin.qrguardian`
## Technical Stack
The project is based on:
- Kotlin Multiplatform
- Compose Multiplatform
- Android
- iOS
- Kotlin Coroutines
- Kotlinx Serialization, when needed
- Ktor Client, when needed
- Clean Architecture principles
Do not add heavy dependencies unless they are clearly justified by the current task.
## Product Vision
QR Guardian should provide a simple and safe QR/barcode scanning experience.
The target flow is:
1. The user opens the app.
2. The app opens or prepares the QR/barcode scanner.
3. The user scans a QR code or barcode.
4. The app detects the scanned content type.
5. If the scanned content is a URL, the app analyzes whether it may be malicious.
6. The app shows a clear visual result before the user opens the link.
Possible content types:
- URL
- Plain text
- Email
- Phone number
- WiFi QR
- Barcode product code
- Unknown content
Possible URL safety states:
- Safe
- Suspicious
- Malicious
- Unknown
## Architecture Principles
Prefer a simple layered structure:
- `core`: shared utilities, common helpers and platform-independent support code.
- `domain`: business models, repository contracts and use cases.
- `data`: repository implementations, local/remote data sources and mappers.
- `presentation`: UI state, state holders and Compose screens.
Keep the architecture simple and effective.
Follow these principles:
- Kotlin Multiplatform first.
- Clean Architecture without unnecessary complexity.
- SOLID principles where they provide real value.
- Small classes with clear responsibilities.
- Immutable data models.
- Explicit and readable code.
- Testable business logic.
- Avoid premature abstractions.
- Avoid overengineering.
- Avoid global mutable state.
- Keep platform-specific code isolated.
- Do not expose provider-specific security details directly to the UI layer.
Add new packages only when there is real code that justifies them.
## Kotlin Multiplatform Rules
- Put shared business logic in `commonMain`.
- Keep Android-specific code in `androidMain`.
- Keep iOS-specific code in `iosMain`.
- Use `expect/actual` only when needed.
- Avoid Android-only APIs in `commonMain`.
- Avoid iOS-only APIs in `commonMain`.
- Prefer common Kotlin APIs whenever possible.
- Keep shared domain models platform-independent.
- Avoid leaking platform classes into domain or common presentation state.
- Prefer constructor injection over service locators or global singletons.
- Avoid dependency injection frameworks unless explicitly requested.
## Compose Multiplatform Rules
- Keep shared UI in `commonMain` when possible.
- Avoid platform-specific UI unless required.
- Prefer simple, readable composables.
- Keep composables stateless when practical.
- State should be represented with immutable UI state models.
- Use unidirectional data flow for screens.
- Keep screen state separate from domain models when needed.
- Do not introduce a design system until the UI has enough repeated patterns.
- Prefer accessible UI: meaningful text, content descriptions where needed and clear visual states.
- UI should be modern, clean and suitable for a portfolio project.
## Suggested Initial Package Structure
Use this base package:
`com.lmartin.qrguardian`
Suggested packages under `commonMain`:
```text
com.lmartin.qrguardian.core
com.lmartin.qrguardian.domain
com.lmartin.qrguardian.data
com.lmartin.qrguardian.presentation
```
Avoid deep package nesting until the codebase needs it.
## Domain Guidance
When implemented, the domain layer should model the QR analysis flow clearly.

Suggested domain concepts:

```kotlin
enum class QrContentType {
    Url,
    Text,
    Email,
    Phone,
    Wifi,
    Barcode,
    Unknown
}
enum class UrlSafetyStatus {
    Safe,
    Suspicious,
    Malicious,
    Unknown
}
enum class UrlThreatType {
    Malware,
    Phishing,
    SocialEngineering,
    UnwantedSoftware,
    Unknown
}
```

Suggested use cases:

- DetectQrContentTypeUseCase
- AnalyzeQrContentUseCase
- CheckUrlSafetyUseCase

Suggested repository contract:

```kotlin
interface UrlSafetyRepository {
    suspend fun checkUrl(url: String): UrlSafetyResult
}
```

Do not implement all of these unless they are part of the current task. Use this section as project direction.
## Security Rules
Security is a core part of QR Guardian.

Follow these rules:

- Never commit API keys, tokens, secrets or credentials.
- Never hardcode credentials in the mobile app.
- Do not expose URL reputation provider API keys in Android or iOS.
- Prefer a backend intermediary for integrations with security providers.
- Validate and normalize URLs before analyzing them.
- Reject dangerous URL schemes when URL analysis is implemented:
  - `javascript:`
  - `file:`
  - `data:`
  - `intent:`
- Do not automatically open scanned URLs.
- Always show the analyzed result before allowing the user to open a URL.
- Treat unknown results carefully and communicate uncertainty to the user.
- Keep security messaging clear and understandable.

Potential future URL reputation providers:

- Google Safe Browsing
- PhishTank
- VirusTotal
- urlscan.io

Do not integrate any provider unless explicitly requested.
## Backend Guidance
A backend is recommended for real URL safety checks.

Preferred flow for production-like behavior:

Mobile app
↓
QR/content analysis in shared KMP code
↓
Backend endpoint
↓
URL reputation provider
↓
Backend normalized response
↓
Mobile UI safety result

Reasons:

- Avoid exposing API keys in mobile apps.
- Enable caching.
- Allow provider changes without forcing app updates.
- Combine multiple providers later if needed.
- Centralize security rules.

Do not create a backend unless explicitly requested.
## Testing Rules
Add tests mainly for domain and pure Kotlin logic.

Use Arrange, Act, Assert.

Prioritize tests for:

- QR content type detection.
- URL validation.
- URL normalization.
- Dangerous scheme rejection.
- URL safety result mapping.
- Use case behavior.
- Repository behavior with fake data sources.

Keep tests simple and readable.

Avoid testing implementation details.

Do not add complex test infrastructure unless needed.
## Git and Repository Hygiene
- Keep commits focused.
- Do not mix unrelated changes.
- Do not reformat the whole project unless explicitly requested.
- Do not rename modules unless needed.
- Do not add generated files.
- Do not commit local IDE files.
- Do not commit build outputs.
- Do not commit secrets.
- Keep .gitignore suitable for Kotlin Multiplatform, Android, iOS and Xcode.
- Preserve Gradle wrapper files.

Required files that should not be ignored:

- gradlew
- gradlew.bat
- gradle/wrapper/gradle-wrapper.jar
- gradle/wrapper/gradle-wrapper.properties
## Recommended .gitignore Coverage
The repository .gitignore should cover at least:

```gitignore
# Gradle
.gradle/
build/
**/build/

# Kotlin
.kotlin/

# IntelliJ / Android Studio
.idea/
*.iml
*.ipr
*.iws

# Android
local.properties
captures/
.externalNativeBuild/
.cxx/
*.apk
*.ap_
*.aab
*.dex

# iOS / Xcode
DerivedData/
iosApp/DerivedData/
*.xcuserstate
*.xcworkspace/xcuserdata/
*.xcodeproj/xcuserdata/
*.xcodeproj/project.xcworkspace/xcuserdata/
*.moved-aside
*.hmap
*.ipa
*.dSYM.zip
*.dSYM

# CocoaPods
Pods/
iosApp/Pods/

# Swift Package Manager
.build/
.swiftpm/

# macOS
.DS_Store

# Secrets
*.keystore
*.jks
*.p12
*.mobileprovision
.env
.env.*
secrets.properties
```
## Validation
When modifying the project, run the most relevant validation command available.

Preferred:

`./gradlew build`

If full build is too heavy or not available, run a smaller relevant task, for example:

`./gradlew :shared:compileCommonMainKotlinMetadata`

or

`./gradlew :androidApp:assembleDebug`

When adding tests, run the related test task.

Always report:

- What was changed.
- What validation was executed.
- Whether validation passed.
- Any known issues or limitations.
## Do Not Do Without Explicit Instruction
Do not implement these features unless the user explicitly asks for them:

- Camera scanning.
- Barcode detection.
- QR scanner library integration.
- Google Safe Browsing integration.
- PhishTank integration.
- VirusTotal integration.
- urlscan.io integration.
- Backend creation.
- API keys or secret configuration.
- Local database persistence.
- Dependency injection framework.
- Analytics or tracking.
- Complex design system.
- Authentication.
- User accounts.
- Push notifications.
- Ads.
- Payments.
- Unnecessary module splitting.
## Agent Behavior
When working on this repository:

- Make the smallest useful change for the current task.
- Prefer simple Kotlin code.
- Keep the project buildable.
- Explain important trade-offs briefly.
- Ask for clarification only when the task is genuinely ambiguous.
- Do not invent requirements.
- Do not silently add dependencies.
- Do not hide failed validation.
