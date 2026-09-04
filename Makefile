# Event Ticket Booking System - Development Makefile

.PHONY: help build test clean start stop restart logs status setup-dev

# Default target
help:
	@echo "Event Ticket Booking System - Development Commands"
	@echo ""
	@echo "Available commands:"
	@echo "  setup-dev     - Set up development environment"
	@echo "  build         - Build all services"
	@echo "  test          - Run all tests"
	@echo "  clean         - Clean build artifacts"
	@echo "  start         - Start all services with Docker Compose"
	@echo "  stop          - Stop all services"
	@echo "  restart       - Restart all services"
	@echo "  logs          - Show logs from all services"
	@echo "  status        - Show status of all services"
	@echo "  db-setup      - Set up databases only"
	@echo "  db-reset      - Reset all databases"
	@echo "  lint          - Run code quality checks"
	@echo "  security-scan - Run security vulnerability scan"

# Set up development environment
setup-dev:
	@echo "Setting up development environment..."
	@chmod +x scripts/setup-dev.sh
	@./scripts/setup-dev.sh

# Build all services
build:
	@echo "Building all services..."
	@mvn clean package -DskipTests

# Run all tests
test:
	@echo "Running all tests..."
	@mvn clean test

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@mvn clean
	@docker system prune -f

# Start all services
start:
	@echo "Starting all services..."
	@docker-compose up -d
	@echo "Services started. Check status with 'make status'"

# Stop all services
stop:
	@echo "Stopping all services..."
	@docker-compose down

# Restart all services
restart: stop start

# Show logs from all services
logs:
	@docker-compose logs -f

# Show status of all services
status:
	@echo "Service Status:"
	@docker-compose ps
	@echo ""
	@echo "Health Checks:"
	@curl -s http://localhost:8081/actuator/health | jq . || echo "Auth Service: Not responding"
	@curl -s http://localhost:8082/actuator/health | jq . || echo "Event Service: Not responding"
	@curl -s http://localhost:8083/actuator/health | jq . || echo "Ticket Service: Not responding"
	@curl -s http://localhost:8084/actuator/health | jq . || echo "Payment Service: Not responding"
	@curl -s http://localhost:8085/actuator/health | jq . || echo "Notification Service: Not responding"

# Set up databases only
db-setup:
	@echo "Setting up databases..."
	@docker-compose up -d auth-db event-db ticket-db payment-db notification-db redis
	@echo "Waiting for databases to be ready..."
	@sleep 10

# Reset all databases
db-reset:
	@echo "Resetting all databases..."
	@docker-compose down -v
	@docker volume prune -f
	@make db-setup

# Run code quality checks
lint:
	@echo "Running code quality checks..."
	@mvn checkstyle:check
	@mvn spotbugs:check

# Run security vulnerability scan
security-scan:
	@echo "Running security vulnerability scan..."
	@mvn org.owasp:dependency-check-maven:check

# Development shortcuts
dev-auth:
	@echo "Starting Auth Service in development mode..."
	@cd auth-service && mvn spring-boot:run

dev-event:
	@echo "Starting Event Service in development mode..."
	@cd event-service && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

dev-ticket:
	@echo "Starting Ticket Service in development mode..."
	@cd ticket-service && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083

dev-payment:
	@echo "Starting Payment Service in development mode..."
	@cd payment-service && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8084

dev-notification:
	@echo "Starting Notification Service in development mode..."
	@cd notification-service && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8085

# Docker shortcuts
docker-build:
	@echo "Building Docker images..."
	@docker-compose build

docker-rebuild:
	@echo "Rebuilding Docker images..."
	@docker-compose build --no-cache

# Monitoring
monitor:
	@echo "Opening monitoring tools..."
	@echo "Adminer (Database): http://localhost:8080"
	@echo "Redis Commander: http://localhost:8081"
	@echo "Service Health Checks:"
	@echo "  Auth Service: http://localhost:8081/actuator/health"
	@echo "  Event Service: http://localhost:8082/actuator/health"
	@echo "  Ticket Service: http://localhost:8083/actuator/health"
	@echo "  Payment Service: http://localhost:8084/actuator/health"
	@echo "  Notification Service: http://localhost:8085/actuator/health"