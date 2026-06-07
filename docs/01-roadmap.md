# QR Guardian Roadmap

## Phases

### Phase 0 - Project Foundation
**Goal**
- Prepare package structure.
- Create AGENTS.md.
- Create documentation.
- Ensure .gitignore is correct.
- Keep project buildable.

**Expected output**
- Clean KMP project.
- Main package `com.lmartin.qrguardian`.
- Basic README.
- `/docs` folder.
- Agent instructions.

### Phase 1 - Intro Screen and Navigation
**Goal**
- Implement intro/launch screen.
- Add navigation structure.
- Create first visual identity.
- Add fake navigation to scan screen.

**Expected output**
- Intro screen.
- Basic navigation.
- Shared Compose UI.

### Phase 2 - Mock QR Analysis Flow
**Goal**
- Implement fake scan result flow without camera.
- Add domain models.
- Add content type detection.
- Add fake URL safety analysis.

**Expected output**
- QR analysis domain models.
- Use cases.
- Fake repository.
- Result screen with mocked data.
- Unit tests.

### Phase 3 - Camera and QR/Barcode Scanner
**Goal**
- Add camera permissions.
- Implement QR/barcode scanning.
- Connect real scan result to analysis flow.

**Expected output**
- Camera capture screen.
- Permission handling.
- Scanner integration.
- Android implementation.
- iOS implementation or documented limitation if delayed.

### Phase 4 - URL Safety Backend
**Goal**
- Add backend intermediary for URL reputation checks.
- Protect provider API keys.
- Return normalized safety result to app.

**Expected output**
- Backend endpoint specification.
- Basic backend implementation.
- KMP client integration.

### Phase 5 - Real URL Safety Provider
**Goal**
- Integrate a real URL reputation provider through backend.
- The current codebase already includes local optional providers for Google Safe Browsing and URLhaus; this phase is about the backend-mediated production path.

**Potential providers**
- PhishTank.
- VirusTotal.
- urlscan.io.

**Expected output**
- Real safety results.
- Provider mapping.
- Error handling.
- Cache strategy if needed.

### Phase 6 - Local History
**Goal**
- Store previous scans locally.
- Show recent scans.

**Expected output**
- History screen or section.
- Local persistence.
- Clear/delete actions.

### Phase 7 - UI Polish and Portfolio Readiness
**Goal**
- Improve visual design.
- Add screenshots.
- Improve README.
- Add architecture diagram if useful.
- Make the project attractive for CV/GitHub.

**Expected output**
- Polished UI.
- Good README.
- Screenshots.
- Technical explanation.
- Final validation.

## Phase Status Table
| Phase | Goal | Status | Notes |
|---|---|---|---|
| 0 | Project Foundation | Pending | Base KMP app and AGENTS exist; docs finalized in this phase. |
| 1 | Intro Screen and Navigation | Pending | No dedicated intro/navigation implementation yet. |
| 2 | Mock QR Analysis Flow | Pending | Domain flow and mocked results still pending. |
| 3 | Camera and QR/Barcode Scanner | Pending | Scanner and permissions not implemented yet. |
| 4 | URL Safety Backend | Pending | No backend endpoint integration yet. |
| 5 | Real URL Safety Provider | Pending | Provider integration intentionally deferred. |
| 6 | Local History | Pending | Persistence/history not started. |
| 7 | UI Polish and Portfolio Readiness | Pending | Final polish and assets pending. |
