package com.eventbooking.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private UUID transactionId;
    private UUID orderId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String paymentIntentId;
    private String clientSecret;
    private String errorMessage;
    private String errorCode;
    private String declineCode;
    private Boolean requiresAction;
}
