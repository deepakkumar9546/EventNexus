package com.eventbooking.payment.service;

import com.eventbooking.payment.dto.PaymentResponse;
import com.eventbooking.payment.dto.ProcessPaymentRequest;

import java.util.UUID;

public interface PaymentService {
    
    /**
     * Process payment for an order using Stripe
     */
    PaymentResponse processPayment(ProcessPaymentRequest request);
    
    /**
     * Confirm a payment intent
     */
    PaymentResponse confirmPayment(String paymentIntentId);
    
    /**
     * Refund a payment
     */
    PaymentResponse refundPayment(UUID orderId, String reason);
    
    /**
     * Get payment status
     */
    PaymentResponse getPaymentStatus(UUID transactionId);
}
