# Flagent Frontend E2E Tests

Playwright-based end-to-end tests for the Flagent web UI.

## Prerequisites

- Node.js 18+
- Backend running (see below)

## Quick Start

### E2E with Production Build (recommended)

One server, no webpack dev. Script builds production frontend and runs tests against backend.

1. Start backend from repo root:
   ```bash
   ./gradlew :backend:runDev
   ```

2. In another terminal, run E2E:
   ```bash
   cd frontend/e2e
   npm install
   npx playwright install chromium   # first-time only
   npm run test:oss   # or test:tenant, test:auth
   ```
   The script builds `:frontend:jsBrowserProductionWebpack` and uses backend (18000) for both API and UI.

### Local Development (dev server)

1. Start both from repo root: `./gradlew run` (backend 18000, frontend 8080)
2. Run tests: `npm run test` (uses FRONTEND_URL=8080)

### CI Mode

When `CI=true`, Playwright starts servers via `./gradlew run` before tests.

## Commands

| Command | Description |
|---------|-------------|
| `npm run test` | Run all E2E tests (headless, requires backend running) |
| `npm run test:headed` | Run tests with browser visible |
| `npm run test:ui` | Open Playwright UI mode |
| `npm run test:debug` | Run in debug mode |
| `npm run report` | Open last test report |
| `npm run test:oss` | **Run OSS scenario** (starts backend, creates tenant, runs "Open Source" tests) |
| `npm run test:tenant` | **Run Tenant scenario** (starts backend, runs "With Tenant" tests) |
| `npm run test:auth` | **Run Auth scenario** (starts backend, runs "With Auth" tests) |
| `npm run test:all-scenarios` | **Run all 3 scenarios sequentially** (OSS → Tenant → Auth) |

## Environment Variables

| Variable | Default | Description |
|----------|---------|--------------|
| `FRONTEND_URL` | http://localhost:8080 (or BACKEND_URL for prod) | Frontend base URL |
| `BACKEND_URL` | http://localhost:18000 | Backend API URL |
| `CI` | - | When set, starts servers automatically |
| `FLAGENT_ADMIN_EMAIL` | admin@local | Admin email for auth scenario |
| `FLAGENT_ADMIN_PASSWORD` | admin | Admin password for auth scenario (matches runDev) |
| `FLAGENT_ADMIN_API_KEY` | - | X-Admin-Key for tenant creation (when admin auth enabled) |

## Test Structure

- `tests/landing.spec.ts` - Landing page, navigation buttons
- `tests/dashboard.spec.ts` - Dashboard stats and layout
- `tests/flags.spec.ts` - Flags list, create flag, flag detail
- `tests/flag-editor.spec.ts` - Flag edit: enable/disable, description, variants, segments, history, delete
- `tests/create-flag.spec.ts` - Create flag via /flags/new (3 scenarios: OSS, With Tenant, With Auth)
- `tests/debug-console.spec.ts` - Debug console evaluation
- `tests/navigation.spec.ts` - Navbar, URL navigation, all routes
- `tests/analytics.spec.ts` - Analytics page
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
