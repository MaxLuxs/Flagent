#!/usr/bin/env bash
# Seed demo data for Flagent - tenant, flags, segments, variants, experiments,
# evaluation events (analytics/metrics), analytics events, crash reports.
# Prerequisites: Backend running (./gradlew :backend:runDev)
#
# Usage:
#   ./scripts/seed-demo-data.sh
#     Creates a new tenant and seeds all data. Prints API key for frontend.
#
#   FLAGENT_API_KEY=your-existing-key ./scripts/seed-demo-data.sh
#     Seeds data into your existing tenant (no new tenant created).
#
#   FLAGENT_DEMO_TENANT_KEY=demo ./scripts/seed-demo-data.sh
#     Creates tenant with fixed key "demo" (idempotent: run again = new API key only if tenant is new).

set -e

BACKEND_URL="${BACKEND_URL:-http://localhost:18000}"
ADMIN_KEY="${FLAGENT_ADMIN_API_KEY:-dev-admin-key}"
EXISTING_API_KEY="${FLAGENT_API_KEY:-}"
FIXED_TENANT_KEY="${FLAGENT_DEMO_TENANT_KEY:-}"

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

# Use existing API key or create tenant
if [ -n "$EXISTING_API_KEY" ]; then
  echo "Using existing FLAGENT_API_KEY (seeding into your tenant)..."
  API_KEY="$EXISTING_API_KEY"
  AUTH_HEADER="X-API-Key: $API_KEY"
else
  TENANT_KEY="${FIXED_TENANT_KEY:-demo-$(date +%s)}"
  echo "Creating tenant: $TENANT_KEY..."
  TENANT_RESP=$(curl -s -X POST "$BACKEND_URL/admin/tenants" \
    -H "Content-Type: application/json" \
    -H "X-Admin-Key: $ADMIN_KEY" \
    -d "{\"key\":\"$TENANT_KEY\",\"name\":\"Demo Tenant\",\"plan\":\"ENTERPRISE\",\"ownerEmail\":\"admin@local\"}" 2>/dev/null || echo "{}")

  if command -v jq &>/dev/null; then
    API_KEY=$(echo "$TENANT_RESP" | jq -r '.apiKey // empty')
  else
    API_KEY=$(echo "$TENANT_RESP" | grep -o '"apiKey":"[^"]*"' | cut -d'"' -f4)
  fi

  if [ -n "$API_KEY" ]; then
    echo "✓ Tenant created, API key: ${API_KEY:0:12}..."
    AUTH_HEADER="X-API-Key: $API_KEY"
  else
    echo "⚠ No tenant/apiKey (OSS or tenant exists). Proceeding without X-API-Key..."
    AUTH_HEADER=""
  fi
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

# --- Flags (with segments/variants so evaluations work) ---

# 1. Simple boolean flag (legacy sample)
echo "Creating flag: new_checkout_flow..."
F1=$(api POST "/api/v1/flags" '{"key":"new_checkout_flow","description":"New checkout flow toggle"}')
F1_ID=$(getId "$F1")
if [ -n "$F1_ID" ]; then
  api PUT "/api/v1/flags/$F1_ID/enabled" '{"enabled":true}' >/dev/null
  S1=$(api POST "/api/v1/flags/$F1_ID/segments" '{"description":"All users","rolloutPercent":100}')
  S1_ID=$(getId "$S1")
  if [ -n "$S1_ID" ]; then
    V1=$(api POST "/api/v1/flags/$F1_ID/variants" '{"key":"on"}')
    V1_ID=$(getId "$V1")
    [ -n "$V1_ID" ] && api PUT "/api/v1/flags/$F1_ID/segments/$S1_ID/distributions" "{\"distributions\":[{\"variantID\":$V1_ID,\"variantKey\":\"on\",\"percent\":100}]}" >/dev/null
  fi
  echo "  ✓ new_checkout_flow (id=$F1_ID)"
fi

# 2. Android sample: boolean flag for new UI
echo "Creating flag: new_android_ui..."
F_ANDROID=$(api POST "/api/v1/flags" '{"key":"new_android_ui","description":"Android sample: new UI toggle"}')
F_ANDROID_ID=$(getId "$F_ANDROID")
if [ -n "$F_ANDROID_ID" ]; then
  api PUT "/api/v1/flags/$F_ANDROID_ID/enabled" '{"enabled":true}' >/dev/null
  S_ANDROID=$(api POST "/api/v1/flags/$F_ANDROID_ID/segments" '{"description":"All users","rolloutPercent":100}')
  S_ANDROID_ID=$(getId "$S_ANDROID")
  if [ -n "$S_ANDROID_ID" ]; then
    V_ANDROID=$(api POST "/api/v1/flags/$F_ANDROID_ID/variants" '{"key":"enabled"}')
    V_ANDROID_ID=$(getId "$V_ANDROID")
    [ -n "$V_ANDROID_ID" ] && api PUT "/api/v1/flags/$F_ANDROID_ID/segments/$S_ANDROID_ID/distributions" "{\"distributions\":[{\"variantID\":$V_ANDROID_ID,\"variantKey\":\"enabled\",\"percent\":100}]}" >/dev/null
  fi
  echo "  ✓ new_android_ui (id=$F_ANDROID_ID)"
fi

# 3. A/B experiment flag
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

# 4. Feature with segment + constraint
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

# 5. Gradual rollout flag
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

# 6. Extra flags for analytics variety (used in evaluations below)
echo "Creating extra flags for analytics..."
F5=$(api POST "/api/v1/flags" '{"key":"search_redesign","description":"New search UI"}')
F5_ID=$(getId "$F5")
if [ -n "$F5_ID" ]; then
  api PUT "/api/v1/flags/$F5_ID/enabled" '{"enabled":true}' >/dev/null
  S5=$(api POST "/api/v1/flags/$F5_ID/segments" '{"description":"All","rolloutPercent":100}')
  S5_ID=$(getId "$S5")
  V5=$(api POST "/api/v1/flags/$F5_ID/variants" '{"key":"on"}')
  V5_ID=$(getId "$V5")
  [ -n "$S5_ID" ] && [ -n "$V5_ID" ] && api PUT "/api/v1/flags/$F5_ID/segments/$S5_ID/distributions" "{\"distributions\":[{\"variantID\":$V5_ID,\"variantKey\":\"on\",\"percent\":100}]}" >/dev/null
  echo "  ✓ search_redesign (id=$F5_ID)"
fi

F6=$(api POST "/api/v1/flags" '{"key":"promo_banner","description":"Promo banner visibility"}')
F6_ID=$(getId "$F6")
if [ -n "$F6_ID" ]; then
  api PUT "/api/v1/flags/$F6_ID/enabled" '{"enabled":true}' >/dev/null
  S6=$(api POST "/api/v1/flags/$F6_ID/segments" '{"description":"All","rolloutPercent":50}')
  S6_ID=$(getId "$S6")
  V6=$(api POST "/api/v1/flags/$F6_ID/variants" '{"key":"show"}')
  V6_ID=$(getId "$V6")
  [ -n "$S6_ID" ] && [ -n "$V6_ID" ] && api PUT "/api/v1/flags/$F6_ID/segments/$S6_ID/distributions" "{\"distributions\":[{\"variantID\":$V6_ID,\"variantKey\":\"show\",\"percent\":100}]}" >/dev/null
  echo "  ✓ promo_banner (id=$F6_ID)"
fi

# 7. Android sample: numeric config flags
echo "Creating numeric config flags for Android sample..."

F_CART=$(api POST "/api/v1/flags" '{"key":"cart_max_items","description":"Max items in cart (Android sample)"}')
F_CART_ID=$(getId "$F_CART")
if [ -n "$F_CART_ID" ]; then
  api PUT "/api/v1/flags/$F_CART_ID/enabled" '{"enabled":true}' >/dev/null
  S_CART=$(api POST "/api/v1/flags/$F_CART_ID/segments" '{"description":"All users","rolloutPercent":100}')
  S_CART_ID=$(getId "$S_CART")
  if [ -n "$S_CART_ID" ]; then
    V_CART=$(api POST "/api/v1/flags/$F_CART_ID/variants" '{"key":"default","attachment":"{\"value\":42}"}')
    V_CART_ID=$(getId "$V_CART")
    [ -n "$V_CART_ID" ] && api PUT "/api/v1/flags/$F_CART_ID/segments/$S_CART_ID/distributions" "{\"distributions\":[{\"variantID\":$V_CART_ID,\"variantKey\":\"default\",\"percent\":100}]}" >/dev/null
  fi
  echo "  ✓ cart_max_items (id=$F_CART_ID, value=42)"
fi

F_DISCOUNT=$(api POST "/api/v1/flags" '{"key":"discount_rate","description":"Discount rate (Android sample)"}')
F_DISCOUNT_ID=$(getId "$F_DISCOUNT")
if [ -n "$F_DISCOUNT_ID" ]; then
  api PUT "/api/v1/flags/$F_DISCOUNT_ID/enabled" '{"enabled":true}' >/dev/null
  S_DISCOUNT=$(api POST "/api/v1/flags/$F_DISCOUNT_ID/segments" '{"description":"All users","rolloutPercent":100}')
  S_DISCOUNT_ID=$(getId "$S_DISCOUNT")
  if [ -n "$S_DISCOUNT_ID" ]; then
    V_DISCOUNT=$(api POST "/api/v1/flags/$F_DISCOUNT_ID/variants" '{"key":"default","attachment":"{\"value\":0.15}"}')
    V_DISCOUNT_ID=$(getId "$V_DISCOUNT")
    [ -n "$V_DISCOUNT_ID" ] && api PUT "/api/v1/flags/$F_DISCOUNT_ID/segments/$S_DISCOUNT_ID/distributions" "{\"distributions\":[{\"variantID\":$V_DISCOUNT_ID,\"variantKey\":\"default\",\"percent\":100}]}" >/dev/null
  fi
  echo "  ✓ discount_rate (id=$F_DISCOUNT_ID, value=0.15)"
fi

# Wait for EvalCache to pick up new flags (local default refresh ~3s)
echo "Waiting for eval cache refresh (4s)..."
sleep 4

# --- Evaluation calls (feed Core metrics: /metrics/overview, per-flag stats) ---
# Fetch flag IDs from API so we have them even when seeding into existing tenant (flags already exist)
echo "Sending evaluation requests (for Analytics / Overview charts)..."
FLAGS_JSON=""
if [ -n "$AUTH_HEADER" ]; then
  FLAGS_JSON=$(curl -s "$BACKEND_URL/api/v1/flags" -H "Content-Type: application/json" -H "$AUTH_HEADER" 2>/dev/null || echo "{}")
else
  FLAGS_JSON=$(curl -s "$BACKEND_URL/api/v1/flags" -H "Content-Type: application/json" 2>/dev/null || echo "{}")
fi
if command -v jq &>/dev/null && [ -n "$FLAGS_JSON" ]; then
  # Backend GET /flags returns JSON array of flag objects: [{ "id": 1, "key": "...", ... }, ...]
  eval_flag_ids=$(echo "$FLAGS_JSON" | jq -r '.[].id' 2>/dev/null | tr '\n' ' ')
fi
if [ -z "$eval_flag_ids" ]; then
  [ -n "$F1_ID" ] && eval_flag_ids="$eval_flag_ids $F1_ID"
  [ -n "$F2_ID" ] && eval_flag_ids="$eval_flag_ids $F2_ID"
  [ -n "$F3_ID" ] && eval_flag_ids="$eval_flag_ids $F3_ID"
  [ -n "$F4_ID" ] && eval_flag_ids="$eval_flag_ids $F4_ID"
  [ -n "$F5_ID" ] && eval_flag_ids="$eval_flag_ids $F5_ID"
  [ -n "$F6_ID" ] && eval_flag_ids="$eval_flag_ids $F6_ID"
fi
NOW_MS=$(($(date +%s) * 1000))
eval_count=0
idx=0
for f in $eval_flag_ids; do
  [ -z "$f" ] && continue
  n=80
  [ $idx -eq 1 ] && n=120
  [ $idx -eq 2 ] && n=45
  [ $idx -eq 3 ] && n=60
  [ $idx -eq 4 ] && n=90
  [ $idx -eq 5 ] && n=30
  i=0
  while [ $i -lt $n ]; do
    api POST "/api/v1/evaluation" "{\"flagID\":$f,\"entityID\":\"user_f${f}_${i}\",\"enableDebug\":false}" >/dev/null 2>&1 && eval_count=$((eval_count + 1))
    i=$((i + 1))
  done
  idx=$((idx + 1))
done
echo "  ✓ $eval_count evaluation requests sent (flags: $(echo $eval_flag_ids | wc -w | tr -d ' '))"
if [ "$eval_count" -eq 0 ] && [ -n "$AUTH_HEADER" ]; then
  echo "  ⚠ Tip: GET /api/v1/flags returned no IDs or POST /evaluation failed. Check X-API-Key and that flags exist."
fi

# Batch flush for evaluation events is ~1s; wait so DB has data for Overview
echo "Waiting for metrics persistence (3s)..."
sleep 3

# --- Analytics events (Firebase-style; for Analytics tab "Events") ---
echo "Sending analytics events..."
TS="${NOW_MS}"
api POST "/api/v1/analytics/events" "{\"events\":[
  {\"eventName\":\"first_open\",\"timestampMs\":$TS,\"userId\":\"user_1\",\"platform\":\"ios\",\"appVersion\":\"1.0.0\"},
  {\"eventName\":\"screen_view\",\"eventParams\":\"{\\\"screen\\\":\\\"dashboard\\\"}\",\"timestampMs\":$TS,\"userId\":\"user_1\"},
  {\"eventName\":\"screen_view\",\"eventParams\":\"{\\\"screen\\\":\\\"flags\\\"}\",\"timestampMs\":$TS,\"userId\":\"user_1\"},
  {\"eventName\":\"button_click\",\"eventParams\":\"{\\\"name\\\":\\\"create_flag\\\"}\",\"timestampMs\":$TS},
  {\"eventName\":\"evaluation\",\"eventParams\":\"{\\\"flag_key\\\":\\\"new_checkout_flow\\\"}\",\"timestampMs\":$TS,\"userId\":\"user_2\"},
  {\"eventName\":\"purchase\",\"eventParams\":\"{\\\"value\\\":29.99}\",\"timestampMs\":$TS,\"userId\":\"user_2\"}
]}" >/dev/null 2>&1 || true
# Spread events over last 7 days so Overview charts have multiple buckets
echo "  Seeding analytics for last 7 days..."
SEC_PER_DAY=86400
for day in 0 1 2 3 4 5 6; do
  DAY_MS=$(( (NOW_MS / 1000 - day * SEC_PER_DAY) * 1000 ))
  api POST "/api/v1/analytics/events" "{\"events\":[
    {\"eventName\":\"session_start\",\"timestampMs\":$DAY_MS,\"userId\":\"user_$((day % 3 + 1))\",\"platform\":\"android\",\"appVersion\":\"1.0.$day\"},
    {\"eventName\":\"screen_view\",\"eventParams\":\"{\\\"screen\\\":\\\"home\\\"}\",\"timestampMs\":$DAY_MS,\"userId\":\"user_$((day % 3 + 1))\"},
    {\"eventName\":\"screen_view\",\"eventParams\":\"{\\\"screen\\\":\\\"checkout\\\"}\",\"timestampMs\":$DAY_MS,\"userId\":\"user_$((day % 3 + 1))\"},
    {\"eventName\":\"purchase\",\"eventParams\":\"{\\\"value\\\":$((day * 10 + 5))}\",\"timestampMs\":$DAY_MS,\"userId\":\"user_$((day % 2 + 1))\"}
  ]}" >/dev/null 2>&1 || true
done
echo "  ✓ Analytics events sent (incl. 7 days history)"

# --- Crash reports (for Crash / Metrics dashboard) ---
echo "Sending crash reports..."
CRASH_TS=$(($(date +%s) * 1000))
api POST "/api/v1/crashes/batch" "[
  {\"stackTrace\":\"at com.example.MainActivity.onCreate(MainActivity.kt:42)\\n  at ...\",\"message\":\"NullPointerException\",\"platform\":\"android\",\"appVersion\":\"1.2.0\",\"deviceInfo\":\"Pixel 6\",\"activeFlagKeys\":[\"new_checkout_flow\",\"payment_method_exp\"],\"timestamp\":$CRASH_TS},
  {\"stackTrace\":\"at io.ktor.serialization.KotlinxSerializationKt.decode(KotlinxSerialization.kt:10)\",\"message\":\"SerializationException\",\"platform\":\"ios\",\"appVersion\":\"1.1.5\",\"deviceInfo\":\"iPhone 14\",\"activeFlagKeys\":[\"dark_mode_rollout\"],\"timestamp\":$((CRASH_TS - 3600000))},
  {\"stackTrace\":\"at kotlinx.coroutines.CoroutineScopeKt.coroutineScope(CoroutineScope.kt:...)\",\"message\":\"TimeoutException\",\"platform\":\"android\",\"appVersion\":\"1.2.0\",\"breadcrumbs\":\"screen=settings;action=save\",\"timestamp\":$((CRASH_TS - 7200000))},
  {\"stackTrace\":\"at UIKit.UIViewController.viewDidLoad\",\"message\":\"Fatal error: Unexpectedly found nil\",\"platform\":\"ios\",\"appVersion\":\"1.1.4\",\"deviceInfo\":\"iPhone 13\",\"activeFlagKeys\":[\"premium_features\",\"search_redesign\"],\"timestamp\":$((CRASH_TS - 86400000))},
  {\"stackTrace\":\"at android.app.Activity.performCreate\",\"message\":\"IllegalStateException\",\"platform\":\"android\",\"appVersion\":\"1.2.0\",\"deviceInfo\":\"Samsung S21\",\"breadcrumbs\":\"screen=payment;action=submit\",\"timestamp\":$((CRASH_TS - 172800000))},
  {\"stackTrace\":\"at ReactNativeRenderer.commitRoot\",\"message\":\"Invariant Violation\",\"platform\":\"web\",\"appVersion\":\"2.0.0\",\"timestamp\":$((CRASH_TS - 259200000))},
  {\"stackTrace\":\"at kotlin.Throwable.<init>\",\"message\":\"NetworkError\",\"platform\":\"android\",\"appVersion\":\"1.1.9\",\"deviceInfo\":\"OnePlus 9\",\"activeFlagKeys\":[\"promo_banner\"],\"timestamp\":$((CRASH_TS - 432000000))}
]" >/dev/null 2>&1 || true
echo "  ✓ Crash reports sent (10 total)"

# --- Data check: show what's in DB so you can see what might be missing ---
echo ""
echo "=== Data check (local analytics & crashlytics) ==="
START_7D=$(( (NOW_MS / 1000 - 7 * SEC_PER_DAY) * 1000 ))
END_MS=$(( (NOW_MS / 1000 + 60) * 1000 ))
if [ -n "$AUTH_HEADER" ]; then
  OVERVIEW=$(curl -s "$BACKEND_URL/api/v1/analytics/overview?start=$START_7D&end=$END_MS" -H "$AUTH_HEADER" 2>/dev/null || echo "{}")
  CRASH_RESP=$(curl -s "$BACKEND_URL/api/v1/crashes?limit=1" -H "$AUTH_HEADER" 2>/dev/null || echo "{\"total\":0}")
else
  OVERVIEW=$(curl -s "$BACKEND_URL/api/v1/analytics/overview?start=$START_7D&end=$END_MS" 2>/dev/null || echo "{}")
  CRASH_RESP=$(curl -s "$BACKEND_URL/api/v1/crashes?limit=1" 2>/dev/null || echo "{\"total\":0}")
fi
if command -v jq &>/dev/null; then
  EVENTS=$(echo "$OVERVIEW" | jq -r '.totalEvents // 0')
  USERS=$(echo "$OVERVIEW" | jq -r '.uniqueUsers // 0')
  CRASHES=$(echo "$CRASH_RESP" | jq -r '.total // 0')
  echo "  Analytics (last 7d): $EVENTS events, $USERS unique users"
  echo "  Crash reports:      $CRASHES total"
  if [ "${EVENTS:-0}" -eq 0 ]; then echo "  ⚠ No analytics events in range — check tenant / start/end or run without FLAGENT_API_KEY for OSS."; fi
  if [ "${CRASHES:-0}" -eq 0 ]; then echo "  ⚠ No crash reports — check POST /api/v1/crashes/batch and tenant."; fi
else
  echo "  (Install jq to see counts: brew install jq)"
fi
echo ""

echo ""
echo "=== Done ==="
echo ""
echo "Open the app and check:"
echo "  • Dashboard  – overview"
echo "  • Flags      – list and edit all flags, segments, variants"
echo "  • Analytics  – Overview (evaluation counts), Events, By flags"
echo "  • Metrics    – Crash dashboard (if enabled)"
echo ""
if [ -n "$API_KEY" ] && [ -z "$EXISTING_API_KEY" ]; then
  echo "API Key (save in Settings or localStorage):"
  echo "  $API_KEY"
  echo ""
  echo "Or run frontend with: ENV_API_KEY=$API_KEY"
fi
if [ -n "$EXISTING_API_KEY" ]; then
  echo "Data was added to the tenant for the provided FLAGENT_API_KEY."
fi
