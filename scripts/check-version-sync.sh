#!/usr/bin/env bash
# Check that VERSION is in sync with all files that sync-version.sh updates.
# Run from repo root. Exit 0 if sync, 1 if out of sync.
# Usage: ./scripts/check-version-sync.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ ! -f VERSION ]; then
  exit 0
fi
VERSION=$(cat VERSION | tr -d '\n\r' | tr -d ' ')
test -n "$VERSION" || exit 0

ERRORS=()

# package.json
for f in sdk/javascript/package.json sdk/javascript-enhanced/package.json sdk/javascript-debug-ui/package.json; do
  if [ -f "$f" ] && ! grep -q "\"version\": \"$VERSION\"" "$f"; then
    ERRORS+=("$f: expected \"version\": \"$VERSION\"")
  fi
done

# Python
[ -f sdk/python/setup.py ] && ! grep -q "version=\"$VERSION\"" sdk/python/setup.py && ERRORS+=("sdk/python/setup.py")
[ -f sdk/python/src/flagent/__init__.py ] && ! grep -q "__version__ = \"$VERSION\"" sdk/python/src/flagent/__init__.py && ERRORS+=("sdk/python/src/flagent/__init__.py")

# Helm
[ -f helm/flagent/Chart.yaml ] && ! grep -q "^version: $VERSION" helm/flagent/Chart.yaml && ERRORS+=("helm/flagent/Chart.yaml")

# Backend
[ -f backend/src/main/kotlin/flagent/route/InfoRoutes.kt ] && ! grep -q "?: \"$VERSION\"" backend/src/main/kotlin/flagent/route/InfoRoutes.kt && ERRORS+=("InfoRoutes.kt")

# Frontend
[ -f frontend/src/jsMain/kotlin/flagent/frontend/components/Navbar.kt ] && ! grep -q "Text(\"v$VERSION\")" frontend/src/jsMain/kotlin/flagent/frontend/components/Navbar.kt && ERRORS+=("Navbar.kt")
[ -f frontend/src/jsMain/kotlin/flagent/frontend/components/ShellLayout.kt ] && ! grep -q "mutableStateOf(\"v$VERSION\")" frontend/src/jsMain/kotlin/flagent/frontend/components/ShellLayout.kt && ERRORS+=("ShellLayout.kt")

# Kotlin SDK fallback
[ -f sdk/kotlin-enhanced/build.gradle.kts ] && ! grep -q "kotlin-client:$VERSION" sdk/kotlin-enhanced/build.gradle.kts && ERRORS+=("kotlin-enhanced/build.gradle.kts")
[ -f sdk/kotlin-debug-ui/build.gradle.kts ] && ! grep -q "kotlin-enhanced:$VERSION" sdk/kotlin-debug-ui/build.gradle.kts && ERRORS+=("kotlin-debug-ui/build.gradle.kts")

# README - at least one dependency example
[ -f README.md ] && ! grep -q "com.flagent:kotlin-client:$VERSION" README.md && ! grep -q "com.flagent:ktor-flagent:$VERSION" README.md && ERRORS+=("README.md")

if [ ${#ERRORS[@]} -gt 0 ]; then
  echo "Version sync check failed. VERSION=$VERSION but these files are out of sync:" >&2
  printf '  - %s\n' "${ERRORS[@]}" >&2
  echo "" >&2
  echo "Run: ./scripts/sync-version.sh" >&2
  exit 1
fi
exit 0
