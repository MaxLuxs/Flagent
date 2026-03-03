# Сценарии использования

> [English](../../guides/use-cases.md)

Flagent может использоваться в различных сценариях для улучшения доставки ПО, экспериментов и пользовательского опыта. Вот некоторые распространенные сценарии использования и примеры из реальной жизни.

## Feature Flags для безопасного развертывания

Развертывайте код в production за feature flags, обеспечивая мгновенные откаты без изменения кода.

### Пример: Постепенный rollout новой системы оплаты

```kotlin
val client = FlagentClient.create(
    baseUrl = "http://localhost:18000/api/v1",
    apiKey = "your-api-key"
)

// Проверить, включена ли новая система оплаты
if (client.isEnabled("new_payment_system", entityContext = userContext)) {
    // Использовать новую реализацию оплаты
    newPaymentService.processPayment(amount, currency)
} else {
    // Откат на legacy систему оплаты
    legacyPaymentService.processPayment(amount, currency)
}
```

**Преимущества:**
- ✅ Безопасное развертывание кода в production
- ✅ Мгновенный откат при обнаружении проблем
- ✅ Постепенный rollout (1%, 10%, 50%, 100%)
- ✅ A/B тестирование новой реализации

## A/B тестирование для оптимизации продукта

Проводите эксперименты для тестирования новых функций и измерения их влияния на ключевые метрики.

### Пример: A/B тест процесса оформления заказа

```kotlin
val assignment = client.evaluate(
    flagKey = "checkout_experiment",
    entityContext = mapOf(
        "user_id" to userId,
        "country" to "US",
        "tier" to userTier
    )
)

when (assignment?.variant) {
    "control" -> showOldCheckoutFlow()
    "variant_a" -> showNewCheckoutFlowA()
    "variant_b" -> showNewCheckoutFlowB()
}

// Отслеживать конверсию
analytics.track("checkout_completed", mapOf(
    "variant" to assignment?.variant,
    "amount" to orderAmount
))
```

**Преимущества:**
- ✅ Тестировать несколько вариантов одновременно
- ✅ Расчет статистической значимости
- ✅ Детерминированный bucketing (стабильные результаты)
- ✅ Аналитика в реальном времени

## Постепенные rollout с Kill Switch

Развертывайте функции постепенно и мгновенно отключайте при обнаружении проблем.

### Пример: Rollout новой функции

```kotlin
// Начать с 1% rollout
val enabled = client.isEnabled(
    "new_feature",
    entityContext = mapOf(
        "region" to "US",
        "tier" to "premium"
    )
)

if (enabled) {
    try {
        // Реализация новой функции
        newFeature.execute()
    } catch (e: Exception) {
        // Логировать ошибку и отключить функцию через Flagent UI
        logger.error("New feature failed", e)
        // Функция будет автоматически отключена в Flagent
    }
} else {
    // Откат на старую реализацию
    oldFeature.execute()
}
```

**Преимущества:**
- ✅ Начать с малого процента (1%)
- ✅ Мониторить метрики и ошибки
- ✅ Увеличивать постепенно (10%, 50%, 100%)
- ✅ Мгновенный kill switch при возникновении проблем

## Динамическая конфигурация

Обновляйте настройки приложения без повторного развертывания.

### Пример: Динамические лимиты скорости

```kotlin
val config = client.getVariantAttachment("app_config")
val maxRetries = config?.get("max_retries")?.toIntOrNull() ?: 3
val timeout = config?.get("timeout_ms")?.toLongOrNull() ?: 5000
val rateLimit = config?.get("rate_limit_per_minute")?.toIntOrNull() ?: 100

// Использовать динамическую конфигурацию
retryService.maxRetries = maxRetries
httpClient.timeout = Duration.ofMillis(timeout)
rateLimiter.limitPerMinute = rateLimit
```

**Преимущества:**
- ✅ Изменять настройки без развертывания кода
- ✅ Конфигурация для конкретных окружений
- ✅ Мгновенные обновления во всех экземплярах
- ✅ Откат изменений конфигурации

## Сегментация и таргетирование пользователей

Таргетируйте конкретные сегменты пользователей с помощью сложных правил.

### Пример: Премиум функции для конкретных регионов

```kotlin
val enabled = client.isEnabled(
    "premium_feature",
    entityContext = mapOf(
        "tier" to "premium",
        "region" to "US",
        "subscription_age_days" to subscriptionAge.toString(),
        "account_value" to accountValue.toString()
    )
)

if (enabled) {
    // Показать премиум функцию
    showPremiumFeature()
} else {
    // Показать стандартную функцию
    showStandardFeature()
}
```

**Преимущества:**
- ✅ Таргетирование по нескольким атрибутам
- ✅ Сложные правила таргетирования (логика AND/OR)
- ✅ Процентное таргетирование
- ✅ Функции для конкретных регионов

## Canary развертывания

Постепенно развертывайте новые версии для подмножества пользователей.

### Пример: Canary развертывание новой версии

```kotlin
val variant = client.evaluate(
    "api_version",
    entityContext = mapOf(
        "user_id" to userId,
        "api_key" to apiKey
    )
)?.variant ?: "v1"

when (variant) {
    "v1" -> useAPIv1()
    "v2" -> useAPIv2() // Canary версия
    "v3" -> useAPIv3() // Будущая версия
}
```

**Преимущества:**
- ✅ Тестировать новые версии с реальными пользователями
- ✅ Мониторить производительность и ошибки
- ✅ Мгновенный откат при возникновении проблем
- ✅ Стратегия постепенной миграции

## Dark Mode / Beta функции

Включайте функции для конкретных групп пользователей (внутреннее тестирование, beta пользователи).

### Пример: Доступ к beta функциям

```kotlin
val isBetaUser = client.isEnabled(
    "beta_features",
    entityContext = mapOf(
        "user_id" to userId,
        "user_role" to userRole,
        "beta_program" to "enabled"
    )
)

if (isBetaUser) {
    // Показать beta функции
    showBetaFeatures()
}
```

**Преимущества:**
- ✅ Внутреннее тестирование без влияния на production
- ✅ Управление программой beta пользователей
- ✅ Feature gating для премиум пользователей
- ✅ Постепенный выпуск функций

## Оптимизация производительности

Тестируйте оптимизации производительности с помощью A/B тестирования.

### Пример: Оптимизация запросов к БД

```kotlin
val variant = client.evaluate(
    "db_query_optimization",
    entityContext = mapOf(
        "user_id" to userId
    )
)?.variant ?: "old"

when (variant) {
    "old" -> {
        // Использовать старый запрос
        val result = database.oldQuery()
        // Отслеживать производительность
        metrics.record("query_time", oldQueryTime, mapOf("variant" to "old"))
    }
    "optimized" -> {
        // Использовать оптимизированный запрос
        val result = database.optimizedQuery()
        // Отслеживать производительность
        metrics.record("query_time", optimizedQueryTime, mapOf("variant" to "optimized"))
    }
}
```

**Преимущества:**
- ✅ Безопасно тестировать улучшения производительности
- ✅ Сравнивать старую vs оптимизированную реализации
- ✅ Измерять влияние на производительность в реальных условиях
- ✅ Постепенно развертывать оптимизированную версию

## Аварийные Kill Switch

Мгновенно отключайте функции в production при обнаружении критических проблем.

### Пример: Kill switch системы оплаты

```kotlin
val paymentEnabled = client.isEnabled(
    "payment_system",
    entityContext = emptyMap() // Глобальный флаг
)

if (!paymentEnabled) {
    // Система оплаты глобально отключена
    return ErrorResponse("Система оплаты временно недоступна")
}

// Нормальная обработка оплаты
processPayment()
```

**Преимущества:**
- ✅ Мгновенное глобальное отключение функции
- ✅ Экстренное реагирование на критические проблемы
- ✅ Не требуется развертывание кода
- ✅ Понятные сообщения об ошибках для пользователей

## Multi-Tenant конфигурация

Разная конфигурация для разных арендаторов или клиентов.

### Пример: Функции для конкретных арендаторов

```kotlin
val tenantFeatures = client.evaluate(
    "tenant_features",
    entityContext = mapOf(
        "tenant_id" to tenantId,
        "plan" to tenantPlan
    )
)?.variantAttachment

val maxUsers = tenantFeatures?.get("max_users")?.toIntOrNull() ?: 10
val storageGB = tenantFeatures?.get("storage_gb")?.toIntOrNull() ?: 100
val enabledFeatures = tenantFeatures?.get("features")?.split(",") ?: emptyList()
```

**Преимущества:**
- ✅ Кастомизировать функции для каждого арендатора
- ✅ Разные планы и конфигурации
- ✅ Легкое обновление/понижение планов
- ✅ Изоляция арендаторов

## Примеры из реального мира

### E-commerce платформа
- **Сценарий**: A/B тестирование вариантов процесса оформления заказа
- **Преимущества**: Увеличение коэффициента конверсии на 15% после определения лучшего варианта
- **Реализация**: Разделение 50/50 между старым и новым оформлением заказа, измерение коэффициентов конверсии

### SaaS приложение
- **Сценарий**: Постепенный rollout нового дизайна UI
- **Преимущества**: Снижение жалоб пользователей за счет постепенного развертывания (1% → 10% → 50% → 100%)
- **Реализация**: Rollout по регионам, мониторинг отзывов пользователей

### Мобильное приложение
- **Сценарий**: Feature flags для новых функций приложения
- **Преимущества**: Включение функций сначала для beta пользователей, затем постепенное расширение
- **Реализация**: Таргетирование на основе уровня пользователя (beta → premium → все пользователи)

## Лучшие практики

1. **Начинайте с малого**: Начинайте с 1% rollout и постепенно увеличивайте
2. **Мониторьте метрики**: Отслеживайте ключевые метрики (конверсия, ошибки, производительность)
3. **Настройте алерты**: Настройте алерты для процента ошибок и деградации производительности
4. **Документируйте флаги**: Документируйте, что делает каждый флаг и какое ожидается влияние
5. **Очищайте**: Удаляйте неиспользуемые флаги для снижения сложности
6. **Тестируйте локально**: Используйте локальные переопределения для тестирования перед production
7. **Контроль версий**: Отслеживайте изменения флагов в системе контроля версий (при использовании flags as code)

## Следующие шаги

- 📖 [Начало работы](getting-started.ru.md) - Настройте Flagent
- 🏗️ [Архитектура](../architecture/backend.md) - Поймите, как работает Flagent
- 📚 [Документация API](../api/endpoints.md) - Изучите API endpoints
- 💻 [Примеры кода](../examples/README.ru.md) - См. больше примеров
