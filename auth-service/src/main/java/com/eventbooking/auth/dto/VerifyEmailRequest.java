package com.eventbooking.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequest {
    
    @NotBlank(message = "Verification token is required")
    private String token;

    // Default constructor
    public VerifyEmailRequest() {}

    // Constructor
    public VerifyEmailRequest(String token) {
        this.token = token;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}