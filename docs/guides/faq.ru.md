# Часто задаваемые вопросы (FAQ)

> [English](faq.md) | Русский

Ответы на типичные вопросы о Flagent.

## Общие вопросы

### Что такое Flagent?

Flagent — open-source платформа для feature flags и экспериментов на Kotlin/Ktor. Позволяет безопасно выкатывать фичи, проводить A/B тесты и управлять динамической конфигурацией с тонкой сегментацией и аналитикой в реальном времени.

### Flagent бесплатный?

Да. Flagent распространяется под Apache 2.0, в том числе для коммерческого использования.

### Чем Flagent отличается от других решений?

- **Современный стек**: Kotlin/Ktor, Kotlin Coroutines, высокая производительность
- **Нативный Kotlin SDK**: полноценная поддержка Kotlin
- **Self-Hosted**: полный контроль над инфраструктурой и данными
- **Production-Ready**: Clean Architecture, лучшие практики
- **Open Source**: активная разработка

### Как быстро начать?

Самый быстрый способ — Docker:

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

Откройте `http://localhost:18000`. Подробнее: [Getting Started](getting-started.md).

### Какие БД поддерживаются?

PostgreSQL (рекомендуется для production), MySQL, SQLite (только для разработки).

### Можно ли запускать в Kubernetes?

Да. Flagent stateless, подходит для Kubernetes. См. [Deployment](deployment.md).

### Системные требования?

Память: минимум 512 MB, рекомендуется 2 GB+. CPU: 1 ядро минимум, 2+ рекомендуется. БД: PostgreSQL 12+, MySQL 8+ или SQLite 3.

### Flagent готов к production?

Да: быстрый evaluation, горизонтальное масштабирование, пул соединений к БД, health check, метрики Prometheus, обработка ошибок и логирование.

## Feature flags

### Как создать флаг?

Через Web UI (`http://localhost:18000`), REST API (`POST /api/v1/flags`) или SDK (Kotlin, JavaScript, Swift).

### Как проверить, включён ли флаг?

Через Kotlin SDK: `FlagentClient.create(...).isEnabled("my-feature")`. См. примеры в [Getting Started](getting-started.md).

### Как делать постепенный rollout?

Создайте флаг, задайте процент (например 10%), следите за метриками, постепенно увеличивайте (10% → 50% → 100%). Подробнее: [Use Cases](use-cases.md).

### Можно ли мгновенно выключить фичу в production?

Да. Флаги переключаются через UI или API без деплоя (kill switch).

## A/B тестирование

Флаг с несколькими вариантами → задать распределение → evaluation в коде → сбор метрик по вариантам → анализ. Назначение детерминировано (MurmurHash3). Поддерживается A/B/n. Статистическая значимость и экспорт данных — в расширенных возможностях.

## SDK и интеграция

Официальные SDK: Kotlin (JVM, Android, KMP), JavaScript/TypeScript (Node.js, браузер, React), Swift (iOS, macOS, tvOS). Есть Ktor-плагин (`ktor-flagent`) и Spring Boot Starter (`flagent-spring-boot-starter`). См. [README — SDKs](https://github.com/MaxLuxs/Flagent/blob/main/README.md#-sdks).

## Производительность

Низкая задержка за счёт in-memory кэша; типично < 10 ms на запрос. 100+ evaluations/s на инстанс (зависит от железа и настройки). Горизонтальное масштабирование за load balancer. Базовое потребление памяти ~200 MB. Кэш с настраиваемым TTL и интервалом обновления; SDK кэшируют локально.

## Безопасность

Поддерживаются: JWT, Basic Auth, API key в заголовке, cookie. В Enterprise: SAML, OAuth/OIDC. Данные: шифрование БД (SSL/TLS), self-hosted, без отправки во внешние сервисы. Подробнее: [Configuration](configuration.md#authentication-configuration).

## Конфигурация

Всё через переменные окружения. См. [Configuration](configuration.md). Конфиг-файлы пока не поддерживаются. Несколько окружений (dev/staging/prod): отдельные БД и инстансы или переменные окружения.

## Устранение неполадок

**Не запускается:** проверьте строку подключения к БД, доступность БД, порт 18000, переменные окружения.  
**Флаги не работают:** флаг включён в UI, SDK указывает на правильный URL, контекст entity совпадает с правилами таргетинга, SDK инициализирован.  
**Медленно:** пул соединений, кэш, сетевая задержка до БД, индексы. См. [Performance Optimizations](../features/performance-optimizations.md).

## Разработка и контрибьюция

Fork → ветка → изменения → pull request. См. [Contributing](contributing.md). Баги и фичи: [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) с описанием, шагами воспроизведения и окружением.

## Roadmap

**Уже есть:** импорт/экспорт YAML/JSON, CLI и GitHub webhook, Python/Go/Java SDK, Enterprise (multi-tenancy, SSO, RBAC, Smart Rollout, Anomaly Detection). **В планах:** бинарный CLI, вебхуки, расширенная аналитика, .NET SDK, multi-region, audit logs. См. [Roadmap](roadmap.md).

## Поддержка

- [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) — вопросы, баги, фичи
- [Документация](https://maxluxs.github.io/Flagent/guides/getting-started.md)
- [Примеры кода](https://github.com/MaxLuxs/Flagent/tree/main/samples)
- max.developer.luxs@gmail.com

## Лицензия

Apache License 2.0. Коммерческое использование разрешено. [LICENSE](https://github.com/MaxLuxs/Flagent/blob/main/LICENSE).

---

**Остались вопросы?** [Создайте issue](https://github.com/MaxLuxs/Flagent/issues) или напишите на max.developer.luxs@gmail.com.
