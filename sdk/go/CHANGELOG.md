# Changelog

All notable changes to the Flagent Go SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-01-27

### Added
- Initial release of Flagent Go SDK
- Basic HTTP client with retry logic and exponential backoff
- Context support for cancellation and timeouts
- Type-safe models for all API resources
- Evaluation API support (single and batch)
- Flag management API (get, list)
- Snapshot API for client-side evaluation
- Health check endpoint
- Comprehensive test coverage
- Examples and documentation
- Helper functions for pointer creation

### Features
- **Idiomatic Go** - Clean, simple API following Go conventions
- **Context Support** - Full context.Context support
- **Type-Safe** - Strongly typed models with proper error handling
- **Auto-Retry** - Automatic retry on network errors
- **Connection Pooling** - Efficient HTTP connection management
- **Error Hierarchy** - Structured error types for better error handling
- **Configurable** - Multiple client options for customization

[0.1.0]: https://github.com/MaxLuxs/Flagent/releases/tag/go-sdk-v0.1.0
