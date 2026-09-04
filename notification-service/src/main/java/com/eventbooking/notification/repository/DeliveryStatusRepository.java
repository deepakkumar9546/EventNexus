package com.eventbooking.notification.repository;

import com.eventbooking.notification.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryStatusRepository extends JpaRepository<DeliveryStatus, UUID> {
    
    List<DeliveryStatus> findByNotificationIdOrderByEventTimestampDesc(UUID notificationId);
    
    Optional<DeliveryStatus> findTopByProviderMessageIdOrderByEventTimestampDesc(String providerMessageId);
    
    List<DeliveryStatus> findByProviderAndEventType(String provider, String eventType);
}
