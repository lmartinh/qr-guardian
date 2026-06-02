# Local Security Checks

QR Guardian runs with local security checks only by default.

For optional remote reputation details, see [Remote Reputation](remote-reputation.md).

## Current Behavior
- The app normalizes and classifies scanned text locally.
- The local analyzer evaluates URLs and sensitive QR payloads without any backend.
- Remote reputation checks are prepared but not enabled in the current version.
- The current remote reputation repository is a `NoOpUrlReputationRepository`.
- No API keys are required to run the project.
- No backend is required to run the project.

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

The current version only returns `NotConfigured` for remote checks.

## Future Integration
When a real provider is added, the use case should merge local and remote signals without letting remote `Clean` results override local suspicious findings.
