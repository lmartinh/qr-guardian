# Testing Strategy

QR Guardian keeps most automated coverage in `shared/src/commonTest` because the key behavior lives in shared Kotlin code: classification, local security checks, metadata inference, remote reputation mapping, final result assembly, and presentation state.

The test suite is designed to be deterministic. Unit tests do not require real network calls or real API keys.

## Test Approach

Tests are focused on behavior rather than implementation details.

General style:
- Arrange
- Act
- Assert
- deterministic fake inputs
- fake HTTP clients for network-shaped behavior
- text payload fixtures instead of QR image decoding in unit tests

The QR image samples under `docs/assets/sample-qrs/` are manual QA/demo assets. The underlying payloads are mirrored as regression fixtures in shared tests.

## What Is Covered

### Normalization and Classification

Covered in:
- `DefaultQrTextNormalizerTest`
- `DefaultQrContentClassifierTest`

Coverage includes:
- whitespace trimming
- blank input
- null-character removal
- long text preservation
- URL, email, phone, SMS, WiFi, vCard, geo, crypto, and plain text classification

### Local URL Heuristics

Covered in:
- `UrlLocalSecurityAnalyzerTest`
- `DefaultLocalScanAnalyzerTest`
- `TypeSpecificSecurityAnalyzerTest`
- `AnalyzeQrSafetyUseCaseTest`

Coverage includes:
- HTTP vs HTTPS
- shorteners
- credentials or `@` in URLs
- IPv4 and local-looking destinations
- localhost regressions
- punycode-style/lookalike regressions
- suspicious wording and brand impersonation
- many query parameters
- dangerous file extensions
- dangerous scheme blocking, including mixed-case schemes
- sensitive QR actions such as phone, SMS, email, WiFi, crypto, vCard, and geo payloads

### URL Metadata And File/Download Inference

Covered in:
- `KtorUrlMetadataRepositoryTest`
- `QrSafetyAnalysisAssemblerTest`
- `AnalyzeQrSafetyUseCaseTest`
- `QrSampleDatasetRegressionTest`

Coverage includes:
- HEAD metadata parsing
- `Content-Type`
- `Content-Disposition`
- attachment/download inference
- `filename*`
- redirect final URL behavior
- `405 Method Not Allowed` fallback
- transport failure fallback
- path-based file inference
- PDF/menu URLs
- archives
- executable or installer-like downloads
- unknown binary files

Metadata tests use Ktor test/fake HTTP behavior instead of live servers.

### Remote Reputation

Covered in:
- `GoogleSafeBrowsingUrlReputationRepositoryTest`
- `UrlHausReputationRepositoryTest`
- `CompositeUrlReputationRepositoryTest`
- `NoOpUrlReputationRepositoryTest`
- `UrlReputationRepositoryFactoryTest`
- `RemoteReputationConfigFactoryTest`

Coverage includes:
- not-configured behavior
- provider selection
- Google Safe Browsing clean and malicious responses
- URLhaus clean and malicious responses
- malformed provider payload handling
- HTTP/provider failures
- multiple-provider composition
- local-safe + remote-malicious final result behavior

Provider tests use fake HTTP clients. No unit test calls real Google Safe Browsing or URLhaus endpoints.

### Result Assembly And UI State

Covered in:
- `QrSafetyAnalysisAssemblerTest`
- `AnalyzeQrSafetyUseCaseTest`
- `ResultExtractorsTest`
- `ResultUiStateTest`
- `QrGuardianAppStateTest`

Coverage includes:
- local and remote result section mapping
- blocked-scheme result rows
- final result level calculation
- `openableUrl`
- `canOpen`
- result open-button gating
- non-URL suppression of the open action
- app-level scan/result state transitions

### Pipeline And DI Smoke Tests

Covered in:
- `QrGuardianSecurityPipelineFactoryTest`
- `QrGuardianKoinTest`
- `QrGuardianHttpClientFactoryTest`

Coverage includes:
- Koin startup
- `AnalyzeQrSafetyUseCase` resolution
- empty remote config
- Google-only config
- URLhaus-only config
- both providers configured
- shared HTTP client creation

## Dataset Regression Tests

`QrSampleDatasetRegressionTest` runs stable sample payloads through the shared analysis pipeline.

It verifies:
- detected content type
- final level
- local level
- open-button behavior
- openable URL
- local reasons
- local metadata rows

This gives the manual QR dataset a matching automated text-fixture safety net without making unit tests depend on QR image decoding.

## Validation Commands

Primary validation:

```bash
./gradlew :shared:allTests
./gradlew check
git diff --check
```

Useful optional commands:

```bash
./gradlew spotlessCheck
./gradlew spotlessApply
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
./gradlew :shared:koverHtmlReport
```

Run `spotlessApply` only when formatting needs to be fixed locally.

## GitHub Actions

`.github/workflows/ci.yml` is the main deterministic CI workflow. It runs on pull requests and manual dispatch, and covers:

- environment validation with Java and Gradle
- Android lint
- optional Spotless checks when the task exists
- `./gradlew :shared:allTests`
- best-effort `:shared:koverXmlReport`
- Android release assembly
- test, coverage, and release artifacts

`.github/workflows/ai-tools.yml` is a manual, report-first workflow for AI-assisted review with [Mobile AI Toolkit](https://github.com/lmartinh/mobile-ai-toolkit/tree/main). It can run:

- `compose-guardrails` against shared Compose presentation code
- `kmp-project-auditor` against the project root

The default provider is `fake`, so the workflow can run without secrets. Real providers (`openai`, `anthropic`, or `gemini`) are opt-in and require GitHub Actions secrets in the repository or fork running the workflow.

AI tool reports are advisory. They can help reviewers spot Compose, KMP, and architecture risks, but findings can include false positives or false negatives and do not replace deterministic tests or code review.

## Current Testing Limitations

- QR image decoding is validated manually through the sample QR images, not by unit tests.
- Composable rendering is mostly covered through state and extractor tests rather than snapshot tests.
- Real provider behavior is not tested against live APIs.
- Production backend/proxy behavior is not covered because this repository does not include a backend.
