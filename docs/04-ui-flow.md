# UI Flow

## 1) Intro / Launch Screen
**Purpose**
- Present QR Guardian value.
- Establish safety-first behavior.
- Provide clear entry into scanning.

**Main content**
- App name: QR Guardian
- Tagline: Scan smarter. Open safer.
- Short explanation of scan + safety flow

**Main CTA**
- Start scanning / Scan QR

**Navigation target**
- Camera Capture Screen

## 2) Camera Capture Screen
**Purpose**
- Capture QR/barcode content.
- Guide user visually during scan.

**States and behavior**
- Permission state: request/handle camera access.
- Camera preview state: show live preview.
- Scan frame: centered visual frame.
- Helper text: e.g. "Place the QR code inside the frame".
- Loading/analyzing state: brief processing state after detection.
- Error state: permission denied, camera unavailable, decode failure.

**Navigation target**
- Result Screen when scan content is detected and processed.

## 3) Result Screen
**Purpose**
- Show scanned content and interpretation.
- Show URL safety status when applicable.
- Enable explicit user actions.

**Main content**
- Raw scanned content
- Parsed value (when available)
- Detected content type
- URL safety status (if URL)
- Recommended action card

**Actions**
- Open URL (after explicit confirmation)
- Copy content
- Share content
- Scan again
- Back to intro/home

## Primary Navigation Flow
Intro Screen
   ↓ Start scanning
Camera Capture Screen
   ↓ QR/barcode detected
Result Screen
   ↓ Scan again
Camera Capture Screen

## URL-Specific Flow
QR contains URL
   ↓
Normalize URL
   ↓
Check safety
   ↓
Show result
   ↓
User chooses whether to open it

## Visual Guidance by Safety State
- Safe: calm positive state.
- Suspicious: warning state.
- Malicious: danger state.
- Unknown: neutral/caution state.

Exact color tokens are intentionally not defined here.
