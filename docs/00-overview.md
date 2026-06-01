# QR Guardian Overview

## Project
QR Guardian is a Kotlin Multiplatform mobile application for Android and iOS.

Visible app name: **QR Guardian**  
Tagline: **Scan smarter. Open safer.**  
Main package: `com.lmartin.qrguardian`

## What It Does
QR Guardian scans QR codes and barcodes, detects the scanned content type, and helps users avoid opening potentially malicious URLs.

The app always shows analysis results first. It does not automatically open scanned URLs.

## Target Platforms
- Android
- iOS

## Main Value Proposition
- Fast, simple scan experience.
- Clear content classification (URL, text, email, phone, WiFi, barcode, unknown).
- Security-first URL flow before opening links.
- Modern mobile UX suitable for production-style portfolio presentation.

## Why This Is a Strong Portfolio Project
- Demonstrates Kotlin Multiplatform architecture decisions.
- Demonstrates Compose Multiplatform shared UI strategy.
- Shows practical clean architecture and testable domain logic.
- Covers mobile security awareness in user-facing product behavior.
- Enables clear progression from mock to production-like integrations.

## High-Level Feature List
- Intro/launch screen with clear security value.
- Camera capture flow for QR and barcode scanning (planned).
- Content type detection and parsing.
- URL safety evaluation flow (fake first, real provider later via backend).
- Result screen with actionable outcomes (copy, share, open with confirmation, scan again).
- Future optional local history of scans.

## Technical Focus
QR Guardian is intentionally focused on:
- Kotlin Multiplatform
- Compose Multiplatform
- Shared business logic
- Mobile security awareness
- Clean architecture
- Visual mobile experience
