# Architecture

QR Guardian uses a small Kotlin Multiplatform architecture built around shared analysis logic, shared Compose UI, and platform-specific scanner/configuration wiring.

The goal is practical separation of responsibilities, not heavy architecture ceremony.

## Modules

- `androidApp`: Android entry point, Android build configuration, camera permission handling, and platform app root.
- `shared`: Kotlin Multiplatform code for domain logic, data repositories, Compose UI, state holders, Koin wiring, and platform-specific scanner/network implementations.
- `iosApp`: iOS Xcode project and iOS configuration files.

Main package:
- `com.lmartin.qrguardian`

## Shared Package Structure

The shared module follows a simple layered package structure:
- `core`: platform-independent support code and shared factories.
- `domain`: models, analyzers, classifiers, repository contracts, and use cases.
- `data`: Ktor-backed metadata and remote reputation implementations.
- `presentation`: Compose screens, UI state, text mapping, and state holders.
- `di`: Koin initialization and platform network module binding.

## Platform Responsibilities

Platform-specific code is kept at the edges:
- Android handles Android camera permission, Android scanner integration, `BuildConfig` provider values, and the Android Ktor engine.
- iOS handles iOS camera permission, iOS scanner integration, `Info.plist` provider values, and the iOS Ktor engine.

The shared domain model does not expose Android or iOS platform types.

## Scan-To-Result Flow

1. Platform scanner emits raw scanned text.
2. `App` sends the text into `AnalyzeQrSafetyUseCase`.
3. The use case normalizes and classifies the payload.
4. Dangerous schemes are blocked before URL work starts.
5. Local Scan evaluates the payload.
6. URL payloads run metadata and remote reputation checks in shared code.
7. `QrSafetyAnalysisAssembler` combines local, metadata, and remote signals into result sections.
8. The final `QrAnalysisResult` exposes the detected content, overall level, section results, and optional openable URL.
9. `ResultUiState` projects the analysis for Compose.
10. The result screen renders state and does not re-decide security rules.

## Key Shared Components

### `AnalyzeQrSafetyUseCase`

This is the main analysis entry point. It coordinates:
- text normalization
- content classification
- dangerous scheme detection
- local scan analysis
- URL metadata lookup
- optional remote reputation lookup
- final result assembly

### Local Scan

Local Scan is always enabled. It checks suspicious URL features, sensitive QR payload types, dangerous schemes, and file/download signals without requiring API keys.

### URL Metadata

`KtorUrlMetadataRepository` performs HEAD metadata requests for URL payloads. It maps available headers and redirect information into file/resource metadata, then falls back to path-based inference when metadata is unavailable.

### Remote Reputation

Remote reputation is implemented behind `UrlReputationRepository`. Current optional provider implementations include:
- `GoogleSafeBrowsingUrlReputationRepository`
- `UrlHausReputationRepository`
- `CompositeUrlReputationRepository`
- `NoOpUrlReputationRepository`

Remote checks are URL-only and remain disabled when keys are blank.

### `QrSafetyAnalysisAssembler`

The assembler turns local scan output, URL metadata, and remote reputation results into display-ready `ScanSectionResult` values. This keeps result section titles, descriptions, metadata rows, and reason strings in one shared place.

### `QrAnalysisResult`

`QrAnalysisResult` is the final shared model passed to presentation. It contains:
- original and normalized content
- detected content type
- overall security level
- whether opening is allowed
- optional openable URL
- Local Scan section
- Remote Reputation section

### `ResultUiState`

`ResultUiState` projects `QrAnalysisResult` into result-screen state. It owns the open-button gate through `showOpenButton`, so composables render the decision instead of recalculating it.

## Dependency Injection Boundary

Koin is used only at the app wiring boundary.

`QrGuardianSecurityPipelineFactory` is the explicit composition point for the analysis pipeline. Koin provides platform dependencies and configuration, then delegates pipeline construction to the factory.

This keeps:
- domain classes framework-independent
- provider selection explicit
- platform HTTP engine wiring isolated
- tests able to construct and exercise the pipeline directly

## Why This Shape

The architecture is intentionally simple because the project is a mobile portfolio app with a focused product scope.

The current structure avoids:
- unnecessary module splitting
- a dependency injection framework inside domain logic
- platform APIs leaking into shared models
- UI code making security decisions
- provider-specific details leaking directly into screens
