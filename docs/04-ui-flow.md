# UI Flow

QR Guardian follows a short security-first journey:

Intro
-> Camera
-> QR/barcode detected
-> Local Scan
-> Remote Reputation, optional
-> Result

The app never opens scanned content automatically. The user always reviews the result first.

## Intro Screen

Purpose:
- Present QR Guardian clearly.
- Explain the scan-first safety flow in a compact way.
- Provide the primary action to start scanning.
- Handle denied camera permission by offering a route to platform settings.

Main elements:
- App name and tagline.
- Short explanatory copy.
- Start-scanning call to action.
- Permission messaging when needed.

## Camera Screen

Purpose:
- Capture QR and barcode content.
- Keep the scan interaction simple and direct.

Main states:
- Permission request or denied state.
- Live camera preview.
- Scan frame and helper text.
- Brief analyzing state after detection.
- Error state for camera unavailability or scan failure.

Navigation:
- Successful scan detection sends the payload through shared analysis.
- The app navigates to the result screen after analysis completes.

## Result Screen

Purpose:
- Show what was scanned.
- Show the detected content type.
- Show Local Scan findings.
- Show Remote Reputation state for URL payloads.
- Make the open action explicit and gated by the final result.

Main content:
- Result header with safe, suspicious, dangerous, or unknown tone.
- Scanned content details.
- Local Scan section.
- Remote Reputation section.
- Action area for opening when allowed and scanning again.

## Result States

- Safe: calm positive presentation for results without warning signals.
- Suspicious: warning presentation for results with caution signals that may still be openable.
- Dangerous: strong caution presentation for blocked/high-risk results.
- Unknown: neutral presentation when content cannot be evaluated precisely.

## Open Button Behavior

The result screen renders the open-button decision from `ResultUiState.showOpenButton`.

- Dangerous URL results are not openable.
- Non-URL payloads do not show an open-link action.
- Safe and suspicious URL results can show the open action when the shared analysis exposes an openable URL.

The composable does not recalculate security rules. It renders the result state produced by shared analysis.

## Visual Direction

QR Guardian uses a clean mobile portfolio style with:
- shared Compose Multiplatform UI
- bright surfaces
- Outfit typography
- clear safety colors for safe, suspicious, dangerous, and unknown states
- restrained use of the lavender brand accent

Dangerous actions should never become the dominant CTA.
