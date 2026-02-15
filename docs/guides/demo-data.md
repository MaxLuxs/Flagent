# Demo Data: Full Tenant with Flags, Analytics, Crashes

To explore the UI with realistic data (flags, segments, A/B experiments, analytics charts, crash reports), use the demo data seeder.

## Quick start

1. Start the backend:
   ```bash
   ./gradlew :backend:runDev
   ```

2. Run the seeder:
   ```bash
   ./scripts/seed-demo-data.sh
   ```

3. Copy the printed **API Key** and open the app (e.g. http://localhost:8080). In **Settings**, paste the API key (or set it in localStorage). Then open:
   - **Dashboard** — overview
   - **Flags** — list and edit flags, segments, variants
   - **Analytics** — Overview (evaluation counts), Events, By flags
   - **Metrics** — Crash dashboard (if enabled)

## What gets created

- **Tenant** — new tenant with default environment (unless you use an existing key).
- **Flags** — 6 flags: boolean toggle, A/B experiment, segment with constraint (e.g. `tier=premium`), gradual rollout, and two extra for analytics.
- **Evaluation events** — hundreds of evaluation API calls so Core metrics (Overview, per-flag stats) show real charts.
- **Analytics events** — Firebase-style events (e.g. `first_open`, `screen_view`, `purchase`) for the Analytics → Events tab.
- **Crash reports** — a few sample crash reports for the Crash dashboard.

## Seed into your existing tenant

If you already have an API key and want to add demo data to that tenant:

```bash
FLAGENT_API_KEY=your-existing-api-key ./scripts/seed-demo-data.sh
```

No new tenant is created; all flags, evaluations, and events are added to the tenant of that key.

## Fixed tenant key (e.g. for demos)

To create a tenant with a fixed key (e.g. `demo`) so you can re-run the script and always target the same tenant (new tenant only if it doesn’t exist yet):

```bash
FLAGENT_DEMO_TENANT_KEY=demo ./scripts/seed-demo-data.sh
```

## Environment variables

| Variable | Description |
|----------|-------------|
| `BACKEND_URL` | Backend base URL (default: `http://localhost:18000`) |
| `FLAGENT_ADMIN_API_KEY` | Admin key for creating tenants (default: `dev-admin-key`) |
| `FLAGENT_API_KEY` | If set, seed into this tenant instead of creating a new one |
| `FLAGENT_DEMO_TENANT_KEY` | If set, create tenant with this key (e.g. `demo`) |

## E2E tests and analytics data

The frontend E2E suite can create a tenant and seed analytics (flags + evaluations) automatically in tests that need it (e.g. Analytics page tests). To run only the “seed + analytics” flow as a one-off (e.g. to get an API key for manual testing), run the analytics tests with tenant support:

```bash
cd frontend/e2e
npm run test:tenant
# or run a single analytics test that seeds data
npx playwright test tests/analytics.spec.ts -g "Overview tab shows"
```

The bash script `scripts/seed-demo-data.sh` is the recommended way to get a full demo tenant with all data (flags, evaluations, analytics events, crashes) in one go.
