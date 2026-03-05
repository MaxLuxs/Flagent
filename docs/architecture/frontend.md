# Frontend Architecture

## Overview

The Flagent admin UI is built with **Compose for Web** (Kotlin/JS). It provides a single-page application for managing feature flags, segments, experiments, and analytics, with the same design system and navigation as the documentation site where applicable.

## Stack

- **UI:** Compose for Web (Kotlin/JS)
- **HTTP:** Ktor client (or platform fetch)
- **State:** Component state and optional shared state
- **Build:** Gradle (JS target)

## Main Components

1. **Dashboard** — Quick stats, recent flags, quick actions (create flag, evaluation console).
2. **Flags List** — List all flags with filters (key, status, tags) and search; create/edit entry point.
3. **Flag Editor** — Create or edit a flag: key, description, status, targeting rules, variants, distributions.
4. **Segment Editor** — Create/edit segments and constraints (e.g. region, tier, custom attributes).
5. **Experiments** — List experiments and view metrics per variant; adjust distributions.
6. **Evaluation / Debug Console** — Single and batch evaluation with entity ID and context.
7. **Analytics** — Metrics over time; crash reporting when enabled.

## API Integration

The frontend uses the backend REST API:

- `GET /api/v1/flags` — list flags
- `POST /api/v1/flags` — create flag
- `PUT /api/v1/flags/{id}` — update flag
- `POST /api/v1/evaluation` — evaluate (single/batch)
- Segments, experiments, and analytics use the corresponding API endpoints.

All requests are sent from the browser to the same origin (or configured API base URL) with auth (e.g. JWT, cookie) when enabled.

## Documentation

- [Frontend UI Guide](../guides/frontend-ui.md) — how to use the admin UI (sections, tips, navigation).
- [Backend architecture](backend.md) — API and server structure.
