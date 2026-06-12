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

QR Guardian is a Kotlin Multiplatform mobile app for Android and iOS that scans QR codes and barcodes, classifies their content, and shows a security-oriented result before the user decides whether to open anything.

The project demonstrates a shared KMP security pipeline, Compose Multiplatform UI, camera-based scanning, local threat detection, optional remote reputation checks, dependency injection, regression tests, formatting checks and CI validation.

## Why this project exists

QR Guardian is built as a portfolio-grade mobile project focused on realistic Kotlin Multiplatform architecture.

It is intentionally small enough to understand quickly, but complete enough to demonstrate production-style concerns:

- shared business logic for Android and iOS
- clear separation between domain, data, presentation and platform code
- security-first UX that never opens scanned content automatically
- local-first behavior without requiring API keys
- optional provider-based remote reputation
- testable scan pipeline and deterministic validation
- CI, formatting and documentation suitable for a public repository

## Product features

- Scans QR codes and barcodes from the camera.
- Classifies common payloads such as URLs, WiFi, SMS, email, plain text, crypto, vCard and geo links.
- Runs a local security scan for every detected payload.
- Performs optional remote reputation checks for URLs.
- Shows a clear result before the user opens anything.
- Blocks the open action for dangerous URL results.
- Works without API keys in local-only mode.

## Technical highlights

- Kotlin Multiplatform shared domain and presentation logic.
- Compose Multiplatform UI for Android and iOS.
- Camera-based QR and barcode scanning.
- Local URL safety checks with deterministic heuristics.
- HEAD metadata inspection for URL resources.
- File/download inference using headers, redirects and path fallback.
- Optional Google Safe Browsing and URLhaus reputation providers.
- Ktor Client for network metadata and reputation calls.
- Koin dependency injection kept at the app wiring boundary.
- Framework-independent domain logic.
- Shared regression tests for the security pipeline.
- Spotless and ktlint formatting.
- GitHub Actions CI for validation, tests, lint, Kover and Android assembly.
- Manual AI-assisted review workflows using Mobile AI Toolkit.

## Screenshots

| Intro | Camera |
|---|---|
| <img src="docs/assets/screenshots/intro.png" alt="QR Guardian intro screen" width="260"> | <img src="docs/assets/screenshots/camera.png" alt="QR Guardian camera scanner screen" width="260"> |

| Result: safe local scan | Result: suspicious local scan | Result: dangerous blocked URL |
|---|---|---|
| <img src="docs/assets/screenshots/result-safe.png" alt="QR Guardian result screen showing file/PDF detection with local scan details" width="260"> | <img src="docs/assets/screenshots/result-suspicious.png" alt="QR Guardian result screen showing a suspicious scan result" width="260"> | <img src="docs/assets/screenshots/result-danger.png" alt="QR Guardian result screen showing a dangerous blocked URL" width="260"> |

## Security pipeline

```text
Camera scan
  ↓
QR / barcode payload
  ↓
Content classification
  ↓
Local Scan
  ↓
HEAD metadata inspection, for URLs
  ↓
Remote Reputation, optional and URL-only
  ↓
Result: Safe / Suspicious / Dangerous
```

The app always shows the result before the user opens anything. The open action is only available for URL results that are not classified as dangerous.

## Security model

### Local Scan

- Always enabled.
- Requires no API keys.
- Uses deterministic local QR and content checks to classify payloads.
- Blocks dangerous schemes such as `javascript:`, `file:`, `data:`, and `intent:`.
- Checks HTTP vs HTTPS, suspicious URL shapes, shortener-like domains, and IP, localhost or private-looking destinations.
- Flags risky file extensions and executable or script-like downloads.
- Performs HEAD metadata inspection for URLs when the server supports it, then falls back to path-based inference when needed.
- Detects download-like content such as PDF/menu links as file metadata.
- Does not call external reputation services.

### Remote Reputation

- Optional and URL-only.
- Uses Google Safe Browsing and URLhaus when configured.
- Shows `Not configured` when no keys are provided.
- Adds external signals without replacing local checks.
- Handles provider failures safely and keeps the local result visible.
- A clean remote result is not a guarantee of safety.
- Never stores keys in the repository.

PDF and menu URLs are shown as file metadata first. They are not automatically treated as dangerous just because they point to a downloadable file.

## Architecture

```text
shared/
├── domain          # scan models, classification, local rules and result composition
├── data            # HEAD metadata, remote reputation providers and repositories
├── presentation    # shared UI state, app state and result presentation models
└── di              # Koin modules and pipeline wiring
```

- Shared domain and presentation logic lives in Kotlin Multiplatform code.
- Compose Multiplatform is used for the shared UI.
- Platform-specific code handles camera and HTTP engine wiring where needed.
- Ktor Client handles HEAD metadata checks and remote reputation requests.
- Koin is used only at the app wiring boundary.
- `QrGuardianSecurityPipelineFactory` is the single source of truth for pipeline composition.
- Domain code remains framework-independent and does not depend on Koin.

Dependency injection is handled with Koin, but only at the app wiring boundary. The security pipeline is assembled by `QrGuardianSecurityPipelineFactory`, which keeps the dependency graph explicit, testable and independent from the DI framework.

## Sample QR dataset

QR Guardian includes a small synthetic dataset for manual testing and demos.
It covers safe URLs, suspicious URLs, downloads, WiFi, SMS, email, plain text, crypto, vCard and geo payloads.

Use it to validate local scan heuristics, HEAD metadata checks and result rendering. Remote Reputation depends on provider configuration and live lookup results.

The underlying text payloads are also covered by shared regression tests, so the sample images remain manual QA assets rather than automated decoding fixtures.

Dataset: [docs/assets/sample-qrs/README.md](docs/assets/sample-qrs/README.md)

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

Formatting uses Spotless with ktlint for Kotlin sources and Gradle Kotlin scripts. Run `spotlessApply` locally before committing if `spotlessCheck` reports formatting issues.

Shared regression tests cover:

- content normalization
- local URL heuristics
- metadata parsing
- file and download inference
- remote reputation composition
- provider selection
- result open-button gating

## Continuous Integration

GitHub Actions CI runs on pull requests and manual dispatch. It validates:

- environment checks
- Android lint
- optional Spotless checks
- shared tests
- best-effort Kover XML generation
- Android release assembly

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

## Troubleshooting

- Camera does not open: check camera permission.
- Remote Reputation shows `Not configured`: the API keys are missing or empty.
- PDF or file type is not shown: the server may not support HEAD or may not expose `Content-Type` or `Content-Disposition`.
- iOS keys are not picked up: verify `RemoteReputation.xcconfig` is included and the `Info.plist` build settings still reference the values.
- Remote provider results can change because reputation providers depend on live external data.

## Known limitations

- Remote Reputation is optional and stays disabled until API keys are configured.
- Mobile API keys are not fully secret when shipped in an app binary.
- HEAD metadata depends on server support, so some URLs may not expose file details.
- Remote providers can return false negatives, so a clean reputation result is not a guarantee of safety.
- QR Guardian is a portfolio and demo project, not a replacement for dedicated security tooling.

## License

Licensed under the [Apache License 2.0](LICENSE).
