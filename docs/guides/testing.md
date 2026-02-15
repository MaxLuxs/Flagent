# Testing Guide

How to run all tests (unit, integration, E2E) in the Flagent project.

## Quick reference

| Test suite | Command | Requirements |
|------------|--------|--------------|
| **Backend unit** | `./gradlew :backend:test` | JDK 21, no DB (SQLite in-memory) |
| **Backend integration** | `./gradlew :backend:test -PincludeIntegrationTests --tests "*IntegrationTest"` | Postgres (see below) |
| **Backend E2E** | `./gradlew :backend:test -PincludeE2E --tests "flagent.test.e2e.E2ETest"` | Docker (Testcontainers) |
| **Shared (KMP)** | `./gradlew :shared:jvmTest` | JDK 21 |
| **Frontend unit** | `./gradlew :frontend:jsTest` | Node.js |
| **Ktor plugin** | `./gradlew :ktor-flagent:test` | JDK 21 |
| **SDK Kotlin** | `./gradlew :kotlin-client:test` | JDK 21 |
| **SDK Java** | `./gradlew :java-client:test` | JDK 21 |
| **Koin** | `./gradlew :flagent-koin:test` | JDK 21 |
| **Playwright E2E** | `cd frontend/e2e && npx playwright test` | Backend + frontend running |

## Run everything that works locally (no Docker/Postgres)

```bash
./gradlew clean :backend:test :shared:jvmTest :frontend:jsTest :ktor-flagent:test :kotlin-client:test :java-client:test :flagent-koin:test --no-daemon -q
```

- **Backend test** excludes `*IntegrationTest` and tag `e2e` by default (so no Postgres/Docker needed).
- **Frontend jsTest** compiles and runs Kotlin/JS unit tests.

## Backend tests in detail

### Unit tests (default)

- **Task:** `:backend:test`
- **DB:** SQLite in-memory (`:memory:`).
- **Excluded:** Tests matching `*IntegrationTest`, and tests tagged `e2e` (unless `-PincludeE2E`).
- **Env:** Admin auth and API key are set in `build.gradle.kts` for tests.

### Integration tests (Postgres)

- **Task:** `:backend:test -PincludeIntegrationTests --tests "*IntegrationTest"`
- **DB:** Postgres. Set env or use CI:
  - `FLAGENT_DB_DBDRIVER=postgres`
  - `FLAGENT_DB_DBCONNECTIONSTR=jdbc:postgresql://localhost:5432/flagent_test?user=postgres&password=test`
- **CI:** GitHub Actions runs this job with a Postgres service container.

### E2E tests (tag `e2e`)

- **Task:** `:backend:test -PincludeE2E --tests "flagent.test.e2e.E2ETest"`
- **Needs:** Postgres via **Testcontainers** (Docker). Also `FLAGENT_RECORDER_ENABLED=false` when `-PincludeE2E` to avoid Kafka.
- **Note:** If Docker/Testcontainers are not available, these tests can fail with “EmbeddedServer was stopped” or “Connection refused”. CI runs them in a suitable environment.

### Load / performance tests

- Tagged `e2e` or `performance`; excluded by default.
- Run with `-PincludeE2E` (and optionally `-PincludeCompatibilityTests` for compatibility tests).

## Frontend tests

### Unit (Kotlin/JS)

```bash
./gradlew :frontend:jsTest
```

Runs tests in `frontend/src/jsTest/`.

### Playwright E2E

1. Start backend and frontend (e.g. `./gradlew run` or `:backend:runDev` + frontend dev server on port 8080).
2. Run:

```bash
cd frontend/e2e
npx playwright test
```

- **Smoke only:** `E2E_MODE=smoke npx playwright test`
- **One browser:** `npx playwright test --project=chromium`
- **List tests:** `npx playwright test --list`

Auth setup runs first (login + tenant), then tests use saved storage state.

## CI (GitHub Actions)

- **unit_test:** `:backend:test`, `:shared:jvmTest`, then `:backend:test --tests "flagent.test.e2e.E2ETest"`, then jacoco report.
- **integration_test:** Postgres service + `:backend:test -PincludeIntegrationTests --tests "*IntegrationTest"`.
- Other jobs: frontend build, SDKs, etc.

## Fixing “Unresolved reference” in backend test

If a backend test fails to compile with “Unresolved reference” to `flagent.test.bodyJsonObject` (or similar), that test may be in a package that doesn’t see the test helpers. Either:

- Use the same pattern as `E2ETest`: define local extension functions in the test class/file, or
- Ensure the test lives in a source set that compiles together with `flagent.test` (e.g. under `backend/src/test/kotlin`).

## Clean build if Kotlin cache errors appear

If you see “Could not close incremental caches” or “Storage … is already registered”:

```bash
./gradlew clean
./gradlew :backend:compileTestKotlin  # or the test task you need
```

Then run the test task again.
