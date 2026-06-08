# Testing Strategy

## Testing Approach

Prioritize pragmatic tests in shared Kotlin logic (`commonMain`-oriented domain and pure functions). Keep tests readable and focused on behavior.

Testing style:
- Arrange
- Act
- Assert

Automated tests should prefer stored QR text payloads over QR image decoding. The sample QR images under `docs/assets/sample-qrs/` are documentation and manual QA assets; the same payloads are mirrored as stable regression fixtures in shared tests.

## Domain Tests

Prioritize coverage for:
- Normalization: whitespace trimming, null removal, empty input, long input.
- Classification: URL, email, phone, SMS, WiFi, vCard, geo, crypto, plain text.
- URL checks: HTTPS, `@` in URL, IPv4 host, link shorteners, dangerous file extensions, sensitive words, long URLs, too many query params, too many subdomains, brand impersonation.
- URL metadata: HEAD parsing, file type detection, attachment detection, redirect handling and unavailable responses.
- Result section mapping: local scan and remote reputation sections, including `NotApplicable`, `NotConfigured` and `Unavailable` states.
- Sensitive actions: `tel:`, `sms:`, `mailto:`, WiFi QR, crypto URIs.
- Final result mapping: dangerous overrides, suspicious warning behavior, safe/unknown output, `canOpen` derivation.
- Unknown content fallback.
- Dangerous scheme rejection when URL handling is extended to support it.

## Presentation Tests

Optional and incremental:
- State holder behavior
- UI state transitions
- Result mapping for view state

## Platform Tests

Camera and scanner behavior will require platform-specific testing later:
- Android camera and scanner integration tests
- iOS camera and scanner integration tests
- Permission behavior tests on both platforms

## Current Shared Test Coverage

The current implementation already includes focused tests for:
- `DefaultQrTextNormalizer`
- `DefaultQrContentClassifier`
- `UrlLocalSecurityAnalyzer`
- `WifiLocalSecurityAnalyzer`
- `SensitiveActionAnalyzer`
- `PlainTextSecurityAnalyzer`
- `DefaultLocalScanAnalyzer`
- `KtorUrlMetadataRepository`
- `AnalyzeQrSafetyUseCase`
- Koin wiring and pipeline assembly
- app state and result open-button gating

These tests document the expected behavior of the local verification pipeline and act as regression coverage for future rules.

That coverage already exercises the main shared behavior at the right level:
- local classification and rule evaluation
- URL metadata parsing and file-type mapping
- remote reputation repository parsing and composition
- presentation state for open actions and scanner flow

## Validation Commands

The main commands documented for this project are:

```bash
./gradlew spotlessCheck
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
./gradlew :shared:koverHtmlReport
git diff --check
```

Spotless enforces Kotlin formatting with ktlint. Run `./gradlew spotlessApply` locally before committing if formatting needs to be fixed.

## Guidance

- Prefer deterministic fake data sources in tests.
- Fake or mock HEAD metadata and remote reputation. Do not make real network calls in tests.
- Use the sample QR text payloads as regression cases for the security pipeline.
- Keep QR image decoding out of unit tests unless a future test seam makes it trivial and stable.
- Avoid testing implementation details.
- Avoid heavy test infrastructure until needed.
