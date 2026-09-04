#!/bin/bash

# Event Ticket Booking System - Development Setup Script

set -e

echo "🚀 Setting up Event Ticket Booking System development environment..."

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java version must be 17 or higher. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java $JAVA_VERSION found"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.9 or higher."
    exit 1
fi
echo "✅ Maven found"

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker."
    exit 1
fi
echo "✅ Docker found"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose."
    exit 1
fi
echo "✅ Docker Compose found"

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cat > .env << EOF
# Database Configuration
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=mySecretKeyForDevelopment123456789

# Email Configuration (Update with your credentials)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Stripe Configuration (Update with your credentials)
STRIPE_API_KEY=sk_test_your_stripe_key_here
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret_here

# AWS Configuration (Update with your credentials)
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_REGION=us-east-1
AWS_S3_BUCKET=event-images-dev-bucket
AWS_SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/your-account/event-notifications
EOF
    echo "✅ .env file created. Please update it with your actual credentials."
else
    echo "✅ .env file already exists"
fi

# Build the project
echo "🔨 Building the project..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Project built successfully"
else
    echo "❌ Project build failed"
    exit 1
fi

# Start infrastructure with Docker Compose
echo "🐳 Starting infrastructure services (PostgreSQL, Redis)..."
docker-compose up -d auth-db event-db ticket-db payment-db notification-db redis

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
sleep 10

# Check if databases are ready
echo "🔍 Checking database connections..."
for i in {1..30}; do
    if docker-compose exec -T auth-db pg_isready -U auth_user -d auth_service > /dev/null 2>&1; then
        echo "✅ Auth database is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Auth database failed to start"
        exit 1
    fi
    sleep 2
done

# Create logs directory
mkdir -p logs

echo "🎉 Development environment setup complete!"
echo ""
echo "📚 Next steps:"
echo "1. Update the .env file with your actual credentials"
echo "2. Start the services:"
echo "   - Option A: Use Docker Compose: docker-compose up -d"
echo "   - Option B: Run services locally (see README.md)"
echo ""
echo "🔗 Service URLs (when running):"
echo "   - Auth Service: http://localhost:8081"
echo "   - Event Service: http://localhost:8082"
echo "   - Ticket Service: http://localhost:8083"
echo "   - Payment Service: http://localhost:8084"
echo "   - Notification Service: http://localhost:8085"
echo ""
echo "📖 For more information, see README.md"