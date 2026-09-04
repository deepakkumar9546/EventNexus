package com.eventbooking.notification.service;

import com.eventbooking.notification.dto.CreateTemplateRequest;
import com.eventbooking.notification.dto.TemplateDto;
import com.eventbooking.notification.entity.NotificationTemplate;
import com.eventbooking.notification.entity.TemplateCategory;
import com.eventbooking.notification.exception.TemplateAlreadyExistsException;
import com.eventbooking.notification.exception.TemplateNotFoundException;
import com.eventbooking.notification.mapper.TemplateMapper;
import com.eventbooking.notification.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TemplateServiceImpl implements TemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
    
    private final NotificationTemplateRepository templateRepository;
    private final TemplateMapper templateMapper;
    
    public TemplateServiceImpl(NotificationTemplateRepository templateRepository,
                               TemplateMapper templateMapper) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
    }
    
    @Override
    @Transactional
    public TemplateDto createTemplate(CreateTemplateRequest request, UUID createdBy) {
        log.info("Creating notification template: {}", request.getName());
        
        // Check if template already exists
        if (templateRepository.existsByName(request.getName())) {
            throw new TemplateAlreadyExistsException("Template already exists: " + request.getName());
        }
        
        NotificationTemplate template = new NotificationTemplate();
        template.setName(request.getName());
        template.setSubject(request.getSubject());
        template.setHtmlContent(request.getHtmlContent());
        template.setTextContent(request.getTextContent());
        template.setCategory(request.getCategory());
        template.setCreatedBy(createdBy);
        template.setVersion(1);
        template.setIsActive(true);
        
        NotificationTemplate saved = templateRepository.save(template);
        log.info("Template created successfully: {}", saved.getId());
        
        return templateMapper.toDto(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TemplateDto getTemplate(UUID templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateId));
        
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TemplateDto getTemplateByName(String name) {
        NotificationTemplate template = templateRepository.findByName(name)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + name));
        
        return templateMapper.toDto(template);
    }
    
    @Override
    @Transactional
    public TemplateDto updateTemplate(UUID templateId, CreateTemplateRequest request) {
        log.info("Updating template: {}", templateId);
        
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateId));
        
        template.setSubject(request.getSubject());
        template.setHtmlContent(request.getHtmlContent());
        template.setTextContent(request.getTextContent());
        template.setCategory(request.getCategory());
        template.setVersion(template.getVersion() + 1);
        
        NotificationTemplate updated = templateRepository.save(template);
        log.info("Template updated successfully: {}", updated.getId());
        
        return templateMapper.toDto(updated);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TemplateDto> listTemplates(TemplateCategory category, Boolean isActive, Pageable pageable) {
        Page<NotificationTemplate> templates;
        
        if (category != null && isActive != null) {
            templates = templateRepository.findByCategoryAndIsActive(category, isActive, pageable);
        } else if (category != null) {
            templates = templateRepository.findByCategory(category, pageable);
        } else if (isActive != null) {
            templates = templateRepository.findByIsActive(isActive, pageable);
        } else {
            templates = templateRepository.findAll(pageable);
        }
        
        return templates.map(templateMapper::toDto);
    }
    
    @Override
    @Transactional
    public void setTemplateActive(UUID templateId, boolean active) {
        log.info("Setting template {} active status to: {}", templateId, active);
        
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + templateId));
        
        template.setIsActive(active);
        templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(UUID templateId) {
        log.info("Deleting template: {}", templateId);
        
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }
        
        templateRepository.deleteById(templateId);
        log.info("Template deleted successfully: {}", templateId);
    }
}
