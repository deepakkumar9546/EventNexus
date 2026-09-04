package com.eventbooking.payment.dto;

import com.eventbooking.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    
    private UUID id;
    private UUID userId;
    private UUID eventId;
    private String orderNumber;
    private BigDecimal subtotalAmount;
    private BigDecimal serviceFee;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String currency;
    private UUID reservationId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;
    private List<OrderItemDto> orderItems;
}
