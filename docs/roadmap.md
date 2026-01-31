# Roadmap

> [English](#english) | [Русский](#русский)

---

## English

**Flagent** is the first Kotlin-native feature flag platform. This roadmap outlines development phases and planned features.

### Current Status (Q1 2026)

**Done:** Full CRUD (flags, segments, variants, constraints, distributions), evaluation engine (MurmurHash3), EvalCache, Kafka/Kinesis/PubSub recorders, PostgreSQL/MySQL/SQLite, Admin UI (Compose for Web), Kotlin/JS/Swift SDKs + Enhanced variants, Ktor plugin, Client-side evaluation (Kotlin/Go Enhanced), Real-time SSE (Kotlin/Go Enhanced), Python SDK, Go SDK + Go Enhanced, Docker, CI/CD.

**In progress:** Documentation, Kubernetes/Helm.

### Planned

| Phase   | Focus                    | Key items                                      |
|---------|--------------------------|------------------------------------------------|
| Q2 2026 | Community & GitOps      | Feature flags as code (YAML/CLI), Webhooks, Java SDK |
| Q3 2026 | Scale & integrations    | Edge service, Debug UI, public benchmarks      |
| Q4 2026 | Enterprise               | Multi-tenancy, SSO/SAML, RBAC, AI rollouts (optional) |

Detailed plans and internal strategy remain in private docs. For contributions and feature requests, see [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) and [Contributing](contributing.md).

---

## Русский

**Flagent** — первая Kotlin-native платформа для feature flags. В дорожной карте — фазы разработки и запланированные возможности.

### Текущее состояние (Q1 2026)

**Сделано:** Полный CRUD (флаги, сегменты, варианты, ограничения, распределения), движок evaluation (MurmurHash3), EvalCache, рекордеры Kafka/Kinesis/PubSub, PostgreSQL/MySQL/SQLite, Admin UI (Compose for Web), SDK Kotlin/JS/Swift + Enhanced, плагин Ktor, client-side evaluation (Kotlin/Go Enhanced), Real-time SSE (Kotlin/Go Enhanced), Python SDK, Go SDK + Go Enhanced, Docker, CI/CD.

**В работе:** Документация, Kubernetes/Helm.

### Планы

| Фаза    | Фокус                     | Основное                                       |
|---------|---------------------------|------------------------------------------------|
| Q2 2026 | Сообщество и GitOps       | Feature flags as code (YAML/CLI), Webhooks, Java SDK |
| Q3 2026 | Масштаб и интеграции      | Edge-сервис, Debug UI, публичные бенчмарки     |
| Q4 2026 | Enterprise                | Multi-tenancy, SSO/SAML, RBAC, AI rollouts (опционально) |

Детальные планы и внутренняя стратегия — в приватной документации. По контрибуции и запросам фич: [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) и [Contributing](contributing.md).
