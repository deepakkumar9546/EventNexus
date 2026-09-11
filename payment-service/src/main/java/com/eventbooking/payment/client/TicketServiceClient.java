package com.eventbooking.payment.client;

import com.eventbooking.common.client.ResilientServiceClient;
import com.eventbooking.common.dto.ApiResponse;
import com.eventbooking.payment.dto.TicketGenerationRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class TicketServiceClient extends ResilientServiceClient {

    private static final Logger logger =
            LoggerFactory.getLogger(TicketServiceClient.class);

    @Value("${services.ticket.url:http://localhost:8083}")
    private String ticketServiceUrl;

    public TicketServiceClient(
            RestTemplate restTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {

        super(
                restTemplate,
                circuitBreakerRegistry,
                retryRegistry,
                "ticketService"
        );
    }

    public boolean generateTickets(
        UUID orderId,
        UUID userId,
        UUID ticketTypeId,
        Integer quantity,
        String holderName) {

        return executeWithResilience(() -> {

            String url = ticketServiceUrl + "/api/tickets/internal/generate";

           TicketGenerationRequest request =
                TicketGenerationRequest.builder()
                .orderId(orderId)
                .userId(userId)
                .ticketTypeId(ticketTypeId)
                .quantity(quantity)
                .holderName(holderName)
                .build();

            logger.info(
                    "Requesting ticket generation for order: {}",
                    orderId
            );

            ResponseEntity<ApiResponse<Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            new ParameterizedTypeReference<ApiResponse<Object>>() {}
                    );

            ApiResponse<Object> body = response.getBody();

            return body != null && body.isSuccess();
        });
    }
}