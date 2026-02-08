<p align="center"><strong><a href="README.md">English</a></strong> | Русский</p>

# Flagent

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

**Flagent** — это современная, production-ready платформа для feature flags и экспериментов, построенная на **Kotlin/Ktor**. Первое Kotlin-native решение в экосистеме feature flags, сочетающее type-safety, coroutines и чистую архитектуру для высокопроизводительного управления функциональностью с AI-возможностями.

## 🎯 Почему Flagent?

### Kotlin-Native превосходство
- **Industry-Standard Evaluation API** - Простая миграция с существующих feature flag решений
- **Type-Safe SDK** - Валидация на этапе компиляции и автодополнение в IDE
- **Coroutine-First** - Неблокирующий I/O и структурированная конкурентность
- **Ktor Ecosystem** - Бесшовная интеграция с Ktor приложениями
- **Clean Architecture** - DDD принципы и тестируемый дизайн

### AI-возможности (в планах)
- **Smart Rollouts** - ML-based автоматическое масштабирование на основе метрик
- **Anomaly Detection** - Авто-откат при деградации производительности
- **Predictive Targeting** - Рекомендации сегментов на основе данных
- **A/B Insights** - Автоматический анализ экспериментов

### Enterprise-Ready
- **Расширенное тестовое покрытие** - 200+ тестовых файлов
- **Высокая производительность** - Низкая задержка evaluation, нагрузочное тестирование (см. [docs/performance/benchmarks.md](docs/performance/benchmarks.md))
- **Multi-Tenancy** - Изолированные окружения для команд (в планах)
- **Real-Time обновления** - SSE в Kotlin Enhanced, Go Enhanced

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

### Функции в планах
- ✅ **Client-Side Evaluation** - Kotlin Enhanced, Go Enhanced (offline-first local evaluator)
- ✅ **Real-Time обновления** - SSE в Kotlin Enhanced, Go Enhanced
- 🚧 **GitOps поддержка** - Feature flags as code (YAML/JSON) (Q2 2026)
- 🚧 **Multi-Tenancy** - Изоляция команд и лимиты ресурсов (Q3 2026)
- 🚧 **SSO/SAML** - Enterprise аутентификация (Auth0, Okta) (Q3 2026)
- 🚧 **RBAC** - Role-based access control (Q3 2026)
- 🚧 **AI Rollouts** - ML-powered постепенные rollout (Q4 2026)

## 📖 Документация

- 📖 **[Начало работы](https://maxluxs.github.io/Flagent/guides/getting-started.ru.md)** - Быстрый старт и настройка
- 📖 **[Совместимость API](https://maxluxs.github.io/Flagent/guides/compatibility.md)** - Evaluation API, руководство по миграции
- 📖 **[API Reference](https://maxluxs.github.io/Flagent)** - Документация API
- 📖 **[OpenAPI спецификация](https://maxluxs.github.io/Flagent/api/openapi.yaml)** - OpenAPI YAML
- 📖 **[Архитектура](https://maxluxs.github.io/Flagent/architecture/backend.md)** - Дизайн системы
- 📖 **[Конфигурация](https://maxluxs.github.io/Flagent/guides/configuration.ru.md)** - Переменные окружения
- 📖 **[Развертывание](https://maxluxs.github.io/Flagent/guides/deployment.ru.md)** - Production развертывание
- 📖 **[Сценарии использования](https://maxluxs.github.io/Flagent/guides/use-cases.ru.md)** - Примеры

## 🏗️ Структура проекта

```
flagent/
├── backend/          # Ktor backend (Clean Architecture)
├── frontend/         # Compose for Web UI
├── sdk/              # Client SDKs (Kotlin, JS, Swift, Python, Go)
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

См. [Руководство по развертыванию](https://maxluxs.github.io/Flagent/guides/deployment.ru.md) для production настройки.

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
    implementation("com.flagent:ktor-flagent:0.1.5")
    // Kotlin client
    implementation("com.flagent:kotlin-client:0.1.5")
    // Kotlin Enhanced (offline eval, SSE)
    implementation("com.flagent:kotlin-enhanced:0.1.5")
    // Kotlin Debug UI
    implementation("com.flagent:kotlin-debug-ui:0.1.5")
    // Shared (KMP; подтягивается ktor-flagent или для multiplatform)
    implementation("com.flagent:shared:0.1.5")
}
```

Публикуемые артефакты: `shared` (KMP: root + `shared-jvm`, `shared-js`), `ktor-flagent`, `kotlin-client`, `kotlin-enhanced`, `kotlin-debug-ui`. Для чтения используйте [GitHub PAT](https://github.com/settings/tokens) с правом `read:packages` (или `GITHUB_TOKEN` в CI). Версию замените на актуальную из [релизов](https://github.com/MaxLuxs/Flagent/releases).

### Backend SDK (Доступны)
- **[Kotlin SDK](sdk/kotlin)** - Type-safe Kotlin client + Enhanced вариант
- **[JavaScript/TypeScript SDK](sdk/javascript)** - Node.js/Browser support + Enhanced вариант
- **[Ktor Plugin](ktor-flagent)** - Интеграция первого класса для Ktor серверов

### Mobile SDK (Доступны)
- **[Swift SDK](sdk/swift)** - iOS/macOS нативный клиент + Enhanced вариант

### Дополнительные SDK (Доступны)
- **[Python SDK](sdk/python)** - Asyncio, типизированный client
- **[Go SDK](sdk/go)** + **[Go Enhanced](sdk/go-enhanced)** - goroutines, client-side eval, SSE

### Debug Tools (Доступны)
- **[Kotlin Debug UI](sdk/kotlin-debug-ui)** - Встроенная панель отладки
- **[Swift Debug UI](sdk/swift-debug-ui)** - Нативные SwiftUI инструменты отладки
- **[JavaScript Debug UI](sdk/javascript-debug-ui)** - React-based консоль отладки

## 🔧 Пример использования

### Kotlin
```kotlin
val client = FlagentClient.create(
    baseUrl = "http://localhost:18000/api/v1",
    apiKey = "your-api-key"
)

// Простая проверка флага
if (client.isEnabled("new_payment_flow")) {
    newPaymentSystem.process()
} else {
    legacyPaymentSystem.process()
}

// A/B тестирование
val variant = client.evaluate(
    flagKey = "checkout_experiment",
    entityContext = mapOf("user_id" to userId)
)?.variant

when (variant) {
    "control" -> showOldCheckout()
    "variant_a" -> showNewCheckoutA()
    "variant_b" -> showNewCheckoutB()
}
```

### JavaScript/TypeScript
```javascript
import { FlagentClient } from '@flagent/client';

const client = new FlagentClient({
  baseUrl: 'http://localhost:18000/api/v1',
  apiKey: 'your-api-key'
});

// Простая проверка флага
if (await client.isEnabled('new_payment_flow')) {
  newPaymentSystem.process();
} else {
  legacyPaymentSystem.process();
}
```

### Swift
```swift
let client = FlagentClient(
    baseURL: "http://localhost:18000/api/v1",
    apiKey: "your-api-key"
)

// Простая проверка флага
if try await client.isEnabled("new_payment_flow") {
    newPaymentSystem.process()
} else {
    legacyPaymentSystem.process()
}
```

## 🤝 Участие в разработке

Мы приветствуем вклад в проект! Пожалуйста, см. наше руководство по участию:

1. Форкните репозиторий
2. Создайте вашу feature ветку (`git checkout -b feature/amazing-feature`)
3. Закоммитьте ваши изменения (`git commit -m 'Add some amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

Для более подробной информации, см. [Настройка разработки](README.md#development).

## 🌍 Локализация

Flagent полностью локализован для СНГ рынка:

- ✅ **Русская документация** - Полная документация на русском языке
- ✅ **Русский UI** - Интерфейс admin панели на русском
- ✅ **Русская поддержка** - Поддержка на русском языке

## 📊 Roadmap

См. наш детальный [Roadmap](docs/guides/roadmap.md) для полного видения проекта.

### Фаза 1: Foundation (Q1 2026)
- ✅ Client-side evaluation (Go Enhanced, Kotlin Enhanced)
- ✅ Real-time обновления (SSE) в Go Enhanced, Kotlin Enhanced
- ✅ Python и Go SDK + Go Enhanced
- 🚧 Kubernetes Helm чарты

### Фаза 2: Community (Q2-Q3 2026)
- Feature Flags as Code (GitOps)
- CLI инструмент
- Webhooks и интеграции
- Edge Service для масштабирования

### Фаза 3: Enterprise (Q3-Q4 2026)
- Multi-tenancy архитектура
- SSO/SAML
- RBAC
- Audit logs
- AI-powered rollouts (MVP)

## 📄 Лицензия

Этот проект лицензирован под Apache License 2.0 - см. файл [LICENSE](LICENSE) для деталей.

## 💬 Сообщество и поддержка

- 💝 **[Поддержать проект](https://github.com/sponsors/MaxLuxs)** - Спонсорская поддержка разработки Flagent
- 🐛 **[GitHub Issues](https://github.com/MaxLuxs/Flagent/issues)** - Вопросы, баги, запросы функций
- 📚 **[Документация](https://maxluxs.github.io/Flagent/guides/getting-started.ru.md)** - Руководства и API
- 💻 **[Примеры](samples)** - Примеры кода и туториалы
- 📧 **Поддержка:** max.developer.luxs@gmail.com

## ⭐ Звезды

Если вам нравится Flagent, поставьте звезду на GitHub! Это помогает другим разработчикам найти проект.

---

**Построен с ❤️ на Kotlin и Ktor**
