package com.eventbooking.payment.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(UUID orderId) {
        super("Order not found with ID: " + orderId);
    }
    
    public OrderNotFoundException(String orderNumber) {
        super("Order not found with order number: " + orderNumber);
    }
}
