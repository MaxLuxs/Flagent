# Flagent Admin UI (Frontend)

The Flagent admin UI is a Compose for Web application that runs alongside the backend. It provides a single interface for managing feature flags, segments, experiments, and viewing analytics.

## Access

- **When using Docker:** Open `http://localhost:18000` — the UI is served by the same server.
- **When running locally:** Backend serves UI at port 18000; for frontend dev with hot reload, the frontend can run on port 8080 (`./gradlew run`).

Default credentials (when auth is enabled): `admin@local` / `admin`.

## Main Sections

### Dashboard

- **Quick stats** — total flags, enabled count, recent activity.
- **Quick actions** — create flag, open evaluation console, go to flags/segments.
- **Recent flags** — list of recently updated flags.

Use the Dashboard as the starting point after login.

### Flags

- **List** — all feature flags with filters (key, status, tags) and search.
- **Create** — create a new flag (boolean, variants, or other value types).
- **Edit** — change key, description, status, targeting rules, variants, and distributions.
- **Targeting** — define segments and rules so the flag is enabled only for certain entities (e.g. by region, tier, custom context).

Best practice: give flags a clear key (e.g. `new_checkout_flow`) and a short description.

### Segments

- **List** — all segments used in targeting.
- **Create / Edit** — define constraints (e.g. `region` in `["US","EU"]`, `tier == "premium"`).

Segments are reusable; multiple flags can use the same segment.

### Experiments

- **List** — experiments (flags with variants and distributions).
- **Metrics** — view assignment counts and metrics per variant.
- **Distributions** — set variant weights (e.g. 50% control, 50% treatment).

Use experiments for A/B tests and gradual rollouts.

### Evaluation / Debug Console

- **Single evaluation** — enter flag key, entity ID, and optional context; see the evaluated variant and reason.
- **Batch** — evaluate multiple flags for one entity in one request.

Use this to verify that targeting and distributions behave as expected before releasing.

### Analytics

- **Metrics** — usage and evaluation metrics over time.
- **Crash reporting** (if enabled) — view and group crashes; link to flags if applicable.

## Tips

1. **Use segments** for repeated targeting logic instead of duplicating rules on each flag.
2. **Test in Debug Console** after changing targeting or distributions.
3. **Use gradual rollout** — start with a small percentage for a variant, then increase after validation.
4. **Name flags and variants clearly** so analytics and reports are easy to understand.

## Architecture

The frontend is built with **Compose for Web** (Kotlin/JS). It talks to the backend REST API; all state is loaded from the server. For technical details, see [Architecture: Frontend](../architecture/frontend.md).

## Keyboard and Navigation

- Top navbar: Home, Quick Start, SDKs, API, Docs (this documentation).
- Sidebar in the app: Dashboard, Flags, Segments, Experiments, Analytics, etc.
- Use search/filters on list pages to find flags or segments quickly.

## Further Reading

- [Getting Started](getting-started.md) — install and run Flagent.
- [Configuration](configuration.md) — environment variables and auth.
- [Deployment](deployment.md) — Docker, Compose, and production setup.
