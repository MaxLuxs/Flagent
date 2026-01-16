#!/bin/bash

# Generate JavaScript/TypeScript SDK from OpenAPI specification
# Requires OpenAPI Generator: https://openapi-generator.tech/

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
OPENAPI_SPEC="$PROJECT_ROOT/docs/api/openapi.yaml"
OUTPUT_DIR="$SCRIPT_DIR"

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

echo "Generating JavaScript/TypeScript SDK from $OPENAPI_SPEC..."

# Use npx if openapi-generator-cli is not installed globally
GENERATOR_CMD="openapi-generator-cli"
if ! command -v openapi-generator-cli &> /dev/null; then
    GENERATOR_CMD="npx --yes @openapitools/openapi-generator-cli"
fi

# Generate TypeScript-Axios client
$GENERATOR_CMD generate \
    -i "$OPENAPI_SPEC" \
    -g typescript-axios \
    -o "$OUTPUT_DIR" \
    --additional-properties=npmName=@flagent/client,npmVersion=1.0.0,withInterfaces=true,withSeparateModelsAndApi=true,modelPackage=models,apiPackage=api,sourceFolder=src

echo "JavaScript/TypeScript SDK generated successfully in $OUTPUT_DIR"
