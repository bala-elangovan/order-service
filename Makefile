.PHONY: help build format swagger run-api run-consumer run-all infra-up infra-down postgres-up postgres-down postgres-clean kafka-up kafka-down kafka-clean kafka-topics-create kafka-topics kafka-produce kafka-produce-many kafka-produce-fulfillment kafka-consumer-groups kafka-lag clean-all

help: ## Show available commands
	@echo "Order Management Service - Makefile"
	@echo "===================================="
	@echo ""
	@echo "Build:"
	@echo "  make build               - Build all modules"
	@echo "  make format              - Format code with Spotless"
	@echo ""
	@echo "Run Applications:"
	@echo "  make run-api             - Run the Order API service (port 8080)"
	@echo "  make run-consumer        - Run the Order Consumer service (port 8081)"
	@echo "  make run-all             - Run both API and Consumer services"
	@echo ""
	@echo "Development:"
	@echo "  make swagger             - Open Swagger UI in browser"
	@echo "  make infra-up            - Start all infrastructure (Postgres + Kafka)"
	@echo "  make infra-down          - Stop all infrastructure"
	@echo ""
	@echo "PostgreSQL:"
	@echo "  make postgres-up         - Start PostgreSQL container"
	@echo "  make postgres-down       - Stop PostgreSQL container"
	@echo "  make postgres-clean      - Stop and remove PostgreSQL volume"
	@echo ""
	@echo "Kafka:"
	@echo "  make kafka-up                - Start Kafka and Zookeeper"
	@echo "  make kafka-down              - Stop Kafka and Zookeeper"
	@echo "  make kafka-clean             - Stop and remove Kafka volumes"
	@echo "  make kafka-topics-create     - Create all Kafka topics"
	@echo "  make kafka-topics            - List all Kafka topics"
	@echo ""
	@echo "  Produce Events:"
	@echo "  make kafka-produce           - Produce a sample order event"
	@echo "  make kafka-produce-many      - Produce random order events (COUNT=100)"
	@echo "  make kafka-produce-fulfillment ORDER_ID=<id> - Produce release + shipment events"
	@echo ""
	@echo "  Consumer Monitoring:"
	@echo "  make kafka-consumer-groups   - List all consumer groups"
	@echo "  make kafka-lag               - Show consumer lag for all groups"
	@echo ""
	@echo "Cleanup:"
	@echo "  make clean-all           - Stop all containers and remove all volumes"
	@echo ""

# =============================================================================
# Build
# =============================================================================

build: ## Build all modules
	@echo "Building all modules..."
	./gradlew build -x test

format: ## Format code with Spotless
	@echo "Formatting code..."
	./gradlew spotlessApply

# =============================================================================
# Run Applications
# =============================================================================

run-api: ## Run the Order API service (port 8080)
	@echo "Starting Order API service on port 8080..."
	./gradlew :orders:orders-app:bootRun --args='--spring.profiles.active=local'

run-consumer: ## Run the Order Consumer service (port 8081)
	@echo "Starting Order Consumer service on port 8081..."
	./gradlew :orders:orders-consumer:bootRun --args='--spring.profiles.active=local'

run-all: ## Run both API and Consumer services (requires 2 terminals or use &)
	@echo "Starting both services..."
	@echo "Run 'make run-api' in one terminal and 'make run-consumer' in another"
	@echo "Or use: make run-api & make run-consumer"

# =============================================================================
# Development
# =============================================================================

swagger: ## Open Swagger UI in browser
	@open http://localhost:8080/orders/v1/swagger-ui/index.html

infra-up: postgres-up kafka-up ## Start all infrastructure (Postgres + Kafka)
	@echo "All infrastructure is up"

infra-down: postgres-down kafka-down ## Stop all infrastructure
	@echo "All infrastructure is down"

# =============================================================================
# PostgreSQL
# =============================================================================

postgres-up: ## Start PostgreSQL
	@echo "Starting PostgreSQL..."
	@docker-compose up -d postgres
	@echo "Waiting for PostgreSQL..."
	@sleep 3
	@docker-compose exec -T postgres pg_isready -U orders_user -d orders > /dev/null 2>&1 && \
		echo "PostgreSQL is ready" || echo "PostgreSQL failed to start"

postgres-down: ## Stop PostgreSQL
	@echo "Stopping PostgreSQL..."
	@docker-compose stop postgres

postgres-clean: ## Stop PostgreSQL and remove volume
	@echo "Stopping PostgreSQL and removing volume..."
	@docker-compose down -v --remove-orphans
	@docker volume rm order-management-service_postgres_data 2>/dev/null || true
	@echo "PostgreSQL cleaned"

# =============================================================================
# Kafka
# =============================================================================

kafka-up: ## Start Kafka and Zookeeper
	@echo "Starting Kafka..."
	@docker-compose up -d kafka zookeeper
	@echo "Waiting for Kafka..."
	@sleep 5
	@echo "Kafka is ready"

kafka-down: ## Stop Kafka and Zookeeper
	@echo "Stopping Kafka..."
	@docker-compose stop kafka zookeeper

kafka-clean: ## Stop Kafka and remove volumes
	@echo "Stopping Kafka and removing volumes..."
	@docker-compose stop kafka zookeeper
	@docker volume rm order-management-service_kafka_data order-management-service_zookeeper_data 2>/dev/null || true
	@echo "Kafka cleaned"

# Topic names (matching application-kafka.yml)
ORDER_TOPIC ?= checkout-order-create
RELEASE_TOPIC ?= release-events
SHIPMENT_TOPIC ?= shipment-events

# Default count for kafka-produce-many
COUNT ?= 100

# Order ID for producing release/shipment events (format: CC-YYYYMMDD-NNNNNNN)
ORDER_ID ?= 10-20251228-0000001

kafka-topics-create: ## Create all Kafka topics
	@echo "Creating all Kafka topics..."
	@docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 \
		--create --topic $(ORDER_TOPIC) --partitions 3 --replication-factor 1 --if-not-exists
	@docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 \
		--create --topic $(RELEASE_TOPIC) --partitions 3 --replication-factor 1 --if-not-exists
	@docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 \
		--create --topic $(SHIPMENT_TOPIC) --partitions 3 --replication-factor 1 --if-not-exists
	@echo "All topics created"

kafka-topics: ## List all Kafka topics
	@echo "Listing Kafka topics..."
	@docker exec order-kafka kafka-topics --bootstrap-server localhost:9092 --list

# -----------------------------------------------------------------------------
# Order Events
# -----------------------------------------------------------------------------

kafka-produce: ## Produce a sample order event
	@echo "Producing sample order event to '$(ORDER_TOPIC)'..."
	@cat api/test-data/order-event-sample.json | tr -d '\n' | tr -s ' ' | \
		docker exec -i order-kafka \
		kafka-console-producer --bootstrap-server localhost:9092 --topic $(ORDER_TOPIC)
	@echo "Sample order event produced"

kafka-produce-many: ## Produce multiple random order events (default: 100)
	@./scripts/generate-orders.sh $(COUNT) $(ORDER_TOPIC)

# -----------------------------------------------------------------------------
# Fulfillment Events (Release + Shipment)
# -----------------------------------------------------------------------------

kafka-produce-fulfillment: ## Produce release and shipment events for a specific order (ORDER_ID=<id>)
	@./scripts/generate-test-events.sh $(ORDER_ID)

# -----------------------------------------------------------------------------
# Consumer Monitoring
# -----------------------------------------------------------------------------

kafka-consumer-groups: ## List all consumer groups
	@echo "Listing consumer groups..."
	@docker exec order-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list

kafka-lag: ## Show consumer lag for all groups
	@echo "Checking consumer lag..."
	@docker exec order-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list 2>/dev/null | while read group; do \
		if [ -n "$$group" ]; then \
			echo "\n=== Consumer Group: $$group ==="; \
			docker exec order-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group "$$group" 2>/dev/null; \
		fi; \
	done

# =============================================================================
# Cleanup
# =============================================================================

clean-all: ## Stop all containers and remove all volumes
	@echo "Stopping all containers and removing volumes..."
	@docker-compose down -v --remove-orphans
	@echo "All cleaned"

.DEFAULT_GOAL := help
