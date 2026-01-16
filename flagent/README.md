## Introduction

Flagent is an open source service that delivers the right experience to the right entity and monitors the impact. It provides feature flags, experimentation (A/B testing), and dynamic configuration. It has clear swagger REST APIs for flags management and flag evaluation.

Flagent is built with Kotlin/Ktor and provides a modern, high-performance solution for feature flagging and experimentation.

## Documentation

- [Full Documentation](https://maxluxs.github.io/Flagent) - Complete documentation with examples and API reference
- [API Documentation](http://localhost:18000/docs) - Interactive Swagger UI (when server is running)

## Quick demo

### Run with Gradle

```bash
cd backend
./gradlew run
```

The server will start on `http://localhost:18000`

### Run with Docker

```bash
# Build Docker image (when Dockerfile is available)
docker build -t flagent .

# Start the docker container
docker run -it -p 18000:18000 flagent

# Open the Flagent UI
open localhost:18000
```

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

## Flagent UI

The UI is built with Compose for Web and provides a modern, intuitive interface for managing feature flags and experiments.

## Technology Stack

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

| Language   | SDK | Status |
| ---------- | --- | ------ |
| Kotlin     | [Flagent Kotlin SDK](./sdk/kotlin/) | ✅ Available |
| JavaScript/TypeScript | [Flagent JavaScript SDK](./sdk/javascript/) | ✅ Available |
| Swift      | [Flagent Swift SDK](./sdk/swift/) | ✅ Available |

## Development

### Requirements

- JDK 21+
- Gradle 8.14+

### Build

```bash
cd backend
./gradlew build
```

### Configuration

All settings are configured via environment variables. See [AppConfig.kt](backend/src/main/kotlin/flagent/config/AppConfig.kt) for the full list of options.

Example:

```bash
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
export PORT=18000
cd backend
./gradlew run
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
└── sdk/              # Client SDKs (Kotlin, JavaScript, Swift)
```

## License

- Flagent - Apache 2.0
