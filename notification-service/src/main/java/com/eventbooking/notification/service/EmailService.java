package com.eventbooking.notification.service;

import com.eventbooking.notification.entity.Notification;

public interface EmailService {
    
    /**
     * Send an email notification
     * @param notification The notification to send
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendEmail(Notification notification);
    
    /**
     * Retry sending a failed notification
     * @param notification The notification to retry
     * @return true if retry was successful, false otherwise
     */
    boolean retryEmail(Notification notification);
}
