#!/usr/bin/env bash
set -euo pipefail

workspace_dir="${KMP_PROJECT_AUDITOR_WORKSPACE:-${GITHUB_WORKSPACE:-$(pwd -P)}}"
toolkit_dir="${MOBILE_AI_TOOLKIT_DIR:-$workspace_dir/mobile-ai-toolkit}"
target_path="${KMP_PROJECT_AUDITOR_TARGET:-.}"
report_path="${KMP_PROJECT_AUDITOR_REPORT_PATH:-artifacts/kmp-project-auditor-report.md}"
fail_on_findings="${KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS:-false}"
write_step_summary="${KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY:-false}"
provider="${MOBILE_AI_PROVIDER:-fake}"

script_path="$toolkit_dir/.github/scripts/run-kmp-project-auditor.sh"

if [[ ! -d "$toolkit_dir" ]]; then
  echo "mobile-ai-toolkit checkout not found at $toolkit_dir" >&2
  exit 1
fi

if [[ ! -f "$script_path" ]]; then
  echo "kmp-project-auditor script not found at $script_path" >&2
  exit 1
fi

mkdir -p "$workspace_dir/$(dirname "$report_path")"

MOBILE_AI_TOOLKIT_DIR="$toolkit_dir" \
KMP_PROJECT_AUDITOR_WORKSPACE="$workspace_dir" \
MOBILE_AI_PROVIDER="$provider" \
KMP_PROJECT_AUDITOR_TARGET="$target_path" \
KMP_PROJECT_AUDITOR_REPORT_PATH="$report_path" \
KMP_PROJECT_AUDITOR_FAIL_ON_FINDINGS="$fail_on_findings" \
KMP_PROJECT_AUDITOR_WRITE_STEP_SUMMARY="$write_step_summary" \
bash "$script_path"

report_abs="$workspace_dir/$(printf '%s' "$report_path" | sed 's#^\./##')"
if [[ ! -f "$report_abs" ]]; then
  echo "kmp-project-auditor report was not generated at $report_abs" >&2
  exit 1
fi
