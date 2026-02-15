# Testing (samples & SDK)

See [Current status](status.md) for backend/frontend test commands and [Contributing](../guides/contributing.md) for full workflow.

## Sample and SDK tests

| Component        | Test command | Notes |
|------------------|--------------|--------|
| **Kotlin Debug UI** | `./gradlew :kotlin-debug-ui:test` | Module must compile (Compose BOM / ExposedDropdownMenu). |
| **Flutter App**     | `cd samples/flutter_app && flutter pub get && flutter test` | Requires Flutter SDK. |

## Quick commands

```bash
# Kotlin Debug UI
./gradlew :kotlin-debug-ui:test

# Flutter App sample
cd samples/flutter_app && flutter pub get && flutter test
```
