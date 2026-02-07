.PHONY: generate-kotlin generate-javascript generate-swift generate-java generate all

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

generate: generate-kotlin generate-javascript generate-swift generate-java

all: generate
