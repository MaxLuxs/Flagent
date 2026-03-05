# Why Flagent?

A short guide to Flagent's value proposition and how it compares to other feature-flag and experimentation platforms.

## Why Flagent?

- **Kotlin-native** — The first feature-flag platform built with Kotlin and Ktor at the core. Type-safe SDKs, coroutines, and clean architecture. No wrappers or second-class support: your backend and mobile stack speak the same language as the platform.
- **Self-hosted first** — Your data stays on your infrastructure. Deploy on-premise or in your own cloud; no mandatory SaaS. Optional [Flagent Cloud](pricing-and-editions.md) (planned) for those who want managed hosting later.
- **OpenFeature-friendly** — Evaluation follows industry-standard semantics. You can migrate from or integrate with OpenFeature providers and keep a consistent mental model for flags and experiments.
- **One platform** — Feature flags, A/B experiments, analytics events, evaluation counts, and crash ingestion in a single UI and API. No juggling multiple tools for rollout, experimentation, and observability.
- **Enterprise when you need it** — [Enterprise](enterprise.md) adds multi-tenancy, SSO (SAML, OIDC), RBAC, audit log, Smart Rollout (automated gradual rollouts), Anomaly Detection (alerts, optional rollback), and crash-by-flag analytics. Start with open source; upgrade when your organisation demands it.

## Comparison

| Dimension | Flagent | LaunchDarkly | Unleash | OpenFeature (backend-agnostic) |
|-----------|---------|--------------|---------|-------------------------------|
| **Language / framework** | Kotlin-native (Ktor, coroutines) | Polyglot (SDKs for many languages) | Node/Java-centric, polyglot SDKs | Provider-agnostic; many SDKs |
| **Hosting** | Self-hosted; Cloud planned | SaaS + on-prem/PD | Self-hosted, Open Source; SaaS (Unleash Cloud) | Depends on provider |
| **Open source** | Yes (Apache 2.0) | No | Yes (Unleash Open Source) | Spec + open SDKs; backends vary |
| **Experiments (A/B)** | Yes | Yes | Yes | Depends on provider |
| **Analytics** | Events, evaluation counts; advanced in Enterprise | Yes | Yes (integrations) | Depends on provider |
| **Crash by flag** | Yes (OSS: list; Enterprise: by flag + anomaly) | Via integrations | Via integrations | Depends on provider |
| **SDKs** | Kotlin (KMP), JS, Swift, Python, Go, Java, Ktor plugin | Many (LD SDKs) | Many (Unleash SDKs) | Many (OpenFeature SDKs) |
| **Enterprise** | SSO, RBAC, audit, Smart Rollout, Anomaly (see [Enterprise](enterprise.md)) | Full enterprise suite | Pro/Enterprise tiers | Depends on provider |

Sources: public documentation and marketing pages of each product (LaunchDarkly, Unleash, OpenFeature). Comparison is for orientation only; verify current offerings before decisions.

## When to choose Flagent

- **Kotlin / Ktor / JVM teams** — You want a feature-flag and experimentation stack that is native to your stack, with first-class Ktor plugin and type-safe Kotlin SDKs.
- **Self-hosted requirement** — Compliance, data residency, or preference for running the system yourself. Flagent gives you a single deployable backend and UI.
- **Flags + experiments + analytics in one place** — You prefer one admin UI, one API, and one set of concepts (flags, segments, variants, evaluation) instead of stitching several tools.
- **Future Enterprise needs** — You expect to need SSO, RBAC, audit, or automated rollouts later. Flagent’s open-source core and optional Enterprise build let you start simple and grow.

For a quick run-through, see [Getting Started](getting-started.md). For performance characteristics, see [Benchmarks](../performance/benchmarks.md).
