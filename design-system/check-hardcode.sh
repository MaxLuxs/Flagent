#!/usr/bin/env bash
# Scans frontend (and optionally SDK) Kotlin/TS/Swift for hardcoded hex colors and rgba().
# Exits with 1 if any match is found (excluding allowed paths).
# Usage: from repo root: ./design-system/check-hardcode.sh

set -e
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# Patterns: hex #RRGGBB and rgba( or rgb( (ERE)
HEX_PATTERN='#[0-9A-Fa-f]{6}'
RGBA_PATTERN='rgba?[[:space:]]*\('

# Directories to scan (Kotlin/TS/Swift UI code)
SCAN_DIRS=(
  "frontend/src/jsMain/kotlin"
  "sdk/kotlin-debug-ui/src/main/kotlin"
  "sdk/javascript-debug-ui/src"
  "sdk/swift-debug-ui/Sources"
)

# Skip paths: tokens source, generated token files, tests
# Optional: design-system/hardcode-allowlist.txt lists path suffixes to skip (one per line)
skip_path() {
  local f="$1"
  case "$f" in
    *tokens/tokens.json|*design-system/build/*|*jsTest/*|*FlagentTokens.ts|*FlagentTokens.swift*) return 0;;
  esac
  if [ -f "$ROOT/design-system/hardcode-allowlist.txt" ]; then
    while IFS= read -r suffix || [ -n "$suffix" ]; do
      [[ "$suffix" =~ ^#.*$ || -z "$suffix" ]] && continue
      if [[ "$f" == *"$suffix" ]]; then return 0; fi
    done < "$ROOT/design-system/hardcode-allowlist.txt"
  fi
  return 1
}

FOUND=0
for dir in "${SCAN_DIRS[@]}"; do
  if [ ! -d "$dir" ]; then
    continue
  fi
  while IFS= read -r -d '' f; do
    skip_path "$f" && continue
    if grep -n -E "($HEX_PATTERN|$RGBA_PATTERN)" "$f" > /dev/null 2>&1; then
      echo "Hardcode in: $f"
      grep -n -E "($HEX_PATTERN|$RGBA_PATTERN)" "$f" || true
      FOUND=1
    fi
  done < <(find "$dir" -type f \( -name "*.kt" -o -name "*.ts" -o -name "*.tsx" -o -name "*.swift" \) -not -path "*/build/*" -not -path "*/.gradle/*" -print0 2>/dev/null)
done

if [ $FOUND -eq 1 ]; then
  echo ""
  echo "Use design tokens (FlagentTheme / FlagentTokens / FlagentDesignTokens) instead of hardcoded colors."
  echo "See docs/design-system/README.md"
  exit 1
fi
echo "No hardcoded colors found in scanned paths."
exit 0
