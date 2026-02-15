# Testing (samples & SDK)

See [Current status](status.md) for backend/frontend test commands and [Contributing](../guides/contributing.md) for full workflow.

## Sample and SDK tests

| Component        | Test command | Notes |
|------------------|--------------|--------|
| **Kotlin client (KMP)** | `./gradlew :kotlin-client:jvmTest` | API client; регенерация: `sdk/kotlin/generate.sh`, см. [CODEGEN](../../sdk/kotlin/CODEGEN.md). Отчёт покрытия: `:kotlin-client:jacocoTestReport` (HTML в `sdk/kotlin/build/reports/jacoco/jacocoTestReport/html/`). Проверка покрытия ≥85%: `./gradlew :kotlin-client:jacocoCoverageVerification`. |
| **Kotlin Debug UI** | `./gradlew :kotlin-debug-ui:jvmTest` | Compose Multiplatform (JVM/Android/iOS). |
| **Flutter App**     | `cd samples/flutter_app && flutter pub get && flutter test` | Requires Flutter SDK. |

## Quick commands

```bash
# Kotlin API client (KMP)
./gradlew :kotlin-client:jvmTest

# Kotlin Debug UI
./gradlew :kotlin-debug-ui:jvmTest

# Flutter App sample
cd samples/flutter_app && flutter pub get && flutter test
```
