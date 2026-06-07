# Local Security Checks

QR Guardian runs with local security checks enabled by default.

For optional remote reputation details, see [Remote Reputation](remote-reputation.md).

## Current Behavior

- The app normalizes and classifies scanned text locally.
- Local checks evaluate URLs and sensitive QR payloads without any backend.
- URLs also get a HEAD metadata check so the app can show destination details before opening anything.
- Download-like links can be identified as file metadata, including PDF and menu URLs.
- Executable and script downloads are treated as high risk.
- Remote reputation checks are optional and apply only to URLs.
- Non-URL payloads do not trigger HEAD or remote reputation checks.
- No API keys are required to run the project.
- No backend is required to run the project.
- Runtime wiring is centralized in Koin, but security pipeline composition stays in `QrGuardianSecurityPipelineFactory`.
- Android and iOS provide `RemoteReputationConfig` explicitly before initializing Koin.

## Current Repository Contract

The remote reputation layer is represented by a repository contract and a no-op implementation:
- `UrlReputationRepository`
- `NoOpUrlReputationRepository`

The remote section uses `NotApplicable` for non-URL payloads and `NotConfigured` when no provider is enabled.

## Practical Notes

- PDF and menu URLs are surfaced as file metadata, not automatically blocked as dangerous.
- Executable and script downloads deserve stronger warnings because they can lead to malware.
- HEAD responses depend on server support, so some URLs may not expose complete metadata.

## Future Integration

When a real provider is added, the use case should merge local and remote signals without letting remote clean results override local suspicious findings.
