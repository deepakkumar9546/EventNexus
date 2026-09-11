# EventNexus

A full-stack event ticket booking platform built with **Java, Spring Boot, React, TypeScript, PostgreSQL, Redis, and Docker**, following a microservices architecture.

EventNexus allows users to browse events, view ticket types and availability, purchase tickets, view orders and tickets, and download tickets with QR codes.

---

## Features

- User registration and JWT-based authentication
- Event browsing and search
- Event categories
- Ticket type management
- Ticket availability management
- Ticket reservation and purchase
- Order management
- Payment processing
- Stripe payment integration
- Mock payment gateway for local development
- Saga-based ticket purchase workflow
- Automatic ticket generation
- QR code generation
- Ticket image download
- Email notification support
- Redis caching
- Database-per-service architecture
- PostgreSQL for persistent storage
- Spring Boot Actuator health checks
- Docker Compose based development environment
- Unit and integration testing
- React-based responsive frontend

---

## Architecture

EventNexus follows a **microservices architecture** with five Spring Boot services.

```text
                           ┌─────────────────────────┐
                           │      React Frontend     │
                           │    React + TypeScript   │
                           │        Vite :3000       │
                           └────────────┬────────────┘
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 │                      │                      │
                 ▼                      ▼                      ▼
          ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
          │ Auth Service│       │Event Service│       │Ticket Service│
          │    :8091    │       │    :8092    │       │    :8093     │
          └──────┬──────┘       └──────┬──────┘       └──────┬──────┘
                 │                      │                      │
                 ▼                      ▼                      ▼
          ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
          │ PostgreSQL  │       │ PostgreSQL  │       │ PostgreSQL  │
          │ auth_service│       │event_service│       │ticket_service│
          └─────────────┘       └─────────────┘       └─────────────┘

                           ┌─────────────────────────┐
                           │    Payment Service      │
                           │         :8094           │
                           └────────────┬────────────┘
                                        │
                                        ▼
                                 ┌─────────────┐
                                 │ PostgreSQL  │
                                 │payment_service│
                                 └─────────────┘

                           ┌─────────────────────────┐
                           │  Notification Service  │
                           │         :8095           │
                           └────────────┬────────────┘
                                        │
                                        ▼
                                 ┌─────────────┐
                                 │ PostgreSQL  │
                                 │notification_│
                                 │   service   │
                                 └─────────────┘

                           ┌─────────────────────────┐
                           │          Redis          │
                           │          :6379          │
                           └─────────────────────────┘

EventNexus/
├── auth-service/
├── event-service/
├── ticket-service/
├── payment-service/
├── notification-service/
├── shared-common/
├── frontend/
├── e2e-tests/
├── deployment/
├── docs/
├── scripts/
├── Requirements/
├── docker-compose.yml
├── docker-compose.override.yml
├── pom.xml
├── .env.example
└── README.md


Each microservice owns its own PostgreSQL database following the database-per-service approach.

- Microservices
- Service	Responsibility	Port
- Auth Service	Registration, login, JWT authentication and authorization	8091
- Event Service	Event and category management	8092
- Ticket Service	Ticket types, inventory, reservations and ticket generation	8093
- Payment Service	Orders and payment processing	8094
- Notification Service	Notification and email delivery	8095
- Technology Stack
- Backend
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Data Redis
- PostgreSQL
- Redis
- Maven
- Spring Boot Actuator
- Frontend
- React 19
- TypeScript
- Vite
- Redux Toolkit
- React Redux
- React Router
- Tailwind CSS
- Vitest
- React Testing Library
- QRCode
- html2canvas
- Infrastructure & Integrations
- Docker
- Docker Compose
- Stripe
- AWS services integration
- Mailpit
- Adminer
- Redis Commander
- Project Structure

Prerequisites

Install the following before running the project:

- Java 21+
- Maven 3.9+
- Node.js and npm
- Docker Desktop
- Git

Docker Compose is used to run the backend services and supporting infrastructure.

Environment Configuration

Sensitive configuration is provided through environment variables.

Create a .env file in the project root using .env.example as a reference.

Example:

JWT_SECRET=your-jwt-secret

STRIPE_SECRET_KEY=your-stripe-secret-key
STRIPE_WEBHOOK_SECRET=your-stripe-webhook-secret

Never commit .env or real credentials to Git.

The repository already excludes .env through .gitignore.

Running the Backend with Docker

From the project root:

mvn clean package -DskipTests

Start the complete development environment:

docker compose up -d --build

Check running services:

docker compose ps

Stop the environment:

docker compose down

Rebuild a specific service:

docker compose up -d --build auth-service

View service logs:

docker compose logs -f auth-service
Docker Services

The Docker Compose environment includes:

- notification-db
- notification-service
- auth-db
- auth-service
- payment-db
- event-db
- event-service
- ticket-db
- ticket-service
- payment-service
- redis
- redis-commander
- adminer
- mailpit
- Application Ports
- Backend Services
- Component	Port
- Auth Service	8091
- Event Service	8092
- Ticket Service	8093
- Payment Service	8094
- Notification Service	8095
- Infrastructure
- Component	Port
- Redis	6379
- Mailpit Web UI	8025
- Mailpit SMTP	1025
- Adminer	8080
- Redis Commander	8081
- PostgreSQL Databases
- Database	Port
- Auth PostgreSQL	5432
- Event PostgreSQL	5433
- Ticket PostgreSQL	5434
- Payment PostgreSQL	5435
- Notification PostgreSQL	5436
- Running the Frontend

Navigate to the frontend directory:

cd frontend

Install dependencies:

npm install

Start the development server:

npm run dev

The frontend is available at:

http://localhost:3000

The Vite development server proxies API requests to the corresponding backend microservices.

Frontend Commands

- Development
- npm run dev
- Production Build
- npm run build
- Run Tests
- npm test -- --run
- Test Watch Mode
- npm run test:watch
- Test Coverage
- npm run test:coverage
- Lint
- npm run lint
- Backend Testing

Run the complete backend test suite from the project root:

mvn clean test

Run tests for an individual service:

cd auth-service
mvn test

The project contains unit and integration tests across the microservices and shared components.

Ticket Purchase Flow

The ticket purchase process follows a Saga-based workflow across multiple services.

User
 │
 ▼
React Frontend
 │
 ▼
Ticket / Purchase Flow
 │
 ├── Reserve Tickets
 │
 ├── Create Order
 │
 ├── Process Payment
 │
 ├── Generate Tickets
 │
 └── Send Notification
 │
 ▼
Order Confirmation
 │
 ▼
Generated Ticket
 │
 ▼
QR Code

The Saga workflow coordinates the distributed transaction across services and provides compensation handling when a step fails.

Authentication

The application uses JWT-based authentication with Spring Security.

The authentication flow includes:

User registration
User login
JWT token generation
Authenticated API requests
Role-based authorization

Sensitive JWT configuration is provided through environment variables.

Payments

EventNexus supports Stripe payment integration.

For local development, the application can use a mock payment gateway so the complete purchase workflow can be tested without requiring a live Stripe account.

Stripe credentials can be supplied through environment variables when Stripe integration is required.

Ticket Generation

After a successful purchase:

The order is created.
Payment is processed.
Tickets are generated.
Each ticket receives a unique ticket number.
A QR code is generated.
The ticket becomes available in the user's account.
The ticket can be downloaded as an image.
Email Testing

The Docker environment includes Mailpit for local email testing.

Open the Mailpit Web UI:

http://localhost:8025

Mailpit allows application emails to be inspected locally without sending real emails.

Database Architecture

EventNexus follows the database-per-service architecture.

Auth Service          → auth_service
Event Service         → event_service
Ticket Service        → ticket_service
Payment Service       → payment_service
Notification Service  → notification_service

Each service owns its data independently, reducing coupling between services.

Redis

Redis is used for application caching and supporting service functionality.

The Docker environment provides a dedicated Redis instance with persistent storage and memory management configuration.

Redis is available at:

localhost:6379
Monitoring and Health Checks

Each Spring Boot service exposes Actuator endpoints.

Health endpoint:

/actuator/health

For example:

http://localhost:8091/actuator/health

Similar health endpoints are available for the other backend services.

Development Tools
Adminer

Adminer provides a browser-based interface for managing the PostgreSQL databases.

http://localhost:8080
Redis Commander

Redis Commander provides a browser-based interface for inspecting Redis.

http://localhost:8081
Mailpit

Mailpit provides a local web interface for inspecting application emails.

http://localhost:8025
Security

The application includes:

JWT authentication
Spring Security
Role-based authorization
Environment-based secret configuration
Service-to-service authentication
Input validation and sanitization
Rate limiting
Security-focused automated tests

Sensitive credentials must never be committed to the repository.

API Overview
Auth Service
POST /api/auth/register
POST /api/auth/login
POST /api/auth/verify-email
POST /api/auth/forgot-password
POST /api/auth/reset-password
Event Service
GET  /api/events
GET  /api/events/{id}
POST /api/events
PUT  /api/events/{id}
GET  /api/events/search
Ticket Service

Ticket and ticket-type APIs provide functionality for:

Ticket type management
Ticket availability
Ticket reservation
Ticket purchase
Ticket generation
User ticket retrieval
Payment Service

Payment APIs provide functionality for:

Payment processing
Order management
Payment status
Refund processing
Notification Service

Notification APIs provide functionality for:

Notification delivery
Email delivery
Delivery status tracking

For detailed implementation information, refer to the service source code and documentation under the docs/ directory.

Development Workflow

Recommended development workflow:

Create a feature branch.
Implement the change.
Add or update tests.
Run backend tests.
Run frontend tests.
Build the frontend.
Verify Docker services.
Review the changes.
Commit using a meaningful commit message.
Push the changes to GitHub.
Troubleshooting
Docker services are not starting

Check the service status:

docker compose ps

View logs:

docker compose logs -f <service-name>
Rebuild services
docker compose down
docker compose up -d --build
Backend build issues

Run:

mvn clean package -DskipTests
Frontend build issues

From the frontend directory:

npm install
npm run build
Database connection issues

Verify that the PostgreSQL containers are running:

docker compose ps
Redis connection issues

Verify Redis:

docker compose logs redis


###License

This project is licensed under the MIT License.
