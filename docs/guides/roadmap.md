# Roadmap

> [English](#english) | [Русский](#русский)

---

## English

**Flagent** is the first Kotlin-native feature flag platform. This roadmap outlines development phases and planned features.

### Current Status (Q1 2026)

**Done:** Full CRUD (flags, segments, variants, constraints, distributions), evaluation engine (MurmurHash3), EvalCache, Kafka/Kinesis/PubSub recorders, PostgreSQL/MySQL/SQLite, Admin UI (Compose for Web), Kotlin/JS/Swift SDKs + Enhanced variants, Ktor plugin, Client-side evaluation (Kotlin/Go Enhanced), Real-time SSE (Kotlin/Go Enhanced), Python SDK, Go SDK + Go Enhanced, Java SDK, Spring Boot Starter, Docker, CI/CD, Debug UI (Kotlin/Swift/JS), Helm chart. **Import/export:** YAML/JSON import (POST /import) and export in Settings (OSS). **Enterprise (internal module):** Multi-tenancy (tenants, API keys), SSO (SAML + OAuth/OIDC), RBAC (custom roles), Smart Rollout and Anomaly Detection (rules/metrics-based).

**In progress:** Documentation improvements.

### Planned

| Phase   | Focus                    | Key items                                      |
|---------|--------------------------|------------------------------------------------|
| Q2 2026 | Community & GitOps      | ✅ YAML/JSON import/export done. CLI, Webhooks planned. |
| Q3 2026 | Scale & integrations    | Edge service, public benchmarks. |
| Q4 2026 | Enterprise               | ✅ Multi-tenancy, SSO, RBAC, Smart Rollout, Anomaly done (Enterprise). Audit logs, compliance planned. |

Detailed plans and internal strategy remain in private docs. For contributions and feature requests, see [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) and [Contributing](guides/contributing.md).

---

## Русский

**Flagent** — первая Kotlin-native платформа для feature flags. В дорожной карте — фазы разработки и запланированные возможности.

### Текущее состояние (Q1 2026)

**Сделано:** Полный CRUD (флаги, сегменты, варианты, ограничения, распределения), движок evaluation (MurmurHash3), EvalCache, рекордеры Kafka/Kinesis/PubSub, PostgreSQL/MySQL/SQLite, Admin UI (Compose for Web), SDK Kotlin/JS/Swift + Enhanced, плагин Ktor, client-side evaluation (Kotlin/Go Enhanced), Real-time SSE (Kotlin/Go Enhanced), Python SDK, Go SDK + Go Enhanced, Java SDK, Spring Boot Starter, Docker, CI/CD, Debug UI (Kotlin/Swift/JS), Helm chart. **Импорт/экспорт:** YAML/JSON (POST /import, настройки). **Enterprise (модуль internal):** мультитенантность, SSO (SAML + OAuth/OIDC), RBAC (кастомные роли), Smart Rollout и Anomaly Detection (на правилах/метриках).

**В работе:** Улучшение документации.

### Планы

| Фаза    | Фокус                     | Основное                                       |
|---------|---------------------------|------------------------------------------------|
| Q2 2026 | Сообщество и GitOps       | ✅ Импорт/экспорт YAML/JSON готов. CLI, Webhooks в планах. |
| Q3 2026 | Масштаб и интеграции      | Edge-сервис, публичные бенчмарки. |
| Q4 2026 | Enterprise                | ✅ Multi-tenancy, SSO, RBAC, Smart Rollout, Anomaly готовы (Enterprise). Аудит, соответствие в планах. |

Детальные планы и внутренняя стратегия — в приватной документации. По контрибуции и запросам фич: [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) и [Contributing](guides/contributing.md).
