#!/usr/bin/env bash
# Verify all SDKs: build and run tests where applicable.
# Exits with non-zero if any step fails.
# Requires (depending on SDKs): JDK 21, Node 20, Python 3.11, Go 1.22, Dart stable.
# Usage: ./scripts/verify-sdks.sh
# Optional: SKIP_KOTLIN=1 SKIP_JS=1 SKIP_PYTHON=1 SKIP_GO=1 SKIP_DART=1 to skip specific SDKs.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

FAILED=0
run() {
  echo ">>> $*"
  if "$@"; then
    echo "OK: $*"
  else
    echo "FAILED: $*"
    FAILED=1
    return 1
  fi
}

# --- Kotlin SDKs (Gradle) ---
if [[ -z "${SKIP_KOTLIN:-}" ]]; then
  echo "========== Kotlin SDKs =========="
  run ./gradlew :kotlin-client:jvmTest :kotlin-enhanced:jvmTest :flagent-koin:jvmTest --no-daemon
  # kotlin-debug-ui requires display (or xvfb); in CI see job kotlin-debug-ui in ci.yml
  # Optional: ./gradlew :kotlin-debug-ui:jvmTest
fi

# --- JavaScript ---
if [[ -z "${SKIP_JS:-}" ]]; then
  echo "========== sdk/javascript =========="
  (cd sdk/javascript && (npm ci 2>/dev/null || npm install) && npm run build && npm test) || FAILED=1
  echo "========== sdk/javascript-enhanced =========="
  (cd sdk/javascript-enhanced && npm install && npm run build && npm test) || FAILED=1
fi

# --- Python ---
if [[ -z "${SKIP_PYTHON:-}" ]]; then
  echo "========== sdk/python =========="
  (cd sdk/python && pip install -e ".[dev]" && python -c "import flagent; print('OK')" && pytest tests/ -v) || FAILED=1
fi

# --- Go ---
if [[ -z "${SKIP_GO:-}" ]]; then
  echo "========== sdk/go =========="
  (cd sdk/go && go build ./... && go test ./...) || FAILED=1
  echo "========== sdk/go-enhanced =========="
  (cd sdk/go-enhanced && go build . && go test ./...) || FAILED=1
fi

# --- Dart ---
if [[ -z "${SKIP_DART:-}" ]]; then
  echo "========== sdk/dart =========="
  (cd sdk/dart && dart pub get && dart run build_runner build --delete-conflicting-outputs && dart analyze && dart test) || FAILED=1
  echo "========== sdk/flutter-enhanced =========="
  (cd sdk/flutter-enhanced && dart pub get && dart analyze && dart test) || FAILED=1
fi

if [[ "$FAILED" -ne 0 ]]; then
  echo ""
  echo "========== verify-sdks.sh: one or more steps failed =========="
  exit 1
fi

echo ""
echo "========== verify-sdks.sh: all SDK checks passed =========="
exit 0
