package com.eventbooking.notification.service;

import com.eventbooking.notification.dto.NotificationDto;
import com.eventbooking.notification.dto.SendNotificationRequest;
import com.eventbooking.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    
    /**
     * Send a notification using a template
     */
    NotificationDto sendNotification(SendNotificationRequest request);
    
    /**
     * Get notification by ID
     */
    NotificationDto getNotification(UUID notificationId);
    
    /**
     * Get notifications for a user
     */
    Page<NotificationDto> getUserNotifications(UUID userId, NotificationStatus status, Pageable pageable);
    
    /**
     * Resend a failed notification
     */
    NotificationDto resendNotification(UUID notificationId);
    
    /**
     * Process pending notifications (for retry mechanism)
     */
    void processPendingNotifications();
}
