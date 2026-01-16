######################################
# Build stage
######################################
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Install dependencies for building
RUN apk add --no-cache git

# Copy Gradle wrapper and build files
COPY flagent/gradlew ./flagent/
COPY flagent/gradlew.bat ./flagent/
COPY flagent/gradle ./flagent/gradle
COPY flagent/gradle.properties ./flagent/
COPY flagent/build.gradle.kts ./flagent/
COPY flagent/settings.gradle.kts ./flagent/
COPY flagent/gradle/libs.versions.toml ./flagent/gradle/

# Copy source code
COPY flagent/shared ./flagent/shared
COPY flagent/backend ./flagent/backend

# Build the application
WORKDIR /app/flagent
RUN chmod +x ./gradlew && \
    ./gradlew :backend:installDist --no-daemon --stacktrace

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
COPY --from=build /app/flagent/backend/build/install/backend ./backend

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Set default environment variables
ENV HOST=0.0.0.0
ENV PORT=18000
ENV FLAGENT_DB_DBDRIVER=sqlite3
ENV FLAGENT_DB_DBCONNECTIONSTR=/data/flagent.sqlite

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
