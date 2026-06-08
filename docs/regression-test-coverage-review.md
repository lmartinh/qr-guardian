# Regression Test Coverage Review

## 1. Current test structure

The current regression suite is concentrated in `shared/src/commonTest`, with shared pure Kotlin coverage for domain logic, metadata inference, remote reputation, composition, and result presentation state.

### Normalization and classification
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/normalizer/DefaultQrTextNormalizerTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/normalizer/DefaultQrTextNormalizerTest.kt)
  - Covers whitespace trimming, blank input, null-character removal, and long-text preservation.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/classifier/DefaultQrContentClassifierTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/classifier/DefaultQrContentClassifierTest.kt)
  - Covers URL, email, phone, SMS, Wi-Fi, vCard, geo, crypto, and plain text classification.

### Local URL analysis
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/UrlLocalSecurityAnalyzerTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/UrlLocalSecurityAnalyzerTest.kt)
  - Covers HTTPS vs HTTP, `@` in URL, IPv4 host, link shortener, dangerous file extensions, suspicious wording, long URLs, many query params, and brand impersonation.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/DefaultLocalScanAnalyzerTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/DefaultLocalScanAnalyzerTest.kt)
  - Covers final local scan levels for safe HTTPS, HTTP, dangerous file URL, plain text, and dangerous scheme rejection through the use case.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/TypeSpecificSecurityAnalyzerTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/TypeSpecificSecurityAnalyzerTest.kt)
  - Covers Wi-Fi open network, Wi-Fi with credentials, `tel:`, `sms:`, `mailto:`, and plain text behavior.

### URL metadata and file/download inference
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/metadata/KtorUrlMetadataRepositoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/metadata/KtorUrlMetadataRepositoryTest.kt)
  - Covers HEAD parsing, `405 Method Not Allowed` fallback, request normalization, `filename*`, attachment detection, `application/octet-stream`, HTML/web-page behavior, and path-based file inference.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/QrSafetyAnalysisAssemblerTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/QrSafetyAnalysisAssemblerTest.kt)
  - Covers how metadata is turned into local scan rows and reasons for web pages, PDF URLs, archive URLs, and APK URLs.

### Remote reputation
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/NoOpUrlReputationRepositoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/NoOpUrlReputationRepositoryTest.kt)
  - Covers the not-configured path.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/CompositeUrlReputationRepositoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/CompositeUrlReputationRepositoryTest.kt)
  - Covers empty composition, clean/clean, clean/malicious, suspicious/clean, error handling, not-configured merging, and deduplication.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/UrlHausReputationRepositoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/UrlHausReputationRepositoryTest.kt)
  - Covers missing API key, clean response, malicious response, unknown response, HTTP error, and thrown exception.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/google/GoogleSafeBrowsingUrlReputationRepositoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/google/GoogleSafeBrowsingUrlReputationRepositoryTest.kt)
  - Covers missing API key, clean response, malicious response, Google malware test URL, HTTP error, and thrown exception.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/UrlReputationRepositoryFactoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/UrlReputationRepositoryFactoryTest.kt)
  - Covers provider selection and composite repository creation.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/RemoteReputationConfigFactoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/data/reputation/RemoteReputationConfigFactoryTest.kt)
  - Covers optional API-key normalization and provider enablement.

### Final result assembly and presentation
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/AnalyzeQrSafetyUseCaseTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/AnalyzeQrSafetyUseCaseTest.kt)
  - Covers the end-to-end shared analysis flow, parallel metadata/reputation checks, dangerous scheme rejection, bare-domain normalization, remote-malicious danger escalation, and non-URL short-circuiting.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/ResultExtractorsTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/ResultExtractorsTest.kt)
  - Covers result-row formatting for URLs, PDFs, archives, unknown binary files, SMS, Wi-Fi, and selected display strings.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/ResultUiStateTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/ResultUiStateTest.kt)
  - Covers open-button gating, openable URL exposure, non-URL suppression, and section projection.

### Koin / integration smoke tests
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/core/network/QrGuardianHttpClientFactoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/core/network/QrGuardianHttpClientFactoryTest.kt)
  - Covers that the shared HTTP client factory can create a working client.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/core/security/QrGuardianSecurityPipelineFactoryTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/core/security/QrGuardianSecurityPipelineFactoryTest.kt)
  - Covers factory-created analysis with empty config, Google-only config, URLhaus-only config, and both providers together.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/di/QrGuardianKoinTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/di/QrGuardianKoinTest.kt)
  - Covers Koin startup, successful use-case resolution, configured provider wiring, and override behavior.

### Dataset regression
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/QrSampleDatasetRegressionTest.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/QrSampleDatasetRegressionTest.kt)
  - Runs the stored QR payload dataset through the analysis pipeline and verifies content type, levels, open-button behavior, openable URL, local reasons, and local metadata.
- [`shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/fixtures/QrSampleCases.kt`](/Users/leti/Documents/qr-guardian/shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/fixtures/QrSampleCases.kt)
  - Defines the current dataset payloads and expectations.

## 2. Existing coverage by behavior

| Area | Behavior | Covered? | Current test file(s) | Notes |
|---|---|---:|---|---|
| Raw content / normalization | trims surrounding whitespace | Yes | `DefaultQrTextNormalizerTest` | Directly covered. |
| Raw content / normalization | preserves meaningful content | Yes | `DefaultQrTextNormalizerTest` | Long-text preservation is covered. |
| Raw content / normalization | handles empty/blank values | Yes | `DefaultQrTextNormalizerTest`, `AnalyzeQrSafetyUseCaseTest` | Blank input is covered at the normalizer level. |
| Raw content / normalization | handles malformed input safely | Partial | `DefaultQrTextNormalizerTest`, `AnalyzeQrSafetyUseCaseTest` | Null-character removal is covered; broader malformed Unicode/control payloads are not. |
| Content classification | web URL | Yes | `DefaultQrContentClassifierTest` | `http` and `https` classification are covered. |
| Content classification | non-web URL | Partial | `DefaultQrContentClassifierTest`, `AnalyzeQrSafetyUseCaseTest` | Bare domains are treated as URLs; custom schemes are not broadly classified beyond dangerous-scheme rejection. |
| Content classification | plain text | Yes | `DefaultQrContentClassifierTest`, `TypeSpecificSecurityAnalyzerTest` | Covered in both classifier and local analyzer paths. |
| Content classification | email | Yes | `DefaultQrContentClassifierTest`, `TypeSpecificSecurityAnalyzerTest` | Covered. |
| Content classification | phone | Yes | `DefaultQrContentClassifierTest`, `TypeSpecificSecurityAnalyzerTest` | Covered. |
| Content classification | SMS | Yes | `DefaultQrContentClassifierTest`, `TypeSpecificSecurityAnalyzerTest` | Covered. |
| Content classification | Wi-Fi | Yes | `DefaultQrContentClassifierTest`, `TypeSpecificSecurityAnalyzerTest` | Covered. |
| Content classification | unknown barcode content | No | None found | No explicit regression case for an unknown barcode payload. |
| URL scheme safety | HTTPS | Yes | `UrlLocalSecurityAnalyzerTest`, `AnalyzeQrSafetyUseCaseTest` | Covered as safe or preferred. |
| URL scheme safety | HTTP | Yes | `UrlLocalSecurityAnalyzerTest`, `AnalyzeQrSafetyUseCaseTest` | Covered as suspicious and openable. |
| URL scheme safety | `javascript:` | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Blocked before URL work starts. |
| URL scheme safety | `data:` | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Blocked before URL work starts. |
| URL scheme safety | `file:` | No | None found | Not covered by a dedicated regression case. |
| URL scheme safety | custom schemes | Partial | `AnalyzeQrSafetyUseCaseTest` | Dangerous custom schemes are blocked; benign custom-scheme handling is not directly exercised. |
| URL scheme safety | uppercase/mixed-case schemes | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Covered via mixed-case `javascript:` blocking. |
| URL host analysis | normal domain | Yes | `UrlLocalSecurityAnalyzerTest`, `KtorUrlMetadataRepositoryTest`, `QrSampleDatasetRegressionTest` | Covered heavily. |
| URL host analysis | IP literal | Yes | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | Covered with IPv4 literals. |
| URL host analysis | private IP | Yes | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | `192.168.x.x` is covered. |
| URL host analysis | localhost | Partial | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | Covered as suspicious, but private-host trust distinctions are still a separate gap. |
| URL host analysis | suspicious TLD | No | None found | No dedicated coverage for suspicious TLD heuristics. |
| URL host analysis | punycode / Unicode | Partial | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | Covered as suspicious via existing heuristic coverage, but no dedicated IDN/Unicode spoofing rule exists yet. |
| URL host analysis | very long host | No | None found | Long URL path is covered, but not a long host specifically. |
| URL host analysis | missing host | Partial | `DefaultQrContentClassifierTest`, `UrlLocalSecurityAnalyzerTest` | Missing-host edge cases are not explicitly asserted. |
| Shorteners and suspicious URL shape | known shortener | Yes | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | Covered with `bit.ly`. |
| Shorteners and suspicious URL shape | long URL | Yes | `UrlLocalSecurityAnalyzerTest` | Covered via length threshold. |
| Shorteners and suspicious URL shape | misleading URL | Partial | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | `@` and brand-impersonation patterns are covered, but not a broader misleading-URL matrix. |
| Shorteners and suspicious URL shape | credentials in URL | Yes | `UrlLocalSecurityAnalyzerTest`, `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Covered as a misleading URL pattern. |
| Shorteners and suspicious URL shape | suspicious query parameters if implemented | Yes | `UrlLocalSecurityAnalyzerTest`, `QrSampleDatasetRegressionTest` | Many query params are covered. |
| File/download detection | `.pdf` | Yes | `KtorUrlMetadataRepositoryTest`, `QrSafetyAnalysisAssemblerTest`, `AnalyzeQrSafetyUseCaseTest` | Covered end to end. |
| File/download detection | `.apk` | Yes | `UrlLocalSecurityAnalyzerTest`, `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered as dangerous. |
| File/download detection | `.exe` | Yes | `QrSampleDatasetRegressionTest` | Dataset coverage exists. |
| File/download detection | `.zip` | Yes | `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered as archive/download. |
| File/download detection | image files | Partial | `KtorUrlMetadataRepositoryTest`, `ResultExtractorsTest` | Formatting logic exists, but there is no direct regression sample for image URL metadata. |
| File/download detection | media files | Partial | `ResultExtractorsTest` | Display logic exists, but no direct metadata regression sample was found. |
| File/download detection | archive files | Yes | `KtorUrlMetadataRepositoryTest`, `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered. |
| File/download detection | installer/executable files | Yes | `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest`, `GoogleSafeBrowsingUrlReputationRepositoryTest` | Covered for APK/EXE and security impact. |
| File/download detection | unknown binary | Yes | `KtorUrlMetadataRepositoryTest`, `ResultExtractorsTest` | Covered via `application/octet-stream`. |
| File/download detection | URL path extension fallback | Yes | `KtorUrlMetadataRepositoryTest`, `AnalyzeQrSafetyUseCaseTest` | Covered when HEAD is empty, disallowed, or transport fails. |
| File/download detection | `Content-Type` | Yes | `KtorUrlMetadataRepositoryTest` | Covered. |
| File/download detection | `Content-Disposition` | Yes | `KtorUrlMetadataRepositoryTest` | Covered. |
| File/download detection | redirect final URL | Yes | `KtorUrlMetadataRepositoryTest`, `QrSampleDatasetRegressionTest` | Covered with a redirected PDF destination and resolved file rows. |
| File/download detection | missing metadata / HEAD failure | Yes | `KtorUrlMetadataRepositoryTest`, `AnalyzeQrSafetyUseCaseTest` | `405` fallback and transport failure fallback are covered. |
| Local Scan result | safe result | Yes | `DefaultLocalScanAnalyzerTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Local Scan result | warning result | Yes | `DefaultLocalScanAnalyzerTest`, `UrlLocalSecurityAnalyzerTest`, `TypeSpecificSecurityAnalyzerTest` | Covered. |
| Local Scan result | danger result | Yes | `DefaultLocalScanAnalyzerTest`, `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Local Scan result | risk reasons | Yes | `UrlLocalSecurityAnalyzerTest`, `TypeSpecificSecurityAnalyzerTest`, `QrSafetyAnalysisAssemblerTest`, `AnalyzeQrSafetyUseCaseTest` | Covered, but not every heuristic has a dedicated assertion. |
| Local Scan result | action row | Partial | `ResultExtractorsTest`, `ResultUiStateTest` | The label/value mapping is covered, not the rendered composable itself. |
| Local Scan result | file/resource rows | Yes | `QrSafetyAnalysisAssemblerTest`, `ResultExtractorsTest`, `QrSampleDatasetRegressionTest` | Covered for PDF/archive/APK/unknown binary. |
| Local Scan result | openable URL | Yes | `AnalyzeQrSafetyUseCaseTest`, `ResultUiStateTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Local Scan result | blocked URL | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Dangerous schemes and dangerous file URLs are covered. |
| Remote Reputation | clean provider result | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, `CompositeUrlReputationRepositoryTest` | Covered. |
| Remote Reputation | malicious provider result | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, `CompositeUrlReputationRepositoryTest` | Covered. |
| Remote Reputation | suspicious provider result | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `CompositeUrlReputationRepositoryTest` | Covered. |
| Remote Reputation | provider not configured | Yes | `NoOpUrlReputationRepositoryTest`, `RemoteReputationConfigFactoryTest`, `UrlReputationRepositoryFactoryTest` | Covered. |
| Remote Reputation | provider network failure | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, `CompositeUrlReputationRepositoryTest` | Error/unavailable handling is covered. |
| Remote Reputation | provider parsing failure | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest` | Malformed payloads are now directly covered as error fallback. |
| Remote Reputation | multiple providers combined | Yes | `CompositeUrlReputationRepositoryTest`, `QrGuardianSecurityPipelineFactoryTest` | Covered. |
| Remote Reputation | local safe + remote malicious | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrGuardianSecurityPipelineFactoryTest` | Covered with safe local analysis and malicious remote reputation. |
| Remote Reputation | local warning + remote clean | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrGuardianSecurityPipelineFactoryTest` | Covered. |
| Remote Reputation | remote unavailable but local result still shown | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSampleDatasetRegressionTest` | Covered through error/unconfigured paths. |
| Final result assembly | overall level calculation | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Final result assembly | local scan section | Yes | `AnalyzeQrSafetyUseCaseTest`, `QrSafetyAnalysisAssemblerTest`, `ResultUiStateTest` | Covered. |
| Final result assembly | remote reputation section | Yes | `AnalyzeQrSafetyUseCaseTest`, `ResultUiStateTest` | Covered. |
| Final result assembly | section titles | Partial | `ResultUiStateTest`, `ResultExtractorsTest` | Titles are asserted in some tests, but not across all section states. |
| Final result assembly | section descriptions | Partial | `ResultUiStateTest`, `QrSafetyAnalysisAssemblerTest` | Descriptions are covered in a few paths, not exhaustively. |
| Final result assembly | rows shown for normal URLs | Yes | `ResultExtractorsTest`, `QrSafetyAnalysisAssemblerTest` | Covered. |
| Final result assembly | rows shown for file/download URLs | Yes | `ResultExtractorsTest`, `QrSafetyAnalysisAssemblerTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Final result assembly | rows shown for dangerous schemes | Yes | `AnalyzeQrSafetyUseCaseTest`, `ResultExtractorsTest`, `ResultUiStateTest`, `QrSampleDatasetRegressionTest` | Blocked-scheme rows and blocked local signal messaging are directly covered. |
| Final result assembly | open-button visibility | Yes | `ResultUiStateTest`, `QrSampleDatasetRegressionTest` | Covered. |
| Presentation state | `ResultUiState.showOpenButton` | Yes | `ResultUiStateTest`, `QrSampleDatasetRegressionTest` | Covered directly. |
| Presentation state | `ResultUiState.openableUrl` | Yes | `ResultUiStateTest`, `QrSampleDatasetRegressionTest` | Covered directly. |
| Presentation state | no duplicated open-button decision in composables | Partial | `ResultUiStateTest`, `ResultExtractorsTest` | The state behavior is covered, but there is no direct composable-level regression test proving the composable never re-implements the gate. |
| Presentation state | result screen receives enough information without recalculating security rules | Partial | `ResultUiStateTest`, `ResultExtractorsTest` | The projections are tested, but not the full screen rendering path. |
| DI / integration | Koin starts successfully | Yes | `QrGuardianKoinTest` | Covered. |
| DI / integration | `AnalyzeQrSafetyUseCase` resolves | Yes | `QrGuardianKoinTest` | Covered. |
| DI / integration | optional remote config is wired correctly | Yes | `RemoteReputationConfigFactoryTest`, `QrGuardianKoinTest`, `QrGuardianSecurityPipelineFactoryTest` | Covered. |
| DI / integration | no real API keys required in tests | Yes | `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, `QrGuardianKoinTest` | Covered via empty keys and mock engines. |
| DI / integration | no real network calls in unit tests | Yes | `KtorUrlMetadataRepositoryTest`, `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, `QrGuardianKoinTest` | Covered with `MockEngine`. |

## 3. Regression gaps

### High priority

| Missing behavior | Why it matters | Suggested test file | Suggested test type | Production code changes needed? |
|---|---|---|---|---|

No remaining high-priority gaps are identified in this batch.

### Medium priority

| Missing behavior | Why it matters | Suggested test file | Suggested test type | Production code changes needed? |
|---|---|---|---|---|
| Private `localhost`/private-IP distinctions | Improves trust in host heuristics and reduces false confidence. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/UrlLocalSecurityAnalyzerTest.kt` | unit test | No |
| Suspicious TLD heuristics | Adds coverage for obvious lookalike domains without changing business logic. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/UrlLocalSecurityAnalyzerTest.kt` | unit test | No |
| Very long host regression | Long-host attacks are different from long-path attacks. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/analyzer/UrlLocalSecurityAnalyzerTest.kt` | unit test | No |
| Composable-level open-button rendering | The current tests stop at state and extractor logic; a render-level check would close the gap. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/` | integration-style commonTest | No |

### Low priority

| Missing behavior | Why it matters | Suggested test file | Suggested test type | Production code changes needed? |
|---|---|---|---|---|
| Image and media metadata regressions | Useful for completeness, but lower risk than safety-gating bugs. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/usecase/QrSafetyAnalysisAssemblerTest.kt` | dataset regression test | No |
| Unknown barcode payloads | Good for completeness, but lower risk if the classifier already defaults safely. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/domain/classifier/DefaultQrContentClassifierTest.kt` | unit test | No |
| Dedicated screen rendering snapshots | Nice for demonstration, but not needed for this regression strategy. | `shared/src/commonTest/kotlin/com/lmartin/qrguardian/presentation/result/` | avoid for now | Yes, if snapshot infra were introduced |

## 4. Recommended test plan

1. Add missing high-risk local URL regression cases.
   - Focus on `localhost`, punycode/Unicode, and private-host distinctions.
   - Independently testable in `UrlLocalSecurityAnalyzerTest` and `AnalyzeQrSafetyUseCaseTest`.
2. Add metadata-focused file/download regressions.
   - Cover image/media metadata examples and other remaining file-type edge cases.
   - Keep these in `KtorUrlMetadataRepositoryTest` and `QrSafetyAnalysisAssemblerTest`.
3. Add remote reputation failure and malicious-composition regressions.
   - Cover local warning + remote clean, plus any remaining provider edge cases.
   - Keep these in `GoogleSafeBrowsingUrlReputationRepositoryTest`, `UrlHausReputationRepositoryTest`, and `AnalyzeQrSafetyUseCaseTest`.
4. Add result-assembly and presentation regressions.
   - Verify section titles/descriptions for key states and open-button gating remains derived from `ResultUiState`.
   - Keep these in `ResultExtractorsTest`, `ResultUiStateTest`, and `QrSampleDatasetRegressionTest`.
5. Expand the dataset regression fixture only after the focused tests are in place.
   - This keeps the dataset readable and avoids turning it into an unstructured catch-all.

## 5. Suggested dataset additions

| Sample name | Raw value | Expected content type | Expected level | Expected open button | Expected local notes | Expected remote notes |
|---|---|---|---|---|---|---|
| Safe HTTPS web URL | `https://example.com` | `Url` | `Safe` | `Yes` | Web page, no suspicious local reasons | Remote reputation not configured or clean, depending on test harness |
| HTTP web URL | `http://example.com` | `Url` | `Suspicious` | `Yes` | Missing HTTPS reason | Remote reputation not configured or clean, depending on test harness |
| JavaScript scheme | `javascript:alert(1)` | `Unknown` | `Dangerous` | `No` | Blocked scheme | Remote not applicable |
| Data scheme | `data:text/html;base64,PHNjcmlwdD4=` | `Unknown` | `Dangerous` | `No` | Blocked scheme | Remote not applicable |
| PDF URL | `https://example.com/file.pdf` | `Url` | `Safe` | `Yes` | PDF document, file type PDF | Remote reputation not configured or clean, depending on test harness |
| APK URL | `https://example.com/app.apk` | `Url` | `Dangerous` | `No` | Android app, dangerous file type | Remote not applicable if local danger blocks opening |
| EXE URL | `https://example.com/setup.exe` | `Url` | `Dangerous` | `No` | Windows executable, dangerous file type | Remote not applicable if local danger blocks opening |
| ZIP URL | `https://example.com/archive.zip` | `Url` | `Suspicious` | `Yes` | Archive, download-like file | Remote reputation not configured or clean, depending on test harness |
| Known shortener URL | `https://bit.ly/example` | `Url` | `Suspicious` | `Yes` | Shortener reason | Remote reputation not configured or clean, depending on test harness |
| Private IP URL | `http://192.168.1.10/login` | `Url` | `Suspicious` | `Yes` | IPv4/private-IP reason | Remote reputation not configured or clean, depending on test harness |
| Localhost URL | `http://localhost/admin` | `Url` | `Suspicious` | `Yes` | Localhost-specific reason | Remote reputation not configured or clean, depending on test harness |
| Punycode / Unicode suspicious URL | `https://xn--pple-43d.com/login` | `Url` | `Suspicious` | `Yes` | Suspicious via existing heuristics; IDN-specific heuristics are still not explicit | Remote reputation not configured or clean, depending on test harness |
| Plain text | `Hello from QR Guardian` | `PlainText` | `Unknown` | `No` | Plain-text local scan | Remote not applicable |
| Email-like content | `mailto:test@example.com?subject=Hello` | `Email` | `Suspicious` | `No` | Sensitive action / prefilled content | Remote not applicable |
| Phone-like content | `tel:+34600000000` | `Phone` | `Suspicious` | `No` | Sensitive action | Remote not applicable |
| Metadata HEAD failure | `https://example.com/menu.pdf` | `Url` | `Safe` or `Suspicious` depending on local rules | `Yes` | File inferred from path even when HEAD fails | Remote reputation not configured or clean, depending on test harness |
| Remote malicious URL | `https://example.com` | `Url` | `Dangerous` | `No` | Local result may be safe | Remote malicious reason/category |
| Remote provider unavailable | `https://example.com` | `Url` | local result only | `Yes` if local result is openable | Local scan stays visible | Remote unavailable/not configured reason |

## 6. What already looks solid

- The shared test suite already covers the main local analysis path.
- URL metadata inference is well covered for the current file/download cases.
- Both remote reputation providers have good fake-network coverage for success, failure, and missing-key cases.
- `ResultUiState` already has focused behavior tests for open-button gating.
- The dataset regression test is a strong base for future additions because it exercises the pipeline end to end without real network calls.
