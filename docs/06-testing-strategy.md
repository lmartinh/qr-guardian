# Testing Strategy

## Testing Approach
Prioritize pragmatic tests in shared Kotlin logic (`commonMain`-oriented domain and pure functions). Keep tests readable and focused on behavior.

Testing style:
- Arrange
- Act
- Assert

## Domain Tests
Prioritize coverage for:
- Normalization: whitespace trimming, null removal, empty input, long input.
- Classification: URL, email, phone, SMS, WiFi, vCard, geo, crypto, plain text.
- URL checks: HTTPS, `@` in URL, IPv4 host, link shorteners, dangerous file extensions, sensitive words, long URLs, too many query params, too many subdomains, brand impersonation.
- Sensitive actions: `tel:`, `sms:`, `mailto:`, WiFi QR, crypto URIs.
- Final result mapping: dangerous overrides, suspicious warning behavior, safe/unknown output, `canOpen` derivation.
- Unknown content fallback.
- Dangerous scheme rejection when URL handling is extended to support it.

## Presentation Tests
Optional and incremental:
- State holder behavior
- UI state transitions
- Result mapping for view state

## Platform Tests
Camera/scanner behavior will require platform-specific testing later:
- Android camera/scanner integration tests
- iOS camera/scanner integration tests
- Permission behavior tests on both platforms

## Current Shared Test Coverage
The current implementation already includes focused tests for:
- `DefaultQrTextNormalizer`
- `DefaultQrContentClassifier`
- `UrlLocalSecurityAnalyzer`
- `WifiLocalSecurityAnalyzer`
- `SensitiveActionAnalyzer`
- `PlainTextSecurityAnalyzer`
- `DefaultQrSecurityAnalyzer`

These tests document the expected behavior of the local verification pipeline and act as regression coverage for future rules.

## Guidance
- Prefer deterministic fake data sources in tests.
- Avoid testing implementation details.
- Avoid heavy test infrastructure until needed.
