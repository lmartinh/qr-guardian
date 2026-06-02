# Security Model

## Local Verification Pipeline
QR Guardian performs a local first-pass analysis on the scanned text before any user action.

The pipeline is:
1. Normalize the raw text.
2. Classify the content type.
3. Run type-specific local checks.
4. Combine the detected signals into a single security result.
5. Show the result to the user before any opening action.

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

## Dangerous Schemes
These schemes should be rejected or strongly blocked when URL handling is implemented:
- `javascript:`
- `file:`
- `data:`
- `intent:`

## URL Processing Rules
- Normalize URL before analysis.
- Validate structure and supported schemes.
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

## Potential Providers (Future)
- Google Safe Browsing
- PhishTank
- VirusTotal
- urlscan.io

Provider integration is future work and is intentionally out of scope for this documentation task.
