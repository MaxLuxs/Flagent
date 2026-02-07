<div align="center">
  <p><strong>English</strong> | <a href="README.ru.md">Русский</a></p>
  <h1>Flagent</h1>
  <p><strong>The First Kotlin-Native Feature Flag Platform</strong></p>
  <p>Type-safe, coroutine-first feature flags and experimentation with AI-powered rollouts</p>
  
  <p>
    <a href="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml?query=branch%3Amain+">
      <img src="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml/badge.svg?branch=main" alt="CI">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/actions/workflows/ci-swift.yml?query=branch%3Amain+">
      <img src="https://github.com/MaxLuxs/Flagent/actions/workflows/ci-swift.yml/badge.svg?branch=main" alt="CI (Swift)">
    </a>
    <a href="https://codecov.io/gh/MaxLuxs/Flagent">
      <img src="https://codecov.io/gh/MaxLuxs/Flagent/branch/main/graph/badge.svg" alt="Code Coverage">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/releases">
      <img src="https://img.shields.io/github/release/MaxLuxs/Flagent.svg?style=flat&color=green" alt="Release">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/blob/main/LICENSE">
      <img src="https://img.shields.io/badge/license-Apache%202.0-green.svg" alt="License">
    </a>
  </p>
  
  <p>
    <a href="https://github.com/MaxLuxs/Flagent/stargazers">
      <img src="https://img.shields.io/github/stars/MaxLuxs/Flagent?style=social" alt="GitHub Stars">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/network/members">
      <img src="https://img.shields.io/github/forks/MaxLuxs/Flagent?style=social" alt="GitHub Forks">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/graphs/contributors">
      <img src="https://img.shields.io/github/contributors/MaxLuxs/Flagent" alt="Contributors">
    </a>
  </p>
  
  <p>
    <a href="#-quick-start">Quick Start</a> •
    <a href="https://maxluxs.github.io/Flagent/guides/getting-started.md">Documentation</a> •
    <a href="#-key-features">Features</a> •
    <a href="#-sdks">SDKs</a> •
    <a href="#-use-cases">Use Cases</a> •
    <a href="docs/guides/roadmap.md">Roadmap</a>
  </p>
</div>

---

**Flagent** is a modern, production-ready feature flag and experimentation platform built with **Kotlin/Ktor**. The first Kotlin-native solution in the feature flags ecosystem, combining type-safety, coroutines, and clean architecture to deliver high-performance feature management with AI-powered capabilities.

## 🎯 Why Flagent?

### Kotlin-Native Excellence
- **Industry-Standard Evaluation API** - Easy migration from existing feature flag solutions
- **Type-Safe SDKs** - Compile-time validation and IDE autocomplete
- **Coroutine-First** - Non-blocking I/O and structured concurrency
- **Ktor Ecosystem** - Seamless integration with Ktor applications
- **Clean Architecture** - DDD principles and testable design

### AI-Powered Intelligence (Roadmap)
- **Smart Rollouts** - ML-based automatic scaling based on metrics
- **Anomaly Detection** - Auto-rollback on performance degradation  
- **Predictive Targeting** - Data-driven segment recommendations
- **A/B Insights** - Automated experiment analysis

### Enterprise-Ready
- **Extensive test coverage** - 200+ test files
- **High Performance** - Low latency evaluation, load-tested (see [docs/performance/benchmarks.md](docs/performance/benchmarks.md))
- **Multi-Tenancy** - Isolated environments for teams (Roadmap)
- **Real-Time Updates** - SSE in Kotlin Enhanced, Go Enhanced

## 🚀 Quick Start

Get Flagent running in under 5 minutes:

```bash
# Using Docker (Recommended)
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent

# Open Flagent UI
open http://localhost:18000
```

Configure admin auth via `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD`, `FLAGENT_JWT_AUTH_SECRET`. See [docs/guides/configuration.md](docs/guides/configuration.md).

## Self-Hosted (Open Source) from GitHub

Run backend and frontend from the public repo (no Enterprise submodule = single-tenant, no X-API-Key).

**1. Clone (without internal submodule):**
```bash
git clone https://github.com/MaxLuxs/Flagent.git flagent && cd flagent
# Do not run: git submodule update --init internal  (that would pull Enterprise)
```

**2. Run backend and frontend together (dev):**
```bash
./gradlew run
```
- Backend: http://localhost:18000 (API, health, Swagger at /docs)
- **Java 21 required.** If you see `UnsupportedClassVersionError`, set `JAVA_HOME` to JDK 21 (e.g. `~/.gradle/jdks/eclipse_adoptium-21-*/jdk-*/Contents/Home` when Gradle auto-provisions it)
- Frontend: http://localhost:8080 (Compose dev server; uses `ENV_API_BASE_URL` → 18000)
- Requires `org.gradle.parallel=true` (default in `gradle.properties`). Ctrl+C stops both.

**3. Or run separately:**
```bash
# Terminal 1 – backend
./gradlew :backend:run

# Terminal 2 – frontend
./gradlew :frontend:jsBrowserDevelopmentRun
```
Then open http://localhost:8080. Frontend defaults to edition `open_source` and `ENV_API_BASE_URL=http://localhost:18000` in `frontend/src/jsMain/resources/index.html`.

**4. Production-like (single process):** build frontend so backend can serve static files, then run backend from repo root:
```bash
./gradlew :frontend:jsBrowserDevelopmentWebpack
./gradlew :backend:run
```
Backend serves the UI from `frontend/build/dist/js/developmentExecutable` when present; open http://localhost:18000.

**Docker:** The image at `ghcr.io/maxluxs/flagent` is backend-only (no UI in image). For full self-hosted UI, use option 2 or 3 above, or build frontend and mount it into a custom image.

**With Enterprise (internal submodule):** Admin login and protected `/admin/*` are available. See [frontend/EDITION_GUIDE.md](frontend/EDITION_GUIDE.md) for first run (admin env → login → create tenant) and [docs/guides/configuration.md](docs/guides/configuration.md) for Admin Auth variables.

**OSS frontend + Enterprise backend:** If you build the frontend as Open Source (default) but run the backend with `internal` (enterprise), you will get 401 until a tenant exists. The UI shows "Create tenant first: POST /admin/tenants" and two actions: **Create first tenant** (opens /tenants) and **Log in (admin)** (opens /login). Set on the backend: `FLAGENT_ADMIN_AUTH_ENABLED=true`, `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD`, `FLAGENT_JWT_AUTH_SECRET` (min 32 chars), and optionally `FLAGENT_ADMIN_API_KEY`. Then log in at /login or create a tenant at /tenants.

**"Admin auth is disabled"** when logging in means the backend has admin login turned off. Set `FLAGENT_ADMIN_AUTH_ENABLED=true` and the env vars above on the backend and restart. See [docs/guides/configuration.md](docs/guides/configuration.md) → Admin Auth.

**Login first (always show login screen):** In the frontend set `ENV_FEATURE_AUTH=true` (e.g. in `frontend/src/jsMain/resources/index.html` or via `?ENV_FEATURE_AUTH=true`). Unauthenticated users will be redirected to /login before seeing the dashboard.

**Auth in Open Source:** Auth is enabled by default. Configure `FLAGENT_ADMIN_EMAIL`, `FLAGENT_ADMIN_PASSWORD`, `FLAGENT_JWT_AUTH_SECRET` (min 32 chars) on the backend. To disable (open access, dev only): `FLAGENT_ADMIN_AUTH_ENABLED=false` and `ENV_FEATURE_AUTH=false` (frontend). See [frontend/EDITION_GUIDE.md](frontend/EDITION_GUIDE.md) → Authentication in Open Source.

## ✨ Key Features

### Core Features (Available Now)
- ✅ **Feature Flags** - Gradual rollouts, kill switches, and remote configuration
- ✅ **A/B Testing** - Multi-variant experiments with deterministic bucketing (MurmurHash3)
- ✅ **Advanced Targeting** - Segment users by attributes, percentages, or complex constraint rules
- ✅ **Multi-Environment** - Separate configurations for dev, staging, and production
- ✅ **Data Recorders** - Kafka, Kinesis, PubSub integration for analytics
- ✅ **High Performance** - Low-latency evaluation with EvalCache and TTL
- ✅ **Client-Side Evaluation** - Offline-first local evaluation in Kotlin Enhanced, Go Enhanced
- ✅ **Real-Time Updates** - SSE for instant flag changes in Kotlin Enhanced, Go Enhanced
- ✅ **Multiple Databases** - PostgreSQL, MySQL, SQLite support
- ✅ **Docker Ready** - Production-ready Docker images with Compose
- ✅ **Official SDKs** - Kotlin, JavaScript/TypeScript, Swift, Python, Go with Enhanced variants
- ✅ **Ktor Plugin** - First-class Ktor server-side integration
- ✅ **Admin UI** - Modern Compose for Web dashboard
- ✅ **Debug Console** - Real-time evaluation testing and debugging

### Roadmap Features
- 🚧 **GitOps Support** - Feature flags as code (YAML/JSON) (Q2 2026)
- 🚧 **Multi-Tenancy** - Team isolation and resource limits (Q3 2026)
- 🚧 **SSO/SAML** - Enterprise authentication (Auth0, Okta) (Q3 2026)
- 🚧 **RBAC** - Role-based access control (Q3 2026)
- 🚧 **AI Rollouts** - ML-powered gradual rollouts (Q4 2026)

## 📖 Documentation

- 📖 **[Getting Started](https://maxluxs.github.io/Flagent/guides/getting-started.md)** - Quick start and setup
- 📖 **[API Compatibility](https://maxluxs.github.io/Flagent/guides/compatibility.md)** - Evaluation API, migration guide
- 📖 **[API Reference](https://maxluxs.github.io/Flagent)** - API docs and OpenAPI
- 📖 **[OpenAPI spec](https://maxluxs.github.io/Flagent/api/openapi.yaml)** - OpenAPI specification
- 📖 **[Architecture](https://maxluxs.github.io/Flagent/architecture/backend.md)** - System design

## 🏗️ Project Structure

```
flagent/
├── backend/          # Ktor backend (Clean Architecture)
├── frontend/         # Compose for Web UI
├── sdk/              # Client SDKs (Kotlin, JS, Swift, Python, Go)
├── ktor-flagent/     # Ktor plugin
└── docs/guides/roadmap.md   # Development roadmap
```

## Development

**Version:** single source is root file `VERSION`. Gradle reads it; run `./scripts/sync-version.sh` to propagate to npm/pip/Go/Swift/Helm/Java. See [docs/guides/versioning.md](docs/guides/versioning.md).

### Requirements
- JDK 21+
- Gradle 8.13 (wrapper)

### Build
```bash
./gradlew build
```

### Run Backend
```bash
./gradlew :backend:run
```

### Configuration
All settings via environment variables. See [AppConfig.kt](backend/src/main/kotlin/flagent/config/AppConfig.kt) for options.

Example:
```bash
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
export PORT=18000
./gradlew :backend:run
```

### API Documentation (when server running)
- **Swagger UI**: http://localhost:18000/docs
- **OpenAPI YAML**: http://localhost:18000/api/v1/openapi.yaml

## 📚 SDKs

Official SDKs available for multiple platforms:

| Language | Package | Status | Features |
|----------|---------|--------|----------|
| **Kotlin** | [flagent-kotlin](sdk/kotlin) | ✅ Stable | Full API support, coroutines |
| **Kotlin Enhanced** | [kotlin-enhanced](sdk/kotlin-enhanced) | ✅ Stable | Client-side eval, real-time updates |
| **JavaScript/TypeScript** | [flagent-js](sdk/javascript) | ✅ Stable | Full API support, async/await |
| **Swift** | [flagent-swift](sdk/swift) | ✅ Stable | Full API support, async/await |
| **Python** | [flagent-python](sdk/python) | ✅ Stable | Full API support, asyncio |
| **Go** | [flagent-go](sdk/go) | ✅ Stable | Full API support, goroutines |
| **Go Enhanced** | [go-enhanced](sdk/go-enhanced) | ✅ Stable | Client-side eval, real-time updates |

### Add as dependency (Kotlin/JVM)

Artifacts are published to [GitHub Packages](https://github.com/MaxLuxs/Flagent/packages). Add the repository and dependency:

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
    // Ktor plugin (server)
    implementation("com.flagent:ktor-flagent:0.1.5")
    // Kotlin client
    implementation("com.flagent:kotlin-client:0.1.5")
    // Kotlin Enhanced (offline eval, SSE)
    implementation("com.flagent:kotlin-enhanced:0.1.5")
    // Kotlin Debug UI
    implementation("com.flagent:kotlin-debug-ui:0.1.5")
    // Shared (KMP; pulled transitively by ktor-flagent, or use for multi-platform)
    implementation("com.flagent:shared:0.1.5")
}
```

Published artifacts: `shared` (KMP: root + `shared-jvm`, `shared-js`), `ktor-flagent`, `kotlin-client`, `kotlin-enhanced`, `kotlin-debug-ui`. For public read use a [GitHub PAT](https://github.com/settings/tokens) with `read:packages` (or `GITHUB_TOKEN` in CI). Replace version with the [latest release](https://github.com/MaxLuxs/Flagent/releases) (current: 0.1.5).

### Server-Side Integration

- **Ktor Plugin** - [ktor-flagent](ktor-flagent) - First-class Ktor integration with middleware support

See [Getting Started](https://maxluxs.github.io/Flagent/guides/getting-started.md) and [API Reference](https://maxluxs.github.io/Flagent) for usage guides.

## 🛠️ Technology Stack

- **Kotlin** - Modern JVM language with coroutines
- **Ktor** - Web framework for building async applications
- **Exposed** - Type-safe SQL framework
- **Kotlinx Serialization** - JSON serialization
- **Compose for Web** - Modern UI framework
- **PostgreSQL/MySQL/SQLite** - Database support

## 📦 Installation

### Docker (Recommended)

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

### Docker Compose (with PostgreSQL)

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
docker compose up -d
```

### Build from Source

```bash
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent
./gradlew build
./gradlew :backend:run
```

See [Deployment Guide](https://maxluxs.github.io/Flagent/guides/deployment.md) for production setup.

## 🤝 Contributing

We welcome contributions from the community! Flagent is built with love by developers, for developers.

### How to Contribute

1. **Fork the repository**
2. **Create your feature branch** (`git checkout -b feature/amazing-feature`)
3. **Make your changes** - Follow our [code style guidelines](https://maxluxs.github.io/Flagent/guides/contributing.md)
4. **Add tests** - Ensure your changes are well-tested
5. **Commit your changes** (`git commit -m 'Add some amazing feature'`)
6. **Push to the branch** (`git push origin feature/amazing-feature`)
7. **Open a Pull Request**

### Areas to Contribute

- 🐛 **Bug Fixes** - Help us squash bugs
- ✨ **New Features** - Implement features from our [roadmap](docs/guides/roadmap.md)
- 📚 **Documentation** - Improve guides and examples
- 🌍 **SDKs** - Add support for new languages
- 🎨 **UI/UX** - Enhance the admin dashboard
- ⚡ **Performance** - Optimize evaluation speed
- 🧪 **Tests** - Increase test coverage

For more details, see our [Contributing Guide](https://maxluxs.github.io/Flagent/guides/contributing.md) and [Development Setup](README.md#development).

## 📄 License

Flagent is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) for details.

## 🗺️ Roadmap

Flagent is evolving into a production-ready feature flag and experimentation platform. Our roadmap focuses on community needs and enterprise requirements.

### Phase 1: Foundation (Q1 2026) 
**Goal**: Close critical gaps for production use
- ✅ Core feature flags and A/B testing (DONE)
- ✅ Admin UI and Debug Console (DONE)
- ✅ Data recorders: Kafka, Kinesis, PubSub (DONE)
- ✅ Python SDK with asyncio (DONE)
- ✅ Go SDK (DONE)
- ✅ Client-side evaluation in Go SDK (DONE)
- ✅ Real-time updates (SSE) in Go SDK (DONE) ⭐ **NEW**
- 🚧 Enhanced documentation

**Target**: 100+ GitHub stars, 5+ production deployments

### Phase 2: Community (Q2-Q3 2026)
**Goal**: Build community and product-market fit
- Feature Flags as Code (GitOps workflow)
- CLI tool for automation
- Webhooks and integrations (Slack, Datadog)
- Java/.NET SDKs
- Edge Service for SDK scaling
- Debug UI for SDKs (Compose/SwiftUI/React)
- Public performance benchmarks

**Target**: 500+ stars, 20+ contributors, 50+ deployments

### Phase 3: Enterprise (Q3-Q4 2026)
**Goal**: Enterprise-ready features + SaaS launch
- Multi-tenancy architecture
- SSO/SAML (Auth0, Okta, Azure AD)
- RBAC with custom roles
- Audit logs and compliance
- Advanced analytics dashboard
- AI-powered rollouts (MVP)

**Target**: 2000+ stars, SaaS beta, 10+ paying customers

### Phase 4: Scale (2027+)
**Goal**: Market leadership
- AI Anomaly Detection
- Predictive Targeting
- Terraform/Pulumi providers
- Kubernetes Operator
- SOC 2 compliance
- Enterprise SLA guarantees

**Target**: 5000+ stars, $50k MRR, 50+ enterprise customers

See our [detailed roadmap](docs/guides/roadmap.md) for more information.

## 🌟 Community & Support

Join our growing community of developers building better feature flag systems!

### Get Help

- 🐛 **[GitHub Issues](https://github.com/MaxLuxs/Flagent/issues)** - Ask questions, report bugs, or request features
- 📚 **[Documentation](https://maxluxs.github.io/Flagent/guides/getting-started.md)** - Guides and API reference
- 💻 **[Code Examples](samples)** - Real-world examples in Kotlin, Python, JavaScript, Swift, and Go
- 🎯 **[Roadmap](docs/guides/roadmap.md)** - Our journey to becoming the best open-source feature flag platform

### Stay Updated

- ⭐ **Star us on GitHub** to stay updated with new releases
- 👀 **Watch the repository** to get notified about issues and PRs
- 🗣️ **Share your experience** - Blog posts, talks, and social media mentions are always appreciated!

### Enterprise Support

Need help deploying Flagent in production? Want custom features or SLA guarantees? We offer:

- 🏢 **Professional Services** - Architecture review, deployment assistance, custom integrations
- 🎓 **Training & Workshops** - Team training on feature flag best practices
- 🔒 **Priority Support** - Faster response times and dedicated Slack channel
- 🚀 **Managed Hosting** (Coming Soon) - Fully managed SaaS solution

Contact: **max.developer.luxs@gmail.com**

## 🎯 Use Cases

- **Feature Flags** - Safe deployments with instant rollbacks
- **A/B Testing** - Product optimization with statistical significance
- **Gradual Rollouts** - Progressive feature releases with kill switches
- **Dynamic Configuration** - Update settings without redeployment
- **User Segmentation** - Target specific user groups with complex rules
- **Kill Switches** - Emergency feature disable in production
- **Multi-Variant Testing** - Test multiple variations simultaneously
- **Canary Releases** - Test with subset of users before full rollout

## 📊 Performance & Scalability

- **Low latency** - Evaluation API targets sub-10ms p99 (see [benchmarks](docs/performance/benchmarks.md))
- **High throughput** - Load-tested with k6; run evaluation benchmarks locally (see [benchmarks](docs/performance/benchmarks.md))
- **Horizontal scaling** - Stateless architecture supports multiple instances
- **Modest memory** - Approximately 200MB typical base usage
- **EvalCache with TTL** - Caching significantly reduces database load
- **Deterministic bucketing** - Consistent user assignments using MurmurHash3

## 👥 Who's Using Flagent?

Flagent is trusted by teams building high-performance Kotlin applications:

- 🚀 **Early Adopters** - Join our growing community of production users
- 🔬 **Research Projects** - Used in academic and research environments
- 🏢 **Internal Tools** - Powering feature flags for internal applications

*Are you using Flagent in production? [Let us know](https://github.com/MaxLuxs/Flagent/issues) or email max.developer.luxs@gmail.com.*

## 🔗 Links

- 📖 **[Documentation](https://maxluxs.github.io/Flagent/guides/getting-started.md)**
- 📖 **[API Reference](https://maxluxs.github.io/Flagent)**
- 🐳 **[Container image (GHCR)](https://github.com/MaxLuxs/Flagent/pkgs/container/flagent)**
- 📦 **[Releases](https://github.com/MaxLuxs/Flagent/releases)**
- 🐛 **[Issues](https://github.com/MaxLuxs/Flagent/issues)**

---

**Made with ❤️ by the Flagent team**
