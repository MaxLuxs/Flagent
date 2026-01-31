# Changelog

All notable changes to Flagent will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Enterprise contract: `EnterpriseConfigurator.configureRoutes(Routing, EnterpriseBackendContext)` for registering tenant/billing/SSO routes from enterprise module
- `EnterpriseBackendContext`: `createTenantSchema`, `runTenantSchemaMigrations`, `dropTenantSchema` for tenant provisioning in enterprise
- Backend `Database`: tenant schema helpers for enterprise (`createTenantSchema`, `runTenantSchemaMigrations`, `dropTenantSchema`)
- When enterprise absent: core creates tenant/billing/SSO tables (self-hosted) and registers routes via `DefaultEnterpriseConfigurator.configureRoutes`
- When enterprise present: `EnterprisePlugin.configure()` runs migrations; route registration in enterprise is prepared (repos/services/routes move is follow-up)

### Changed
- Application.kt: tenant/billing/SSO middleware and routes only when enterprise absent; always calls `enterpriseConfigurator.configureRoutes()`; enterprise tables created by enterprise when present, by core when absent

### Fixed
- Fixed all broken links in README files
- Generated SDK documentation (JavaScript, Swift, Kotlin)
- Fixed SDK generation scripts to use correct project root path
- Updated .gitignore to include SDK documentation

### Changed
- SDK documentation is now included in repository for better discoverability

## [0.1.0] - 18-01-2025

### Added
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

## [0.1.0] - 2024-XX-XX

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

[Unreleased]: https://github.com/MaxLuxs/Flagent/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/MaxLuxs/Flagent/releases/tag/v0.1.0
