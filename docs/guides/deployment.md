# Deployment Guide

> [English](deployment.md) | [Русский](deployment.ru.md)

This guide covers deploying Flagent in various environments, from local development to production.

## Requirements

- **JDK 21+** for running the application
- **Database**: PostgreSQL (recommended), MySQL, or SQLite (development only)
- **Docker** (optional, for containerized deployment)
- **Kubernetes** (optional, for Kubernetes deployment)

## Quick Start with Docker

The fastest way to get started with Flagent:

```bash
# Pull the Docker image
docker pull ghcr.io/maxluxs/flagent

# Run Flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent

# Open Flagent UI
open http://localhost:18000
```

**Default credentials:**
- Email: `admin@local`
- Password: `admin`

## Docker Compose (with PostgreSQL)

For a complete setup with PostgreSQL:

```bash
# Clone the repository
git clone https://github.com/MaxLuxs/Flagent.git
cd Flagent

# Start with Docker Compose
docker compose up -d

# Access the UI
open http://localhost:18000
```

See `docker-compose.yml` for the complete configuration.

## Self-Hosted Deployment

### Building from Source

1. **Clone the repository**

   ```bash
   git clone https://github.com/MaxLuxs/Flagent.git
   cd Flagent
   ```

2. **Build the application**

   **Option A: installDist (distribution with script)**

   ```bash
   ./gradlew :backend:installDist
   ```

   This creates `backend/build/install/backend/`. Run:

   ```bash
   ./backend/build/install/backend/bin/backend
   ```

   **Option B: Fat JAR**

   ```bash
   ./gradlew :backend:shadowJar
   ```

   This creates `backend/build/libs/backend-all.jar`. Run:

   ```bash
   java -jar backend/build/libs/backend-all.jar
   ```

### Configuration

All settings are configured via environment variables. See [Configuration Guide](configuration.md) for all options.

**Basic configuration:**

```bash
export HOST=0.0.0.0
export PORT=18000
export FLAGENT_DB_DBDRIVER=postgres
export FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@localhost:5432/flagent
export FLAGENT_LOGRUS_LEVEL=info
export FLAGENT_LOGRUS_FORMAT=json

# When using installDist:
./backend/build/install/backend/bin/backend

# When using fat JAR:
java -jar backend/build/libs/backend-all.jar
```

## Production Setup

### Database Configuration

**PostgreSQL (Recommended for Production):**

```bash
export FLAGENT_DB_DBDRIVER=postgres
export FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@db-host:5432/flagent?sslmode=require
```

**MySQL:**

```bash
export FLAGENT_DB_DBDRIVER=mysql
export FLAGENT_DB_DBCONNECTIONSTR=user:password@tcp(db-host:3306)/flagent?parseTime=true
```

### Security Configuration

**Enable authentication:**

```bash
# JWT Authentication
export FLAGENT_JWT_AUTH_ENABLED=true
export FLAGENT_JWT_AUTH_SECRET=your-secure-secret-key

# Or Basic Authentication
export FLAGENT_BASIC_AUTH_ENABLED=true
export FLAGENT_BASIC_AUTH_USERNAME=admin
export FLAGENT_BASIC_AUTH_PASSWORD=secure-password

# Or Header-based (user from header, e.g. X-Email)
export FLAGENT_HEADER_AUTH_ENABLED=true
export FLAGENT_HEADER_AUTH_USER_FIELD=X-Email
```

### Monitoring Configuration

**Enable Prometheus metrics:**

```bash
export FLAGENT_PROMETHEUS_ENABLED=true
export FLAGENT_PROMETHEUS_PATH=/metrics
```

Metrics will be available at `http://localhost:18000/metrics`

**Enable StatsD metrics:**

```bash
export FLAGENT_STATSD_ENABLED=true
export FLAGENT_STATSD_HOST=127.0.0.1
export FLAGENT_STATSD_PORT=8125
export FLAGENT_STATSD_PREFIX=flagent.
```

### Logging Configuration

**Production logging:**

```bash
export FLAGENT_LOGRUS_LEVEL=info
export FLAGENT_LOGRUS_FORMAT=json
export FLAGENT_EVAL_DEBUG_ENABLED=false
export FLAGENT_EVAL_LOGGING_ENABLED=true
```

## Kubernetes Deployment

Flagent is stateless and can be deployed to Kubernetes. Here's a basic Kubernetes configuration:

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flagent
spec:
  replicas: 3
  selector:
    matchLabels:
      app: flagent
  template:
    metadata:
      labels:
        app: flagent
    spec:
      containers:
      - name: flagent
        image: ghcr.io/maxluxs/flagent:latest
        ports:
        - containerPort: 18000
        env:
        - name: HOST
          value: "0.0.0.0"
        - name: PORT
          value: "18000"
        - name: FLAGENT_DB_DBDRIVER
          value: "postgres"
        - name: FLAGENT_DB_DBCONNECTIONSTR
          valueFrom:
            secretKeyRef:
              name: flagent-secrets
              key: database-url
        - name: FLAGENT_JWT_AUTH_SECRET
          valueFrom:
            secretKeyRef:
              name: flagent-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /api/v1/health
            port: 18000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/health
            port: 18000
          initialDelaySeconds: 10
          periodSeconds: 5
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: flagent
spec:
  selector:
    app: flagent
  ports:
  - port: 80
    targetPort: 18000
  type: LoadBalancer
```

### Secrets

Create secrets for sensitive configuration:

```bash
kubectl create secret generic flagent-secrets \
  --from-literal=database-url=postgresql://user:password@db:5432/flagent \
  --from-literal=jwt-secret=your-secret-key
```

## Docker Deployment

### Dockerfile

Flagent includes a Dockerfile for containerized deployment:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY backend/build/libs/backend-*-all.jar app.jar
EXPOSE 18000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run

```bash
# Build the Docker image
docker build -t flagent:latest .

# Run the container
docker run -d \
  -p 18000:18000 \
  -e FLAGENT_DB_DBDRIVER=postgres \
  -e FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@db:5432/flagent \
  flagent:latest
```

## Environment-Specific Configuration

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

### Staging

```bash
# .env.staging
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=staging
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@staging-db:5432/flagent
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=staging-secret-key
```

### Production

```bash
# .env.production
HOST=0.0.0.0
PORT=18000
ENVIRONMENT=production
FLAGENT_DB_DBDRIVER=postgres
FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@prod-db:5432/flagent?sslmode=require
FLAGENT_LOGRUS_LEVEL=info
FLAGENT_LOGRUS_FORMAT=json
FLAGENT_EVAL_DEBUG_ENABLED=false
FLAGENT_PROMETHEUS_ENABLED=true
FLAGENT_STATSD_ENABLED=true
FLAGENT_JWT_AUTH_SECRET=production-secret-key
FLAGENT_RECORDER_ENABLED=true
FLAGENT_RECORDER_TYPE=kafka
FLAGENT_RECORDER_KAFKA_BROKERS=kafka1:9092,kafka2:9092
```

## Scaling

### Horizontal Scaling

Flagent is stateless and can be scaled horizontally:

1. **Deploy multiple instances** behind a load balancer
2. **Use shared database** (PostgreSQL or MySQL)
3. **Configure sticky sessions** if needed (optional)

**Example with Docker Compose:**

```yaml
version: '3.8'
services:
  flagent:
    image: ghcr.io/maxluxs/flagent:latest
    deploy:
      replicas: 3
    ports:
      - "18000:18000"
    environment:
      - FLAGENT_DB_DBDRIVER=postgres
      - FLAGENT_DB_DBCONNECTIONSTR=postgresql://user:password@db:5432/flagent
```

### Vertical Scaling

Increase resources for better performance:

- **Memory**: 2GB+ recommended for production
- **CPU**: 2+ cores recommended for high traffic
- **Database**: HikariCP pool (default 10 connections; see Database.kt for tuning)

## Health Checks

Flagent provides health check endpoints:

### Health Endpoint

```bash
curl http://localhost:18000/api/v1/health
```

Response:
```json
{
  "status": "healthy",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Metrics Endpoint

```bash
curl http://localhost:18000/metrics
```

Returns Prometheus-formatted metrics.

## Monitoring

### Prometheus Integration

Flagent exports Prometheus metrics at `/metrics`. Configure Prometheus to scrape:

```yaml
scrape_configs:
  - job_name: 'flagent'
    static_configs:
      - targets: ['flagent:18000']
```

### Grafana Dashboards

Create Grafana dashboards using Prometheus metrics:
- HTTP request counts and latencies
- Evaluation counts by flag
- Error rates
- Database connection pool metrics

### Logging

Configure log aggregation:
- **JSON format** for structured logs
- **Log levels**: debug (dev), info (prod)
- **Centralized logging**: Send logs to ELK, Splunk, or similar

## Backup & Recovery

### Database Backups

**PostgreSQL:**

```bash
# Backup
pg_dump -h db-host -U user flagent > flagent-backup.sql

# Restore
psql -h db-host -U user flagent < flagent-backup.sql
```

**MySQL:**

```bash
# Backup
mysqldump -h db-host -u user -p flagent > flagent-backup.sql

# Restore
mysql -h db-host -u user -p flagent < flagent-backup.sql
```

### Point-in-Time Recovery

Configure PostgreSQL or MySQL for point-in-time recovery:
- Enable WAL archiving (PostgreSQL)
- Enable binary logging (MySQL)
- Regular backups with retention policy

## Troubleshooting

### Application Won't Start

1. **Check database connection**
   ```bash
   # Test PostgreSQL connection
   psql postgresql://user:password@host:5432/flagent
   ```

2. **Check port availability**
   ```bash
   # Check if port is in use
   lsof -i :18000
   ```

3. **Check logs**
   ```bash
   # View application logs
   docker logs flagent
   ```

### Performance Issues

1. **Database connection pooling**
   - Pool size is configured in `Database.kt` (default: 10). Modify `maximumPoolSize` for higher load.
   - Check database connections: `SELECT count(*) FROM pg_stat_activity;`

2. **Cache configuration**
   - Enable caching: `FLAGENT_EVALCACHE_REFRESHINTERVAL=3s`
   - Check cache hit rates in metrics

3. **Resource limits**
   - Increase memory: `-Xmx2g`
   - Increase CPU cores

### Common Issues

**Issue**: Database connection errors

**Solution**: Check database URL, credentials, and network connectivity

**Issue**: High memory usage

**Solution**: Increase heap size or reduce cache size

**Issue**: Slow evaluation times

**Solution**: Enable caching, optimize database queries, check network latency

## Next Steps

- 📖 [Configuration Guide](configuration.md) - Complete configuration options
- 🏗️ [Architecture](../architecture/backend.md) - Understand Flagent architecture
- 📚 [API Documentation](../api/endpoints.md) - Explore API endpoints
- 💻 [Examples](../examples/README.md) - Code examples and tutorials
