# Security Model

QR Guardian helps identify suspicious QR/barcode content before the user opens anything. It combines local heuristics, URL metadata, and optional remote reputation checks.

It does not guarantee that a URL, file, or destination is safe.

For detailed rule references, see:
- [Local Security Checks](security/local-security-checks.md)
- [Remote Reputation](security/remote-reputation.md)

## Security Principles

- Scanned content is never opened automatically.
- Local Scan always runs.
- Remote Reputation is optional and URL-only.
- Dangerous content is blocked from opening.
- Suspicious URL content can still be openable when the final policy allows it.
- Unknown or unavailable results are shown clearly instead of hidden.
- Mobile API keys are not treated as fully secret.

## Local Scan

Local Scan runs without API keys or a backend.

It checks:
- content classification
- dangerous URL schemes
- HTTP vs HTTPS
- suspicious URL shapes
- shorteners
- credentials or `@` in URLs
- IP literal and local-looking destinations
- lookalike or punycode-style signals where implemented
- suspicious file extensions
- URL metadata and file/download inference

Local Scan is heuristic. It can identify warning signals, but it cannot prove that a destination is safe.

## URL Metadata

For URL payloads, QR Guardian attempts HEAD metadata inspection when the server supports it.

Metadata can show:
- content type
- file name
- file type
- download disposition
- redirect/final destination

If HEAD fails, is blocked, or returns incomplete data, the app falls back to path-based inference where possible. Some servers do not expose useful metadata.

PDF and menu URLs can be shown as file metadata without automatically being marked dangerous. Executable and script-like downloads are treated as high risk.

## Remote Reputation

Remote Reputation is enabled only when provider keys are configured.

Supported optional providers:
- Google Safe Browsing
- URLhaus

Without keys, the app stays in local-only mode and the remote section reports `NotConfigured`.

Provider failures are handled safely: the local result remains visible and the remote section can show an unavailable state.

A clean provider response is not a guarantee of safety. Providers can miss threats, return stale data, or be unavailable.

## Opening Policy

The final result determines whether the app exposes an open action.

- Dangerous: no open action.
- Suspicious: open action can be available for URLs, but the UI presents warning context.
- Safe: open action can be available for URLs.
- Unknown: no guarantee is implied; availability depends on the final result model.
- Non-URL: no open-link action.

The result screen renders the policy from shared state instead of recomputing security rules in UI code.

## API Key Guidance

The repository includes empty example configuration files:
- `local.properties.example`
- `iosApp/Configuration/RemoteReputation.example.xcconfig`

Real keys must not be committed.

Embedding keys in a mobile app is acceptable for local development, demos, and portfolio use, but production use should prefer a backend or proxy that stores provider keys server-side.
