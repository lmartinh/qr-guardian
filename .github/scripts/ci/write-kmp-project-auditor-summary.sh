#!/usr/bin/env bash
set -euo pipefail

summary_path="${GITHUB_STEP_SUMMARY:-}"
if [[ -z "$summary_path" ]]; then
  echo "GITHUB_STEP_SUMMARY is required" >&2
  exit 1
fi

report_path="${KMP_PROJECT_AUDITOR_REPORT_PATH:-artifacts/kmp-project-auditor-report.md}"
report_abs="${KMP_PROJECT_AUDITOR_WORKSPACE:-${GITHUB_WORKSPACE:-$(pwd -P)}}/$(printf '%s' "$report_path" | sed 's#^\./##')"
report_artifact="${KMP_PROJECT_AUDITOR_REPORT_ARTIFACT:-kmp-project-auditor-report}"
provider="${MOBILE_AI_PROVIDER:-fake}"
mode="Report only"

report_status="⚪ Not available"
if [[ -f "$report_abs" ]]; then
  report_status="✅ OK"
fi

{
  echo "## KMP Project Auditor Summary"
  echo
  echo "| Item | Result |"
  echo "|---|---|"
  echo "| Provider | \`$provider\` |"
  echo "| Mode | $mode |"
  echo "| Report generated | $report_status |"
  echo "| Report artifact | \`$report_artifact\` |"
  echo
  if [[ -f "$report_abs" ]]; then
    echo "Open the uploaded artifact for the full Markdown report."
  else
    echo "The report file was not found at the expected path: \`$report_path\`."
  fi
} >> "$summary_path"
