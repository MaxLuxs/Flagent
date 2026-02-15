# Configuration

> [English](configuration.md) | [Русский](configuration.ru.md)

Flagent can be configured using environment variables. All configuration options are loaded at startup from environment variables.

## Environment Variables

All Flagent settings are configured via environment variables. See [AppConfig.kt](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/config/AppConfig.kt) for the complete list of configuration options.

## Server Configuration

### Basic Settings

```bash
# Server host (default: localhost)
HOST=0.0.0.0

# Server port (default: 18000)
PORT=18000

# Netty worker pool size (default: CPU cores). Increase for high evaluation throughput.
FLAGENT_WORKER_POOL_SIZE=8

# Environment (development, staging, production)
ENVIRONMENT=production
```

### Static Files (Frontend UI)

When the backend serves the admin UI from the same process, it looks for static files in several locations. Use `FLAGENT_STATIC_DIR` to override (e.g. in Docker):

```bash
# Absolute path to directory with built frontend (index.html, frontend.js, etc.)
# When set, backend serves UI from this directory. Used in Docker image (/app/static).
FLAGENT_STATIC_DIR=/app/static
```

If not set, the backend searches relative to the working directory: `frontend/build/kotlin-webpack/js/productionExecutable`, `frontend/build/dist/js/productionExecutable`, etc.

### Logging

```bash
# Log level (debug, info, warn, error)
FLAGENT_LOGRUS_LEVEL=info

# Log format (text, json)
FLAGENT_LOGRUS_FORMAT=json

# Enable pprof (for profiling)
FLAGENT_PPROF_ENABLED=false
```

## Database Configuration

Flagent supports PostgreSQL, MySQL, and SQLite databases.

### PostgreSQL (Recommended for Production)

The backend expects a **JDBC URL** (not a plain `postgresql://` URI):

```bash
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent?user=user&password=password&sslmode=disable
```

**Connection string format:** `jdbc:postgresql://host:port/database?user=...&password=...&param=value`

**Common parameters:**
- `user`, `password` — credentials (or in URL: `jdbc:postgresql://user:password@host:5432/db`)
- `sslmode` — disable, require, verify-full
- `connect_timeout` — connection timeout in seconds

### MySQL

Use a **JDBC URL**:

```bash
FLAGENT_DB_DBDRIVER=mysql
FLAGENT_DB_DBCONNECTIONSTR=jdbc:mysql://localhost:3306/flagent?user=root&password=root&parseTime=true
```

**Parameters:** `user`, `password`, `parseTime`, `charset`, `connectionTimeout`, etc.

### SQLite (Development Only)

```bash
FLAGENT_DB_DBDRIVER=sqlite3
FLAGENT_DB_DBCONNECTIONSTR=/path/to/flagent.sqlite
```

**Note:** SQLite is recommended for development only. Use PostgreSQL or MySQL for production.

### Connection Pooling

Connection pool is managed by HikariCP (default: 10 connections). Pool size is configured in `Database.kt` and is not exposed via environment variables. See [Database.kt](https://github.com/MaxLuxs/Flagent/blob/main/backend/src/main/kotlin/flagent/repository/Database.kt) for current values.

## Middleware Configuration

### Gzip Compression

```bash
# Enable Gzip compression (default: true)
FLAGENT_MIDDLEWARE_GZIP_ENABLED=true
```

### Verbose Logging

```bash
# Enable verbose request logging (default: true)
FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_ENABLED=true

# Exclude URLs from verbose logging (comma-separated). Default: /api/v1/evaluation, /api/v1/evaluation/batch
FLAGENT_MIDDLEWARE_VERBOSE_LOGGER_EXCLUDE_URLS=/health,/metrics
```

### Rate Limiting

```bash
# Rate limiter per flag per second for console logging (default: 100)
FLAGENT_RATELIMITER_PERFLAG_PERSECOND_CONSOLE_LOGGING=100
```

## Evaluation Configuration

### Debug Mode

```bash
# Enable debug mode for evaluations (default: true)
FLAGENT_EVAL_DEBUG_ENABLED=true

# Enable evaluation logging (default: true)
FLAGENT_EVAL_LOGGING_ENABLED=true
```

### Cache Configuration

```bash
# Cache refresh timeout (default: 59s)
FLAGENT_EVALCACHE_REFRESHTIMEOUT=59s

# Cache refresh interval (default: 3s)
FLAGENT_EVALCACHE_REFRESHINTERVAL=3s
```

**Duration Format:**
- `30s` - 30 seconds
- `5m` - 5 minutes
- `1h` - 1 hour

### Evaluation Events Retention (Core Metrics)

Controls cleanup of old `evaluation_events` records used for OSS analytics:

```bash
# Retention period in days (default: 90)
FLAGENT_EVALUATION_EVENTS_RETENTION_DAYS=90

# Enable periodic cleanup (default: true)
FLAGENT_EVALUATION_EVENTS_CLEANUP_ENABLED=true

# Cleanup interval (default: 24h)
FLAGENT_EVALUATION_EVENTS_CLEANUP_INTERVAL=24h
```

### Evaluation Mode

```bash
# Evaluation-only mode (read-only, no mutations)
FLAGENT_EVAL_ONLY_MODE=false
```

When enabled, Flagent will only handle evaluation requests and reject all mutation requests (create, update, delete).

## Metrics & Monitoring

### Prometheus Metrics

```bash
# Enable Prometheus metrics endpoint (default: false)
FLAGENT_PROMETHEUS_ENABLED=true

# Prometheus metrics path (default: /metrics)
FLAGENT_PROMETHEUS_PATH=/metrics
```

After enabling, metrics are available at: `http://localhost:18000/metrics`

### StatsD Metrics

```bash
# Enable StatsD metrics (default: false)
FLAGENT_STATSD_ENABLED=true

# StatsD host (default: 127.0.0.1)
FLAGENT_STATSD_HOST=127.0.0.1

# StatsD port (default: 8125)
FLAGENT_STATSD_PORT=8125

# StatsD prefix (default: flagent.)
FLAGENT_STATSD_PREFIX=flagent.
```

### Health Check

Health check endpoint is always available at: `http://localhost:18000/api/v1/health`

## Authentication Configuration

### JWT Authentication

```bash
FLAGENT_JWT_AUTH_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=your-secret-key
# FLAGENT_JWT_AUTH_USER_CLAIM=sub
# FLAGENT_JWT_AUTH_COOKIE_TOKEN_NAME=access_token
```

### Basic Authentication

```bash
FLAGENT_BASIC_AUTH_ENABLED=true
FLAGENT_BASIC_AUTH_USERNAME=admin
FLAGENT_BASIC_AUTH_PASSWORD=admin
```

### Header-Based Authentication

User identity is taken from a request header (e.g. X-Email for Cloudflare Access):

```bash
FLAGENT_HEADER_AUTH_ENABLED=true
FLAGENT_HEADER_AUTH_USER_FIELD=X-Email
```

### Cookie-Based Authentication

```bash
FLAGENT_COOKIE_AUTH_ENABLED=true
FLAGENT_COOKIE_AUTH_USER_FIELD=CF_Authorization
# FLAGENT_COOKIE_AUTH_USER_FIELD_JWT_CLAIM=email
```

### Enterprise Dev Mode (Local Development Only)

> **SECURITY WARNING: NEVER set these in production.** Bypasses X-API-Key auth and multi-tenant isolation.

```bash
# Enable dev mode (required for dev-only features)
FLAGENT_DEV_MODE=true

# When FLAGENT_DEV_MODE=true: make X-API-Key optional, use first active tenant as fallback
# Both variables must be set. Create a tenant via POST /admin/tenants first.
FLAGENT_DEV_SKIP_TENANT_AUTH=true
```

## Admin Auth (required for UI login)

Admin auth is **enabled by default**. To log in to the UI you **must** set admin credentials and a JWT secret; otherwise you get "Admin credentials not configured".

```bash
# Required for login to work
FLAGENT_ADMIN_EMAIL=admin@local
FLAGENT_ADMIN_PASSWORD=admin
FLAGENT_JWT_AUTH_SECRET=at-least-32-characters-secret

# Optional: disable admin auth (dev only, not recommended)
# FLAGENT_ADMIN_AUTH_ENABLED=false

# Optional: API key for admin API without login (X-Admin-Key header)
# FLAGENT_ADMIN_API_KEY=your-admin-api-key
```

- **Login:** Use `FLAGENT_ADMIN_EMAIL` as the login field (email, not username).
- **JWT secret:** Must be at least 32 characters; used to sign tokens after login.
- **Flow:** Set vars → start server → open UI → log in with email/password. Then create a tenant and use its API key for `/api/v1/*` requests.

## Data Recording Configuration

### Kafka

```bash
# Enable Kafka recording (default: false)
FLAGENT_KAFKA_ENABLED=true

# Kafka brokers (comma-separated)
FLAGENT_KAFKA_BROKERS=localhost:9092

# Kafka topic (default: flagent-evaluations)
FLAGENT_KAFKA_TOPIC=flagent-evaluations

# Kafka client ID (default: flagent)
FLAGENT_KAFKA_CLIENT_ID=flagent

# Kafka compression type (none, gzip, snappy, lz4, zstd)
FLAGENT_KAFKA_COMPRESSION_TYPE=gzip
```

### AWS Kinesis

```bash
# Enable Kinesis recording (default: false)
FLAGENT_KINESIS_ENABLED=true

# Kinesis stream name
FLAGENT_KINESIS_STREAM_NAME=flagent-evaluations

# AWS region (default: us-east-1)
FLAGENT_KINESIS_REGION=us-east-1

# AWS credentials (or use IAM role)
FLAGENT_KINESIS_ACCESS_KEY_ID=your-access-key
FLAGENT_KINESIS_SECRET_ACCESS_KEY=your-secret-key
```

### Google Pub/Sub

```bash
# Enable Pub/Sub recording (default: false)
FLAGENT_PUBSUB_ENABLED=true

# Pub/Sub project ID
FLAGENT_PUBSUB_PROJECT_ID=your-project-id

# Pub/Sub topic name
FLAGENT_PUBSUB_TOPIC_NAME=flagent-evaluations

# Google credentials file path
FLAGENT_PUBSUB_CREDENTIALS_FILE=/path/to/credentials.json
```

## Firebase Integration

### Firebase Remote Config Sync

Syncs Flagent flags to Firebase Remote Config so mobile apps can continue reading from Firebase while Flagent remains the source of truth.

```bash
# Enable Firebase RC sync (default: false)
FLAGENT_FIREBASE_RC_SYNC_ENABLED=true

# Firebase project ID (required when sync enabled)
FLAGENT_FIREBASE_RC_PROJECT_ID=your-firebase-project-id

# Service account credentials: JSON string or file path
FLAGENT_FIREBASE_RC_CREDENTIALS_JSON={"type":"service_account",...}
# Or:
FLAGENT_FIREBASE_RC_CREDENTIALS_FILE=/path/to/service-account.json
# Or use GOOGLE_APPLICATION_CREDENTIALS env var

# Sync interval (default: 5s). Examples: 5s, 1m, 5m
FLAGENT_FIREBASE_RC_SYNC_INTERVAL=5m

# Optional prefix for parameter keys in Firebase RC (e.g. flagent_)
FLAGENT_FIREBASE_RC_PARAMETER_PREFIX=flagent_
```

**Mapping:** One Flagent flag → one Firebase RC parameter. Boolean flags → `"true"`/`"false"`. Experiments → JSON `{"variant":"control","attachment":{...}}`. Firebase RC limit: 2000 parameters.

### Firebase Analytics (GA4 Measurement Protocol)

Sends evaluation events to GA4 so they appear in the same property as Firebase Analytics.

```bash
# Enable Firebase Analytics reporter (default: false)
FLAGENT_FIREBASE_ANALYTICS_ENABLED=true

# GA4 Measurement Protocol API secret (from GA4 Admin → Data streams → Measurement Protocol)
FLAGENT_FIREBASE_ANALYTICS_API_SECRET=your-api-secret

# Measurement ID (e.g. G-XXXXXXXXXX) or Firebase App ID for mobile
FLAGENT_FIREBASE_ANALYTICS_MEASUREMENT_ID=G-XXXXXXXXXX

# Keys in entityContext for GA4 user identity (default: app_instance_id, client_id)
FLAGENT_FIREBASE_ANALYTICS_APP_INSTANCE_ID_KEY=app_instance_id
FLAGENT_FIREBASE_ANALYTICS_CLIENT_ID_KEY=client_id
```

**Required:** Pass `app_instance_id` (Firebase app) or `client_id` (web) in `entityContext` when evaluating so events attach to the correct user in GA4.

## Analytics Retention

Control retention of analytics events (Firebase-level: first_open, session_start, screen_view, custom).

```bash
# Retention days (default: 90)
FLAGENT_ANALYTICS_RETENTION_DAYS=90

# Enable cleanup job (default: true)
FLAGENT_ANALYTICS_CLEANUP_ENABLED=true

# Cleanup interval (default: 24h). Examples: 1h, 12h, 24h
FLAGENT_ANALYTICS_CLEANUP_INTERVAL=24h
```

## MCP (Model Context Protocol)

MCP enables AI assistants (Cursor, Claude, GigaChat, DeepSeek) to access Flagent configurations and evaluate flags. Connect via Streamable HTTP or SSE transport.

```bash
# Enable MCP server (default: false)
FLAGENT_MCP_ENABLED=true

# MCP endpoint path (default: /mcp)
FLAGENT_MCP_PATH=/mcp
```

**Tools:** `evaluate_flag`, `list_flags`, `get_flag`, `analyze_flags`, `suggest_segments`, `optimize_experiment`; when not in eval-only mode also `create_flag`, `update_flag`.  
**Resources:** `flagent://flags`, `flagent://config/snapshot`  

See [MCP Guide](mcp.md) for full tool descriptions, examples, and best practices.

**Connect with MCP Inspector:**
```bash
npx -y @modelcontextprotocol/inspector --connect http://localhost:18000/mcp
```

For Cursor: add to MCP settings with transport `http` and URL `http://localhost:18000/mcp`.

## Example Configuration Files

### Development

```bash
# .env.development
HOST=localhost
PORT=18000
ENVIRONMENT=development
FLAGENT_DB_DBDRIVER=sqlite3
FLAGENT_DB_DBCONNECTIONSTR=flagent.sqlite
FLAGENT_LOGRUS_LEVEL=debug
FLAGENT_LOGRUS_FORMAT=text
FLAGENT_EVAL_DEBUG_ENABLED=true
FLAGENT_PROMETHEUS_ENABLED=false
```

### Production

```bash
# .env.production
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=production
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://db:5432/flagent?user=user&password=password&sslmode=require
FLAGENT_ADMIN_EMAIL=admin@yourcompany.com
FLAGENT_ADMIN_PASSWORD=secure-password
FLAGENT_JWT_AUTH_SECRET=your-secure-secret-key-min-32-chars
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_PROMETHEUS_PATH=/metrics
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=kafka1:9092,kafka2:9092
FLAGENT_RECORDER_KAFKA_TOPIC=flagent-records
```

## Configuration Validation

Flagent validates configuration at startup. If any required configuration is missing or invalid, the application will fail to start with a clear error message.

## Environment Variable Precedence

1. System environment variables (highest priority)
2. `.env` file in the working directory
3. Default values (lowest priority)

## Security Best Practices

1. **Never commit secrets** - Use environment variables or secret management systems
2. **Use secure connection strings** - Enable SSL for database connections in production
3. **Rotate secrets regularly** - Change JWT secrets and API keys periodically
4. **Limit database access** - Use database users with minimal required permissions
5. **Enable authentication** - Always enable authentication in production
6. **Use HTTPS** - Configure reverse proxy with HTTPS in production

## Next Steps

- 📖 [Deployment Guide](deployment.md) - Learn how to deploy Flagent
- 🏗️ [Architecture](../architecture/backend.md) - Understand Flagent architecture
- 📚 [API Documentation](../api/endpoints.md) - Explore API endpoints
