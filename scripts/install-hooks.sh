#!/usr/bin/env bash
# Install git hooks (e.g. pre-commit for VERSION sync check).
# Usage: ./scripts/install-hooks.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOOKS_SRC="$ROOT/scripts/githooks"
HOOKS_DEST="$ROOT/.git/hooks"
mkdir -p "$HOOKS_DEST"
for h in pre-commit; do
  if [ -f "$HOOKS_SRC/$h" ]; then
    cp "$HOOKS_SRC/$h" "$HOOKS_DEST/$h"
    chmod +x "$HOOKS_DEST/$h"
    echo "Installed .git/hooks/$h"
  fi
done
