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
for f in docs/api/openapi.yaml sdk/java/api/openapi.yaml sdk/go/api/api/openapi.yaml backend/src/main/resources/openapi/documentation.yaml; do
  if [ -f "$f" ]; then
    sed -i '' "s/^  version: .*/  version: $VERSION/" "$f" 2>/dev/null || true
    sed -i '' "s/version: \"[^\"]*\"/version: \"$VERSION\"/" "$f" 2>/dev/null || true
    sed -i '' "s/version: [0-9][0-9.]*$/version: $VERSION/" "$f" 2>/dev/null || true
  fi
done

# Swagger UI static bundles (version in header and Info example)
for f in docs/api-docs.html backend/docs/index.html; do
  if [ -f "$f" ]; then
    sed -i '' "s/Version: [0-9][0-9.]*/Version: $VERSION/" "$f"
    sed -i '' "s/\"version\" : \"[0-9][0-9.]*\"/\"version\" : \"$VERSION\"/" "$f"
  fi
done

# Flutter/Dart pubspec
for f in sdk/flutter-enhanced/pubspec.yaml sdk/dart/pubspec.yaml samples/flutter/pubspec.yaml; do
  if [ -f "$f" ]; then sed -i '' "s/^version: .*/version: $VERSION/" "$f"; fi
done

# docs/sdk.html (Flutter example)
if [ -f docs/sdk.html ]; then
  sed -i '' "s/flagent_client: \^[0-9][0-9.]*/flagent_client: ^$VERSION/" docs/sdk.html
fi

# sdk/java/build.gradle (standalone Maven/Gradle build)
if [ -f sdk/java/build.gradle ]; then
  sed -i '' "s/version = '[0-9][0-9.]*'/version = '$VERSION'/" sdk/java/build.gradle
fi

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

# Frontend navbar and shell layout (fallback version when API unreachable)
for f in frontend/src/jsMain/kotlin/flagent/frontend/components/Navbar.kt frontend/src/jsMain/kotlin/flagent/frontend/components/ShellLayout.kt; do
  if [ -f "$f" ]; then
    sed -i '' "s/Text(\"v[^\"]*\")/Text(\"v$VERSION\")/" "$f" 2>/dev/null || true
    sed -i '' "s/mutableStateOf(\"v[^\"]*\")/mutableStateOf(\"v$VERSION\")/" "$f" 2>/dev/null || true
  fi
done

# Go
if [ -f sdk/go/client.go ]; then sed -i '' "s/defaultUserAgent = \"flagent-go-client\/[^\"]*\"/defaultUserAgent = \"flagent-go-client\/$VERSION\"/" sdk/go/client.go; fi

# Kotlin SDK fallback dependency versions (when not building from source)
for f in sdk/kotlin-enhanced/build.gradle.kts sdk/kotlin-debug-ui/build.gradle.kts; do
  if [ -f "$f" ]; then
    sed -i '' "s/com\.flagent:kotlin-client:[0-9][^\"]*/com.flagent:kotlin-client:$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/com\.flagent:kotlin-enhanced:[0-9][^\"]*/com.flagent:kotlin-enhanced:$VERSION/g" "$f" 2>/dev/null || true
  fi
done

# Swift
if [ -f sdk/swift/project.yml ]; then sed -i '' "s/version: [0-9.]*/version: $VERSION/" sdk/swift/project.yml; fi

# sdk/ANDROID_IOS.md OpenAPI generator artifactVersion
if [ -f sdk/ANDROID_IOS.md ]; then sed -i '' "s/artifactVersion=[0-9][0-9.]*/artifactVersion=$VERSION/" sdk/ANDROID_IOS.md; fi

if [ -f sdk/swift/FlagentClient.podspec ]; then
  sed -i '' "s/s.version = '[^']*'/s.version = '$VERSION'/" sdk/swift/FlagentClient.podspec
  sed -i '' "s/:tag => 'v[^']*'/:tag => 'v$VERSION'/" sdk/swift/FlagentClient.podspec
fi

# Docs and README: com.flagent:artifact:VERSION, backend-X-all.jar, from: "VERSION", @flagent/client@X
for f in README.md README.ru.md docs/guides/getting-started.md docs/guides/getting-started.ru.md docs/guides/deployment.md docs/guides/deployment.ru.md; do
  if [ -f "$f" ]; then
    sed -i '' "s/\(com\.flagent:[a-z-]*:\)[0-9][0-9.]*/\1$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/backend-[0-9][0-9.]*-all\.jar/backend-$VERSION-all.jar/g" "$f" 2>/dev/null || true
    sed -i '' "s/from: \"[0-9][0-9.]*\"/from: \"$VERSION\"/" "$f" 2>/dev/null || true
    sed -i '' "s/@flagent\/client@[0-9][0-9.]*/@flagent\/client@$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/(current: [0-9][0-9.]*)/(current: $VERSION)/" "$f" 2>/dev/null || true
  fi
done

# SDK READMEs (including flagent-koin, dart, flutter-enhanced, samples)
for f in sdk/kotlin/README.md sdk/kotlin-enhanced/README.md sdk/kotlin-debug-ui/README.md sdk/javascript/README.md sdk/javascript/README.ru.md sdk/swift/README.md sdk/swift-enhanced/README.md sdk/swift-debug-ui/README.md sdk/java/README.md sdk/spring-boot-starter/README.md sdk/flagent-koin/README.md sdk/dart/README.md sdk/flutter-enhanced/README.md sdk/go/api/README.md sdk/ANDROID_IOS.md sdk/README.md ktor-flagent/README.md samples/flutter/README.md; do
  if [ -f "$f" ]; then
    sed -i '' "s/\(com\.flagent:[a-z-]*:\)[0-9][0-9.]*/\1$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/<version>[0-9][0-9.]*<\/version>/<version>$VERSION<\/version>/" "$f" 2>/dev/null || true
    sed -i '' "s/from: \"[0-9][0-9.]*\"/from: \"$VERSION\"/" "$f" 2>/dev/null || true
    sed -i '' "s/@flagent\/client@[0-9][0-9.]*/@flagent\/client@$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/API version: [0-9][0-9.]*/API version: $VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/API version [0-9][0-9.]*\.x/API version ${VERSION%.*}.x/g" "$f" 2>/dev/null || true
    sed -i '' "s/Package version: [0-9][0-9.]*/Package version: $VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/flagent_client: [0-9][0-9.]*/flagent_client: $VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/flagent_client: \^[0-9][0-9.]*/flagent_client: ^$VERSION/g" "$f" 2>/dev/null || true
    sed -i '' "s/flagent_enhanced: \^[0-9][0-9.]*/flagent_enhanced: ^$VERSION/g" "$f" 2>/dev/null || true
  fi
done

# Backend MCP plugin version
if [ -f backend/src/main/kotlin/flagent/mcp/FlagentMcpPlugin.kt ]; then
  sed -i '' "s/version = \"[0-9][0-9.]*\"/version = \"$VERSION\"/" backend/src/main/kotlin/flagent/mcp/FlagentMcpPlugin.kt
fi

# docs/index.html (Swagger UI version display)
if [ -f docs/index.html ]; then
  sed -i '' "s/\"version\" : \"[0-9][0-9.]*\"/\"version\" : \"$VERSION\"/" docs/index.html
  sed -i '' "s/Version: [0-9][0-9.]*/Version: $VERSION/" docs/index.html
fi

# Samples
if [ -f samples/react-native/package.json ]; then sed -i '' "s/\"version\": \"[0-9][0-9.]*\"/\"version\": \"$VERSION\"/" samples/react-native/package.json; fi

# SDK generate.sh scripts (packageVersion/pubVersion for next codegen)
if [ -f sdk/go/generate.sh ]; then sed -i '' "s/packageVersion=[0-9][0-9.]*/packageVersion=$VERSION/" sdk/go/generate.sh; fi
if [ -f sdk/dart/generate.sh ]; then sed -i '' "s/pubVersion=[0-9][0-9.]*/pubVersion=$VERSION/" sdk/dart/generate.sh; fi
if [ -f sdk/python/generate.sh ]; then sed -i '' "s/packageVersion=[0-9][0-9.]*/packageVersion=$VERSION/" sdk/python/generate.sh; fi

# JS test expectation (HealthApi.test.ts)
if [ -f sdk/javascript/__tests__/HealthApi.test.ts ]; then
  sed -i '' "s/version: '[0-9][0-9.]*'/version: '$VERSION'/" sdk/javascript/__tests__/HealthApi.test.ts
  sed -i '' "s/\.toBe('[0-9][0-9.]*')/\.toBe('$VERSION')/" sdk/javascript/__tests__/HealthApi.test.ts
fi

# docs/script.js (i18n strings with API version)
if [ -f docs/script.js ]; then
  sed -i '' "s/API version [0-9][0-9.]*\.x/API version ${VERSION%.*}.x/g" docs/script.js
  sed -i '' "s/версии [0-9][0-9.]*\.x/версии ${VERSION%.*}.x/g" docs/script.js
fi

echo "Synced version $VERSION to package.json, setup.py, Chart.yaml, OpenAPI, Java, backend, frontend, Go, Swift, docs, SDK READMEs. Gradle uses root VERSION file."
