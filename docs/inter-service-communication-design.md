# Inter-Service Communication Design

## Overview

This document defines the communication patterns, contracts, and strategies for inter-service communication in the Event Ticket Booking System. It covers synchronous API contracts, asynchronous messaging patterns, distributed transaction handling, resilience patterns, and service discovery mechanisms.

## Table of Contents

1. [Service-to-Service API Contracts](#service-to-service-api-contracts)
2. [Event-Driven Messaging Patterns](#event-driven-messaging-patterns)
3. [Distributed Transaction Patterns (Saga)](#distributed-transaction-patterns-saga)
4. [Circuit Breaker and Retry Strategies](#circuit-breaker-and-retry-strategies)
5. [Service Discovery and Load Balancing](#service-discovery-and-load-balancing)

## Service-to-Service API Contracts

### Authentication Service API Contracts

#### Validate Token Endpoint
**Purpose**: Validate JWT tokens and retrieve user information for other services

```
POST /api/auth/internal/validate-token
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200 OK):
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "roles": ["USER", "ORGANIZER"],
  "expiresAt": "2024-01-15T12:00:00Z"
}

Response (401 Unauthorized):
{
  "valid": false,
  "error": "TOKEN_EXPIRED",
  "message": "JWT token has expired"
}
```

#### Get User Details Endpoint
**Purpose**: Retrieve user information by user ID

```
GET /api/auth/internal/users/{userId}
Authorization: Service-Token <service-secret>

Response (200 OK):
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "emailVerified": true,
  "createdAt": "2024-01-01T10:00:00Z"
}

Response (404 Not Found):
{
  "error": "USER_NOT_FOUND",
  "message": "User with ID 550e8400-e29b-41d4-a716-446655440000 not found"
}
```

#### Batch Get Users Endpoint
**Purpose**: Retrieve multiple users in a single request for efficiency

```
POST /api/auth/internal/users/batch
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "userIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "660e8400-e29b-41d4-a716-446655440001"
  ]
}

Response (200 OK):
{
  "users": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user1@example.com",
      "firstName": "John",
      "lastName": "Doe"
    },
    {
      "userId": "660e8400-e29b-41d4-a716-446655440001",
      "email": "user2@example.com",
      "firstName": "Jane",
      "lastName": "Smith"
    }
  ]
}
```

### Event Management Service API Contracts

#### Get Event Details Endpoint
**Purpose**: Retrieve event information for ticket and payment services

```
GET /api/events/internal/{eventId}
Authorization: Service-Token <service-secret>

Response (200 OK):
{
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "organizerId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Summer Music Festival 2024",
  "description": "Annual outdoor music festival",
  "eventDate": "2024-07-15T18:00:00Z",
  "venueName": "Central Park Amphitheater",
  "venueAddress": "123 Park Ave, New York, NY 10001",
  "category": "MUSIC",
  "status": "PUBLISHED",
  "imageUrl": "https://cdn.example.com/events/770e8400.jpg"
}

Response (404 Not Found):
{
  "error": "EVENT_NOT_FOUND",
  "message": "Event with ID 770e8400-e29b-41d4-a716-446655440000 not found"
}
```

#### Validate Event Organizer Endpoint
**Purpose**: Verify if a user is the organizer of an event

```
GET /api/events/internal/{eventId}/validate-organizer/{userId}
Authorization: Service-Token <service-secret>

Response (200 OK):
{
  "isOrganizer": true,
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}

Response (200 OK - Not Organizer):
{
  "isOrganizer": false,
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Batch Get Events Endpoint
**Purpose**: Retrieve multiple events efficiently

```
POST /api/events/internal/batch
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "eventIds": [
    "770e8400-e29b-41d4-a716-446655440000",
    "880e8400-e29b-41d4-a716-446655440001"
  ]
}

Response (200 OK):
{
  "events": [
    {
      "eventId": "770e8400-e29b-41d4-a716-446655440000",
      "name": "Summer Music Festival 2024",
      "eventDate": "2024-07-15T18:00:00Z",
      "status": "PUBLISHED"
    },
    {
      "eventId": "880e8400-e29b-41d4-a716-446655440001",
      "name": "Tech Conference 2024",
      "eventDate": "2024-08-20T09:00:00Z",
      "status": "PUBLISHED"
    }
  ]
}
```

### Ticket Service API Contracts

#### Check Ticket Availability Endpoint
**Purpose**: Verify ticket availability before payment processing

```
POST /api/tickets/internal/check-availability
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "ticketRequests": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    },
    {
      "ticketTypeId": "aa0e8400-e29b-41d4-a716-446655440001",
      "quantity": 1
    }
  ]
}

Response (200 OK):
{
  "available": true,
  "ticketAvailability": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "available": true,
      "quantityAvailable": 50,
      "quantityRequested": 2
    },
    {
      "ticketTypeId": "aa0e8400-e29b-41d4-a716-446655440001",
      "available": true,
      "quantityAvailable": 20,
      "quantityRequested": 1
    }
  ]
}

Response (200 OK - Insufficient Inventory):
{
  "available": false,
  "ticketAvailability": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "available": false,
      "quantityAvailable": 1,
      "quantityRequested": 2,
      "error": "INSUFFICIENT_INVENTORY"
    }
  ]
}
```

#### Reserve Tickets Endpoint
**Purpose**: Reserve tickets during payment processing

```
POST /api/tickets/internal/reserve
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
  "ticketRequests": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "quantity": 2
    }
  ],
  "reservationDurationMinutes": 15
}

Response (200 OK):
{
  "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
  "reserved": true,
  "expiresAt": "2024-01-15T10:45:00Z",
  "tickets": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "unitPrice": 50.00,
      "totalPrice": 100.00
    }
  ]
}

Response (409 Conflict):
{
  "reserved": false,
  "error": "INSUFFICIENT_INVENTORY",
  "message": "Not enough tickets available"
}
```

#### Confirm Ticket Purchase Endpoint
**Purpose**: Convert reservation to confirmed tickets after successful payment

```
POST /api/tickets/internal/confirm-purchase
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
  "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "holderName": "John Doe"
}

Response (200 OK):
{
  "confirmed": true,
  "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
  "tickets": [
    {
      "ticketId": "dd0e8400-e29b-41d4-a716-446655440000",
      "ticketNumber": "TKT-2024-001234",
      "qrCode": "https://cdn.example.com/qr/dd0e8400.png",
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000"
    },
    {
      "ticketId": "ee0e8400-e29b-41d4-a716-446655440001",
      "ticketNumber": "TKT-2024-001235",
      "qrCode": "https://cdn.example.com/qr/ee0e8400.png",
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000"
    }
  ]
}

Response (404 Not Found):
{
  "confirmed": false,
  "error": "RESERVATION_NOT_FOUND",
  "message": "Reservation bb0e8400-e29b-41d4-a716-446655440000 not found or expired"
}
```

#### Release Reservation Endpoint
**Purpose**: Release reserved tickets when payment fails or times out

```
POST /api/tickets/internal/release-reservation
Authorization: Service-Token <service-secret>
Content-Type: application/json

Request:
{
  "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
  "reason": "PAYMENT_FAILED"
}

Response (200 OK):
{
  "released": true,
  "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
  "ticketsReleased": 2
}
```

#### Get Ticket Details Endpoint
**Purpose**: Retrieve ticket information for notifications and display

```
GET /api/tickets/internal/{ticketId}
Authorization: Service-Token <service-secret>

Response (200 OK):
{
  "ticketId": "dd0e8400-e29b-41d4-a716-446655440000",
  "ticketNumber": "TKT-2024-001234",
  "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
  "holderName": "John Doe",
  "qrCode": "https://cdn.example.com/qr/dd0e8400.png",
  "status": "ACTIVE",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

### Payment Service API Contracts

#### Get Order Details Endpoint
**Purpose**: Retrieve order information for other services

```
GET /api/payments/internal/orders/{orderId}
Authorization: Service-Token <service-secret>

Response (200 OK):
{
  "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
  "orderNumber": "ORD-2024-001234",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "eventId": "770e8400-e29b-41d4-a716-446655440000",
  "totalAmount": 150.00,
  "currency": "USD",
  "paymentStatus": "COMPLETED",
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
      "quantity": 2,
      "unitPrice": 50.00,
      "totalPrice": 100.00
    }
  ],
  "createdAt": "2024-01-15T10:30:00Z",
  "completedAt": "2024-01-15T10:31:00Z"
}

Response (404 Not Found):
{
  "error": "ORDER_NOT_FOUND",
  "message": "Order with ID cc0e8400-e29b-41d4-a716-446655440000 not found"
}
```

### Shared DTOs

#### UserInfoDto
```java
public class UserInfoDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
```

#### EventInfoDto
```java
public class EventInfoDto {
    private UUID eventId;
    private UUID organizerId;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private String venueName;
    private String venueAddress;
    private String category;
    private String status;
    private String imageUrl;
}
```

#### TicketAvailabilityDto
```java
public class TicketAvailabilityDto {
    private UUID ticketTypeId;
    private boolean available;
    private int quantityAvailable;
    private int quantityRequested;
    private String error;
}
```

#### TicketReservationDto
```java
public class TicketReservationDto {
    private UUID reservationId;
    private UUID userId;
    private UUID eventId;
    private List<TicketRequestDto> ticketRequests;
    private LocalDateTime expiresAt;
    private boolean reserved;
}
```

#### TicketInfoDto
```java
public class TicketInfoDto {
    private UUID ticketId;
    private String ticketNumber;
    private UUID ticketTypeId;
    private UUID eventId;
    private UUID orderId;
    private String holderName;
    private String qrCode;
    private String status;
    private LocalDateTime createdAt;
}
```

#### OrderInfoDto
```java
public class OrderInfoDto {
    private UUID orderId;
    private String orderNumber;
    private UUID userId;
    private UUID eventId;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentStatus;
    private String paymentMethod;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
```

## Event-Driven Messaging Patterns

### Message Queue Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Payment        │         │  Ticket         │         │  Event          │
│  Service        │         │  Service        │         │  Service        │
└────────┬────────┘         └────────┬────────┘         └────────┬────────┘
         │                           │                           │
         │ Publish Events            │ Publish Events            │ Publish Events
         ▼                           ▼                           ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Amazon SNS Topics                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │ payment-     │  │ ticket-      │  │ event-       │                 │
│  │ events       │  │ events       │  │ events       │                 │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                 │
└─────────┼──────────────────┼──────────────────┼───────────────────────┘
          │                  │                  │
          │ Fan-out          │ Fan-out          │ Fan-out
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          Amazon SQS Queues                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │ notification-│  │ ticket-      │  │ analytics-   │                 │
│  │ queue        │  │ processing   │  │ queue        │                 │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                 │
└─────────┼──────────────────┼──────────────────┼───────────────────────┘
          │                  │                  │
          │ Consume          │ Consume          │ Consume
          ▼                  ▼                  ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  Notification   │  │  Ticket         │  │  Analytics      │
│  Service        │  │  Service        │  │  Service        │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### SNS Topics Configuration

#### payment-events Topic
**Purpose**: Broadcast payment-related events to multiple subscribers

**Subscribers**:
- notification-queue (for order confirmations)
- ticket-processing-queue (for ticket generation)
- analytics-queue (for business intelligence)

**Message Attributes**:
- `eventType`: PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_PROCESSED
- `orderId`: UUID of the order
- `userId`: UUID of the user
- `timestamp`: ISO 8601 timestamp

#### ticket-events Topic
**Purpose**: Broadcast ticket-related events

**Subscribers**:
- notification-queue (for ticket delivery)
- analytics-queue (for inventory tracking)

**Message Attributes**:
- `eventType`: TICKETS_GENERATED, TICKET_CANCELLED, TICKET_TRANSFERRED
- `ticketId`: UUID of the ticket
- `orderId`: UUID of the order
- `timestamp`: ISO 8601 timestamp

#### event-events Topic
**Purpose**: Broadcast event lifecycle events

**Subscribers**:
- notification-queue (for event updates to attendees)
- ticket-processing-queue (for inventory updates)

**Message Attributes**:
- `eventType`: EVENT_CREATED, EVENT_UPDATED, EVENT_CANCELLED, EVENT_PUBLISHED
- `eventId`: UUID of the event
- `organizerId`: UUID of the organizer
- `timestamp`: ISO 8601 timestamp

### SQS Queue Configuration

#### notification-queue
**Purpose**: Process notification requests asynchronously

**Configuration**:
- Visibility Timeout: 30 seconds
- Message Retention: 4 days
- Receive Message Wait Time: 20 seconds (long polling)
- Dead Letter Queue: notification-dlq (after 3 retries)

**Message Format**:
```json
{
  "messageId": "msg-123456",
  "eventType": "PAYMENT_COMPLETED",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": {
    "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "eventId": "770e8400-e29b-41d4-a716-446655440000",
    "totalAmount": 150.00,
    "notificationType": "ORDER_CONFIRMATION"
  }
}
```

#### ticket-processing-queue
**Purpose**: Process ticket generation and updates

**Configuration**:
- Visibility Timeout: 60 seconds
- Message Retention: 4 days
- Receive Message Wait Time: 20 seconds
- Dead Letter Queue: ticket-processing-dlq (after 3 retries)

**Message Format**:
```json
{
  "messageId": "msg-789012",
  "eventType": "PAYMENT_COMPLETED",
  "timestamp": "2024-01-15T10:30:00Z",
  "payload": {
    "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
    "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "eventId": "770e8400-e29b-41d4-a716-446655440000",
    "tickets": [
      {
        "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
        "quantity": 2
      }
    ]
  }
}
```

### Event Message Schemas

#### PaymentCompletedEvent
```json
{
  "eventType": "PAYMENT_COMPLETED",
  "version": "1.0",
  "timestamp": "2024-01-15T10:30:00Z",
  "correlationId": "corr-123456",
  "payload": {
    "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
    "orderNumber": "ORD-2024-001234",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "eventId": "770e8400-e29b-41d4-a716-446655440000",
    "totalAmount": 150.00,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "transactionId": "txn-987654",
    "items": [
      {
        "ticketTypeId": "990e8400-e29b-41d4-a716-446655440000",
        "quantity": 2,
        "unitPrice": 50.00
      }
    ]
  }
}
```

#### PaymentFailedEvent
```json
{
  "eventType": "PAYMENT_FAILED",
  "version": "1.0",
  "timestamp": "2024-01-15T10:30:00Z",
  "correlationId": "corr-123456",
  "payload": {
    "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "reservationId": "bb0e8400-e29b-41d4-a716-446655440000",
    "failureReason": "INSUFFICIENT_FUNDS",
    "errorMessage": "Payment declined by issuing bank",
    "retryable": false
  }
}
```

#### TicketsGeneratedEvent
```json
{
  "eventType": "TICKETS_GENERATED",
  "version": "1.0",
  "timestamp": "2024-01-15T10:31:00Z",
  "correlationId": "corr-123456",
  "payload": {
    "orderId": "cc0e8400-e29b-41d4-a716-446655440000",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "eventId": "770e8400-e29b-41d4-a716-446655440000",
    "tickets": [
      {
        "ticketId": "dd0e8400-e29b-41d4-a716-446655440000",
        "ticketNumber": "TKT-2024-001234",
        "qrCode": "https://cdn.example.com/qr/dd0e8400.png"
      },
      {
        "ticketId": "ee0e8400-e29b-41d4-a716-446655440001",
        "ticketNumber": "TKT-2024-001235",
        "qrCode": "https://cdn.example.com/qr/ee0e8400.png"
      }
    ]
  }
}
```

#### EventCancelledEvent
```json
{
  "eventType": "EVENT_CANCELLED",
  "version": "1.0",
  "timestamp": "2024-01-15T10:00:00Z",
  "correlationId": "corr-789012",
  "payload": {
    "eventId": "770e8400-e29b-41d4-a716-446655440000",
    "eventName": "Summer Music Festival 2024",
    "organizerId": "550e8400-e29b-41d4-a716-446655440000",
    "cancellationReason": "Venue unavailable",
    "affectedTicketCount": 150
  }
}
```

### Message Processing Patterns

#### Idempotency
All message consumers must implement idempotency to handle duplicate messages:

```java
@Service
public class PaymentEventConsumer {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @SqsListener(value = "notification-queue")
    public void processPaymentEvent(PaymentCompletedEvent event) {
        String idempotencyKey = "processed:" + event.getCorrelationId();
        
        // Check if already processed
        Boolean processed = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "true", Duration.ofHours(24));
        
        if (Boolean.FALSE.equals(processed)) {
            log.info("Message already processed: {}", event.getCorrelationId());
            return;
        }
        
        try {
            // Process the event
            sendOrderConfirmationEmail(event);
        } catch (Exception e) {
            // Remove idempotency key on failure to allow retry
            redisTemplate.delete(idempotencyKey);
            throw e;
        }
    }
}
```

#### Dead Letter Queue Handling
Failed messages are moved to DLQ after max retries:

```java
@Service
public class DeadLetterQueueMonitor {
    
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void monitorDLQ() {
        List<Message> messages = sqsClient.receiveMessage(
            ReceiveMessageRequest.builder()
                .queueUrl(dlqUrl)
                .maxNumberOfMessages(10)
                .build()
        ).messages();
        
        for (Message message : messages) {
            // Log for investigation
            log.error("DLQ Message: {}", message.body());
            
            // Send alert to operations team
            alertService.sendAlert("DLQ message detected", message.body());
            
            // Optionally move to long-term storage
            archiveService.archiveFailedMessage(message);
        }
    }
}
```

#### Message Ordering
For events that require ordering (e.g., event updates), use FIFO queues:

```
Queue Name: event-updates.fifo
Message Group ID: eventId
Message Deduplication ID: eventId + timestamp
```

## Distributed Transaction Patterns (Saga)

### Saga Pattern Overview

The ticket purchase flow uses the Choreography-based Saga pattern to maintain data consistency across services without distributed transactions.

### Ticket Purchase Saga Flow

```
┌─────────────┐
│   User      │
│  Initiates  │
│  Purchase   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Payment Service                               │
│  1. Create Order (PENDING)                                       │
│  2. Call Ticket Service: Reserve Tickets                         │
└──────┬──────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Ticket Service                                │
│  3. Reserve Tickets (15 min hold)                                │
│  4. Return Reservation ID                                        │
└──────┬──────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Payment Service                               │
│  5. Process Payment with Gateway                                 │
│  6a. If Success: Update Order (COMPLETED)                        │
│  6b. If Failure: Update Order (FAILED)                           │
│  7. Publish Event to SNS                                         │
└──────┬──────────────────────────────────────────────────────────┘
       │
       ├─────────────────────────────────────────────────────────┐
       │                                                          │
       ▼ (Success)                                                ▼ (Failure)
┌─────────────────────────────────────────────────────────────────┐
│              Ticket Service (via SQS)                            │
│  8a. Confirm Purchase: Convert Reservation to Tickets            │
│  8b. Release Reservation: Return tickets to inventory            │
│  9. Generate QR Codes                                            │
│  10. Publish TicketsGenerated Event                              │
└──────┬──────────────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────────────────────┐
│            Notification Service (via SQS)                        │
│  11. Send Order Confirmation Email                               │
│  12. Attach Tickets with QR Codes                                │
└─────────────────────────────────────────────────────────────────┘
```

### Saga State Machine

#### Payment Service Saga Orchestrator

```java
@Service
public class TicketPurchaseSagaOrchestrator {
    
    @Autowired
    private TicketServiceClient ticketServiceClient;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private SnsPublisher snsPublisher;
    
    @Transactional
    public OrderResult executePurchaseSaga(PurchaseRequest request) {
        // Step 1: Create order in PENDING state
        Order order = createPendingOrder(request);
        
        try {
            // Step 2: Reserve tickets (compensatable)
            ReservationResult reservation = ticketServiceClient.reserveTickets(
                ReservationRequest.builder()
                    .userId(request.getUserId())
                    .eventId(request.getEventId())
                    .ticketRequests(request.getTicketRequests())
                    .reservationDurationMinutes(15)
                    .build()
            );
            
            if (!reservation.isReserved()) {
                return handleReservationFailure(order, reservation);
            }
            
            order.setReservationId(reservation.getReservationId());
            orderRepository.save(order);
            
            // Step 3: Process payment (pivot point - non-compensatable)
            PaymentResult paymentResult = paymentGatewayService.processPayment(
                PaymentRequest.builder()
                    .orderId(order.getId())
                    .amount(order.getTotalAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .build()
            );
            
            if (paymentResult.isSuccessful()) {
                return handlePaymentSuccess(order, paymentResult, reservation);
            } else {
                return handlePaymentFailure(order, paymentResult, reservation);
            }
            
        } catch (Exception e) {
            return handleSagaException(order, e);
        }
    }
    
    private OrderResult handlePaymentSuccess(Order order, PaymentResult payment, 
                                            ReservationResult reservation) {
        // Update order status
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setPaymentTransactionId(payment.getTransactionId());
        orderRepository.save(order);
        
        // Publish success event (async ticket confirmation)
        snsPublisher.publish("payment-events", PaymentCompletedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .reservationId(reservation.getReservationId())
            .totalAmount(order.getTotalAmount())
            .build()
        );
        
        return OrderResult.success(order);
    }
    
    private OrderResult handlePaymentFailure(Order order, PaymentResult payment,
                                            ReservationResult reservation) {
        // Update order status
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setFailureReason(payment.getErrorMessage());
        orderRepository.save(order);
        
        // Compensate: Release ticket reservation
        try {
            ticketServiceClient.releaseReservation(
                ReleaseRequest.builder()
                    .reservationId(reservation.getReservationId())
                    .reason("PAYMENT_FAILED")
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to release reservation: {}", reservation.getReservationId(), e);
            // Publish compensation failure event for manual intervention
            snsPublisher.publish("saga-compensation-failed", 
                CompensationFailedEvent.builder()
                    .orderId(order.getId())
                    .reservationId(reservation.getReservationId())
                    .reason("Failed to release reservation")
                    .build()
            );
        }
        
        // Publish failure event
        snsPublisher.publish("payment-events", PaymentFailedEvent.builder()
            .orderId(order.getId())
            .userId(order.getUserId())
            .reservationId(reservation.getReservationId())
            .failureReason(payment.getErrorMessage())
            .build()
        );
        
        return OrderResult.failure(order, payment.getErrorMessage());
    }
    
    private OrderResult handleReservationFailure(Order order, ReservationResult reservation) {
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setFailureReason("Insufficient ticket inventory");
        orderRepository.save(order);
        
        return OrderResult.failure(order, "Tickets not available");
    }
    
    private OrderResult handleSagaException(Order order, Exception e) {
        log.error("Saga execution failed for order: {}", order.getId(), e);
        
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setFailureReason("System error: " + e.getMessage());
        orderRepository.save(order);
        
        return OrderResult.failure(order, "System error occurred");
    }
}
```

#### Ticket Service Saga Participant

```java
@Service
public class TicketSagaParticipant {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private SnsPublisher snsPublisher;
    
    @SqsListener(value = "ticket-processing-queue")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            // Confirm the reservation and generate tickets
            Reservation reservation = reservationRepository
                .findById(event.getPayload().getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                    "Reservation not found: " + event.getPayload().getReservationId()));
            
            // Generate tickets
            List<Ticket> tickets = generateTickets(reservation, event.getPayload().getOrderId());
            
            // Update inventory
            inventoryService.confirmPurchase(reservation);
            
            // Mark reservation as completed
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
            
            // Publish tickets generated event
            snsPublisher.publish("ticket-events", TicketsGeneratedEvent.builder()
                .orderId(event.getPayload().getOrderId())
                .userId(event.getPayload().getUserId())
                .eventId(event.getPayload().getEventId())
                .tickets(tickets.stream()
                    .map(this::toTicketDto)
                    .collect(Collectors.toList()))
                .build()
            );
            
        } catch (Exception e) {
            log.error("Failed to process payment completed event", e);
            throw e; // Will retry via SQS
        }
    }
    
    @SqsListener(value = "ticket-processing-queue")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        try {
            // Release the reservation
            Reservation reservation = reservationRepository
                .findById(event.getPayload().getReservationId())
                .orElse(null);
            
            if (reservation != null) {
                inventoryService.releaseReservation(reservation);
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment failed event", e);
            throw e; // Will retry via SQS
        }
    }
}
```

### Saga Compensation Strategies

#### Compensation Actions

| Step | Action | Compensation | Trigger |
|------|--------|--------------|---------|
| 1 | Create Order (PENDING) | Update Order (FAILED) | Any subsequent failure |
| 2 | Reserve Tickets | Release Reservation | Payment failure or timeout |
| 3 | Process Payment | Refund Payment | Ticket generation failure (rare) |
| 4 | Generate Tickets | Cancel Tickets | Manual cancellation only |

#### Timeout Handling

```java
@Service
public class ReservationTimeoutHandler {
    
    @Scheduled(fixedDelay = 60000) // Every minute
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Reservation> expiredReservations = reservationRepository
            .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);
        
        for (Reservation reservation : expiredReservations) {
            try {
                // Release inventory
                inventoryService.releaseReservation(reservation);
                
                // Update reservation status
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
                
                log.info("Released expired reservation: {}", reservation.getId());
                
            } catch (Exception e) {
                log.error("Failed to cleanup reservation: {}", reservation.getId(), e);
            }
        }
    }
}
```

### Event Sourcing for Saga State

```java
@Entity
@Table(name = "saga_events")
public class SagaEvent {
    @Id
    @GeneratedValue
    private UUID id;
    
    private UUID sagaId;
    private String sagaType;
    private String eventType;
    private String payload;
    private LocalDateTime timestamp;
    private String status; // PENDING, COMPLETED, COMPENSATED, FAILED
}

@Service
public class SagaEventStore {
    
    @Autowired
    private SagaEventRepository repository;
    
    public void recordEvent(UUID sagaId, String eventType, Object payload, String status) {
        SagaEvent event = new SagaEvent();
        event.setSagaId(sagaId);
        event.setSagaType("TICKET_PURCHASE");
        event.setEventType(eventType);
        event.setPayload(toJson(payload));
        event.setStatus(status);
        event.setTimestamp(LocalDateTime.now());
        
        repository.save(event);
    }
    
    public List<SagaEvent> getSagaHistory(UUID sagaId) {
        return repository.findBySagaIdOrderByTimestamp(sagaId);
    }
}
```

## Circuit Breaker and Retry Strategies

### Circuit Breaker Pattern

#### Resilience4j Configuration

```java
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open circuit if 50% of calls fail
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before half-open
            .slidingWindowSize(10) // Consider last 10 calls
            .minimumNumberOfCalls(5) // Need at least 5 calls to calculate rate
            .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 test calls in half-open
            .slowCallRateThreshold(50) // Consider slow if 50% calls are slow
            .slowCallDurationThreshold(Duration.ofSeconds(2)) // Call is slow if > 2s
            .build();
        
        return CircuitBreakerRegistry.of(config);
    }
}
```

#### Service-Specific Circuit Breaker Settings

```yaml
resilience4j:
  circuitbreaker:
    instances:
      authService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 30s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
        
      ticketService:
        registerHealthIndicator: true
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        permittedNumberOfCallsInHalfOpenState: 5
        waitDurationInOpenState: 60s
        failureRateThreshold: 40
        
      paymentGateway:
        registerHealthIndicator: true
        slidingWindowSize: 15
        minimumNumberOfCalls: 8
        permittedNumberOfCallsInHalfOpenState: 4
        waitDurationInOpenState: 45s
        failureRateThreshold: 30
        slowCallDurationThreshold: 5s
        slowCallRateThreshold: 60
        
      emailService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 60s
        failureRateThreshold: 60
```

#### Circuit Breaker Implementation

```java
@Service
public class TicketServiceClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    private final CircuitBreaker circuitBreaker;
    
    public TicketServiceClient(CircuitBreakerRegistry registry) {
        this.circuitBreaker = registry.circuitBreaker("ticketService");
    }
    
    public ReservationResult reserveTickets(ReservationRequest request) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                ResponseEntity<ReservationResult> response = restTemplate.postForEntity(
                    "http://ticket-service/api/tickets/internal/reserve",
                    request,
                    ReservationResult.class
                );
                return response.getBody();
            } catch (HttpServerErrorException e) {
                throw new TicketServiceException("Ticket service error", e);
            }
        });
    }
    
    public ReservationResult reserveTicketsWithFallback(ReservationRequest request) {
        return circuitBreaker.executeSupplier(
            () -> reserveTickets(request),
            throwable -> {
                log.error("Circuit breaker fallback triggered for ticket reservation", throwable);
                return ReservationResult.failure("Service temporarily unavailable");
            }
        );
    }
}
```

### Retry Strategy

#### Retry Configuration

```yaml
resilience4j:
  retry:
    instances:
      authService:
        maxAttempts: 3
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
        ignoreExceptions:
          - com.eventbooking.common.exception.ValidationException
          
      ticketService:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
        ignoreExceptions:
          - com.eventbooking.ticket.exception.InsufficientInventoryException
          
      paymentGateway:
        maxAttempts: 2
        waitDuration: 2s
        enableExponentialBackoff: false
        retryExceptions:
          - java.net.SocketTimeoutException
        ignoreExceptions:
          - com.eventbooking.payment.exception.PaymentDeclinedException
          - com.eventbooking.payment.exception.InvalidCardException
          
      emailService:
        maxAttempts: 5
        waitDuration: 1s
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - com.amazonaws.services.simpleemail.model.ThrottlingException
          - java.net.SocketTimeoutException
```

#### Retry Implementation

```java
@Service
public class PaymentGatewayService {
    
    @Autowired
    private StripeClient stripeClient;
    
    @Autowired
    private RetryRegistry retryRegistry;
    
    private final Retry retry;
    
    public PaymentGatewayService(RetryRegistry registry) {
        this.retry = registry.retry("paymentGateway");
        
        // Add event listeners for monitoring
        retry.getEventPublisher()
            .onRetry(event -> log.warn("Retry attempt {} for payment processing", 
                event.getNumberOfRetryAttempts()))
            .onSuccess(event -> log.info("Payment processing succeeded after {} attempts",
                event.getNumberOfRetryAttempts()))
            .onError(event -> log.error("Payment processing failed after {} attempts",
                event.getNumberOfRetryAttempts()));
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        return retry.executeSupplier(() -> {
            try {
                return stripeClient.charge(request);
            } catch (StripeNetworkException e) {
                // Retryable network error
                throw new RetryablePaymentException("Network error", e);
            } catch (CardException e) {
                // Non-retryable card error
                throw new PaymentDeclinedException("Card declined", e);
            }
        });
    }
}
```

#### Combined Circuit Breaker and Retry

```java
@Service
public class ResilientServiceClient {
    
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    
    public ResilientServiceClient(CircuitBreakerRegistry cbRegistry, 
                                 RetryRegistry retryRegistry) {
        this.circuitBreaker = cbRegistry.circuitBreaker("ticketService");
        this.retry = retryRegistry.retry("ticketService");
    }
    
    public ReservationResult reserveTicketsWithResilience(ReservationRequest request) {
        // Retry wraps Circuit Breaker (retry -> circuit breaker -> actual call)
        Supplier<ReservationResult> supplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> makeApiCall(request));
        
        supplier = Retry.decorateSupplier(retry, supplier);
        
        return Try.ofSupplier(supplier)
            .recover(throwable -> {
                log.error("All resilience mechanisms exhausted", throwable);
                return ReservationResult.failure("Service unavailable");
            })
            .get();
    }
    
    private ReservationResult makeApiCall(ReservationRequest request) {
        // Actual HTTP call
        return restTemplate.postForObject(
            "http://ticket-service/api/tickets/internal/reserve",
            request,
            ReservationResult.class
        );
    }
}
```

### Bulkhead Pattern

#### Thread Pool Isolation

```yaml
resilience4j:
  bulkhead:
    instances:
      ticketService:
        maxConcurrentCalls: 50
        maxWaitDuration: 500ms
        
      paymentGateway:
        maxConcurrentCalls: 20
        maxWaitDuration: 1s
        
      emailService:
        maxConcurrentCalls: 100
        maxWaitDuration: 100ms
```

```java
@Service
public class BulkheadProtectedService {
    
    @Autowired
    private BulkheadRegistry bulkheadRegistry;
    
    private final Bulkhead bulkhead;
    
    public BulkheadProtectedService(BulkheadRegistry registry) {
        this.bulkhead = registry.bulkhead("ticketService");
    }
    
    public ReservationResult reserveTickets(ReservationRequest request) {
        return bulkhead.executeSupplier(() -> {
            return ticketServiceClient.reserveTickets(request);
        });
    }
}
```

### Rate Limiting

```yaml
resilience4j:
  ratelimiter:
    instances:
      authService:
        limitForPeriod: 100
        limitRefreshPeriod: 1s
        timeoutDuration: 0s
        
      ticketService:
        limitForPeriod: 200
        limitRefreshPeriod: 1s
        timeoutDuration: 100ms
        
      paymentGateway:
        limitForPeriod: 50
        limitRefreshPeriod: 1s
        timeoutDuration: 500ms
```

```java
@Service
public class RateLimitedService {
    
    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;
    
    private final RateLimiter rateLimiter;
    
    public RateLimitedService(RateLimiterRegistry registry) {
        this.rateLimiter = registry.rateLimiter("paymentGateway");
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        return rateLimiter.executeSupplier(() -> {
            return paymentGateway.charge(request);
        });
    }
}
```

### Timeout Configuration

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        
        // Connection timeout: time to establish connection
        factory.setConnectTimeout(5000); // 5 seconds
        
        // Read timeout: time to wait for response
        factory.setReadTimeout(10000); // 10 seconds
        
        // Connection request timeout: time to get connection from pool
        factory.setConnectionRequestTimeout(3000); // 3 seconds
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add interceptors for logging and authentication
        restTemplate.setInterceptors(List.of(
            new LoggingInterceptor(),
            new ServiceAuthenticationInterceptor()
        ));
        
        return restTemplate;
    }
}
```

## Service Discovery and Load Balancing

### AWS ECS Service Discovery

#### Service Discovery Configuration

```yaml
# ECS Service Definition
services:
  auth-service:
    image: auth-service:latest
    service_registries:
      - registry_arn: arn:aws:servicediscovery:us-east-1:123456789:service/srv-auth
        container_name: auth-service
        container_port: 8080
    networks:
      - service-mesh
      
  event-service:
    image: event-service:latest
    service_registries:
      - registry_arn: arn:aws:servicediscovery:us-east-1:123456789:service/srv-event
        container_name: event-service
        container_port: 8081
    networks:
      - service-mesh
      
  ticket-service:
    image: ticket-service:latest
    service_registries:
      - registry_arn: arn:aws:servicediscovery:us-east-1:123456789:service/srv-ticket
        container_name: ticket-service
        container_port: 8082
    networks:
      - service-mesh
      
  payment-service:
    image: payment-service:latest
    service_registries:
      - registry_arn: arn:aws:servicediscovery:us-east-1:123456789:service/srv-payment
        container_name: payment-service
        container_port: 8083
    networks:
      - service-mesh
      
  notification-service:
    image: notification-service:latest
    service_registries:
      - registry_arn: arn:aws:servicediscovery:us-east-1:123456789:service/srv-notification
        container_name: notification-service
        container_port: 8084
    networks:
      - service-mesh
```

#### Cloud Map Namespace

```
Namespace: eventbooking.local (Private DNS)

Services:
- auth-service.eventbooking.local
- event-service.eventbooking.local
- ticket-service.eventbooking.local
- payment-service.eventbooking.local
- notification-service.eventbooking.local
```

### Spring Cloud LoadBalancer

#### LoadBalancer Configuration

```java
@Configuration
@LoadBalancerClient(name = "ticket-service", configuration = TicketServiceLoadBalancerConfig.class)
public class LoadBalancerConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}

@Configuration
public class TicketServiceLoadBalancerConfig {
    
    @Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
            .withDiscoveryClient()
            .withHealthChecks()
            .withCaching()
            .build(context);
    }
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name
        );
    }
}
```

#### Service Client with Load Balancing

```java
@Service
public class LoadBalancedTicketServiceClient {
    
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    private static final String TICKET_SERVICE_URL = "http://ticket-service.eventbooking.local";
    
    public ReservationResult reserveTickets(ReservationRequest request) {
        // RestTemplate will automatically use service discovery and load balancing
        ResponseEntity<ReservationResult> response = restTemplate.postForEntity(
            TICKET_SERVICE_URL + "/api/tickets/internal/reserve",
            request,
            ReservationResult.class
        );
        
        return response.getBody();
    }
    
    public EventInfoDto getEventDetails(UUID eventId) {
        ResponseEntity<EventInfoDto> response = restTemplate.getForEntity(
            "http://event-service.eventbooking.local/api/events/internal/" + eventId,
            EventInfoDto.class
        );
        
        return response.getBody();
    }
}
```

### Health Checks

#### Service Health Check Endpoints

```java
@RestController
@RequestMapping("/actuator/health")
public class HealthCheckController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @GetMapping
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus();
        status.setStatus("UP");
        status.setTimestamp(LocalDateTime.now());
        
        // Check database
        try {
            dataSource.getConnection().close();
            status.addComponent("database", "UP");
        } catch (Exception e) {
            status.addComponent("database", "DOWN");
            status.setStatus("DOWN");
        }
        
        // Check Redis
        try {
            redisTemplate.opsForValue().get("health-check");
            status.addComponent("redis", "UP");
        } catch (Exception e) {
            status.addComponent("redis", "DOWN");
            status.setStatus("DEGRADED");
        }
        
        HttpStatus httpStatus = status.getStatus().equals("UP") 
            ? HttpStatus.OK 
            : HttpStatus.SERVICE_UNAVAILABLE;
        
        return ResponseEntity.status(httpStatus).body(status);
    }
    
    @GetMapping("/ready")
    public ResponseEntity<String> readiness() {
        // Check if service is ready to accept traffic
        boolean ready = checkDatabaseConnection() && checkDependencies();
        
        return ready 
            ? ResponseEntity.ok("READY") 
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("NOT_READY");
    }
    
    @GetMapping("/live")
    public ResponseEntity<String> liveness() {
        // Simple liveness check
        return ResponseEntity.ok("ALIVE");
    }
}
```

#### ECS Health Check Configuration

```json
{
  "healthCheck": {
    "command": [
      "CMD-SHELL",
      "curl -f http://localhost:8080/actuator/health || exit 1"
    ],
    "interval": 30,
    "timeout": 5,
    "retries": 3,
    "startPeriod": 60
  }
}
```

### Application Load Balancer Configuration

#### ALB Target Groups

```yaml
TargetGroups:
  AuthServiceTargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      Name: auth-service-tg
      Port: 8080
      Protocol: HTTP
      VpcId: !Ref VPC
      TargetType: ip
      HealthCheckEnabled: true
      HealthCheckPath: /actuator/health
      HealthCheckProtocol: HTTP
      HealthCheckIntervalSeconds: 30
      HealthCheckTimeoutSeconds: 5
      HealthyThresholdCount: 2
      UnhealthyThresholdCount: 3
      Matcher:
        HttpCode: 200
      TargetGroupAttributes:
        - Key: deregistration_delay.timeout_seconds
          Value: 30
        - Key: stickiness.enabled
          Value: true
        - Key: stickiness.type
          Value: lb_cookie
        - Key: stickiness.lb_cookie.duration_seconds
          Value: 86400
          
  TicketServiceTargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      Name: ticket-service-tg
      Port: 8082
      Protocol: HTTP
      VpcId: !Ref VPC
      TargetType: ip
      HealthCheckEnabled: true
      HealthCheckPath: /actuator/health
      HealthCheckProtocol: HTTP
      HealthCheckIntervalSeconds: 30
      HealthCheckTimeoutSeconds: 5
      HealthyThresholdCount: 2
      UnhealthyThresholdCount: 3
```

#### ALB Listener Rules

```yaml
ListenerRules:
  AuthServiceRule:
    Type: AWS::ElasticLoadBalancingV2::ListenerRule
    Properties:
      Actions:
        - Type: forward
          TargetGroupArn: !Ref AuthServiceTargetGroup
      Conditions:
        - Field: path-pattern
          Values:
            - /api/auth/*
      ListenerArn: !Ref HTTPSListener
      Priority: 1
      
  EventServiceRule:
    Type: AWS::ElasticLoadBalancingV2::ListenerRule
    Properties:
      Actions:
        - Type: forward
          TargetGroupArn: !Ref EventServiceTargetGroup
      Conditions:
        - Field: path-pattern
          Values:
            - /api/events/*
      ListenerArn: !Ref HTTPSListener
      Priority: 2
      
  TicketServiceRule:
    Type: AWS::ElasticLoadBalancingV2::ListenerRule
    Properties:
      Actions:
        - Type: forward
          TargetGroupArn: !Ref TicketServiceTargetGroup
      Conditions:
        - Field: path-pattern
          Values:
            - /api/tickets/*
      ListenerArn: !Ref HTTPSListener
      Priority: 3
```

### Service Mesh (Optional - AWS App Mesh)

#### App Mesh Configuration

```yaml
AppMesh:
  Mesh:
    Name: eventbooking-mesh
    
  VirtualNodes:
    AuthServiceNode:
      ServiceDiscovery:
        DNS:
          Hostname: auth-service.eventbooking.local
      Listeners:
        - PortMapping:
            Port: 8080
            Protocol: http
          HealthCheck:
            Protocol: http
            Path: /actuator/health
            HealthyThreshold: 2
            UnhealthyThreshold: 3
            TimeoutMillis: 5000
            IntervalMillis: 30000
      Backends:
        - VirtualService:
            VirtualServiceName: ticket-service.eventbooking.local
            
    TicketServiceNode:
      ServiceDiscovery:
        DNS:
          Hostname: ticket-service.eventbooking.local
      Listeners:
        - PortMapping:
            Port: 8082
            Protocol: http
          HealthCheck:
            Protocol: http
            Path: /actuator/health
      Backends:
        - VirtualService:
            VirtualServiceName: event-service.eventbooking.local
            
  VirtualServices:
    AuthService:
      Provider:
        VirtualRouter:
          VirtualRouterName: auth-service-router
          
    TicketService:
      Provider:
        VirtualRouter:
          VirtualRouterName: ticket-service-router
          
  VirtualRouters:
    AuthServiceRouter:
      Listeners:
        - PortMapping:
            Port: 8080
            Protocol: http
      Routes:
        - Name: auth-route
          HttpRoute:
            Match:
              Prefix: /
            Action:
              WeightedTargets:
                - VirtualNode: auth-service-node
                  Weight: 100
                  
    TicketServiceRouter:
      Listeners:
        - PortMapping:
            Port: 8082
            Protocol: http
      Routes:
        - Name: ticket-route
          HttpRoute:
            Match:
              Prefix: /
            Action:
              WeightedTargets:
                - VirtualNode: ticket-service-node
                  Weight: 100
```

### Service Authentication

#### Service-to-Service JWT

```java
@Component
public class ServiceAuthenticationInterceptor implements ClientHttpRequestInterceptor {
    
    @Value("${service.auth.secret}")
    private String serviceSecret;
    
    @Value("${service.name}")
    private String serviceName;
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                       ClientHttpRequestExecution execution) throws IOException {
        // Generate service token
        String serviceToken = generateServiceToken();
        
        // Add to request headers
        request.getHeaders().add("X-Service-Token", serviceToken);
        request.getHeaders().add("X-Service-Name", serviceName);
        
        return execution.execute(request, body);
    }
    
    private String generateServiceToken() {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute
            .claim("type", "SERVICE")
            .signWith(SignatureAlgorithm.HS256, serviceSecret)
            .compact();
    }
}

@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${service.auth.secret}")
    private String serviceSecret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        
        // Only validate service token for internal endpoints
        if (path.startsWith("/api/") && path.contains("/internal/")) {
            String serviceToken = request.getHeader("X-Service-Token");
            String serviceName = request.getHeader("X-Service-Name");
            
            if (serviceToken == null || !validateServiceToken(serviceToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid service token");
                return;
            }
            
            // Add service name to request attributes
            request.setAttribute("serviceName", serviceName);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean validateServiceToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(serviceSecret)
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Monitoring and Observability

#### Distributed Tracing with AWS X-Ray

```java
@Configuration
public class XRayConfig {
    
    @Bean
    public Filter tracingFilter() {
        return new AWSXRayServletFilter("event-ticket-booking");
    }
    
    @Bean
    public TracingInterceptor tracingInterceptor() {
        return new TracingInterceptor();
    }
}

@Component
public class TracingInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) throws IOException {
        Segment segment = AWSXRay.getCurrentSegment();
        Subsegment subsegment = segment.beginSubsegment(request.getURI().getHost());
        
        try {
            subsegment.putMetadata("http.method", request.getMethod().toString());
            subsegment.putMetadata("http.url", request.getURI().toString());
            
            ClientHttpResponse response = execution.execute(request, body);
            
            subsegment.putMetadata("http.status", response.getStatusCode().value());
            
            return response;
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            subsegment.end();
        }
    }
}
```

#### Service Metrics

```java
@Service
public class MetricsService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    public void recordServiceCall(String targetService, String operation, 
                                  boolean success, long durationMs) {
        Timer.builder("service.call")
            .tag("target", targetService)
            .tag("operation", operation)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordCircuitBreakerState(String service, String state) {
        Gauge.builder("circuit.breaker.state", () -> stateToValue(state))
            .tag("service", service)
            .register(meterRegistry);
    }
    
    private double stateToValue(String state) {
        switch (state) {
            case "CLOSED": return 0;
            case "OPEN": return 1;
            case "HALF_OPEN": return 0.5;
            default: return -1;
        }
    }
}
```

## Summary

This inter-service communication design provides:

1. **Clear API Contracts**: Well-defined REST endpoints and DTOs for synchronous communication
2. **Event-Driven Architecture**: SNS/SQS-based asynchronous messaging for loose coupling
3. **Saga Pattern**: Distributed transaction handling with compensation for data consistency
4. **Resilience**: Circuit breakers, retries, bulkheads, and rate limiting for fault tolerance
5. **Service Discovery**: AWS Cloud Map and Spring Cloud LoadBalancer for dynamic service location
6. **Security**: Service-to-service authentication with JWT tokens
7. **Observability**: Distributed tracing, metrics, and health checks for monitoring

These patterns ensure the system is scalable, resilient, and maintainable while meeting the requirements for high availability and performance.
