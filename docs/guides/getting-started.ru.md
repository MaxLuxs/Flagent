# Начало работы с Flagent

> [English](getting-started.md) | Русский

Добро пожаловать в Flagent! Это руководство поможет вам начать работу с feature flags и экспериментами менее чем за 10 минут.

## Что такое Flagent?

Flagent — это open-source платформа для feature flags и экспериментов, которая помогает вам:

- **Безопасно развертывать** - Постепенный rollout функций с kill switches
- **Проводить эксперименты** - A/B тестирование новых функций со статистической точностью
- **Таргетировать пользователей** - Показывать функции определенным сегментам пользователей
- **Быстро двигаться** - Развертывать код в production без риска

## Быстрый старт (5 минут)

### 1. Запустить Flagent сервер

Для работы бэкенду нужны **база данных** и **учётные данные админа** (и JWT secret для входа). Без них при входе в UI будет ошибка «Admin credentials not configured».

#### Через Docker (Рекомендуется)

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 \
  -e FLAGENT_ADMIN_EMAIL=admin@local \
  -e FLAGENT_ADMIN_PASSWORD=admin \
  -e FLAGENT_JWT_AUTH_SECRET=change-me-min-32-chars-for-dev-only \
  -v flagent-data:/data \
  ghcr.io/maxluxs/flagent
```

По умолчанию в образе используется SQLite (`/data/flagent.sqlite`). Том `-v flagent-data:/data` сохраняет данные между перезапусками.

#### Через Docker Compose (PostgreSQL)

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
docker compose up -d
```

В `docker-compose.yml` уже заданы PostgreSQL и учётные данные.

#### Сборка из исходников

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
./gradlew build
```

Перед запуском задайте переменные окружения (иначе вход в UI не сработает):

```bash
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
export FLAGENT_ADMIN_EMAIL=admin@local
export FLAGENT_ADMIN_PASSWORD=admin
export FLAGENT_JWT_AUTH_SECRET=dev-secret-at-least-32-characters-long
export PORT=18000
./gradlew :backend:run
```

Для PostgreSQL: [Configuration](configuration.md).

### 2. Открыть Flagent UI

Откройте браузер и перейдите по адресу:

```
http://localhost:18000
```

**Данные по умолчанию:**
- Email: `admin@local`
- Пароль: `admin`

### 3. Создать первый флаг

1. Нажмите кнопку **"Создать флаг"**
2. Введите данные флага:
   - **Ключ**: `my_first_flag`
   - **Описание**: `Мой первый feature flag`
   - **Включен**: ✓ Отметить
3. Добавить **Сегмент**:
   - **Rollout**: 100%
4. Добавить **Вариант**:
   - **Ключ**: `enabled`
5. Добавить **Распределение**:
   - **Вариант**: `enabled`
   - **Процент**: 100%
6. Нажать **"Сохранить"**

### 4. Оценить из вашего приложения

Выберите ваш язык:

**Kotlin:**
```kotlin
val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")

val result = client.evaluate(
    flagKey = "my_first_flag",
    entityID = "user123"
)

if (result.variantKey == "enabled") {
    // Функция включена
}
```

**Python:**
```python
from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")
result = await client.evaluate(flag_key="my_first_flag", entity_id="user123")

if result.is_enabled():
    # Функция включена
```

**JavaScript:**
```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1'
});

const result = await client.evaluate({
  flagKey: 'my_first_flag',
  entityID: 'user123'
});

if (result.variantKey === 'enabled') {
  // Функция включена
}
```

**Swift:**
```swift
let client = FlagentClient(baseURL: "http://localhost:18000/api/v1")

let result = try await client.evaluate(
    flagKey: "my_first_flag",
    entityID: "user123"
)

if result.variantKey == "enabled" {
    // Функция включена
}
```

### 5. Протестировать флаг

Запустите ваше приложение и убедитесь, что оценка флага работает!

## Основные концепции

### Флаг

**Флаг** — это переключатель функции, который контролирует, включена или выключена функция.

```kotlin
val result = client.evaluate(flagKey = "new_payment_flow", entityID = "user123")
```

### Вариант

**Вариант** представляет различные версии функции (например, control, treatment_a, treatment_b).

```kotlin
when (result.variantKey) {
    "control" -> showOldUI()
    "treatment_a" -> showNewUIv1()
    "treatment_b" -> showNewUIv2()
}
```

### Сегмент

**Сегмент** определяет правила для таргетирования конкретных пользователей.

**Пример**: Показать функцию премиум пользователям в США
- Ограничение 1: `tier == "premium"`
- Ограничение 2: `region == "US"`
- Rollout: 50%

### Ограничение

**Ограничение** — это правило, которое сопоставляет контекст пользователя.

**Операторы:**
- `EQ` (равно)
- `NEQ` (не равно)
- `IN` (в списке)
- `GT/LT` (больше/меньше)
- `CONTAINS` (строка содержит)
- `EREG` (совпадение regex)

**Пример:**
```json
{
  "property": "region",
  "operator": "IN",
  "value": "US,CA,UK"
}
```

### Распределение

**Распределение** контролирует процентное разделение между вариантами.

**Пример**: 50/50 A/B тест
- Вариант A: 50%
- Вариант B: 50%

### Rollout

**Rollout** контролирует, какой процент совпавших пользователей видит функцию.

**Пример**: Постепенный rollout
1. Старт: 1% rollout
2. После 1 дня: 10% rollout
3. После 1 недели: 50% rollout
4. После 2 недель: 100% rollout

## Распространенные сценарии использования

### 1. Kill Switch

**Сценарий**: Мгновенно отключить проблемную функцию

```kotlin
// Настройка: Создать флаг с 100% включением
val flag = createFlag(key = "new_feature", enabled = true)

// В production: Переключить флаг в UI при возникновении проблем
if (client.evaluate("new_feature", entityID).isEnabled()) {
    showNewFeature()
} else {
    showOldFeature()
}
```

### 2. Постепенный rollout

**Сценарий**: Развернуть функцию для 1%, 10%, 50%, 100%

```kotlin
// Настройка: Создать флаг с rollout=1%
val segment = Segment(rolloutPercent = 1)

// Мониторить метрики, постепенно увеличивать rollout
// День 1: 1% -> День 2: 10% -> Неделя 1: 50% -> Неделя 2: 100%
```

### 3. A/B тест

**Сценарий**: Тестировать вариант A vs B процесса оформления заказа

```kotlin
val result = client.evaluate("checkout_experiment", entityID = user.id)

when (result.variantKey) {
    "control" -> showOldCheckout()
    "variant_a" -> showNewCheckoutA()
    "variant_b" -> showNewCheckoutB()
}

// Отслеживать конверсию
analytics.track("checkout_completed", mapOf("variant" to result.variantKey))
```

### 4. Сегментация пользователей

**Сценарий**: Показывать премиум функцию только премиум пользователям

```kotlin
// Настройка: Добавить ограничение tier=="premium"
val result = client.evaluate(
    flagKey = "premium_feature",
    entityID = user.id,
    context = mapOf("tier" to user.tier)
)

if (result.isEnabled()) {
    showPremiumFeature()
}
```

### 5. Региональные функции

**Сценарий**: Показывать функцию только в определенных регионах

```kotlin
// Настройка: Добавить ограничение region IN ["US","CA","UK"]
val result = client.evaluate(
    flagKey = "regional_feature",
    entityID = user.id,
    context = mapOf("region" to user.region)
)
```

## Лучшие практики

### 1. Давайте флагам понятные имена

**Хорошо:**
- `new_payment_flow`
- `checkout_experiment_v2`
- `dark_mode_enabled`

**Плохо:**
- `test_flag`
- `flag_123`
- `new_thing`

### 2. Начинайте с малых rollout

Всегда начинайте с 1-5% rollout и постепенно увеличивайте после мониторинга метрик.

### 3. Используйте ограничения для таргетирования

Вместо хардкода ID пользователей, используйте ограничения:

```kotlin
// Плохо: Хардкод ID пользователей в коде
if (userId in listOf("user1", "user2")) {
    showNewFeature()
}

// Хорошо: Используйте флаг с ограничением
val result = client.evaluate(
    flagKey = "new_feature",
    entityID = userId,
    context = mapOf("tier" to "premium")
)
```

### 4. Очищайте старые флаги

Удаляйте флаги после того, как функция полностью развернута или эксперимент завершен.

### 5. Мониторьте метрики

Всегда мониторьте ключевые метрики при развертывании новых функций:
- Процент ошибок
- Метрики производительности
- Вовлеченность пользователей
- Коэффициент конверсии

### 6. Используйте режим отладки

Включите режим отладки во время разработки, чтобы видеть логи оценки:

```kotlin
val result = client.evaluate(
    flagKey = "test_flag",
    entityID = "test_user",
    enableDebug = true
)

println(result.debugLogs) // Посмотреть, почему флаг совпал или не совпал
```

## Настройка SDK

Подставьте версию из корневого файла [`VERSION`](https://github.com/MaxLuxs/Flagent/blob/main/VERSION) или [Releases](https://github.com/MaxLuxs/Flagent/releases). В примерах ниже может быть указана конкретная версия (например `0.1.6`).

### Kotlin/Android

```kotlin
dependencies {
    implementation("com.flagent:kotlin-client:0.1.6")
}
```

```kotlin
val client = FlagentClient.create(
    baseUrl = "http://localhost:18000/api/v1"
)
```

### Python

```bash
pip install flagent-python-client
```

```python
from flagent import FlagentClient

client = FlagentClient(base_url="http://localhost:18000/api/v1")
```

### JavaScript/Node.js

```bash
npm install @flagent/client
```

```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1'
});
```

### Swift/iOS

```swift
// Package.swift
.package(url: "https://github.com/MaxLuxs/Flagent.git", from: "0.1.6")
```

```swift
let client = FlagentClient(baseURL: "http://localhost:18000/api/v1")
```

## Продвинутые возможности

### Client-Side Evaluation (Offline-First)

Оценивайте флаги локально без API вызовов:

```kotlin
val manager = OfflineFlagentManager(exportApi, flagApi)
manager.bootstrap() // Загрузить снимок один раз

// Быстрая локальная оценка без API вызова
val result = manager.evaluate(flagKey = "feature", entityID = "user123")
```

**Преимущества:**
- Значительно быстрее, чем запросы к серверу
- Работает offline
- Значительное снижение нагрузки на сервер

См. [Руководство по Client-Side Evaluation](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/CLIENT_SIDE_EVALUATION.md)

### Real-Time обновления

Получайте мгновенные обновления флагов через Server-Sent Events:

```kotlin
manager.enableRealtimeUpdates(baseUrl = "http://localhost:18000")

// Флаги автоматически обновляются при изменении (< 1s латентность)
```

См. [Руководство по Real-Time обновлениям](https://github.com/MaxLuxs/Flagent/blob/main/sdk/kotlin-enhanced/REALTIME_UPDATES.md)

## Следующие шаги

- **[Документация API](../api/endpoints.md)** - Изучите все API endpoints
- **[Архитектура](../architecture/backend.md)** - Поймите, как работает Flagent
- **[Сценарии использования](use-cases.ru.md)** - Посмотрите примеры из реального мира
- **[Примеры](../examples/README.ru.md)** - Примеры кода для распространенных сценариев

## Устранение неполадок

### Флаг не найден

**Проблема**: Оценка возвращает "флаг не найден"

**Решения**:
1. Проверьте, что флаг существует в UI
2. Проверьте правильность написания ключа флага
3. Убедитесь, что флаг включен

### Оценка всегда возвращает null

**Проблема**: Флаг включен, но оценка возвращает null вариант

**Причины**:
1. Не настроены сегменты
2. Ограничения сегмента не совпадают
3. Процент rollout равен 0%
4. Не настроены распределения

**Решения**:
1. Добавьте хотя бы один сегмент
2. Проверьте совпадение ограничений с режимом отладки
3. Увеличьте процент rollout
4. Добавьте распределения в сегмент

### Медленная оценка

**Проблема**: Оценка флага занимает > 100ms

**Решения**:
1. Используйте client-side evaluation для меньшей латентности
2. Включите кэширование в SDK
3. Используйте batch оценку для нескольких флагов
4. Проверьте сетевую латентность до сервера

### Docker контейнер не запускается

**Проблема**: Контейнер немедленно завершается

**Решения**:
1. Проверьте логи Docker: `docker logs <container_id>`
2. Убедитесь, что порт 18000 не занят
3. Проверьте подключение к базе данных, если используете внешнюю БД

## Поддержка

- **Документация**: https://maxluxs.github.io/Flagent/guides/getting-started.ru.md
- **GitHub Issues**: https://github.com/MaxLuxs/Flagent/issues
- **Контакт**: max.developer.luxs@gmail.com

## Лицензия

Flagent — это open-source проект под лицензией Apache 2.0.
