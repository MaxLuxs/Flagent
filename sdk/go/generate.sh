#!/bin/bash

# Generate Go SDK from OpenAPI specification
# Requires OpenAPI Generator: https://openapi-generator.tech/

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OPENAPI_SPEC="$PROJECT_ROOT/docs/api/openapi.yaml"
OUTPUT_DIR="$SCRIPT_DIR/api"

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

echo "Generating Go SDK from $OPENAPI_SPEC..."

# Use npx if openapi-generator-cli is not installed globally
GENERATOR_CMD="openapi-generator-cli"
if ! command -v openapi-generator-cli &> /dev/null; then
    GENERATOR_CMD="npx --yes @openapitools/openapi-generator-cli"
fi

# Generate into api/ subpackage (parent go.mod is used)
$GENERATOR_CMD generate \
    -i "$OPENAPI_SPEC" \
    -g go \
    -o "$OUTPUT_DIR" \
    --additional-properties=packageName=api,withGoMod=false,isGoSubmodule=true,packageVersion=0.1.5

# Fix: OpenAPI Generator uses undefined FLAG_TAGS_OPERATOR type for inline enum
for f in "$OUTPUT_DIR/model_eval_context.go" "$OUTPUT_DIR/model_evaluation_batch_request.go"; do
    if [[ -f "$f" ]]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' 's/var flagTagsOperator FLAG_TAGS_OPERATOR = "ANY"/var flagTagsOperator string = "ANY"/g' "$f"
        else
            sed -i 's/var flagTagsOperator FLAG_TAGS_OPERATOR = "ANY"/var flagTagsOperator string = "ANY"/g' "$f"
        fi
    fi
done

# Fix: test files use GIT_USER_ID/GIT_REPO_ID - replace with actual module path
for f in "$OUTPUT_DIR/test/"*.go; do
    if [[ -f "$f" ]]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' 's|github.com/GIT_USER_ID/GIT_REPO_ID/api|github.com/MaxLuxs/Flagent/sdk/go/api|g' "$f"
        else
            sed -i 's|github.com/GIT_USER_ID/GIT_REPO_ID/api|github.com/MaxLuxs/Flagent/sdk/go/api|g' "$f"
        fi
    fi
done

echo "Go SDK generated successfully in $OUTPUT_DIR"
