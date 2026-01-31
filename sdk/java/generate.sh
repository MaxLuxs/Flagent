#!/bin/bash

# Generate Java SDK from OpenAPI specification
# Requires OpenAPI Generator: https://openapi-generator.tech/

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OPENAPI_SPEC="$PROJECT_ROOT/docs/api/openapi.yaml"
OUTPUT_DIR="$SCRIPT_DIR"

if ! command -v openapi-generator-cli &> /dev/null && ! command -v npx &> /dev/null; then
    echo "Error: openapi-generator-cli is not installed and npx is not available"
    echo "Install it with: npm install -g @openapitools/openapi-generator-cli"
    exit 1
fi

if [ ! -f "$OPENAPI_SPEC" ]; then
    echo "Error: OpenAPI specification not found at $OPENAPI_SPEC"
    exit 1
fi

echo "Generating Java SDK from $OPENAPI_SPEC..."

GENERATOR_CMD="openapi-generator-cli"
if ! command -v openapi-generator-cli &> /dev/null; then
    GENERATOR_CMD="npx --yes @openapitools/openapi-generator-cli"
fi

# Java 11+ native HttpClient, Jackson; Jakarta EE for Spring Boot 3
$GENERATOR_CMD generate \
    -i "$OPENAPI_SPEC" \
    -g java \
    -o "$OUTPUT_DIR" \
    --additional-properties=library=native,invokerPackage=com.flagent.client,apiPackage=com.flagent.client.api,modelPackage=com.flagent.client.model,groupId=com.flagent,artifactId=flagent-java-client,artifactVersion=1.0.0,sourceFolder=src/main/java,useJakartaEe=true,dateLibrary=java8,hideGenerationTimestamp=true

echo "Java SDK generated successfully in $OUTPUT_DIR"
