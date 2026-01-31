# Frequently Asked Questions (FAQ)

Common questions about Flagent and their answers.

## General Questions

### What is Flagent?

Flagent is a production-ready, open-source feature flag and experimentation platform built with Kotlin/Ktor. It enables teams to safely deploy features, run A/B tests, and manage dynamic configurations with fine-grained targeting and real-time analytics.

### Is Flagent free?

Yes! Flagent is open-source and licensed under Apache 2.0. You can use it for free, including commercial use.

### What makes Flagent different from other feature flag solutions?

- **Modern Stack**: Built with Kotlin/Ktor and Kotlin Coroutines for high performance
- **Native Kotlin SDK**: First-class Kotlin support with modern language features
- **Self-Hosted**: Full control over your infrastructure and data
- **Production-Ready**: Built with Clean Architecture and best practices
- **Open Source**: Completely open-source with active development

### What makes Flagent stand out?

- **Kotlin/Ktor stack** – First Kotlin-native feature flag platform
- **Type-safe SDKs** – Compile-time validation, IDE autocomplete
- **Coroutines** – Native async/await, non-blocking I/O
- **High performance** – Sub-millisecond evaluation, EvalCache, client-side evaluation (Go/Kotlin Enhanced)
- **Clean Architecture** – DDD, testable, maintainable
- **Open source** – Apache 2.0, self-hosted, no vendor lock-in

## Installation & Deployment

### How do I get started with Flagent?

The fastest way is using Docker:

```bash
docker pull ghcr.io/maxluxs/flagent
docker run -d -p 18000:18000 ghcr.io/maxluxs/flagent
```

Then open `http://localhost:18000` in your browser. See [Getting Started](getting-started.md) for more details.

### What databases does Flagent support?

Flagent supports:
- **PostgreSQL** (recommended for production)
- **MySQL**
- **SQLite** (development only)

### Can I run Flagent in Kubernetes?

Yes! Flagent is stateless and can be deployed to Kubernetes. See [Deployment Guide](deployment.md) for Kubernetes configuration examples.

### What are the system requirements?

- **Memory**: Minimum 512MB, recommended 2GB+
- **CPU**: 1 core minimum, 2+ cores recommended
- **Database**: PostgreSQL 12+, MySQL 8+, or SQLite 3
- **Network**: HTTP/HTTPS access for API and UI

### Is Flagent production-ready?

Yes! Flagent is designed for production use with:
- High performance evaluation engine
- Horizontal scaling support
- Database connection pooling
- Health check endpoints
- Prometheus metrics
- Error handling and logging

## Feature Flags

### How do I create a feature flag?

You can create feature flags using:
1. **Web UI**: Navigate to `http://localhost:18000` and use the UI
2. **REST API**: POST to `/api/v1/flags` endpoint
3. **SDK**: Use client SDKs (Kotlin, JavaScript, Swift)

### How do I check if a feature flag is enabled?

Using Kotlin SDK:

```kotlin
val client = FlagentClient.create(baseUrl = "http://localhost:18000/api/v1")
val enabled = client.isEnabled("my-feature")
if (enabled) {
    // Feature is enabled
}
```

### How do I roll out a feature gradually?

1. Create a feature flag
2. Set rollout percentage (e.g., 10%)
3. Monitor metrics and errors
4. Gradually increase (10% → 50% → 100%)

See [Use Cases](use-cases.md) for detailed examples.

### Can I instantly disable a feature in production?

Yes! Feature flags can be instantly toggled on/off via UI or API without code changes or deployments. This is called a "kill switch".

## A/B Testing

### How does A/B testing work in Flagent?

1. Create a flag with multiple variants (e.g., control, variant_a, variant_b)
2. Set distribution percentages (e.g., 50/50 split)
3. Evaluate the flag in your code
4. Track metrics for each variant
5. Analyze results to determine the winner

### Is A/B testing deterministic?

Yes! Flagent uses MurmurHash3 hashing to ensure consistent assignment. The same user will always get the same variant.

### How do I calculate statistical significance?

Flagent provides statistical significance calculation in the Advanced Analytics feature. You can also export evaluation data to analyze externally.

### Can I run multi-variant tests?

Yes! Flagent supports A/B/n testing with any number of variants (e.g., A/B/C/D).

## SDKs & Integration

### What SDKs are available?

Official SDKs:
- **Kotlin** - For JVM, Android, Kotlin Multiplatform
- **JavaScript/TypeScript** - For Node.js, browser, React
- **Swift** - For iOS, macOS, tvOS

See [Client SDKs](https://github.com/MaxLuxs/Flagent/blob/main/README.md#client-libraries) for details.

### How do I integrate Flagent into my application?

1. Install the SDK for your language
2. Initialize the client with your Flagent URL
3. Check feature flags in your code
4. Deploy and start using feature flags

See [Getting Started](getting-started.md) for examples.

### Is there a Ktor plugin?

Yes! Flagent provides a native Ktor plugin (`ktor-flagent`) for seamless integration with Ktor applications.

### Can I use Flagent with Spring Boot?

Yes! You can use the Kotlin SDK or REST API directly from Spring Boot applications.

## Performance & Scalability

### How fast is Flagent?

Flagent achieves sub-millisecond evaluation times with in-memory caching. Typical evaluation time is under 1ms per request.

### How many evaluations per second can Flagent handle?

Flagent can handle 10,000+ evaluations per second per instance, depending on hardware and configuration.

### Can Flagent scale horizontally?

Yes! Flagent is stateless and can be deployed behind a load balancer. Each instance can handle evaluations independently.

### How much memory does Flagent use?

Base memory usage is approximately 200MB, varying based on the number of flags and cache size.

### Does Flagent cache flags?

Yes! Flagent uses in-memory caching with configurable TTL and refresh intervals. SDKs also cache flags locally to reduce server load.

## Security & Authentication

### How do I secure Flagent?

Flagent supports multiple authentication methods:
- JWT tokens
- Basic authentication
- Header-based API keys
- Cookie-based authentication

See [Configuration](configuration.md#authentication-configuration) for details.

### Can I use OAuth or SAML?

OAuth and SAML support is planned for future releases. Currently, you can use JWT tokens or API keys.

### Is my data secure?

Yes! Flagent:
- Supports encrypted database connections (SSL/TLS)
- Uses secure authentication methods
- Allows you to self-host on your infrastructure
- Never sends data to external services

## Configuration

### How do I configure Flagent?

All configuration is done via environment variables. See [Configuration Guide](configuration.md) for all options.

### Can I use a configuration file?

Currently, Flagent only supports environment variables. Configuration files may be added in future releases.

### How do I configure multi-environment (dev, staging, prod)?

You can:
1. Use separate database instances for each environment
2. Configure different Flagent instances with environment-specific settings
3. Use environment variables to switch configurations

See [Multi-Environment Management](https://github.com/MaxLuxs/Flagent/blob/main/README.md#multi-environment-management) for details.

## Troubleshooting

### Flagent won't start

Check:
1. Database connection string is correct
2. Database is accessible
3. Port 18000 is not already in use
4. Environment variables are set correctly

See logs for specific error messages.

### Feature flags are not working

Check:
1. Flag is enabled in Flagent UI
2. SDK is connected to correct Flagent URL
3. Entity context matches targeting rules
4. SDK is properly initialized

### Performance is slow

Check:
1. Database connection pooling is configured
2. Cache is enabled and working
3. Network latency to database
4. Database indexes are created

See [Performance Optimizations](features/performance-optimizations.md) for tips.

## Development & Contributing

### How do I contribute to Flagent?

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

See [Contributing Guide](contributing.md) for details.

### How do I report a bug?

Create an issue on [GitHub](https://github.com/MaxLuxs/Flagent/issues) with:
- Description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, versions, etc.)

### Can I request a feature?

Yes! Create a feature request on [GitHub](https://github.com/MaxLuxs/Flagent/issues) with:
- Description of the feature
- Use case and motivation
- Potential implementation approach

## Roadmap

### What features are planned?

Upcoming features:
- GitOps integration (feature flags as code)
- Advanced analytics with ML insights
- More SDKs (Python, Go, Java, .NET)
- Multi-region support
- Enterprise SSO (SAML, OIDC, LDAP)

See [Roadmap](roadmap.md) for full details.

### When will feature X be available?

Check the [Roadmap](roadmap.md) or [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) for planned features and timelines.

## Support

### Where can I get help?

- **GitHub Discussions** - Ask questions and share ideas
- **GitHub Issues** - Report bugs or request features
- **Documentation** - Complete guides and API reference
- **Code Examples** - Real-world examples and tutorials

### Is there commercial support available?

Commercial support may be available in the future. Contact us via GitHub for inquiries.

## License

### What license is Flagent under?

Flagent is licensed under Apache License 2.0. See [LICENSE](https://github.com/MaxLuxs/Flagent/blob/main/LICENSE) for details.

### Can I use Flagent commercially?

Yes! Apache 2.0 allows commercial use without restrictions.

---

**Still have questions?** [Open a GitHub Discussion](https://github.com/MaxLuxs/Flagent/discussions) or [create an issue](https://github.com/MaxLuxs/Flagent/issues).
