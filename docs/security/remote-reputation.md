# Remote Reputation

QR Guardian keeps local security checks enabled by default.

## Dependency Injection

- Koin is used only to wire the app at the platform boundary.
- `QrGuardianSecurityPipelineFactory` is the single source of truth for security pipeline composition.
- Koin delegates to that factory instead of rebuilding the pipeline in the module graph.
- Domain classes stay framework-independent and use constructor injection.

## Providers

The codebase currently supports these optional remote providers for URL payloads:
- Google Safe Browsing
- URLhaus

The shared factory behavior is unchanged:
- no keys configured -> `NoOpUrlReputationRepository`
- Google key only -> `GoogleSafeBrowsingUrlReputationRepository`
- URLhaus key only -> `UrlHausReputationRepository`
- both keys configured -> `CompositeUrlReputationRepository`

## Keys

Use these exact configuration keys everywhere:
- `GOOGLE_SAFE_BROWSING_API_KEY`
- `URLHAUS_API_KEY`

The keys are optional. Missing or blank values keep the app in local-only mode.

## Default Behavior

- Local Scan always runs.
- Remote Reputation only applies to URLs.
- Without keys, the remote section is shown as `NotConfigured`.
- Non-URL payloads remain `NotApplicable`.

## Android Configuration

Android reads optional keys from `local.properties` in the project root and passes them into `RemoteReputationConfig`.

The repository includes `local.properties.example` with empty placeholders only.

Example:

```properties
GOOGLE_SAFE_BROWSING_API_KEY=your_google_key
URLHAUS_API_KEY=your_urlhaus_auth_key
```

Leave either value empty to keep local-only mode.
`local.properties` stays ignored by Git.

## iOS Configuration

iOS reads the keys from `Info.plist`, which is populated from `iosApp/Configuration/Config.xcconfig`.

That xcconfig file already includes the optional override:

```xcconfig
#include? "RemoteReputation.xcconfig"
```

To enable remote providers, copy:

`iosApp/Configuration/RemoteReputation.example.xcconfig`

to:

`iosApp/Configuration/RemoteReputation.xcconfig`

Example local override file:

```xcconfig
GOOGLE_SAFE_BROWSING_API_KEY = your_google_key
URLHAUS_API_KEY = your_urlhaus_auth_key
```

Leave the values empty to keep local-only mode.

## Production Guidance

Mobile binaries can be inspected, so API keys should not be treated as fully secret when embedded in an app.

For production use, a backend or proxy is still the safer option because it centralizes key handling, supports caching, and avoids exposing provider keys in the client.

This project intentionally avoids a backend so it can run out of the box without extra infrastructure.

## Provider Notes

If Google Safe Browsing is configured:
- clean responses keep the local result unchanged
- malicious responses upgrade the final result to `Dangerous`
- errors keep the local result and do not crash the app

If URLhaus is configured:
- malware matches upgrade the final result to `Dangerous`
- clean responses keep the local result unchanged
- errors keep the local result and do not crash the app

If both providers are configured:
- the composite repository merges results conservatively

If the scanned content is not a URL:
- the remote section is `NotApplicable`
- no provider request is made
