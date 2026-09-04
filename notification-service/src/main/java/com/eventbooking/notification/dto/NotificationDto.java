package com.eventbooking.notification.dto;

import com.eventbooking.notification.entity.NotificationChannel;
import com.eventbooking.notification.entity.NotificationStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {
    
    private UUID id;
    private UUID userId;
    private String recipientEmail;
    private String subject;
    private NotificationStatus status;
    private NotificationChannel channel;
    private Integer retryCount;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private String errorMessage;
    private LocalDateTime createdAt;
    
    public NotificationDto() {
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
    
    public NotificationChannel getChannel() {
        return channel;
    }
    
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    public Instant getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
    
    public Instant getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public Instant getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(Instant failedAt) {
        this.failedAt = failedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
