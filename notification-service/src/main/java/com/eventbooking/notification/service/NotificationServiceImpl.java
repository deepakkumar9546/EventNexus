package com.eventbooking.notification.service;

import com.eventbooking.notification.dto.NotificationDto;
import com.eventbooking.notification.dto.SendNotificationRequest;
import com.eventbooking.notification.entity.Notification;
import com.eventbooking.notification.entity.NotificationStatus;
import com.eventbooking.notification.entity.NotificationTemplate;
import com.eventbooking.notification.exception.NotificationNotFoundException;
import com.eventbooking.notification.exception.TemplateNotFoundException;
import com.eventbooking.notification.mapper.NotificationMapper;
import com.eventbooking.notification.repository.NotificationRepository;
import com.eventbooking.notification.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    private final NotificationMapper notificationMapper;
    
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationTemplateRepository templateRepository,
                                   EmailService emailService,
                                   NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.emailService = emailService;
        this.notificationMapper = notificationMapper;
    }
    
    @Override
    @Transactional
    public NotificationDto sendNotification(SendNotificationRequest request) {
        log.info("Sending notification using template {} to user {}", 
                 request.getTemplateName(), request.getUserId());
        
        // Get template
        NotificationTemplate template = templateRepository
                .findByNameAndIsActiveTrue(request.getTemplateName())
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Active template not found: " + request.getTemplateName()));
        
        // Render template with variables
        String renderedSubject = renderTemplate(template.getSubject(), request.getVariables());
        String renderedHtml = renderTemplate(template.getHtmlContent(), request.getVariables());
        String renderedText = renderTemplate(template.getTextContent(), request.getVariables());
        
        // Create notification
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setTemplate(template);
        notification.setRecipientEmail(request.getRecipientEmail());
        notification.setSubject(renderedSubject);
        notification.setHtmlContent(renderedHtml);
        notification.setTextContent(renderedText);
        notification.setStatus(NotificationStatus.PENDING);
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: {}", saved.getId());
        
        // Send email asynchronously
        boolean sent = emailService.sendEmail(saved);
        
        if (sent) {
            log.info("Notification sent successfully: {}", saved.getId());
        } else {
            log.warn("Failed to send notification: {}", saved.getId());
        }
        
        return notificationMapper.toDto(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));
        
        return notificationMapper.toDto(notification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(UUID userId, NotificationStatus status, Pageable pageable) {
        Page<Notification> notifications;
        
        if (status != null) {
            notifications = notificationRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            notifications = notificationRepository.findByUserId(userId, pageable);
        }
        
        return notifications.map(notificationMapper::toDto);
    }
    
    @Override
    @Transactional
    public NotificationDto resendNotification(UUID notificationId) {
        log.info("Resending notification: {}", notificationId);
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));
        
        // Reset status for retry
        notification.setStatus(NotificationStatus.PENDING);
        notification.setErrorMessage(null);
        notification.setErrorCode(null);
        notification.setFailedAt(null);
        
        notificationRepository.save(notification);
        
        // Retry sending
        emailService.retryEmail(notification);
        
        return notificationMapper.toDto(notification);
    }
    
    @Override
    @Transactional
    public void processPendingNotifications() {
        log.debug("Processing pending notifications");
        
        // Find failed notifications that haven't exceeded max retries
        List<Notification> pendingNotifications = notificationRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3);
        
        log.info("Found {} pending notifications to retry", pendingNotifications.size());
        
        for (Notification notification : pendingNotifications) {
            try {
                emailService.retryEmail(notification);
            } catch (Exception e) {
                log.error("Error retrying notification {}: {}", 
                          notification.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Simple template rendering - replaces {{variable}} with values
     */
    private String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            
            if (value != null) {
                result = result.replace("{{" + matcher.group(1) + "}}", value.toString());
            }
        }
        
        return result;
    }
}
