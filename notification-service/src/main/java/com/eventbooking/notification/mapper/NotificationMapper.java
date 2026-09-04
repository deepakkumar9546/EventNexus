package com.eventbooking.notification.mapper;

import com.eventbooking.notification.dto.NotificationDto;
import com.eventbooking.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setRecipientEmail(notification.getRecipientEmail());
        dto.setSubject(notification.getSubject());
        dto.setStatus(notification.getStatus());
        dto.setChannel(notification.getChannel());
        dto.setRetryCount(notification.getRetryCount());
        dto.setSentAt(notification.getSentAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        dto.setFailedAt(notification.getFailedAt());
        dto.setErrorMessage(notification.getErrorMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        
        return dto;
    }
}
