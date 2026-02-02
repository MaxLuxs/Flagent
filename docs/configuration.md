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

# Environment (development, staging, production)
ENVIRONMENT=production
```

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

```bash
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@localhost:5432/flagent?sslmode=disable
```

**Connection String Format:**
```
postgresql://[user[:password]@][host][:port][/database][?param1=value1&...]
```

**Parameters:**
- `sslmode` - SSL mode (disable, require, verify-full)
- `connect_timeout` - Connection timeout in seconds
- `pool_size` - Connection pool size (default: 10)

### MySQL

```bash
FLAGENT_DB_DBDRIVER=mysql
FLAGENT_DB_DBCONNECTIONSTR=user:password@tcp(127.0.0.1:3306)/flagent?parseTime=true
```

**Connection String Format:**
```
[user[:password]@]tcp([host][:port])[/database][?param1=value1&...]
```

**Parameters:**
- `parseTime` - Parse time/date strings (true/false)
- `charset` - Character set (utf8mb4, latin1)
- `timeout` - Connection timeout

### SQLite (Development Only)

```bash
FLAGENT_DB_DBDRIVER=sqlite3
FLAGENT_DB_DBCONNECTIONSTR=/path/to/flagent.sqlite
```

**Note:** SQLite is recommended for development only. Use PostgreSQL or MySQL for production.

### Connection Pooling

```bash
# Maximum number of connections in pool (default: 10)
FLAGENT_DB_POOL_SIZE=20

# Connection timeout in milliseconds (default: 30000)
FLAGENT_DB_CONNECTION_TIMEOUT=30000

# Idle timeout in milliseconds (default: 600000)
FLAGENT_DB_IDLE_TIMEOUT=600000

# Maximum lifetime in milliseconds (default: 1800000)
FLAGENT_DB_MAX_LIFETIME=1800000
```

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

# Exclude URLs from verbose logging (comma-separated)
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
# JWT secret key
FLAGENT_JWT_SECRET=your-secret-key

# JWT expiration time (default: 24h)
FLAGENT_JWT_EXPIRATION=24h

# JWT issuer (default: flagent)
FLAGENT_JWT_ISSUER=flagent
```

### Basic Authentication

```bash
# Basic auth username
FLAGENT_BASIC_AUTH_USERNAME=admin

# Basic auth password
FLAGENT_BASIC_AUTH_PASSWORD=admin
```

### Header-Based Authentication

```bash
# Header name for API key (default: X-API-Key)
FLAGENT_HEADER_AUTH_HEADER=X-API-Key

# API keys (comma-separated)
FLAGENT_HEADER_AUTH_API_KEYS=key1,key2,key3
```

### Cookie-Based Authentication

```bash
# Cookie name for authentication (default: auth_token)
FLAGENT_COOKIE_AUTH_COOKIE_NAME=auth_token

# Cookie secure flag (default: false)
FLAGENT_COOKIE_AUTH_SECURE=true

# Cookie HTTP-only flag (default: true)
FLAGENT_COOKIE_AUTH_HTTP_ONLY=true
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

## Admin Auth (Enterprise)

When enabled, `/admin/*` routes (e.g. create/list tenants) require either a JWT from `POST /auth/login` (admin email/password) or the `X-Admin-Key` header. Disabled by default for backward compatibility.

```bash
# Enable admin auth (default: false)
FLAGENT_ADMIN_AUTH_ENABLED=true

# Admin login (for POST /auth/login)
FLAGENT_ADMIN_EMAIL=admin@example.com
FLAGENT_ADMIN_PASSWORD=your-secret-password

# Optional: bcrypt hash instead of plain password (future)
# FLAGENT_ADMIN_PASSWORD_HASH=$2a$10$...

# Optional: API key for admin requests without login (X-Admin-Key header)
FLAGENT_ADMIN_API_KEY=your-admin-api-key

# JWT secret for signing admin tokens (required when admin auth enabled)
FLAGENT_JWT_AUTH_SECRET=at-least-32-characters-secret
```

**Flow:** 1) Set the variables above. 2) Open UI → Login with admin email/password (or use `X-Admin-Key` in API requests). 3) After login, go to Tenants → Create first tenant → use the returned API key for `/api/v1/*` requests.

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
FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@db:5432/flagent?sslmode=require
FLAGENT_DB_POOL_SIZE=20
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_PROMETHEUS_PATH=/metrics
FLAGENT_JWT_SECRET=your-secure-secret-key
FLAGENT_KAFKA_ENABLED=true
FLAGENT_KAFKA_BROKERS=kafka1:9092,kafka2:9092
FLAGENT_KAFKA_TOPIC=flagent-evaluations
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
- 🏗️ [Architecture](architecture/backend.md) - Understand Flagent architecture
- 📚 [API Documentation](api/endpoints.md) - Explore API endpoints
