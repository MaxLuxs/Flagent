#!/usr/bin/env bash
# Sync project version from root VERSION file to all build configs and docs.
# Single source of truth: VERSION (root). Run from repo root after changing VERSION.
# Usage: ./scripts/sync-version.sh
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ ! -f VERSION ]; then
  echo "Error: VERSION file not found in $ROOT" >&2
  exit 1
fi
VERSION=$(cat VERSION | tr -d '\n\r' | tr -d ' ')
test -n "$VERSION" || { echo "Error: VERSION file is empty" >&2; exit 1; }

# Gradle reads VERSION file in root build.gradle.kts. Subprojects inherit (no version= in their build files).

# package.json
for f in sdk/javascript/package.json sdk/javascript-enhanced/package.json sdk/javascript-debug-ui/package.json; do
  if [ -f "$f" ]; then sed -i '' "s/\"version\": \"[^\"]*\"/\"version\": \"$VERSION\"/" "$f"; fi
done

# package-lock.json: run npm install in sdk/javascript* after sync to refresh; script does not touch lockfiles

# Python
if [ -f sdk/python/setup.py ]; then sed -i '' "s/version=\"[^\"]*\"/version=\"$VERSION\"/" sdk/python/setup.py; fi
if [ -f sdk/python/src/flagent/__init__.py ]; then sed -i '' "s/__version__ = \"[^\"]*\"/__version__ = \"$VERSION\"/" sdk/python/src/flagent/__init__.py; fi

# Helm
if [ -f helm/flagent/Chart.yaml ]; then
  sed -i '' "s/^version: .*/version: $VERSION/" helm/flagent/Chart.yaml
  sed -i '' "s/^appVersion: .*/appVersion: \"$VERSION\"/" helm/flagent/Chart.yaml
fi

# OpenAPI (API version)
for f in docs/api/openapi.yaml sdk/java/api/openapi.yaml backend/src/main/resources/openapi/documentation.yaml; do
  if [ -f "$f" ]; then
    sed -i '' "s/^  version: .*/  version: $VERSION/" "$f" 2>/dev/null || true
    sed -i '' "s/version: \"[^\"]*\"/version: \"$VERSION\"/" "$f" 2>/dev/null || true
  fi
done

# sdk/java (pom project version is line 8)
if [ -f sdk/java/pom.xml ]; then sed -i '' "8s/<version>.*<\/version>/<version>$VERSION<\/version>/" sdk/java/pom.xml; fi
if [ -f sdk/java/src/main/java/com/flagent/client/Configuration.java ]; then
  sed -i '' "s/VERSION = \"[^\"]*\"/VERSION = \"$VERSION\"/" sdk/java/src/main/java/com/flagent/client/Configuration.java
  sed -i '' "s/The version of the OpenAPI document: [^ ]*/The version of the OpenAPI document: $VERSION/" sdk/java/src/main/java/com/flagent/client/Configuration.java
fi

# Backend default version (only the flagent.version default)
if [ -f backend/src/main/kotlin/flagent/route/InfoRoutes.kt ]; then
  sed -i '' "s/System.getProperty(\"flagent.version\") ?: \"[^\"]*\"/System.getProperty(\"flagent.version\") ?: \"$VERSION\"/" backend/src/main/kotlin/flagent/route/InfoRoutes.kt
fi

# Frontend navbar
if [ -f frontend/src/jsMain/kotlin/flagent/frontend/components/Navbar.kt ]; then
  sed -i '' "s/Text(\"v[^\"]*\")/Text(\"v$VERSION\")/" frontend/src/jsMain/kotlin/flagent/frontend/components/Navbar.kt
fi

# Go
if [ -f sdk/go/client.go ]; then sed -i '' "s/defaultUserAgent   = \"flagent-go-client\/[^\"]*\"/defaultUserAgent   = \"flagent-go-client\/$VERSION\"/" sdk/go/client.go; fi

# Kotlin SDK fallback dependency versions (when not building from source)
for f in sdk/kotlin-enhanced/build.gradle.kts sdk/kotlin-debug-ui/build.gradle.kts; do
  if [ -f "$f" ]; then
    sed -i '' "s/com\.flagent:flagent-kotlin-client:[0-9][^\"]*/com.flagent:flagent-kotlin-client:$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/com\.flagent:flagent-kotlin-enhanced-client:[0-9][^\"]*/com.flagent:flagent-kotlin-enhanced-client:$VERSION/g" "$f" 2>/dev/null || true
  fi
done

# Swift
if [ -f sdk/swift/project.yml ]; then sed -i '' "s/version: [0-9.]*/version: $VERSION/" sdk/swift/project.yml; fi
if [ -f sdk/swift/FlagentClient.podspec ]; then
  sed -i '' "s/s.version = '[^']*'/s.version = '$VERSION'/" sdk/swift/FlagentClient.podspec
  sed -i '' "s/:tag => 'v[^']*'/:tag => 'v$VERSION'/" sdk/swift/FlagentClient.podspec
fi

echo "Synced version $VERSION to package.json, setup.py, Chart.yaml, OpenAPI, Java, backend, frontend, Go, Swift. Gradle uses root VERSION file."
