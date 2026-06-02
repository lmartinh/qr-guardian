# Local Security Checks

QR Guardian runs with local security checks only by default.

For optional remote reputation details, see [Remote Reputation](remote-reputation.md).

## Current Behavior
- The app normalizes and classifies scanned text locally.
- The local scan block evaluates URLs and sensitive QR payloads without any backend.
- URLs also get a HEAD metadata check so the app can show destination details before opening anything.
- Remote reputation checks are optional and only apply to URLs.
- Non-URL payloads do not trigger HEAD or remote reputation checks.
- No API keys are required to run the project.
- No backend is required to run the project.
- Runtime wiring is centralized in `QrGuardianHttpClientFactory` and `QrGuardianSecurityPipelineFactory`.

## Remote Reputation Readiness
The project is prepared for future optional providers such as:
- Google Safe Browsing
- VirusTotal
- URLhaus
- PhishTank

Each developer should configure their own API keys in a future iteration.

Those keys must not be committed to this repository and should not be hardcoded in a mobile client.

For production use, a backend or proxy is still the safer integration point because mobile binaries can be inspected and secrets extracted.

## Current Repository Contract
The remote reputation layer is represented by a repository contract and a no-op implementation:
- `UrlReputationRepository`
- `NoOpUrlReputationRepository`

The remote section uses `NotApplicable` for non-URL payloads and `NotConfigured` when no provider is enabled.

## Future Integration
When a real provider is added, the use case should merge local and remote signals without letting remote `Clean` results override local suspicious findings.
