#!/bin/bash
# Safe setup: push enterprise to Flagent-Enterprise, then replace folder with submodule in main repo.
# Run from repo root: flagent/

set -e

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ENTERPRISE_DIR="$REPO_ROOT/internal/flagent-enterprise"
REMOTE_URL="https://github.com/MaxLuxs/Flagent-Enterprise.git"

echo "=== 1. Push enterprise code to Flagent-Enterprise (requires auth) ==="
if [ ! -d /tmp/flagent-enterprise-push/.git ]; then
  echo "Temp repo not found. Creating..."
  cp -R "$ENTERPRISE_DIR" /tmp/flagent-enterprise-push
  cd /tmp/flagent-enterprise-push
  [ -f .gitignore ] || echo -e "build/\n.gradle/\n*.iml\n.idea/\nout/\n.DS_Store" > .gitignore
  git init
  git add .
  git commit -m "Initial enterprise module: entities, tables, EnterprisePlugin (migrations + configureRoutes stub)"
  git remote add origin "$REMOTE_URL"
  git branch -M main
fi
cd /tmp/flagent-enterprise-push
echo "Pushing to $REMOTE_URL ..."
git push -u origin main
echo "Done. Enterprise code is on GitHub."

echo ""
echo "=== 2. Replace folder with submodule in main repo ==="
cd "$REPO_ROOT"
git rm -r --cached internal/flagent-enterprise 2>/dev/null || true
rm -rf internal/flagent-enterprise
git submodule add "$REMOTE_URL" internal/flagent-enterprise
echo "Submodule added. Commit with: git add .gitmodules internal/flagent-enterprise && git commit -m 'chore: Flagent-Enterprise as submodule'"
