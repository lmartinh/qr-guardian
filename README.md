<p align="center">
  <img src="docs/assets/qr-guardian-hero-new.png" alt="QR Guardian hero banner" width="100%">
</p>

# QR Guardian

> Scan smarter. Open safer.

[English](README.md) · [Español](README.es.md)

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4)](https://www.jetbrains.com/compose-multiplatform/)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS-0A7EA4)](https://developer.android.com/)
[![Package](https://img.shields.io/badge/Package-com.lmartin.qrguardian-2ea44f)](https://kotlinlang.org/docs/packages.html)

QR Guardian is a Kotlin Multiplatform mobile app for Android and iOS that scans QR codes and barcodes, classifies the content, and shows a safety result before the user decides whether to open anything.

## What QR Guardian does

- Detects QR, barcode and common content types.
- Runs a local security pass on every scan by default.
- Performs optional remote reputation checks only for URLs.
- Never opens scanned content automatically.
- Presents clear results before the user acts.

## Screenshots

| Intro | Camera |
|---|---|
| ![QR Guardian intro screen](docs/assets/screenshots/intro.png) | ![QR Guardian camera scanner screen](docs/assets/screenshots/camera.png) |

| Result: safe local scan | Result: suspicious local scan | Result: dangerous blocked URL |
|---|---|---|
| ![QR Guardian result screen showing file/PDF detection with local scan details](docs/assets/screenshots/result-safe.png) | ![QR Guardian result screen showing a suspicious scan result](docs/assets/screenshots/result-suspicious.png) | ![QR Guardian result screen showing a dangerous blocked URL](docs/assets/screenshots/result-danger.png) |

## Sample QR dataset

QR Guardian includes a small synthetic dataset for manual testing and demos.
It covers safe URLs, suspicious URLs, downloads, WiFi, SMS, email, plain text, crypto, vCard and geo payloads.

Use it to validate local scan heuristics, HEAD metadata checks and result rendering. Remote Reputation depends on provider configuration and live lookup results.

The underlying text payloads are also covered by shared regression tests, so the sample images remain manual QA assets rather than automated decoding fixtures.

Dataset: [docs/assets/sample-qrs/README.md](docs/assets/sample-qrs/README.md)

## Core flow

Intro
→ Camera
→ QR/barcode detected
→ Local Scan
→ Remote Reputation, optional
→ Result

The app always shows the result before the user opens anything. The open action is only available for URL results that are not classified as dangerous.

## Security model

### Local Scan

- Always enabled.
- Runs without API keys.
- Uses local QR and content checks to classify payloads.
- Blocks dangerous schemes such as `javascript:`, `file:`, `data:`, and `intent:`.
- Checks HTTP vs HTTPS, suspicious URL shapes, and IP/local-looking destinations.
- Performs HEAD metadata inspection for URLs when the server supports it, then falls back to path-based inference when needed.
- Detects download-like content such as PDF/menu links as file metadata.
- Treats executable or script downloads as high risk.
- Does not call external reputation services.

### Remote Reputation

- Optional and URL-only.
- Uses Google Safe Browsing and URLhaus when configured.
- Shows `Not configured` when no keys are provided.
- Handles provider failures safely and keeps the local result visible.
- A clean remote result is not a guarantee of safety.
- Never stores keys in the repository.

PDF and menu URLs are shown as file metadata first. They are not automatically treated as dangerous just because they point to a downloadable file.

## Getting Started

### Android

1. Open the project in Android Studio.
2. Run the `androidApp` configuration or `./gradlew :androidApp:assembleDebug`.
3. Camera permission is required for scanning.
4. No API keys are required for local-only mode.
5. Optional: create `local.properties` from `local.properties.example` to enable remote providers.

### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Run the `iosApp` target.
3. Camera permission is required for scanning.
4. No API keys are required for local-only mode.
5. Optional: copy `iosApp/Configuration/RemoteReputation.example.xcconfig` to `iosApp/Configuration/RemoteReputation.xcconfig` and add keys.

### Camera permissions

- Android uses `android.permission.CAMERA`.
- iOS uses `NSCameraUsageDescription`.
- If permission is denied, the camera scanner cannot start.

## Optional Remote Reputation configuration

QR Guardian works out of the box without API keys. Leave the values empty to keep local-only mode.

### Android

1. Copy or create `local.properties` in the project root.
2. Add the keys:

```properties
GOOGLE_SAFE_BROWSING_API_KEY=your_google_key
URLHAUS_API_KEY=your_urlhaus_auth_key
```

### iOS

1. Copy `iosApp/Configuration/RemoteReputation.example.xcconfig` to `iosApp/Configuration/RemoteReputation.xcconfig`.
2. Add the keys:

```xcconfig
GOOGLE_SAFE_BROWSING_API_KEY = your_google_key
URLHAUS_API_KEY = your_urlhaus_auth_key
```

API keys embedded in mobile apps cannot be fully protected. This setup is suitable for development, demos and portfolio use. For production, use a backend or proxy.

## Architecture and dependency injection

- Shared domain and presentation logic lives in Kotlin Multiplatform code.
- Compose Multiplatform is used for the shared UI.
- Ktor Client handles HEAD metadata checks and remote reputation requests.
- Koin is used only at the app wiring boundary.
- `QrGuardianSecurityPipelineFactory` is the single source of truth for pipeline composition.
- Domain code remains framework-independent.

Dependency injection is handled with Koin, but only at the app wiring boundary. The security pipeline is assembled by `QrGuardianSecurityPipelineFactory`, which keeps the dependency graph explicit, testable and independent from the DI framework.

## Portfolio highlights

- Kotlin Multiplatform shared security pipeline.
- Compose Multiplatform UI for Android and iOS.
- Camera-based QR and barcode scanning.
- Local URL safety checks and HEAD metadata inspection.
- Optional remote reputation providers.
- Koin dependency injection kept at the wiring boundary.
- Unit-tested domain logic and provider selection.

## Tests and validation

Recently validated with:

```bash
./gradlew :shared:allTests
./gradlew check
git diff --check
```

Optional local checks that are also valid for this repository:

```bash
./gradlew spotlessCheck
./gradlew spotlessApply
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
./gradlew :shared:koverHtmlReport
```

Formatting uses Spotless with ktlint for Kotlin sources and Gradle Kotlin scripts. Run `spotlessApply` locally before committing if `spotlessCheck` reports formatting issues. Shared regression tests cover normalization, local URL heuristics, metadata parsing, file/download inference, remote reputation composition, and result open-button gating.

GitHub Actions CI runs environment validation, Android lint, optional Spotless checks, shared tests, best-effort Kover XML generation, and Android release assembly for pull requests and manual runs.

## AI Mobile Tools

AI-assisted review tools are run manually from the GitHub Actions workflow [`AI Mobile Tools`](.github/workflows/ai-tools.yml). They use [Mobile AI Toolkit](https://github.com/lmartinh/mobile-ai-toolkit/tree/main) to generate report artifacts with:

- `compose-guardrails` for Compose architecture, state, side-effect, accessibility and multiplatform boundary review.
- `kmp-project-auditor` for Kotlin Multiplatform source-set and platform-boundary review.

The normal CI workflow does not run real AI providers or require provider secrets. The default AI provider is `fake`, which is safe for deterministic validation and does not require secrets. Real providers are manual opt-in only and require repository secrets configured in the repository or fork where the workflow runs.

AI tool findings are advisory and report-first. They are useful review inputs, but they do not replace tests, code review, or manual security judgment.

### Running AI Mobile Tools with your own provider

1. Fork the repository.
2. Open your fork on GitHub.
3. Go to `Settings` → `Secrets and variables` → `Actions`.
4. Add the secret for the provider you want to use:
   - `OPENAI_API_KEY`
   - `ANTHROPIC_API_KEY`
   - `GEMINI_API_KEY`
5. Go to `Actions` → `AI Mobile Tools`.
6. Click `Run workflow`.
7. Select the branch.
8. Select the provider.
9. Select the tools.
10. Run the workflow.

Real providers are available only when the repository or fork running the workflow has the required secret configured. API keys must not be passed as workflow inputs and must not be committed to the repository.

## Troubleshooting

- Camera does not open: check camera permission.
- Remote Reputation shows `Not configured`: the API keys are missing or empty.
- PDF or file type is not shown: the server may not support HEAD or may not expose `Content-Type` or `Content-Disposition`.
- iOS keys are not picked up: verify `RemoteReputation.xcconfig` is included and the `Info.plist` build settings still reference the values.

## Documentation

- [Overview](docs/00-overview.md)
- [Functional Specification](docs/02-functional-specification.md)
- [Architecture](docs/03-architecture.md)
- [UI Flow](docs/04-ui-flow.md)
- [Security Model](docs/05-security-model.md)
- [Local Security Checks](docs/security/local-security-checks.md)
- [Remote Reputation](docs/security/remote-reputation.md)
- [Testing Strategy](docs/06-testing-strategy.md)
- [Agent Guidelines](AGENTS.md)

## License

Licensed under the [Apache License 2.0](LICENSE).

## Known limitations

- Remote Reputation is optional and stays disabled until API keys are configured.
- Mobile API keys are not fully secret when shipped in an app binary.
- HEAD metadata depends on server support, so some URLs may not expose file details.
- Remote providers can return false negatives, so a clean reputation result is not a guarantee of safety.
- QR Guardian is a portfolio and demo project, not a replacement for dedicated security tooling.
