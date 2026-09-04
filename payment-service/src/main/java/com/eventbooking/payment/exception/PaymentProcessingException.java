package com.eventbooking.payment.exception;

public class PaymentProcessingException extends RuntimeException {
    
    private final String errorCode;
    private final String declineCode;
    
    public PaymentProcessingException(String message) {
        super(message);
        this.errorCode = null;
        this.declineCode = null;
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.declineCode = null;
    }
    
    public PaymentProcessingException(String message, String errorCode, String declineCode) {
        super(message);
        this.errorCode = errorCode;
        this.declineCode = declineCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getDeclineCode() {
        return declineCode;
    }
}
