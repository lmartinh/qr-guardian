# Remote Reputation

Remote Reputation is optional and applies only to URL payloads. QR Guardian works in local-only mode when no provider keys are configured.

Remote results are additional context. A clean response from a provider is not a guarantee that a URL is safe.

## Providers

The current codebase supports optional checks with:
- Google Safe Browsing
- URLhaus

Provider selection is handled in shared code:
- no keys configured -> `NoOpUrlReputationRepository`
- Google key only -> `GoogleSafeBrowsingUrlReputationRepository`
- URLhaus key only -> `UrlHausReputationRepository`
- both keys configured -> `CompositeUrlReputationRepository`

## Configuration Keys

Use these keys:
- `GOOGLE_SAFE_BROWSING_API_KEY`
- `URLHAUS_API_KEY`

Missing or blank values keep the app in local-only mode.

Do not commit real API keys.

## Android Configuration

Android reads optional values from `local.properties` through generated build config values.

The repository includes `local.properties.example` with empty placeholders:

```properties
GOOGLE_SAFE_BROWSING_API_KEY=
URLHAUS_API_KEY=
```

Developers can create a local `local.properties` file with real values for demos or development.

## iOS Configuration

iOS reads optional values from `Info.plist`, populated through `iosApp/Configuration/Config.xcconfig`.

The repository includes:
- `iosApp/Configuration/RemoteReputation.example.xcconfig`
- `iosApp/Configuration/Config.xcconfig`

`Config.xcconfig` includes the optional local override:

```xcconfig
#include? "RemoteReputation.xcconfig"
```

Developers can copy the example file to `RemoteReputation.xcconfig` locally and add real values there.

## Runtime Behavior

- Local Scan always runs.
- Remote Reputation only runs for URLs.
- Non-URL payloads show `NotApplicable`.
- Missing provider keys show `NotConfigured`.
- Provider failures show an unavailable remote state and keep the local result visible.
- Malicious provider results can upgrade the final result to dangerous.
- Clean provider results do not override local suspicious or dangerous findings.

## Testing Behavior

Remote provider tests use fake HTTP clients.

The shared test suite covers:
- missing API keys
- clean provider responses
- malicious provider responses
- unknown responses
- malformed provider payload handling
- HTTP/provider errors
- Google-only, URLhaus-only, and combined-provider composition
- local-safe + remote-malicious final result behavior

No unit test calls real Google Safe Browsing or URLhaus endpoints, and no unit test requires real API keys.

## Production Guidance

API keys embedded in mobile apps are not fully secret because app binaries can be inspected.

For production usage, a backend or proxy is preferred because it can:
- keep provider keys server-side
- add caching
- normalize provider responses
- swap or combine providers without forcing app updates

This repository intentionally avoids a backend so the app can run out of the box for development, demos, and portfolio review.
