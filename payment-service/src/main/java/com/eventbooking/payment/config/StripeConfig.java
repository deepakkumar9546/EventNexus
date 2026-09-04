package com.eventbooking.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {
    
    @Value("${stripe.api-key}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            Stripe.apiKey = apiKey;
            log.info("Stripe API key configured successfully");
        } else {
            log.warn("Stripe API key not configured. Payment processing will not work.");
        }
    }
}
