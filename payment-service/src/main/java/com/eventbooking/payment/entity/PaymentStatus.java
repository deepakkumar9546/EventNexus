package com.eventbooking.payment.entity;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
