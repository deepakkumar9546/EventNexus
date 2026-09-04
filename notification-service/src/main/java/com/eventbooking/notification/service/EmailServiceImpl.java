package com.eventbooking.notification.service;

import com.eventbooking.notification.entity.DeliveryStatus;
import com.eventbooking.notification.entity.Notification;
import com.eventbooking.notification.entity.NotificationStatus;
import com.eventbooking.notification.repository.DeliveryStatusRepository;
import com.eventbooking.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailServiceImpl(JavaMailSender mailSender,
                            NotificationRepository notificationRepository,
                            DeliveryStatusRepository deliveryStatusRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
        this.deliveryStatusRepository = deliveryStatusRepository;
    }
    
    @Override
    @Transactional
    public boolean sendEmail(Notification notification) {
        try {
            log.info("Sending email notification {} to {}", 
                     notification.getId(), notification.getRecipientEmail());
            
            // Create MIME message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getTextContent(), notification.getHtmlContent());
            
            // Send email
            mailSender.send(message);
            
            // Update notification status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);
            
            // Record delivery status
            DeliveryStatus deliveryStatus = new DeliveryStatus();
            deliveryStatus.setNotificationId(notification.getId());
            deliveryStatus.setProvider("SMTP");
            deliveryStatus.setStatus("SENT");
            deliveryStatus.setEventType("send");
            deliveryStatus.setEventTimestamp(Instant.now());
            deliveryStatusRepository.save(deliveryStatus);
            
            log.info("Email sent successfully for notification {}", notification.getId());
            return true;
            
        } catch (MessagingException e) {
            log.error("Failed to send email for notification {}: {}", 
                      notification.getId(), e.getMessage(), e);
            
            // Update notification with error
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(Instant.now());
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending email for notification {}: {}", 
                      notification.getId(), e.getMessage(), e);
            
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(Instant.now());
            notification.setErrorMessage("Unexpected error: " + e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
            
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean retryEmail(Notification notification) {
        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            log.warn("Max retries exceeded for notification {}", notification.getId());
            notification.setStatus(NotificationStatus.PERMANENTLY_FAILED);
            notificationRepository.save(notification);
            return false;
        }
        
        log.info("Retrying email notification {} (attempt {}/{})", 
                 notification.getId(), 
                 notification.getRetryCount() + 1, 
                 notification.getMaxRetries());
        
        return sendEmail(notification);
    }
}
