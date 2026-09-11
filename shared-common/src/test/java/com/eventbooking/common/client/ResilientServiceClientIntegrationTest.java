package com.eventbooking.common.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Integration tests for resilience patterns (circuit breaker, retry)
 * Tests service communication resilience and fault tolerance
 */
class ResilientServiceClientIntegrationTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;
    private TestResilientClient testClient;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        
        // Configure circuit breaker with low thresholds for testing
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowSize(5)
                .minimumNumberOfCalls(3)
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        
        circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        
        // Configure retry with short delays for testing
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        
        retryRegistry = RetryRegistry.of(retryConfig);
        
        testClient = new TestResilientClient(restTemplate, circuitBreakerRegistry, retryRegistry);
    }

    @Test
    void testSuccessfulCall_NoRetry() {
        // Arrange
        String url = "http://test-service/api/data";
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("Success", org.springframework.http.MediaType.TEXT_PLAIN));

        // Act
        String result = testClient.callService(url);

        // Assert
        assertEquals("Success", result);
        mockServer.verify();
    }

    @Test
    void testTransientFailure_RetriesAndSucceeds() {
        // Arrange
        String url = "http://test-service/api/data";
        
        // First two calls fail, third succeeds
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("Success after retry", org.springframework.http.MediaType.TEXT_PLAIN));

        // Act
        String result = testClient.callService(url);

        // Assert
        assertEquals("Success after retry", result);
        mockServer.verify();
    }

    @Test
    void testPermanentFailure_ExhaustsRetries() {
        // Arrange
        String url = "http://test-service/api/data";
        
        // All calls fail
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(Exception.class, () -> testClient.callService(url));
        mockServer.verify();
    }

    @Test
    void testCircuitBreaker_OpensAfterFailures() {
        // Arrange
        String url = "http://test-service/api/data";
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testService");
        
        // Simulate multiple failures to open circuit
        for (int i = 0; i < 5; i++) {
            mockServer.expect(requestTo(url))
                    .andRespond(withServerError());
        }

        // Act - Make calls until circuit opens
        for (int i = 0; i < 5; i++) {
            try {
                testClient.callService(url);
            } catch (Exception e) {
                // Expected failures
            }
        }

        // Assert - Circuit should be open
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        
        // Further calls should fail fast without hitting the service
        assertThrows(Exception.class, () -> testClient.callService(url));
    }

    @Test
    void testCircuitBreaker_HalfOpenAfterWaitDuration() {
        // Arrange
        String url = "http://test-service/api/data";
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testService");

        // The first call makes 3 retry attempts and opens the circuit.
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // This expectation will be used by the half-open call after the wait duration.
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("Success", org.springframework.http.MediaType.TEXT_PLAIN));

        // Act - Open the circuit
        try {
            testClient.callService(url);
        } catch (Exception e) {
            // Expected failure
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Wait for circuit to transition to half-open
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted while waiting for circuit breaker");
        }

        // Act - Next call should be allowed in half-open state
        String result = testClient.callService(url);

        // Assert
        assertEquals("Success", result);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        mockServer.verify();
    }

    @Test
    void testFallback_ReturnsDefaultValue() {
        // Arrange
        String url = "http://test-service/api/data";
        
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());
        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act
        String result = testClient.callServiceWithFallback(url);

        // Assert
        assertEquals("Fallback value", result);
    }

    @Test
    void testRetryMetrics_TrackAttempts() {
        // Arrange
        String url = "http://test-service/api/data";
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        mockServer.expect(requestTo(url))
                .andRespond(request -> {
                    attemptCount.incrementAndGet();
                    return withServerError().createResponse(request);
                });
        mockServer.expect(requestTo(url))
                .andRespond(request -> {
                    attemptCount.incrementAndGet();
                    return withServerError().createResponse(request);
                });
        mockServer.expect(requestTo(url))
                .andRespond(request -> {
                    attemptCount.incrementAndGet();
                    return withSuccess("Success", org.springframework.http.MediaType.TEXT_PLAIN)
                            .createResponse(request);
                });

        // Act
        String result = testClient.callService(url);

        // Assert
        assertEquals("Success", result);
        assertEquals(3, attemptCount.get());
    }

    @Test
    void testTimeout_FailsFast() {
        // Arrange
        String url = "http://test-service/api/data";
        
        mockServer.expect(requestTo(url))
                .andRespond(request -> {
                    try {
                        Thread.sleep(5000); // Simulate slow response
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return withSuccess("Too slow", org.springframework.http.MediaType.TEXT_PLAIN)
                            .createResponse(request);
                });

        // Act & Assert - Should timeout before response
        assertThrows(Exception.class, () -> testClient.callServiceWithTimeout(url, 1000));
    }

    /**
     * Test implementation of ResilientServiceClient for testing
     */
    private static class TestResilientClient extends ResilientServiceClient {
        
        public TestResilientClient(RestTemplate restTemplate,
                                  CircuitBreakerRegistry circuitBreakerRegistry,
                                  RetryRegistry retryRegistry) {
            super(restTemplate, circuitBreakerRegistry, retryRegistry, "testService");
        }
        
        public String callService(String url) {
            return executeWithResilience(() -> 
                restTemplate.getForObject(url, String.class)
            );
        }
        
        public String callServiceWithFallback(String url) {
            return executeWithResilienceAndFallback(() -> 
                restTemplate.getForObject(url, String.class),
                "Fallback value"
            );
        }
        
        public String callServiceWithTimeout(String url, long timeoutMs) {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout((int) timeoutMs);
            requestFactory.setReadTimeout((int) timeoutMs);

            restTemplate.setRequestFactory(requestFactory);

            return executeWithResilience(() -> {
                restTemplate.getForObject(url, String.class);
                return "Success";
            });
}
    }
}
