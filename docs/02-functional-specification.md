# Functional Specification

## App Goal
QR Guardian helps users scan QR codes and barcodes and decide safely what to do with scanned content, especially URLs.

## Supported Content Types
- URL
- Plain text
- Email
- Phone number
- WiFi QR
- Barcode product code
- Unknown content

## Main User Flows
1. User opens app and sees intro/launch screen.
2. User taps primary CTA to start scanning.
3. App enters camera capture flow.
4. App detects QR/barcode content.
5. App analyzes content type.
6. If content is URL, app checks safety status.
7. App shows result screen and user chooses next action.

## Main Screens
- Intro / Launch Screen
- Camera Capture Screen
- Result Screen

## Expected Actions
- Start scanning
- Open URL (only after result and explicit user action)
- Copy content
- Share content
- Scan again
- Return to intro/home

## Functional Requirements
- The app must show an intro screen.
- The app must allow starting a scan.
- The app must scan QR codes.
- The app must scan barcodes.
- The app must detect content type.
- The app must analyze URLs before opening them.
- The app must show a result screen.
- The app must allow copying scanned content.
- The app must allow scanning again.
- The app must not automatically open URLs.

## Non-Functional Requirements
- Shared business logic in KMP.
- Shared UI with Compose Multiplatform where possible.
- Clean and testable architecture.
- Simple and visual UX.
- Security-aware behavior.

## MVP Non-Goals
- User accounts
- Payments
- Ads
- Analytics
- Push notifications
- Complex backend dashboard
- Full antivirus-style scanning
- Automatic opening of links
