package com.eventbooking.payment.dto;

import com.eventbooking.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
    
    private String paymentMethod;
    private String notes;
}
