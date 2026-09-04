# Stripe Payment Gateway Integration Guide

## Overview

This document describes the Stripe payment gateway integration for the Event Ticket Booking System. The integration provides secure payment processing with PCI DSS compliance, comprehensive error handling, and support for various payment scenarios.

## Features

### Core Payment Processing
- **Payment Intent Creation**: Secure payment processing using Stripe Payment Intents API
- **3D Secure Support**: Automatic handling of Strong Customer Authentication (SCA) requirements
- **Multiple Payment Methods**: Support for credit/debit cards with extensibility for other methods
- **Real-time Payment Status**: Immediate feedback on payment success or failure

### Security & Compliance
- **PCI DSS Compliance**: No sensitive card data touches our servers
- **Webhook Signature Verification**: Ensures webhook authenticity
- **Secure Token Handling**: Payment methods are tokenized by Stripe
- **Encrypted Communication**: All API calls use HTTPS

### Error Handling
- **User-Friendly Error Messages**: Translates technical decline codes into actionable messages
- **Comprehensive Logging**: Detailed logging for debugging and monitoring
- **Automatic Retry Logic**: Built-in retry mechanisms for transient failures
- **Transaction Tracking**: Complete audit trail of all payment attempts

### Additional Features
- **Refund Processing**: Full and partial refund support
- **Payment Confirmation**: Webhook-based payment confirmation
- **Transaction History**: Complete payment transaction records
- **Metadata Tracking**: Order and event information attached to Stripe payments

## Configuration

### Environment Variables

Add the following to your `application.yml` or environment variables:

```yaml
stripe:
  api-key: ${STRIPE_API_KEY:sk_test_...}
  webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_...}
```

### Stripe Account Setup

1. **Create a Stripe Account**: Sign up at https://stripe.com
2. **Get API Keys**: 
   - Navigate to Developers > API keys
   - Copy your Secret key (starts with `sk_test_` for test mode)
3. **Configure Webhooks**:
   - Navigate to Developers > Webhooks
   - Add endpoint: `https://your-domain.com/api/webhooks/stripe`
   - Select events: `payment_intent.succeeded`, `payment_intent.payment_failed`, `payment_intent.canceled`, `charge.refunded`
   - Copy the webhook signing secret (starts with `whsec_`)

### Test Mode vs Production

- **Test Mode**: Use test API keys (prefix `sk_test_`) for development
- **Production Mode**: Use live API keys (prefix `sk_live_`) for production
- Test cards: https://stripe.com/docs/testing

## API Endpoints

### Process Payment

**Endpoint**: `POST /api/payments/process`

**Request Body**:
```json
{
  "orderId": "uuid",
  "paymentMethodId": "pm_card_visa",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "savePaymentMethod": false
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment processed successfully",
  "data": {
    "transactionId": "uuid",
    "orderId": "uuid",
    "status": "succeeded",
    "amount": 150.00,
    "currency": "USD",
    "paymentIntentId": "pi_...",
    "requiresAction": false
  }
}
```

**3D Secure Required Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment requires additional authentication",
  "data": {
    "transactionId": "uuid",
    "orderId": "uuid",
    "status": "requires_action",
    "amount": 150.00,
    "currency": "USD",
    "paymentIntentId": "pi_...",
    "clientSecret": "pi_..._secret_...",
    "requiresAction": true
  }
}
```

**Error Response** (402 Payment Required):
```json
{
  "success": false,
  "message": "Your card has insufficient funds. Please use a different payment method.",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Confirm Payment

**Endpoint**: `POST /api/payments/confirm/{paymentIntentId}`

Used after 3D Secure authentication is complete.

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Payment confirmed successfully",
  "data": {
    "transactionId": "uuid",
    "orderId": "uuid",
    "status": "succeeded",
    "amount": 150.00,
    "currency": "USD",
    "paymentIntentId": "pi_..."
  }
}
```

### Refund Payment

**Endpoint**: `POST /api/payments/refund/{orderId}?reason=Customer%20requested%20refund`

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Refund processed successfully",
  "data": {
    "transactionId": "uuid",
    "orderId": "uuid",
    "status": "succeeded",
    "amount": 150.00,
    "currency": "USD"
  }
}
```

### Get Payment Status

**Endpoint**: `GET /api/payments/status/{transactionId}`

**Success Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "transactionId": "uuid",
    "orderId": "uuid",
    "status": "succeeded",
    "amount": 150.00,
    "currency": "USD",
    "paymentIntentId": "pi_..."
  }
}
```

## Payment Flow

### Standard Payment Flow

1. **Create Order**: User selects tickets and creates an order
2. **Collect Payment Method**: Frontend collects card details using Stripe.js (PCI compliant)
3. **Process Payment**: Backend calls `/api/payments/process` with payment method ID
4. **Handle Response**:
   - If `status: "succeeded"` → Payment complete, show confirmation
   - If `requiresAction: true` → Trigger 3D Secure authentication
   - If `status: "failed"` → Show error message to user

### 3D Secure Flow

1. **Initial Payment Attempt**: Returns `requiresAction: true` with `clientSecret`
2. **Frontend Authentication**: Use Stripe.js to handle 3D Secure challenge
3. **Confirm Payment**: Call `/api/payments/confirm/{paymentIntentId}` after authentication
4. **Complete Order**: Payment confirmed, proceed with ticket generation

### Webhook Flow

1. **Stripe Event**: Payment status changes (success, failure, refund)
2. **Webhook Delivery**: Stripe sends event to `/api/webhooks/stripe`
3. **Signature Verification**: Webhook signature is verified
4. **Event Processing**: Payment status updated in database
5. **Acknowledgment**: Return 200 OK to Stripe

## Error Handling

### Card Decline Codes

The system translates Stripe decline codes into user-friendly messages:

| Decline Code | User Message |
|--------------|--------------|
| `insufficient_funds` | Your card has insufficient funds. Please use a different payment method. |
| `lost_card` / `stolen_card` | This card has been reported as lost or stolen. Please use a different payment method. |
| `expired_card` | Your card has expired. Please use a different payment method. |
| `incorrect_cvc` | The card security code (CVC) is incorrect. Please check and try again. |
| `incorrect_number` | The card number is incorrect. Please check and try again. |
| `card_not_supported` | This type of card is not supported. Please use a different payment method. |
| `fraudulent` | This transaction was flagged as potentially fraudulent. Please contact your bank. |
| `generic_decline` | Your card was declined. Please contact your bank or use a different payment method. |

### Exception Types

- **PaymentProcessingException**: Payment gateway errors
- **InvalidOrderException**: Order validation errors
- **OrderNotFoundException**: Order not found errors

### Retry Strategy

- **Transient Failures**: Automatic retry with exponential backoff
- **Card Declines**: No automatic retry (user must provide different payment method)
- **API Errors**: Logged and reported to monitoring systems

## Database Schema

### PaymentTransaction Table

```sql
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    gateway_transaction_id VARCHAR(255),
    payment_intent_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    gateway_response TEXT,
    error_code VARCHAR(100),
    error_message TEXT,
    decline_code VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);
```

### Transaction Statuses

- `processing`: Payment is being processed
- `succeeded`: Payment completed successfully
- `failed`: Payment failed
- `requires_action`: Requires 3D Secure authentication
- `canceled`: Payment was canceled

## Testing

### Test Cards

Use these test cards in test mode:

| Card Number | Scenario |
|-------------|----------|
| 4242 4242 4242 4242 | Successful payment |
| 4000 0025 0000 3155 | Requires 3D Secure authentication |
| 4000 0000 0000 9995 | Declined - insufficient funds |
| 4000 0000 0000 0069 | Declined - expired card |
| 4000 0000 0000 0127 | Declined - incorrect CVC |

### Testing Webhooks Locally

1. Install Stripe CLI: https://stripe.com/docs/stripe-cli
2. Login: `stripe login`
3. Forward webhooks: `stripe listen --forward-to localhost:8080/api/webhooks/stripe`
4. Use the webhook signing secret provided by the CLI

## Monitoring & Logging

### Key Metrics to Monitor

- Payment success rate
- Average payment processing time
- Decline rate by decline code
- 3D Secure authentication rate
- Refund rate

### Log Levels

- **INFO**: Successful payments, refunds, confirmations
- **WARN**: Card declines, payment failures
- **ERROR**: API errors, webhook processing errors

### Important Log Messages

```
Processing payment for order: {orderId}
Payment succeeded for order: {orderId}
Card declined for order: {orderId} - {declineCode}
Payment requires action for order: {orderId}
Refund processed successfully for order: {orderId}
```

## Security Best Practices

1. **Never Log Sensitive Data**: Don't log full card numbers, CVCs, or API keys
2. **Use HTTPS**: All communication must use HTTPS in production
3. **Verify Webhooks**: Always verify webhook signatures
4. **Rotate API Keys**: Regularly rotate Stripe API keys
5. **Monitor for Fraud**: Set up Stripe Radar for fraud detection
6. **Limit API Key Permissions**: Use restricted API keys when possible

## Troubleshooting

### Common Issues

**Issue**: "Stripe API key not configured"
- **Solution**: Set `STRIPE_API_KEY` environment variable

**Issue**: "Invalid webhook signature"
- **Solution**: Verify `STRIPE_WEBHOOK_SECRET` is correct

**Issue**: "Payment processing failed"
- **Solution**: Check Stripe Dashboard for detailed error information

**Issue**: "Order has expired"
- **Solution**: Orders expire after 15 minutes; user must create a new order

### Support Resources

- Stripe Documentation: https://stripe.com/docs
- Stripe Support: https://support.stripe.com
- Stripe Status: https://status.stripe.com

## Future Enhancements

- Support for additional payment methods (Apple Pay, Google Pay, ACH)
- Saved payment methods for returning customers
- Subscription billing for recurring events
- Multi-currency support
- Partial refunds
- Payment installments
