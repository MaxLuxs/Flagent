# Build-Time Verification

> [English](build-time-verification.md) | [Русский](build-time-verification.ru.md)

Gradle plugin verifies that flag keys used in code exist in Flagent (via API or local file). Optionally, strict mode allows only keys from generated constants or `@FlagKey` annotations.

## Installation

In root **settings.gradle.kts** (if plugin is in same repo):

```kotlin
includeBuild("gradle-plugins/flagent-gradle-plugin")
```

Or publish to Maven and use:

```kotlin
plugins {
    id("com.flagent.verify-flags") version "0.1.0"
}
```

## Scenario A: Verify only (keys must exist in Flagent)

Plugin scans `src/**/*.kt` and `src/**/*.java` for:

- `@FlagKey("key")` — annotation
- `isEnabled("key")` — call
- `evaluate("key", ...)` — call
- `evaluateBatch(..., listOf("key1", "key2"), ...)` — string literals in the list

All found keys are checked against Flagent (API or `flagsFile`). If a key is not found and `failOnUnknown = true`, the build fails.

### Configuration

```kotlin
plugins { id("com.flagent.verify-flags") }

flagent {
    baseUrl = "https://api.flagent.example.com"
    apiKey = providers.environmentVariable("FLAGENT_API_KEY").getOrElse("")
    flagsFile = file("flags.yaml")  // alternative to API
    failOnUnknown = true  // fail build if key not found
}
```

### Local file instead of API

If `flagsFile` is set, plugin reads keys from YAML/JSON:

```yaml
flags:
  - key: new_checkout
    description: "..."
  - key: dark_mode
```

### Run

```bash
./gradlew verifyFlags
```

---

## Scenario B: Strict mode (only FlagKeys.* or @FlagKey)

For teams that want to forbid raw string literals in `isEnabled("...")` / `evaluate("...")`, use:

1. **Artifact `flagent-annotations`** — declare keys in code with `@FlagKey`, and/or  
2. **Task `generateFlagKeys`** — generate `FlagKeys.kt` from API or `flagsFile`  
3. **Option `allowOnlyGeneratedOrAnnotated = true`** — build fails if any raw string is used

### Step 1: Annotations (optional)

Add dependency and declare keys:

```kotlin
// build.gradle.kts
dependencies {
    compileOnly("com.flagent:flagent-annotations:0.1.6")  // use project version
}
```

```kotlin
// src/main/kotlin/AppFlags.kt
package myapp

import com.flagent.annotations.FlagKey

object AppFlags {
    @FlagKey("new_checkout_flow")
    const val NEW_CHECKOUT_FLOW = "new_checkout_flow"

    @FlagKey("dark_mode")
    const val DARK_MODE = "dark_mode"
}
```

Then use `client.isEnabled(AppFlags.NEW_CHECKOUT_FLOW, entityId, context)`.

### Step 2: Generate FlagKeys.kt

Configure the plugin to generate an object or enum from Flagent (API or file):

```kotlin
plugins { id("com.flagent.verify-flags") }

flagent {
    baseUrl = "https://api.flagent.example.com"
    apiKey = providers.environmentVariable("FLAGENT_API_KEY").getOrElse("")
    flagsFile = file("flags.yaml")  // or use API when flagsFile is not set

    // Generation
    flagKeysOutputDir = layout.buildDirectory.dir("generated/sources/flagent/flagKeys")
    flagKeysFormat = "object"   // or "enum"
    flagKeysPackage = "myapp.flags"

    failOnUnknown = true
    allowOnlyGeneratedOrAnnotated = true  // strict mode
}
```

Add the generated directory to Kotlin source sets so the compiler sees `FlagKeys.kt`:

```kotlin
kotlin {
    sourceSets["main"].kotlin.srcDirs(layout.buildDirectory.dir("generated/sources/flagent/flagKeys"))
}
```

Run generation (runs automatically before `compileKotlin`):

```bash
./gradlew generateFlagKeys
```

Generated file example (`flagKeysFormat = "object"`):

```kotlin
package myapp.flags

object FlagKeys {
    const val DARK_MODE: String = "dark_mode"
    const val NEW_CHECKOUT_FLOW: String = "new_checkout_flow"
}
```

Use in code: `client.isEnabled(FlagKeys.NEW_CHECKOUT_FLOW, entityId, context)`.

### Step 3: Enable strict mode

Set `allowOnlyGeneratedOrAnnotated = true`. The build will fail if any raw string appears in `isEnabled("...")` or `evaluate("...")` (or in `evaluateBatch(..., listOf("..."), ...)`). Only keys from generated `FlagKeys` or from `@FlagKey` constants are allowed.

### Full example (Scenario B)

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm")
    id("com.flagent.verify-flags")
}

dependencies {
    compileOnly("com.flagent:flagent-annotations:0.1.6")
    implementation("com.flagent:kotlin-enhanced:0.1.6")
}

flagent {
    baseUrl = "https://api.flagent.example.com"
    apiKey = providers.environmentVariable("FLAGENT_API_KEY").getOrElse("")
    flagsFile = file("src/main/resources/flags.yaml")
    failOnUnknown = true
    flagKeysOutputDir = layout.buildDirectory.dir("generated/sources/flagent/flagKeys")
    flagKeysFormat = "object"
    flagKeysPackage = "myapp.flags"
    allowOnlyGeneratedOrAnnotated = true
}

kotlin {
    jvmToolchain(17)
    sourceSets["main"].kotlin.srcDirs(layout.buildDirectory.dir("generated/sources/flagent/flagKeys"))
}
```

```kotlin
// Usage: only FlagKeys.* or constants with @FlagKey
client.isEnabled(FlagKeys.NEW_CHECKOUT_FLOW, "user1", null)
```

---

## Reference: extension properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `baseUrl` | String | `http://localhost:18000` | Flagent API base URL |
| `apiKey` | String | `""` | X-API-Key header |
| `flagsFile` | File | (none) | YAML/JSON file with list of `{ key: "..." }` instead of API |
| `failOnUnknown` | Boolean | `true` | Fail build if code references a key not in Flagent |
| `flagKeysOutputDir` | Directory | `build/generated/sources/flagent/flagKeys` | Where to write generated `FlagKeys.kt` |
| `flagKeysFormat` | String | `"object"` | `"object"` or `"enum"` |
| `flagKeysPackage` | String | `"flagent.generated"` | Package of generated class |
| `allowOnlyGeneratedOrAnnotated` | Boolean | `false` | If true, raw strings in isEnabled/evaluate are forbidden |

## Tasks

- **verifyFlags** — scan sources, check keys against API/file; if strict mode, fail on raw strings.
- **generateFlagKeys** — generate `FlagKeys.kt` from API or `flagsFile`; runs before `compileKotlin` when plugin is applied.

## See also

- Full spec (strict mode) — see internal/private docs.
- [samples/kotlin](../../samples/kotlin) — Kotlin sample
- [samples/kotlin-strict-flags](../../samples/kotlin-strict-flags) — minimal project with generateFlagKeys and allowOnlyGeneratedOrAnnotated
