# Flagent Documentation

Feature flags, A/B testing, dynamic configuration. Documentation index.

---

## Start Here

| I want to… | Go to |
|------------|-------|
| **Run Flagent in 5 min** | [Quick Start](quickstart.html) |
| **See all SDKs** | [SDKs Overview](sdk.html) |
| **Try the API** | [API Reference](api-docs.html) |
| **Read step-by-step** | [Getting Started](guides/getting-started.md) |

---

## Flagent Cloud — Coming Soon

Managed Flagent in the cloud — no setup, no maintenance. Fully managed feature flags and A/B testing as a service. [Home](/)

---

## Guides

| Topic | EN | RU |
|-------|----|----|
| Getting Started | [Guide](guides/getting-started.md) | [Русский](guides/getting-started.ru.md) |
| Configuration | [Guide](guides/configuration.md) | [Русский](guides/configuration.ru.md) |
| Deployment | [Guide](guides/deployment.md) | [Русский](guides/deployment.ru.md) |
| Versioning | [Guide](guides/versioning.md) | — |
| Compatibility | [Guide](guides/compatibility.md) | — |
| FAQ | [Guide](guides/faq.md) | — |
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

| Resource | Link |
|----------|------|
| OpenAPI Spec | [api/openapi.yaml](api/openapi.yaml) |
| Endpoints Reference | [api/endpoints.md](api/endpoints.md) |
| **Examples** | [examples/README.md](examples/README.md) · [Русский](examples/README.ru.md) — API, Ktor, config |
| **SDK Integration** | [examples/sdk-integration.md](examples/sdk-integration.md) — Ktor, Spring, Kotlin, JS, Swift |
| Tutorial: Gradual Rollout | [tutorials/gradual-rollout.md](tutorials/gradual-rollout.md) |

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
| Benchmarks | [performance/benchmarks.md](performance/benchmarks.md) |
| Tuning | [performance/tuning-guide.md](performance/tuning-guide.md) |

---

## Folder Structure

```
docs/
├── README.md           ← you are here
├── quickstart.html     # 5-min start
├── sdk.html            # SDK overview
├── api-docs.html       # Swagger UI
│
├── guides/             # User guides
├── examples/           # API + SDK examples
├── api/                # OpenAPI spec
├── architecture/       # Backend, frontend, eval spec
├── features/           # SDK features
├── performance/       # Benchmarks, tuning
└── tutorials/          # Step-by-step tutorials
```
