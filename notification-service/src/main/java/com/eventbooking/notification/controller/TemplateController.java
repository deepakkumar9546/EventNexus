package com.eventbooking.notification.controller;

import com.eventbooking.common.dto.ApiResponse;
import com.eventbooking.notification.dto.CreateTemplateRequest;
import com.eventbooking.notification.dto.TemplateDto;
import com.eventbooking.notification.entity.TemplateCategory;
import com.eventbooking.notification.service.TemplateService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/templates")
public class TemplateController {
    
    private static final Logger log = LoggerFactory.getLogger(TemplateController.class);
    
    private final TemplateService templateService;
    
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateDto>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        log.info("Creating template: {}", request.getName());
        
        // In production, get user ID from JWT token
        UUID createdBy = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        
        TemplateDto template = templateService.createTemplate(request, createdBy);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(template));
    }
    
    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateDto>> getTemplate(@PathVariable UUID templateId) {
        log.info("Fetching template: {}", templateId);
        
        TemplateDto template = templateService.getTemplate(templateId);
        
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<TemplateDto>> getTemplateByName(@PathVariable String name) {
        log.info("Fetching template by name: {}", name);
        
        TemplateDto template = templateService.getTemplateByName(name);
        
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TemplateDto>>> listTemplates(
            @RequestParam(required = false) TemplateCategory category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Listing templates - category: {}, active: {}", category, active);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TemplateDto> templates = templateService.listTemplates(category, active, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    @PutMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateDto>> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody CreateTemplateRequest request) {
        log.info("Updating template: {}", templateId);
        
        TemplateDto template = templateService.updateTemplate(templateId, request);
        
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    @PatchMapping("/{templateId}/active")
    public ResponseEntity<ApiResponse<Void>> setTemplateActive(
            @PathVariable UUID templateId,
            @RequestParam boolean active) {
        log.info("Setting template {} active status to: {}", templateId, active);
        
        templateService.setTemplateActive(templateId, active);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @DeleteMapping("/{templateId}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID templateId) {
        log.info("Deleting template: {}", templateId);
        
        templateService.deleteTemplate(templateId);
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
