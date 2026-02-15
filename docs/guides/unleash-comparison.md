# Flagent vs Unleash — Feature Comparison

This document summarizes open-source Unleash functionality and how Flagent compares, for prioritization and roadmap.

## Overview

- **Unleash** — Open-source feature flag server (Node/Java). Many enterprises use it.
- **Flagent** — Kotlin/Ktor-based feature flags, A/B testing, and dynamic configuration. Offline-first SDK, MCP for AI assistants.

## Feature Comparison

| Area | Unleash (OSS) | Flagent |
|------|----------------|---------|
| **Feature flags** | Toggles, variants, strategies | ✅ Flags, variants, segments, distributions |
| **Targeting** | Strategies, constraints | ✅ Segments with constraints (attr in/not in, etc.) |
| **A/B experiments** | Built-in | ✅ Variants + distributions, experiment insights |
| **Environments** | Multiple (dev, prod) | ✅ Multi-tenancy, environment by token |
| **API** | REST, GraphQL | ✅ REST, OpenAPI |
| **SDK** | Client SDKs (many languages) | ✅ KMP SDK (Android, iOS, JVM, JS), Ktor plugin |
| **Offline / cache** | Varies by SDK | ✅ Offline-first, TTL cache, bootstrap |
| **Admin UI** | React app | ✅ Compose for Web (Kotlin/JS) |
| **GitOps / Export** | Export/import | ✅ YAML/JSON export, GitOps guide |
| **Webhooks** | Yes | ✅ Flag events |
| **Audit** | Enterprise | ✅ Snapshots, history (OSS) |
| **MCP / AI** | — | ✅ MCP server (evaluate, list, create, update, analyze) |

## Possible Additions (from Unleash)

1. **Feature dependencies** — Flag A depends on Flag B (e.g. "payment_v2" only when "new_checkout" is on). Could be implemented as a constraint type or explicit dependency graph.
2. **Change requests** — Approval workflow for flag changes. Fits enterprise; could be a separate workflow layer.
3. **Projects / Applications** — Group flags by project or application. Flagent has tags; projects could be a first-class grouping.
4. **Archive flags** — Soft delete / archive instead of hard delete. Flagent has restore; archive could be a status.
5. **Flag usage analytics** — Which flags are evaluated most, by which apps. Flagent has metrics and top flags; can be extended.

## Prioritization

- **High:** Feature dependencies (safety), Archive (cleanup without loss).
- **Medium:** Projects/Applications (organization), Change requests (enterprise).
- **Low:** Additional Unleash-specific strategy names (can be mapped to segments/constraints).

## Implementation status

Flagent already provides: flags with variants and segments, constraints, distributions, multi-tenancy, REST API, offline-first SDK, Admin UI (Compose for Web), export/import, webhooks, flag history, and MCP for AI assistants. The items above (dependencies, change requests, projects, archive, usage analytics) are tracked as enhancements; see [Roadmap](roadmap.md) for planned work.

## References

- [Unleash docs](https://docs.getunleash.io/)
- [Flagent Roadmap](roadmap.md)
