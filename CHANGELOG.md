# Changelog

All notable changes to Flagent will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

(No changes yet)

## [0.1.5] - 2025-02-08

### Added
- Pre-commit hook and `check-version-sync.sh` to enforce VERSION consistency across the project
- `scripts/install-hooks.sh` for one-time hook setup after clone
- Extended `sync-version.sh` to update docs, SDK READMEs, and frontend ShellLayout

### Changed
- Unified Maven artifact names (kotlin-client, kotlin-enhanced, kotlin-debug-ui) in docs and build configs
- Documentation restructure: internal plans moved to internal/docs/, deployment JAR paths corrected

## [0.1.4] - 2025-02-08

### Added
- Core metrics: `evaluation_events` table, `CoreMetricsService`, `EvaluationEventRepository`, `GET /api/v1/metrics/overview` (OSS only when enterprise absent)
- Enterprise contract: `EnterpriseConfigurator.configureRoutes(Routing, EnterpriseBackendContext)` for registering tenant/billing/SSO routes from enterprise module
- `EnterpriseBackendContext`: `createTenantSchema`, `runTenantSchemaMigrations`, `dropTenantSchema` for tenant provisioning in enterprise
- Backend `Database`: tenant schema helpers for enterprise (`createTenantSchema`, `runTenantSchemaMigrations`, `dropTenantSchema`)
- When enterprise absent: core creates tenant/billing/SSO tables (self-hosted) and registers routes via `DefaultEnterpriseConfigurator.configureRoutes`
- When enterprise present: `EnterprisePlugin.configure()` runs migrations; route registration in enterprise is prepared (repos/services/routes move is follow-up)

### Fixed
- Fixed all broken links in README files
- Generated SDK documentation (JavaScript, Swift, Kotlin)
- Fixed SDK generation scripts to use correct project root path
- Updated .gitignore to include SDK documentation

### Changed
- SDK documentation is now included in repository for better discoverability
- Application.kt: tenant/billing/SSO middleware and routes only when enterprise absent; always calls `enterpriseConfigurator.configureRoutes()`; enterprise tables created by enterprise when present, by core when absent

## [0.1.0] - 2025-01-18

### Added
- Initial release of Flagent
- Feature flags management
- A/B testing and experimentation
- Dynamic configuration
- REST API for flags management and evaluation
- Kotlin/Ktor backend
- Compose for Web frontend
- Multiple database support (PostgreSQL, MySQL, SQLite)
- Authentication middleware (JWT, Basic, Header, Cookie)
- Data recording (Kafka, Kinesis, PubSub)
- OpenAPI/Swagger documentation
- Kotlin and JavaScript SDKs
- Professional CI/CD infrastructure with GitHub Actions
- Docker multi-stage build support
- Comprehensive documentation with Docsify (English and Russian)
- GitHub templates for Issues and Pull Requests
- Security scanning with CodeQL
- Code coverage tracking with Codecov
- Makefile for build automation
- LICENSE (Apache 2.0)

### Changed
- Improved README.md with professional badges and structure
- Enhanced documentation structure

### Security
- Added CodeQL security scanning workflow

[Unreleased]: https://github.com/MaxLuxs/Flagent/compare/v0.1.5...HEAD
[0.1.5]: https://github.com/MaxLuxs/Flagent/compare/v0.1.4...v0.1.5
[0.1.4]: https://github.com/MaxLuxs/Flagent/compare/v0.1.0...v0.1.4
[0.1.0]: https://github.com/MaxLuxs/Flagent/releases/tag/v0.1.0
