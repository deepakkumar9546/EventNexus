package com.eventbooking.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_status")
public class DeliveryStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;
    
    @Column(nullable = false, length = 50)
    private String provider;
    
    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;
    
    @Column(nullable = false, length = 50)
    private String status;
    
    @Column(name = "event_type", length = 50)
    private String eventType;
    
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public DeliveryStatus() {
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String getProviderMessageId() {
        return providerMessageId;
    }
    
    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Instant getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
