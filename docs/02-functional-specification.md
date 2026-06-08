# Functional Specification

This document describes QR Guardian from a product and behavior point of view. For implementation details, see [Architecture](03-architecture.md) and [Security Model](05-security-model.md).

## App Goal

QR Guardian helps users scan QR codes and barcodes and decide what to do with the result, especially when the scanned content is a URL.

The app never opens scanned content automatically.

## Supported Content Types

The shared classifier handles the main payload families used by the app:
- URL
- Plain text
- Email
- Phone number
- SMS
- WiFi QR
- vCard/contact
- Geo/location
- Crypto/payment URI
- Unknown content

Barcode scanner payloads are analyzed by their text content. The current shared model does not expose a separate product-code content type.

## Main User Flow

1. User opens the app and sees the intro screen.
2. User starts scanning.
3. The camera screen requests permission if needed.
4. The scanner captures a QR/barcode payload.
5. Shared analysis normalizes and classifies the content.
6. Local Scan runs for the payload.
7. URL payloads can run HEAD metadata checks and optional Remote Reputation.
8. The result screen shows the final safety state and available actions.

## Screen Behavior

### Intro

- Shows the app purpose and visual identity.
- Provides the primary start-scanning action.
- Handles denied camera permission by allowing the user to open platform settings.

### Camera

- Shows a live scanner when camera permission is granted.
- Shows permission or denied states when access is missing.
- Navigates to the result flow after a payload is detected and analyzed.

### Result

- Shows the scanned content and detected content type.
- Shows a Local Scan section for all payloads.
- Shows a Remote Reputation section for URL payloads.
- Shows `NotApplicable` for Remote Reputation on non-URL payloads.
- Shows `NotConfigured` when no remote provider keys are configured.
- Allows scanning again.
- Allows opening only when the analyzed result exposes an openable URL.

## Local Scan Behavior

Local Scan is always enabled and does not require API keys.

It checks:
- content classification
- dangerous URL schemes
- HTTP vs HTTPS
- suspicious URL shapes
- credentials or `@` in URLs
- IP literal and local-looking destinations
- shorteners and lookalike signals
- suspicious file extensions
- URL metadata when HEAD is available
- file/download inference when metadata is missing

## Remote Reputation Behavior

Remote Reputation is optional and URL-only.

When configured, the app can check:
- Google Safe Browsing
- URLhaus

When keys are missing or blank, the app remains in local-only mode and the remote section reports that reputation checks are not configured.

When provider calls fail or return malformed payloads, the app keeps the local result visible and marks the remote section unavailable.

## Open Action Policy

- Dangerous results are not openable.
- Safe and suspicious URL results can be openable when the final analysis allows it.
- Non-URL payloads do not expose an open-link action.
- The user must explicitly choose to open a URL after reviewing the result.

## Limitations

- Local rules are heuristics and can miss threats.
- Remote providers can fail or return false negatives.
- A clean remote result is not a guarantee of safety.
- Mobile API keys are not fully secret if embedded in an app binary.
- Production use should prefer a backend/proxy for provider keys.
