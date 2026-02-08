# Build-Time Verification

> [English](build-time-verification.md) | [Русский](build-time-verification.ru.md)

Gradle plugin verifies that flag keys used in code exist in Flagent (via API or local file).

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

## Configuration

```kotlin
plugins { id("com.flagent.verify-flags") }

flagent {
    baseUrl = "https://api.flagent.example.com"
    apiKey = providers.environmentVariable("FLAGENT_API_KEY").getOrElse("")
    flagsFile = file("flags.yaml")  // alternative to API
    failOnUnknown = true  // fail build if key not found
}
```

## Scanning

Plugin scans `src/**/*.kt` and `src/**/*.java` for:

- `@FlagKey("key")` — annotation
- `isEnabled("key")` — call
- `evaluate("key", ...)` — call

## Run

```bash
./gradlew verifyFlags
```

## Local file instead of API

If `flagsFile` is set, plugin reads keys from YAML/JSON:

```yaml
flags:
  - key: new_checkout
    description: "..."
  - key: dark_mode
```
