.PHONY: generate-kotlin generate-javascript generate-swift generate-java generate-dart generate-go generate-python generate all

generate-kotlin:
	@echo "Generating Kotlin SDK..."
	cd kotlin && ./generate.sh

generate-javascript:
	@echo "Generating JavaScript/TypeScript SDK..."
	cd javascript && ./generate.sh

generate-swift:
	@echo "Generating Swift SDK..."
	cd swift && ./generate.sh

generate-java:
	@echo "Generating Java SDK..."
	cd java && ./generate.sh

generate-dart:
	@echo "Generating Dart SDK..."
	cd dart && ./generate.sh

generate-go:
	@echo "Generating Go SDK..."
	cd go && ./generate.sh

generate-python:
	@echo "Generating Python SDK..."
	cd python && ./generate.sh

generate: generate-kotlin generate-javascript generate-swift generate-java generate-dart generate-go generate-python

all: generate
