# Pricing and editions

Flagent is offered in three ways:

- **Open Source (self-hosted)** — Free. Use today. No limits on flags or experiments.
- **Flagent Cloud (SaaS)** — Planned; not yet available. Contact us for a consultation.
- **Enterprise** — Custom. Dedicated support, custom development, SLA, on-premise, training. Contact us for a consultation.

## What Flagent includes (not just feature flags)

Flagent is **feature flags + A/B experiments + analytics + crash analytics + integrations**:

- **Analytics** — Events (first_open, session_start, custom), evaluation counts, and in Enterprise: per-flag/variant insights and advanced metrics.
- **Crash analytics** — Ingestion and list of crash reports (all editions); in Enterprise: crash rate by flag and integration with Anomaly Detection and Smart Rollout.
- **Integrations** — Webhooks, Realtime (SSE), MCP for AI assistants, Export/Import (YAML/JSON).

**In all editions:** feature flags, A/B experiments, gradual rollouts, kill switches, targeting & segments, client-side evaluation, official SDKs (Kotlin, JS, Swift, Python, Go, Java), Ktor plugin & REST API, Admin UI & Debug Console, analytics events & evaluation counts, crash ingestion & list, Export/Import, Webhooks, Realtime (SSE), MCP, basic auth (JWT). The table below shows **where editions differ**.

## Feature comparison

| Category | Capability | Open Source | SaaS (planned) | Enterprise |
|----------|------------|:-----------:|:--------------:|:-----------:|
| **Analytics** | Advanced analytics & insights (per-flag/variant) | — | ✓ | ✓ |
| **Crash** | Crash rate by flag & anomaly integration | — | — | ✓ |
| **Automation** | Anomaly detection & alerts | — | — | ✓ |
| | Smart rollout (auto-rollback) | — | — | ✓ |
| **Hosting** | Self-hosted | ✓ | ✓ | ✓ |
| | Managed cloud (Flagent Cloud) | — | ✓ | ✓ |
| | On-premise, multi-tenancy | — | — | ✓ |
| **Security** | RBAC | Basic | Basic | Custom roles |
| | SSO (SAML, OIDC) | — | — | ✓ |
| | Audit log | — | Basic | Advanced |
| **Support** | Community | ✓ | ✓ | ✓ |
| | Priority support | — | ✓ | ✓ |
| | 99.9% uptime SLA | — | ✓ | ✓ |
| | Dedicated support | — | — | ✓ |

*✓ = included, — = not included*

**Audit log:** Open Source does not persist audit events. SaaS (when available) and Enterprise provide who-changed-what audit trails; Enterprise adds advanced retention and filtering.

## Flagent Cloud (SaaS) — planned

Flagent Cloud is **not yet launched**. Planned tiers: Starter (entry), Pro (higher limits, SLA), Team (multi-tenant). To get notified when Flagent Cloud is available, [join the waitlist](https://github.com/MaxLuxs/Flagent/issues/new?template=cloud_waitlist) or star the repo and watch [Releases](https://github.com/MaxLuxs/Flagent/releases).

## Enterprise

See [Enterprise](enterprise.md) for full capabilities and contact. Custom contracts: dedicated support, custom development, on-premise deployment, training, SLA.

## Roadmap and sponsorship

- [Roadmap](roadmap.md) — Development phases and planned features.
- [Sponsor the project](https://github.com/sponsors/MaxLuxs) — Support Flagent development.
