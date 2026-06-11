#!/usr/bin/env bash
set -euo pipefail

summary_path="${GITHUB_STEP_SUMMARY:-}"
if [ -z "$summary_path" ]; then
  echo "GITHUB_STEP_SUMMARY is required" >&2
  exit 1
fi

assemble_exit_code="${ASSEMBLE_EXIT_CODE:-0}"

human_size() {
  awk -v bytes="$1" 'BEGIN {
    split("B KB MB GB TB", units, " ");
    value = bytes + 0;
    unit_index = 1;
    while (value >= 1024 && unit_index < 5) {
      value /= 1024;
      unit_index++;
    }
    printf "%.1f %s", value, units[unit_index];
  }'
}

artifact_candidates=()
artifact_roots=(
  "androidApp/build/outputs/apk/release"
  "androidApp/build/outputs/bundle/release"
)

if [ "$assemble_exit_code" -eq 0 ]; then
  for root_dir in "${artifact_roots[@]}"; do
    if [ -d "$root_dir" ]; then
      while IFS= read -r -d '' file; do
        artifact_candidates+=("$file")
      done < <(find "$root_dir" -maxdepth 1 -type f \( -name '*.apk' -o -name '*.aab' \) -print0)
    fi
  done
fi

{
  echo "## Assemble Release Summary"
  echo
  echo "| Item | Result |"
  echo "|---|---|"

  if [ "$assemble_exit_code" -eq 0 ]; then
    echo "| assembleRelease | ✅ OK |"
    if [ "${#artifact_candidates[@]}" -gt 0 ]; then
      echo "| Release artifacts found | ✅ OK |"
      echo "| Artifact count | ${#artifact_candidates[@]} |"
      echo
      echo "### Release Artifacts"
      echo
      echo "| File | Size |"
      echo "|---|---:|"
      for artifact_path in "${artifact_candidates[@]}"; do
        artifact_size_bytes="$(stat -c '%s' "$artifact_path" 2>/dev/null || stat -f '%z' "$artifact_path")"
        artifact_size="$(human_size "$artifact_size_bytes")"
        echo "| \`$artifact_path\` | $artifact_size |"
      done
    else
      echo "| Release artifacts found | ⚪ Not available |"
      echo "| Artifact count | 0 |"
    fi
  else
    echo "| assembleRelease | ❌ KO |"
    echo "| Release artifacts found | ⚪ Not checked |"
  fi
} >> "$summary_path"
