# Agent Task Backlog

## Agent Task 01 - Initial Project Structure
**Goal**
- Align baseline structure for QR Guardian.

**Scope**
- Ensure package is `com.lmartin.qrguardian`.
- Create/align base packages.
- Update README.
- Review `.gitignore`.

**Out of scope**
- Implement screens.
- Add scanner.
- Add backend.

**Definition of Done**
- Package and structure aligned.
- Documentation/README updated.
- Build remains healthy for touched scope.

## Agent Task 02 - Intro Screen
**Goal**
- Deliver intro/launch screen and basic navigation entry.

**Scope**
- Create intro screen.
- Add simple navigation.
- Add app title and CTA.

**Out of scope**
- Camera implementation.
- URL analysis implementation.

**Definition of Done**
- Intro screen visible on launch.
- CTA navigates to scan flow placeholder/screen.

## Agent Task 03 - Mock Analysis Flow
**Goal**
- Implement result flow without real scanner.

**Scope**
- Add domain models.
- Add fake scanned contents.
- Add content type detection.
- Add fake URL safety result.
- Add result screen.

**Out of scope**
- Real camera integration.
- Real provider integration.

**Definition of Done**
- End-to-end mock flow works from fake scan input to result UI.
- Unit tests cover core use cases.

## Agent Task 04 - Camera Capture Screen
**Goal**
- Add real capture flow.

**Scope**
- Add camera screen.
- Add permissions.
- Add QR/barcode scanner.
- Connect scanned result to analysis.

**Out of scope**
- Real URL reputation provider integration.

**Definition of Done**
- Scan produces real payload and navigates to result.
- Permission denial state handled.

## Agent Task 05 - URL Safety Backend
**Goal**
- Establish backend intermediary for safety checks.

**Scope**
- Define backend endpoint.
- Implement backend intermediary.
- Keep API keys outside mobile app.

**Out of scope**
- Multi-provider orchestration beyond MVP.

**Definition of Done**
- Mobile app can request safety status from backend contract.
- Secrets are not embedded in client code.

## Agent Task 06 - Real Provider Integration
**Goal**
- Integrate selected provider through backend.

**Scope**
- Integrate provider.
- Map provider response to app statuses.

**Out of scope**
- UI redesign unrelated to safety flow.

**Definition of Done**
- Real provider responses produce normalized in-app safety states.

## Agent Task 07 - Local History
**Goal**
- Provide local scan history.

**Scope**
- Persist scan history.
- Display recent scans.

**Out of scope**
- Cloud sync and cross-device sync.

**Definition of Done**
- User can view recent scans and clear history.

## Agent Task 08 - Portfolio Polish
**Goal**
- Improve project presentation quality.

**Scope**
- Improve UI polish.
- Add screenshots.
- Improve README.
- Add final documentation improvements.

**Out of scope**
- New large product features.

**Definition of Done**
- Repo presentation is portfolio-ready with clear visuals and technical narrative.
