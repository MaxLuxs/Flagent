# Единая точка входа в клиентских SDK

План введения единой точки входа (builder / фасад) во всех клиентских SDK Flagent, чтобы разработчики настраивали и использовали клиент через один понятный API, без ручной сборки низкоуровневых API.

## Цель

- **Один конфиг** (builder или options) — baseUrl, auth, cache, режим (server-side / client-side evaluation).
- **Один клиент** — после `build()` / `initialize()` вызывать только `evaluate()`, `isEnabled()`, при необходимости `evaluateBatch()`.
- **Скрыть внутренние слои** — не требовать от пользователя знать про EvaluationApi, ExportApi, FlagApi, FlagentManager vs OfflineFlagentManager и т.д.

## Общий контракт API (целевой)

Все SDK стремятся к одному стилю:

```
1. Конфигурация (builder или options):
   - baseUrl (обязательно)
   - auth: bearer token / api key / basic (опционально)
   - cache: enabled, ttlMs (опционально)
   - mode: "server" | "offline" (client-side evaluation), опционально
   - platform: httpClientEngine / context — где нужно для KMP/Android/iOS

2. Создание клиента:
   - client = Flagent.builder().baseUrl(...).auth(...).cache(...).build()
   - client.initialize()  // при необходимости (офлайн bootstrap), suspend/async

3. Использование:
   - client.evaluate(flagKey, entityID, entityType?, entityContext?) -> EvalResult
   - client.isEnabled(flagKey, entityID, context?) -> Boolean  // удобный обёртка
   - client.evaluateBatch(flagKeys, entities) -> List<EvalResult>  // где есть batch
```

Имена классов и методов могут отличаться по языку (Flagent / FlagentClient, builder / options), но смысл один.

---

## Какие SDK затрагиваем

| SDK | Где живёт | Сейчас | Нужна единая точка входа |
|-----|-----------|--------|---------------------------|
| **Kotlin (KMP)** | kotlin-client + kotlin-enhanced | EvaluationApi → FlagentManager или ExportApi+FlagApi → OfflineFlagentManager | ✅ Да (приоритет 1) |
| **JavaScript/TypeScript** | sdk/javascript + sdk/javascript-enhanced | Configuration → FlagentManager | ✅ Да |
| **Swift** | sdk/swift + sdk/swift-enhanced | FlagentConfig → FlagentManager | ✅ Да |
| **Go** | sdk/go + sdk/go-enhanced | NewClient + NewOfflineManager / NewManager | ✅ Да |
| **Dart/Flutter** | sdk/dart + sdk/flutter-enhanced | FlagentManager(baseUrl, config) | ✅ Да (уже близко) |
| **Java** | sdk/java + spring-boot-starter | ApiClient + EvaluationApi; Spring — FlagentEvaluationFacade | ⚪ Опционально (серверный) |
| **Python** | sdk/python | Генерация из OpenAPI | ⚪ По желанию (часто серверный) |

Debug UI (kotlin-debug-ui, swift-debug-ui, javascript-debug-ui) и flagent-koin не меняем — они потребляют уже созданный клиент/менеджер.

---

## План по этапам

### Этап 1: Kotlin (KMP) — kotlin-enhanced

**Зачем первым:** основной SDK в репозитории, Android/sample-kotlin, есть и server-side, и client-side evaluation.

**Шаги:**

1. **Ввести фасад и билдер в kotlin-enhanced**
   - Добавить объект/класс `Flagent` (или `FlagentClient`) с методом `builder()`.
   - `Flagent.Builder`: цепочка методов `baseUrl()`, `httpClientEngine()`, `auth {}`, `cache(enable, ttlMs)`, `offlineSupport(Boolean)` / `mode(FlagentMode.SERVER | OFFLINE)`, `build()`.
   - Внутри `build()`: при server — создавать EvaluationApi + FlagentManager; при offline — ExportApi + FlagApi + OfflineFlagentManager. Возвращать общий интерфейс (например, `FlagentClient` с методами `evaluate`, `isEnabled`, `evaluateBatch`).

2. **Общий интерфейс клиента**
   - Определить интерфейс (например, `FlagentClient`) с методами: `suspend fun evaluate(...)`, `suspend fun isEnabled(...)`, `suspend fun evaluateBatch(...)`.
   - Реализации: текущий FlagentManager и OfflineFlagentManager обернуть в адаптеры к этому интерфейсу (или привести их к одному интерфейсу, если сигнатуры совпадают).

3. **Расширить FlagentConfig**
   - При необходимости добавить в FlagentConfig (или в builder) поля: offlineSupport, auth (если не через отдельный блок). Не дублировать лишнее — builder может принимать существующий FlagentConfig и дополнять его.

4. **Документация и примеры**
   - Обновить README kotlin-enhanced: "Рекомендуемый способ — Flagent.builder()...".
   - Обновить sample-kotlin и android-sample: перейти на Flagent.builder() вместо ручного EvaluationApi + FlagentManager.
   - В README оставить старый способ как "продвинутый" (прямое использование FlagentManager/OfflineFlagentManager).

5. **Обратная совместимость**
   - FlagentManager и OfflineFlagentManager оставить публичными; фасад только добавляется. Koin-модуль при желании можно доработать под создание через Flagent.builder() позже.

**Результат этапа 1:** приложение может писать только `Flagent.builder().baseUrl(...).cache(true).build()` и вызывать `evaluate` / `isEnabled`, без знания про EvaluationApi и типов менеджеров.

---

### Этап 2: JavaScript/TypeScript

**Где:** `sdk/javascript` (base client), `sdk/javascript-enhanced` (FlagentManager, кэш).

**Шаги:**

1. **Единая точка входа в @flagent/enhanced-client**
   - Экспорт: `Flagent.create(options)` или `Flagent.initialize(options)`.
   - Options: `basePath`, `cacheTtlMs`, `enableCache`, опционально `offline` / `clientSideEvaluation`, auth (headers или callback). По возможности один тип `FlagentOptions`.

2. **Один клиентский объект**
   - Возвращать объект с методами: `evaluate(...)`, `isEnabled(...)`, `evaluateBatch(...)`.
   - Внутри при `offline: true` использовать client-side evaluation (если уже есть в JS enhanced); иначе — текущий FlagentManager поверх base client.

3. **Документация**
   - README javascript-enhanced: основной пример через `Flagent.create(...)`; старый способ с `new FlagentManager(config, ...)` оставить как альтернатива.

4. **Семплы**
   - Обновить примеры и React Native snippet в README/REACT_NATIVE.md под новый вход.

**Результат:** веб/React Native инициализируют один раз через `Flagent.create({ basePath, cache: {...} })` и дальше только `evaluate` / `isEnabled`.

---

### Этап 3: Swift

**Где:** `sdk/swift`, `sdk/swift-enhanced`.

**Шаги:**

1. **Builder или единый конфиг**
   - В swift-enhanced добавить, например, `FlagentClient.Builder` или статический `FlagentClient.create(config: FlagentConfig)` с расширенным `FlagentConfig`: baseURL, cache, auth, режим offline/client-side.

2. **Единый тип клиента**
   - Один тип (например, `FlagentClient`) с методами `evaluate(...)`, `isEnabled(...)`, `evaluateBatch(...)`.
   - Внутри — обёртка над текущим FlagentManager или offline-менеджером, в зависимости от конфига.

3. **Документация**
   - README swift-enhanced: рекомендуемый путь — создание через builder/create; старый способ оставить.

**Результат:** iOS/macOS приложение настраивает клиент одним вызовом и использует только evaluate/isEnabled.

---

### Этап 4: Go

**Где:** `sdk/go`, `sdk/go-enhanced`.

**Шаги:**

1. **Функция-конструктор с опциями**
   - Например, `enhanced.NewFlagent(ctx, baseURL, enhanced.Options{...})` или `flagent.New(ctx, flagent.Config{...})`.
   - Options: BaseURL, Cache TTL, Offline/ClientSideEvaluation, HTTP client, auth.

2. **Единый интерфейс**
   - Интерфейс с методами `Evaluate`, `IsEnabled`, `EvaluateBatch`.
   - Реализации: текущий manager и offline manager приводятся к этому интерфейсу; конструктор возвращает интерфейс.

3. **Документация и примеры**
   - README go-enhanced: основной пример через новый конструктор; старый способ оставить.

**Результат:** сервисы на Go создают клиент одним вызовом и работают через общий интерфейс.

---

### Этап 5: Dart/Flutter

**Где:** `sdk/dart`, `sdk/flutter-enhanced`.

**Текущее состояние:** уже есть `FlagentManager(baseUrl, config)` — один конструктор с конфигом.

**Шаги:**

1. **Привести к общему стилю**
   - Добавить именованный конструктор или фасад, например `Flagent.managed(baseUrl, config)` или `Flagent.create({ required baseUrl, cache, ... })`, возвращающий тот же FlagentManager, чтобы документация и примеры во всех SDK говорили одно и то же ("создаёте через Flagent.create / builder").

2. **Опционально: isEnabled**
   - Убедиться, что есть удобный метод вида `isEnabled(flagKey, entityID, context)` поверх evaluate.

3. **Документация**
   - README: "Рекомендуемый способ: Flagent.create(...)" и один блок кода в начале.

**Результат:** Flutter-разработчик видит тот же паттерн, что и в Kotlin/JS/Swift.

---

### Этап 6 (опционально): Java

**Где:** `sdk/java`, `sdk/spring-boot-starter`.

**Особенность:** в основном серверный сценарий; Spring уже даёт FlagentEvaluationFacade и конфиг через `application.yml`.

**Шаги (по желанию):**

1. **Фасад для standalone Java**
   - Класс `Flagent` или `FlagentClient` с билдером: baseUrl, auth, cache settings; `build()` возвращает объект с методами `evaluate`, `evaluateBatch` (и при необходимости `isEnabled`).
   - Внутри — существующие ApiClient и EvaluationApi.

2. **Spring**
   - Оставить как есть; при необходимости в доке указать: "Для Spring используйте starter; для standalone — Flagent.builder()".

**Результат:** единообразие с другими SDK при использовании без Spring.

---

### Этап 7 (опционально): Python

**Где:** `sdk/python` (генерируется из OpenAPI).

**Шаги (если решим делать единый вход):**

1. Тонкая обёртка в отдельном модуле или в том же пакете: функция `create_client(base_url, ...)` с опциями кэша и auth, возвращающая объект с `evaluate`, `evaluate_batch`.
2. Внутри — сгенерированный client + простой кэш при необходимости.
3. README: один рекомендуемый пример через эту обёртку.

---

## Порядок выполнения (сводка)

| Приоритет | SDK | Действие |
|-----------|-----|----------|
| 1 | Kotlin (kotlin-enhanced) | Фасад Flagent + Builder, интерфейс FlagentClient, обновить samples и README |
| 2 | JavaScript/TypeScript | Flagent.create() в enhanced-client, один объект с evaluate/isEnabled/evaluateBatch |
| 3 | Swift | Builder/create в swift-enhanced, единый FlagentClient |
| 4 | Go | NewFlagent(options), интерфейс Evaluate/IsEnabled/EvaluateBatch |
| 5 | Dart/Flutter | Flagent.create() / Flagent.managed() в документации и API, проверить isEnabled |
| 6 | Java | По желанию: Flagent.builder() для standalone |
| 7 | Python | По желанию: create_client() обёртка |

---

## Критерии готовности

- В каждом целевом SDK есть один рекомендуемый способ создать клиент (builder / create / options).
- Документация и основной пример в README используют этот способ.
- Методы evaluate и isEnabled (или эквивалент) доступны без знания внутренних API (EvaluationApi, ExportApi и т.д.).
- Обратная совместимость: старый способ (прямое использование manager/base API) по-прежнему работает и при необходимости описан как "продвинутый".

---

## Связанные документы

- [Текущий статус и следующие шаги](status.md)
- [Тестирование и верификация](verification-and-tests.md)
- README по каждому SDK в `sdk/<sdk>/README.md`
