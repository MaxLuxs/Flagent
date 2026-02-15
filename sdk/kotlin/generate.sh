#!/bin/bash

# Generate Kotlin Multiplatform SDK from OpenAPI specification.
# Uses OpenAPI Generator with library=multiplatform (Ktor + kotlinx.serialization).
# Output is written to build/generated-kmp, then apply-kmp.sh copies into src/commonMain
# and patches ApiClient to use our expect/actual createDefaultHttpClientEngine().

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OPENAPI_SPEC="$PROJECT_ROOT/docs/api/openapi.yaml"
OUTPUT_DIR="$SCRIPT_DIR/build/generated-kmp"
COMMON_SRC="$SCRIPT_DIR/src/commonMain/kotlin/com/flagent/client"

# Check if OpenAPI Generator is installed (try npx if not installed globally)
if ! command -v openapi-generator-cli &> /dev/null && ! command -v npx &> /dev/null; then
    echo "Error: openapi-generator-cli is not installed and npx is not available"
    echo "Install it with: npm install -g @openapitools/openapi-generator-cli"
    exit 1
fi

# Check if OpenAPI spec exists
if [ ! -f "$OPENAPI_SPEC" ]; then
    echo "Error: OpenAPI specification not found at $OPENAPI_SPEC"
    exit 1
fi

echo "Generating Kotlin KMP client from $OPENAPI_SPEC into $OUTPUT_DIR..."

# Use npx if openapi-generator-cli is not installed globally
GENERATOR_CMD="openapi-generator-cli"
if ! command -v openapi-generator-cli &> /dev/null; then
    GENERATOR_CMD="npx --yes @openapitools/openapi-generator-cli"
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# library=multiplatform → src/commonMain/kotlin, Ktor client, kotlinx.serialization
# mapFileBinaryToByteArray=true → binary/File responses become ByteArray (KMP-friendly)
$GENERATOR_CMD generate \
    -i "$OPENAPI_SPEC" \
    -g kotlin \
    -o "$OUTPUT_DIR" \
    --additional-properties=library=multiplatform,packageName=com.flagent.client,groupId=com.flagent,artifactId=flagent-kotlin-client,artifactVersion=1.0.0,serializationLibrary=kotlinx_serialization,dateLibrary=kotlinx-datetime,sourceFolder=src/commonMain/kotlin,mapFileBinaryToByteArray=true

echo "Generated raw KMP client in $OUTPUT_DIR"
echo "Applying into $COMMON_SRC (keeping Engine.kt, patching ApiClient)..."
"$SCRIPT_DIR/apply-kmp.sh"
echo "Kotlin KMP SDK generated and applied successfully."
