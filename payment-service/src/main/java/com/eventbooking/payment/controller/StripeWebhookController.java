package com.eventbooking.payment.controller;

import com.eventbooking.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {
    
    private final PaymentService paymentService;
    
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("Stripe webhook secret not configured. Skipping signature verification.");
            return ResponseEntity.ok("Webhook received (signature verification disabled)");
        }
        
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
        
        log.info("Received Stripe webhook event: {}", event.getType());
        
        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "payment_intent.canceled":
                handlePaymentIntentCanceled(event);
                break;
            case "charge.refunded":
                handleChargeRefunded(event);
                break;
            default:
                log.debug("Unhandled event type: {}", event.getType());
        }
        
        return ResponseEntity.ok("Webhook processed");
    }
    
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));
            
            log.info("Payment intent succeeded: {}", paymentIntent.getId());
            
            // Confirm the payment in our system
            paymentService.confirmPayment(paymentIntent.getId());
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.succeeded webhook: {}", e.getMessage(), e);
        }
    }
    
    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));
            
            log.warn("Payment intent failed: {}", paymentIntent.getId());
            
            // The payment failure is already handled in the processPayment method
            // This webhook is mainly for logging and monitoring
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.payment_failed webhook: {}", e.getMessage(), e);
        }
    }
    
    private void handlePaymentIntentCanceled(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));
            
            log.info("Payment intent canceled: {}", paymentIntent.getId());
            
        } catch (Exception e) {
            log.error("Error handling payment_intent.canceled webhook: {}", e.getMessage(), e);
        }
    }
    
    private void handleChargeRefunded(Event event) {
        try {
            log.info("Charge refunded event received");
            // Refund handling is done through the refundPayment method
            // This webhook is mainly for logging and monitoring
            
        } catch (Exception e) {
            log.error("Error handling charge.refunded webhook: {}", e.getMessage(), e);
        }
    }
}
