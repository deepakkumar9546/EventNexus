package com.eventbooking.notification.repository;

import com.eventbooking.notification.entity.Notification;
import com.eventbooking.notification.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status, Pageable pageable);
    
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
    
    long countByUserIdAndStatus(UUID userId, NotificationStatus status);
}
