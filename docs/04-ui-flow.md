# UI Flow

## Overview

QR Guardian follows a simple security-first flow:

Intro
→ Camera
→ QR/barcode detected
→ Local Scan
→ Remote Reputation, optional
→ Result

The app never opens scanned content automatically. The user always sees the result first, and the open action is only available for URL results that are not classified as dangerous.

## Intro Screen

Purpose:
- Explain the product value quickly.
- Reinforce the safety-first promise.
- Offer a clear entry point into scanning.

Main elements:
- App name and tagline.
- Short explanatory text.
- Primary call to action to start scanning.

Navigation target:
- Camera screen.

## Camera Screen

Purpose:
- Capture QR and barcode content.
- Guide the user with a clear scan frame.

Main states:
- Permission request or denied state.
- Live camera preview.
- Centered scan frame.
- Short helper text.
- Brief analyzing state after detection.
- Error state for camera unavailability or scan failure.

Navigation target:
- Result screen after the scan has been processed.

## Result Screen

Purpose:
- Show the scanned content.
- Show the local safety result.
- Show optional remote reputation state for URLs.
- Let the user decide whether to open the URL.

Main content:
- Raw scanned content.
- Detected content type.
- Local Scan section.
- Remote Reputation section.
- Recommended action card.

Main actions:
- Open link when allowed.
- Copy content.
- Share content.
- Scan again.

## Safety States

- Safe: calm positive state.
- Suspicious: warning state.
- Dangerous: strong caution state.
- Unknown: uncertain state.

## URL Flow

URL detected
→ Normalize
→ Run local checks
→ Inspect HEAD metadata when available
→ Run remote reputation only if configured
→ Present result
→ User chooses whether to open it

## Implementation Notes

- Local Scan is always enabled.
- Remote Reputation is optional.
- PDF and menu links can appear as file metadata in the result.
- Executable and script downloads should be treated as high risk.
