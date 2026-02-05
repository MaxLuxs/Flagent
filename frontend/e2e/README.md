# Flagent Frontend E2E Tests

Playwright-based end-to-end tests for the Flagent web UI.

## Prerequisites

- Node.js 18+
- Backend running (see below)

## Quick Start

### Local Development (dev server)

1. Start backend and frontend from repo root:
   ```bash
   ./gradlew run
   ```
   (backend 18000, frontend 8080)

2. In another terminal, run E2E:
   ```bash
   cd frontend/e2e
   npm install
   npx playwright install chromium   # first-time only
   npm run test:smoke   # fast smoke (~1-2 min)
   # or
   npm run test:full    # full OSS suite
   ```

Auth is set up once per run (login + tenant) and saved to `playwright/.auth/user.json` for all tests.

### E2E with Production Build

One server, no webpack dev. Script builds production frontend and runs tests against backend.

1. Start backend from repo root: `./gradlew :backend:runDev`
2. In another terminal: `cd frontend/e2e && npm run test:oss-scenario` (or `test:tenant`, `test:auth`)

### CI Mode

When `CI=true`, Playwright starts servers via `./gradlew run` before tests.

## Commands

| Command | Description |
|---------|-------------|
| `npm run test` | Run all E2E tests (headless, requires backend running) |
| `npm run test:smoke` | **Smoke tests** — 8 key tests (~1-2 min), use for quick validation |
| `npm run test:full` | **Full OSS suite** — all @oss tests |
| `npm run test:headed` | Run tests with browser visible |
| `npm run test:ui` | Open Playwright UI mode |
| `npm run test:debug` | Run in debug mode |
| `npm run report` | Open last test report |
| `npm run test:oss` | Run OSS tests (FLAGENT_EDITION=oss) |
| `npm run test:enterprise` | Run Enterprise tests (FLAGENT_EDITION=enterprise) |
| `npm run test:oss-scenario` | **OSS scenario** (run with backend; creates tenant, runs create-flag Open Source) |
| `npm run test:tenant` | **Run Tenant scenario** (with backend, create-flag With Tenant) |
| `npm run test:auth` | **Run Auth scenario** (with backend, create-flag With Auth) |
| `npm run test:all-scenarios` | **Run all 3 scenarios** (oss-scenario → tenant → auth) |

## Environment Variables

| Variable | Default | Description |
|----------|---------|--------------|
| `FRONTEND_URL` | http://localhost:8080 (or BACKEND_URL for prod) | Frontend base URL |
| `BACKEND_URL` | http://localhost:18000 | Backend API URL |
| `CI` | - | When set, starts servers automatically |
| `FLAGENT_EDITION` | - | `oss` or `enterprise` — run only tests tagged @oss or @enterprise |
| `E2E_MODE` | - | `smoke` — run only @smoke tests |
| `FLAGENT_ADMIN_EMAIL` | admin@local | Admin email for auth scenario |
| `FLAGENT_ADMIN_PASSWORD` | admin | Admin password for auth scenario (matches runDev) |
| `FLAGENT_ADMIN_API_KEY` | - | X-Admin-Key for tenant creation (when admin auth enabled) |

## Edition and Mode Filtering

Tests are tagged with `@oss`, `@enterprise`, and `@smoke`. Use `FLAGENT_EDITION` and `E2E_MODE` to filter:

| FLAGENT_EDITION | E2E_MODE | Runs |
|-----------------|----------|------|
| `oss` | `smoke` | @oss + @smoke: dashboard, flags, create-flag, navigation, landing, debug-console, settings, analytics |
| `oss` | (full) | All @oss tests |
| `enterprise` | - | Tests tagged @enterprise |

```bash
npm run test:smoke       # Smoke tests (fast, for CI)
npm run test:full        # Full OSS suite
npm run test:oss         # Same as test:full
npm run test:enterprise  # Enterprise tests only
```

## Test Structure

- `tests/landing.spec.ts` - Landing page, navigation buttons
- `tests/dashboard.spec.ts` - Dashboard stats and layout
- `tests/flags.spec.ts` - Flags list, create flag, flag detail
- `tests/flag-editor.spec.ts` - Flag edit: enable/disable, description, variants, segments, history, delete
- `tests/create-flag.spec.ts` - Create flag via /flags/new (3 scenarios: OSS, With Tenant, With Auth)
- `tests/debug-console.spec.ts` - Debug console evaluation
- `tests/navigation.spec.ts` - Navbar, URL navigation, all routes
- `tests/analytics.spec.ts` - Analytics page (Overview, By flags, per-flag metrics). Seed data: creates flags and evaluation events. For full coverage (Overview cards, chart, top flags), use OSS backend with Core metrics.
- `tests/settings.spec.ts` - Settings page
- `tests/experiments.spec.ts` - Experiments (A/B) page

## Create Flag Scenarios

The `create-flag.spec.ts` runs 3 scenarios (9 tests total):

| Scenario | When it runs | Setup |
|----------|--------------|-------|
| **Open Source** | Backend does not require tenant | Clears localStorage |
| **With Tenant** | Backend has `/admin/tenants` | Creates tenant via API, sets `api_key` |
| **With Auth** | Backend has auth + tenant | Logs in, creates tenant, sets `auth_token` + `api_key` |

For **With Tenant** and **With Auth** to pass, configure the backend:

```bash
# For With Tenant: allow tenant creation without auth, or set admin key
FLAGENT_ADMIN_API_KEY=your-admin-key  # optional, for X-Admin-Key

# For With Auth: enable admin login
FLAGENT_ADMIN_AUTH_ENABLED=true
FLAGENT_ADMIN_EMAIL=admin@local
FLAGENT_ADMIN_PASSWORD=admin123
FLAGENT_JWT_AUTH_SECRET=your-secret-min-32-chars
```

E2E tests use `FLAGENT_ADMIN_EMAIL` and `FLAGENT_ADMIN_PASSWORD` from env (default: admin@local / admin).

### Running Specific Scenarios

Use `test:oss`, `test:tenant`, `test:auth` — they build production frontend and run against backend (single server):

```bash
# Terminal 1: backend
./gradlew :backend:runDev

# Terminal 2: E2E
cd frontend/e2e
npm run test:oss      # OSS: clears localStorage
npm run test:tenant   # Tenant: creates tenant via API, sets api_key
npm run test:auth     # Auth: login + tenant, sets auth_token + api_key
```

## Browsers

Tests run on Chromium, Firefox, and WebKit by default. To run on a single browser:

```bash
npx playwright test --project=chromium
```
