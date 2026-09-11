package com.eventbooking.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketGenerationRequest {

    private UUID orderId;

    private UUID userId;

    private UUID ticketTypeId;

    private Integer quantity;

    private String holderName;
}