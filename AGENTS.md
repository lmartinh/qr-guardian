# AGENTS.md

Guidance for Codex/agent work in QR Guardian.

## Project

QR Guardian is a Kotlin Multiplatform + Compose Multiplatform mobile app for Android and iOS.

Main package:
- `com.lmartin.qrguardian`

Modules:
- `androidApp`
- `shared`
- `iosApp`

The app scans QR codes and barcodes, classifies content, runs local security checks, optionally checks URL reputation providers, and shows a result before the user opens anything.

## Working Principles

- Act as a senior Kotlin Multiplatform and Compose Multiplatform engineer with pragmatic Clean Architecture, SOLID, testing, and simplification judgment.
- Make the smallest useful change for the current task.
- Do not invent product requirements.
- Do not introduce new dependencies unless they are necessary for the task and clearly justified.
- Do not reformat the whole project unless explicitly requested.
- Keep platform-specific code isolated from shared domain models.
- Prefer simple Kotlin and readable Compose code.
- Prefer deleting code over adding code when it improves clarity.
- Preserve the README visual identity unless the task explicitly changes it.

## Engineering Standards

- Avoid clean architecture theater: every layer, abstraction, interface, mapper, use case, manager, provider, factory, or wrapper must earn its existence.
- Prefer simple functions and small cohesive classes over generic architectures.
- Do not move code only for aesthetic reasons.
- Prefer direct constructor injection through Koin wiring over service locator patterns.
- Keep Compose UI as dumb as reasonably possible: render state, emit events, and avoid business/security decisions in composables.
- Use `expect`/`actual` or platform modules only when platform-specific behavior is truly needed.
- Add tests in proportion to risk, especially for shared domain behavior, security rules, and result gating.

## Architecture Boundaries

- Shared business logic belongs in `shared/src/commonMain`.
- Android-specific code belongs in `shared/src/androidMain` or `androidApp`.
- iOS-specific code belongs in `shared/src/iosMain` or `iosApp`.
- Domain models should remain platform-independent.
- Koin is used at the app wiring boundary.
- `QrGuardianSecurityPipelineFactory` is the explicit security pipeline composition point.
- UI should render `QrAnalysisResult` / `ResultUiState` instead of recalculating security rules.

## Security Rules

- Never commit API keys, tokens, secrets, signing files, or private environment files.
- Keep `local.properties` and local/private xcconfig files out of source control.
- Local-only mode must work without provider keys.
- Google Safe Browsing and URLhaus are optional remote providers.
- Mobile-embedded keys are not fully secret; production usage should prefer a backend/proxy.
- Do not automatically open scanned URLs.
- Always show the analyzed result before allowing an open action.
- Do not make security guarantees such as "detects all malware" or "guarantees safe links".

## Documentation Rules

- Keep public docs concise and current.
- Prefer updating existing docs over adding new files.
- Avoid keeping temporary audit logs or agent-process documents in `docs/`.
- Preserve README structure, hero image, screenshots, badges, and portfolio tone.
- Keep `README.md` and `README.es.md` aligned.
- Update documentation links when files are deleted or renamed.

## CI And AI Tools

- Keep normal CI deterministic and independent from real AI providers or provider secrets.
- Treat `.github/workflows/ai-tools.yml` as manual, report-first review support.
- Use `MOBILE_AI_PROVIDER=fake` as the safe default for AI tooling.
- Do not pass API keys as workflow inputs and do not commit provider secrets.
- If workflow behavior changes, update `README.md`, `README.es.md`, and `docs/06-testing-strategy.md` together.

## Testing

Most regression coverage lives in `shared/src/commonTest`.

Prioritize tests for:
- normalization and classification
- local URL heuristics
- dangerous scheme blocking
- URL metadata and file/download inference
- remote reputation mapping with fake HTTP clients
- final result assembly
- open-button/result gating
- pipeline/Koin composition

Do not use real network calls or real provider API keys in unit tests.

## Validation

After documentation-only changes, run:

```bash
./gradlew :shared:allTests
./gradlew check
git diff --check
```

For narrower code changes, run the most relevant Gradle task plus `git diff --check`.

Always report:
- what changed
- validation commands executed
- whether validation passed
- any known limitations
