.PHONY: generate-kotlin generate-javascript generate-swift generate all

generate-kotlin:
	@echo "Generating Kotlin SDK..."
	cd kotlin && ./generate.sh

generate-javascript:
	@echo "Generating JavaScript/TypeScript SDK..."
	cd javascript && ./generate.sh

generate-swift:
	@echo "Generating Swift SDK..."
	cd swift && ./generate.sh

generate: generate-kotlin generate-javascript generate-swift

all: generate
