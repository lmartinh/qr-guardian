# Remote Reputation

QR Guardian keeps local security checks enabled by default.

## Current State
- Remote reputation checks are optional.
- Google Safe Browsing and URLhaus are the remote providers prepared in the codebase.
- No API keys are included in the repository.
- Without an API key, the app falls back to local-only mode through `NoOpUrlReputationRepository`.

## Configuration
In a future setup, each developer can provide their own API key for Google Safe Browsing.
URLhaus uses its own optional API key as well.

The repository includes `local.properties.example` as a documentation aid:
- `GOOGLE_SAFE_BROWSING_API_KEY=`
- `URLHAUS_API_KEY=`

The example file is intentionally empty and does not contain secrets.

## Production Guidance
Mobile binaries can be inspected, so API keys should not be treated as secret when embedded directly in an app.

For production deployments, a backend or proxy is still the safer option because it centralizes key handling and makes it easier to swap providers later.

This project intentionally avoids a backend so it can be cloned and run without external infrastructure maintained by the author.

## Provider Behavior
If Google Safe Browsing is configured:
- Clean responses keep the local result unchanged.
- Malicious responses upgrade the final result to `Dangerous`.
- Errors do not break the app and keep the local result.

If URLhaus is configured:
- Malware matches upgrade the final result to `Dangerous`.
- Clean responses keep the local result unchanged.
- Errors do not break the app and keep the local result.

If both providers are configured:
- Their results are combined conservatively by the composite repository.

If Google Safe Browsing is not configured:
- The repository returns `NotConfigured`.
- The app stays in local-only mode.
