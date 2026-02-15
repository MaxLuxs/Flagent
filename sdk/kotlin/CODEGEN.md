# Генерация Kotlin KMP клиента из OpenAPI

Клиент генерируется [OpenAPI Generator](https://openapi-generator.tech) с `library=multiplatform` (Ktor + kotlinx.serialization). Результат — общий код в `commonMain`, платформенный движок HTTP задаётся через expect/actual в модуле.

## Требования

- [OpenAPI Generator CLI](https://openapi-generator.tech/docs/installation): `npm install -g @openapitools/openapi-generator-cli` или через `npx`
- Спека: `docs/api/openapi.yaml`

## Команда

Из корня репозитория или из `sdk/kotlin`:

```bash
./sdk/kotlin/generate.sh
```

Скрипт:

1. Генерирует клиент в `sdk/kotlin/build/generated-kmp` (опции: `library=multiplatform`, `mapFileBinaryToByteArray=true`, `sourceFolder=src/commonMain/kotlin`).
2. Вызывает `apply-kmp.sh`, который:
   - копирует из `build/generated-kmp` в `src/commonMain/kotlin/com/flagent/client` только нужное: `apis/`, `models/`, `auth/`, по файлам из `infrastructure/` (чтобы не затирать наши файлы);
   - подменяет наш `Engine.kt` (expect `createDefaultHttpClientEngine()`);
   - убирает дубликаты `@Serializable` в сгенерированных моделях;
   - заменяет `Map<String, Any>` на `JsonObject` в моделях и в `ExportApi` (для совместимости с kotlinx.serialization);
   - патчит `ApiClient.kt`: при `engine == null` используется `createDefaultHttpClientEngine()`, убираются лишние safe call (`authentications?.` → `authentications.`).

## Что не перезаписывается

- `src/commonMain/.../infrastructure/Engine.kt` — expect для платформенного движка.
- Все `actual` в `jvmMain`, `androidMain`, `iosMain`, `jsMain`, `linuxX64Main`, `mingwX64Main`, `macosX64Main`.
- `InstantEpochMillisecondsSerializer.kt` — остаётся (при копировании infrastructure по файлам он не удаляется).

## Даты (Instant)

В сгенерированном коде используется **kotlin.time.Instant**. Сериализация по умолчанию — ISO-8601 строка. Бэкенд должен отдавать даты в формате ISO (например `"2024-01-01T00:00:00Z"`).

## Проверка после генерации

```bash
./gradlew :kotlin-client:jvmTest
./gradlew :kotlin-client:compileKotlinJvm :kotlin-client:compileKotlinIosArm64
```
