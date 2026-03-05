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
| **Understand why Flagent** | [Why Flagent](guides/why-flagent.md) — value props and comparison |
| **See all SDKs** | [SDKs Overview](sdk.html) |
| **Try the API** | [API Reference](api-docs.html) |
| **Use the Admin UI** | [Frontend UI Guide](guides/frontend-ui.md) |
| **Try the UI with demo data** | [Demo Data](guides/demo-data.md) |
| **Read step-by-step** | [Getting Started](guides/getting-started.md) |
| **Tutorial: gradual rollout (Ktor)** | [Gradual Rollout New Payment (30 min)](tutorials/gradual-rollout-ktor-payment.md) |
| **Performance** | [Benchmarks](performance/benchmarks.md) — evaluation API baselines and how to reproduce |
| **30-min Ktor tutorial** | [Gradual Rollout: Ktor Payment](tutorials/gradual-rollout-ktor-payment.md) |

---

## Documentation Structure

- **Getting Started** — [Getting Started](guides/getting-started.md), [Configuration](guides/configuration.md), [Deployment](guides/deployment.md)
- **Guides** — Versioning, FAQ, Use Cases, GitOps, [Frontend UI](guides/frontend-ui.md), and more (see sidebar)
- **API** — [OpenAPI Spec](api/openapi.yaml), [Endpoints](api/endpoints.md), [Examples](examples/README.md)
- **SDKs** — [SDKs Overview](sdk.html), [SDK Integration Examples](examples/sdk-integration.md)

---

## Flagent Cloud — Coming Soon

Managed Flagent in the cloud — no setup, no maintenance. Fully managed feature flags and A/B testing as a service. [Join the waitlist](https://github.com/MaxLuxs/Flagent/issues/new?template=cloud_waitlist.md) to get notified when it's available. [Home](index.html)

**Blog:** [Blog index](blog/index.html) — product updates and guides (e.g. [Feature flags the Kotlin way](blog/2026-03-feature-flags-kotlin-way.html), [Self-hosted Flagent + Ktor](blog/2026-03-self-hosted-flagent-ktor.html)).

---

## Guides

| Topic | EN |
|------------|-----|
| Getting Started | [Guide](guides/getting-started.md) |
| Configuration | [Guide](guides/configuration.md) |
| Deployment | [Guide](guides/deployment.md) |
| Versioning | [Guide](guides/versioning.md) |
| Compatibility | [Guide](guides/compatibility.md) |
| FAQ | [Guide](guides/faq.md) |
| MCP (AI Assistants) | [Guide](guides/mcp.md) |
| Enterprise | [Guide](guides/enterprise.md) |
| Use Cases | [Guide](guides/use-cases.md) |
| GitOps | [Guide](guides/gitops.md) |
| Preview Environments | [Guide](guides/preview-environments.md) |
| Trunk-Based Development | [Guide](guides/trunk-based-development.md) |
| Build-Time Verification | [Guide](guides/build-time-verification.md) |
| Declarative UI | [Guide](guides/declarative-ui.md) |
| VS Code Extension | [Guide](guides/vscode-extension.md) |
| Publishing | [Guide](guides/publishing.md) |
| Roadmap | [Guide](guides/roadmap.md) |
| Contributing | [Guide](guides/contributing.md) |

---

## API & Examples

| Resource | Link |
|----------|------|
| OpenAPI Spec | [api/openapi.yaml](api/openapi.yaml) |
| Endpoints Reference | [api/endpoints.md](api/endpoints.md) |
| SDKs Overview | [sdks/README.md](sdks/README.md) |
| **Examples** | [examples/README.md](examples/README.md) |
| **SDK Integration** | [examples/sdk-integration.md](examples/sdk-integration.md) — Ktor, Spring, Kotlin, JS, Swift |
| Tutorial: Gradual Rollout | [tutorials/gradual-rollout.md](tutorials/gradual-rollout.md) |
| Tutorial: Ktor Payment (30 min) | [tutorials/gradual-rollout-ktor-payment.md](tutorials/gradual-rollout-ktor-payment.md) |

---

## Architecture & Features

| Topic | Link |
|-------|------|
| Backend | [architecture/backend.md](architecture/backend.md) |
| Frontend | [architecture/frontend.md](architecture/frontend.md) |
| Evaluation Spec | [architecture/evaluation-spec.md](architecture/evaluation-spec.md) |
| Offline-First SDK | [features/offline-first-sdk.md](features/offline-first-sdk.md) |
| Performance Optimizations | [features/performance-optimizations.md](features/performance-optimizations.md) |
| Real-Time Updates | [features/real-time-updates.md](features/real-time-updates.md) |
| Benchmarks | [performance/benchmarks.md](performance/benchmarks.md) — Evaluation API: sub-ms to low-ms latency at thousands of req/s; [reproduce](performance/benchmarks.md#reproduce-these-numbers) |
| Tuning | [performance/tuning-guide.md](performance/tuning-guide.md) |

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
└── gh_pages/          # Landing (index.html) for GitHub Pages
```
