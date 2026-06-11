#!/usr/bin/env bash
set -euo pipefail

summary_path="${GITHUB_STEP_SUMMARY:-}"
if [ -z "$summary_path" ]; then
  echo "GITHUB_STEP_SUMMARY is required" >&2
  exit 1
fi

android_lint_result="${ANDROID_LINT_RESULT:-skipped}"
spotless_result="${SPOTLESS_RESULT:-skipped}"

format_result() {
  case "$1" in
    ok)
      printf '✅ OK'
      ;;
    ko)
      printf '❌ KO'
      ;;
    skipped)
      printf '⚪ Skipped'
      ;;
    *)
      printf '⚪ Skipped'
      ;;
  esac
}

{
  echo "## Quality Summary"
  echo
  echo "| Check | Result |"
  echo "|---|---|"
  printf '| Android Lint | %s |\n' "$(format_result "$android_lint_result")"
  printf '| Spotless | %s |\n' "$(format_result "$spotless_result")"
} >> "$summary_path"
