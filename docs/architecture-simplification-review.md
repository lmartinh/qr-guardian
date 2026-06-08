# Architecture Simplification Review

## 1. Current High-Level Architecture

### Main modules and packages
- `androidApp`: Android entry point, permission handling, app wiring, and platform build config.
- `shared`: Kotlin Multiplatform shared code for domain, data, presentation, and common app composition.
- `shared/src/commonMain`: the main business and UI implementation used by both platforms.
- `shared/src/androidMain` and `shared/src/iosMain`: platform-specific scanner, HTTP client, and DI wiring.

### Main flow from scan to result
1. Platform scanner emits raw text.
2. `App` coordinates screen state and sends the scan text into `AnalyzeQrSafetyUseCase`.
3. `AnalyzeQrSafetyUseCase` normalizes and classifies the payload, rejects dangerous schemes, runs local analysis, and for URLs performs metadata and reputation checks.
4. `QrSafetyAnalysisAssembler` turns raw analysis outputs into the final `QrAnalysisResult`.
5. `ResultViewModel` exposes a `ResultUiState` to Compose.
6. Result UI reads the analysis model directly and renders the final screen.

### Responsibility split
- Domain: normalization, classification, local analysis, URL metadata inference, reputation mapping, and final result assembly.
- Data: Ktor-backed metadata lookup and remote reputation repository implementations.
- Presentation: Compose screens, UI state objects, and simple state holders for camera/result flow.
- Platform: camera scanner integration, permission handling, bundle/local config loading, and HTTP engine wiring.
- DI: Koin wires the platform dependencies into the shared pipeline.

## 2. Simplification Opportunities

### Repeated logic
- Open-button gating is checked in both `ResultUiState.showOpenButton` and `ResultContent`.
- Dead helpers exist in URL metadata inference and result assembly: `shouldShowDownload` and `shouldShowFileDetails` are not used.

### Thin abstractions
- `QrGuardianSecurityPipelineFactory` is a thin wrapper around constructor wiring. It is readable, but it mostly mirrors what Koin already does.
- `ResultViewModel` and `CameraViewModel` are lightweight state holders rather than lifecycle-aware Android ViewModels. That is acceptable, but the naming can suggest more framework behavior than they provide.

### Unnecessary interfaces
- Some domain interfaces have single concrete implementations, but most of them still represent real seams:
  - `QrTextNormalizer`
  - `QrContentClassifier`
  - `LocalScanAnalyzer`
- These are still justified because they isolate business behavior and keep tests simple. I would not remove them yet.

### Mappers / assemblers
- `QrSafetyAnalysisAssembler` is doing meaningful transformation and is not just copying fields, but it is a large concentration of presentation-ready text assembly. It should stay for now, with only dead-code cleanup.

### Presentation duplication
- `ResultUiState` mirrors `QrAnalysisResult` closely, but it adds real UI-gating behavior. Keep it, but trim duplicated predicates.

### Koin wiring
- Koin currently delegates through a factory object and a separate configuration module. This is acceptable, but there is room to flatten one layer if future refactors need less indirection.

### Dead or low-value code
- `shouldShowDownload` in `UrlResourceInference` is unused.
- `shouldShowFileDetails` in `QrSafetyAnalysisAssembler` is unused.

### Tests too coupled to implementation details
- Koin tests compare the resolved use case against the pipeline factory. This is fine today because the factory is the composition boundary, but if the wiring is flattened later, those tests should be rewritten to assert behavior rather than factory equivalence.

## 3. Risk Assessment

### Low-risk refactors
- Remove unused helpers in metadata and assembler code.
- Trim duplicated open-button gating logic in presentation state.
- Keep existing behavior and test coverage intact.

### Medium-risk refactors
- Flatten Koin wiring further and remove the pipeline factory if the app boundary is updated at the same time.
- Collapse some thin view-state helpers if the UI flow is revisited.

### Refactors to avoid now
- Reworking the scan/result state model.
- Removing the domain interfaces that currently define useful seams.
- Splitting modules or introducing new abstractions just to reduce file count.
- Changing UI styling or screen structure.

## 4. Recommended Execution Plan

1. Remove unused helpers in shared domain code.
2. Simplify presentation gating so open-button visibility is derived once.
3. Run the shared test suite that covers the changed code.
4. If future cleanup is desired, revisit the Koin composition layer and decide whether the factory indirection still earns its place.

## 5. Koin Composition Follow-up

- The factory was kept.
- It still earns its place because it centralizes non-trivial pipeline construction: shared normalizers, local analyzers, URL metadata lookup, and provider-selection logic for optional remote reputation checks.
- What changed: the Koin module was flattened so `initKoin` now declares the `RemoteReputationConfig` binding and the `AnalyzeQrSafetyUseCase` binding inline instead of routing through separate module helpers.
- What was intentionally left unchanged: `QrGuardianSecurityPipelineFactory`, provider selection, platform-specific HTTP client wiring, and the behavior of the shared analysis pipeline.
- Remaining future simplification opportunities: if the app ever grows more composition complexity, the next question is whether `QrGuardianSecurityPipelineFactory` should remain the single pipeline assembly point or whether Koin should own the full object graph directly. For now, the factory is still the clearer boundary.

## 6. Result Assembly Follow-up

1. `QrSafetyAnalysisAssembler` currently owns the final shaping of scan sections. It combines local scan output, URL metadata, and remote reputation into `ScanSectionResult` values, and it also formats the section titles, descriptions, metadata rows, and reason strings used by the result screen.
2. It contains both business decisions and UI text decisions. The business part is the level/status combination and metadata risk interpretation. The UI part is the human-readable wording for section titles, descriptions, and metadata labels.
3. `ResultUiState` does not duplicate much business logic. It mostly projects `QrAnalysisResult` into UI-friendly accessors, and `showOpenButton` is the main UI decision it owns. `canOpen`, `openableUrl`, `overallLevel`, `localScan`, and `remoteReputation` are direct projections.
4. `ResultContent` is already mostly render-only. It still maps `overallLevel` to presentation status text and passes the derived state into child composables, but it no longer makes security decisions or re-evaluates whether the open action should be shown.
5. File/download URL information is assembled in the right layer. `AnalyzeQrSafetyUseCase` gathers the facts, `KtorUrlMetadataRepository` infers metadata details, and `QrSafetyAnalysisAssembler` turns those facts into the final local result section. The composable only displays the result.
6. Low-risk simplifications found:
   - Keep `showOpenButton` as the single gate for the open action.
   - Avoid reintroducing any open-button predicate into `ResultContent`.
   - Keep the section-building logic in the assembler rather than spreading it across UI helpers.
   - Continue to treat metadata rows and reason strings as presentation output from assembled domain facts.
7. Things that should intentionally stay unchanged:
   - The final `QrAnalysisResult` shape.
   - The local scan and remote reputation result model split.
   - The URL/file/download interpretation rules.
   - The visible result UI styling and copy.
   - The current test coverage around open-button behavior and file/download handling.
