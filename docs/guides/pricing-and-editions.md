# Pricing and editions

Flagent is offered in three ways:

- **Open Source (self-hosted)** — Free. Use today. No limits on flags or experiments.
- **Flagent Cloud (SaaS)** — Planned; not yet available. Indicative pricing and tiers are subject to change.
- **Enterprise** — Custom pricing. Dedicated support, custom development, SLA, on-premise, training.

## What Flagent includes (not just feature flags)

Flagent is **feature flags + A/B experiments + analytics + crash analytics + integrations**:

- **Analytics** — Events (first_open, session_start, custom), evaluation counts, and in Enterprise: per-flag/variant insights and advanced metrics.
- **Crash analytics** — Ingestion and list of crash reports (all editions); in Enterprise: crash rate by flag and integration with Anomaly Detection and Smart Rollout.
- **Integrations** — Webhooks, Realtime (SSE), MCP for AI assistants, Export/Import (YAML/JSON).

See the comparison table below for what is supported in each edition.

## Feature comparison

| Category | Capability | Open Source | SaaS (planned) | Enterprise |
|----------|------------|-------------|----------------|------------|
| **Flags and experiments** | Feature flags | Yes | Yes | Yes |
| | A/B testing & experiments | Yes | Yes | Yes |
| | Gradual rollouts & kill switches | Yes | Yes | Yes |
| | Multi-environment, targeting, segments | Yes | Yes | Yes |
| | Client-side evaluation (offline-first) | Yes | Yes | Yes |
| | Official SDKs (Kotlin, JS, Swift, Python, Go, Java) | Yes | Yes | Yes |
| | Ktor plugin & REST API | Yes | Yes | Yes |
| | Admin UI & Debug Console | Yes | Yes | Yes |
| **Analytics** | Analytics events (first_open, session_start, custom) | Yes | Yes | Yes |
| | Evaluation counts & core metrics | Yes | Yes | Yes |
| | Advanced analytics & insights (per-flag/variant) | No | Yes | Yes |
| | Data recorders (Kafka, Kinesis, PubSub) | Yes | Yes | Yes |
| **Crash analytics** | Crash reporting (ingestion, list, stack traces) | Yes | Yes | Yes |
| | Crash rate by flag & anomaly integration | No | No | Yes |
| **Automation and reliability** | Anomaly detection & alerts | No | No | Yes |
| | Smart rollout (auto-rollback) | No | No | Yes |
| **Integrations and hosting** | Export/Import, Webhooks, Realtime (SSE), MCP | Yes | Yes | Yes |
| | Self-hosted deployment | Yes | Yes | Yes |
| | Managed cloud hosting | No | Yes | Yes |
| | On-premise, multi-tenancy | No | No | Yes |
| **Security and control** | Basic auth (JWT) | Yes | Yes | Yes |
| | RBAC | Basic | Basic | Custom roles |
| | SSO (SAML, OIDC) | No | No | Yes |
| | Audit log | Basic | Basic | Advanced |
| **Support** | Community / Priority / SLA / Dedicated | Yes / No / No / No | Yes / Yes / Yes / No | Yes / Yes / Yes / Yes |

## Flagent Cloud (SaaS) — planned

Flagent Cloud is **not yet launched**. Planned tiers (indicative; subject to change):

- **Starter** — Free tier (e.g. 100k evaluations/month), community support.
- **Pro** — ~$49–99/month, higher limits, SLA, priority support, advanced analytics.
- **Team** — From ~$199/month, multi-tenant, extended limits.

Star the [GitHub repo](https://github.com/MaxLuxs/Flagent) or watch [Issues](https://github.com/MaxLuxs/Flagent/issues) for updates on SaaS availability.

## Enterprise

Custom contracts: dedicated support, custom development, on-premise deployment, training, SLA. Contact: **max.developer.luxs@gmail.com**.

## Roadmap and sponsorship

- [Roadmap](roadmap.md) — Development phases and planned features.
- [Sponsor the project](https://github.com/sponsors/MaxLuxs) — Support Flagent development.
