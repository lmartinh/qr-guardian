# Security Model

For implementation details of the current local-first checks, see [Local Security Checks](security/local-security-checks.md).
For provider-specific remote configuration, see [Remote Reputation](security/remote-reputation.md).

## Local Verification Pipeline
QR Guardian performs a local first-pass analysis on the scanned text before any user action.

The pipeline is:
1. Normalize the raw text.
2. Classify the content type.
3. If the payload is a URL, run the local scan and remote reputation checks in parallel.
4. For URL payloads, perform a HEAD metadata check to inspect the destination before opening it.
5. Combine the detected signals into a single overall result.
6. Show the result to the user before any opening action.

## Normalization Checks
Before classification, the scanner payload is normalized locally:
- `trim()` whitespace.
- Remove null characters.
- Detect empty text after normalization.
- Flag unusually long text.
- Flag suspicious control characters.

If normalization reveals problems, the result stays explicit and traceable in the final reasons list.

## Content Classification Checks
The classifier separates the payload into a content type so each format can be handled with the right rules:
- URL
- Email / `mailto`
- Phone / `tel`
- SMS / `sms`
- WiFi QR
- vCard
- Geo
- Crypto
- Plain text
- Unknown

## Local Result Rules
The final local result follows these rules:
- `Dangerous` overrides everything else.
- If there is no `Dangerous` signal but at least one `Suspicious` signal, the result is `Suspicious`.
- If the content is a clean HTTPS URL with no signals, it can be marked `Safe`.
- If the content cannot be evaluated reliably, it stays `Unknown`.

When the URL HEAD metadata check is available, the local scan section also shows:
- Final URL after redirects
- Content type and content disposition
- Content length, file name and file extension
- File type classification
- Whether the destination looks like a downloadable file

`canOpen` is derived from the level:
- `Dangerous` -> `false`
- `Suspicious` -> `true`
- `Safe` -> `true`
- `Unknown` -> `true`

## URL Checks
URL checks run as independent rules so they can evolve without coupling:

| Check | Result | Notes |
| --- | --- | --- |
| Missing HTTPS | `Suspicious` | `http://` is not blocked outright, but it is flagged for review. |
| `@` in URL | `Suspicious` | Can hide the real destination. |
| IPv4 host | `Suspicious` | IP-based hosts deserve manual review. |
| Link shortener | `Suspicious` | Known shorteners are harder to inspect. |
| Dangerous file extension | `Dangerous` | Examples: `.apk`, `.exe`, `.js`, `.dmg`. |
| Sensitive words | `Suspicious` | Examples: `login`, `verify`, `password`, `update`. |
| Long URL | `Suspicious` | Threshold: more than 300 characters. |
| Too many query params | `Suspicious` | Threshold: more than 8 query parameters. |
| Too many subdomains | `Suspicious` | Threshold: more than 4 dot-separated parts in the host. |
| Brand impersonation | `Suspicious` | Known brand names combined with sensitive words. |

## Sensitive Action Checks
Some payloads are not URL threats, but they still trigger sensitive actions:

### WiFi QR
- Missing `S:` is flagged as `Suspicious`.
- `T:nopass` is flagged as `Suspicious`.
- The result always explains that the payload configures a network.
- The app must never connect automatically.

### `tel:`
- Marked as `Suspicious`.
- The app must never dial automatically.

### `sms:`
- Marked as `Suspicious`.
- If a body is preset, it adds a reason.
- If the body contains a URL, it adds an additional reason.
- The app must never send automatically.

### `mailto:`
- Marked as `Suspicious`.
- If subject/body is prefilled, it adds a reason.
- The app must never send automatically.

### Crypto URIs
- Marked as `Suspicious`.
- The result should warn that a payment or asset transfer may happen.

## Unsupported or Uncertain Payloads
- Plain text stays `Unknown`, not `Safe`.
- Unknown content stays `Unknown` and should still be presented carefully.
- The UI should always explain uncertainty instead of hiding it.

## Why Scanned URLs Can Be Dangerous
QR and barcode payloads can hide malicious links that are not obvious to users before opening. Attack vectors include phishing, malware delivery pages, credential theft and social engineering.

## Core Product Rule
The app must never open scanned links automatically.

Users always see the analysis result first and explicitly decide what to do.
The result screen only shows an open action for URL payloads that are not classified as dangerous, and bare domains are opened as `https://...` when possible.

## Dangerous Schemes
These schemes should be rejected or strongly blocked when URL handling is implemented:
- `javascript:`
- `file:`
- `data:`
- `intent:`

## URL Processing Rules
- Normalize URL before analysis.
- Validate structure and supported schemes.
- Use HEAD for URL metadata when the payload is a URL.
- Keep unknown/failed analysis outcomes clearly marked as uncertain.

## Result Texts
Security levels use English copy so the app stays GitHub-friendly and consistent across platforms:
- `Safe`: "Looks safe"
- `Suspicious`: "Be careful"
- `Dangerous`: "Potentially dangerous"
- `Unknown`: "Unknown content"

Descriptions:
- `Safe`: "No suspicious signals were detected by local checks."
- `Suspicious`: "This QR code contains signals that should be reviewed before opening."
- `Dangerous`: "This QR code contains high-risk signals. Opening it is not recommended."
- `Unknown`: "This QR code could not be fully evaluated locally."

## Result Sections
The current analysis result is split into two sections so the UI can present them independently:

- `Local Scan`: local normalization, content classification, URL rules and HEAD metadata.
- `Remote Reputation`: optional provider lookup, shown only for URL payloads.

Android and iOS both default to local-only mode when no keys are configured. Android reads optional keys from `local.properties`; iOS reads optional keys from `iosApp/Configuration/RemoteReputation.xcconfig` through `Info.plist`.

The security pipeline composition itself is kept in `QrGuardianSecurityPipelineFactory`. Koin is only used to wire the app together at the platform boundary.

Remote reputation section states:
- `Completed`: the provider returned a reputation result.
- `NotConfigured`: no provider is configured for this installation.
- `NotApplicable`: the scanned payload is not a URL.
- `Unavailable`: the provider could not be reached.

## Key Management Rule
Mobile apps must not expose URL reputation provider API keys.

## Recommended Production-Like Flow
Mobile app  
↓  
Backend endpoint  
↓  
URL reputation provider  
↓  
Backend normalized response  
↓  
Mobile app result screen

## Provider Status
Google Safe Browsing and URLhaus are already implemented in the codebase as optional remote providers.

Other provider ideas remain future work and are intentionally out of scope for this documentation task:
- PhishTank
- VirusTotal
- urlscan.io
