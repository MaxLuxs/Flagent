<p align="center"><strong><a href="../../README.md">English</a></strong></p>

# Flagent

**Ship Features Safely. Experiment Confidently.** — первый Kotlin-native сервис feature flags: type-safe, coroutine-first флаги и эксперименты; опционально Smart Rollout и обнаружение аномалий (Enterprise).

<p align="center">
    <a href="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml?query=branch%3Amain+" target="_blank">
        <img src="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml/badge.svg?branch=main" alt="CI">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/actions/workflows/ci-swift.yml?query=branch%3Amain+" target="_blank">
        <img src="https://github.com/MaxLuxs/Flagent/actions/workflows/ci-swift.yml/badge.svg?branch=main" alt="CI (Swift)">
    </a>
    <a href="https://codecov.io/gh/MaxLuxs/Flagent">
        <img src="https://codecov.io/gh/MaxLuxs/Flagent/branch/main/graph/badge.svg" alt="Code Coverage">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/releases" target="_blank">
        <img src="https://img.shields.io/github/release/MaxLuxs/Flagent.svg?style=flat&color=green" alt="Release">
    </a>
    <a href="https://img.shields.io/badge/license-Apache%202.0-green.svg" target="_blank">
        <img src="https://img.shields.io/badge/license-Apache%202.0-green.svg" alt="License">
    </a>
</p>

**Flagent** — современная production-ready платформа для feature flags и экспериментов на **Kotlin/Ktor**. Первое Kotlin-native решение в экосистеме feature flags: type-safety, coroutines, чистая архитектура. Сборка Enterprise добавляет мультитенантность, SSO, RBAC, Smart Rollout и обнаружение аномалий.

**Проблема → Решение:** Командам нужно безопасно выкатывать фичи, проводить A/B тесты и откатываться без редеплоя. Flagent даёт feature flags, эксперименты, постепенные раскаты, kill switches и опциональную аналитику крашей по флагам в одной self-hosted или (в планах) облачной платформе — с Kotlin-native SDK и единым UI.

**Полный лендинг (обзор продукта, тарифы, CTA):** При запуске Flagent с включённым маркетинговым лендингом (`ENV_SHOW_LANDING=true`) откройте корень приложения (например `http://localhost:18000`). См. также [Документация](https://maxluxs.github.io/Flagent/languages/ru/guides/getting-started.ru.md) и [Тарифы и издания](../guides/pricing-and-editions.md).

## 🎯 Почему Flagent?

### Kotlin-Native превосходство
- **Industry-Standard Evaluation API** - Простая миграция с существующих feature flag решений
- **Type-Safe SDK** - Валидация на этапе компиляции и автодополнение в IDE
- **Coroutine-First** - Неблокирующий I/O и структурированная конкурентность
- **Ktor Ecosystem** - Бесшовная интеграция с Ktor приложениями
- **Clean Architecture** - DDD принципы и тестируемый дизайн

### Smart Rollout и Anomaly (Enterprise)
- **Smart Rollout** — настраиваемые автоматические постепенные раскаты (на метриках; Enterprise).
- **Anomaly Detection** — алерты и опциональный откат при деградации (Enterprise).
- **В планах:** predictive targeting, A/B insights, ML-автоматизация.

### Enterprise-Ready
- **Расширенное тестовое покрытие** — 200+ тестовых файлов
- **Высокая производительность** — низкая задержка evaluation, нагрузочное тестирование (см. [docs/performance/benchmarks.md](docs/performance/benchmarks.md))
- **Multi-Tenancy** — изолированные окружения для команд (Enterprise)
- **Real-Time обновления** — SSE в Kotlin Enhanced, Go Enhanced

## 🚀 Быстрый старт

Запустите Flagent менее чем за 5 минут:

```bash
# Через Docker (Рекомендуется)
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent

# Открыть Flagent UI
open http://localhost:18000
```

Настройте admin auth через `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD`, `FLAGENT_JWT_AUTH_SECRET`. См. [docs/guides/configuration.md](docs/guides/configuration.md).

### Скриншоты

| Дашборд | Список флагов | Debug Console |
|---------|----------------|---------------|
| ![Дашборд](docs/assets/screenshots/screenshot-dashboard.png) | ![Список флагов](docs/assets/screenshots/screenshot-flags-list.png) | ![Debug Console](docs/assets/screenshots/screenshot-debug-console.png) |
| **Эксперименты (A/B)** | **Создание флага** | **Тарифы** |
| ![Эксперименты](docs/assets/screenshots/screenshot-experiments.png) | ![Создание флага](docs/assets/screenshots/screenshot-create-flag.png) | ![Тарифы](docs/assets/screenshots/screenshot-pricing.png) |

## ✨ Ключевые возможности

### Основные возможности (Доступны сейчас)
- ✅ **Feature Flags** - Постепенные rollout, kill switches и удаленная конфигурация
- ✅ **A/B тестирование** - Мультивариантные эксперименты с детерминированным bucketing (MurmurHash3)
- ✅ **Продвинутый таргетинг** - Сегментация пользователей по атрибутам, процентам или сложным правилам ограничений
- ✅ **Multi-Environment** - Отдельные конфигурации для dev, staging и production
- ✅ **Data Recorders** - Интеграция с Kafka, Kinesis, PubSub для аналитики
- ✅ **Высокая производительность** - Низкая задержка evaluation с EvalCache и TTL
- ✅ **Несколько БД** - Поддержка PostgreSQL, MySQL, SQLite
- ✅ **Docker Ready** - Production-ready Docker образы с Compose
- ✅ **Официальные SDK** - Kotlin, JavaScript/TypeScript, Swift, Python, Go с Enhanced вариантами
- ✅ **Ktor Plugin** - Интеграция первого класса для Ktor серверов
- ✅ **Admin UI** - Современная панель управления на Compose for Web
- ✅ **Debug Console** - Тестирование и отладка evaluation в реальном времени

### Импорт/экспорт и Enterprise (модуль internal)
- ✅ **Импорт/экспорт** — флаги в YAML/JSON: POST /import и экспорт из настроек (OSS). Git sync и отдельный CLI-бинарник пока нет.
- ✅ **Multi-Tenancy** — тенанты, API-ключ на тенанта, X-Tenant-ID, переключатель тенанта в UI (Enterprise).
- ✅ **SSO** — провайдеры SAML и OAuth/OIDC, вход по тенанту и JWT (Enterprise). Совместимо с любым IdP (Okta, Auth0, Azure AD и т.д.).
- ✅ **RBAC** — кастомные роли и права, проверка на API (Enterprise).
- ✅ **Smart Rollout и Anomaly** — конфиг автоматического раската и алерты по аномалиям с опцией отката (Enterprise). Без ML-модели; на правилах и метриках.

## 📖 Документация

- 📖 **[Начало работы](https://maxluxs.github.io/Flagent/languages/ru/guides/getting-started.ru.md)** - Быстрый старт и настройка
- 📖 **[Совместимость API](https://maxluxs.github.io/Flagent/guides/compatibility.md)** - Evaluation API, руководство по миграции
- 📖 **[API Reference](https://maxluxs.github.io/Flagent)** - Документация API
- 📖 **[OpenAPI спецификация](https://maxluxs.github.io/Flagent/api/openapi.yaml)** - OpenAPI YAML
- 📖 **[Архитектура](https://maxluxs.github.io/Flagent/architecture/backend.md)** - Дизайн системы
- 📖 **[Конфигурация](https://maxluxs.github.io/Flagent/languages/ru/guides/configuration.ru.md)** - Переменные окружения
- 📖 **[Развертывание](https://maxluxs.github.io/Flagent/languages/ru/guides/deployment.ru.md)** - Production развертывание
- 📖 **[Сценарии использования](https://maxluxs.github.io/Flagent/languages/ru/guides/use-cases.ru.md)** - Примеры

## 🏗️ Структура проекта

```
flagent/
├── backend/          # Ktor backend (Clean Architecture)
├── frontend/         # Compose for Web UI
├── sdk/              # Клиентские SDK
│   ├── kotlin/       # Kotlin (KMP) базовый клиент
│   ├── kotlin-enhanced/   # Client-side eval, SSE
│   ├── kotlin-debug-ui/  # Compose debug-панель
│   ├── flagent-koin/     # Koin DI модуль
│   ├── java/         # Java клиент
│   ├── spring-boot-starter/
│   ├── javascript/   # JS/TS базовый
│   ├── javascript-enhanced/
│   ├── javascript-debug-ui/
│   ├── swift/        # Swift базовый
│   ├── swift-enhanced/
│   ├── swift-debug-ui/
│   ├── python/
│   ├── go/
│   ├── go-enhanced/
│   ├── dart/         # Dart базовый
│   └── flutter-enhanced/
├── ktor-flagent/     # Ktor plugin
└── docs/guides/roadmap.md   # Roadmap разработки
```

**Версия:** единственный источник — корневой файл `VERSION`. Gradle читает его; для npm/pip/Go/Swift/Helm/Java запустите `./scripts/sync-version.sh`. См. [docs/guides/versioning.md](docs/guides/versioning.md).

См. [README.md](README.md#development) для детальной структуры проекта и настройки разработки.

## 🛠️ Стек технологий

- **Kotlin** - Современный JVM язык с coroutines
- **Ktor** - Web-фреймворк для построения async приложений
- **Exposed** - Type-safe SQL фреймворк
- **Kotlinx Serialization** - JSON сериализация
- **Compose for Web** - Современный UI фреймворк
- **PostgreSQL/MySQL/SQLite** - Поддержка баз данных

## 📦 Установка

### Docker (Рекомендуется)

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

Без переменных окружения используется SQLite-файл в контейнере (данные теряются при перезапуске). Для сохранения данных используйте volume (см. [README.md](README.md#option-2-docker-with-persistent-sqlite)).

### Docker Compose (с PostgreSQL)

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
docker compose up -d
```

### Сборка из исходников

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
./gradlew build
./gradlew :backend:run
```

**Требуется Java 21.** При ошибке `UnsupportedClassVersionError` задайте `JAVA_HOME` на JDK 21 (например, `~/.gradle/jdks/eclipse_adoptium-21-*/jdk-*/Contents/Home` при auto-provisioning Gradle).

См. [Руководство по развертыванию](https://maxluxs.github.io/Flagent/languages/ru/guides/deployment.ru.md) для production настройки.

## 🎯 SDK и интеграции

### Подключение как зависимость (Kotlin/JVM)

Артефакты публикуются в [GitHub Packages](https://github.com/MaxLuxs/Flagent/packages). Добавьте репозиторий и зависимость:

**Gradle (Kotlin DSL):**

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/MaxLuxs/Flagent")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // Ktor plugin (сервер)
    implementation("com.flagent:ktor-flagent:0.1.6")
    // Kotlin client
    implementation("com.flagent:kotlin-client:0.1.6")
    // Kotlin Enhanced (offline eval, SSE)
    implementation("com.flagent:kotlin-enhanced:0.1.6")
    // Kotlin Debug UI
    implementation("com.flagent:kotlin-debug-ui:0.1.6")
    // Shared (KMP; подтягивается ktor-flagent или для multiplatform)
    implementation("com.flagent:shared:0.1.6")
}
```

Подставьте версию из корневого [`VERSION`](VERSION) или [Releases](https://github.com/MaxLuxs/Flagent/releases).

Публикуемые артефакты: `shared`, `ktor-flagent`, `kotlin-client`, `kotlin-enhanced`, `kotlin-debug-ui`, `flagent-koin`, `flagent-java-client` (Maven), `flagent-spring-boot-starter`. Версию см. в [релизах](https://github.com/MaxLuxs/Flagent/releases). Чтение: [GitHub PAT](https://github.com/settings/tokens) с `read:packages` или `GITHUB_TOKEN` в CI.

### SDK (все стабильны)
- **Kotlin:** [kotlin](sdk/kotlin), [kotlin-enhanced](sdk/kotlin-enhanced), [flagent-koin](sdk/flagent-koin)
- **JVM:** [java](sdk/java), [spring-boot-starter](sdk/spring-boot-starter)
- **JS/TS:** [javascript](sdk/javascript), [javascript-enhanced](sdk/javascript-enhanced)
- **Swift:** [swift](sdk/swift), [swift-enhanced](sdk/swift-enhanced)
- **Python:** [python](sdk/python) · **Go:** [go](sdk/go), [go-enhanced](sdk/go-enhanced)
- **Dart/Flutter:** [dart](sdk/dart), [flutter-enhanced](sdk/flutter-enhanced)
- **Сервер:** [Ktor Plugin](ktor-flagent)

### Debug UI
- [Kotlin Debug UI](sdk/kotlin-debug-ui) · [Swift Debug UI](sdk/swift-debug-ui) · [JavaScript Debug UI](sdk/javascript-debug-ui)

Примеры кода: [samples](samples/README.md) (Kotlin, Ktor, Spring Boot, Android, JS, Swift, Flutter и др.).

## 📌 Репозиторий

**GitHub topics** (настраиваются в настройках репозитория): `feature-flags`, `kotlin`, `kotlin-multiplatform`, `ab-testing`, `experimentation`, `launchdarkly-alternative`, `crash-reporting`, `feature-toggle`.

**Релизы:** см. [Releases](https://github.com/MaxLuxs/Flagent/releases). Чеклист к релизу — [Contributing](https://maxluxs.github.io/Flagent/guides/contributing.md) или внутренняя документация.

## 🤝 Участие в разработке

Форк → ветка → правки (по [стилю кода](https://maxluxs.github.io/Flagent/guides/contributing.md)) → тесты → PR. Подробнее: [Contributing](https://maxluxs.github.io/Flagent/guides/contributing.md), [Development](README.md#development).

## 🌍 Локализация

Flagent полностью локализован для СНГ рынка:

- ✅ **Русская документация** - Полная документация на русском языке
- ✅ **Русский UI** - Интерфейс admin панели на русском
- ✅ **Русская поддержка** - Поддержка на русском языке

## 📊 Roadmap

[Полный roadmap](docs/guides/roadmap.md) — Фаза 1 (Q1 2026) сделана (ядро, SDK, импорт/экспорт, Enterprise); фазы 2–4 в планах (CLI, вебхуки, .NET, Edge, audit, SaaS, AI, Terraform/K8s, SOC 2).

## 📄 Лицензия

Этот проект лицензирован под Apache License 2.0 - см. файл [LICENSE](LICENSE) для деталей.

## 💬 Поддержка

- 📖 [Документация](https://maxluxs.github.io/Flagent/languages/ru/guides/getting-started.ru.md) · [Примеры](samples/README.md) · 🐛 [Issues](https://github.com/MaxLuxs/Flagent/issues)
- 💝 [Спонсировать](https://github.com/sponsors/MaxLuxs) · 📧 max.developer.luxs@gmail.com

---

**Построен с ❤️ на Kotlin и Ktor**
