<h1 align="center">Flagent</h1>

<p align="center">
  <strong>Open-source feature management platform built with Kotlin/Ktor</strong>
</p>

<p align="center">
    <a href="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml?query=branch%3Amain+" target="_blank">
        <img src="https://github.com/MaxLuxs/Flagent/actions/workflows/ci.yml/badge.svg?branch=main" alt="Build Status">
    </a>
    <a href="https://codecov.io/gh/MaxLuxs/Flagent">
        <img src="https://codecov.io/gh/MaxLuxs/Flagent/branch/main/graph/badge.svg" alt="Code Coverage">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/releases" target="_blank">
        <img src="https://img.shields.io/github/release/MaxLuxs/Flagent.svg?style=flat&color=green" alt="Release">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/stargazers">
        <img src="https://img.shields.io/github/stars/MaxLuxs/Flagent?style=flat&color=yellow" alt="GitHub Stars">
    </a>
    <a href="https://github.com/MaxLuxs/Flagent/network/members">
        <img src="https://img.shields.io/github/forks/MaxLuxs/Flagent?style=flat&color=blue" alt="GitHub Forks">
    </a>
    <a href="https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin" target="_blank">
        <img src="https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin" alt="Kotlin Version">
    </a>
    <a href="LICENSE" target="_blank">
        <img src="https://img.shields.io/badge/license-Apache%202.0-green.svg" alt="License">
    </a>
</p>

<p align="center">
  <a href="#-what-is-flagent">About</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#key-features">Features</a> •
  <a href="#client-libraries">SDKs</a> •
  <a href="https://maxluxs.github.io/Flagent">Documentation</a> •
  <a href="#community--support">Community</a>
</p>

<br/>

## 🚀 What is Flagent?

**Flagent** is a production-ready, open-source feature flag and experimentation platform built with Kotlin/Ktor. It enables teams to safely deploy features, run A/B tests, and manage dynamic configurations with fine-grained targeting and real-time analytics.

Feature flags in Flagent let you deploy code to production behind toggles, reducing the risk of impacting your users. Roll out features gradually, test with real production data, and maintain full control over when and how features are enabled.

Flagent is designed for modern development teams who need:
- **Feature Flags** - Gradual rollouts, kill switches, and remote configuration
- **A/B Testing** - Multi-variant experiments with statistical significance analysis
- **Advanced Targeting** - Segment users by attributes, percentages, or complex rules
- **Multi-Environment Management** - Separate configurations for dev, staging, and production
- **Analytics Integration** - Real-time metrics and integration with popular analytics platforms
- **Enterprise Scalability** - High-performance evaluation engine built on Kotlin Coroutines
- **Self-Hosted or Cloud** - Deploy on your infrastructure or use managed hosting

Built with Clean Architecture principles, Flagent provides a modern, type-safe API and comprehensive SDKs for seamless integration into any tech stack.

## Documentation

- 📖 [Full Documentation](https://maxluxs.github.io/Flagent) - Complete documentation with examples and API reference
- 📖 [API Documentation](http://localhost:18000/docs) - Interactive Swagger UI (when server is running)

## Quick Start

Get Flagent running in under 5 minutes.

### Option 1: Docker (Recommended)

```sh
# Pull and run Flagent
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent

# Open Flagent UI
open http://localhost:18000
```

**Default credentials:**
- Username: `admin`
- Password: `admin`

### Option 2: Docker Compose (with PostgreSQL)

```bash
# Clone the repository
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent

# Start with Docker Compose
docker compose up -d

# Access the UI
open http://localhost:18000
```

### Option 3: Build from Source

```bash
cd flagent
./gradlew :backend:run
```

The server will start on `http://localhost:18000`

### Try the Evaluation API

```bash
curl --request POST \
     --url http://localhost:18000/api/v1/evaluation \
     --header 'content-type: application/json' \
     --data '{
       "entityID": "user-123",
       "entityType": "user",
       "entityContext": {
         "state": "NY",
         "tier": "premium"
       },
       "flagID": 1,
       "enableDebug": true
     }'
```

For more examples, see [Documentation](https://maxluxs.github.io/Flagent) and [Code Examples](https://github.com/MaxLuxs/Flagent/tree/main/samples).

## Performance & Scalability

Flagent is built for production-scale deployments with a focus on performance and reliability.

### Evaluation Performance

- **Sub-millisecond Latency** - Typical evaluation time under 1ms per request
- **Kotlin Coroutines** - Asynchronous, non-blocking evaluation engine
- **In-Memory Caching** - Fast lookup with configurable TTL and refresh intervals
- **Deterministic Evaluation** - Consistent results using MurmurHash3 hashing

### Scalability

- **Horizontal Scaling** - Stateless architecture supports multiple instances behind a load balancer
- **Database Connection Pooling** - Efficient resource usage with HikariCP
- **Client-Side Caching** - SDKs cache flags locally to reduce server load
- **Batch Evaluation** - Evaluate multiple flags in a single API request

### Benchmarks

Flagent handles high-throughput scenarios:
- **10,000+ evaluations/second** per instance (varies by hardware)
- **Low memory footprint** - ~200MB base memory usage
- **Database agnostic** - Supports PostgreSQL, MySQL, SQLite with similar performance characteristics

## Performance Metrics

Flagent automatically collects performance metrics using Micrometer and Prometheus. Metrics are collected for all HTTP requests without any additional code.

### Automatic HTTP Metrics

The following metrics are automatically collected by the MicrometerMetrics plugin:

- **`http.server.requests`** - Total count of HTTP requests with tags: `method`, `uri`, `status`
- **`http.server.requests.duration`** - Request duration histogram with tags: `method`, `uri`, `status`
- **JVM metrics** - Memory usage, thread counts, garbage collection

### Enabling Metrics

To enable Prometheus metrics endpoint, set the environment variable:

```bash
export FLAGENT_PROMETHEUS_ENABLED=true
export FLAGENT_PROMETHEUS_PATH=/metrics  # optional, defaults to /metrics
```

After starting the server, metrics are available at:

```bash
curl http://localhost:18000/metrics
```

### StatsD Metrics

StatsD metrics can also be enabled for integration with Datadog, Graphite, etc.:

```bash
export FLAGENT_STATSD_ENABLED=true
export FLAGENT_STATSD_HOST=127.0.0.1
export FLAGENT_STATSD_PORT=8125
export FLAGENT_STATSD_PREFIX=flagent.
```

See [AppConfig.kt](flagent/backend/src/main/kotlin/flagent/config/AppConfig.kt) for all metrics configuration options.

## Flagent UI

The UI is built with Compose for Web and provides a modern, intuitive interface for managing feature flags and experiments.

## Technology Stack

- **Kotlin** - Modern JVM language
- **Ktor** - Web framework
- **Exposed** - ORM for database operations
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx Serialization** - JSON serialization
- **HikariCP** - Connection pooling
- **Compose for Web** - Frontend UI

## Key Features

### 🚀 Feature Flags & Remote Configuration
- **Feature Flags Management** - Toggle features on/off instantly without code deployments
- **Gradual Rollouts** - Roll out features progressively (1%, 10%, 50%, 100%) with kill switches
- **Dynamic Configuration** - Update application settings without redeployment
- **Kill Switches** - Instantly disable features in production if issues are detected

### 🧪 A/B Testing & Experimentation
- **Multi-Variant Experiments** - Run A/B/n tests with any number of variants
- **Statistical Significance** - Built-in calculation of p-values and confidence intervals
- **Deterministic Bucketing** - Consistent assignment using MurmurHash3 for reliable results
- **Custom Targeting Rules** - Segment users by attributes, percentages, or complex constraints
- **Real-Time Analytics** - Track experiment results with live metrics and dashboards

### 🎯 Advanced Targeting & Segmentation
- **User Segmentation** - Target users by attributes (region, tier, age, custom properties)
- **Percentage Rollouts** - Gradual rollouts with precise percentage control
- **Constraint Evaluation** - Complex rules with AND/OR logic, comparisons, and regex matching
- **Multi-Environment Support** - Separate configurations for development, staging, and production

### ⚡ Performance & Scalability
- **High Performance** - Built on Kotlin Coroutines for asynchronous, non-blocking evaluation
- **Low Latency** - Sub-millisecond evaluation times with in-memory caching
- **Horizontal Scalability** - Stateless architecture supports horizontal scaling
- **Connection Pooling** - Efficient database connections with HikariCP
- **Edge Caching** - Client-side caching for offline-first SDKs

### 📊 Analytics & Monitoring
- **Real-Time Metrics** - Prometheus metrics for HTTP requests, evaluation counts, and latencies
- **Analytics Integration** - Native integrations with Segment, Mixpanel, Amplitude, and more
- **Data Recording** - Export evaluation data to Kafka, AWS Kinesis, or Google Pub/Sub
- **Performance Monitoring** - Built-in metrics for request duration, cache hit rates, and errors
- **Health Checks** - RESTful health check endpoints for monitoring and load balancing

### 🔌 Integration & Ecosystem
- **REST API** - Comprehensive REST API with OpenAPI/Swagger documentation
- **Official SDKs** - Native SDKs for Kotlin, JavaScript/TypeScript, and Swift
- **Ktor Plugin** - Native integration for Ktor applications (`ktor-flagent`)
- **CI/CD Integration** - GitHub Actions, GitLab CI, Jenkins, and other CI/CD platforms
- **Webhooks** - Real-time notifications for flag changes and events
- **Notification Systems** - Slack, Microsoft Teams, Discord, and email integrations

### 🗄️ Database & Storage
- **Multiple Database Support** - PostgreSQL, MySQL, and SQLite out of the box
- **Migration System** - Database migrations with Exposed ORM
- **Data Export** - Export evaluation data to BigQuery, MongoDB, and other storage systems
- **Backup & Recovery** - Database backups and point-in-time recovery support

### 🔒 Security & Authentication
- **Multiple Auth Methods** - JWT, Basic Auth, Header-based, and Cookie-based authentication
- **Role-Based Access Control** - Fine-grained permissions for flags and environments
- **API Key Management** - Secure API key generation and rotation
- **Audit Logging** - Track all changes with detailed audit logs

### 🌐 Deployment Options
- **Docker** - Official Docker images for easy deployment
- **Docker Compose** - Complete setup with PostgreSQL included
- **Kubernetes** - Helm charts and Kubernetes manifests for production deployments
- **Self-Hosted** - Full control over your infrastructure
- **Cloud Ready** - Deploy to AWS, GCP, Azure, or any cloud provider

## Client Libraries

Flagent provides official SDK clients:

| Language | SDK | Status |
| ---------- | --- | ------ |
| Kotlin | [Flagent Kotlin SDK](./sdk/kotlin/) | ✅ Available |
| JavaScript/TypeScript | [Flagent JavaScript SDK](./sdk/javascript/) | ✅ Available |
| Swift | [Flagent Swift SDK](./sdk/swift/) | ✅ Available |

## Development

### Requirements

- JDK 21+
- Gradle 9.0

### Build

```bash
cd flagent
./gradlew build
```

### Configuration

All settings are configured via environment variables. See [AppConfig.kt](flagent/backend/src/main/kotlin/flagent/config/AppConfig.kt) for the full list of options.

Example:

```bash
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
export PORT=18000
cd flagent
./gradlew :backend:run
```

### API Documentation

After starting the server, interactive API documentation is available:

- **Swagger UI**: http://localhost:18000/docs
- **OpenAPI specification (YAML)**: http://localhost:18000/api/v1/openapi.yaml
- **OpenAPI specification (JSON)**: http://localhost:18000/api/v1/openapi.json

## Use Cases

### 1. Feature Flags for Safe Deployments

Deploy code to production behind feature flags, enabling instant rollbacks without code changes:

```kotlin
// Kotlin SDK Example
val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")

if (client.isEnabled("new_payment_flow")) {
    // New payment implementation
    newPaymentFlow.process()
} else {
    // Fallback to old implementation
    legacyPaymentFlow.process()
}
```

### 2. A/B Testing for Product Optimization

Run experiments to test new features and measure impact:

```kotlin
val assignment = client.evaluate("checkout_experiment", entityContext = userContext)
when (assignment?.variant) {
    "control" -> showOldCheckout()
    "variant_a" -> showNewCheckoutVariantA()
    "variant_b" -> showNewCheckoutVariantB()
}
```

### 3. Gradual Rollouts with Kill Switches

Roll out features gradually and instantly disable if issues are detected:

```kotlin
// Roll out to 10% of users in NY state
val enabled = client.isEnabled(
    "new_feature",
    entityContext = mapOf("state" to "NY")
)

if (enabled) {
    // Feature enabled for 10% of NY users
    // Can be instantly disabled via UI or API
}
```

### 4. Dynamic Configuration

Update application settings without redeployment:

```kotlin
val config = client.getVariantAttachment("app_config")
val maxRetries = config["max_retries"]?.toInt() ?: 3
val timeout = config["timeout_ms"]?.toLong() ?: 5000
```

### 5. User Segmentation and Targeting

Target specific user segments with complex rules:

```kotlin
// Enable feature only for premium users in specific regions
val enabled = client.isEnabled(
    "premium_feature",
    entityContext = mapOf(
        "tier" to "premium",
        "region" to "US",
        "subscription_age_days" to "30"
    )
)
```

See [Use Cases Documentation](https://maxluxs.github.io/Flagent/#/examples) for more examples.

## Project Structure

```
flagent/
├── backend/          # Ktor backend server (Clean Architecture)
│   ├── domain/       # Domain layer (entities, use cases, interfaces)
│   ├── service/      # Application layer (services, DTOs, mappers)
│   ├── repository/   # Infrastructure layer (database, cache)
│   ├── route/        # Presentation layer (HTTP handlers)
│   └── config/       # Configuration and application setup
├── frontend/         # Compose for Web frontend
├── ktor-flagent/     # Ktor plugin for Flagent functionality
├── shared/           # Shared models between backend and frontend
└── sdk/              # Client SDKs
    ├── kotlin/       # Kotlin/JVM SDK
    ├── javascript/   # JavaScript/TypeScript SDK
    └── swift/        # Swift/iOS SDK
```

## Open-Source vs Enterprise Build

- **Open-source (default):** Core feature flags, evaluation, A/B testing, and APIs. Build with `./gradlew build`. If `internal/flagent-enterprise` is not present, only core is built.
- **Enterprise (optional):** When `internal/flagent-enterprise` exists (or is added as a submodule), the build includes the enterprise module: multi-tenancy, billing (Stripe), SSO/SAML, and related tables. The backend loads it via ServiceLoader and runs enterprise migrations and routes when the module is on the classpath. See [internal/README.md](internal/README.md).

## Deployment

Flagent supports multiple deployment options for different use cases.

### Quick Start with Docker

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

### Docker Compose (with PostgreSQL)

```bash
docker compose up -d
```

### Kubernetes

Deploy to Kubernetes with Helm or manifests (see [deployment guide](https://maxluxs.github.io/Flagent/#/deployment)).

### Self-Hosted

1. **Build from source:**
   ```bash
   cd flagent
   ./gradlew build
   ```

2. **Configure environment variables:**
   ```bash
   export FLAGENT_DB_DBDRIVER=postgres
   export FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@localhost:5432/flagent
   export PORT=18000
   ```

3. **Run the application:**
   ```bash
   ./gradlew :backend:run
   ```

See [Deployment Documentation](https://maxluxs.github.io/Flagent/#/deployment) for production setup, multi-environment configuration, and scaling strategies.

## Multi-Environment Management

Flagent supports managing feature flags across multiple environments:

- **Development** - Local development and testing
- **Staging** - Pre-production testing and validation
- **Production** - Live production environment

Each environment can have:
- Separate database instances
- Different configuration settings
- Environment-specific flags and experiments
- Independent analytics and metrics

See [Multi-Environment Setup](https://maxluxs.github.io/Flagent/#/features/feature-flags-as-code) for detailed configuration.

## Community & Support

We know that learning a new tool can be challenging. We're here to help!

### 💬 Getting Help

- **[GitHub Discussions](https://github.com/MaxLuxs/Flagent/discussions)** - Ask questions, share ideas, and discuss use cases with the community
- **[GitHub Issues](https://github.com/MaxLuxs/Flagent/issues)** - Report bugs or request features
- **[Documentation](https://maxluxs.github.io/Flagent)** - Complete guides, API reference, tutorials, and examples
- **[Code Examples](https://github.com/MaxLuxs/Flagent/tree/main/samples)** - Real-world implementation examples

### 🤝 Contributing

Flagent is an open-source project, and we welcome contributions from the community! Whether you're fixing bugs, improving documentation, or adding new features, your help is appreciated.

Check out our [Contributing Guide](https://github.com/MaxLuxs/Flagent/blob/main/docs/contributing.md) to get started:
- Code of conduct
- Development environment setup
- Running tests
- Pull request process
- Code style guidelines

### 🗺️ Roadmap

We're continuously improving Flagent. Upcoming features include:
- 🔄 **GitOps Integration** - Feature flags as code with Git-based workflows
- 📊 **Advanced Analytics** - Statistical significance, cohort analysis, and ML-powered insights
- 🔌 **More Integrations** - Additional analytics platforms and notification systems
- 🌐 **Multi-Region** - Cross-region replication and edge caching
- 🔐 **Enterprise SSO** - SAML, OIDC, and LDAP authentication

See our [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) for the full roadmap and vote on features you'd like to see!

## License

Flagent is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE) for details.

**Apache 2.0** allows you to:
- ✅ Use Flagent commercially
- ✅ Modify and distribute
- ✅ Place warranty
- ✅ Patent use

**You must:**
- Include copyright and license notice
- State changes
- Include the original LICENSE file

## Comparison with Alternatives

Flagent provides a modern, open-source alternative to commercial feature flag solutions:

| Feature | Flagent | Unleash | Flagsmith | LaunchDarkly |
|---------|---------|---------|-----------|--------------|
| Open Source | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| Self-Hosted | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| A/B Testing | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| Analytics Integration | ✅ Yes | ⚠️ Limited | ✅ Yes | ✅ Yes |
| Multi-Environment | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| Kotlin/JVM Native | ✅ Yes | ⚠️ Java SDK | ⚠️ Java SDK | ⚠️ Java SDK |
| Modern UI | ✅ Compose | ⚠️ React | ✅ React | ✅ React |
| Free Tier | ✅ Unlimited | ✅ Unlimited | ⚠️ Limited | ❌ No |

**Why Flagent?**
- 🚀 Built with modern Kotlin/Ktor stack
- 🎯 Simple, intuitive API and UI
- ⚡ High performance with Kotlin Coroutines
- 🔧 Easy to extend and customize
- 📦 Production-ready with comprehensive features
