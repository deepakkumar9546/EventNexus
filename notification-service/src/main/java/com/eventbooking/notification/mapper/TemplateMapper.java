package com.eventbooking.notification.mapper;

import com.eventbooking.notification.dto.TemplateDto;
import com.eventbooking.notification.entity.NotificationTemplate;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {
    
    public TemplateDto toDto(NotificationTemplate template) {
        if (template == null) {
            return null;
        }
        
        TemplateDto dto = new TemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setSubject(template.getSubject());
        dto.setCategory(template.getCategory());
        dto.setVersion(template.getVersion());
        dto.setIsActive(template.getIsActive());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        
        return dto;
    }
}
