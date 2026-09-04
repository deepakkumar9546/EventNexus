package com.eventbooking.notification.repository;

import com.eventbooking.notification.entity.NotificationTemplate;
import com.eventbooking.notification.entity.TemplateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
    
    Optional<NotificationTemplate> findByName(String name);
    
    Optional<NotificationTemplate> findByNameAndIsActiveTrue(String name);
    
    Page<NotificationTemplate> findByCategory(TemplateCategory category, Pageable pageable);
    
    Page<NotificationTemplate> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<NotificationTemplate> findByCategoryAndIsActive(TemplateCategory category, Boolean isActive, Pageable pageable);
    
    boolean existsByName(String name);
}
