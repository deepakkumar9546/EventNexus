package com.eventbooking.notification.exception;

public class TemplateAlreadyExistsException extends RuntimeException {
    
    public TemplateAlreadyExistsException(String message) {
        super(message);
    }
    
    public TemplateAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
