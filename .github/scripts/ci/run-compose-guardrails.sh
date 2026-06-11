#!/usr/bin/env bash
set -euo pipefail

workspace_dir="${COMPOSE_GUARDRAILS_WORKSPACE:-${GITHUB_WORKSPACE:-$(pwd -P)}}"
toolkit_dir="${MOBILE_AI_TOOLKIT_DIR:-$workspace_dir/mobile-ai-toolkit}"
target_path="${COMPOSE_GUARDRAILS_TARGET:-.}"
rule_set="${COMPOSE_GUARDRAILS_RULE_SET:-default}"
report_path="${COMPOSE_GUARDRAILS_REPORT_PATH:-artifacts/compose-guardrails-report.md}"
changed_files_only="${COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY:-false}"
fail_on_findings="${COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS:-false}"
provider="${MOBILE_AI_PROVIDER:-fake}"

script_path="$toolkit_dir/.github/scripts/run-compose-guardrails.sh"

if [[ ! -f "$script_path" ]]; then
  echo "compose-guardrails script not found at $script_path" >&2
  exit 1
fi

mkdir -p "$workspace_dir/$(dirname "$report_path")"

MOBILE_AI_TOOLKIT_DIR="$toolkit_dir" \
COMPOSE_GUARDRAILS_WORKSPACE="$workspace_dir" \
MOBILE_AI_PROVIDER="$provider" \
COMPOSE_GUARDRAILS_TARGET="$target_path" \
COMPOSE_GUARDRAILS_RULE_SET="$rule_set" \
COMPOSE_GUARDRAILS_REPORT_PATH="$report_path" \
COMPOSE_GUARDRAILS_CHANGED_FILES_ONLY="$changed_files_only" \
COMPOSE_GUARDRAILS_FAIL_ON_FINDINGS="$fail_on_findings" \
bash "$script_path"

report_abs="$workspace_dir/$(printf '%s' "$report_path" | sed 's#^\./##')"
if [[ ! -f "$report_abs" ]]; then
  echo "compose-guardrails report was not generated at $report_abs" >&2
  exit 1
fi
