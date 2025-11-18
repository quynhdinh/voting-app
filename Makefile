# Makefile to build and publish Docker images for services
# Usage: make build-all or make push-all

# Variables
VERSION ?= v1.0.0
REGISTRY ?= 
# If you want to use a specific registry, uncomment and configure:
# REGISTRY ?= docker.io/yourusername/
# REGISTRY ?= ghcr.io/yourusername/
# REGISTRY ?= your-registry.com/

SERVICES = user vote result contest

# Colors for output
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
RESET  := $(shell tput -Txterm sgr0)

.PHONY: help build-all build push-all push clean

help: ## Show this help
	@echo "$(GREEN)Available commands:$(RESET)"
	@echo ""
	@echo "  $(YELLOW)make build-all$(RESET)     - Build all Docker images"
	@echo "  $(YELLOW)make push-all$(RESET)      - Build and publish all images"
	@echo "  $(YELLOW)make build SERVICE=user$(RESET)  - Build a specific image"
	@echo "  $(YELLOW)make push SERVICE=user$(RESET)   - Build and publish a specific image"
	@echo "  $(YELLOW)make clean$(RESET)         - Clean local images"
	@echo ""
	@echo "Variables:"
	@echo "  VERSION=$(VERSION)  - Image version (default: v1.0.0)"
	@echo "  REGISTRY=$(REGISTRY)  - Docker registry (default: empty, uses local Docker Hub)"
	@echo ""

build-all: ## Build all images
	@echo "$(GREEN)Building all images...$(RESET)"
	@for service in $(SERVICES); do \
		echo "$(YELLOW)Building $$service:$(VERSION)...$(RESET)"; \
		$(MAKE) build SERVICE=$$service; \
	done
	@echo "$(GREEN)✓ All images built$(RESET)"

build: ## Build a specific image (usage: make build SERVICE=user)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(YELLOW)Error: Specify the service with SERVICE=<name>$(RESET)"; \
		echo "Available services: $(SERVICES)"; \
		exit 1; \
	fi
	@if [ ! -d "$(SERVICE)" ]; then \
		echo "$(YELLOW)Error: Service '$(SERVICE)' does not exist$(RESET)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Building $(SERVICE):$(VERSION)...$(RESET)"
	@cd $(SERVICE) && mvn clean package -DskipTests
	@echo "$(GREEN)Building Docker image $(SERVICE):$(VERSION)...$(RESET)"
	docker buildx build --platform linux/amd64,linux/arm64 -t $(REGISTRY)$(SERVICE):$(VERSION) -t $(REGISTRY)$(SERVICE):latest ./$(SERVICE)
	@echo "$(GREEN)✓ $(SERVICE):$(VERSION) built$(RESET)"

push-all: build-all ## Build and publish all images
	@echo "$(GREEN)Publishing all images...$(RESET)"
	@for service in $(SERVICES); do \
		echo "$(YELLOW)Publishing $$service:$(VERSION)...$(RESET)"; \
		$(MAKE) push SERVICE=$$service; \
	done
	@echo "$(GREEN)✓ All images published$(RESET)"

push: build ## Build and publish a specific image (usage: make push SERVICE=user)
	@if [ -z "$(SERVICE)" ]; then \
		echo "$(YELLOW)Error: Specify the service with SERVICE=<name>$(RESET)"; \
		echo "Available services: $(SERVICES)"; \
		exit 1; \
	fi
	@if [ -z "$(REGISTRY)" ]; then \
		echo "$(YELLOW)Warning: REGISTRY is not configured. Images will only be built locally.$(RESET)"; \
		echo "$(YELLOW)To publish, configure REGISTRY, for example: make push SERVICE=$(SERVICE) REGISTRY=docker.io/username/$(RESET)"; \
		exit 0; \
	fi
	@echo "$(GREEN)Publishing $(SERVICE):$(VERSION)...$(RESET)"
	docker push $(REGISTRY)$(SERVICE):$(VERSION)
	docker push $(REGISTRY)$(SERVICE):latest
	@echo "$(GREEN)✓ $(SERVICE):$(VERSION) published$(RESET)"

clean: ## Clean local images
	@echo "$(YELLOW)Cleaning local images...$(RESET)"
	@for service in $(SERVICES); do \
		docker rmi $(REGISTRY)$$service:$(VERSION) 2>/dev/null || true; \
		docker rmi $(REGISTRY)$$service:latest 2>/dev/null || true; \
	done
	@echo "$(GREEN)✓ Cleanup completed$(RESET)"

# Individual targets for each service
user: ## Build the user image
	$(MAKE) build SERVICE=user

vote: ## Build the vote image
	$(MAKE) build SERVICE=vote

result: ## Build the result image
	$(MAKE) build SERVICE=result

contest: ## Build the contest image
	$(MAKE) build SERVICE=contest

