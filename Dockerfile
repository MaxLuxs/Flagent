######################################
# Build stage (Debian-based: Gradle's Node.js binary requires glibc, Alpine uses musl)
######################################
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Install dependencies for building (git for Gradle; Node for design-system token generation)
RUN apt-get update && apt-get install -y --no-install-recommends git ca-certificates curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and build files
COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY gradle.properties ./
COPY build.gradle.kts ./
COPY settings.gradle.kts ./
COPY gradle/libs.versions.toml ./gradle/

# Copy source code (backend + frontend + deps for Gradle project structure)
COPY shared ./shared
COPY backend ./backend
COPY frontend ./frontend
COPY design-system ./design-system
COPY ktor-flagent ./ktor-flagent
COPY sdk ./sdk
COPY samples ./samples
COPY gradle-plugins ./gradle-plugins
COPY VERSION ./

# Limit Gradle heap in Docker so multi-platform build does not OOM (runner has ~7GB)
ENV GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

# Build backend and frontend (separate RUNs for clearer CI logs on failure)
WORKDIR /app
RUN chmod +x ./gradlew

RUN ./gradlew :backend:installDist --no-daemon --stacktrace

RUN ./gradlew :frontend:jsBrowserDevelopmentWebpack --no-daemon

######################################
# Runtime stage
######################################
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install runtime dependencies if needed
RUN apk add --no-cache tzdata && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup

# Copy built application from build stage
COPY --from=build /app/backend/build/install/backend ./backend
COPY --from=build /app/frontend/build/kotlin-webpack/js/developmentExecutable ./static

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Set default environment variables
ENV HOST=0.0.0.0
ENV PORT=18000
ENV FLAGENT_DB_DBDRIVER=sqlite3
ENV FLAGENT_DB_DBCONNECTIONSTR=/data/flagent.sqlite
ENV FLAGENT_STATIC_DIR=/app/static

# Default credentials for dev (override in production)
ENV FLAGENT_ADMIN_EMAIL=admin@local
ENV FLAGENT_ADMIN_PASSWORD=admin
ENV FLAGENT_JWT_AUTH_SECRET=dev-secret-min-32-chars-required-for-jwt

# Expose port
EXPOSE 18000

# Health check (install wget for health check)
USER root
RUN apk add --no-cache wget
USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:18000/api/v1/health || exit 1

# Run the application
CMD ["./backend/bin/backend"]
