# Architecture

## Package Root
`com.lmartin.qrguardian`

## Layered Structure
- `core`: shared utilities, platform-independent helpers, common low-level primitives.
- `domain`: business models, use cases, contracts.
- `data`: repository implementations, data sources, mappers.
- `presentation`: UI state models, state holders, screen-level composition.

Suggested packages:
- `com.lmartin.qrguardian.core`
- `com.lmartin.qrguardian.domain`
- `com.lmartin.qrguardian.data`
- `com.lmartin.qrguardian.presentation`

## High-Level Flow
Camera scan  
↓  
Raw scanned content  
↓  
QrAnalysisResult  
↓  
Result screen

Current shared logic normalizes and classifies the payload first, then runs the local scan and optional remote reputation blocks. URL payloads also perform a HEAD metadata check so the result can show destination details separately from the remote provider state.

Koin is used only at the platform app boundary. `QrGuardianSecurityPipelineFactory` composes the pipeline explicitly so the domain layer stays framework-independent and easy to test.

The app now has real camera scanning, local-only security checks by default, and optional provider-backed reputation checks when keys are configured.

## Suggested Domain Models
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

## Expected Use Cases
- DetectQrContentTypeUseCase
- AnalyzeQrContentUseCase
- CheckUrlSafetyUseCase

## Expected Repository Contract
```kotlin
interface UrlSafetyRepository {
    suspend fun checkUrl(url: String): UrlSafetyResult
}
```

This document defines direction only. Implementation is intentionally out of scope for this documentation task.
