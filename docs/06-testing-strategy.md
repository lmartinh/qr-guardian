# Testing Strategy

## Testing Approach
Prioritize pragmatic tests in shared Kotlin logic (`commonMain`-oriented domain and pure functions). Keep tests readable and focused on behavior.

Testing style:
- Arrange
- Act
- Assert

## Domain Tests
Prioritize coverage for:
- URL detection
- Email detection
- Phone detection
- WiFi QR detection
- Barcode detection (when logic exists)
- Unknown content fallback
- Dangerous scheme rejection
- URL normalization
- URL safety use case behavior

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

## Guidance
- Prefer deterministic fake data sources in tests.
- Avoid testing implementation details.
- Avoid heavy test infrastructure until needed.
