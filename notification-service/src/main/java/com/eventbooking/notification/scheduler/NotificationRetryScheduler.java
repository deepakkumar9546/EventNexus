package com.eventbooking.notification.scheduler;

import com.eventbooking.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationRetryScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);
    
    private final NotificationService notificationService;
    
    public NotificationRetryScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Process pending notifications every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void retryFailedNotifications() {
        log.info("Starting scheduled retry of failed notifications");
        
        try {
            notificationService.processPendingNotifications();
        } catch (Exception e) {
            log.error("Error during scheduled notification retry: {}", e.getMessage(), e);
        }
        
        log.info("Completed scheduled retry of failed notifications");
    }
}
