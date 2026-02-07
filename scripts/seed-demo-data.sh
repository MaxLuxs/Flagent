#!/usr/bin/env bash
# Seed demo data for Flagent - flags, segments, variants, experiments.
# Prerequisites: Backend running (./gradlew :backend:runDev)
# Usage: ./scripts/seed-demo-data.sh

set -e

BACKEND_URL="${BACKEND_URL:-http://localhost:18000}"
ADMIN_KEY="${FLAGENT_ADMIN_API_KEY:-dev-admin-key}"

echo "=== Flagent Demo Data Seeder ==="
echo "Backend: $BACKEND_URL"

# Check backend
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/api/v1/health" 2>/dev/null || echo "000")
if [ "$STATUS" != "200" ]; then
  echo "❌ Backend not running at $BACKEND_URL (status: $STATUS)"
  echo "Start with: ./gradlew :backend:runDev"
  exit 1
fi
echo "✓ Backend OK"

# Create tenant (enterprise) - X-Admin-Key required
TENANT_KEY="demo-$(date +%s)"
echo "Creating tenant: $TENANT_KEY..."
TENANT_RESP=$(curl -s -X POST "$BACKEND_URL/admin/tenants" \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: $ADMIN_KEY" \
  -d "{\"key\":\"$TENANT_KEY\",\"name\":\"Demo Tenant\",\"plan\":\"ENTERPRISE\",\"ownerEmail\":\"admin@local\"}" 2>/dev/null || echo "{}")

# Extract API key (jq or grep)
if command -v jq &>/dev/null; then
  API_KEY=$(echo "$TENANT_RESP" | jq -r '.apiKey // empty')
else
  API_KEY=$(echo "$TENANT_RESP" | grep -o '"apiKey":"[^"]*"' | cut -d'"' -f4)
fi

if [ -n "$API_KEY" ]; then
  echo "✓ Tenant created, API key: ${API_KEY:0:12}..."
  AUTH_HEADER="X-API-Key: $API_KEY"
else
  echo "⚠ No tenant/apiKey (OSS or existing). Proceeding without X-API-Key..."
  AUTH_HEADER=""
fi

api() {
  if [ -n "$AUTH_HEADER" ]; then
    curl -s -X "$1" "$BACKEND_URL$2" -H "Content-Type: application/json" -H "$AUTH_HEADER" -d "$3"
  else
    curl -s -X "$1" "$BACKEND_URL$2" -H "Content-Type: application/json" -d "$3"
  fi
}

getId() {
  if command -v jq &>/dev/null; then
    echo "$1" | jq -r '.id // .ID // empty'
  else
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2
  fi
}

# 1. Simple boolean flag
echo "Creating flag: new_checkout_flow..."
F1=$(api POST "/api/v1/flags" '{"key":"new_checkout_flow","description":"New checkout flow toggle"}')
F1_ID=$(getId "$F1")
if [ -n "$F1_ID" ]; then
  api PUT "/api/v1/flags/$F1_ID/enabled" '{"enabled":true}' >/dev/null
  echo "  ✓ new_checkout_flow (id=$F1_ID)"
fi

# 2. A/B experiment flag
echo "Creating flag: payment_method_exp..."
F2=$(api POST "/api/v1/flags" '{"key":"payment_method_exp","description":"A/B test: payment method selection"}')
F2_ID=$(getId "$F2")
if [ -n "$F2_ID" ]; then
  api PUT "/api/v1/flags/$F2_ID/enabled" '{"enabled":true}' >/dev/null
  S2=$(api POST "/api/v1/flags/$F2_ID/segments" '{"description":"All users","rolloutPercent":100}')
  S2_ID=$(getId "$S2")
  if [ -n "$S2_ID" ]; then
    V2A=$(api POST "/api/v1/flags/$F2_ID/variants" '{"key":"control"}')
    V2B=$(api POST "/api/v1/flags/$F2_ID/variants" '{"key":"variant_card"}')
    V2A_ID=$(getId "$V2A")
    V2B_ID=$(getId "$V2B")
    if [ -n "$V2A_ID" ] && [ -n "$V2B_ID" ]; then
      api PUT "/api/v1/flags/$F2_ID/segments/$S2_ID/distributions" "{\"distributions\":[{\"variantID\":$V2A_ID,\"variantKey\":\"control\",\"percent\":50},{\"variantID\":$V2B_ID,\"variantKey\":\"variant_card\",\"percent\":50}]}" >/dev/null
    fi
  fi
  echo "  ✓ payment_method_exp (id=$F2_ID) - 50/50 A/B"
fi

# 3. Feature with segment
echo "Creating flag: premium_features..."
F3=$(api POST "/api/v1/flags" '{"key":"premium_features","description":"Premium user features"}')
F3_ID=$(getId "$F3")
if [ -n "$F3_ID" ]; then
  api PUT "/api/v1/flags/$F3_ID/enabled" '{"enabled":true}' >/dev/null
  S3=$(api POST "/api/v1/flags/$F3_ID/segments" '{"description":"Premium tier","rolloutPercent":100}')
  S3_ID=$(getId "$S3")
  if [ -n "$S3_ID" ]; then
    api POST "/api/v1/flags/$F3_ID/segments/$S3_ID/constraints" '{"property":"tier","operator":"EQ","value":"premium"}' >/dev/null
  fi
  V3=$(api POST "/api/v1/flags/$F3_ID/variants" '{"key":"enabled"}')
  V3_ID=$(getId "$V3")
  if [ -n "$S3_ID" ] && [ -n "$V3_ID" ]; then
    api PUT "/api/v1/flags/$F3_ID/segments/$S3_ID/distributions" "{\"distributions\":[{\"variantID\":$V3_ID,\"variantKey\":\"enabled\",\"percent\":100}]}" >/dev/null
  fi
  echo "  ✓ premium_features (id=$F3_ID) - tier=premium"
fi

# 4. Gradual rollout flag
echo "Creating flag: dark_mode_rollout..."
F4=$(api POST "/api/v1/flags" '{"key":"dark_mode_rollout","description":"Dark mode gradual rollout"}')
F4_ID=$(getId "$F4")
if [ -n "$F4_ID" ]; then
  api PUT "/api/v1/flags/$F4_ID/enabled" '{"enabled":true}' >/dev/null
  S4=$(api POST "/api/v1/flags/$F4_ID/segments" '{"description":"25% rollout","rolloutPercent":25}')
  S4_ID=$(getId "$S4")
  if [ -n "$S4_ID" ]; then
    V4=$(api POST "/api/v1/flags/$F4_ID/variants" '{"key":"enabled"}')
    V4_ID=$(getId "$V4")
    if [ -n "$V4_ID" ]; then
      api PUT "/api/v1/flags/$F4_ID/segments/$S4_ID/distributions" "{\"distributions\":[{\"variantID\":$V4_ID,\"variantKey\":\"enabled\",\"percent\":100}]}" >/dev/null
    fi
  fi
  echo "  ✓ dark_mode_rollout (id=$F4_ID) - 25%"
fi

# 5. Analytics events (if endpoint exists)
echo "Sending analytics events..."
api POST "/api/v1/analytics/events" '{"events":[{"name":"first_open","timestamp":'$(date +%s000)'},{"name":"screen_view","properties":{"screen":"dashboard"},"timestamp":'$(date +%s000)'}]}' >/dev/null 2>&1 || true

echo ""
echo "=== Done ==="
echo "Open http://localhost:8080/dashboard (or $BACKEND_URL if frontend served by backend)"
if [ -n "$API_KEY" ]; then
  echo "API Key for SDK: $API_KEY"
  echo "Save in localStorage (Settings) or set ENV_API_KEY in frontend"
fi
