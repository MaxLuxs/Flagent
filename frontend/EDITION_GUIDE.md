# Flagent Edition Guide: Open Source vs Enterprise

## Overview

Flagent supports two editions:

1. **Open Source Edition** (default) - Free, self-hosted version with core features
2. **Enterprise Edition** - Paid version with advanced enterprise features

The split is implemented via feature flags on the frontend and a backend submodule.

---

## Architecture

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
├── backend/                      # Open Source backend
├── internal/                     # Optional private submodule
│   └── flagent-enterprise/       # Enterprise plugin (routes, services, billing, SSO, tenants)
│       ├── build.gradle.kts
│       └── src/main/kotlin/flagent/enterprise/
│           ├── route/            # BillingRoutes, SsoRoutes, TenantRoutes, MetricsRoutes, etc.
│           ├── service/          # BillingService, SsoService, TenantProvisioningService, etc.
│           └── ...
└── README.md
```

The frontend reads `window.ENV_EDITION` (or `?edition=open_source` / `?edition=enterprise` in the URL). The backend optionally loads the `:flagent-enterprise` module from `internal/flagent-enterprise` when the submodule is present.

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
        // All gated by isEnterprise in code (Metrics, Smart Rollout, Anomaly are Enterprise-only)
        val enableMetrics: Boolean = isEnterprise && ...
        val enableSmartRollout: Boolean = isEnterprise && ...
        val enableAnomalyDetection: Boolean = isEnterprise && ...
        val enableMultiTenancy: Boolean = isEnterprise && ...
        val enableSso: Boolean = isEnterprise && ...
        val enableBilling: Boolean = isEnterprise && ...
        val enableSlack: Boolean = isEnterprise && ...
    }
}
```

Additional config: `ENV_DEPLOYMENT_MODE` (`self_hosted` | `saas`) and `ENV_PLAN` (`open_source` | `enterprise` | `saas_enterprise` | `saas_lowprice`) control plan/tier when using SaaS deployment. `ENV_SHOW_LANDING` (`true` | `false`) shows the full marketing landing at `/` (Product, Pricing, Blog, Footer); auto `true` for SaaS, optional for self-hosted.

### Conditional UI

```kotlin
// Show component only in Enterprise
if (AppConfig.Features.enableMultiTenancy) {
    TenantsList()
}

// Or via badge
if (AppConfig.isEnterprise) {
    Span { Text("ENTERPRISE") }
}
```

---

## Configuration

### Open Source Edition (default)

**index.html or .env:**
```javascript
window.ENV_EDITION = "open_source";  // or omit entirely
```

You can also override edition via URL: `?edition=open_source` or `?edition=enterprise` (see `src/jsMain/resources/index.html`).

**Available features:**
- ✅ Flags CRUD
- ✅ Segments, Constraints, Variants
- ✅ Evaluation
- ✅ Real-time Updates (SSE)
- ❌ Metrics & Analytics (Enterprise only)
- ❌ Smart Rollout (AI-powered) (Enterprise only)
- ❌ Anomaly Detection (Enterprise only)
- ❌ Multi-Tenancy
- ❌ SSO/SAML
- ❌ Billing (Stripe)
- ❌ Slack Integration
- ❌ Advanced Analytics
- ❌ Audit Logs
- ❌ RBAC

### Authentication in Open Source (Enabled by Default)

By default, Open Source self-hosted **requires authentication** — unauthenticated users are redirected to /login.

| Component | Variable | Default |
|-----------|----------|---------|
| Frontend auth UI (redirect to /login) | `ENV_FEATURE_AUTH` | `true` |
| Backend admin login (POST /auth/login) | `FLAGENT_ADMIN_AUTH_ENABLED` | `true` |
| JWT for API protection | `FLAGENT_JWT_AUTH_ENABLED` | `false` |

**Required for auth (backend env):**
- `FLAGENT_ADMIN_EMAIL=admin@example.com`
- `FLAGENT_ADMIN_PASSWORD=your-secure-password`
- `FLAGENT_JWT_AUTH_SECRET=your-secret-min-32-chars` (required for JWT tokens)

**To disable auth** (open access, dev only):
- Backend: `FLAGENT_ADMIN_AUTH_ENABLED=false`
- Frontend: `window.ENV_FEATURE_AUTH = "false"` or `?ENV_FEATURE_AUTH=false`

**Optional:** `FLAGENT_JWT_AUTH_ENABLED=true` — protects API routes with JWT; evaluation endpoints can stay whitelisted via `FLAGENT_JWT_AUTH_WHITELIST_PATHS`.

See [docs/guides/configuration.md](../docs/guides/configuration.md) for full Admin Auth and JWT options.

### Enterprise Edition

**index.html or .env:**
```javascript
window.ENV_EDITION = "enterprise";
```

**Available features:**
- ✅ All Open Source features
- ✅ Multi-Tenancy
- ✅ SSO/SAML (OAuth, OIDC)
- ✅ Billing (Stripe)
- ✅ Slack Integration
- ✅ Advanced Analytics
- ✅ Audit Logs
- ✅ RBAC

**API key (required):** Enterprise backend requires `X-API-Key` header for `/api/v1/*` routes. Configure via:
1. **ENV_API_KEY** in index.html: `window.ENV_API_KEY = "your-api-key"`
2. **localStorage** after creating a tenant: the API key is stored automatically when you create a tenant via the UI
3. **Manual setup:** `curl -X POST http://localhost:18000/admin/tenants -H "Content-Type: application/json" -d '{"key":"dev","name":"Dev","plan":"STARTER","ownerEmail":"dev@local"}'` — copy `apiKey` from the response

4. **Dev mode (backend env, local only):** set both `FLAGENT_DEV_MODE=true` and `FLAGENT_DEV_SKIP_TENANT_AUTH=true` when starting the backend — X-API-Key becomes optional; the first active tenant is used as fallback. Useful for local debugging without removing the enterprise submodule. Create a tenant via POST /admin/tenants first. **SECURITY: NEVER use in production.** Both vars required to prevent accidental enablement.

### First run (Enterprise, with admin auth)

When admin auth is enabled (`FLAGENT_ADMIN_AUTH_ENABLED=true`):

1. **Backend:** Set `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD` (or `FLAGENT_ADMIN_PASSWORD_HASH`), optionally `FLAGENT_ADMIN_API_KEY`, and `FLAGENT_JWT_AUTH_SECRET`. See [docs/guides/configuration.md](../docs/guides/configuration.md) → Admin Auth.
2. **Open UI** → if required, you will be prompted to **Login** (admin email/password). Alternatively use **Admin API Key** in settings or `ENV_ADMIN_API_KEY` for API-only access.
3. Go to **Tenants** → **Create first tenant** (or use "Create first tenant" from error messages). Save the returned **API key** (it is stored in localStorage automatically when created via UI).
4. Use that API key for all `/api/v1/*` requests (flags, segments, evaluation). The UI sends it as `X-API-Key` when set.

Without admin auth, `/admin/tenants` remains open (anyone who can reach the server can create tenants). Enable admin auth in production.

---

## Backend: Private Submodule

### Setup Git Submodule

The Enterprise plugin lives in a private submodule at `internal/flagent-enterprise`. The public repo uses `internal/` for optional submodules.

```bash
# Clone with submodule (from your org's clone URL)
git clone --recursive git@github.com:YourOrg/flagent.git

# Or after cloning
git submodule update --init --recursive
```

### Structure with Submodule

```
flagent/                          # Public repo
├── frontend/                     # Public frontend (supports both editions)
├── backend/                      # Open Source backend (base functionality)
├── internal/                     # Optional private submodule(s)
│   └── flagent-enterprise/       # Enterprise plugin (:flagent-enterprise)
│       ├── .git                  # Link to private repository
│       ├── build.gradle.kts
│       └── src/main/kotlin/flagent/enterprise/
│           ├── route/            # BillingRoutes, SsoRoutes, TenantRoutes, MetricsRoutes, etc.
│           ├── service/          # BillingService, SsoService, TenantProvisioningService, etc.
│           └── ...
└── settings.gradle.kts           # Conditionally includes :flagent-enterprise when present
```

### Gradle Configuration

**settings.gradle.kts:**
```kotlin
include(":shared")
include(":backend")
include(":frontend")

// Enterprise module (optional, when internal/flagent-enterprise submodule is present)
if (file("internal/flagent-enterprise/build.gradle.kts").exists()) {
    include(":flagent-enterprise")
    project(":flagent-enterprise").projectDir = file("internal/flagent-enterprise")
}
```

**backend/build.gradle.kts:**
```kotlin
dependencies {
    implementation(project(":shared"))
    // Enterprise plugin loaded at runtime when present
    if (project.findProject(":flagent-enterprise") != null) {
        runtimeOnly(project(":flagent-enterprise"))
    }
    // ...
}
```

The backend discovers the Enterprise plugin via `META-INF/services` (e.g. `EnterpriseConfigurator`). No hardcoded edition check in Application.kt is required for route registration; the plugin registers its routes when present.

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

Run from the **repository root**:

```bash
# Clone repository
git clone git@github.com:YourOrg/flagent.git
cd flagent

# Build frontend
./gradlew :frontend:build

# Run backend (Open Source)
./gradlew :backend:run
```

### Enterprise Development

Run from the **repository root** (there is no `gradlew` in `frontend/`):

```bash
# Clone with submodule
git clone --recursive git@github.com:YourOrg/flagent.git
cd flagent

# Update submodule
git submodule update --remote internal/flagent-enterprise

# Build frontend (with Enterprise support)
./gradlew :frontend:build

# Run backend (Enterprise: loads :flagent-enterprise when submodule is present)
./gradlew :backend:run
```

Edition can be switched in the browser via `?edition=enterprise` or `?edition=open_source` (see `index.html`).

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
| Real-time Updates (SSE) | ✅ | ✅ |
| **Metrics & Analytics** | ❌ | ✅ |
| **Smart Rollout (AI)** | ❌ | ✅ |
| **Anomaly Detection** | ❌ | ✅ |
| **Multi-Tenancy** | ❌ | ✅ |
| **SSO/SAML** | ❌ | ✅ |
| **Billing (Stripe)** | ❌ | ✅ |
| **Slack Integration** | ❌ | ✅ |
| **Advanced Analytics** | ❌ | ✅ |
| **Audit Logs** | ❌ | ✅ |
| **RBAC** | ❌ | ✅ |
| **Crash Analytics** | ❌ | ✅ |
| **Webhooks** | ❌ | ✅ |
| **GitOps (YAML/CLI)** | ❌ | ✅ |

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

### Q: Can I enable Enterprise features in the Open Source version?

**A:** No. Enterprise features are disabled when `ENV_EDITION != "enterprise"`. This is controlled via `AppConfig.isEnterprise`.

### Q: How do I update the Enterprise submodule?

**A:**
```bash
cd internal/flagent-enterprise
git pull origin main
cd ../..
git add internal
git commit -m "Update enterprise submodule"
```

### Q: What if the enterprise submodule is not initialized?

**A:** The frontend continues to work in Open Source mode. Enterprise features are hidden; core features (flags, segments, evaluation, real-time) remain available.

### Q: Can I build a single Docker image for both editions?

**A:** Yes, but separate images are recommended:
- `flagent/flagent:latest` - Open Source
- `flagent/flagent-enterprise:latest` - Enterprise

This simplifies licensing and deployment.

---

## License

- **Open Source Edition**: MIT License (flagent/)
- **Enterprise Edition**: Commercial License (flagent-enterprise/, private repository)

---

## Summary

Flagent uses **compile-time and runtime feature flags** to separate Open Source and Enterprise editions:

1. **Frontend** - Single codebase, conditional UI via `AppConfig.Features`
2. **Backend** - Open Source in the public repo, Enterprise in a private submodule
3. **Configuration** - Via `ENV_EDITION` and `FLAGENT_EDITION`
4. **Deployment** - Separate Docker images per edition

This allows:
- ✅ Maintaining both editions from one frontend codebase
- ✅ Keeping Enterprise code in a private repository
- ✅ Switching editions via environment variables
- ✅ Preventing accidental enablement of Enterprise features in Open Source
