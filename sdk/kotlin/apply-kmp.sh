#!/bin/bash

# Copies generated KMP client from build/generated-kmp into src/commonMain,
# keeps our expect/actual Engine.kt and patches ApiClient to use createDefaultHttpClientEngine().

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/build/generated-kmp"
GEN_SRC="$OUTPUT_DIR/src/commonMain/kotlin/com/flagent/client"
COMMON_SRC="$SCRIPT_DIR/src/commonMain/kotlin/com/flagent/client"

if [ ! -d "$GEN_SRC" ]; then
    echo "Error: Generated source not found at $GEN_SRC. Run generate.sh first."
    exit 1
fi

# Backup our Engine.kt (expect + actuals live in platform sources; we only overwrite commonMain)
ENGINE_KT="$COMMON_SRC/infrastructure/Engine.kt"
OUR_ENGINE='package com.flagent.client.infrastructure

import io.ktor.client.engine.HttpClientEngine

/**
 * Platform-specific default HTTP client engine.
 * Used when no engine is passed to ApiClient (e.g. CIO on JVM, Darwin on iOS).
 */
expect fun createDefaultHttpClientEngine(): HttpClientEngine
'

# Copy generated commonMain: apis, models, auth entirely; infrastructure file-by-file so we don't remove our custom files (e.g. InstantEpochMillisecondsSerializer.kt, Engine.kt)
rm -rf "$COMMON_SRC/apis" "$COMMON_SRC/models" "$COMMON_SRC/auth"
cp -R "$GEN_SRC/apis" "$GEN_SRC/models" "$GEN_SRC/auth" "$COMMON_SRC/"
for f in "$GEN_SRC"/infrastructure/*.kt; do [ -f "$f" ] && cp "$f" "$COMMON_SRC/infrastructure/"; done

# Restore our Engine.kt so we keep expect/actual wiring
echo "$OUR_ENGINE" > "$ENGINE_KT"

# Fix duplicate @Serializable (generator bug)
find "$COMMON_SRC" -name "*.kt" -exec sed -i.bak 's/@Serializable@Serializable/@Serializable/g' {} \;
find "$COMMON_SRC" -name "*.kt.bak" -delete

# Fix Map<String, Any> → JsonObject (kotlinx.serialization has no serializer for Any)
for f in "$COMMON_SRC"/models/*.kt "$COMMON_SRC"/apis/ExportApi.kt; do
  [ -f "$f" ] || continue
  sed -i.bak 's/kotlin\.collections\.Map<kotlin\.String, kotlin\.Any>/kotlinx.serialization.json.JsonObject/g' "$f"
  sed -i.bak 's/Map<kotlin\.String, kotlin\.Any>/kotlinx.serialization.json.JsonObject/g' "$f"
  # ExportApi: fix KSerializer and serializer() call for JsonObject
  sed -i.bak 's/serializer<Map<String, kotlin\.Any>>()/kotlinx.serialization.json.JsonObject.serializer()/g' "$f"
  # Add import if we have JsonObject and no such import yet
  if grep -q 'JsonObject' "$f" && ! grep -q 'import kotlinx.serialization.json.JsonObject' "$f"; then
    sed -i.bak '/^package /a\
\
import kotlinx.serialization.json.JsonObject
' "$f"
  fi
  rm -f "$f.bak"
done

# Patch ApiClient: use createDefaultHttpClientEngine() when engine is null.
# Generated: client = httpClientEngine?.let { HttpClient(it, clientConfig) } ?: HttpClient(clientConfig)
# We want:    client = HttpClient((httpClientEngine ?: createDefaultHttpClientEngine()), clientConfig)
API_CLIENT_KT="$COMMON_SRC/infrastructure/ApiClient.kt"
if [ -f "$API_CLIENT_KT" ]; then
    if grep -q 'createDefaultHttpClientEngine' "$API_CLIENT_KT"; then
        echo "ApiClient.kt already uses createDefaultHttpClientEngine, skip patch."
    else
        perl -i.bak -0pe 's/httpClientEngine\?\.let\s*\{\s*HttpClient\(it,\s*clientConfig\)\s*\}\s*\?\s*:\s*HttpClient\(clientConfig\)/HttpClient((httpClientEngine ?: createDefaultHttpClientEngine()), clientConfig)/gs' "$API_CLIENT_KT"
    fi
    # Убираем лишние safe call (authentications не nullable)
    sed -i.bak 's/authentications?.values/authentications.values/g' "$API_CLIENT_KT"
    sed -i.bak 's/authentications\.values?.firstOrNull/authentications.values.firstOrNull/g' "$API_CLIENT_KT"
    sed -i.bak 's/authentications?.get(/authentications.get(/g' "$API_CLIENT_KT"
    rm -f "$API_CLIENT_KT.bak"
fi

echo "Applied KMP sources; Engine.kt and ApiClient engine wiring kept."
