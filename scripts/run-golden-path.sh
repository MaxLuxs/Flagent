#!/usr/bin/env bash
# Golden path: start Flagent + optionally seed one flag + run Ktor sample.
# Usage: ./scripts/run-golden-path.sh [--no-seed] [--no-sample]
#   --no-seed   skip seeding new_payment_flow flag
#   --no-sample skip running sample-ktor (only start Flagent)
#
# Prerequisites: Docker, optional: jq. For sample: JDK 21, ./gradlew.
# Result: Flagent UI at http://localhost:18000, sample at http://localhost:8080.

set -e

NO_SEED=""
NO_SAMPLE=""
for arg in "$@"; do
  case "$arg" in
    --no-seed)   NO_SEED=1 ;;
    --no-sample) NO_SAMPLE=1 ;;
  esac
done

BACKEND_URL="${BACKEND_URL:-http://localhost:18000}"
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

echo "=== Flagent Golden Path ==="

# 1. Start Flagent (Docker Compose)
if ! docker compose ps -q backend 2>/dev/null | head -1 | xargs docker inspect --format '{{.State.Running}}' 2>/dev/null | grep -q true; then
  echo "Starting Flagent (docker compose up -d)..."
  docker compose up -d
else
  echo "Flagent already running (docker compose)."
fi

# 2. Wait for health
echo "Waiting for Flagent health at $BACKEND_URL/api/v1/health ..."
for i in {1..60}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/api/v1/health" 2>/dev/null || echo "000")
  if [ "$STATUS" = "200" ]; then
    echo "  OK (health 200)"
    break
  fi
  if [ "$i" -eq 60 ]; then
    echo "  Timeout waiting for health (got $STATUS)"
    exit 1
  fi
  sleep 1
done

# 3. Optional seed: one flag new_payment_flow (segment + variant + distribution)
seed_flag() {
  local base="$1"
  echo "Seeding flag new_payment_flow..."
  F=$(curl -s -S -X POST "$base/api/v1/flags" -H "Content-Type: application/json" \
    -d '{"key":"new_payment_flow","description":"New payment flow (golden path)","enabled":true}')
  FID=$(echo "$F" | jq -r '.id // empty')
  if [ -z "$FID" ]; then
    echo "  (flag may already exist or create failed: $F)"
    return
  fi
  curl -s -S -X PUT "$base/api/v1/flags/$FID/enabled" -H "Content-Type: application/json" -d '{"enabled":true}' >/dev/null
  S=$(curl -s -S -X POST "$base/api/v1/flags/$FID/segments" -H "Content-Type: application/json" \
    -d '{"description":"All users","rolloutPercent":100}')
  SID=$(echo "$S" | jq -r '.id // empty')
  if [ -n "$SID" ]; then
    V=$(curl -s -S -X POST "$base/api/v1/flags/$FID/variants" -H "Content-Type: application/json" -d '{"key":"control"}')
    VID=$(echo "$V" | jq -r '.id // empty')
    if [ -n "$VID" ]; then
      curl -s -S -X PUT "$base/api/v1/flags/$FID/segments/$SID/distributions" -H "Content-Type: application/json" \
        -d "{\"distributions\":[{\"variantID\":$VID,\"variantKey\":\"control\",\"percent\":100}]}" >/dev/null
    fi
  fi
  echo "  Created new_payment_flow (id=$FID). Try: curl -s $BACKEND_URL/api/v1/evaluation -H 'Content-Type: application/json' -d '{\"flagKey\":\"new_payment_flow\",\"entityID\":\"user1\"}'"
}

if [ -z "$NO_SEED" ]; then
  if command -v jq &>/dev/null; then
    seed_flag "$BACKEND_URL"
  else
    echo "Skipping seed (install jq to auto-seed: brew install jq)"
  fi
fi

# 4. Run Ktor sample (foreground)
if [ -z "$NO_SAMPLE" ]; then
  echo "Starting Ktor sample (port 8080)..."
  echo "  Sample: http://localhost:8080"
  echo "  Flagent UI: http://localhost:18000"
  echo "  Example: curl -s http://localhost:8080/feature/new_payment_flow?entityID=user1"
  echo ""
  exec ./gradlew :sample-ktor:runSample --no-daemon
else
  echo "Done. Flagent at $BACKEND_URL. Run sample with: ./gradlew :sample-ktor:runSample"
fi
