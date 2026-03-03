# Kotlin SDK (base, generated)

Это base Kotlin/JVM SDK для Flagent API, **сгенерированное из OpenAPI**.

Если вам нужен более удобный API (кэш, offline-first, менеджер) — используйте `kotlin-enhanced/`.

## Требования

- Kotlin: 2.2.20
- Gradle: 8.14

## Сборка

В директории SDK:

```bash
gradle wrapper
./gradlew check assemble
```

## Документация

Сгенерированная документация по endpoints и моделям находится в `docs/`.

Полная (англ.) документация генератора — в [`README.md`](./README.md).

## Генерация заново

SDK генерируется из OpenAPI спецификации. Обычно это делается скриптом `generate.sh` в этой директории.

