#!/usr/bin/env bash
# Vegeta burst test for POST /api/v1/evaluation
# Usage: ./evaluation-vegeta.sh [BASE_URL] [rate] [duration]
# Example: ./evaluation-vegeta.sh http://localhost:8000 500 10s
# Requires: vegeta (go install github.com/tsenart/vegeta/v12/cmd/vegeta@latest)

set -e

BASE_URL="${1:-http://localhost:8000}"
RATE="${2:-500}"
DURATION="${3:-10s}"
TARGETS_FILE="${TARGETS_FILE:-$(mktemp)}"
BODY='{"flagID":1,"entityID":"user_1","entityType":"user","entityContext":{"country":"US"},"enableDebug":false}'

cleanup() { rm -f "$TARGETS_FILE"; }
trap cleanup EXIT

if ! command -v vegeta &>/dev/null; then
  echo "vegeta not found. Install: go install github.com/tsenart/vegeta/v12/cmd/vegeta@latest"
  exit 1
fi

{
  echo "POST $BASE_URL/api/v1/evaluation"
  echo "Content-Type: application/json"
  echo ""
  echo "$BODY"
} > "$TARGETS_FILE"

echo "Evaluation burst test: $RATE req/s for $DURATION -> $BASE_URL/api/v1/evaluation"
vegeta attack -targets="$TARGETS_FILE" -rate="$RATE" -duration="$DURATION" | vegeta report
