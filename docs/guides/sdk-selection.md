# Какой SDK выбрать

Краткое руководство по выбору клиентского SDK Flagent в зависимости от платформы, сценария (server-side vs client-side evaluation) и способа интеграции.

## Сводная таблица

| SDK | Платформа | Рекомендуемый вход | Server-side eval | Client-side eval (offline) | Когда использовать |
|-----|-----------|--------------------|------------------|----------------------------|--------------------|
| **Kotlin Enhanced** | JVM, Android, KMP (iOS) | `Flagent.builder().baseUrl(...).build()` | ✅ | ✅ (mode `.offline`) | Мобильные приложения (Android/iOS), десктоп, общий Kotlin-код. Единый API: `evaluate`, `isEnabled`, `evaluateBatch`. |
| **Kotlin (base)** | JVM, Android, KMP | `EvaluationApi` + низкоуровневый API | ✅ | ❌ | Когда нужен только вызов API без кэша и офлайн-режима. |
| **Ktor plugin** | Ktor server | `installFlagent { ... }` | ✅ | ❌ | Сервер на Ktor: один плагин, `getFlagentClient()`, кэш на сервере. |
| **Spring Boot Starter** | Spring Boot | `application.yml` + `FlagentEvaluationFacade` | ✅ | ❌ | Сервер на Spring: автоконфиг, инъекция фасада. |
| **JavaScript Enhanced** | Browser, Node, React Native | `Flagent.create({ basePath, ... })` | ✅ | ❌* | Веб и React Native; кэш, один объект с `evaluate`/`isEnabled`/`evaluateBatch`. |
| **Swift Enhanced** | iOS, macOS | `Flagent.builder()...build()` | ✅ | ✅ (`.mode(.offline)`) | Нативные приложения Apple. |
| **Go Enhanced** | Go 1.18+ | `enhanced.NewFlagent(ctx, baseURL, opts)` | ✅ | ✅ (`opts.Offline = true`) | Микросервисы, CLI, бэкенды на Go. Интерфейс `Client`: `Evaluate`, `IsEnabled`, `EvaluateBatch`. |
| **Flutter/Dart Enhanced** | Flutter, Web | `Flagent.create(baseUrl: ..., config: ...)` | ✅ | ✅ (offline snapshot) | Кроссплатформенное мобильное/веб приложение. |
| **Python** | 3.8+ | `FlagentClient(base_url=...)` (или обёртка) | ✅ | ❌ | Скрипты, ML-пайпы, серверы на Python. |
| **Java** | JVM | `Flagent.INSTANCE.builder()` (standalone) или Spring Starter | ✅ | ❌ | Серверы на Java без Kotlin. |

\* В JS Enhanced client-side evaluation можно реализовать поверх Export API и локального вычисления при необходимости; основной сценарий — server-side с кэшем.

## Server-side vs client-side evaluation

- **Server-side:** каждый вызов `evaluate`/`isEnabled` при необходимости идёт на сервер (или отдаётся из кэша). Актуально для серверов (Ktor, Spring Boot, Go), когда нужна централизованная логика и минимальная задержка на клиенте не критична.
- **Client-side (offline):** SDK один раз загружает снапшот флагов (Export API или Flags API), дальше вычисляет вариант локально по правилам (constraints, rollout). Низкая задержка (&lt;1 ms), работа без сети после bootstrap. Доступно в **Kotlin Enhanced** (`.mode(.offline)`), **Swift Enhanced** (`.mode(.offline)`), **Go Enhanced** (`opts.Offline = true`), **Flutter Enhanced**.

## Real-time updates (SSE)

Подписка на изменения флагов без опроса: **Kotlin Enhanced**, **Go Enhanced** поддерживают SSE; после изменения на сервере клиент получает обновления и при необходимости перезагружает снапшот или инвалидирует кэш.

## Рекомендуемый способ создания клиента

Во всех Enhanced SDK рекомендуется один вход:

- **Kotlin:** `Flagent.builder().baseUrl(...).cache(...).mode(...).build()`
- **JS:** `Flagent.create({ basePath, enableCache, cacheTtlMs, ... })`
- **Swift:** `Flagent.builder().basePath(...).build()`
- **Go:** `enhanced.NewFlagent(ctx, baseURL, enhanced.DefaultOptions())`
- **Dart/Flutter:** `Flagent.create(baseUrl: ..., config: ...)`

Прямое использование `FlagentManager` / `OfflineFlagentManager` / base API остаётся для продвинутых сценариев (кастомный кэш, тонкая настройка).

## Связанные разделы

- [SDKs Overview](../sdks/README.md) — установка и ссылки по каждому SDK.
- [SDK Integration Examples](../examples/sdk-integration.md) — примеры кода по платформам.
- [Getting Started](getting-started.md) — быстрый старт и первый флаг.
- [Offline-First SDK](../features/offline-first-sdk.md) — детали client-side evaluation.
