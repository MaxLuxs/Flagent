#!/bin/bash
set -e

echo "=== Smoke Test: Backend with SQLite ==="

# Set environment variables
export FLAGENT_DB_DBDRIVER=sqlite3
export FLAGENT_DB_DBCONNECTIONSTR=:memory:
export FLAGENT_SERVER_PORT=8080

# Build backend
echo "Building backend..."
./gradlew :backend:build -x test --console=plain > /dev/null 2>&1

# Start backend in background
echo "Starting backend..."
java -jar backend/build/libs/backend-1.0.0.jar > /tmp/flagent_backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
for i in {1..30}; do
  if curl -s http://localhost:8080/api/v1/health > /dev/null 2>&1; then
    echo "Backend started successfully!"
    break
  fi
  if [ $i -eq 30 ]; then
    echo "Backend failed to start within 30 seconds"
    cat /tmp/flagent_backend.log
    kill $BACKEND_PID 2>/dev/null || true
    exit 1
  fi
  sleep 1
done

# Test health endpoint
echo "Testing health endpoint..."
RESPONSE=$(curl -s http://localhost:8080/api/v1/health)
echo "Response: $RESPONSE"

if echo "$RESPONSE" | grep -q "status"; then
  echo "✓ Health endpoint returned valid response"
else
  echo "✗ Health endpoint returned invalid response"
  kill $BACKEND_PID 2>/dev/null || true
  exit 1
fi

# Cleanup
echo "Stopping backend..."
kill $BACKEND_PID 2>/dev/null || true
wait $BACKEND_PID 2>/dev/null || true

echo "=== Smoke Test Passed ==="
