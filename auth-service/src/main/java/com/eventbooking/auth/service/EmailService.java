package com.eventbooking.auth.service;

public interface EmailService {
    
    /**
     * Send email verification message
     */
    void sendEmailVerification(String email, String firstName, String verificationToken);
    
    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(String email, String firstName, String resetToken);
    
    /**
     * Send password change confirmation
     */
    void sendPasswordChangeConfirmation(String email, String firstName);
    
    /**
     * Send account lock notification
     */
    void sendAccountLockNotification(String email, String firstName);
}