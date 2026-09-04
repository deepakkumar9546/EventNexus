package com.eventbooking.notification.dto;

import com.eventbooking.notification.entity.TemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "HTML content is required")
    private String htmlContent;
    
    @NotBlank(message = "Text content is required")
    private String textContent;
    
    @NotNull(message = "Category is required")
    private TemplateCategory category;
    
    public CreateTemplateRequest() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getHtmlContent() {
        return htmlContent;
    }
    
    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    
    public String getTextContent() {
        return textContent;
    }
    
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
    
    public TemplateCategory getCategory() {
        return category;
    }
    
    public void setCategory(TemplateCategory category) {
        this.category = category;
    }
}
