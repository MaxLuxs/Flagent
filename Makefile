################################
### Public targets
################################

.PHONY: all
all: build test

.PHONY: build
build:
	@echo "Building Flagent..."
	@./gradlew build --no-daemon

.PHONY: test
test:
	@echo "Running tests..."
	@./gradlew test --no-daemon

.PHONY: clean
clean:
	@echo "Cleaning up..."
	@./gradlew clean --no-daemon
	@find . -name '*.log' -type f -delete
	@find . -name 'build' -type d -exec rm -rf {} + 2>/dev/null || true

.PHONY: run
run:
	@echo "Running Flagent server..."
	@./gradlew :backend:run --no-daemon

.PHONY: docker-build
docker-build:
	@echo "Building Docker image..."
	@docker build -t flagent:latest .

.PHONY: docker-run
docker-run:
	@echo "Running Docker container..."
	@docker run -it -p 18000:18000 -v flagent-data:/data flagent:latest

.PHONY: lint
lint:
	@echo "Running linters..."
	@./gradlew detekt --no-daemon || echo "detekt not configured, skipping"
	@./gradlew ktlintCheck --no-daemon || echo "ktlint not configured, skipping"

.PHONY: test-coverage
test-coverage:
	@echo "Generating test coverage report..."
	@./gradlew test jacocoTestReport --no-daemon
	@echo "Coverage report generated at: backend/build/reports/jacoco/test/html/index.html"

.PHONY: serve-docs
serve-docs:
	@echo "Serving documentation..."
	@npx -y docsify-cli serve docs

.PHONY: help
help:
	@echo "Available targets:"
	@echo "  make build         - Build the project"
	@echo "  make test          - Run tests"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make run           - Run the server locally"
	@echo "  make docker-build  - Build Docker image"
	@echo "  make docker-run    - Run Docker container"
	@echo "  make lint          - Run linters"
	@echo "  make test-coverage - Generate test coverage report"
	@echo "  make serve-docs    - Serve documentation locally"
	@echo "  make help          - Show this help message"
