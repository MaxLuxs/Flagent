#!/usr/bin/env bash
# Run locally the same steps that CI runs on push to main.
# Requires: JDK 21, Docker, Node 20 (for frontend E2E optional).
# Usage: ./scripts/ci-local.sh [--docker-only] [--no-docker]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

DOCKER_ONLY=
NO_DOCKER=
for arg in "$@"; do
  case "$arg" in
    --docker-only) DOCKER_ONLY=1 ;;
    --no-docker)   NO_DOCKER=1 ;;
  esac
done

run() {
  echo ">>> $*"
  "$@"
}

# --- Container scan (security-scan.yml): exact same Docker build as CI ---
run_docker_build() {
  echo "========== Container build (same as CI container-scan) =========="
  docker build -t flagent:ci-local -f Dockerfile .
  echo "Docker build OK."
}

# --- CI main job (ci.yml unit_test) ---
run_gradle_ci() {
  echo "========== Gradle build (ci unit_test) =========="
  chmod +x ./gradlew
  run ./gradlew build --no-daemon -x test
  run ./gradlew :backend:test :shared:jvmTest --no-daemon
  run ./design-system/check-hardcode.sh
  run ./gradlew :backend:test --no-daemon --tests "flagent.test.e2e.E2ETest"
  echo "Gradle CI steps OK."
}

# --- Frontend webpack (used in Docker + frontend-e2e) ---
run_frontend_webpack() {
  echo "========== Frontend webpack (same as in Docker) =========="
  run ./gradlew :frontend:jsBrowserDevelopmentWebpack --no-daemon
  echo "Frontend webpack OK."
}

if [[ -n "$DOCKER_ONLY" ]]; then
  run_docker_build
  exit 0
fi

if [[ -z "$NO_DOCKER" ]]; then
  run_docker_build
fi

run_gradle_ci
run_frontend_webpack

echo ""
echo "========== CI-local passed (Docker + Gradle + frontend). =========="
echo "Optional: run actionlint, go/js/python SDKs, android-sample, samples, frontend-e2e in CI."
