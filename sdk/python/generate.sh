#!/bin/bash

# Generate Python SDK from OpenAPI specification
# Requires OpenAPI Generator: https://openapi-generator.tech/

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OPENAPI_SPEC="$PROJECT_ROOT/docs/api/openapi.yaml"
OUTPUT_DIR="$SCRIPT_DIR/src/flagent/_generated"
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

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

echo "Generating Python SDK from $OPENAPI_SPEC..."

# Use npx if openapi-generator-cli is not installed globally
GENERATOR_CMD="openapi-generator-cli"
if ! command -v openapi-generator-cli &> /dev/null; then
    GENERATOR_CMD="npx --yes @openapitools/openapi-generator-cli"
fi

# Generate into temp directory (generator overwrites entire output dir)
$GENERATOR_CMD generate \
    -i "$OPENAPI_SPEC" \
    -g python \
    -o "$TEMP_DIR" \
    --additional-properties=packageName=openapi_client,packageVersion=0.1.5,library=asyncio,generateSourceCodeOnly=true,projectName=flagent-generated

# Remove old generated code
rm -rf "$OUTPUT_DIR"

# Copy openapi_client contents into _generated
mkdir -p "$OUTPUT_DIR"
cp -r "$TEMP_DIR/openapi_client/"* "$OUTPUT_DIR/"

# Fix imports: openapi_client -> flagent._generated (so imports work from flagent package)
for f in $(find "$OUTPUT_DIR" -name "*.py"); do
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' \
            -e 's/from openapi_client\./from flagent._generated./g' \
            -e 's/from openapi_client import /from flagent._generated import /g' \
            -e 's/import openapi_client\.models$/from flagent._generated import models/g' \
            -e 's/getattr(openapi_client\.models,/getattr(models,/g' \
            "$f"
    else
        sed -i \
            -e 's/from openapi_client\./from flagent._generated./g' \
            -e 's/from openapi_client import /from flagent._generated import /g' \
            -e 's/import openapi_client\.models$/from flagent._generated import models/g' \
            -e 's/getattr(openapi_client\.models,/getattr(models,/g' \
            "$f"
    fi
done

echo "Python SDK generated successfully in $OUTPUT_DIR"
