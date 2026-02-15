# Current status and next steps

> See [Roadmap](../guides/roadmap.md) for phases and [Contributing](../guides/contributing.md) for how to run tests and contribute.

## Recent (Q1 2026)

- **Experiments UI (OSS):** Variant comparison table, distribution chart, filters (All/Enabled/Disabled) on Experiments page.
- **Core metrics:** Usage-by-client endpoint; `start`/`end` required for `/flags/{id}/usage`; tests and backend fixes.
- **Enterprise:** Change requests (create/list/approve/reject/apply), Projects/Applications/Instances (backend + frontend: list, create, breadcrumbs).
- **Backend tests:** E2E and load tests tagged `e2e` and excluded by default; run with `-PincludeE2E` and no Kafka. UserRepository Exposed fixes; CoreMetricsRoutes tests for missing params.

## In progress

- **Documentation** — roadmap lists “Documentation improvements” as in progress.

## Next steps (by priority)

1. **Documentation** — keep improving guides (configuration, testing, MCP, frontend UI), add or update examples where needed.
2. **Optional: Projects polish** — filter flags by `projectId` (GET `/flags?projectId=…`), link API key to Instance for Projects (if desired).
3. **Optional: Newsletter** — connect landing-page “Subscribe” (LandingFooter) to a backend endpoint or external provider (e.g. Mailchimp/SendGrid) when needed.
4. **Q2+** — YAML/JSON import/export and CLI script are done; released CLI binary and more webhooks (see roadmap).

## Running tests

- Default: `./gradlew :backend:test` (e2e excluded).
- With E2E: `./gradlew :backend:test -PincludeE2E`.
- Frontend: `./gradlew :frontend:jsTest`; E2E: see [demo-data](../guides/demo-data.md).
- Dart SDK: `cd sdk/dart && dart test`.
- Samples & SDK (Kotlin Debug UI, Flutter App): see [Testing (samples & SDK)](verification-and-tests.md).
