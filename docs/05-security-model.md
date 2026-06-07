# Security Model

For the implementation details of the local-first checks, see [Local Security Checks](security/local-security-checks.md).
For provider-specific remote configuration, see [Remote Reputation](security/remote-reputation.md).

## Local First

QR Guardian analyzes scanned content locally before any user action.

The local pipeline:
1. Normalizes the raw text.
2. Classifies the content type.
3. Runs local URL and sensitive-content checks.
4. Performs HEAD metadata inspection for URLs when the server supports it.
5. Combines the signals into one result.
6. Shows the result before anything is opened.

## Local Scan

Local Scan is always enabled and does not require API keys.

It covers:
- QR and content normalization.
- Content classification.
- URL safety rules.
- HEAD metadata inspection for URLs.
- Download-like file detection, including PDF and menu links.
- High-risk handling for executable and script downloads.

Local Scan does not call external reputation services.

## Remote Reputation

Remote Reputation is optional and applies only to URLs.

When configured, it can use:
- Google Safe Browsing
- URLhaus

When no keys are configured, the remote section is shown as `Not configured`.

Remote reputation results never override the core rule that the user must review the scan result before opening anything.

## Dangerous Schemes

These schemes should be rejected or strongly blocked:
- `javascript:`
- `file:`
- `data:`
- `intent:`

## Result Behavior

- Safe: the scan looks clean locally.
- Suspicious: the scan contains warning signals.
- Dangerous: the scan contains high-risk signals.
- Unknown: the scan could not be fully evaluated.

The open action is only available for URL results that are not dangerous.

## Architecture Boundary

The security pipeline is assembled by `QrGuardianSecurityPipelineFactory`.

Koin is used only at the app wiring boundary, so the domain layer remains framework-independent and easy to test.

## Security Principle

The app never opens scanned content automatically. Users always see the analysis first and explicitly decide what to do next.
