# Flagent Edition Guide: Open Source vs Enterprise

## Overview

Flagent поддерживает две версии (editions):

1. **Open Source Edition** (по умолчанию) - бесплатная, self-hosted версия с core функциями
2. **Enterprise Edition** - платная версия с расширенными enterprise features

Разделение реализовано через feature flags на frontend и backend сабмодуль.

---

## Архитектура разделения

```
flagent/                          # Public repository (Open Source + Frontend)
├── frontend/                     # Kotlin/JS frontend (public)
│   ├── src/
│   │   └── jsMain/
│   │       ├── kotlin/
│   │       │   └── flagent/frontend/
│   │       │       ├── config/
│   │       │       │   └── AppConfig.kt  # Edition detection
│   │       │       └── components/       # All UI components
│   │       └── resources/
│   │           └── index.html            # ENV_EDITION config
│   └── build.gradle.kts
├── backend/                      # Open Source backend (or stub)
└── README.md

flagent-enterprise/               # Private submodule (Enterprise Backend)
├── backend/                      # Full Enterprise backend
│   ├── src/
│   │   └── main/kotlin/flagent/
│   │       ├── service/
│   │       │   ├── BillingService.kt
│   │       │   ├── SsoService.kt
│   │       │   └── TenantProvisioningService.kt
│   │       └── route/
│   │           ├── BillingRoutes.kt
│   │           ├── SsoRoutes.kt
│   │           └── TenantRoutes.kt
│   └── build.gradle.kts
└── README.md
```

---

## Frontend: Edition Detection

### AppConfig.kt

```kotlin
enum class Edition {
    OPEN_SOURCE,  // Free, self-hosted
    ENTERPRISE    // Paid, with advanced features
}

object AppConfig {
    val edition: Edition by lazy {
        when ((js("window.ENV_EDITION") as? String)?.lowercase()) {
            "enterprise" -> Edition.ENTERPRISE
            else -> Edition.OPEN_SOURCE
        }
    }
    
    val isEnterprise: Boolean get() = edition == Edition.ENTERPRISE
    val isOpenSource: Boolean get() = edition == Edition.OPEN_SOURCE
    
    object Features {
        // Open Source Features (available in both)
        val enableMetrics: Boolean = true
        val enableSmartRollout: Boolean = true
        val enableAnomalyDetection: Boolean = true
        
        // Enterprise-Only Features
        val enableMultiTenancy: Boolean = isEnterprise && ...
        val enableSso: Boolean = isEnterprise && ...
        val enableBilling: Boolean = isEnterprise && ...
        val enableSlack: Boolean = isEnterprise && ...
    }
}
```

### Условное отображение UI

```kotlin
// Показывать компонент только в Enterprise
if (AppConfig.Features.enableMultiTenancy) {
    TenantsList()
}

// Или через badge
if (AppConfig.isEnterprise) {
    Span { Text("ENTERPRISE") }
}
```

---

## Configuration

### Open Source Edition (default)

**index.html или .env:**
```javascript
window.ENV_EDITION = "open_source";  // или не указывать вообще
```

**Доступные features:**
- ✅ Flags CRUD
- ✅ Segments, Constraints, Variants
- ✅ Evaluation
- ✅ Metrics & Analytics
- ✅ Smart Rollout (AI-powered)
- ✅ Anomaly Detection
- ✅ Real-time Updates
- ❌ Multi-Tenancy
- ❌ SSO/SAML
- ❌ Billing (Stripe)
- ❌ Slack Integration
- ❌ Advanced Analytics
- ❌ Audit Logs
- ❌ RBAC

### Enterprise Edition

**index.html или .env:**
```javascript
window.ENV_EDITION = "enterprise";
```

**Доступные features:**
- ✅ Все Open Source features
- ✅ Multi-Tenancy
- ✅ SSO/SAML (OAuth, OIDC)
- ✅ Billing (Stripe)
- ✅ Slack Integration
- ✅ Advanced Analytics
- ✅ Audit Logs
- ✅ RBAC

---

## Backend: Приватный сабмодуль

### Setup Git Submodule

```bash
# В public repository (flagent/)
git submodule add git@github.com:YourOrg/flagent-enterprise.git enterprise

# Клонирование с сабмодулем
git clone --recursive git@github.com:YourOrg/flagent.git

# Или после клонирования
git submodule update --init --recursive
```

### Структура с сабмодулем

```
flagent/                          # Public repo
├── frontend/                     # Public frontend (поддерживает обе версии)
├── backend/                      # Open Source backend (базовая функциональность)
├── enterprise/                   # Private submodule (Enterprise backend)
│   ├── .git                      # Ссылка на приватный репозиторий
│   ├── backend/
│   │   └── src/main/kotlin/
│   │       └── flagent/enterprise/
│   │           ├── service/      # Enterprise services
│   │           └── route/        # Enterprise routes
│   └── README.md
└── settings.gradle.kts           # Условное подключение enterprise модуля
```

### Gradle Configuration

**settings.gradle.kts:**
```kotlin
rootProject.name = "flagent"

include(":frontend")
include(":backend")

// Условно подключаем enterprise модуль, если он существует
if (file("enterprise/backend").exists()) {
    include(":enterprise-backend")
    project(":enterprise-backend").projectDir = file("enterprise/backend")
}
```

**backend/build.gradle.kts:**
```kotlin
dependencies {
    // Базовые зависимости для Open Source
    implementation("io.ktor:ktor-server-core:$ktor_version")
    // ...
    
    // Условно подключаем Enterprise модуль
    if (project.findProject(":enterprise-backend") != null) {
        implementation(project(":enterprise-backend"))
    }
}
```

### Backend: Edition Detection

**backend/src/main/kotlin/flagent/config/AppConfig.kt:**
```kotlin
object AppConfig {
    val edition: Edition = when (System.getenv("FLAGENT_EDITION")?.lowercase()) {
        "enterprise" -> Edition.ENTERPRISE
        else -> Edition.OPEN_SOURCE
    }
    
    val multiTenancyEnabled: Boolean = 
        edition == Edition.ENTERPRISE && 
        System.getenv("FLAGENT_MULTI_TENANCY_ENABLED")?.toBoolean() ?: true
        
    val stripeEnabled: Boolean = 
        edition == Edition.ENTERPRISE && 
        System.getenv("FLAGENT_STRIPE_ENABLED")?.toBoolean() ?: false
}
```

**backend/src/main/kotlin/flagent/application/Application.kt:**
```kotlin
fun Application.module() {
    // Core routes (всегда доступны)
    configureFlagRoutes(flagService)
    configureEvaluationRoutes(evaluationService)
    
    // Enterprise routes (только если модуль подключен)
    if (AppConfig.edition == Edition.ENTERPRISE) {
        configureBillingRoutes(billingService)
        tenantRoutes(tenantProvisioningService)
        ssoRoutes(ssoService)
    }
}
```

---

## Deployment

### Open Source Deployment

**Docker Compose:**
```yaml
version: '3.8'
services:
  flagent:
    image: flagent/flagent:latest
    environment:
      - FLAGENT_EDITION=open_source
      - FLAGENT_DB_DBDRIVER=postgres
      - FLAGENT_DB_DBCONNECTIONSTR=...
    ports:
      - "18000:18000"
```

**Frontend:**
```html
<script>
  window.ENV_EDITION = "open_source";
</script>
```

### Enterprise Deployment

**Docker Compose:**
```yaml
version: '3.8'
services:
  flagent:
    image: flagent/flagent-enterprise:latest
    environment:
      - FLAGENT_EDITION=enterprise
      - FLAGENT_MULTI_TENANCY_ENABLED=true
      - FLAGENT_STRIPE_ENABLED=true
      - FLAGENT_STRIPE_SECRET_KEY=sk_live_...
      - FLAGENT_SSO_ENABLED=true
      - FLAGENT_SLACK_ENABLED=true
    ports:
      - "18000:18000"
```

**Frontend:**
```html
<script>
  window.ENV_EDITION = "enterprise";
</script>
```

---

## Development Workflow

### Open Source Development

```bash
# Clone repository
git clone git@github.com:YourOrg/flagent.git
cd flagent

# Build frontend
cd flagent/frontend
./gradlew build

# Run backend (Open Source)
cd ../backend
./gradlew run
```

### Enterprise Development

```bash
# Clone with submodule
git clone --recursive git@github.com:YourOrg/flagent.git
cd flagent

# Update submodule
git submodule update --remote enterprise

# Build frontend (с Enterprise поддержкой)
cd flagent/frontend
./gradlew build

# Run backend (Enterprise)
cd ../enterprise/backend
export FLAGENT_EDITION=enterprise
./gradlew run
```

---

## Feature Flag Matrix

| Feature | Open Source | Enterprise |
|---------|------------|------------|
| Flags CRUD | ✅ | ✅ |
| Segments & Constraints | ✅ | ✅ |
| Variants & Distributions | ✅ | ✅ |
| Evaluation (single & batch) | ✅ | ✅ |
| Flag History (Snapshots) | ✅ | ✅ |
| Tags | ✅ | ✅ |
| Export | ✅ | ✅ |
| Debug Console | ✅ | ✅ |
| Metrics & Analytics | ✅ | ✅ |
| Smart Rollout (AI) | ✅ | ✅ |
| Anomaly Detection | ✅ | ✅ |
| Real-time Updates (SSE) | ✅ | ✅ |
| **Multi-Tenancy** | ❌ | ✅ |
| **SSO/SAML** | ❌ | ✅ |
| **Billing (Stripe)** | ❌ | ✅ |
| **Slack Integration** | ❌ | ✅ |
| **Advanced Analytics** | ❌ | ✅ |
| **Audit Logs** | ❌ | ✅ |
| **RBAC** | ❌ | ✅ |

---

## Environment Variables

### Open Source

```bash
# Edition
FLAGENT_EDITION=open_source

# Frontend
ENV_EDITION=open_source
ENV_API_BASE_URL=http://localhost:18000
ENV_FEATURE_METRICS=true
ENV_FEATURE_SMART_ROLLOUT=true
ENV_FEATURE_ANOMALY_DETECTION=true
```

### Enterprise

```bash
# Edition
FLAGENT_EDITION=enterprise

# Frontend
ENV_EDITION=enterprise
ENV_API_BASE_URL=http://localhost:18000

# Enterprise Features
FLAGENT_MULTI_TENANCY_ENABLED=true
FLAGENT_STRIPE_ENABLED=true
FLAGENT_STRIPE_SECRET_KEY=sk_live_...
FLAGENT_SSO_ENABLED=true
FLAGENT_SLACK_ENABLED=true
FLAGENT_SLACK_WEBHOOK_URL=https://hooks.slack.com/...
```

---

## FAQ

### Q: Можно ли включить Enterprise features в Open Source версии?

**A:** Нет. Enterprise features автоматически отключаются, если `ENV_EDITION != "enterprise"`. Это контролируется через `AppConfig.isEnterprise`.

### Q: Как обновить Enterprise backend?

**A:** 
```bash
cd flagent/enterprise
git pull origin main
cd ..
git add enterprise
git commit -m "Update enterprise submodule"
```

### Q: Что если enterprise сабмодуль не инициализирован?

**A:** Frontend продолжит работать в Open Source режиме. Enterprise features будут скрыты, но все core features доступны.

### Q: Можно ли собрать один Docker образ для обеих версий?

**A:** Да, но лучше разделить:
- `flagent/flagent:latest` - Open Source
- `flagent/flagent-enterprise:latest` - Enterprise

Это упрощает licensing и deployment.

---

## License

- **Open Source Edition**: MIT License (flagent/)
- **Enterprise Edition**: Commercial License (flagent-enterprise/, приватный репозиторий)

---

## Summary

Flagent использует **compile-time и runtime feature flags** для разделения Open Source и Enterprise версий:

1. **Frontend** - единый код, условное отображение через `AppConfig.Features`
2. **Backend** - Open Source в публичном репозитории, Enterprise в приватном сабмодуле
3. **Конфигурация** - через `ENV_EDITION` и `FLAGENT_EDITION`
4. **Deployment** - отдельные Docker образы для каждой версии

Это позволяет:
- ✅ Поддерживать обе версии из одного codebase (frontend)
- ✅ Защитить Enterprise код в приватном репозитории
- ✅ Легко переключаться между версиями через environment variables
- ✅ Предотвратить случайное включение Enterprise features в Open Source
