# Event Ticket Booking System MVP

A microservices-based event ticket booking platform built with Spring Boot and React.

## Architecture

The system consists of 5 microservices:

- **Auth Service** (Port 8081): User authentication and authorization
- **Event Service** (Port 8082): Event creation and management
- **Ticket Service** (Port 8083): Ticket inventory and generation
- **Payment Service** (Port 8084): Payment processing and order management
- **Notification Service** (Port 8085): Email notifications and delivery

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL 15+ (for local development without Docker)
- Redis 7+ (for local development without Docker)

## Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd event-ticket-booking-system
   ```

2. **Build the applications**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start the infrastructure and services**
   ```bash
   docker-compose up -d
   ```

4. **Verify services are running**
   ```bash
   docker-compose ps
   ```

## Local Development Setup

### Database Setup

1. **Install PostgreSQL and create databases**
   ```sql
   CREATE DATABASE auth_service;
   CREATE DATABASE event_service;
   CREATE DATABASE ticket_service;
   CREATE DATABASE payment_service;
   CREATE DATABASE notification_service;
   ```

2. **Create users and grant permissions**
   ```sql
   CREATE USER auth_user WITH PASSWORD 'auth_password';
   GRANT ALL PRIVILEGES ON DATABASE auth_service TO auth_user;
   
   CREATE USER event_user WITH PASSWORD 'event_password';
   GRANT ALL PRIVILEGES ON DATABASE event_service TO event_user;
   
   CREATE USER ticket_user WITH PASSWORD 'ticket_password';
   GRANT ALL PRIVILEGES ON DATABASE ticket_service TO ticket_user;
   
   CREATE USER payment_user WITH PASSWORD 'payment_password';
   GRANT ALL PRIVILEGES ON DATABASE payment_service TO payment_user;
   
   CREATE USER notification_user WITH PASSWORD 'notification_password';
   GRANT ALL PRIVILEGES ON DATABASE notification_service TO notification_user;
   ```

3. **Install and start Redis**
   ```bash
   # macOS with Homebrew
   brew install redis
   brew services start redis
   
   # Ubuntu/Debian
   sudo apt-get install redis-server
   sudo systemctl start redis-server
   ```

### Running Services Locally

1. **Build the project**
   ```bash
   mvn clean install
   ```

2. **Start each service in separate terminals**
   ```bash
   # Terminal 1 - Auth Service
   cd auth-service
   mvn spring-boot:run
   
   # Terminal 2 - Event Service
   cd event-service
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
   
   # Terminal 3 - Ticket Service
   cd ticket-service
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083
   
   # Terminal 4 - Payment Service
   cd payment-service
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8084
   
   # Terminal 5 - Notification Service
   cd notification-service
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8085
   ```

## Service Endpoints

### Auth Service (http://localhost:8081)
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Email verification
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset

### Event Service (http://localhost:8082)
- `GET /api/events` - List events
- `GET /api/events/{id}` - Get event details
- `POST /api/events` - Create event (organizers only)
- `PUT /api/events/{id}` - Update event (organizers only)
- `GET /api/events/search` - Search events

### Ticket Service (http://localhost:8083)
- `GET /api/tickets/availability/{eventId}` - Check ticket availability
- `POST /api/tickets/reserve` - Reserve tickets
- `POST /api/tickets/purchase` - Purchase tickets
- `GET /api/tickets/orders/{userId}` - Get user orders

### Payment Service (http://localhost:8084)
- `POST /api/payments/process` - Process payment
- `GET /api/payments/orders/{orderId}` - Get order details
- `POST /api/payments/refund` - Process refund

### Notification Service (http://localhost:8085)
- `POST /api/notifications/send` - Send notification
- `GET /api/notifications/status/{id}` - Check delivery status

## Configuration

### Environment Variables

Create a `.env` file in the root directory:

```env
# Database Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=your-secret-key-here

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Stripe Configuration
STRIPE_API_KEY=your-stripe-api-key
STRIPE_WEBHOOK_SECRET=your-stripe-webhook-secret

# AWS Configuration
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_REGION=us-east-1
AWS_S3_BUCKET=your-s3-bucket-name
AWS_SQS_QUEUE_URL=your-sqs-queue-url
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Run Tests for Specific Service
```bash
cd auth-service
mvn test
```

## Docker Commands

### Start all services
```bash
docker-compose up -d
```

### Stop all services
```bash
docker-compose down
```

### View logs
```bash
docker-compose logs -f [service-name]
```

### Rebuild and restart a service
```bash
docker-compose up -d --build [service-name]
```

## Monitoring and Health Checks

Each service exposes health check endpoints:
- `GET /actuator/health` - Service health status
- `GET /actuator/info` - Service information

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 5432-5436, 6379, and 8081-8085 are available
2. **Database connection issues**: Verify PostgreSQL is running and credentials are correct
3. **Redis connection issues**: Ensure Redis is running on port 6379
4. **Docker build failures**: Run `mvn clean package` before `docker-compose up`

### Logs

Check service logs for debugging:
```bash
# Docker logs
docker-compose logs -f [service-name]

# Local development logs
tail -f logs/application.log
```

## Development Guidelines

1. **Code Style**: Follow Java coding conventions
2. **Testing**: Maintain 80%+ test coverage
3. **Documentation**: Update API documentation for new endpoints
4. **Security**: Never commit sensitive credentials
5. **Database**: Use migrations for schema changes

## Contributing

1. Create a feature branch from `develop`
2. Make your changes with appropriate tests
3. Submit a pull request to `develop`
4. Ensure CI/CD pipeline passes

## License

This project is licensed under the MIT License.