# Flagent Documentation

Feature flags, A/B testing, dynamic configuration. Documentation index.

**View locally:** Docs use [Docsify](https://docsify.js.org/) and load content over the network. Opening `docs.html` via `file://` will show 404. Run a local server from the **`docs/`** folder (not the project root), then open `http://localhost:PORT/docs.html`. If **Home** shows the wrong page, hard refresh (Ctrl+Shift+R / Cmd+Shift+R).

```bash
cd docs && python3 -m http.server 8080
# open http://localhost:8080/docs.html
```

---

## Start Here

| I want to… | Go to |
|------------|-------|
| **Run Flagent in 5 min** | [Quick Start](quickstart.html) — set DB and admin env (see [Configuration](guides/configuration.md)) |
| **See all SDKs** | [SDKs Overview](sdk.html) |
| **Try the API** | [API Reference](api-docs.html) |
| **Use the Admin UI** | [Frontend UI Guide](guides/frontend-ui.md) |
| **Try the UI with demo data** | [Demo Data](guides/demo-data.md) |
| **Read step-by-step** | [Getting Started](guides/getting-started.md) |

---

## Documentation Structure

- **Getting Started** — [Getting Started](guides/getting-started.md), [Configuration](guides/configuration.md), [Deployment](guides/deployment.md)
- **Guides** — Versioning, FAQ, Use Cases, GitOps, [Frontend UI](guides/frontend-ui.md), and more (see sidebar)
- **API** — [OpenAPI Spec](api/openapi.yaml), [Endpoints](api/endpoints.md), [Examples](examples/README.md)
- **SDKs** — [SDKs Overview](sdk.html), [SDK Integration Examples](examples/sdk-integration.md)

---

## Flagent Cloud — Coming Soon

Managed Flagent in the cloud — no setup, no maintenance. Fully managed feature flags and A/B testing as a service. [Home](index.html)

---

## Guides

| Topic | EN | RU |
|-------|----|----|
| Getting Started | [Guide](guides/getting-started.md) | [Русский](guides/getting-started.ru.md) |
| Configuration | [Guide](guides/configuration.md) | [Русский](guides/configuration.ru.md) |
| Deployment | [Guide](guides/deployment.md) | [Русский](guides/deployment.ru.md) |
| Versioning | [Guide](guides/versioning.md) | [Русский](guides/versioning.ru.md) |
| Compatibility | [Guide](guides/compatibility.md) | — |
| FAQ | [Guide](guides/faq.md) | [Русский](guides/faq.ru.md) |
| MCP (AI Assistants) | [Guide](guides/mcp.md) | [Русский](guides/mcp.ru.md) |
| Use Cases | [Guide](guides/use-cases.md) | [Русский](guides/use-cases.ru.md) |
| GitOps | [Guide](guides/gitops.md) | [Русский](guides/gitops.ru.md) |
| Preview Environments | [Guide](guides/preview-environments.md) | [Русский](guides/preview-environments.ru.md) |
| Trunk-Based Development | [Guide](guides/trunk-based-development.md) | [Русский](guides/trunk-based-development.ru.md) |
| Build-Time Verification | [Guide](guides/build-time-verification.md) | [Русский](guides/build-time-verification.ru.md) |
| Declarative UI | [Guide](guides/declarative-ui.md) | — |
| VS Code Extension | [Guide](guides/vscode-extension.md) | [Русский](guides/vscode-extension.ru.md) |
| Publishing | [Guide](guides/publishing.md) | [Русский](guides/publishing.ru.md) |
| Roadmap | [Guide](guides/roadmap.md) | — |
| Contributing | [Guide](guides/contributing.md) | — |

---

## API & Examples

| Resource | Link | Русский |
|----------|------|---------|
| OpenAPI Spec | [api/openapi.yaml](api/openapi.yaml) | — |
| Endpoints Reference | [api/endpoints.md](api/endpoints.md) | [Русский](api/endpoints.ru.md) |
| SDKs Overview | [sdks/README.md](sdks/README.md) | [Русский](sdks/README.ru.md) |
| **Examples** | [examples/README.md](examples/README.md) · [Русский](examples/README.ru.md) | — |
| **SDK Integration** | [examples/sdk-integration.md](examples/sdk-integration.md) — Ktor, Spring, Kotlin, JS, Swift | — |
| Tutorial: Gradual Rollout | [tutorials/gradual-rollout.md](tutorials/gradual-rollout.md) | — |

---

## Architecture & Features

| Topic | Link | Русский |
|-------|------|---------|
| Backend | [architecture/backend.md](architecture/backend.md) | [Русский](architecture/backend.ru.md) |
| Frontend | [architecture/frontend.md](architecture/frontend.md) | [Русский](architecture/frontend.ru.md) |
| Evaluation Spec | [architecture/evaluation-spec.md](architecture/evaluation-spec.md) | [Русский](architecture/evaluation-spec.ru.md) |
| Offline-First SDK | [features/offline-first-sdk.md](features/offline-first-sdk.md) | [Русский](features/offline-first-sdk.ru.md) |
| Performance Optimizations | [features/performance-optimizations.md](features/performance-optimizations.md) | [Русский](features/performance-optimizations.ru.md) |
| Real-Time Updates | [features/real-time-updates.md](features/real-time-updates.md) | [Русский](features/real-time-updates.ru.md) |
| Benchmarks | [performance/benchmarks.md](performance/benchmarks.md) | [Русский](performance/benchmarks.ru.md) |
| Tuning | [performance/tuning-guide.md](performance/tuning-guide.md) | [Русский](performance/tuning-guide.ru.md) |

---

## Documentation style and layout

- **`styles.css`** — global UI: navbar, footer, buttons, typography. Single source of truth for header/footer; all pages (index, quickstart, sdk, docs, blog) use the same `.navbar` and `.footer`. Do not change nav/footer structure without updating this file.
- **`docsify-custom.css`** — layout and look **only for the Docs page** (`docs.html`): fixed sidebar (260px), content area, markdown section typography, GitHub corner size, `--docs-navbar-height`. No Vue theme; full custom layout.
- **Navbar vs sidebar:** Navbar = site-level links (Home, Quick Start, SDKs, API, Docs, Blog). Sidebar (`_sidebar.md`) = doc tree only (Getting Started, Guides, etc.). No duplicate entries between the two.

---

## Folder Structure

```
docs/
├── README.md           ← you are here
├── docs.html           # Docs viewer (unified navbar + footer)
├── docsify-custom.css  # Docsify theme overrides
├── quickstart.html     # 5-min start
├── sdk.html            # SDK overview
├── api-docs.html       # Swagger UI
├── _sidebar.md         # Sidebar navigation
├── guides/             # User guides (incl. frontend-ui.md)
├── sdks/               # SDK overview (README.md)
├── examples/           # API + SDK examples
├── api/                # OpenAPI spec
├── architecture/       # Backend, frontend, eval spec
├── features/           # SDK features
├── performance/        # Benchmarks, tuning
├── tutorials/         # Step-by-step tutorials
├── tasks/             # Redirect to internal docs (see tasks/README.md)
└── gh_pages/          # Landing (index.html) for GitHub Pages
```
