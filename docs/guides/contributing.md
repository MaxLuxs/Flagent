# Contributing to Flagent

Thank you for your interest in contributing to Flagent! We welcome contributions from the community.

## How to Contribute

There are many ways to contribute to Flagent:

- **Report bugs** - Help us improve by reporting issues
- **Suggest features** - Share your ideas for new features
- **Improve documentation** - Help others understand Flagent better
- **Submit code** - Fix bugs or implement new features
- **Write tests** - Improve test coverage
- **Star the project** - Show your support

## Getting Started

### Prerequisites

- **JDK 21+** for backend development
- **Gradle 8.13** (wrapper) for building
- **Docker** for running services
- **Git** for version control

### Development Setup

1. **Fork the repository**

   ```bash
   git clone https://github.com/YOUR_USERNAME/Flagent.git
   cd Flagent
   ```

2. **Add upstream remote**

   ```bash
   git remote add upstream https://github.com/MaxLuxs/Flagent.git
   ```

3. **Install hooks (optional but recommended)**

   ```bash
   ./scripts/install-hooks.sh
   ```
   This installs a pre-commit hook that checks VERSION sync when releasing. See [Versioning](versioning.md).

4. **Build the project**

   ```bash
   ./gradlew build
   ```

5. **Run the application**

   ```bash
   ./gradlew :backend:run
   ```

   The server will start on `http://localhost:18000`

## Code Style

Flagent follows Kotlin Coding Conventions. See [.cursorrules](https://github.com/MaxLuxs/Flagent/blob/main/.cursorrules) for detailed guidelines.

### Key Points

- Use **Clean Architecture** principles
- Follow **Domain-Driven Design (DDD)** patterns
- Write **type-safe** code (avoid `Any`, `String?` where possible)
- Use **suspend functions** for all I/O operations
- Write **comprehensive tests** for new features
- Document **public APIs** with KDoc

### Code Formatting

We use Kotlin's standard formatting. Run:

```bash
./gradlew ktlintFormat
```

To check formatting:

```bash
./gradlew ktlintCheck
```

## Project Structure

```
flagent/
├── backend/          # Ktor backend server
│   ├── domain/       # Domain layer (entities, use cases, interfaces)
│   ├── service/      # Application layer (services, DTOs, mappers)
│   ├── repository/   # Infrastructure layer (database, cache)
│   ├── route/        # Presentation layer (HTTP handlers)
│   └── config/       # Configuration
├── frontend/         # Compose for Web frontend
├── ktor-flagent/     # Ktor plugin
└── sdk/              # Client SDKs
```

## Development Workflow

1. **Create a feature branch**

   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**

   - Write clean, testable code
   - Add tests for new features
   - Update documentation if needed

3. **Test your changes**

   ```bash
   ./gradlew test
   ```

4. **Commit your changes**

   ```bash
   git commit -m "Add: description of your changes"
   ```

   Use conventional commit messages:
   - `Add:` for new features
   - `Fix:` for bug fixes
   - `Update:` for updates to existing features
   - `Refactor:` for code refactoring
   - `Docs:` for documentation changes

5. **Push to your fork**

   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**

   - Open a PR on GitHub
   - Describe your changes clearly
   - Reference related issues if any
   - Wait for review and feedback

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :backend:test

# Run tests with coverage
./gradlew test jacocoTestReport
```

**Backend test tags and properties:**

- By default, **E2E and load tests** (tag `e2e`) are **excluded** so the suite does not require Kafka. To include them (e.g. for full app flow), run with `-PincludeE2E`; the build sets `FLAGENT_RECORDER_ENABLED=false` so a recorder/Kafka is not required.
- **Integration tests** (suffix `*IntegrationTest`) are excluded by default. Run with `-PincludeIntegrationTests` to include them (see [Deployment](deployment.md) for DB options).
- **Compatibility** and **performance** tags are excluded by default; use `-PincludeCompatibilityTests` if needed.

### Writing Tests

- Write **unit tests** for use cases, services, and repositories
- Write **integration tests** for routes and full scenarios
- Use **MockK** for mocking interfaces
- Use **TestContainers** for integration tests with databases

### Test Coverage

Aim for at least **80% code coverage** for critical business logic.

## Documentation

### Code Documentation

- Document all **public APIs** with KDoc
- Include **usage examples** in KDoc where helpful
- Document **exceptions** that can be thrown
- Use `@param` and `@return` for complex functions

### User Documentation

- Update [docs/](https://github.com/MaxLuxs/Flagent/tree/main/docs) for user-facing documentation
- Add examples for new features
- Update README if needed
- Include code examples

## Architecture Guidelines

### Clean Architecture

Flagent follows Clean Architecture with clear layer separation:

- **Domain Layer** - Business entities and use cases (no dependencies)
- **Application Layer** - Services and DTOs (depends on domain)
- **Infrastructure Layer** - Repositories and external services (depends on domain)
- **Presentation Layer** - Routes and HTTP handlers (depends on application)

### Dependency Direction

Dependencies should point inward:
```
Presentation → Application → Domain ← Infrastructure
```

### Domain-Driven Design

- Use **entities** for business objects
- Use **value objects** for immutable values
- Use **repositories** (interfaces) for data access
- Use **use cases** for business logic

## Pull Request Process

1. **Ensure your code follows style guidelines**
2. **All tests must pass**
3. **Add tests for new features**
4. **Update documentation if needed**
5. **Request review from maintainers**

### PR Checklist

- [ ] Code follows style guidelines
- [ ] Tests added/updated and passing
- [ ] Documentation updated
- [ ] Commit messages are clear
- [ ] No merge conflicts

## Reporting Bugs

When reporting bugs, please include:

1. **Description** - Clear description of the issue
2. **Steps to reproduce** - Detailed steps to reproduce the bug
3. **Expected behavior** - What should happen
4. **Actual behavior** - What actually happens
5. **Environment** - OS, JDK version, Flagent version
6. **Logs** - Relevant error logs or stack traces

Create an issue on [GitHub](https://github.com/MaxLuxs/Flagent/issues) with the label `bug`.

## Requesting Features

When requesting features, please include:

1. **Description** - Clear description of the feature
2. **Use case** - Why is this feature needed?
3. **Motivation** - What problem does it solve?
4. **Proposed solution** - How should it work?
5. **Alternatives** - Other solutions considered

Create an issue on [GitHub](https://github.com/MaxLuxs/Flagent/issues) with the label `enhancement`.

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Accept constructive criticism gracefully
- Focus on what is best for the community

### Unacceptable Behavior

- Harassment or discriminatory language
- Trolling or insulting comments
- Publishing others' private information
- Other conduct inappropriate for a professional setting

## Questions?

- [GitHub Issues](https://github.com/MaxLuxs/Flagent/issues) - Ask questions, report bugs, or request features
- [Documentation](getting-started.md) - Read the documentation
- **Contact** - max.developer.luxs@gmail.com

## Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in relevant documentation

Thank you for contributing to Flagent!
