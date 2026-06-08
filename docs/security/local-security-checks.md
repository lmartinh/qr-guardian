# Local Security Checks

Local Scan is always enabled. It runs without API keys, without a backend, and before the user can open scanned content.

These checks are heuristics. They help identify suspicious content but do not guarantee safety.

## Content Classification

The shared classifier identifies common payload families:
- URL
- plain text
- email
- phone
- SMS
- WiFi
- vCard/contact
- geo/location
- crypto/payment URI
- unknown content

Barcode scanner payloads are analyzed by their text content. The current shared content model does not expose a dedicated product-code type.

Non-URL payloads do not trigger URL metadata or Remote Reputation checks.

## Dangerous Schemes

Dangerous schemes are rejected before URL metadata or reputation work starts:
- `javascript:`
- `file:`
- `data:`
- `intent:`

Mixed-case dangerous schemes are covered by regression tests.

## URL Shape Heuristics

Local URL analysis checks warning signals such as:
- HTTP instead of HTTPS
- known shorteners
- credentials or `@` in the URL
- IPv4 hosts
- local-looking destinations covered by current tests, such as `localhost`
- many query parameters
- many subdomains
- suspicious wording
- brand impersonation patterns
- punycode-style/lookalike signals covered by current tests

These rules are intentionally conservative warning signals. They should not be presented as proof that a URL is malicious.

## File And Download Signals

Local checks flag or describe file-like destinations using:
- suspicious file extensions
- URL path inference
- HEAD metadata
- `Content-Type`
- `Content-Disposition`
- final URL after redirect
- inferred file name and file type

PDF and menu URLs can be shown as file metadata without being automatically marked dangerous. Executable, installer, and script-like downloads are treated as high risk.

## HEAD Metadata Behavior

For URL payloads, the app attempts HEAD metadata inspection when possible.

If metadata is available, the result can show:
- host
- connection type
- path
- content type
- file name
- file type
- download disposition
- resolved destination

If HEAD is unavailable, rejected, or fails during transport, the app falls back to path-based inference and keeps the scan result usable.

## Result Impact

Local Scan can produce:
- Safe
- Suspicious
- Dangerous
- Unknown

Dangerous local findings block opening. Suspicious URL findings can still be openable when the final result policy allows it, but the UI shows warning context first.
