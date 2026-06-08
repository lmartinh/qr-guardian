# QR Guardian Overview

QR Guardian is a Kotlin Multiplatform mobile app for Android and iOS. It scans QR codes and barcodes, classifies the scanned content, runs local security checks, optionally checks remote URL reputation providers, and shows a result before the user opens anything.

The project is designed as a professional portfolio piece: it demonstrates KMP architecture, Compose Multiplatform UI, platform scanner integration, local security heuristics, optional provider configuration, and focused shared test coverage.

## Purpose

QR codes are convenient, but they can hide destinations or actions that are not obvious before scanning. QR Guardian helps users pause before opening scanned content by showing:
- what was scanned
- what type of content it appears to be
- whether local checks found warning signals
- whether optional remote reputation checks reported anything for URLs
- whether opening the URL is allowed by the result policy

The app helps identify suspicious QR/barcode content. It does not guarantee that a URL, file, or destination is safe.

## Platforms

- Android app module: `androidApp`
- Shared Kotlin Multiplatform module: `shared`
- iOS app project: `iosApp/iosApp.xcodeproj`
- Main package: `com.lmartin.qrguardian`

## Core Flow

1. User opens QR Guardian.
2. User starts scanning from the intro screen.
3. Platform scanner captures a QR/barcode payload.
4. Shared KMP logic normalizes and classifies the content.
5. Local Scan runs for every payload.
6. URL payloads can also run HEAD metadata checks and optional Remote Reputation.
7. The result screen shows the analysis before any open action is available.

## Local-Only Mode

Local-only mode works without API keys. It is the default behavior when remote provider values are empty or missing.

Local Scan covers classification, dangerous schemes, suspicious URL patterns, file/download inference, and metadata fallback behavior. It does not call external reputation services.

## Optional Remote Reputation

Remote Reputation applies only to URLs and is enabled only when provider keys are configured.

Supported optional providers:
- Google Safe Browsing
- URLhaus

Remote results are additional context. Provider failures and clean provider responses do not prove that a destination is safe.

## Portfolio Value

QR Guardian is useful as a CV/portfolio project because it combines:
- Kotlin Multiplatform shared business logic
- Compose Multiplatform UI
- Android and iOS scanner integration
- local security heuristics with clear user-facing results
- optional remote provider wiring without committing secrets
- Koin kept at the app wiring boundary
- regression tests concentrated in shared code
