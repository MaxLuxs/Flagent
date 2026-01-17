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
    <a href="https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin" target="_blank">
        <img src="https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin" alt="Kotlin Version">
    </a>
    <a href="LICENSE" target="_blank">
        <img src="https://img.shields.io/badge/license-Apache%202.0-green.svg" alt="License">
    </a>
    <a href="https://img.shields.io/badge/JDK-21+-orange.svg" target="_blank">
        <img src="https://img.shields.io/badge/JDK-21+-orange.svg" alt="JDK">
    </a>
    <a href="https://img.shields.io/badge/Gradle-9.0-green.svg" target="_blank">
        <img src="https://img.shields.io/badge/Gradle-9.0-green.svg" alt="Gradle">
    </a>
</p>

## Introduction

`Flagent` is an open source Kotlin/Ktor service that delivers the right experience to the right entity and monitors the impact. It provides feature flags, experimentation (A/B testing), and dynamic configuration. It has clear swagger REST APIs for flags management and flag evaluation.

## Documentation

- 📖 [Full Documentation](https://maxluxs.github.io/Flagent) - Complete documentation with examples and API reference
- 📖 [API Documentation](http://localhost:18000/docs) - Interactive Swagger UI (when server is running)

## Quick demo

Try it with Docker.

```sh
# Start the docker container
docker pull ghcr.io/maxluxs/flagent
docker run -it -p 18000:18000 ghcr.io/maxluxs/flagent

# Open the Flagent UI
open localhost:18000
```

Or try it with Gradle:

```bash
cd flagent
./gradlew :backend:run
```

The server will start on `http://localhost:18000`

### Try evaluation API

```bash
curl --request POST \
     --url http://localhost:18000/api/v1/evaluation \
     --header 'content-type: application/json' \
     --data '{
       "entityID": "127",
       "entityType": "user",
       "entityContext": {
         "state": "NY"
       },
       "flagID": 1,
       "enableDebug": true
     }'
```

## Flagent Evaluation Performance

Flagent maintains high performance with Kotlin Coroutines for asynchronous evaluation. The evaluation engine is designed for scalability and low latency.

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

## Features

- ✅ Feature flags management
- ✅ A/B testing and experimentation
- ✅ Dynamic configuration
- ✅ Constraint evaluation
- ✅ Data recording (Kafka, Kinesis, PubSub)
- ✅ Multiple database support (PostgreSQL, MySQL, SQLite)
- ✅ Authentication middleware (JWT, Basic, Header, Cookie)
- ✅ Swagger/OpenAPI documentation
- ✅ Compose for Web UI
- ✅ Ktor plugin (`ktor-flagent`)

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

## Project Structure

```
flagent/
├── backend/          # Ktor backend server
├── frontend/         # Compose for Web frontend
├── ktor-flagent/     # Ktor plugin for Flagent functionality
├── shared/           # Shared models between backend and frontend
└── sdk/              # Client SDKs (Kotlin, JavaScript, Swift)
```

## License and Credit

- [`Flagent`](https://github.com/MaxLuxs/Flagent) Apache 2.0
