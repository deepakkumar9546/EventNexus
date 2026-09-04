package com.eventbooking.auth.dto;

import com.eventbooking.common.dto.UserDto;

public class LoginResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserDto user;

    // Default constructor
    public LoginResponse() {}

    // Constructor
    public LoginResponse(String accessToken, long expiresIn, UserDto user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }
}