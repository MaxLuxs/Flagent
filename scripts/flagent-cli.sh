#!/usr/bin/env bash
# Flagent CLI - GitOps sync, flags, and evaluation
# Requires: curl, jq (for JSON output and create/eval)
# Usage:
#   flagent export --url <api> [--output flags.yaml] [--api-key <key>]
#   flagent import --url <api> --file flags.yaml [--api-key <key>]
#   flagent sync   --url <api> --file flags.yaml [--api-key <key>]
#   flagent flags list --url <api> [--limit N] [--offset N] [--key KEY] [--output json] [--api-key <key>]
#   flagent flags create --key KEY --description "..." [--enabled] --url <api> [--api-key <key>]
#   flagent eval --flag-key KEY [--entity-id ID] [--entity-type TYPE] [--context JSON] --url <api> [--api-key <key>] [--output json]
#   flagent flag create --from-branch [branch] --url <api> [--api-key <key>]

set -e

API_URL=""
API_KEY=""
FILE=""
OUTPUT="flags.yaml"
FORMAT="yaml"
BRANCH=""
# flags list
LIMIT=""
OFFSET=""
KEY_FILTER=""
# flags create (--key and --description; --key stored in KEY_FILTER for reuse)
FLAG_DESC=""
FLAG_ENABLED=""
# eval
EVAL_FLAG_KEY=""
EVAL_ENTITY_ID=""
EVAL_ENTITY_TYPE=""
EVAL_CONTEXT=""

# Converts branch name to flag key: feature/foo -> feature_foo
branch_to_flag_key() {
    local b="${1:-}"
    b="${b#refs/heads/}"
    echo "$b" | sed 's/\//_/g' | sed 's/[^a-zA-Z0-9_.-]/_/g' | tr '[:upper:]' '[:lower:]' | sed 's/^ *$//' | sed 's/^$/unnamed/'
}

usage() {
    echo "Flagent CLI - GitOps sync, flags, and evaluation"
    echo ""
    echo "Usage:"
    echo "  flagent export --url <api> [--output flags.yaml] [--api-key <key>]"
    echo "  flagent import --url <api> --file <path> [--api-key <key>]"
    echo "  flagent sync   --url <api> --file <path> [--api-key <key>]"
    echo "  flagent flags list --url <api> [--limit N] [--offset N] [--key KEY] [--output json] [--api-key <key>]"
    echo "  flagent flags create --key KEY --description \"...\" [--enabled] --url <api> [--api-key <key>]"
    echo "  flagent eval --flag-key KEY [--entity-id ID] [--entity-type TYPE] [--context JSON] --url <api> [--api-key <key>] [--output json]"
    echo "  flagent flag create --from-branch [branch] --url <api> [--api-key <key>]"
    echo ""
    echo "Examples:"
    echo "  flagent export --url https://flagent.example.com --api-key sk-xxx"
    echo "  flagent flags list --url http://localhost:18000"
    echo "  flagent flags create --key my_flag --description \"My feature\" --url http://localhost:18000"
    echo "  flagent eval --flag-key my_flag --entity-id user1 --url http://localhost:18000"
    echo "  flagent flag create --from-branch feature/new-payment --url https://flagent.example.com --api-key sk-xxx"
    exit 1
}

# Normalize base URL (strip /api/v1 suffix)
base_url() {
    local u="${1%/}"
    u="${u%/api/v1}"
    u="${u%/api}"
    echo "$u"
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --url) API_URL="$2"; shift 2 ;;
            --file) FILE="$2"; shift 2 ;;
            --output) OUTPUT="$2"; shift 2 ;;
            --api-key) API_KEY="$2"; shift 2 ;;
            --from-branch) BRANCH="$2"; shift 2 ;;
            --limit) LIMIT="$2"; shift 2 ;;
            --offset) OFFSET="$2"; shift 2 ;;
            --key) KEY_FILTER="$2"; shift 2 ;;
            --description) FLAG_DESC="$2"; shift 2 ;;
            --enabled) FLAG_ENABLED="true"; shift 1 ;;
            --flag-key) EVAL_FLAG_KEY="$2"; shift 2 ;;
            --entity-id) EVAL_ENTITY_ID="$2"; shift 2 ;;
            --entity-type) EVAL_ENTITY_TYPE="$2"; shift 2 ;;
            --context) EVAL_CONTEXT="$2"; shift 2 ;;
            *) shift ;;
        esac
    done
}

# Shared: curl opts with optional X-API-Key. First arg = base URL.
curl_get() {
    local base="$1"
    local path="$2"
    CURL_OPTS=(-s -S)
    [[ -n "$API_KEY" ]] && CURL_OPTS+=(-H "X-API-Key: $API_KEY")
    curl "${CURL_OPTS[@]}" "$base$path"
}

curl_post() {
    local base="$1"
    local path="$2"
    local body="$3"
    CURL_OPTS=(-s -S -X POST -H "Content-Type: application/json")
    [[ -n "$API_KEY" ]] && CURL_OPTS+=(-H "X-API-Key: $API_KEY")
    curl "${CURL_OPTS[@]}" -d "$body" "$base$path"
}

create_flag_from_branch() {
    if [[ -z "$API_URL" ]]; then
        echo "Error: --url is required"
        usage
    fi
    local branch="${BRANCH:-$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo '')}"
    if [[ -z "$branch" ]]; then
        echo "Error: --from-branch or git branch required"
        usage
    fi
    local key
    key=$(branch_to_flag_key "$branch")
    BASE="${API_URL%/}"
    BASE="${BASE%/api/v1}"
    BASE="${BASE%/api}"
    local url="${BASE}/api/v1/flags"
    CURL_OPTS=(-s -S -X POST -H "Content-Type: application/json")
    [[ -n "$API_KEY" ]] && CURL_OPTS+=(-H "X-API-Key: $API_KEY")
    local body
    body=$(jq -n --arg key "$key" --arg desc "Auto from branch: $branch" '{key: $key, description: $desc}')
    echo "Creating flag key=$key from branch=$branch at $url ..."
    RESULT=$(curl "${CURL_OPTS[@]}" -d "$body" "$url")
    echo "$RESULT" | jq -r '"Created: \(.key) (id: \(.id))"' 2>/dev/null || echo "$RESULT"
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

flags_list() {
    if [[ -z "$API_URL" ]]; then
        echo "Error: --url is required"
        usage
    fi
    if ! command -v jq &>/dev/null; then
        echo "Error: jq is required for flags list. Install: brew install jq"
        exit 1
    fi
    BASE=$(base_url "$API_URL")
    q=""
    [[ -n "$LIMIT" ]] && q="${q}limit=$LIMIT&"
    [[ -n "$OFFSET" ]] && q="${q}offset=$OFFSET&"
    [[ -n "$KEY_FILTER" ]] && q="${q}key=$(printf '%s' "$KEY_FILTER" | jq -sRr @uri)&"
    path="/api/v1/flags"
    [[ -n "$q" ]] && path="/api/v1/flags?${q%&}"
    RESULT=$(curl_get "$BASE" "$path")
    if [[ "$OUTPUT" == "json" ]]; then
        echo "$RESULT" | jq '.'
        return
    fi
    echo "$RESULT" | jq -r '.[]? | "\(.id)\t\(.key)\t\(.enabled)\t\(.description // "")"' 2>/dev/null | while IFS=$'\t' read -r id key enabled desc; do
        echo "id=$id  key=$key  enabled=$enabled  $desc"
    done
    if ! echo "$RESULT" | jq -e '. | length' &>/dev/null; then
        echo "$RESULT"
    fi
}

flags_create() {
    if [[ -z "$API_URL" ]] || [[ -z "$KEY_FILTER" ]]; then
        echo "Error: --url and --key are required"
        usage
    fi
    if ! command -v jq &>/dev/null; then
        echo "Error: jq is required for flags create. Install: brew install jq"
        exit 1
    fi
    BASE=$(base_url "$API_URL")
    body=$(jq -n --arg key "$KEY_FILTER" --arg desc "${FLAG_DESC:-}" --arg en "${FLAG_ENABLED:-}" '{key: $key, description: $desc, enabled: ($en == "true")}')
    RESULT=$(curl_post "$BASE" "/api/v1/flags" "$body")
    echo "$RESULT" | jq -r '"Created: \(.key) (id: \(.id))"' 2>/dev/null || echo "$RESULT"
}

do_eval() {
    if [[ -z "$API_URL" ]] || [[ -z "$EVAL_FLAG_KEY" ]]; then
        echo "Error: --url and --flag-key are required"
        usage
    fi
    if ! command -v jq &>/dev/null; then
        echo "Error: jq is required for eval. Install: brew install jq"
        exit 1
    fi
    BASE=$(base_url "$API_URL")
    body=$(jq -n \
        --arg fk "$EVAL_FLAG_KEY" \
        --arg eid "${EVAL_ENTITY_ID:-user1}" \
        --arg ety "${EVAL_ENTITY_TYPE:-user}" \
        --argjson ctx "${EVAL_CONTEXT:-{}}" \
        '{flagKey: $fk, entityID: $eid, entityType: $ety, entityContext: $ctx}')
    RESULT=$(curl_post "$BASE" "/api/v1/evaluation" "$body")
    if [[ "$OUTPUT" == "json" ]]; then
        echo "$RESULT" | jq '.'
        return
    fi
    echo "$RESULT" | jq -r '"enabled: \(.enabled // false)\nvariant: \(.variantKey // "null")\nreason: \(.reason // "n/a")"' 2>/dev/null || echo "$RESULT"
}

case "${1:-}" in
    export) shift; parse_args "$@"; export_flags ;;
    import) shift; parse_args "$@"; import_flags ;;
    sync) shift; parse_args "$@"; import_flags ;;
    flags)
        shift
        case "${1:-}" in
            list) shift; parse_args "$@"; flags_list ;;
            create) shift; parse_args "$@"; flags_create ;;
            *) usage ;;
        esac
        ;;
    eval) shift; parse_args "$@"; do_eval ;;
    flag)
        shift
        case "${1:-}" in
            create) shift; parse_args "$@"; create_flag_from_branch ;;
            *) usage ;;
        esac
        ;;
    *) usage ;;
esac
