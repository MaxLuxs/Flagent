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

RUN ./gradlew :backend:installDist :backend:shadowJar --no-daemon --stacktrace

RUN ./gradlew :frontend:jsBrowserDevelopmentWebpack --no-daemon

######################################
# Runtime stage (distroless: no shell, wget, tar, libssh, pam, gnupg — reduces Trivy CVEs)
######################################
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Single fat JAR + static assets (no OS packages → no wget/tar/libssh/curl/gnupg CVEs)
COPY --from=build /app/backend/build/libs/backend-*.jar ./backend.jar
COPY --from=build /app/frontend/build/kotlin-webpack/js/developmentExecutable ./static

# Default environment variables
ENV HOST=0.0.0.0
ENV PORT=18000
ENV FLAGENT_DB_DBDRIVER=sqlite3
ENV FLAGENT_DB_DBCONNECTIONSTR=/data/flagent.sqlite
ENV FLAGENT_STATIC_DIR=/app/static

# Default credentials for dev (override in production)
ENV FLAGENT_ADMIN_EMAIL=admin@local
ENV FLAGENT_ADMIN_PASSWORD=admin
ENV FLAGENT_JWT_AUTH_SECRET=dev-secret-min-32-chars-required-for-jwt

# Expose port (health: use HTTP GET from orchestrator, e.g. k8s readinessProbe to /api/v1/health)
EXPOSE 18000

# Distroless entrypoint is "java -jar"; CMD is the JAR path
CMD ["/app/backend.jar"]
