package com.eventbooking.notification.service;

import com.eventbooking.notification.dto.CreateTemplateRequest;
import com.eventbooking.notification.dto.TemplateDto;
import com.eventbooking.notification.entity.TemplateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TemplateService {
    
    /**
     * Create a new notification template
     */
    TemplateDto createTemplate(CreateTemplateRequest request, UUID createdBy);
    
    /**
     * Get template by ID
     */
    TemplateDto getTemplate(UUID templateId);
    
    /**
     * Get template by name
     */
    TemplateDto getTemplateByName(String name);
    
    /**
     * Update template
     */
    TemplateDto updateTemplate(UUID templateId, CreateTemplateRequest request);
    
    /**
     * List templates with pagination
     */
    Page<TemplateDto> listTemplates(TemplateCategory category, Boolean isActive, Pageable pageable);
    
    /**
     * Activate or deactivate template
     */
    void setTemplateActive(UUID templateId, boolean active);
    
    /**
     * Delete template
     */
    void deleteTemplate(UUID templateId);
}
