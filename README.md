<div align="center">
  <img src="docs/assets/qr-guardian-hero.png" alt="QR Guardian hero - secure QR scanning" width="100%">
</div>

# QR Guardian

> **Read in another language:** **English** · [Español](README.es.md)

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS-0A7EA4)](https://developer.android.com/)
[![Package](https://img.shields.io/badge/Package-com.lmartin.qrguardian-2ea44f)](https://kotlinlang.org/docs/packages.html)

### Scan smarter. Open safer.

## What is QR Guardian?
QR Guardian is a Kotlin Multiplatform mobile app for Android and iOS.

It scans QR codes and barcodes, detects the scanned content type, and helps users evaluate potentially malicious URLs **before opening them**.

## Why this project?
- Build a practical, security-aware mobile app.
- Showcase clean KMP + Compose architecture in a portfolio-ready project.
- Keep logic shared, UI polished, and behavior predictable.

## Core product flow
1. User opens the app.
2. User starts scanning.
3. App reads QR/barcode content.
4. App detects content type.
5. If content is a URL, app evaluates safety.
6. App shows result first; user decides what to do next.

## Planned main screens
- Intro / Launch
- Camera Capture
- Result

## Safety principles
- Never auto-open scanned URLs.
- Warn clearly on suspicious or malicious results.
- Treat unknown results as uncertain.
- Keep security-provider secrets out of mobile clients.
- Perform a local first-pass verification before any opening action.
- Surface the analyzed content type, security level, reasons, and `canOpen` decision to the result screen.
- Show the open action only for URL results that are not dangerous, and open bare domains with `https://` when possible.

## Security checks
- Local checks are enabled by default.
- The local scan block evaluates normalization, classification, URL rules and HEAD metadata.
- Remote reputation checks are optional and only run for URLs.
- Google Safe Browsing and URLhaus can be configured by each developer.
- The current app wiring reads optional host config and still runs local-only when no keys are present.
- Non-URL payloads do not trigger HEAD or remote reputation checks.
- No API keys are included.
- Without an API key, QR Guardian runs in local-only mode.
- The result is split into `Local Scan` and `Remote Reputation` sections.

## Remote Reputation Configuration
QR Guardian works out of the box without API keys.

- Local Scan always runs.
- Remote Reputation is optional and only applies to URLs.
- `GOOGLE_SAFE_BROWSING_API_KEY` enables Google Safe Browsing.
- `URLHAUS_API_KEY` enables URLhaus.
- If neither key is configured, the app stays in local-only mode and the remote section shows `Not configured`.

Android:
- Add the keys to `local.properties` in the project root.
- `local.properties.example` shows the exact empty placeholders.
- Empty or missing values keep local-only mode.

iOS:
- Add the keys through `iosApp/Configuration/RemoteReputation.xcconfig` copied from `iosApp/Configuration/RemoteReputation.example.xcconfig`.
- The values are exposed to `Info.plist` and read by the shared iOS config provider.
- Empty or missing values keep local-only mode.

Never commit real API keys.
For production, use a backend or proxy instead of embedding reputation keys in the mobile binary.

## Tech stack
- Kotlin Multiplatform
- Compose Multiplatform
- Android + iOS
- Kotlin Coroutines
- Clean Architecture principles

## Dependency injection
- QR Guardian uses Koin for KMP dependency injection.
- Koin is used only for app wiring at the platform boundary.
- Domain code remains framework-independent.
- RemoteReputationConfig is provided explicitly by Android and iOS hosts.
- Local-only mode remains the default when no keys are configured.
- Tests can override dependencies through Koin modules.

## Project structure
- `androidApp/`: Android host app.
- `iosApp/`: iOS host app (Xcode project).
- `shared/`: shared KMP logic and shared Compose UI.
- `docs/`: product, architecture, roadmap, security and testing docs.

## Run
Android debug build:
```bash
./gradlew :androidApp:assembleDebug
```

iOS (from Xcode):
`iosApp/` → open in Xcode and run target.

## Test
Android host tests:
```bash
./gradlew :shared:testAndroidHostTest
```

iOS simulator tests:
```bash
./gradlew :shared:iosSimulatorArm64Test
```

Shared domain verification tests:
```bash
./gradlew :shared:allTests
```

Coverage report:
```bash
./gradlew :shared:koverHtmlReport
```

## Documentation index
- [Overview](docs/00-overview.md)
- [Roadmap](docs/01-roadmap.md)
- [Functional Specification](docs/02-functional-specification.md)
- [Architecture](docs/03-architecture.md)
- [UI Flow](docs/04-ui-flow.md)
- [Security Model](docs/05-security-model.md)
- [Local Security Checks](docs/security/local-security-checks.md)
- [Remote Reputation](docs/security/remote-reputation.md)
- [Testing Strategy](docs/06-testing-strategy.md)
- [Agent Tasks](docs/07-agent-tasks.md)
- [Agent Guidelines](AGENTS.md)
