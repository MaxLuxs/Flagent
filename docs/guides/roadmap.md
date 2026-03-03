# Roadmap

**This document is the single source of truth for product development.** Use it for planning, prioritisation, and alignment. See also PRD (internal/private docs) for vision, positioning, and monetisation directions. All “Done” items are verified against the codebase; “Planned” is ordered by priority.

**Flagent** is the first Kotlin-native feature flag platform. Below: what is **done** (Phase 1, Q1 2026) and what is **planned** by priority.

---

### Phase 1: Foundation (Q1 2026) — Done

**Goal:** Production-ready core and integrations.

| Area | Done |
|------|------|
| **Core** | Feature flags, A/B testing, evaluation engine (MurmurHash3), EvalCache, multi-environment, segments, variants, constraints, distributions. |
| **Admin & tools** | Admin UI (Compose for Web), Debug Console (evaluation testing), Experiments UI (variant comparison, distribution charts). |
| **Analytics & crash** | Evaluation counts, analytics events ingestion; crash report ingestion and list (OSS). See [pricing-and-editions](pricing-and-editions.md). |
| **Data** | Data recorders: Kafka, Kinesis, PubSub. |
| **Backend** | PostgreSQL, MySQL, SQLite; Docker, Docker Compose; CI/CD; [Helm chart](../../helm/flagent/). |
| **Benchmarks** | k6 load tests (evaluation, metrics, anomaly) in [infrastructure/load-tests](../../infrastructure/load-tests/); [load-test workflow](https://github.com/MaxLuxs/Flagent/blob/main/.github/workflows/load-test.yml) in CI (report + PR comment); [benchmarks doc](../performance/benchmarks.md) (targets, run instructions); [tuning guide](../performance/tuning-guide.md). |
| **SDKs** | Kotlin (KMP), Kotlin Enhanced (client-side eval, SSE), **flagent-koin** (Koin DI), JavaScript/TypeScript, Swift, Swift Enhanced, Python (asyncio), Go, Go Enhanced (client-side eval, SSE), Java, Spring Boot Starter; Dart, Flutter Enhanced; Ktor plugin; Debug UI (Kotlin, Swift, JS). Gradle plugin: verify-flags, optional [strict flag keys](build-time-verification.md#scenario-b-strict-mode-only-flagkeys-or-flagkey) (generateFlagKeys, allowOnlyGeneratedOrAnnotated). |
| **Import/export** | YAML/JSON import (POST /import) and export from Settings (OSS). |
| **Webhooks** | **Outgoing:** configurable URLs for flag events (created/updated/deleted/enabled/disabled); any endpoint (e.g. Slack, Datadog) via Settings → Webhooks. **Incoming:** GitHub webhook — auto-create flag from PR branch name ([GitOps](gitops.md#github-webhook)). |
| **MCP** | Model Context Protocol for AI assistants ([MCP guide](mcp.md)). |
| **Enterprise** *(internal module)* | Multi-tenancy (tenants, API keys, X-Tenant-ID), SSO (SAML + OAuth/OIDC), RBAC, Smart Rollout, Anomaly Detection (rules/metrics-based), crash-by-flag, Slack notifications. |

**In progress:** Documentation improvements.

**References:** [Configuration](configuration.md) · [Deployment](deployment.md) · [Versioning](versioning.md) · [Contributing](contributing.md)

---

### Phase 2: Community & GitOps (Q2 2026)

**Goal:** Community and product–market fit.

| Priority | Planned |
|----------|---------|
| 1 | **CLI binary** — Releasable package for automation (script `scripts/flagent-cli.sh` exists today). |
| 2 | **.NET SDK** — Official client for .NET. |
| 3 | **Edge Service** — Scale SDK evaluation at the edge. |
| 4 | **Debug UI** — Broader coverage where not yet available. |
| 5 | **Published benchmark baselines** — Baseline numbers in docs or a dedicated page (infrastructure and CI already in place). |

---

### Phase 3: Enterprise & SaaS (Q3–Q4 2026)

**Goal:** Enterprise operations and SaaS launch.

- **Audit logs** — Who changed what, when; compliance-friendly.
- **Compliance** — Policies and docs for regulated use.
- **Advanced analytics** — Usage, experiments, impact.
- **Flagent Cloud (SaaS)** — Managed offering; beta and launch.

---

### Phase 4: Scale & market (2027+)

**Goal:** Market leadership and enterprise scale.

- **AI Anomaly Detection** — ML-driven alerts and rollback hints.
- **Predictive targeting** — Data-driven audience selection.
- **Terraform / Pulumi** — IaC for flags and environments.
- **Kubernetes Operator** — Deploy and manage Flagent in K8s.
- **SOC 2** — Certification and trust center.
- **Enterprise SLA** — Guarantees and support tiers.

---

Contributions and feature requests: [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) · [Contributing](contributing.md)
