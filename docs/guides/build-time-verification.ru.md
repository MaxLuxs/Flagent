# Build-Time Verification

> [English](build-time-verification.md) | [Русский](build-time-verification.ru.md)

Gradle-плагин проверяет, что ключи флагов, используемые в коде, существуют в Flagent (через API или локальный файл).

## Установка

В **settings.gradle.kts** корня проекта (если плагин в том же репо):

```kotlin
includeBuild("gradle-plugins/flagent-gradle-plugin")
```

Или опубликуйте плагин в Maven и подключите:

```kotlin
plugins {
    id("com.flagent.verify-flags") version "0.1.0"
}
```

## Конфигурация

```kotlin
plugins { id("com.flagent.verify-flags") }

flagent {
    baseUrl = "https://api.flagent.example.com"
    apiKey = providers.environmentVariable("FLAGENT_API_KEY").getOrElse("")
    flagsFile = file("flags.yaml")  // альтернатива API
    failOnUnknown = true  // fail build если ключ не найден
}
```

## Сканирование

Плагин ищет в `src/**/*.kt` и `src/**/*.java`:

- `@FlagKey("key")` — аннотация
- `isEnabled("key")` — вызов
- `evaluate("key", ...)` — вызов

## Запуск

```bash
./gradlew verifyFlags
```

## Локальный файл вместо API

Если `flagsFile` задан, плагин читает ключи из YAML/JSON:

```yaml
flags:
  - key: new_checkout
    description: "..."
  - key: dark_mode
```
