#!/usr/bin/env bash
# Flagent CLI - GitOps sync for feature flags
# Usage:
#   flagent export --url <api> [--output flags.yaml] [--api-key <key>]
#   flagent import --url <api> --file flags.yaml [--api-key <key>]
#   flagent sync --url <api> --file flags.yaml [--api-key <key>]  (alias for import)

set -e

API_URL=""
API_KEY=""
FILE=""
OUTPUT="flags.yaml"
FORMAT="yaml"

usage() {
    echo "Flagent CLI - GitOps sync"
    echo ""
    echo "Usage:"
    echo "  flagent export --url <api> [--output flags.yaml] [--api-key <key>]"
    echo "  flagent import --url <api> --file <path> [--api-key <key>]"
    echo "  flagent sync   --url <api> --file <path> [--api-key <key>]"
    echo ""
    echo "Examples:"
    echo "  flagent export --url https://flagent.example.com --api-key sk-xxx"
    echo "  flagent import --url https://flagent.example.com --file flags.yaml --api-key sk-xxx"
    exit 1
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --url) API_URL="$2"; shift 2 ;;
            --file) FILE="$2"; shift 2 ;;
            --output) OUTPUT="$2"; shift 2 ;;
            --api-key) API_KEY="$2"; shift 2 ;;
            *) shift ;;
        esac
    done
}

export_flags() {
    if [[ -z "$API_URL" ]]; then
        echo "Error: --url is required"
        usage
    fi
    BASE="${API_URL%/}"
    CURL_OPTS=(-s -S)
    [[ -n "$API_KEY" ]] && CURL_OPTS+=(-H "X-API-Key: $API_KEY")
    if [[ "$OUTPUT" == *.yaml ]] || [[ "$OUTPUT" == *.yml ]]; then
        echo "Exporting from $BASE/api/v1/export/gitops?format=yaml ..."
        curl "${CURL_OPTS[@]}" "$BASE/api/v1/export/gitops?format=yaml" -o "$OUTPUT"
    else
        echo "Exporting from $BASE/api/v1/export/gitops?format=json ..."
        curl "${CURL_OPTS[@]}" "$BASE/api/v1/export/gitops?format=json" -o "$OUTPUT"
    fi
    echo "Exported to $OUTPUT"
}

import_flags() {
    if [[ -z "$API_URL" ]] || [[ -z "$FILE" ]]; then
        echo "Error: --url and --file are required"
        usage
    fi
    [[ -f "$FILE" ]] || { echo "Error: File not found: $FILE"; exit 1; }
    BASE="${API_URL%/}"
    CURL_OPTS=(-s -S -X POST -H "Content-Type: application/json")
    [[ -n "$API_KEY" ]] && CURL_OPTS+=(-H "X-API-Key: $API_KEY")
    CONTENT=$(cat "$FILE")
    if [[ "$FILE" == *.json ]]; then
        BODY=$(jq -n --arg content "$CONTENT" '{format: "json", content: $content}')
    else
        BODY=$(jq -n --arg content "$CONTENT" '{format: "yaml", content: $content}')
    fi
    echo "Importing from $FILE to $BASE/api/v1/import ..."
    RESULT=$(curl "${CURL_OPTS[@]}" -d "$BODY" "$BASE/api/v1/import")
    echo "$RESULT" | jq -r '"Created: \(.created), Updated: \(.updated)"' 2>/dev/null || echo "$RESULT"
    ERRORS=$(echo "$RESULT" | jq -r '.errors[]?' 2>/dev/null)
    [[ -n "$ERRORS" ]] && echo "Errors: $ERRORS"
}

case "${1:-}" in
    export) shift; parse_args "$@"; export_flags ;;
    import) shift; parse_args "$@"; import_flags ;;
    sync) shift; parse_args "$@"; import_flags ;;
    *) usage ;;
esac
