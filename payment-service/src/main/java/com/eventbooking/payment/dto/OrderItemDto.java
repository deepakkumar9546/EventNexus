package com.eventbooking.payment.dto;

import com.eventbooking.payment.entity.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    
    private UUID id;
    private UUID ticketTypeId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private BigDecimal fees;
    private BigDecimal totalPrice;
    private OrderItemStatus status;
    private Instant createdAt;
}
