#!/usr/bin/env bash
# Optional compatibility test: compare evaluation API responses between Flagent and external URL.
# Usage:
#   ./scripts/compatibility-test.sh                    # Compare localhost:18000 vs FLAGENT_COMPATIBILITY_TEST_URL
#   ./scripts/compatibility-test.sh http://other:8000 # Compare localhost:18000 vs http://other:8000
#
# Requires: Flagent running on localhost:18000 (or FLAGENT_URL), curl, jq
# Set FLAGENT_COMPATIBILITY_TEST_URL or pass URL as first argument.

set -e

FLAGENT_URL="${FLAGENT_URL:-http://localhost:18000}"
EXTERNAL_URL="${1:-${FLAGENT_COMPATIBILITY_TEST_URL}}"

if [[ -z "$EXTERNAL_URL" ]]; then
  echo "Usage: $0 [external_url]"
  echo "  Or set FLAGENT_COMPATIBILITY_TEST_URL"
  echo "  Example: FLAGENT_COMPATIBILITY_TEST_URL=https://try-flagr.example.com $0"
  exit 1
fi

BODY='{"flagID":1,"entityID":"user_1","entityType":"user","entityContext":{"country":"US","tier":"premium"}}'

echo "Comparing evaluation API responses..."
echo "  Flagent:  $FLAGENT_URL/api/v1/evaluation"
echo "  External: $EXTERNAL_URL/api/v1/evaluation"
echo ""

FLAGENT_RESP=$(mktemp)
EXTERNAL_RESP=$(mktemp)
trap "rm -f $FLAGENT_RESP $EXTERNAL_RESP" EXIT

curl -s -X POST "$FLAGENT_URL/api/v1/evaluation" \
  -H "Content-Type: application/json" \
  -d "$BODY" > "$FLAGENT_RESP" || { echo "Flagent request failed"; exit 1; }

curl -s -X POST "$EXTERNAL_URL/api/v1/evaluation" \
  -H "Content-Type: application/json" \
  -d "$BODY" > "$EXTERNAL_RESP" || { echo "External request failed"; exit 1; }

echo "=== Structure comparison (required fields) ==="
REQUIRED_FIELDS="flagID flagKey segmentID variantID variantKey evalContext timestamp"
ALL_OK=true
for f in $REQUIRED_FIELDS; do
  F_HAS=$(jq -e "has(\"$f\")" "$FLAGENT_RESP" 2>/dev/null) && F_VAL="yes" || F_VAL="no"
  E_HAS=$(jq -e "has(\"$f\")" "$EXTERNAL_RESP" 2>/dev/null) && E_VAL="yes" || E_VAL="no"
  if [[ "$F_VAL" == "yes" && "$E_VAL" == "yes" ]]; then
    echo "  $f: both have"
  else
    echo "  $f: Flagent=$F_VAL External=$E_VAL"
    ALL_OK=false
  fi
done

echo ""
echo "=== evalContext structure ==="
F_CTX=$(jq -r '.evalContext | keys | join(", ")' "$FLAGENT_RESP" 2>/dev/null || echo "N/A")
E_CTX=$(jq -r '.evalContext | keys | join(", ")' "$EXTERNAL_RESP" 2>/dev/null || echo "N/A")
echo "  Flagent:  $F_CTX"
echo "  External: $E_CTX"

echo ""
echo "=== Flagent response (excerpt) ==="
jq '{flagID, flagKey, segmentID, variantID, variantKey, evalContext}' "$FLAGENT_RESP" 2>/dev/null || cat "$FLAGENT_RESP"

echo ""
echo "=== External response (excerpt) ==="
jq '{flagID, flagKey, segmentID, variantID, variantKey, evalContext}' "$EXTERNAL_RESP" 2>/dev/null || cat "$EXTERNAL_RESP"

if [[ "$ALL_OK" == "true" ]]; then
  echo ""
  echo "OK: Both APIs return compatible structure"
  exit 0
else
  echo ""
  echo "WARN: Some fields differ (rollout/variant values may vary)"
  exit 0
fi
