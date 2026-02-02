#!/usr/bin/env bash
# Run E2E tests using production build served by backend (single server, no webpack dev)
# Usage: ./run-e2e-with-backend.sh <scenario>
# Scenario: oss | tenant | auth
# Prerequisites: Run backend in another terminal: ./gradlew :backend:runDev

set -e

SCENARIO="${1:-oss}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
E2E_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FLAGENT_ROOT="$(cd "$E2E_DIR/../.." && pwd)"

BACKEND_URL="${BACKEND_URL:-http://localhost:18000}"
# Backend serves production frontend - one URL for both API and UI
FRONTEND_URL="${FRONTEND_URL:-$BACKEND_URL}"

export FLAGENT_ADMIN_API_KEY="${FLAGENT_ADMIN_API_KEY:-dev-admin-key}"
export FLAGENT_ADMIN_EMAIL="${FLAGENT_ADMIN_EMAIL:-admin@local}"
export FLAGENT_ADMIN_PASSWORD="${FLAGENT_ADMIN_PASSWORD:-admin}"

echo "=== E2E Scenario: $SCENARIO ==="
echo "Backend+Frontend: $BACKEND_URL (production build)"

# Build frontend (development - production has CoroutineContext bug, clean fails intermittently)
echo "Building frontend..."
(cd "$FLAGENT_ROOT" && ./gradlew :frontend:jsBrowserDevelopmentWebpack -q) || {
  echo "❌ Frontend build failed"
  exit 1
}

# Check backend is running
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/api/v1/health" 2>/dev/null || echo "000")
if [ "$BACKEND_STATUS" != "200" ]; then
  echo "❌ Backend not running at $BACKEND_URL (status: $BACKEND_STATUS)"
  echo "Start with: ./gradlew :backend:runDev (from flagent root)"
  exit 1
fi

# Check frontend is served (backend serves static from productionExecutable)
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL/" 2>/dev/null || echo "000")
if [ "$FRONTEND_STATUS" != "200" ]; then
  echo "❌ Frontend not served at $FRONTEND_URL (status: $FRONTEND_STATUS)"
  echo "Ensure backend was started from flagent root so it finds frontend/build/.../productionExecutable"
  exit 1
fi

echo "✓ Backend running, production frontend served"

# For OSS: create tenant so backend has one (dev mode uses first tenant when no X-API-Key)
if [ "$SCENARIO" = "oss" ]; then
  echo "Creating tenant for OSS scenario..."
  curl -s -X POST "$BACKEND_URL/admin/tenants" \
    -H "Content-Type: application/json" \
    -H "X-Admin-Key: $FLAGENT_ADMIN_API_KEY" \
    -d '{"key":"e2e-oss-'$(date +%s)'","name":"E2E OSS","plan":"STARTER","ownerEmail":"oss@e2e.local"}' \
    > /dev/null 2>&1 || echo "(tenant may already exist)"
  sleep 1
fi

# Run playwright
echo "Running Playwright tests..."
cd "$E2E_DIR"
export BACKEND_URL
export FRONTEND_URL
export FLAGENT_ADMIN_API_KEY
export FLAGENT_ADMIN_EMAIL
export FLAGENT_ADMIN_PASSWORD
unset CI

case "$SCENARIO" in
  oss)
    npx playwright test --project=chromium tests/create-flag.spec.ts -g "Open Source"
    ;;
  tenant)
    npx playwright test --project=chromium tests/create-flag.spec.ts -g "With Tenant"
    ;;
  auth)
    npx playwright test --project=chromium tests/create-flag.spec.ts -g "With Auth"
    ;;
  *)
    echo "Unknown scenario: $SCENARIO (use oss|tenant|auth)"
    exit 1
    ;;
esac
